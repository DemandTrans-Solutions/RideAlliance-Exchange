package com.clearinghouse.service;

import com.clearinghouse.entity.TripTicket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for executing reporting/charting queries based on extracted parameters.
 */
@Slf4j
@Service
public class ReportingService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Executes a reporting/charting query based on extracted parameters.
     *
     * @param parameters Map of extracted parameters (e.g., chartType, aggregation, field, dateRange)
     * @return List of result data (stub)
     */
    public List<Map<String, Object>> executeReportingQuery(Map<String, Object> parameters) {
        log.info("Executing reporting/charting query with parameters: {}", parameters);
        // Whitelist of allowed fields for group by and filtering
        final List<String> allowedFields = List.of(
                "requestedPickupDate", "requestedDropoffDate", "purpose", "status", "originProvider", "customerFirstName", "customerLastName", "estimatedTripDistance", "serviceLevel"
        );
        String aggregation = (String) parameters.getOrDefault("aggregation", "count");
        String groupByField = (String) parameters.getOrDefault("groupBy", null);
        if (groupByField == null) {
            log.error("Missing required 'groupBy' parameter in reporting query parameters: {}", parameters);
            throw new IllegalArgumentException("Missing required 'groupBy' parameter in reporting query parameters");
        }
        // Add runtime checks to ensure type safety for the filters map
        Map<String, Object> filters = safeCastToMap(parameters.getOrDefault("filters", Map.of()));

        // Validate aggregation
        if (!"count".equalsIgnoreCase(aggregation)) {
            throw new IllegalArgumentException("Unsupported aggregation: " + aggregation);
        }
        // Validate groupBy field
        if (!allowedFields.contains(groupByField)) {
            throw new IllegalArgumentException("Unsupported group by field: " + groupByField);
        }
        // Validate filters
        for (String field : filters.keySet()) {
            if (!allowedFields.contains(field)) {
                throw new IllegalArgumentException("Unsupported filter field: " + field);
            }
        }
        // Sanitize filter values (basic: no SQL injection, only primitives or allowed types)
        for (Object value : filters.values()) {
            if (!(value instanceof String || value instanceof Number || value instanceof Boolean || (value instanceof List<?> list && list.size() == 2 && list.get(0) instanceof Comparable<?> && list.get(1) instanceof Comparable<?>))) {
                throw new IllegalArgumentException("Unsupported filter value type: " + value);
            }
        }

        // If dateRange is present, add it as a filter on the groupBy field if groupBy is a date field
        if (parameters.containsKey("dateRange") && groupByField != null && (groupByField.toLowerCase().contains("date") || groupByField.toLowerCase().contains("time"))) {
            Object dateRangeObj = parameters.get("dateRange");
            if (dateRangeObj instanceof Map<?, ?> dateRangeMap) {
                Object start = dateRangeMap.get("start");
                Object end = dateRangeMap.get("end");
                if (start != null && end != null) {
                    log.info("Applying dateRange filter to groupBy field {}: start={}, end={}", groupByField, start, end);
                    filters = new java.util.HashMap<>(filters); // ensure mutable
                    filters.put(groupByField, List.of(start, end));
                }
            }
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<TripTicket> root = cq.from(TripTicket.class);

        Path<?> groupByPath = root.get(groupByField);
        Expression<?> aggregationExpr;
        if ("count".equalsIgnoreCase(aggregation)) {
            aggregationExpr = cb.count(root);
        } else {
            throw new IllegalArgumentException("Unsupported aggregation: " + aggregation);
        }

        List<Predicate> predicates = new ArrayList<>();
        // Support multiple filters (AND logic)
        // Integrate convertToSqlDate utility method into filter logic
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            if (!allowedFields.contains(field)) {
                throw new IllegalArgumentException("Unsupported filter field: " + field);
            }
            Path<?> path = root.get(field);
            if (value instanceof List<?> listValue && listValue.size() == 2 && listValue.get(0) instanceof String && listValue.get(1) instanceof String) {
                // Convert date range values to java.sql.Date
                try {
                    java.sql.Date startDate = convertToSqlDate((String) listValue.get(0));
                    java.sql.Date endDate = convertToSqlDate((String) listValue.get(1));
                    // Ensure type safety for datePath
                    if (path.getJavaType().equals(java.sql.Date.class)) {
                        Expression<java.sql.Date> datePath = path.as(java.sql.Date.class);
                        predicates.add(cb.between(datePath, startDate, endDate));
                    } else {
                        throw new IllegalArgumentException("Field " + field + " is not of type java.sql.Date");
                    }
                } catch (Exception e) {
                    log.error("Failed to convert date range values for field {}: {}", field, e.getMessage());
                    throw new IllegalArgumentException("Invalid date range values for field: " + field);
                }
            } else if (value instanceof List<?> listValue && listValue.size() == 2 && listValue.get(0) instanceof Number startValue && listValue.get(1) instanceof Number endValue) {
                // Add support for numeric range filters
                // Ensure type compatibility for numeric range filters
                try {
                    // Explicitly convert numeric range values to match the type of numberPath
                    Expression<Double> doublePath = path.as(Double.class);
                    predicates.add(cb.between(doublePath, startValue.doubleValue(), endValue.doubleValue()));
                } catch (Exception e) {
                    log.error("Failed to process numeric range values for field {}: {}", field, e.getMessage());
                    throw new IllegalArgumentException("Invalid numeric range values for field: " + field);
                }
            } else if (value instanceof String) {
                predicates.add(cb.equal(path, value));
            } else if (value instanceof Number) {
                predicates.add(cb.equal(path, value));
            } else if (value instanceof Boolean) {
                predicates.add(cb.equal(path, value));
            } else {
                throw new IllegalArgumentException("Unsupported filter value type for field: " + field);
            }
        }

        cq.multiselect(groupByPath, aggregationExpr)
                .where(predicates.toArray(new Predicate[0]))
                .groupBy(groupByPath)
                .orderBy(cb.asc(groupByPath));

        try {
            List<Object[]> results = entityManager.createQuery(cq).getResultList();
            log.info("Raw query results from getResultList: {}", results);
            List<Map<String, Object>> mappedResults = new ArrayList<>();
            for (Object[] row : results) {
                if (row[0] == null || row[1] == null) {
                    log.warn("Skipping result row with null groupBy or aggregation value: {}", (Object) row);
                    continue;
                }
                mappedResults.add(Map.of(
                        groupByField, row[0],
                        aggregation, row[1]
                ));
            }
            log.info("Reporting query executed successfully. Result size: {}", mappedResults.size());
            return mappedResults;
        } catch (Exception e) {
            log.error("Reporting query execution failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Generates a chart image (PNG) using JFreeChart based on the result data and chart parameters.
     *
     * @param data       List of result data (e.g., from executeReportingQuery)
     * @param parameters Map of chart parameters (e.g., chartType, title, xField, yField)
     * @return byte[] PNG image bytes
     */
    public byte[] generateChartImage(List<Map<String, Object>> data, Map<String, Object> parameters) {
        log.info("Generating chart image. Chart type: {}, Parameters: {}", parameters.getOrDefault("chartType", "bar"), parameters);
        String chartType = ((String) parameters.getOrDefault("chartType", "bar")).toLowerCase();
        String title = (String) parameters.getOrDefault("title", "Report Chart");
        String xField = (String) parameters.getOrDefault("xField", "date");
        String yField = (String) parameters.getOrDefault("yField", "count");

        if (!List.of("bar", "line", "pie").contains(chartType)) {
            throw new IllegalArgumentException("Unsupported chart type: " + chartType);
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("No data provided for chart generation");
        }

        org.jfree.chart.JFreeChart chart;
        switch (chartType) {
            case "line": {
                org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
                for (Map<String, Object> row : data) {
                    Object x = row.get(xField);
                    Object y = row.get(yField);
                    if (x != null && y instanceof Number) {
                        dataset.addValue(((Number) y).doubleValue(), yField, x.toString());
                    }
                }
                chart = org.jfree.chart.ChartFactory.createLineChart(
                        title, xField, yField, dataset,
                        org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);
                break;
            }
            case "pie": {
                org.jfree.data.general.DefaultPieDataset<String> dataset = new org.jfree.data.general.DefaultPieDataset<>();
                for (Map<String, Object> row : data) {
                    Object x = row.get(xField);
                    Object y = row.get(yField);
                    if (x != null && y instanceof Number) {
                        dataset.setValue(x.toString(), ((Number) y).doubleValue());
                    }
                }
                chart = org.jfree.chart.ChartFactory.createPieChart(title, dataset, false, true, false);
                break;
            }
            case "bar":
            default: {
                org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
                for (Map<String, Object> row : data) {
                    Object x = row.get(xField);
                    Object y = row.get(yField);
                    if (x != null && y instanceof Number) {
                        // Ensure Y-axis values are integers for bar chart
                        dataset.addValue(((Number) y).intValue(), yField, x.toString());
                    }
                }
                chart = org.jfree.chart.ChartFactory.createBarChart(
                        title, xField, yField, dataset,
                        org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);

                // After creating the bar chart, force integer ticks on the Y axis
                org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
                org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
                rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());
            }
        }

        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            org.jfree.chart.ChartUtils.writeChartAsPNG(out, chart, 800, 600);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate chart image", e);
        }
    }

    /**
     * Generates a chart image file (PNG or JPG) using JFreeChart based on the result data and chart parameters.
     *
     * @param data       List of result data (e.g., from executeReportingQuery)
     * @param parameters Map of chart parameters (e.g., chartType, title, xField, yField, format)
     * @return File object pointing to the generated image file
     */
    public java.io.File generateChartImageFile(List<Map<String, Object>> data, Map<String, Object> parameters) {
        log.info("Generating chart image file. Chart type: {}, Format: {}, Parameters: {}", parameters.getOrDefault("chartType", "bar"), parameters.getOrDefault("format", "png"), parameters);
        String chartType = ((String) parameters.getOrDefault("chartType", "bar")).toLowerCase();
        String title = (String) parameters.getOrDefault("title", "Report Chart");
        String xField = (String) parameters.getOrDefault("xField", "date");
        String yField = (String) parameters.getOrDefault("yField", "count");
        String format = (String) parameters.get("format");
        if (format == null || format.isBlank()) format = "png";
        format = format.toLowerCase();
        String ext = format.equals("jpg") ? "jpg" : "png";

        if (!List.of("bar", "line", "pie").contains(chartType)) {
            throw new IllegalArgumentException("Unsupported chart type: " + chartType);
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("No data provided for chart generation");
        }

        log.info("[chart] Using xField: {}, yField: {}", xField, yField);
        log.info("[chart] First 3 data rows: {}", data.stream().limit(3).toList());
        int missing = 0;
        for (Map<String, Object> row : data) {
            if (!row.containsKey(xField) || !row.containsKey(yField)) {
                log.warn("[chart] Row missing xField or yField: {}", row);
                missing++;
            }
        }
        if (missing > 0) {
            log.warn("[chart] {} rows missing xField or yField (xField={}, yField={})", missing, xField, yField);
        }

        org.jfree.chart.JFreeChart chart;
        switch (chartType) {
            case "line": {
                org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
                for (Map<String, Object> row : data) {
                    Object x = row.get(xField);
                    Object y = row.get(yField);
                    if (x != null && y instanceof Number) {
                        dataset.addValue(((Number) y).doubleValue(), yField, x.toString());
                    }
                }
                chart = org.jfree.chart.ChartFactory.createLineChart(
                        title, xField, yField, dataset,
                        org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);
                break;
            }
            case "pie": {
                org.jfree.data.general.DefaultPieDataset<String> dataset = new org.jfree.data.general.DefaultPieDataset<>();
                for (Map<String, Object> row : data) {
                    Object x = row.get(xField);
                    Object y = row.get(yField);
                    if (x != null && y instanceof Number) {
                        dataset.setValue(x.toString(), ((Number) y).doubleValue());
                    }
                }
                chart = org.jfree.chart.ChartFactory.createPieChart(title, dataset, false, true, false);
                break;
            }
            case "bar":
            default: {
                org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
                for (Map<String, Object> row : data) {
                    Object x = row.get(xField);
                    Object y = row.get(yField);
                    if (x != null && y instanceof Number) {
                        // Ensure Y-axis values are integers for bar chart
                        dataset.addValue(((Number) y).intValue(), yField, x.toString());
                    }
                }
                chart = org.jfree.chart.ChartFactory.createBarChart(
                        title, xField, yField, dataset,
                        org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);

                // After creating the bar chart, force integer ticks on the Y axis
                org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
                org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
                rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());
            }
        }

        int chartWidth = 2400;
        int chartHeight = 1800;
        try {
            java.io.File tempFile = java.io.File.createTempFile("report_chart_", "." + ext);
            tempFile.deleteOnExit();
            if (ext.equals("jpg")) {
                org.jfree.chart.ChartUtils.writeChartAsJPEG(new java.io.FileOutputStream(tempFile), chart, chartWidth, chartHeight);
            } else {
                org.jfree.chart.ChartUtils.writeChartAsPNG(new java.io.FileOutputStream(tempFile), chart, chartWidth, chartHeight);
            }
            return tempFile;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate chart image file", e);
        }
    }

    private java.sql.Date convertToSqlDate(String dateString) {
        try {
            return java.sql.Date.valueOf(dateString);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert String to java.sql.Date: {}", dateString, e);
            throw new IllegalArgumentException("Invalid date format: " + dateString, e);
        }
    }

    // Refactor to eliminate type safety warnings
    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> safeCastToMap(Object obj) {
        if (obj instanceof Map<?, ?>) {
            return (Map<K, V>) obj;
        } else {
            throw new IllegalArgumentException("Expected a Map but found: " + obj.getClass().getName());
        }
    }
}

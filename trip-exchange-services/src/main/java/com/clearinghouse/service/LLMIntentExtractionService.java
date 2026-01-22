package com.clearinghouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for LLM-based intent extraction and query/code generation via AWS Bedrock.
 * This service classifies user queries and extracts reporting/charting parameters.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LLMIntentExtractionService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Classifies the user query as either VECTOR_SEARCH or REPORTING_CHARTING.
     * Calls AWS Bedrock LLM to perform classification.
     *
     * @param query The user query string
     * @return "VECTOR_SEARCH" or "REPORTING_CHARTING"
     */
    public String classifyQueryType(String query) {
        log.info("Classifying query type for user query: {}", query);
        try {
            String prompt = "Classify the following user query as either VECTOR_SEARCH or REPORTING_CHARTING. " +
                    "Return only one of these two strings.\nQuery: " + query;
            String response = chatClient.prompt().user(prompt).call().content();
            if (response != null) {
                response = response.trim();
                if (response.equalsIgnoreCase("VECTOR_SEARCH") || response.equalsIgnoreCase("REPORTING_CHARTING")) {
                    log.info("LLM classified query as: {}", response.toUpperCase());
                    return response.toUpperCase();
                }
                log.warn("LLM returned unexpected classification: {}. Falling back to VECTOR_SEARCH.", response);
            } else {
                log.warn("LLM returned null for classification. Falling back to VECTOR_SEARCH.");
            }
            return "VECTOR_SEARCH";
        } catch (Exception e) {
            log.error("LLM classifyQueryType failed, falling back to stub logic: {}", e.getMessage(), e);
            if (query != null && (query.toLowerCase().contains("chart") || query.toLowerCase().contains("report"))) {
                return "REPORTING_CHARTING";
            }
            return "VECTOR_SEARCH";
        }
    }

    /**
     * Extracts reporting/charting parameters from the user query using LLM (Bedrock).
     *
     * @param query The user query string
     * @return Map of extracted parameters (e.g., chartType, aggregation, fields, dateRange)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> extractReportingParameters(String query) {
        log.info("Extracting reporting/charting parameters for user query: {}", query);
        // Allowed groupBy fields for prompt and validation
        List<String> allowedFields = List.of(
                "requestedPickupDate", "requestedDropoffDate", "purpose", "status", "originProvider", "customerFirstName", "customerLastName", "estimatedTripDistance", "serviceLevel"
        );
        try {
            String prompt = "Extract the reporting/charting parameters from the following user query. " +
                    "For the groupBy field, ONLY use one of: " + allowedFields + ". " +
                    "Return a JSON object with keys such as chartType, aggregation, groupBy, dateRange, and format if specified.\nQuery: " + query;
            String response = chatClient.prompt().user(prompt).call().content();
            if (response != null) {
                response = response.trim();
                // Find the first JSON object in the response
                int start = response.indexOf('{');
                int end = response.lastIndexOf('}');
                if (start >= 0 && end > start) {
                    String json = response.substring(start, end + 1);
                    Map<String, Object> params = (Map<String, Object>) objectMapper.readValue(json, Map.class);
                    // Normalize: if 'groupBy' is missing but 'field' is present, use 'field' as 'groupBy'
                    if (!params.containsKey("groupBy") && params.containsKey("field")) {
                        params.put("groupBy", params.get("field"));
                        log.warn("LLM returned 'field' instead of 'groupBy'. Normalizing: field={} -> groupBy", params.get("field"));
                    }
                    // Validate groupBy value
                    Object groupBy = params.get("groupBy");
                    if (groupBy == null || !allowedFields.contains(groupBy)) {
                        log.warn("LLM returned invalid groupBy value: {}. Falling back to default: requestedPickupDate", groupBy);
                        params.put("groupBy", "requestedPickupDate");
                    }
                    log.info("LLM extracted parameters: {}", params);
                    // Validate extracted parameters
                    validateReportingParameters(params);
                    return params;
                } else {
                    log.warn("LLM did not return a valid JSON object for parameters. Falling back to stub map. Response: {}", response);
                }
            } else {
                log.warn("LLM returned null for parameter extraction. Falling back to stub map.");
            }
        } catch (Exception e) {
            log.error("LLM extractReportingParameters failed, falling back to stub map: {}", e.getMessage(), e);
        }
        // Fallback stub
        Map<String, Object> stub = Map.of(
                "chartType", "bar",
                "aggregation", "count",
                "groupBy", "requestedPickupDate",
                "dateRange", "2025-07-01 to 2025-07-31"
        );
        log.info("Using fallback stub parameters: {}", stub);
        validateReportingParameters(stub);
        return stub;
    }

    private void validateReportingParameters(Map<String, Object> params) {
        // Only allow known chart types, aggregations, and fields
        List<String> allowedChartTypes = List.of("bar", "line", "pie");
        List<String> allowedAggregations = List.of("count");
        List<String> allowedFields = List.of(
                "requestedPickupDate", "requestedDropoffDate", "purpose", "status", "originProvider", "customerFirstName", "customerLastName", "estimatedTripDistance", "serviceLevel"
        );
        String chartType = (String) params.getOrDefault("chartType", "bar");
        String aggregation = (String) params.getOrDefault("aggregation", "count");
        String groupBy = (String) params.getOrDefault("groupBy", "requestedPickupDate");
        if (!allowedChartTypes.contains(chartType.toLowerCase())) {
            log.warn("Validation failed: Unsupported chart type: {}", chartType);
            throw new IllegalArgumentException("Unsupported chart type: " + chartType);
        }
        if (!allowedAggregations.contains(aggregation.toLowerCase())) {
            log.warn("Validation failed: Unsupported aggregation: {}", aggregation);
            throw new IllegalArgumentException("Unsupported aggregation: " + aggregation);
        }
        if (!allowedFields.contains(groupBy)) {
            log.warn("Validation failed: Unsupported group by field: {}", groupBy);
            throw new IllegalArgumentException("Unsupported group by field: " + groupBy);
        }
        // Optionally validate filters, xField, yField, etc.
    }

    // Optionally, add method to generate backend query (SQL/JPQL/JPA method signature) via LLM
}

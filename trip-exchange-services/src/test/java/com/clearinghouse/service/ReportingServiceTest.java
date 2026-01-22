package com.clearinghouse.service;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReportingServiceTest {
    @Test
    public void testGenerateChartImage_barChart_returnsPngBytes() {
        ReportingService service = new ReportingService();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("date", "2025-07-01");
        row1.put("count", 10);
        data.add(row1);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("date", "2025-07-02");
        row2.put("count", 15);
        data.add(row2);
        Map<String, Object> params = new HashMap<>();
        params.put("chartType", "bar");
        params.put("title", "Test Chart");
        params.put("xField", "date");
        params.put("yField", "count");
        byte[] imageBytes = service.generateChartImage(data, params);
        assertNotNull(imageBytes);
        assertTrue(imageBytes.length > 1000, "Image bytes should be non-trivial size");
        // PNG signature check
        assertEquals((byte)137, imageBytes[0]);
        assertEquals((byte)80, imageBytes[1]);
        assertEquals((byte)78, imageBytes[2]);
        assertEquals((byte)71, imageBytes[3]);
    }

    @Test
    public void testGenerateChartImage_lineChart_returnsPngBytes() {
        ReportingService service = new ReportingService();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("date", "2025-07-01");
        row1.put("count", 10);
        data.add(row1);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("date", "2025-07-02");
        row2.put("count", 15);
        data.add(row2);
        Map<String, Object> params = new HashMap<>();
        params.put("chartType", "line");
        params.put("title", "Test Line Chart");
        params.put("xField", "date");
        params.put("yField", "count");
        byte[] imageBytes = service.generateChartImage(data, params);
        assertNotNull(imageBytes);
        assertTrue(imageBytes.length > 1000, "Image bytes should be non-trivial size");
        assertEquals((byte)137, imageBytes[0]);
        assertEquals((byte)80, imageBytes[1]);
        assertEquals((byte)78, imageBytes[2]);
        assertEquals((byte)71, imageBytes[3]);
    }

    @Test
    public void testGenerateChartImage_pieChart_returnsPngBytes() {
        ReportingService service = new ReportingService();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("date", "2025-07-01");
        row1.put("count", 10);
        data.add(row1);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("date", "2025-07-02");
        row2.put("count", 15);
        data.add(row2);
        Map<String, Object> params = new HashMap<>();
        params.put("chartType", "pie");
        params.put("title", "Test Pie Chart");
        params.put("xField", "date");
        params.put("yField", "count");
        byte[] imageBytes = service.generateChartImage(data, params);
        assertNotNull(imageBytes);
        assertTrue(imageBytes.length > 1000, "Image bytes should be non-trivial size");
        assertEquals((byte)137, imageBytes[0]);
        assertEquals((byte)80, imageBytes[1]);
        assertEquals((byte)78, imageBytes[2]);
        assertEquals((byte)71, imageBytes[3]);
    }

    @Test
    public void testGenerateChartImage_unsupportedChartType_throws() {
        ReportingService service = new ReportingService();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("date", "2025-07-01");
        row.put("count", 10);
        data.add(row);
        Map<String, Object> params = new HashMap<>();
        params.put("chartType", "scatter");
        params.put("xField", "date");
        params.put("yField", "count");
        assertThrows(IllegalArgumentException.class, () -> service.generateChartImage(data, params));
    }

    @Test
    public void testGenerateChartImage_noData_throws() {
        ReportingService service = new ReportingService();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("chartType", "bar");
        params.put("xField", "date");
        params.put("yField", "count");
        assertThrows(IllegalArgumentException.class, () -> service.generateChartImage(data, params));
    }
}

package com.clearinghouse.controller.rest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AssistantControllerDateNormalizationTest {
    @Test
    void testNormalizeDatesInQuery_variousFormats() {
        // mm/dd/yyyy
        assertEquals("The date is 2025-07-08", AssistantController.normalizeDatesInQuery("The date is 7/8/2025"));
        // mm-dd-yyyy
        assertEquals("The date is 2025-07-08", AssistantController.normalizeDatesInQuery("The date is 7-8-2025"));
        // m/d/yy
        assertEquals("The date is 2025-07-08", AssistantController.normalizeDatesInQuery("The date is 7/8/25"));
        // mm/dd/yy
        assertEquals("The date is 2025-07-08", AssistantController.normalizeDatesInQuery("The date is 07/08/25"));
        // mm-dd-yy
        assertEquals("The date is 2025-07-08", AssistantController.normalizeDatesInQuery("The date is 07-08-25"));
        // 2-digit year < 50
        assertEquals("The date is 2024-07-08", AssistantController.normalizeDatesInQuery("The date is 7/8/24"));
        // 2-digit year >= 50
        assertEquals("The date is 1950-07-08", AssistantController.normalizeDatesInQuery("The date is 7/8/50"));
        // Multiple dates
        assertEquals("From 2025-07-08 to 2025-08-09", AssistantController.normalizeDatesInQuery("From 7/8/2025 to 8/9/2025"));
        // No date
        assertEquals("No date here", AssistantController.normalizeDatesInQuery("No date here"));
    }
}

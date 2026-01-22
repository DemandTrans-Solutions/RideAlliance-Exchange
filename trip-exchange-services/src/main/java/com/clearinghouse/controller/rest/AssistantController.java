package com.clearinghouse.controller.rest;


import com.clearinghouse.dto.ChatQueryRecord;
import com.clearinghouse.service.LLMIntentExtractionService;
import com.clearinghouse.service.ReportingService;
import com.clearinghouse.service.TripTicketVectorStoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping(value = {"api/assistant"})
@Slf4j
@AllArgsConstructor
public class AssistantController {

    private final ChatClient chatClient;

    private final TripTicketVectorStoreService tripTicketVectorStoreService;

    private final LLMIntentExtractionService llmIntentExtractionService;

    private final ReportingService reportingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, PromptChatMemoryAdvisor> advisorMap =
            new ConcurrentHashMap<>();


    private final QuestionAnswerAdvisor questionAnswerAdvisor;


    @RequestMapping(value = {"/{userId}/inquire"}, method = RequestMethod.POST)
    public ResponseEntity<?> inquire(@PathVariable("userId") String user,
                                     @RequestBody ChatQueryRecord chatQuery) {
        log.debug("#inquire userId [" + user + "] query [" + chatQuery + "]");

        // Normalize dates in the query string
        String normalizedQuery = normalizeDatesInQuery(chatQuery.query());
        log.info("[inquire] Normalized query: {}", normalizedQuery);
        // Step 1: Classify query type using LLM
        //String queryType = llmIntentExtractionService.classifyQueryType(normalizedQuery);
        String queryType = chatQuery.queryType();
        log.info("[inquire] Detected query type: {}", queryType);
        if ("REPORTING_CHARTING".equals(queryType)) {
            // Step 2: Extract reporting/charting parameters using LLM
            var params = llmIntentExtractionService.extractReportingParameters(normalizedQuery);
            log.info("[inquire] Extracted reporting/charting parameters: {}", params);
            // Set xField and yField for charting if not present
            if (!params.containsKey("xField")) {
                String groupBy = (String) params.get("groupBy");
                if (groupBy != null) {
                    params.put("xField", groupBy);
                    log.info("[inquire] Setting xField to groupBy: {}", groupBy);
                }
            }
            if (!params.containsKey("yField")) {
                String aggregation = (String) params.getOrDefault("aggregation", "count");
                params.put("yField", aggregation);
                log.info("[inquire] Setting yField to aggregation: {}", aggregation);
            }
            // Step 3: Execute backend reporting/charting logic
            log.info("[inquire] About to execute reporting/charting logic with parameters: {}", params);
            Object result = reportingService.executeReportingQuery(params);
            log.info("[inquire] Reporting/charting logic executed. Result: {}", result);
            String chartType = (String) params.getOrDefault("chartType", "bar");
            log.info("[inquire] chartType: {}", chartType);
            boolean wantChart = chartType != null && !"none".equalsIgnoreCase(chartType);
            String format = (String) params.getOrDefault("format", "png");
            log.info("[inquire] Chart format: {}", format);
            log.info("[inquire] wantChart: {}", wantChart);
            if (wantChart) {
                log.info("[inquire] Generating chart image file with params: {} and result: {}", params, result);
                if (!(result instanceof List)) {
                    log.error("[inquire] Reporting query result is not a List, cannot generate chart. Result: {}", result);
                    return ResponseEntity.internalServerError().body("Reporting query result is not a List, cannot generate chart.");
                }
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;
                java.io.File imageFile = reportingService.generateChartImageFile(resultList, params);
                String ext = (format != null && format.equalsIgnoreCase("jpg")) ? "jpg" : "png";
                String filename = "report_chart." + ext;
                log.info("[inquire] Returning chart file: {} (ext: {})", filename, ext);
                Resource resource = new FileSystemResource(imageFile);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(ext.equals("jpg") ? MediaType.IMAGE_JPEG : MediaType.IMAGE_PNG)
                        .body(resource);
            } else {
                try {
                    log.info("[inquire] Returning data as JSON: {}", result);
                    String json = objectMapper.writeValueAsString(result);
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("type", "data", "data", objectMapper.readTree(json)));
                } catch (Exception e) {
                    log.error("[inquire] Failed to serialize data", e);
                    return ResponseEntity.internalServerError().body("Failed to serialize data: " + e.getMessage());
                }
            }
        }

        log.info("[inquire] Fallback (non-reporting) flow triggered for query: {}", normalizedQuery);

        var advisor = this.advisorMap.computeIfAbsent(user,
                k -> PromptChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build());

        String content = this.chatClient
                .prompt()
                .user(normalizedQuery)
                .advisors(advisor, questionAnswerAdvisor)
                .call()
                .content();
        return ResponseEntity.ok(content);
    }

    /**
     * Converts all date-like substrings in the input to yyyy-MM-dd format.
     * Supports m/d/yy, mm/dd/yyyy, m-d-yy, mm-dd-yyyy, etc.
     */
    protected static String normalizeDatesInQuery(String input) {
        if (input == null) return null;
        // Regex for common US date formats (m/d/yy, mm/dd/yyyy, m-d-yy, mm-dd-yyyy)
        String regex = "\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})\\b";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String month = matcher.group(1);
            String day = matcher.group(2);
            String year = matcher.group(3);
            // Normalize year to 4 digits
            if (year.length() == 2) {
                int y = Integer.parseInt(year);
                year = (y >= 50 ? "19" : "20") + year;
            }
            // Pad month and day
            String mm = month.length() == 1 ? "0" + month : month;
            String dd = day.length() == 1 ? "0" + day : day;
            String isoDate = year + "-" + mm + "-" + dd;
            matcher.appendReplacement(sb, isoDate);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    @RequestMapping(value = {"/load"}, method = RequestMethod.GET)
    public String load() {
        log.debug("#load");
        tripTicketVectorStoreService.populateVectorStoreFromReadOnlySummaries();
        log.debug("#load finished");
        return "Vector store loaded successfully";
    }


    @RequestMapping(value = {"/vectorstore/test"}, method = RequestMethod.GET)
    public String testVectorStore(@RequestParam(value = "query", required = false, defaultValue = "trip ticket") String query) {
        int count = tripTicketVectorStoreService.countVectorStoreResults(query);
        log.debug("#vectorstore/test query [{}] result count [{}]", query, count);
        return "Vector store returned " + count + " results for query: '" + query + "'";
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "alive"));
    }
}

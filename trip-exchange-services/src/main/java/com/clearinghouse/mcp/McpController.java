package com.clearinghouse.mcp;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mcp")
public class McpController {
/*
    @Autowired
    //private BedrockService bedrockService;

    @Value("${aws.bedrock.allowedModels}")
    private List<String> allowedModels;
    private Set<String> allowedModelSet;

    private static final Logger logger = LoggerFactory.getLogger(McpController.class);
    private final ConcurrentHashMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();

    @Value("${mcp.quota.maxRequestsPerMinute:60}")
    private int maxRequestsPerMinute;
    @Value("${mcp.quota.maxTokensPerRequest:2048}")
    private int maxTokensPerRequest;

    private static class RequestCounter {
        int count;
        long windowStart;
    }

    @PostConstruct
    public void init() {
        allowedModelSet = new HashSet<>();
        if (allowedModels != null) {
            for (Object modelObj : allowedModels) {
                if (modelObj instanceof String) {
                    allowedModelSet.add((String) modelObj);
                } else if (modelObj instanceof java.util.LinkedHashMap) {
                    Object modelId = ((java.util.LinkedHashMap<?, ?>) modelObj).get("modelId");
                    if (modelId instanceof String) {
                        allowedModelSet.add((String) modelId);
                    }
                }
            }
        }
    }
*/
    /*
    @PostMapping("/invoke")
    public ResponseEntity<ModelInvokeResponse> invokeModel(@RequestBody ModelInvokeRequest request) {
        ModelInvokeResponse response = new ModelInvokeResponse();
        String clientKey = "global"; // For demo, global rate limit. Replace with user/IP for per-user.
        long now = Instant.now().getEpochSecond();
        requestCounters.putIfAbsent(clientKey, new RequestCounter());
        RequestCounter counter = requestCounters.get(clientKey);
        synchronized (counter) {
            if (now - counter.windowStart >= 60) {
                counter.count = 0;
                counter.windowStart = now;
            }
            if (++counter.count > maxRequestsPerMinute) {
                response.setStatus("error");
                response.setError("Rate limit exceeded");
                logger.warn("[{}] Rate limit exceeded for client: {} at {}", now, clientKey, now);
                return ResponseEntity.status(429).body(response);
            }
        }
        if (request.getInputPayload() != null && request.getInputPayload().length() > maxTokensPerRequest) {
            response.setStatus("error");
            response.setError("Input payload too large");
            logger.warn("[{}] Input payload too large for client: {} ({} chars)", now, clientKey, request.getInputPayload().length());
            return ResponseEntity.badRequest().body(response);
        }
        if (!allowedModelSet.contains(request.getModelId())) {
            response.setStatus("error");
            response.setError("Model not allowed");
            logger.warn("[{}] Model not allowed: {} for client: {}", now, request.getModelId(), clientKey);
            return ResponseEntity.badRequest().body(response);
        }
        try {
            logger.info("[{}] Invoking model: {} for client: {} with payload: {}", now, request.getModelId(), clientKey, request.getInputPayload());
            String output = bedrockService.invokeModel(request.getModelId(), request.getInputPayload());
            response.setStatus("success");
            response.setOutput(output);
            logger.info("[{}] Model output for client {}: {}", now, clientKey, output);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setStatus("error");
            response.setError(e.getMessage());
            logger.error("[{}] Model invocation error for client {}: {}", now, clientKey, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(response);
        }
    }*/
}

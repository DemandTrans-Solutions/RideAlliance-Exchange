package com.clearinghouse.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

@Component
@Slf4j
public class HttpLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to initialize
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Wrap request and response so we can access the body multiple times
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        long startTime = System.currentTimeMillis();

        // Log request details before processing
        if (requestWrapper.getRequestURI() != null && !requestWrapper.getRequestURI().contains("/actuator")) {
            logRequest(requestWrapper);
        }

        // Continue with the filter chain
        chain.doFilter(requestWrapper, responseWrapper);

        // Log response details after processing
        if (requestWrapper.getRequestURI() != null && !requestWrapper.getRequestURI().contains("/actuator")) {
            logResponse(responseWrapper, System.currentTimeMillis() - startTime);
        }

        // Copy content back to the original response
        responseWrapper.copyBodyToResponse();
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        StringBuilder logMessage = new StringBuilder("\n");
        logMessage.append("============================ REQUEST ============================\n");
        logMessage.append("URI         : ").append(request.getRequestURI()).append("\n");
        logMessage.append("Method      : ").append(request.getMethod()).append("\n");
        logMessage.append("Client IP   : ").append(request.getRemoteAddr()).append("\n");

        // Log headers
        logMessage.append("Headers     : ").append("\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Skip logging authorization headers completely
            if ("authorization".equalsIgnoreCase(headerName) ||
                    "cookie".equalsIgnoreCase(headerName)) {
                logMessage.append("  ").append(headerName).append(": [REDACTED]\n");
            } else {
                logMessage.append("  ").append(headerName).append(": ")
                        .append(request.getHeader(headerName)).append("\n");
            }
        }

        // Log request parameters if any
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            logMessage.append("Parameters  : ").append("\n");
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                logMessage.append("  ").append(entry.getKey()).append(": ");
                String[] values = entry.getValue();
                if (values.length == 1) {
                    logMessage.append(values[0]);
                } else {
                    logMessage.append(String.join(", ", values));
                }
                logMessage.append("\n");
            }
        }

        // Log request body if available and not a multipart request
        String contentType = request.getContentType();
        if (contentType != null && !contentType.startsWith("multipart/form-data")) {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String bodyContent = new String(content);
                // Truncate if too long
                if (bodyContent.length() > 1000) {
                    bodyContent = bodyContent.substring(0, 1000) + "... [truncated]";
                }
                logMessage.append("Body        : ").append(bodyContent).append("\n");
            }
        }

        logMessage.append("============================ REQUEST END ========================");
        log.info(logMessage.toString());
    }

    private void logResponse(ContentCachingResponseWrapper response, long timeElapsed) {
        StringBuilder logMessage = new StringBuilder("\n");
        logMessage.append("============================ RESPONSE ===========================\n");
        logMessage.append("Status      : ").append(response.getStatus()).append("\n");
        logMessage.append("Time taken  : ").append(timeElapsed).append("ms\n");

        // Log response headers
        logMessage.append("Headers     : ").append("\n");
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            logMessage.append("  ").append(headerName).append(": ")
                    .append(response.getHeader(headerName)).append("\n");
        }

        // Log response body if available
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String contentType = response.getContentType();
            // Only log text-based content types
            if (contentType != null &&
                    (contentType.startsWith("text") || contentType.contains("json") || contentType.contains("xml"))) {
                String bodyContent = new String(content);
                // Truncate if too long
                if (bodyContent.length() > 1000) {
                    bodyContent = bodyContent.substring(0, 1000) + "... [truncated]";
                }
                logMessage.append("Body        : ").append(bodyContent).append("\n");
            } else {
                logMessage.append("Body        : [Binary content with length ").append(content.length).append("]\n");
            }
        }

        logMessage.append("============================ RESPONSE END =======================");
        log.info(logMessage.toString());
    }

    @Override
    public void destroy() {
        // Nothing to clean up
    }
}
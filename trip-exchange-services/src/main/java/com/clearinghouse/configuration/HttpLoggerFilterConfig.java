package com.clearinghouse.configuration;

import com.clearinghouse.filter.HttpLoggingFilter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class HttpLoggerFilterConfig {

    @Bean
    public FilterRegistrationBean<com.clearinghouse.filter.HttpLoggingFilter> loggingFilter() {
        FilterRegistrationBean<HttpLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpLoggingFilter());
        registrationBean.addUrlPatterns("/*");
        // Set lower order to ensure this filter runs early in the chain
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Slf4j
    @Configuration
    public static class McpServerConfig {
        @Value("${spring.ai.mcp.server.enabled:false}")
        private boolean mcpEnabled;

        @Value("${spring.ai.mcp.server.port:8081}")
        private int mcpPort;

        @Autowired
        private ApplicationContext applicationContext;

        @PostConstruct
        public void logMcpServerStatus() {
            if (mcpEnabled) {
                log.info("MCP Server is enabled and will listen on port {}", mcpPort);
            } else {
                log.info("MCP Server is disabled.");
            }
        }

        @EventListener(ApplicationReadyEvent.class)
        public void logMcpServerReady() {
            if (mcpEnabled) {
                log.info("MCP Server should now be accepting connections on port {}", mcpPort);
                // Find beans with methods annotated with @Tool
                String[] beanNames = applicationContext.getBeanDefinitionNames();
                List<String> toolBeans = Arrays.stream(beanNames)
                        .filter(name -> {
                            Object bean = applicationContext.getBean(name);
                            for (Method method : bean.getClass().getMethods()) {
                                if (method.isAnnotationPresent(Tool.class)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
                if (!toolBeans.isEmpty()) {
                    log.info("Beans with @Tool-annotated methods: {}", String.join(", ", toolBeans));
                } else {
                    log.warn("No beans with @Tool-annotated methods found!");
                }
            }
        }
    }
}
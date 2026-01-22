package com.clearinghouse.config;

import com.clearinghouse.service.UberAuthInterceptor;
import com.clearinghouse.service.UberTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UberRestTemplateConfig {

    @Bean
    public RestTemplate tokenRestTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "uberRestTemplate")
    public RestTemplate uberRestTemplate(UberTokenService tokenService) {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add(new UberAuthInterceptor(tokenService::getAccessToken));
        return rt;
    }
}

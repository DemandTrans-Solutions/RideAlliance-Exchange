package com.clearinghouse.configuration;

import com.clearinghouse.security.HmacTokenHandler;
import com.clearinghouse.service.TokenHandler;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HmacSecurityConfig {
    @Value("${token.secret}")
    private String secret;

    @Bean
    public HmacTokenHandler hmacTokenHandler() {
        return new HmacTokenHandler(DatatypeConverter.parseBase64Binary(secret));
    }


    @Bean
    public TokenHandler tokenHandler() {
        return new TokenHandler(DatatypeConverter.parseBase64Binary(secret));
    }
}

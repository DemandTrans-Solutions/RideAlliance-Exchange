package com.clearinghouse.configuration;

import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.filter.StatelessAuthenticationFilter;
import com.clearinghouse.filter.StatelessLoginFilter;
import com.clearinghouse.service.CustomUserDetailsService;
import com.clearinghouse.service.TokenAuthenticationService;
import com.clearinghouse.service.AuthResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class StatelessAuthenticationSecurityConfig {

    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;

    @Autowired
    private CorsFilter corsFilter;

    @Autowired
    private CustomUserDetailsService customerUserDetailsService;

    @Autowired
    private AuthResponseService authResponseService;

    @Autowired
    private UserDAO userDAO;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .requestMatchers("/", "/home", "/favicon.ico", "/resources/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/trip_tickets/generate-test-data").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/assistant/**").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/api/trip_tickets/**").permitAll()
                        //.requestMatchers(HttpMethod.POST, "/api/trip_tickets/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login", "/api/activateAccount", "api/login/validateUser",
                                "/api/checkToken/*", "/api/changePassword", "/api/forgotCrendential/*",
                                "/api/setPassword").permitAll()
                        // Secure MCP HTTP endpoint
                        .requestMatchers("/mcp/**", "/mcp/v1/**").permitAll()
                        //.requestMatchers("/api/**").permitAll()
                        .anyRequest().hasAnyRole("PROVIDERADMIN", "ADMIN", "READONLY", "PROVIDERUSER"))
                .addFilterBefore(corsFilter, HeaderWriterFilter.class)
                .addFilterBefore(new StatelessLoginFilter("/api/login", tokenAuthenticationService, customerUserDetailsService, authResponseService, userDAO, authenticationManagerBean(null)), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customerUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Configuration
    @EnableAsync
    public static class AsyncConfig {
    }
}
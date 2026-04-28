package com.cicd.analyzer.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allow specific origins instead of wildcard for credentials
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // Next.js development server (local)
                "http://localhost:3001",  // Alternative React port (local)
                "http://localhost:3002",
                "http://127.0.0.1:3000",  // Alternative localhost format
                "http://127.0.0.1:3001",  // Alternative localhost format
                "http://10.1.3.212:3000", // Local network
                "http://207.180.209.181:3000",  // Frontend client (production VPS)
                "http://207.180.209.181:3001"   // Frontend admin (production VPS)
        ));

        // Set max age for preflight requests
        corsConfig.setMaxAge(3600L);

        // Allow specific HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));

        // Allow specific headers
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Requested-With",
                "Cache-Control",
                "X-CSRF-Token",
                "X-User-Email"
        ));

        // Expose headers that the frontend can access
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Length",
                "X-Requested-With"
        ));

        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
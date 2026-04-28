package com.cicd.analyzer.pipelineorchestrator.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration Jackson
 * Note: Spring Boot 4.x configure automatiquement Jackson avec support LocalDateTime
 * Ce fichier est conservé pour d'éventuelles configurations futures
 */
@Configuration
public class JacksonConfig {
    // Configuration automatique par Spring Boot 4.x
    // Pas besoin de bean ObjectMapper personnalisé
}
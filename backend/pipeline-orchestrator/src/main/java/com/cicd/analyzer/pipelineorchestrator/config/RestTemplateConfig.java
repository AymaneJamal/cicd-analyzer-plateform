package com.cicd.analyzer.pipelineorchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration Spring pour les appels HTTP inter-microservices
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Bean RestTemplate pour appeler jenkins-connector et ai-analyzer
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
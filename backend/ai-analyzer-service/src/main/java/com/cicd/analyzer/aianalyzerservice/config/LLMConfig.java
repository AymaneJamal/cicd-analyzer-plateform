package com.cicd.analyzer.aianalyzerservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration Spring pour le module LLM
 * Gère l'injection des dépendances et la configuration générale
 */
@Configuration
public class LLMConfig {

    /**
     * Bean RestTemplate pour les appels HTTP aux APIs LLM
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

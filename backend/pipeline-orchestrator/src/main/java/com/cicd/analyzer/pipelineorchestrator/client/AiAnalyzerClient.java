package com.cicd.analyzer.pipelineorchestrator.client;

import com.cicd.analyzer.pipelineorchestrator.dto.response.OptimizationResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Client HTTP pour communiquer avec le microservice ai-analyzer (port 5002)
 * Encapsule l'appel REST vers l'endpoint d'optimisation IA
 */
@Service
@RequiredArgsConstructor
public class AiAnalyzerClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${microservices.ai-analyzer.url}")
    private String aiAnalyzerUrl;

    /**
     * POST /api/optimize
     * Envoie les données Jenkins au service AI pour optimisation
     *
     * @param buildStatistics JSON string des statistiques du build
     * @param pipelineConfig JSON string de la configuration du pipeline
     * @param buildLog Texte brut des logs
     * @param buildHistory JSON string de l'historique des builds
     * @param agentInfo JSON string des agents Jenkins
     * @param serverInfo JSON string des infos serveur
     * @return OptimizationResponse avec le script optimisé et l'analyse
     */
    public OptimizationResponse optimize(
            String buildStatistics,
            String pipelineConfig,
            String buildLog,
            String buildHistory,
            String agentInfo,
            String serverInfo
    ) {
        try {
            String endpoint = aiAnalyzerUrl + "/api/optimize";

            // Construire le body de la requête (format attendu par ai-analyzer)
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("buildStatistics", buildStatistics);
            requestBody.put("pipelineConfig", pipelineConfig);
            requestBody.put("buildLog", buildLog);
            requestBody.put("buildHistory", buildHistory);
            requestBody.put("agentInfo", agentInfo);
            requestBody.put("serverInfo", serverInfo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Appel POST vers ai-analyzer
            ResponseEntity<OptimizationResponse> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    OptimizationResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call ai-analyzer service: " + e.getMessage(), e);
        }
    }

    /**
     * GET /api/health
     * Vérifie le statut du service AI
     */
    public String health() {
        try {
            String endpoint = aiAnalyzerUrl + "/api/health";
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to check ai-analyzer health: " + e.getMessage(), e);
        }
    }

    /**
     * GET /api/provider
     * Récupère le provider LLM actuellement utilisé
     */
    public String getProvider() {
        try {
            String endpoint = aiAnalyzerUrl + "/api/provider";
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ai-analyzer provider: " + e.getMessage(), e);
        }
    }
}
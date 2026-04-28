package com.cicd.analyzer.aianalyzerservice.dto;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires simplifiés pour les DTOs
 * Teste la sérialisation/désérialisation et les getters/setters Lombok
 */
class DTOsTestSimple {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("OptimizationRequest - Getters/Setters fonctionnent")
    void optimizationRequest_GettersSetters_ShouldWork() {
        // Given
        OptimizationRequest request = new OptimizationRequest();

        // When
        request.setBuildStatistics("test stats");
        request.setPipelineConfig("test config");
        request.setBuildLog("test log");
        request.setBuildHistory("test history");
        request.setAgentInfo("test agent");
        request.setServerInfo("test server");

        // Then
        assertThat(request.getBuildStatistics()).isEqualTo("test stats");
        assertThat(request.getPipelineConfig()).isEqualTo("test config");
        assertThat(request.getBuildLog()).isEqualTo("test log");
        assertThat(request.getBuildHistory()).isEqualTo("test history");
        assertThat(request.getAgentInfo()).isEqualTo("test agent");
        assertThat(request.getServerInfo()).isEqualTo("test server");
    }

    @Test
    @DisplayName("OptimizationResponse - Getters/Setters fonctionnent")
    void optimizationResponse_GettersSetters_ShouldWork() {
        // Given
        OptimizationResponse response = new OptimizationResponse();

        // When
        response.setOptimizedScript("test script");
        response.setAnalysis("test analysis");
        response.setOptimizations("test optimizations");
        response.setEstimatedGain("test gain");
        response.setProvider("test provider");
        response.setRawResponse("test raw");

        // Then
        assertThat(response.getOptimizedScript()).isEqualTo("test script");
        assertThat(response.getAnalysis()).isEqualTo("test analysis");
        assertThat(response.getOptimizations()).isEqualTo("test optimizations");
        assertThat(response.getEstimatedGain()).isEqualTo("test gain");
        assertThat(response.getProvider()).isEqualTo("test provider");
        assertThat(response.getRawResponse()).isEqualTo("test raw");
    }

    @Test
    @DisplayName("OptimizationRequest - Sérialisation JSON")
    void optimizationRequest_JsonSerialization_ShouldWork() throws Exception {
        // Given
        OptimizationRequest request = new OptimizationRequest();
        request.setBuildStatistics("{\"buildNumber\": 121}");
        request.setPipelineConfig("{\"jobName\": \"test\"}");
        request.setBuildLog("test log");

        // When
        String json = objectMapper.writeValueAsString(request);

        // Then
        assertThat(json).contains("buildStatistics");
        assertThat(json).contains("pipelineConfig");
        assertThat(json).contains("buildLog");
        assertThat(json).contains("buildNumber");
    }

    @Test
    @DisplayName("OptimizationResponse - Sérialisation JSON")
    void optimizationResponse_JsonSerialization_ShouldWork() throws Exception {
        // Given
        OptimizationResponse response = new OptimizationResponse();
        response.setOptimizedScript("pipeline { agent any }");
        response.setProvider("Gemini");
        response.setAnalysis("{\"test\": \"value\"}");

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertThat(json).contains("optimizedScript");
        assertThat(json).contains("provider");
        assertThat(json).contains("analysis");
        assertThat(json).contains("Gemini");
    }

    @Test
    @DisplayName("OptimizationRequest - Désérialisation JSON")
    void optimizationRequest_JsonDeserialization_ShouldWork() throws Exception {
        // Given
        String json = """
            {
              "buildStatistics": "{\"buildNumber\": 121}",
              "pipelineConfig": "{\"jobName\": \"test\"}",
              "buildLog": "test log"
            }
            """;

        // When
        OptimizationRequest request = objectMapper.readValue(json, OptimizationRequest.class);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getBuildStatistics()).contains("buildNumber");
        assertThat(request.getPipelineConfig()).contains("jobName");
        assertThat(request.getBuildLog()).isEqualTo("test log");
    }

    @Test
    @DisplayName("OptimizationResponse - Désérialisation JSON")
    void optimizationResponse_JsonDeserialization_ShouldWork() throws Exception {
        // Given
        String json = """
            {
              "optimizedScript": "pipeline { agent any }",
              "provider": "Gemini",
              "analysis": "{\"test\": \"value\"}"
            }
            """;

        // When
        OptimizationResponse response = objectMapper.readValue(json, OptimizationResponse.class);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOptimizedScript()).isEqualTo("pipeline { agent any }");
        assertThat(response.getProvider()).isEqualTo("Gemini");
        assertThat(response.getAnalysis()).contains("test");
    }

    @Test
    @DisplayName("OptimizationRequest - Champs null gérés correctement")
    void optimizationRequest_NullFields_ShouldBeHandled() throws Exception {
        // Given
        String json = """
            {
              "buildStatistics": null,
              "pipelineConfig": null
            }
            """;

        // When
        OptimizationRequest request = objectMapper.readValue(json, OptimizationRequest.class);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getBuildStatistics()).isNull();
        assertThat(request.getPipelineConfig()).isNull();
    }
}
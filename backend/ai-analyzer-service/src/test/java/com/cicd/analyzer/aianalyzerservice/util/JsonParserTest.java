package com.cicd.analyzer.aianalyzerservice.util;

import com.cicd.analyzer.aianalyzerservice.dto.OptimizationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour JsonParser
 * Teste le parsing des réponses LLM et la gestion des erreurs
 */
class JsonParserTest {

    private JsonParser jsonParser;

    @BeforeEach
    void setUp() {
        jsonParser = new JsonParser();
    }

    @Test
    @DisplayName("Parse une réponse LLM valide avec tous les champs")
    void parseOptimizationResponse_ValidJson_ShouldParseAllFields() {
        // Given
        String llmResponse = """
            {
              "analysis": {
                "bottlenecks": [
                  {
                    "stage": "Deploy Stack",
                    "duration": 130631,
                    "percentage": 61.3,
                    "issue": "Docker compose taking too long"
                  }
                ],
                "detectedIssues": ["Slow docker pull", "No cache optimization"]
              },
              "optimizations": [
                {
                  "priority": 1,
                  "title": "Enable Docker cache",
                  "description": "Use cached layers to speed up builds",
                  "estimatedGain": "60-80s"
                }
              ],
              "optimizedScript": "pipeline { agent any\\n stages { stage('Build') { steps { sh 'mvn clean package' } } } }",
              "estimatedGain": {
                "currentDuration": 213099,
                "estimatedDuration": 150000,
                "reductionPercentage": "30%"
              }
            }
            """;
        String provider = "Gemini";

        // When
        OptimizationResponse response = jsonParser.parseOptimizationResponse(llmResponse, provider);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo("Gemini");
        assertThat(response.getRawResponse()).isEqualTo(llmResponse);
        assertThat(response.getOptimizedScript()).contains("pipeline { agent any");
        assertThat(response.getAnalysis()).contains("bottlenecks");
        assertThat(response.getOptimizations()).contains("priority");
        assertThat(response.getEstimatedGain()).contains("currentDuration");
    }

    @Test
    @DisplayName("Parse une réponse avec markdown (```json)")
    void parseOptimizationResponse_WithMarkdown_ShouldCleanAndParse() {
        // Given
        String llmResponse = """
            ```json
            {
              "analysis": {
                "bottlenecks": [],
                "detectedIssues": []
              },
              "optimizations": [],
              "optimizedScript": "pipeline { agent any }",
              "estimatedGain": {
                "currentDuration": 100000,
                "estimatedDuration": 80000,
                "reductionPercentage": "20%"
              }
            }
            ```
            """;

        // When
        OptimizationResponse response = jsonParser.parseOptimizationResponse(llmResponse, "OpenAI");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo("OpenAI");
        assertThat(response.getOptimizedScript()).isEqualTo("pipeline { agent any }");
    }

    @Test
    @DisplayName("Parse une réponse avec texte avant/après le JSON")
    void parseOptimizationResponse_WithExtraText_ShouldExtractJson() {
        // Given
        String llmResponse = """
            Voici l'analyse de votre pipeline:
            
            {
              "analysis": {"bottlenecks": [], "detectedIssues": []},
              "optimizations": [],
              "optimizedScript": "pipeline { agent any }",
              "estimatedGain": {"currentDuration": 100000, "estimatedDuration": 80000, "reductionPercentage": "20%"}
            }
            
            J'espère que cela vous aide!
            """;

        // When
        OptimizationResponse response = jsonParser.parseOptimizationResponse(llmResponse, "Groq");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo("Groq");
        assertThat(response.getOptimizedScript()).isEqualTo("pipeline { agent any }");
    }

    @Test
    @DisplayName("Gestion d'un JSON malformé")
    void parseOptimizationResponse_MalformedJson_ShouldReturnErrorResponse() {
        // Given
        String malformedJson = """
            {
              "analysis": {
                "bottlenecks": [
                  "missing closing bracket"
              "optimizations": []
            """;

        // When
        OptimizationResponse response = jsonParser.parseOptimizationResponse(malformedJson, "Gemini");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo("Gemini");
        assertThat(response.getRawResponse()).isEqualTo(malformedJson);
        assertThat(response.getAnalysis()).contains("error");
        assertThat(response.getOptimizedScript()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Gestion d'une réponse null")
    void parseOptimizationResponse_NullResponse_ShouldReturnErrorResponse() {
        // When
        OptimizationResponse response = jsonParser.parseOptimizationResponse(null, "OpenAI");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo("OpenAI");
        assertThat(response.getRawResponse()).isNull();
        assertThat(response.getAnalysis()).isEqualTo("{}");
    }

    @Test
    @DisplayName("Gestion d'une réponse vide")
    void parseOptimizationResponse_EmptyResponse_ShouldReturnErrorResponse() {
        // When
        OptimizationResponse response = jsonParser.parseOptimizationResponse("", "Groq");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo("Groq");
        assertThat(response.getAnalysis()).isEqualTo("{}");
    }

    @Test
    @DisplayName("Parse JSON avec champs manquants")
    void parseOptimizationResponse_MissingFields_ShouldUseDefaults() {
        // Given
        String incompleteJson = """
            {
              "optimizedScript": "pipeline { agent any }"
            }
            """;

        // When
        OptimizationResponse response = jsonParser.parseOptimizationResponse(incompleteJson, "Gemini");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOptimizedScript()).isEqualTo("pipeline { agent any }");
        assertThat(response.getAnalysis()).isEqualTo("{}");
        assertThat(response.getOptimizations()).isEqualTo("{}");
        assertThat(response.getEstimatedGain()).isEqualTo("{}");
    }

    @Test
    @DisplayName("Parse JSON avec nested objects complexes")
    void parseOptimizationResponse_ComplexNestedObjects_ShouldPreserveStructure() {
        // Given
        String complexJson = """
            {
              "analysis": {
                "bottlenecks": [
                  {
                    "stage": "Test Stage",
                    "duration": 45000,
                    "nested": {
                      "details": "Complex nested object",
                      "metrics": [1, 2, 3]
                    }
                  }
                ],
                "detectedIssues": ["Issue 1", "Issue 2"]
              },
              "optimizations": [
                {
                  "priority": 1,
                  "config": {
                    "parallel": true,
                    "agents": ["agent1", "agent2"]
                  }
                }
              ],
              "optimizedScript": "pipeline { parallel { stage('A') {} stage('B') {} } }",
              "estimatedGain": {
                "currentDuration": 213099,
                "estimatedDuration": 150000,
                "breakdown": {
                  "stages": ["Deploy: -60s", "Test: -20s"]
                }
              }
            }
            """;

        // When
        OptimizationResponse response = jsonParser.parseOptimizationResponse(complexJson, "OpenAI");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAnalysis()).contains("nested");
        assertThat(response.getAnalysis()).contains("metrics");
        assertThat(response.getOptimizations()).contains("parallel");
        assertThat(response.getEstimatedGain()).contains("breakdown");
    }
}
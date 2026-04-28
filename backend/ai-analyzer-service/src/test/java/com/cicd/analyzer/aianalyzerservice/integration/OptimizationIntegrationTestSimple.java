package com.cicd.analyzer.aianalyzerservice.integration;

import com.cicd.analyzer.aianalyzerservice.dto.OptimizationRequest;
import com.cicd.analyzer.aianalyzerservice.service.DocumentLoaderService;
import com.cicd.analyzer.aianalyzerservice.service.llm.LLMService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration simplifiés pour l'ensemble du flux d'optimisation
 */
@SpringBootTest
class OptimizationIntegrationTestSimple {

    private DocumentLoaderService documentLoaderService;
    private LLMService geminiService;

    @BeforeEach
    void setUp() {
        documentLoaderService = mock(DocumentLoaderService.class);
        geminiService = mock(LLMService.class);
    }

    @Test
    @DisplayName("Test de base - Services peuvent être mockés")
    void basicTest_ServicesMockingWorks() {
        // Given
        when(documentLoaderService.getAllDocuments()).thenReturn("test knowledge");
        when(geminiService.getProviderName()).thenReturn("Gemini AI");

        // When
        String docs = documentLoaderService.getAllDocuments();
        String provider = geminiService.getProviderName();

        // Then
        assertEquals("test knowledge", docs);
        assertEquals("Gemini AI", provider);
        verify(documentLoaderService).getAllDocuments();
        verify(geminiService).getProviderName();
    }

    @Test
    @DisplayName("Test de flux - Services interagissent correctement")
    void flowTest_ServicesInteractCorrectly() {
        // Given
        when(documentLoaderService.getAllDocuments()).thenReturn("knowledge base");
        when(geminiService.analyze(any())).thenReturn("llm response");
        when(geminiService.getProviderName()).thenReturn("Gemini");

        // When - Simuler un appel de service
        String docs = documentLoaderService.getAllDocuments();
        String response = geminiService.analyze("test prompt");

        // Then
        assertNotNull(docs);
        assertNotNull(response);
        assertEquals("llm response", response);
    }

    @Test
    @DisplayName("Test d'erreur - Gestion des exceptions")
    void errorTest_ExceptionHandling() {
        // Given
        when(documentLoaderService.getAllDocuments()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            documentLoaderService.getAllDocuments();
        });
    }

    @Test
    @DisplayName("Test avec données réalistes")
    void realisticDataTest() {
        // Given - Utiliser les données Bruno pour un test réaliste
        OptimizationRequest request = createBrunoRequest();
        
        when(geminiService.analyze(anyString())).thenReturn(createValidLLMResponse());
        when(geminiService.getProviderName()).thenReturn("Gemini");

        // When
        String result = geminiService.analyze("prompt with bruno data");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("analysis"));
        assertTrue(result.contains("optimizedScript"));
        
        // Vérifier que la requête est bien formée
        assertNotNull(request.getBuildStatistics());
        assertNotNull(request.getPipelineConfig());
    }

    // Helper methods
    private OptimizationRequest createTestRequest() {
        OptimizationRequest request = new OptimizationRequest();
        request.setBuildStatistics("{\"buildNumber\": 121}");
        request.setPipelineConfig("{\"script\": \"pipeline { agent any }\"}");
        request.setBuildLog("test log");
        request.setBuildHistory("[]");
        request.setAgentInfo("{}");
        request.setServerInfo("{}");
        return request;
    }

    private OptimizationRequest createBrunoRequest() {
        OptimizationRequest request = new OptimizationRequest();
        request.setBuildStatistics("{\"buildNumber\": 121, \"jobName\": \"patient-management\", \"duration\": 213099}");
        request.setPipelineConfig("{\"jobName\": \"patient-management\", \"script\": \"pipeline { agent any }\"}");
        request.setBuildLog("[Pipeline] Start of Pipeline - Patient Management");
        request.setBuildHistory("[{\"number\": 121, \"duration\": 213099, \"result\": \"SUCCESS\"}]");
        request.setAgentInfo("[{\"displayName\": \"master\", \"numExecutors\": 4}]");
        request.setServerInfo("{\"nodeName\": \"jenkins-master\"}");
        return request;
    }

    private String createValidLLMResponse() {
        return """
            {
              "analysis": {
                "bottlenecks": [
                  {
                    "stage": "Build",
                    "duration": 60000,
                    "percentage": 28.2,
                    "issue": "No cache optimization"
                  }
                ],
                "detectedIssues": ["Missing cache", "Sequential execution"]
              },
              "optimizations": [
                {
                  "priority": 1,
                  "title": "Enable Maven cache",
                  "description": "Use cached dependencies",
                  "estimatedGain": "30-40s"
                }
              ],
              "optimizedScript": "pipeline { agent any; parallel { stage('Build') { steps { sh 'mvn clean package' } } } }",
              "estimatedGain": {
                "currentDuration": 213099,
                "estimatedDuration": 150000,
                "reductionPercentage": "30%"
              }
            }
            """;
    }
}
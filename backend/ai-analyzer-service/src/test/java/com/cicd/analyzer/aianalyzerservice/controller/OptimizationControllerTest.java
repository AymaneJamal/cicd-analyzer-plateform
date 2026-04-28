package com.cicd.analyzer.aianalyzerservice.controller;

import com.cicd.analyzer.aianalyzerservice.dto.OptimizationRequest;
import com.cicd.analyzer.aianalyzerservice.dto.OptimizationResponse;
import com.cicd.analyzer.aianalyzerservice.service.OptimizationService;
import com.cicd.analyzer.aianalyzerservice.util.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Tests unitaires simplifiés pour OptimizationController
 * Tests sans dépendances Spring Boot avancées
 */
class OptimizationControllerTest {

    private OptimizationController controller;
    private OptimizationService optimizationService;
    private JsonParser jsonParser;

    @BeforeEach
    void setUp() {
        optimizationService = mock(OptimizationService.class);
        jsonParser = mock(JsonParser.class);
        controller = new OptimizationController();
        
        // Injection manuelle des dépendances via réflexion
        injectDependencies();
    }

    private void injectDependencies() {
        try {
            Field optimizationServiceField = OptimizationController.class.getDeclaredField("optimizationService");
            optimizationServiceField.setAccessible(true);
            optimizationServiceField.set(controller, optimizationService);
            
            Field jsonParserField = OptimizationController.class.getDeclaredField("jsonParser");
            jsonParserField.setAccessible(true);
            jsonParserField.set(controller, jsonParser);
        } catch (Exception e) {
            fail("Erreur lors de l'injection des dépendances: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("optimize() - Requête valide doit retourner une réponse correcte")
    void optimize_ValidRequest_ShouldReturnOptimizationResponse() {
        // Given
        OptimizationRequest request = createValidRequest();
        String llmResponse = "{\"optimizedScript\": \"pipeline { agent any }\", \"analysis\": {}}";
        OptimizationResponse expectedResponse = createValidResponse();

        when(optimizationService.optimize(any(), any(), any(), any(), any(), any())).thenReturn(llmResponse);
        when(optimizationService.getCurrentProvider()).thenReturn("Gemini");
        when(jsonParser.parseOptimizationResponse(llmResponse, "Gemini")).thenReturn(expectedResponse);

        // When
        ResponseEntity<OptimizationResponse> result = controller.optimize(request);

        // Then
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("pipeline { agent any }", result.getBody().getOptimizedScript());
        assertEquals("Gemini", result.getBody().getProvider());

        verify(optimizationService).optimize(
                request.getBuildStatistics(),
                request.getPipelineConfig(),
                request.getBuildLog(),
                request.getBuildHistory(),
                request.getAgentInfo(),
                request.getServerInfo()
        );
    }

    @Test
    @DisplayName("optimize() - BuildStatistics null doit retourner BadRequest")
    void optimize_NullBuildStatistics_ShouldReturnBadRequest() {
        // Given
        OptimizationRequest request = createValidRequest();
        request.setBuildStatistics(null);

        // When
        ResponseEntity<OptimizationResponse> result = controller.optimize(request);

        // Then
        assertEquals(400, result.getStatusCode().value());
        verifyNoInteractions(optimizationService);
    }

    @Test
    @DisplayName("optimize() - PipelineConfig null doit retourner BadRequest")
    void optimize_NullPipelineConfig_ShouldReturnBadRequest() {
        // Given
        OptimizationRequest request = createValidRequest();
        request.setPipelineConfig(null);

        // When
        ResponseEntity<OptimizationResponse> result = controller.optimize(request);

        // Then
        assertEquals(400, result.getStatusCode().value());
        verifyNoInteractions(optimizationService);
    }

    @Test
    @DisplayName("optimize() - Erreur du service doit retourner Internal Server Error")
    void optimize_ServiceError_ShouldReturnInternalServerError() {
        // Given
        OptimizationRequest request = createValidRequest();
        when(optimizationService.optimize(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("LLM service unavailable"));

        // When
        ResponseEntity<OptimizationResponse> result = controller.optimize(request);

        // Then
        assertEquals(500, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getAnalysis().contains("LLM service unavailable"));
        assertEquals("N/A", result.getBody().getProvider());
    }

    @Test
    @DisplayName("health() - Doit retourner le statut UP avec provider")
    void health_ShouldReturnStatusUp() {
        // Given
        when(optimizationService.getCurrentProvider()).thenReturn("OpenAI");

        // When
        ResponseEntity<String> result = controller.health();

        // Then
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().contains("UP"));
        assertTrue(result.getBody().contains("OpenAI"));
        verify(optimizationService).getCurrentProvider();
    }

    @Test
    @DisplayName("getProvider() - Doit retourner le provider actuel")
    void getProvider_ShouldReturnCurrentProvider() {
        // Given
        when(optimizationService.getCurrentProvider()).thenReturn("Gemini");

        // When
        ResponseEntity<String> result = controller.getProvider();

        // Then
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().contains("Gemini"));
        verify(optimizationService).getCurrentProvider();
    }

    @Test
    @DisplayName("optimize() - Données réalistes Bruno")
    void optimize_RealisticBrunoData_ShouldWork() {
        // Given
        OptimizationRequest request = createBrunoRequest();
        OptimizationResponse expectedResponse = createValidResponse();
        
        when(optimizationService.optimize(any(), any(), any(), any(), any(), any())).thenReturn("llm response");
        when(optimizationService.getCurrentProvider()).thenReturn("Gemini");
        when(jsonParser.parseOptimizationResponse(any(), any())).thenReturn(expectedResponse);

        // When
        ResponseEntity<OptimizationResponse> result = controller.optimize(request);

        // Then
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        
        // Vérifier que les bons paramètres ont été passés au service
        verify(optimizationService).optimize(
                eq(request.getBuildStatistics()),
                eq(request.getPipelineConfig()),
                eq(request.getBuildLog()),
                eq(request.getBuildHistory()),
                eq(request.getAgentInfo()),
                eq(request.getServerInfo())
        );
    }

    @Test
    @DisplayName("optimize() - Exception lors du parsing doit être gérée")
    void optimize_ParsingError_ShouldBeHandled() {
        // Given
        OptimizationRequest request = createValidRequest();
        when(optimizationService.optimize(any(), any(), any(), any(), any(), any())).thenReturn("response");
        when(optimizationService.getCurrentProvider()).thenReturn("Gemini");
        when(jsonParser.parseOptimizationResponse(any(), any())).thenThrow(new RuntimeException("Parsing failed"));

        // When
        ResponseEntity<OptimizationResponse> result = controller.optimize(request);

        // Then
        assertEquals(500, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getAnalysis().contains("Parsing failed"));
    }

    // Helper methods
    private OptimizationRequest createValidRequest() {
        OptimizationRequest request = new OptimizationRequest();
        request.setBuildStatistics("{\"buildNumber\": 121, \"duration\": 213099}");
        request.setPipelineConfig("{\"jobName\": \"test-job\", \"script\": \"pipeline { agent any }\"}");
        request.setBuildLog("[Pipeline] Start of Pipeline");
        request.setBuildHistory("[{\"number\": 121, \"result\": \"SUCCESS\"}]");
        request.setAgentInfo("[{\"displayName\": \"master\", \"numExecutors\": 4}]");
        request.setServerInfo("{\"nodeName\": \"jenkins-master\"}");
        return request;
    }

    private OptimizationRequest createBrunoRequest() {
        OptimizationRequest request = new OptimizationRequest();
        request.setBuildStatistics("{\"buildNumber\": 121, \"jobName\": \"patient-management-logging-materials\", \"duration\": 213099}");
        request.setPipelineConfig("{\"jobName\": \"patient-management-logging-materials\", \"script\": \"pipeline { agent any }\"}");
        request.setBuildLog("[Pipeline] Start of Pipeline - Patient Management");
        request.setBuildHistory("[{\"number\": 121, \"duration\": 213099, \"result\": \"SUCCESS\"}]");
        request.setAgentInfo("[{\"displayName\": \"master\", \"numExecutors\": 4, \"assignedLabels\": [\"docker\", \"maven\"]}]");
        request.setServerInfo("{\"nodeName\": \"jenkins-master\", \"useSecurity\": true}");
        return request;
    }

    private OptimizationResponse createValidResponse() {
        OptimizationResponse response = new OptimizationResponse();
        response.setOptimizedScript("pipeline { agent any }");
        response.setAnalysis("{\"bottlenecks\": []}");
        response.setOptimizations("[]");
        response.setEstimatedGain("{\"reductionPercentage\": \"30%\"}");
        response.setProvider("Gemini");
        return response;
    }
}
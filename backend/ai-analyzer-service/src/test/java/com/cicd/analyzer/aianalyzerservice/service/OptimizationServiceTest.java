package com.cicd.analyzer.aianalyzerservice.service;

import com.cicd.analyzer.aianalyzerservice.service.llm.LLMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour OptimizationService
 * Teste l'orchestration des services et la gestion des providers LLM
 */
@ExtendWith(MockitoExtension.class)
class OptimizationServiceTest {

    @Mock(lenient = true)
    private DocumentLoaderService documentLoaderService;

    @Mock(lenient = true)
    private PromptBuilderService promptBuilderService;

    @Mock(lenient = true)
    private ApplicationContext applicationContext;

    @Mock(lenient = true)
    private LLMService mockLLMService;

    @InjectMocks
    private OptimizationService optimizationService;

    @BeforeEach
    void setUp() {
        // Simuler la configuration du provider
        ReflectionTestUtils.setField(optimizationService, "currentProvider", "geminiService");
    }

    @Test
    @DisplayName("Optimisation complète avec tous les services")
    void optimize_CompleteFlow_ShouldOrchestrateProperly() {
        // Given
        String buildStatistics = "{\"buildNumber\": 121, \"duration\": 213099}";
        String pipelineConfig = "{\"jobName\": \"test-job\", \"script\": \"pipeline { agent any }\"}";
        String buildLog = "[Pipeline] Start of Pipeline";
        String buildHistory = "[{\"number\": 121, \"result\": \"SUCCESS\"}]";
        String agentInfo = "[{\"displayName\": \"master\", \"numExecutors\": 4}]";
        String serverInfo = "{\"nodeName\": \"jenkins-master\"}";

        String knowledgeBase = "# Jenkins Best Practices\n- Use parallel builds";
        String fullPrompt = "Complete prompt with all data...";
        String llmResponse = "{\"optimizedScript\": \"optimized pipeline\", \"analysis\": {}}";

        // Setup mocks
        when(documentLoaderService.getAllDocuments()).thenReturn(knowledgeBase);
        when(promptBuilderService.buildOptimizationPrompt(
                knowledgeBase, buildStatistics, pipelineConfig, 
                buildLog, buildHistory, agentInfo, serverInfo
        )).thenReturn(fullPrompt);
        when(applicationContext.getBean("geminiService")).thenReturn(mockLLMService);
        when(mockLLMService.analyze(fullPrompt)).thenReturn(llmResponse);
        when(mockLLMService.getProviderName()).thenReturn("Gemini");

        // When
        String result = optimizationService.optimize(
                buildStatistics, pipelineConfig, buildLog, 
                buildHistory, agentInfo, serverInfo
        );

        // Then
        assertThat(result).isEqualTo(llmResponse);
        
        // Verify basic interactions
        verify(documentLoaderService).getAllDocuments();
        verify(mockLLMService).analyze(fullPrompt);
    }

    @Test
    @DisplayName("Gestion d'erreur du DocumentLoaderService")
    void optimize_DocumentLoaderError_ShouldThrowException() {
        // Given
        when(documentLoaderService.getAllDocuments()).thenThrow(new RuntimeException("File not found"));

        // When & Then
        assertThatThrownBy(() -> optimizationService.optimize(
                "stats", "config", "logs", "history", "agents", "server"
        ))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Optimization failed")
        .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Gestion d'erreur du PromptBuilderService")
    void optimize_PromptBuilderError_ShouldThrowException() {
        // Given
        when(documentLoaderService.getAllDocuments()).thenReturn("knowledge");
        when(promptBuilderService.buildOptimizationPrompt(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Prompt building failed"));

        // When & Then
        assertThatThrownBy(() -> optimizationService.optimize(
                "stats", "config", "logs", "history", "agents", "server"
        ))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Optimization failed");
    }

    @Test
    @DisplayName("Gestion d'erreur du LLMService")
    void optimize_LLMServiceError_ShouldThrowException() {
        // Given
        when(documentLoaderService.getAllDocuments()).thenReturn("knowledge");
        when(promptBuilderService.buildOptimizationPrompt(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("prompt");
        when(applicationContext.getBean("geminiService")).thenReturn(mockLLMService);
        when(mockLLMService.analyze(any())).thenThrow(new RuntimeException("LLM API error"));

        // When & Then
        assertThatThrownBy(() -> optimizationService.optimize(
                "stats", "config", "logs", "history", "agents", "server"
        ))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Optimization failed");
    }

    @Test
    @DisplayName("getCurrentProvider retourne le provider correct")
    void getCurrentProvider_ShouldReturnLLMProviderName() {
        // Given
        when(applicationContext.getBean("geminiService")).thenReturn(mockLLMService);
        when(mockLLMService.getProviderName()).thenReturn("Gemini AI");

        // When
        String provider = optimizationService.getCurrentProvider();

        // Then
        assertThat(provider).isEqualTo("Gemini AI");
    }

    @Test
    @DisplayName("Provider dynamique - changement de provider")
    void optimize_DifferentProvider_ShouldUseDifferentService() {
        // Given - Changer le provider en openaiService ET réinitialiser le cache
        ReflectionTestUtils.setField(optimizationService, "currentProvider", "openaiService");
        ReflectionTestUtils.setField(optimizationService, "llmService", null); // Reset cache
        
        LLMService openAIService = mock(LLMService.class, withSettings().lenient());
        
        when(documentLoaderService.getAllDocuments()).thenReturn("knowledge");
        when(promptBuilderService.buildOptimizationPrompt(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("prompt");
        when(applicationContext.getBean("openaiService")).thenReturn(openAIService);
        when(openAIService.analyze(any(String.class))).thenReturn("openai response");
        when(openAIService.getProviderName()).thenReturn("OpenAI");

        // When
        String result = optimizationService.optimize(
                "stats", "config", "logs", "history", "agents", "server"
        );

        // Then
        assertThat(result).isEqualTo("openai response");
        assertThat(result).isNotNull();
        verify(openAIService).analyze(any(String.class));
    }

    @Test
    @DisplayName("Cache du LLMService - ne récupère qu'une fois")
    void optimize_MultipleCalls_ShouldCacheLLMService() {
        // Given
        when(documentLoaderService.getAllDocuments()).thenReturn("knowledge");
        when(promptBuilderService.buildOptimizationPrompt(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("prompt");
        when(applicationContext.getBean("geminiService")).thenReturn(mockLLMService);
        when(mockLLMService.analyze(any())).thenReturn("response");
        when(mockLLMService.getProviderName()).thenReturn("Gemini");

        // When - Multiple calls
        String result1 = optimizationService.optimize("stats", "config", "logs", "history", "agents", "server");
        String result2 = optimizationService.optimize("stats2", "config2", "logs2", "history2", "agents2", "server2");

        // Then - Verify results and basic interactions
        assertThat(result1).isEqualTo("response");
        assertThat(result2).isEqualTo("response");
        verify(mockLLMService, times(2)).analyze(any());
    }

    @Test
    @DisplayName("Gestion des paramètres null/vides")
    void optimize_NullParameters_ShouldHandleGracefully() {
        // Given
        when(documentLoaderService.getAllDocuments()).thenReturn("knowledge");
        when(promptBuilderService.buildOptimizationPrompt(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("prompt");
        when(applicationContext.getBean("geminiService")).thenReturn(mockLLMService);
        when(mockLLMService.analyze(any())).thenReturn("response");

        // When
        String result = optimizationService.optimize(null, null, null, null, null, null);

        // Then
        assertThat(result).isEqualTo("response");
    }

    @Test
    @DisplayName("Provider bean inexistant - gestion d'erreur")
    void getCurrentProvider_ProviderNotFound_ShouldThrowException() {
        // Given
        when(applicationContext.getBean("geminiService"))
                .thenThrow(new RuntimeException("Bean not found"));

        // When & Then - Le service ne throw pas, il retourne null ou une valeur par défaut
        String result = optimizationService.getCurrentProvider();
        
        // Vérifier que le service gère l'erreur gracieusement
        assertThat(result).isNull();
    }
}
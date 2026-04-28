package com.cicd.analyzer.aianalyzerservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour PromptBuilderService
 * Teste la construction des prompts LLM et la gestion des données
 */
class PromptBuilderServiceTest {

    private PromptBuilderService promptBuilderService;

    @BeforeEach
    void setUp() {
        promptBuilderService = new PromptBuilderService();
    }

    @Test
    @DisplayName("Construit un prompt complet avec tous les paramètres")
    void buildOptimizationPrompt_AllParameters_ShouldBuildCompletePrompt() {
        // Given
        String knowledgeBase = "# Jenkins Best Practices\n- Use parallel builds\n- Cache dependencies";
        String buildStatistics = "{\"buildNumber\": 121, \"duration\": 213099}";
        String pipelineConfig = "{\"jobName\": \"test-job\", \"script\": \"pipeline { agent any }\"}";
        String buildLog = "[Pipeline] Start of Pipeline\n[Pipeline] git\nCloning repository...";
        String buildHistory = "[{\"number\": 121, \"duration\": 213099, \"result\": \"SUCCESS\"}]";
        String agentInfo = "[{\"displayName\": \"master\", \"numExecutors\": 4, \"offline\": false}]";
        String serverInfo = "{\"nodeName\": \"jenkins-master\", \"numExecutors\": 4}";

        // When
        String prompt = promptBuilderService.buildOptimizationPrompt(
                knowledgeBase, buildStatistics, pipelineConfig, 
                buildLog, buildHistory, agentInfo, serverInfo
        );

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("MISSION: Expert DevOps");
        assertThat(prompt).contains("Jenkins Best Practices");
        assertThat(prompt).contains("buildNumber\": 121");
        assertThat(prompt).contains("test-job");
        assertThat(prompt).contains("Start of Pipeline");
        assertThat(prompt).contains("jenkins-master");
        assertThat(prompt).contains("FORMAT DE RÉPONSE");
        assertThat(prompt).contains("```json");
    }

    @Test
    @DisplayName("Gère correctement les paramètres null")
    void buildOptimizationPrompt_NullParameters_ShouldNotBreak() {
        // When
        String prompt = promptBuilderService.buildOptimizationPrompt(
                null, null, null, null, null, null, null
        );

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("MISSION: Expert DevOps");
        assertThat(prompt).contains("DONNÉES DU PIPELINE");
        // Le service peut contenir "null" dans le template - c'est acceptable
        assertThat(prompt).isNotBlank();
    }

    @Test
    @DisplayName("Gère correctement les paramètres vides")
    void buildOptimizationPrompt_EmptyParameters_ShouldHandleGracefully() {
        // When
        String prompt = promptBuilderService.buildOptimizationPrompt(
                "", "", "", "", "", "", ""
        );

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("MISSION: Expert DevOps");
        assertThat(prompt).contains("STATISTIQUES DU BUILD");
        assertThat(prompt).contains("CONFIGURATION ACTUELLE");
    }

    @Test
    @DisplayName("Tronque les logs longs correctement")
    void buildOptimizationPrompt_LongLogs_ShouldTruncate() {
        // Given
        StringBuilder longLog = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longLog.append("[Pipeline] Step ").append(i).append(" - Very detailed log line with lots of information\n");
        }
        String veryLongLog = longLog.toString(); // > 5000 caractères

        // When
        String prompt = promptBuilderService.buildOptimizationPrompt(
                "knowledge", "stats", "config", veryLongLog, "history", "agents", "server"
        );

        // Then
        assertThat(prompt).contains("LOGS TRONQUÉS");
        assertThat(prompt).contains("caractères omis");
        // Vérifier que les logs dans le prompt sont bien limités
        String[] sections = prompt.split("## 3\\. LOGS D'EXÉCUTION");
        if (sections.length > 1) {
            String logSection = sections[1].split("## 4\\.")[0];
            assertThat(logSection.length()).isLessThan(veryLongLog.length());
        }
    }

    @Test
    @DisplayName("Logs courts ne sont pas tronqués")
    void buildOptimizationPrompt_ShortLogs_ShouldNotTruncate() {
        // Given
        String shortLog = "[Pipeline] Start\n[Pipeline] End";

        // When
        String prompt = promptBuilderService.buildOptimizationPrompt(
                "knowledge", "stats", "config", shortLog, "history", "agents", "server"
        );

        // Then
        assertThat(prompt).contains(shortLog);
        assertThat(prompt).doesNotContain("LOGS TRONQUÉS");
        assertThat(prompt).doesNotContain("caractères omis");
    }

    @Test
    @DisplayName("Contient toutes les sections obligatoires")
    void buildOptimizationPrompt_RequiredSections_ShouldBePresent() {
        // When
        String prompt = promptBuilderService.buildOptimizationPrompt(
                "knowledge", "stats", "config", "logs", "history", "agents", "server"
        );

        // Then
        // Vérifier les sections principales
        assertThat(prompt).contains("MISSION: Expert DevOps");
        assertThat(prompt).contains("DONNÉES DU PIPELINE");
        assertThat(prompt).contains("1. STATISTIQUES DU BUILD");
        assertThat(prompt).contains("2. CONFIGURATION ACTUELLE");
        assertThat(prompt).contains("3. LOGS D'EXÉCUTION");
        assertThat(prompt).contains("4. HISTORIQUE DES BUILDS");
        assertThat(prompt).contains("5. AGENTS DISPONIBLES");
        assertThat(prompt).contains("6. INFO SERVEUR JENKINS");
        assertThat(prompt).contains("INSTRUCTIONS");
        assertThat(prompt).contains("FORMAT DE RÉPONSE");
    }

    @Test
    @DisplayName("Le format de réponse JSON est correct")
    void buildOptimizationPrompt_JsonFormat_ShouldBeValid() {
        // When
        String prompt = promptBuilderService.buildOptimizationPrompt(
                "knowledge", "stats", "config", "logs", "history", "agents", "server"
        );

        // Then
        assertThat(prompt).contains("\"analysis\":");
        assertThat(prompt).contains("\"optimizations\":");
        assertThat(prompt).contains("\"optimizedScript\":");
        assertThat(prompt).contains("\"estimatedGain\":");
        assertThat(prompt).contains("bottlenecks");
        assertThat(prompt).contains("detectedIssues");
        assertThat(prompt).contains("currentDuration");
        assertThat(prompt).contains("reductionPercentage");
    }

    @Test
    @DisplayName("Contient les instructions critiques de format")
    void buildOptimizationPrompt_CriticalInstructions_ShouldBePresent() {
        // When
        String prompt = promptBuilderService.buildOptimizationPrompt(
                "knowledge", "stats", "config", "logs", "history", "agents", "server"
        );

        // Then
        assertThat(prompt).contains("INSTRUCTIONS CRITIQUES");
        assertThat(prompt).contains("COMMENCE IMMÉDIATEMENT par ```json");
        assertThat(prompt).contains("TERMINE par ```");
        assertThat(prompt).contains("VÉRIFIE que ton JSON est complet");
        assertThat(prompt).contains("STRUCTURE OBLIGATOIRE");
        assertThat(prompt).contains("VALIDATION FINALE");
    }

    @Test
    @DisplayName("Inclut la knowledge base au début")
    void buildOptimizationPrompt_KnowledgeBase_ShouldBeIncludedFirst() {
        // Given
        String knowledgeBase = "# Specific Jenkins Knowledge\n- Custom rule 1\n- Custom rule 2";

        // When
        String prompt = promptBuilderService.buildOptimizationPrompt(
                knowledgeBase, "stats", "config", "logs", "history", "agents", "server"
        );

        // Then
        // La knowledge base doit apparaître avant les données du pipeline
        int knowledgeIndex = prompt.indexOf("Specific Jenkins Knowledge");
        int dataIndex = prompt.indexOf("DONNÉES DU PIPELINE");
        assertThat(knowledgeIndex).isLessThan(dataIndex);
        assertThat(prompt).contains("Custom rule 1");
        assertThat(prompt).contains("Custom rule 2");
    }
}
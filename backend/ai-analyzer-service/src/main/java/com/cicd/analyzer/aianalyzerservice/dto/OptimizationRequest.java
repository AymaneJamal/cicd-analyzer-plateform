package com.cicd.analyzer.aianalyzerservice.dto;

import lombok.Data;

/**
 * DTO pour la requête d'optimisation
 * Contient toutes les données brutes du jenkins-connector
 */
@Data
public class OptimizationRequest {

    /**
     * BuildStatistics (endpoint 6) - JSON brut
     * Contient: stages, durées, résultats tests
     */
    private String buildStatistics;

    /**
     * PipelineConfig (endpoint 5) - JSON brut
     * Contient: script Groovy actuel
     */
    private String pipelineConfig;

    /**
     * BuildLog (endpoint 4) - Texte brut
     * Contient: logs complets d'exécution
     */
    private String buildLog;

    /**
     * Build History (endpoint 3) - JSON brut
     * Contient: historique des derniers builds
     */
    private String buildHistory;

    /**
     * AgentInfo (endpoint 7) - JSON brut
     * Contient: info sur les agents Jenkins
     */
    private String agentInfo;

    /**
     * ServerInfo (endpoint 1) - JSON brut
     * Contient: info générale du serveur Jenkins
     */
    private String serverInfo;
}

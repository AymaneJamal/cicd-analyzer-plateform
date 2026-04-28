package com.cicd.analyzer.pipelineorchestrator.service;

import com.cicd.analyzer.pipelineorchestrator.client.AiAnalyzerClient;
import com.cicd.analyzer.pipelineorchestrator.dto.response.OptimizationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service pour l'optimisation IA
 * Abstraction au-dessus du AiAnalyzerClient
 * Envoie les données Jenkins vers le service AI pour analyse
 */
@Service
@RequiredArgsConstructor
public class AiOptimizationService {

    private final AiAnalyzerClient aiAnalyzerClient;

    /**
     * Envoyer les données Jenkins au service AI pour optimisation
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
        return aiAnalyzerClient.optimize(
                buildStatistics,
                pipelineConfig,
                buildLog,
                buildHistory,
                agentInfo,
                serverInfo
        );
    }

    /**
     * Vérifier le statut du service AI
     * @return JSON string avec le statut
     */
    public String checkHealth() {
        return aiAnalyzerClient.health();
    }

    /**
     * Récupérer le provider LLM actuellement utilisé
     * @return JSON string avec le provider
     */
    public String getCurrentProvider() {
        return aiAnalyzerClient.getProvider();
    }
}

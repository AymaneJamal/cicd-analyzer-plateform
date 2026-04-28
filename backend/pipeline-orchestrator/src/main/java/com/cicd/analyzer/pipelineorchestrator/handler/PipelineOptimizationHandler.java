package com.cicd.analyzer.pipelineorchestrator.handler;

import com.cicd.analyzer.pipelineorchestrator.dto.response.OptimizationResponse;
import com.cicd.analyzer.pipelineorchestrator.entity.JenkinsConnection;
import com.cicd.analyzer.pipelineorchestrator.entity.OptimizationHistory;
import com.cicd.analyzer.pipelineorchestrator.exception.OptimizationException;
import com.cicd.analyzer.pipelineorchestrator.service.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Handler pour l'orchestration complète du workflow d'optimisation de pipeline
 * Combine tous les services pour exécuter le processus end-to-end
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PipelineOptimizationHandler {

    private final JenkinsConnectionService jenkinsConnectionService;
    private final JenkinsDataService jenkinsDataService;
    private final AiOptimizationService aiOptimizationService;
    private final OptimizationHistoryService optimizationHistoryService;
    private final ObjectMapper objectMapper;

    /**
     * Orchestrer le workflow complet d'optimisation de pipeline
     *
     * WORKFLOW:
     * 1. Récupérer la connexion Jenkins (avec vérification propriété user)
     * 2. Récupérer les 6 données nécessaires depuis jenkins-connector
     * 3. Envoyer les données au service AI pour optimisation
     * 4. Sauvegarder le résultat dans l'historique
     * 5. Retourner la réponse d'optimisation
     *
     * @param userEmail Email de l'utilisateur (du header)
     * @param connectionId ID de la connexion Jenkins
     * @param pipelineName Nom du pipeline à optimiser
     * @param buildNumber Numéro du build à analyser
     * @return OptimizationResponse avec le script optimisé et l'analyse
     */
    public OptimizationResponse optimizePipeline(
            String userEmail,
            Long connectionId,
            String pipelineName,
            Integer buildNumber
    ) {
        try {
            System.out.println("🚀 Démarrage optimisation pipeline: " + pipelineName + " build #" + buildNumber);

            // 1. Récupérer la connexion Jenkins (avec vérification propriété)
            JenkinsConnection connection = jenkinsConnectionService.findById(connectionId, userEmail);
            System.out.println("✅ Connexion Jenkins récupérée: " + connection.getName());

            // 2. Récupérer les 6 données nécessaires en appelant jenkins-connector
            System.out.println("📊 Récupération des données Jenkins...");

            String buildStatistics = jenkinsDataService.getBuildStatistics(connection, pipelineName, buildNumber);
            System.out.println("  ✓ Build statistics récupérées");

            String pipelineConfig = jenkinsDataService.getPipelineConfig(connection, pipelineName);
            System.out.println("  ✓ Pipeline config récupérée");

            String buildLog = jenkinsDataService.getBuildLog(connection, pipelineName, buildNumber);
            System.out.println("  ✓ Build logs récupérés");

            String buildHistory = jenkinsDataService.getBuildHistory(connection, pipelineName);
            System.out.println("  ✓ Build history récupéré");

            String agentInfo = jenkinsDataService.getAgents(connection);
            System.out.println("  ✓ Agent info récupéré");

            String serverInfo = jenkinsDataService.getServerInfo(connection);
            System.out.println("  ✓ Server info récupéré");

            // Extraire le script original pour le sauvegarder
            String originalScript = extractOriginalScript(pipelineConfig);

            // Extraire la durée actuelle du build
            Long currentDurationMs = extractCurrentDuration(buildStatistics);

            // 3. Envoyer les données au service AI pour optimisation
            System.out.println("🤖 Envoi vers le service AI pour optimisation...");
            OptimizationResponse aiResponse = aiOptimizationService.optimize(
                    buildStatistics,
                    pipelineConfig,
                    buildLog,
                    buildHistory,
                    agentInfo,
                    serverInfo
            );
            System.out.println("✅ Réponse AI reçue (provider: " + aiResponse.getProvider() + ")");

            // 4. Sauvegarder le résultat dans l'historique
            System.out.println("💾 Sauvegarde dans l'historique...");
            saveOptimizationHistory(
                    connection,
                    pipelineName,
                    buildNumber,
                    aiResponse,
                    originalScript,
                    currentDurationMs
            );
            System.out.println("✅ Historique sauvegardé");

            System.out.println("🎉 Optimisation terminée avec succès !");
            return aiResponse;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'optimisation: " + e.getMessage());
            e.printStackTrace();

            // Tenter de sauvegarder l'erreur dans l'historique
            try {
                JenkinsConnection connection = jenkinsConnectionService.findById(connectionId);
                String originalScript = "Error: Could not retrieve original script";
                optimizationHistoryService.saveFailedOptimization(
                        connection,
                        pipelineName,
                        buildNumber,
                        "Unknown",
                        originalScript,
                        0L,
                        e.getMessage()
                );
            } catch (Exception historyError) {
                System.err.println("⚠️ Impossible de sauvegarder l'erreur dans l'historique: " + historyError.getMessage());
            }

            throw new OptimizationException(pipelineName, buildNumber, e.getMessage());
        }
    }

    /**
     * Sauvegarder le résultat d'optimisation dans l'historique
     */
    private void saveOptimizationHistory(
            JenkinsConnection connection,
            String pipelineName,
            Integer buildNumber,
            OptimizationResponse aiResponse,
            String originalScript,
            Long currentDurationMs
    ) {
        try {
            // Extraire les métriques de gain
            Long estimatedDurationMs = extractEstimatedDuration(aiResponse.getEstimatedGain());
            Double reductionPercentage = extractReductionPercentage(aiResponse.getEstimatedGain());

            // Sauvegarder l'historique
            optimizationHistoryService.saveSuccessfulOptimization(
                    connection,
                    pipelineName,
                    buildNumber,
                    aiResponse.getProvider(),
                    originalScript,
                    aiResponse.getOptimizedScript(),
                    aiResponse.getAnalysis(),
                    aiResponse.getOptimizations(),
                    aiResponse.getEstimatedGain(),
                    currentDurationMs,
                    estimatedDurationMs,
                    reductionPercentage
            );
        } catch (Exception e) {
            System.err.println("⚠️ Erreur sauvegarde historique (non bloquante): " + e.getMessage());
        }
    }

    /**
     * Extraire le script Groovy original de la configuration du pipeline
     */
    private String extractOriginalScript(String pipelineConfigJson) {
        try {
            JsonNode root = objectMapper.readTree(pipelineConfigJson);
            return root.path("script").asText("Script non disponible");
        } catch (Exception e) {
            return "Erreur extraction script: " + e.getMessage();
        }
    }

    /**
     * Extraire la durée actuelle du build depuis les statistiques
     */
    private Long extractCurrentDuration(String buildStatisticsJson) {
        try {
            JsonNode root = objectMapper.readTree(buildStatisticsJson);
            return root.path("duration").asLong(0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Extraire la durée estimée depuis le JSON de gain estimé
     */
    private Long extractEstimatedDuration(String estimatedGainJson) {
        try {
            JsonNode root = objectMapper.readTree(estimatedGainJson);
            return root.path("estimatedDuration").asLong(0L);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extraire le pourcentage de réduction depuis le JSON de gain estimé
     */
    private Double extractReductionPercentage(String estimatedGainJson) {
        try {
            JsonNode root = objectMapper.readTree(estimatedGainJson);
            String percentageStr = root.path("reductionPercentage").asText("0");
            // Enlever le signe % si présent
            percentageStr = percentageStr.replace("%", "").trim();
            return Double.parseDouble(percentageStr);
        } catch (Exception e) {
            return null;
        }
    }
}
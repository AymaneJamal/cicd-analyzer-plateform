package com.cicd.analyzer.pipelineorchestrator.service;

import com.cicd.analyzer.pipelineorchestrator.entity.JenkinsConnection;
import com.cicd.analyzer.pipelineorchestrator.entity.OptimizationHistory;
import com.cicd.analyzer.pipelineorchestrator.enums.OptimizationStatus;
import com.cicd.analyzer.pipelineorchestrator.repository.OptimizationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service métier pour l'historique des optimisations
 * CRUD et sauvegarde des résultats d'optimisation
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OptimizationHistoryService {

    private final OptimizationHistoryRepository optimizationHistoryRepository;
    private final UserService userService;

    /**
     * Sauvegarder un résultat d'optimisation
     * @param history OptimizationHistory entity à sauvegarder
     * @return OptimizationHistory sauvegardé
     */
    public OptimizationHistory save(OptimizationHistory history) {
        return optimizationHistoryRepository.save(history);
    }

    /**
     * Créer et sauvegarder un historique d'optimisation réussie
     */
    public OptimizationHistory saveSuccessfulOptimization(
            JenkinsConnection connection,
            String pipelineName,
            Integer buildNumber,
            String llmProvider,
            String originalScript,
            String optimizedScript,
            String analysisJson,
            String optimizationsJson,
            String estimatedGainJson,
            Long currentDurationMs,
            Long estimatedDurationMs,
            Double reductionPercentage
    ) {
        OptimizationHistory history = new OptimizationHistory();
        history.setJenkinsConnection(connection);
        history.setPipelineName(pipelineName);
        history.setBuildNumber(buildNumber);
        history.setCompletedAt(LocalDateTime.now());
        history.setStatus(OptimizationStatus.COMPLETED);
        history.setLlmProvider(llmProvider);
        history.setOriginalScript(originalScript);
        history.setOptimizedScript(optimizedScript);
        history.setAnalysisJson(analysisJson);
        history.setOptimizationsJson(optimizationsJson);
        history.setEstimatedGainJson(estimatedGainJson);
        history.setCurrentDurationMs(currentDurationMs);
        history.setEstimatedDurationMs(estimatedDurationMs);
        history.setReductionPercentage(reductionPercentage);

        return optimizationHistoryRepository.save(history);
    }

    /**
     * Créer et sauvegarder un historique d'optimisation échouée
     */
    public OptimizationHistory saveFailedOptimization(
            JenkinsConnection connection,
            String pipelineName,
            Integer buildNumber,
            String llmProvider,
            String originalScript,
            Long currentDurationMs,
            String errorMessage
    ) {
        OptimizationHistory history = new OptimizationHistory();
        history.setJenkinsConnection(connection);
        history.setPipelineName(pipelineName);
        history.setBuildNumber(buildNumber);
        history.setCompletedAt(LocalDateTime.now());
        history.setStatus(OptimizationStatus.FAILED);
        history.setLlmProvider(llmProvider);
        history.setOriginalScript(originalScript);
        history.setCurrentDurationMs(currentDurationMs);
        history.setErrorMessage(errorMessage);

        return optimizationHistoryRepository.save(history);
    }

    /**
     * Récupérer l'historique d'optimisations d'une connexion Jenkins
     * @param connectionId ID de la connexion Jenkins
     * @return Liste d'OptimizationHistory (du plus récent au plus ancien)
     */
    public List<OptimizationHistory> findByConnectionId(Long connectionId) {
        return optimizationHistoryRepository.findByJenkinsConnectionIdOrderByCompletedAtDesc(connectionId);
    }

    /**
     * Récupérer tout l'historique d'optimisations d'un utilisateur
     * @param userEmail Email de l'utilisateur
     * @return Liste d'OptimizationHistory (du plus récent au plus ancien)
     */
    public List<OptimizationHistory> findByUserEmail(String userEmail) {
        Long userId = userService.findByEmail(userEmail).getId();
        return optimizationHistoryRepository.findByUserIdOrderByCompletedAtDesc(userId);
    }

    /**
     * Récupérer l'historique d'optimisations pour un pipeline spécifique
     * @param connectionId ID de la connexion Jenkins
     * @param pipelineName Nom du pipeline
     * @return Liste d'OptimizationHistory (du plus récent au plus ancien)
     */
    public List<OptimizationHistory> findByConnectionAndPipeline(Long connectionId, String pipelineName) {
        return optimizationHistoryRepository.findByJenkinsConnectionIdAndPipelineNameOrderByCompletedAtDesc(
                connectionId,
                pipelineName
        );
    }

    /**
     * Récupérer un historique par ID
     * @param id ID de l'historique
     * @return OptimizationHistory
     */
    public OptimizationHistory findById(Long id) {
        return optimizationHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Optimization history not found with id: " + id));
    }
}

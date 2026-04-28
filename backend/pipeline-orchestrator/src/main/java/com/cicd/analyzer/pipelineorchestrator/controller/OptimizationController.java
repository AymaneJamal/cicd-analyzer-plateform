package com.cicd.analyzer.pipelineorchestrator.controller;

import com.cicd.analyzer.pipelineorchestrator.dto.request.OptimizePipelineRequest;
import com.cicd.analyzer.pipelineorchestrator.dto.response.OptimizationResponse;
import com.cicd.analyzer.pipelineorchestrator.entity.OptimizationHistory;
import com.cicd.analyzer.pipelineorchestrator.handler.PipelineOptimizationHandler;
import com.cicd.analyzer.pipelineorchestrator.service.OptimizationHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST pour l'optimisation de pipelines et l'historique
 * Endpoints: POST /optimize, GET /history
 */
@RestController
@RequestMapping("/api/optimization")
@RequiredArgsConstructor
public class OptimizationController {

    private final PipelineOptimizationHandler optimizationHandler;
    private final OptimizationHistoryService optimizationHistoryService;

    /**
     * POST /api/optimization/optimize
     * Optimiser un pipeline Jenkins avec l'IA
     *
     * WORKFLOW COMPLET:
     * 1. Récupérer la connexion Jenkins
     * 2. Récupérer les 6 données Jenkins (statistics, config, logs, history, agents, server)
     * 3. Envoyer au service AI pour optimisation
     * 4. Sauvegarder le résultat dans l'historique
     * 5. Retourner l'OptimizationResponse
     *
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @param request OptimizePipelineRequest (connectionId, pipelineName, buildNumber)
     * @return OptimizationResponse (200 OK)
     */
    @PostMapping("/optimize")
    public ResponseEntity<OptimizationResponse> optimizePipeline(
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody OptimizePipelineRequest request
    ) {
        OptimizationResponse response = optimizationHandler.optimizePipeline(
                userEmail,
                request.getConnectionId(),
                request.getPipelineName(),
                request.getBuildNumber()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/optimization/history?connectionId={id}
     * Récupérer l'historique des optimisations pour une connexion Jenkins
     *
     * @param connectionId ID de la connexion Jenkins
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Liste d'OptimizationHistory (200 OK)
     */
    @GetMapping("/history")
    public ResponseEntity<List<OptimizationHistory>> getHistoryByConnection(
            @RequestParam Long connectionId,
            @RequestHeader("X-User-Email") String userEmail
    ) {
        List<OptimizationHistory> history = optimizationHistoryService.findByConnectionId(connectionId);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/optimization/history/me
     * Récupérer tout l'historique des optimisations de l'utilisateur connecté
     *
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Liste d'OptimizationHistory (200 OK)
     */
    @GetMapping("/history/me")
    public ResponseEntity<List<OptimizationHistory>> getMyHistory(
            @RequestHeader("X-User-Email") String userEmail
    ) {
        List<OptimizationHistory> history = optimizationHistoryService.findByUserEmail(userEmail);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/optimization/history/pipeline?connectionId={id}&pipelineName={name}
     * Récupérer l'historique des optimisations pour un pipeline spécifique
     *
     * @param connectionId ID de la connexion Jenkins
     * @param pipelineName Nom du pipeline
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Liste d'OptimizationHistory (200 OK)
     */
    @GetMapping("/history/pipeline")
    public ResponseEntity<List<OptimizationHistory>> getHistoryByPipeline(
            @RequestParam Long connectionId,
            @RequestParam String pipelineName,
            @RequestHeader("X-User-Email") String userEmail
    ) {
        List<OptimizationHistory> history = optimizationHistoryService.findByConnectionAndPipeline(
                connectionId,
                pipelineName
        );
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/optimization/history/{id}
     * Récupérer un historique d'optimisation par ID
     *
     * @param id ID de l'historique
     * @return OptimizationHistory (200 OK)
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<OptimizationHistory> getHistoryById(
            @PathVariable Long id
    ) {
        OptimizationHistory history = optimizationHistoryService.findById(id);
        return ResponseEntity.ok(history);
    }
}
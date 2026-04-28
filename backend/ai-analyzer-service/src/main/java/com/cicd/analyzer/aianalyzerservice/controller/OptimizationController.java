package com.cicd.analyzer.aianalyzerservice.controller;

import com.cicd.analyzer.aianalyzerservice.dto.OptimizationRequest;
import com.cicd.analyzer.aianalyzerservice.dto.OptimizationResponse;
import com.cicd.analyzer.aianalyzerservice.service.OptimizationService;
import com.cicd.analyzer.aianalyzerservice.util.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST pour l'optimisation de pipelines Jenkins
 * Endpoint principal: POST /api/optimize
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class OptimizationController {

    @Autowired
    private OptimizationService optimizationService;

    @Autowired
    private JsonParser jsonParser;

    /**
     * Endpoint principal: Analyse et optimise un pipeline Jenkins
     *
     * POST /api/optimize
     * Body: OptimizationRequest (6 champs JSON bruts du jenkins-connector)
     *
     * @param request Données du pipeline à optimiser
     * @return Script optimisé + analyse + gains estimés
     */
    @PostMapping("/optimize")
    public ResponseEntity<OptimizationResponse> optimize(@RequestBody OptimizationRequest request) {
        try {
            System.out.println("📥 Requête d'optimisation reçue");

            // Validation basique
            if (request.getBuildStatistics() == null || request.getPipelineConfig() == null) {
                return ResponseEntity.badRequest().build();
            }

            // Appeler le service d'optimisation
            String llmResponse = optimizationService.optimize(
                    request.getBuildStatistics(),
                    request.getPipelineConfig(),
                    request.getBuildLog(),
                    request.getBuildHistory(),
                    request.getAgentInfo(),
                    request.getServerInfo()
            );

            // Parser la réponse du LLM
            String provider = optimizationService.getCurrentProvider();
            OptimizationResponse response = jsonParser.parseOptimizationResponse(llmResponse, provider);

            System.out.println("✅ Optimisation terminée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'optimisation: " + e.getMessage());
            e.printStackTrace();

            // Retourner une erreur avec détails
            OptimizationResponse errorResponse = new OptimizationResponse();
            errorResponse.setAnalysis("{\"error\": \"" + e.getMessage() + "\"}");
            errorResponse.setProvider("N/A");

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        String provider = optimizationService.getCurrentProvider();
        return ResponseEntity.ok("{\"status\": \"UP\", \"provider\": \"" + provider + "\"}");
    }

    /**
     * Endpoint pour connaître le provider LLM actuel
     * GET /api/provider
     */
    @GetMapping("/provider")
    public ResponseEntity<String> getProvider() {
        String provider = optimizationService.getCurrentProvider();
        return ResponseEntity.ok("{\"provider\": \"" + provider + "\"}");
    }
}
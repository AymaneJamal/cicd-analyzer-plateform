package com.cicd.analyzer.pipelineorchestrator.controller;

import com.cicd.analyzer.pipelineorchestrator.dto.request.CreateJenkinsConnectionRequest;
import com.cicd.analyzer.pipelineorchestrator.dto.response.JenkinsConnectionResponse;
import com.cicd.analyzer.pipelineorchestrator.enums.TestStatus;
import com.cicd.analyzer.pipelineorchestrator.handler.JenkinsConnectionTestHandler;
import com.cicd.analyzer.pipelineorchestrator.service.JenkinsConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST pour la gestion des connexions Jenkins
 * Endpoints: CRUD + Test de connexion
 */
@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class JenkinsConnectionController {

    private final JenkinsConnectionService jenkinsConnectionService;
    private final JenkinsConnectionTestHandler jenkinsConnectionTestHandler;

    /**
     * POST /api/connections
     * Créer une nouvelle connexion Jenkins
     *
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @param request CreateJenkinsConnectionRequest (name, url, username, password)
     * @return JenkinsConnectionResponse (201 CREATED)
     */
    @PostMapping
    public ResponseEntity<JenkinsConnectionResponse> createConnection(
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody CreateJenkinsConnectionRequest request
    ) {
        JenkinsConnectionResponse response = jenkinsConnectionService.createConnection(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/connections
     * Récupérer toutes les connexions de l'utilisateur
     *
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return List<JenkinsConnectionResponse> (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<JenkinsConnectionResponse>> getConnections(
            @RequestHeader("X-User-Email") String userEmail
    ) {
        List<JenkinsConnectionResponse> connections = jenkinsConnectionService.findByUser(userEmail);
        return ResponseEntity.ok(connections);
    }

    /**
     * GET /api/connections/active
     * Récupérer les connexions actives de l'utilisateur
     *
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return List<JenkinsConnectionResponse> (200 OK)
     */
    @GetMapping("/active")
    public ResponseEntity<List<JenkinsConnectionResponse>> getActiveConnections(
            @RequestHeader("X-User-Email") String userEmail
    ) {
        List<JenkinsConnectionResponse> connections = jenkinsConnectionService.findActiveByUser(userEmail);
        return ResponseEntity.ok(connections);
    }

    /**
     * POST /api/connections/{id}/test
     * Tester une connexion Jenkins existante
     *
     * @param id ID de la connexion
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return TestStatus (200 OK)
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, String>> testConnection(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String userEmail
    ) {
        TestStatus status = jenkinsConnectionTestHandler.testConnection(id, userEmail);

        Map<String, String> response = new HashMap<>();
        response.put("status", status.name());
        response.put("message", status == TestStatus.SUCCESS ?
                "Connection successful" : "Connection failed");

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/connections/test-credentials
     * Tester des credentials Jenkins avant de créer la connexion
     *
     * @param request CreateJenkinsConnectionRequest (url, username, password)
     * @return Résultat du test (200 OK)
     */
    @PostMapping("/test-credentials")
    public ResponseEntity<Map<String, Object>> testCredentials(
            @Valid @RequestBody CreateJenkinsConnectionRequest request
    ) {
        boolean success = jenkinsConnectionTestHandler.testCredentials(
                request.getUrl(),
                request.getUsername(),
                request.getPassword()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ?
                "Credentials valid" : "Invalid credentials or unreachable server");

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/connections/{id}/toggle
     * Activer/désactiver une connexion
     *
     * @param id ID de la connexion
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @param request Body JSON: {"isActive": true/false}
     * @return JenkinsConnectionResponse (200 OK)
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<JenkinsConnectionResponse> toggleConnection(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestBody ToggleConnectionRequest request
    ) {
        JenkinsConnectionResponse response = jenkinsConnectionService.toggleActive(
                id, userEmail, request.getIsActive()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/connections/{id}
     * Supprimer une connexion
     *
     * @param id ID de la connexion
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return 204 NO CONTENT
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnection(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String userEmail
    ) {
        jenkinsConnectionService.deleteConnection(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    /**
     * DTO interne pour toggle connection
     */
    private static class ToggleConnectionRequest {
        private Boolean isActive;

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }
}
package com.cicd.analyzer.pipelineorchestrator.controller;

import com.cicd.analyzer.pipelineorchestrator.entity.JenkinsConnection;
import com.cicd.analyzer.pipelineorchestrator.service.JenkinsConnectionService;
import com.cicd.analyzer.pipelineorchestrator.service.JenkinsDataService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Controller REST pour récupérer les données Jenkins
 * Endpoints: Pipelines, Builds, Logs, Config, Statistics, Agents, Server Info
 */
@RestController
@RequestMapping("/api/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final JenkinsConnectionService jenkinsConnectionService;
    private final JenkinsDataService jenkinsDataService;
    private final ObjectMapper objectMapper;

    /**
     * GET /api/pipelines?connectionId={id}
     * Récupérer la liste des pipelines d'une connexion Jenkins
     *
     * @param connectionId ID de la connexion Jenkins
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Liste des pipelines (JSON array)
     */
    @GetMapping
    public ResponseEntity<JsonNode> getPipelines(
            @RequestParam Long connectionId,
            @RequestHeader("X-User-Email") String userEmail
    ) throws Exception {
        JenkinsConnection connection = jenkinsConnectionService.findById(connectionId, userEmail);
        String pipelinesJson = jenkinsDataService.getPipelines(connection);
        JsonNode response = objectMapper.readTree(pipelinesJson);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/pipelines/{pipelineName}/builds?connectionId={id}
     * Récupérer l'historique des builds d'un pipeline
     *
     * @param pipelineName Nom du pipeline
     * @param connectionId ID de la connexion Jenkins
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Historique des builds (JSON array)
     */
    @GetMapping("/{pipelineName}/builds")
    public ResponseEntity<JsonNode> getBuilds(
            @PathVariable String pipelineName,
            @RequestParam Long connectionId,
            @RequestHeader("X-User-Email") String userEmail
    ) throws Exception {
        JenkinsConnection connection = jenkinsConnectionService.findById(connectionId, userEmail);
        String buildsJson = jenkinsDataService.getBuildHistory(connection, pipelineName);
        JsonNode response = objectMapper.readTree(buildsJson);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/pipelines/{pipelineName}/builds/{buildNumber}/logs?connectionId={id}
     * Récupérer les logs d'un build spécifique
     *
     * @param pipelineName Nom du pipeline
     * @param buildNumber Numéro du build
     * @param connectionId ID de la connexion Jenkins
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Logs du build (JSON object)
     */
    @GetMapping("/{pipelineName}/builds/{buildNumber}/logs")
    public ResponseEntity<JsonNode> getBuildLogs(
            @PathVariable String pipelineName,
            @PathVariable Integer buildNumber,
            @RequestParam Long connectionId,
            @RequestHeader("X-User-Email") String userEmail
    ) throws Exception {
        JenkinsConnection connection = jenkinsConnectionService.findById(connectionId, userEmail);
        String logsJson = jenkinsDataService.getBuildLog(connection, pipelineName, buildNumber);
        JsonNode response = objectMapper.readTree(logsJson);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/pipelines/{pipelineName}/config?connectionId={id}
     * Récupérer la configuration (script Groovy) d'un pipeline
     *
     * @param pipelineName Nom du pipeline
     * @param connectionId ID de la connexion Jenkins
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Configuration du pipeline (JSON object)
     */
    @GetMapping("/{pipelineName}/config")
    public ResponseEntity<JsonNode> getPipelineConfig(
            @PathVariable String pipelineName,
            @RequestParam Long connectionId,
            @RequestHeader("X-User-Email") String userEmail
    ) throws Exception {
        JenkinsConnection connection = jenkinsConnectionService.findById(connectionId, userEmail);
        String configJson = jenkinsDataService.getPipelineConfig(connection, pipelineName);
        JsonNode response = objectMapper.readTree(configJson);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/pipelines/{pipelineName}/builds/{buildNumber}/statistics?connectionId={id}
     * Récupérer les statistiques détaillées d'un build (durées des stages, etc.)
     *
     * @param pipelineName Nom du pipeline
     * @param buildNumber Numéro du build
     * @param connectionId ID de la connexion Jenkins
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Statistiques du build (JSON object)
     */
    @GetMapping("/{pipelineName}/builds/{buildNumber}/statistics")
    public ResponseEntity<JsonNode> getBuildStatistics(
            @PathVariable String pipelineName,
            @PathVariable Integer buildNumber,
            @RequestParam Long connectionId,
            @RequestHeader("X-User-Email") String userEmail
    ) throws Exception {
        JenkinsConnection connection = jenkinsConnectionService.findById(connectionId, userEmail);
        String statsJson = jenkinsDataService.getBuildStatistics(connection, pipelineName, buildNumber);
        JsonNode response = objectMapper.readTree(statsJson);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/pipelines/agents?connectionId={id}
     * Récupérer la liste des agents Jenkins disponibles
     *
     * @param connectionId ID de la connexion Jenkins
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Liste des agents (JSON array)
     */
    @GetMapping("/agents")
    public ResponseEntity<JsonNode> getAgents(
            @RequestParam Long connectionId,
            @RequestHeader("X-User-Email") String userEmail
    ) throws Exception {
        JenkinsConnection connection = jenkinsConnectionService.findById(connectionId, userEmail);
        String agentsJson = jenkinsDataService.getAgents(connection);
        JsonNode response = objectMapper.readTree(agentsJson);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/pipelines/server-info?connectionId={id}
     * Récupérer les informations du serveur Jenkins
     *
     * @param connectionId ID de la connexion Jenkins
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return Informations serveur (JSON object)
     */
    @GetMapping("/server-info")
    public ResponseEntity<JsonNode> getServerInfo(
            @RequestParam Long connectionId,
            @RequestHeader("X-User-Email") String userEmail
    ) throws Exception {
        JenkinsConnection connection = jenkinsConnectionService.findById(connectionId, userEmail);
        String serverInfoJson = jenkinsDataService.getServerInfo(connection);
        JsonNode response = objectMapper.readTree(serverInfoJson);
        return ResponseEntity.ok(response);
    }
}
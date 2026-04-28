package com.cicd.analyzer.pipelineorchestrator.service;

import com.cicd.analyzer.pipelineorchestrator.client.JenkinsConnectorClient;
import com.cicd.analyzer.pipelineorchestrator.entity.JenkinsConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service pour récupérer les données Jenkins
 * Abstraction au-dessus du JenkinsConnectorClient
 * Convertit les entités JenkinsConnection en appels HTTP
 */
@Service
@RequiredArgsConstructor
public class JenkinsDataService {

    private final JenkinsConnectorClient jenkinsConnectorClient;

    /**
     * Récupérer les informations du serveur Jenkins
     * @param connection JenkinsConnection entity
     * @return JSON string de ServerInfo
     */
    public String getServerInfo(JenkinsConnection connection) {
        return jenkinsConnectorClient.getServerInfo(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword()
        );
    }

    /**
     * Récupérer la liste des pipelines
     * @param connection JenkinsConnection entity
     * @return JSON string de List<Pipeline>
     */
    public String getPipelines(JenkinsConnection connection) {
        return jenkinsConnectorClient.listPipelines(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword()
        );
    }

    /**
     * Récupérer l'historique des builds d'un pipeline
     * @param connection JenkinsConnection entity
     * @param pipelineName Nom du pipeline
     * @return JSON string de List<Build>
     */
    public String getBuildHistory(JenkinsConnection connection, String pipelineName) {
        return jenkinsConnectorClient.getPipelineBuilds(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword(),
                pipelineName
        );
    }

    /**
     * Récupérer les logs d'un build
     * @param connection JenkinsConnection entity
     * @param pipelineName Nom du pipeline
     * @param buildNumber Numéro du build
     * @return JSON string de BuildLog
     */
    public String getBuildLog(JenkinsConnection connection, String pipelineName, Integer buildNumber) {
        return jenkinsConnectorClient.getBuildLogs(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword(),
                pipelineName,
                buildNumber
        );
    }

    /**
     * Récupérer la configuration d'un pipeline
     * @param connection JenkinsConnection entity
     * @param pipelineName Nom du pipeline
     * @return JSON string de PipelineConfig
     */
    public String getPipelineConfig(JenkinsConnection connection, String pipelineName) {
        return jenkinsConnectorClient.getPipelineConfig(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword(),
                pipelineName
        );
    }

    /**
     * Récupérer les statistiques détaillées d'un build (CRITIQUE pour optimisation)
     * @param connection JenkinsConnection entity
     * @param pipelineName Nom du pipeline
     * @param buildNumber Numéro du build
     * @return JSON string de BuildStatistics
     */
    public String getBuildStatistics(JenkinsConnection connection, String pipelineName, Integer buildNumber) {
        return jenkinsConnectorClient.getBuildStatistics(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword(),
                pipelineName,
                buildNumber
        );
    }

    /**
     * Récupérer la liste des agents Jenkins
     * @param connection JenkinsConnection entity
     * @return JSON string de List<AgentInfo>
     */
    public String getAgents(JenkinsConnection connection) {
        return jenkinsConnectorClient.getAgents(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword()
        );
    }
}

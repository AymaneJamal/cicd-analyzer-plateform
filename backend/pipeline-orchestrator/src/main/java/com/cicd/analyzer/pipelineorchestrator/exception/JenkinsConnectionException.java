package com.cicd.analyzer.pipelineorchestrator.exception;

/**
 * Exception levée lors d'erreurs de connexion à Jenkins
 * ou lors d'appels au jenkins-connector service
 */
public class JenkinsConnectionException extends RuntimeException {

    public JenkinsConnectionException(String message) {
        super(message);
    }

    public JenkinsConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public JenkinsConnectionException(Long connectionId) {
        super("Jenkins connection not found with id: " + connectionId);
    }

    public JenkinsConnectionException(Long connectionId, Long userId) {
        super("Jenkins connection " + connectionId + " not found for user " + userId);
    }
}
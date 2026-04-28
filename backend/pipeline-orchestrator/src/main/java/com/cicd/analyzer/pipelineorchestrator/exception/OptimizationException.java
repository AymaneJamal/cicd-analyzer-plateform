package com.cicd.analyzer.pipelineorchestrator.exception;

/**
 * Exception levée lors d'erreurs pendant le processus d'optimisation
 * (collecte des données Jenkins, appel au service AI, parsing, etc.)
 */
public class OptimizationException extends RuntimeException {

    public OptimizationException(String message) {
        super(message);
    }

    public OptimizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public OptimizationException(String pipelineName, Integer buildNumber, String reason) {
        super("Optimization failed for pipeline '" + pipelineName + "' build #" + buildNumber + ": " + reason);
    }
}
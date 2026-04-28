package com.cicd.analyzer.pipelineorchestrator.enums;

/**
 * Statut de test de connexion Jenkins
 */
public enum TestStatus {
    /**
     * Connexion réussie
     */
    SUCCESS,

    /**
     * Connexion échouée
     */
    FAILED,

    /**
     * Connexion non testée
     */
    NOT_TESTED
}
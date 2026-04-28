package com.cicd.analyzer.aianalyzerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse d'optimisation
 * Contient le script optimisé et l'analyse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationResponse {

    /**
     * Script Groovy optimisé COMPLET
     */
    private String optimizedScript;

    /**
     * Analyse détaillée (bottlenecks, issues détectées)
     * Format JSON
     */
    private String analysis;

    /**
     * Gain estimé (pourcentage, durées)
     * Format JSON
     */
    private String estimatedGain;

    /**
     * Liste des optimisations suggérées
     * Format JSON
     */
    private String optimizations;

    /**
     * Provider LLM utilisé (Gemini, OpenAI)
     */
    private String provider;

    /**
     * Réponse brute complète du LLM (pour debug)
     */
    private String rawResponse;
}
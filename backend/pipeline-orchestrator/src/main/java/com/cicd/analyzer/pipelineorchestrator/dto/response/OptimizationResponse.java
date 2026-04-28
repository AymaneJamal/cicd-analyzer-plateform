package com.cicd.analyzer.pipelineorchestrator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour Optimization (depuis ai-analyzer)
 * Wrapper autour de la réponse du service AI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationResponse {

    /**
     * Script Groovy optimisé complet
     */
    private String optimizedScript;

    /**
     * Analyse détaillée (bottlenecks, issues) en JSON
     */
    private String analysis;

    /**
     * Gain estimé (durées, pourcentages) en JSON
     */
    private String estimatedGain;

    /**
     * Liste des optimisations suggérées en JSON
     */
    private String optimizations;

    /**
     * Provider LLM utilisé (Groq, Gemini, OpenAI)
     */
    private String provider;
}
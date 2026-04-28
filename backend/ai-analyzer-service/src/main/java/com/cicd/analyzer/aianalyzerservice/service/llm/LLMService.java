package com.cicd.analyzer.aianalyzerservice.service.llm;

/**
 * Interface commune pour tous les providers LLM (Gemini, OpenAI, Claude, etc.)
 * Permet de switcher facilement entre providers sans changer le code métier
 */
public interface LLMService {

    /**
     * Analyse un pipeline Jenkins et suggère des optimisations
     *
     * @param prompt Le prompt complet (documents + données Jenkins)
     * @return La réponse du LLM (JSON structuré avec script optimisé)
     */
    String analyze(String prompt);

    /**
     * Retourne le nom du provider (pour logging/debug)
     *
     * @return Nom du provider (ex: "Gemini", "OpenAI")
     */
    String getProviderName();
}

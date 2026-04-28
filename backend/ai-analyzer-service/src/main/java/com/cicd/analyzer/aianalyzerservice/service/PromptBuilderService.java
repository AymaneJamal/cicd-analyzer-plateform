package com.cicd.analyzer.aianalyzerservice.service;

import org.springframework.stereotype.Service;

/**
 * Service qui construit le prompt pour le LLM
 * Combine: Documents de référence + Données Jenkins + Instructions
 */
@Service
public class PromptBuilderService {

    /**
     * Construit le prompt complet pour l'analyse et l'optimisation
     *
     * @param knowledgeBase Documents de référence (.md)
     * @param buildStatistics Stats du build (JSON brut)
     * @param pipelineConfig Config actuelle (JSON brut)
     * @param buildLog Logs d'exécution (texte brut)
     * @param buildHistory Historique des builds (JSON brut)
     * @param agentInfo Info agents Jenkins (JSON brut)
     * @param serverInfo Info serveur Jenkins (JSON brut)
     * @return Prompt complet prêt pour le LLM
     */
    public String buildOptimizationPrompt(
            String knowledgeBase,
            String buildStatistics,
            String pipelineConfig,
            String buildLog,
            String buildHistory,
            String agentInfo,
            String serverInfo
    ) {
        return "# MISSION: Expert DevOps - Optimisation Pipeline Jenkins\n" +
                "\n" +
                "Tu es un expert DevOps spécialisé en optimisation de pipelines CI/CD Jenkins.\n" +
                "Ton objectif: analyser ce pipeline et suggérer des optimisations CONCRÈTES et APPLICABLES.\n" +
                "\n" +
                "---\n" +
                "\n" +
                knowledgeBase +
                "\n" +
                "---\n" +
                "\n" +
                "# DONNÉES DU PIPELINE À ANALYSER\n" +
                "\n" +
                "## 1. STATISTIQUES DU BUILD (durées des stages - CRITIQUE)\n" +
                "```json\n" +
                buildStatistics +
                "\n```\n" +
                "\n" +
                "## 2. CONFIGURATION ACTUELLE (script Groovy)\n" +
                "```json\n" +
                pipelineConfig +
                "\n```\n" +
                "\n" +
                "## 3. LOGS D'EXÉCUTION (patterns et warnings)\n" +
                "```\n" +
                truncateLogs(buildLog, 5000) +
                "\n```\n" +
                "\n" +
                "## 4. HISTORIQUE DES BUILDS (tendances)\n" +
                "```json\n" +
                buildHistory +
                "\n```\n" +
                "\n" +
                "## 5. AGENTS DISPONIBLES (ressources)\n" +
                "```json\n" +
                agentInfo +
                "\n```\n" +
                "\n" +
                "## 6. INFO SERVEUR JENKINS\n" +
                "```json\n" +
                serverInfo +
                "\n```\n" +
                "\n" +
                "---\n" +
                "\n" +
                "# INSTRUCTIONS\n" +
                "\n" +
                "1. **ANALYSE** les données (bottlenecks, patterns, problèmes)\n" +
                "2. **IDENTIFIE** 3-5 optimisations prioritaires basées sur la KNOWLEDGE BASE\n" +
                "3. **GÉNÈRE** le script Groovy optimisé COMPLET\n" +
                "4. **ESTIME** le gain de temps attendu\n" +
                "\n" +
                "## FORMAT DE RÉPONSE (JSON strict)\n" +
                "```json\n" +
                "{\n" +
                "  \"analysis\": {\n" +
                "    \"bottlenecks\": [\n" +
                "      {\n" +
                "        \"stage\": \"nom du stage\",\n" +
                "        \"duration\": durée en ms,\n" +
                "        \"percentage\": pourcentage du temps total,\n" +
                "        \"issue\": \"description du problème\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"detectedIssues\": [\n" +
                "      \"Issue 1 trouvée dans les logs\",\n" +
                "      \"Issue 2 détectée\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"optimizations\": [\n" +
                "    {\n" +
                "      \"priority\": 1,\n" +
                "      \"title\": \"Titre court\",\n" +
                "      \"description\": \"Explication détaillée\",\n" +
                "      \"estimatedGain\": \"30-45s\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"optimizedScript\": \"pipeline {\\n  // Script Groovy COMPLET optimisé\\n}\",\n" +
                "  \"estimatedGain\": {\n" +
                "    \"currentDuration\": durée actuelle en ms,\n" +
                "    \"estimatedDuration\": durée estimée en ms,\n" +
                "    \"reductionPercentage\": \"40%\"\n" +
                "  }\n" +
                "}\n" +
                "```\n" +
                "\n" +
                "**INSTRUCTIONS CRITIQUES - FORMAT DE RÉPONSE:**\n" +
                "\n" +
                "1. COMMENCE IMMÉDIATEMENT par ```json\n" +
                "2. Écris le JSON complet et VALIDE\n" +
                "3. TERMINE par ``` \n" +
                "4. AUCUN autre texte avant, après ou au milieu\n" +
                "5. VÉRIFIE que ton JSON est complet avant d'envoyer\n" +
                "6. Si tu ne peux pas finir, écris au moins les 4 champs principaux\n" +
                "\n" +
                "**STRUCTURE OBLIGATOIRE (tous les champs REQUIS):**\n" +
                "```json\n" +
                "{\n" +
                "  \"analysis\": {\n" +
                "    \"bottlenecks\": [...],\n" +
                "    \"detectedIssues\": [...]\n" +
                "  },\n" +
                "  \"optimizations\": [...],\n" +
                "  \"optimizedScript\": \"pipeline { ... }\",\n" +
                "  \"estimatedGain\": {\n" +
                "    \"currentDuration\": 0,\n" +
                "    \"estimatedDuration\": 0,\n" +
                "    \"reductionPercentage\": \"0%\"\n" +
                "  }\n" +
                "}\n" +
                "```\n" +
                "\n" +
                "**VALIDATION FINALE:**\n" +
                "- Compte tes accolades { } \n" +
                "- Vérifie que chaque champ obligatoire existe\n" +
                "- Assure-toi que le JSON se termine correctement par }\n" +
                "- SI TU ES COUPÉ, mets au moins {} vides pour chaque champ\n" +
                "\n" +
                "**DÉBUT DE TA RÉPONSE - MAINTENANT:**";
    }

    /**
     * Tronque les logs si trop longs (évite dépassement limite tokens)
     */
    private String truncateLogs(String logs, int maxLength) {
        if (logs == null) return "";
        if (logs.length() <= maxLength) return logs;

        return logs.substring(0, maxLength) +
                "\n\n... [LOGS TRONQUÉS - " + (logs.length() - maxLength) + " caractères omis] ...";
    }
}
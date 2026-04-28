package com.cicd.analyzer.aianalyzerservice.service;

import com.cicd.analyzer.aianalyzerservice.service.llm.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Service d'orchestration principale
 * Coordonne: Documents + Prompt + LLM + Parsing
 */
@Service
public class OptimizationService {

    @Autowired
    private DocumentLoaderService documentLoader;

    @Autowired
    private PromptBuilderService promptBuilder;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${llm.provider}")
    private String currentProvider;

    private LLMService llmService;

    /**
     * Récupère le LLMService dynamiquement selon le provider configuré
     */
    private LLMService getLLMService() {
        if (llmService == null) {
            llmService = (LLMService) applicationContext.getBean(currentProvider);
        }
        return llmService;
    }

    /**
     * Analyse et optimise un pipeline Jenkins
     *
     * @param buildStatistics Stats du build (JSON brut)
     * @param pipelineConfig Config actuelle (JSON brut)
     * @param buildLog Logs d'exécution (texte brut)
     * @param buildHistory Historique (JSON brut)
     * @param agentInfo Agents disponibles (JSON brut)
     * @param serverInfo Info serveur (JSON brut)
     * @return Réponse du LLM (JSON structuré)
     */
    public String optimize(
            String buildStatistics,
            String pipelineConfig,
            String buildLog,
            String buildHistory,
            String agentInfo,
            String serverInfo
    ) {
        try {
            System.out.println("🚀 Démarrage optimisation avec provider: " + currentProvider);

            // 1. Charger les documents de référence
            String knowledgeBase = documentLoader.getAllDocuments();
            System.out.println("📚 Documents chargés");

            // 2. Construire le prompt
            String prompt = promptBuilder.buildOptimizationPrompt(
                    knowledgeBase,
                    buildStatistics,
                    pipelineConfig,
                    buildLog,
                    buildHistory,
                    agentInfo,
                    serverInfo
            );
            System.out.println("📝 Prompt construit (" + prompt.length() + " caractères)");

            // 3. Appeler le LLM
            LLMService service = getLLMService();
            System.out.println("🤖 Appel " + service.getProviderName() + " API...");
            String response = service.analyze(prompt);
            System.out.println("✅ Réponse reçue (" + response.length() + " caractères)");

            return response;

        } catch (Exception e) {
            System.err.println("❌ Erreur optimisation: " + e.getMessage());
            throw new RuntimeException("Optimization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Retourne le provider actuellement utilisé
     */
    public String getCurrentProvider() {
        return getLLMService().getProviderName();
    }
}
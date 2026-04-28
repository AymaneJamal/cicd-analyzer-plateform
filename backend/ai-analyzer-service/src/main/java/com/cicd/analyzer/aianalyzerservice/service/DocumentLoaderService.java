package com.cicd.analyzer.aianalyzerservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Service qui charge automatiquement TOUS les fichiers .md du dossier knowledge-base
 * Ajouter un nouveau .md = automatiquement utilisé, ZERO code à modifier
 */
@Service
public class DocumentLoaderService {

    @Value("${knowledge-base.path:classpath:knowledge-base/}")
    private String knowledgeBasePath;

    private String cachedDocuments;

    /**
     * Charge tous les documents au démarrage de l'application
     */
    @PostConstruct
    public void init() {
        try {
            this.cachedDocuments = loadAllDocuments();
            System.out.println("✅ Documents chargés: " + countDocuments() + " fichiers");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement documents: " + e.getMessage());
            this.cachedDocuments = "";
        }
    }

    /**
     * Retourne tous les documents chargés (cached)
     */
    public String getAllDocuments() {
        return cachedDocuments;
    }

    /**
     * Charge tous les fichiers .md du dossier knowledge-base
     */
    private String loadAllDocuments() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(knowledgeBasePath + "*.md");

        StringBuilder allDocs = new StringBuilder();
        allDocs.append("# KNOWLEDGE BASE - Best Practices CI/CD\n\n");

        for (Resource resource : resources) {
            allDocs.append("---\n");
            allDocs.append("## Document: ").append(resource.getFilename()).append("\n\n");

            String content = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            allDocs.append(content);
            allDocs.append("\n\n");
        }

        return allDocs.toString();
    }

    /**
     * Compte le nombre de documents chargés
     */
    private int countDocuments() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(knowledgeBasePath + "*.md");
        return resources.length;
    }

    /**
     * Recharge les documents (utile pour refresh sans redémarrage)
     */
    public void reload() {
        try {
            this.cachedDocuments = loadAllDocuments();
            System.out.println("🔄 Documents rechargés");
        } catch (Exception e) {
            System.err.println("❌ Erreur rechargement: " + e.getMessage());
        }
    }
}

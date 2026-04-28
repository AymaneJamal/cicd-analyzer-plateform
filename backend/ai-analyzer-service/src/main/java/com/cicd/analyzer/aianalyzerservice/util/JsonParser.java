package com.cicd.analyzer.aianalyzerservice.util;

import com.cicd.analyzer.aianalyzerservice.dto.OptimizationResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Utilitaire pour parser la réponse JSON du LLM
 * Extrait: optimizedScript, analysis, estimatedGain, optimizations
 */
@Component
public class JsonParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse la réponse brute du LLM en OptimizationResponse
     *
     * @param llmResponse Réponse brute du LLM (JSON string)
     * @param provider Nom du provider utilisé
     * @return OptimizationResponse structuré
     */
    public OptimizationResponse parseOptimizationResponse(String llmResponse, String provider) {
        try {
            // Nettoyer la réponse (enlever markdown si présent)
            String cleanedJson = cleanJsonResponse(llmResponse);

            // Parser le JSON
            JsonNode root = objectMapper.readTree(cleanedJson);

            OptimizationResponse response = new OptimizationResponse();

            // Extraire les champs
            response.setOptimizedScript(extractString(root, "optimizedScript"));
            response.setAnalysis(extractJsonString(root, "analysis"));
            response.setEstimatedGain(extractJsonString(root, "estimatedGain"));
            response.setOptimizations(extractJsonString(root, "optimizations"));
            response.setProvider(provider);
            response.setRawResponse(llmResponse);

            return response;

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing JSON: " + e.getMessage());

            // Retourner une réponse avec l'erreur
            OptimizationResponse errorResponse = new OptimizationResponse();
            errorResponse.setProvider(provider);
            errorResponse.setRawResponse(llmResponse);
            errorResponse.setAnalysis("{\"error\": \"" + e.getMessage() + "\"}");

            return errorResponse;
        }
    }

    /**
     * Nettoie la réponse du LLM (enlève ```json, ```markdown, etc.)
     */
    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";

        // Enlever les balises markdown
        String cleaned = response
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        // Si commence pas par {, chercher le premier {
        if (!cleaned.startsWith("{")) {
            int startIndex = cleaned.indexOf("{");
            if (startIndex != -1) {
                cleaned = cleaned.substring(startIndex);
            }
        }

        // Si finit pas par }, chercher le dernier }
        if (!cleaned.endsWith("}")) {
            int endIndex = cleaned.lastIndexOf("}");
            if (endIndex != -1) {
                cleaned = cleaned.substring(0, endIndex + 1);
            }
        }

        return cleaned;
    }

    /**
     * Extrait un champ string du JSON
     */
    private String extractString(JsonNode root, String fieldName) {
        JsonNode node = root.path(fieldName);
        return node.isMissingNode() ? "" : node.asText();
    }

    /**
     * Extrait un champ objet/array du JSON et le retourne en string JSON
     */
    private String extractJsonString(JsonNode root, String fieldName) {
        try {
            JsonNode node = root.path(fieldName);
            if (node.isMissingNode()) return "{}";
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }
}
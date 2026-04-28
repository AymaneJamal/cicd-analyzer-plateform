package com.cicd.analyzer.aianalyzerservice.service.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;


@Service("gemini")
public class GeminiService implements LLMService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String analyze(String prompt) {
        return analyzeWithRetry(prompt, 3); // 3 tentatives max
    }

    private String analyzeWithRetry(String prompt, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("🤖 Tentative Gemini " + attempt + "/" + maxRetries);
                
                // Build Gemini request
                ObjectNode requestBody = objectMapper.createObjectNode();

                ArrayNode contents = objectMapper.createArrayNode();
                ObjectNode content = objectMapper.createObjectNode();
                ArrayNode parts = objectMapper.createArrayNode();
                ObjectNode part = objectMapper.createObjectNode();

                part.put("text", prompt);
                parts.add(part);
                content.set("parts", parts);
                contents.add(content);

                requestBody.set("contents", contents);

                // Add generation config
                ObjectNode generationConfig = objectMapper.createObjectNode();
                generationConfig.put("temperature", 0.3);
                generationConfig.put("maxOutputTokens", 32768); // Maximum pour Gemini 2.5 Flash
                requestBody.set("generationConfig", generationConfig);

                // Headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Call API
                String url = apiUrl + "?key=" + apiKey;
                HttpEntity<String> entity = new HttpEntity<>(
                        objectMapper.writeValueAsString(requestBody),
                        headers
                );

                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                // Vérifier le contenu de la réponse AVANT parsing
                String responseBody = response.getBody();
                System.out.println("📡 Réponse Gemini reçue (" + (responseBody != null ? responseBody.length() : 0) + " caractères)");
                
                // Détecter les vraies erreurs dans le contenu (pas les réponses valides)
                if (isGeminiError(responseBody)) {
                    System.err.println("❌ Erreur détectée dans la réponse Gemini: " + responseBody);
                    
                    if (attempt < maxRetries) {
                        // Wait avec backoff exponentiel
                        int waitTime = attempt * 2000; // 2s, 4s, 6s...
                        System.out.println("⏳ Attente " + waitTime + "ms avant retry...");
                        Thread.sleep(waitTime);
                        continue;
                    } else {
                        // Dernière tentative échouée, retourner fallback
                        return createFallbackResponse("Erreur Gemini: " + responseBody);
                    }
                }
                
                // Vérifier si la réponse est tronquée (MAX_TOKENS) - ACCEPTER COMME VALIDE
                boolean isTruncated = isTruncatedResponse(responseBody);
                if (isTruncated) {
                    System.out.println("⚠️ Réponse Gemini tronquée (MAX_TOKENS) - Acceptée comme valide");
                    // Ne pas faire de retry - accepter la réponse tronquée
                }

                // Valider que c'est du JSON valide
                if (!isValidJson(responseBody)) {
                    System.err.println("❌ Réponse Gemini n'est pas du JSON valide: " + responseBody);
                    
                    if (attempt < maxRetries) {
                        Thread.sleep(attempt * 2000);
                        continue;
                    } else {
                        return createFallbackResponse("JSON invalide: " + responseBody);
                    }
                }

                // Parse response seulement si valide
                JsonNode root = objectMapper.readTree(responseBody);
                
                String finishReason = root.path("candidates")
                        .get(0)
                        .path("finishReason")
                        .asText();
                        
                String result = root.path("candidates")
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();
                
                // DEBUG: Afficher le contenu brut complet pour diagnostic
                System.out.println("🔍 DEBUG - finishReason: " + finishReason);
                System.out.println("🔍 DEBUG - Réponse complète Gemini (" + responseBody.length() + " chars):");
                System.out.println("=".repeat(80));
                System.out.println(responseBody);
                System.out.println("=".repeat(80));
                
                System.out.println("🔍 DEBUG - text content (premiers 500 chars): " + 
                    (result.length() > 500 ? result.substring(0, 500) + "..." : result));
                System.out.println("🔍 DEBUG - text content (derniers 200 chars): " + 
                    (result.length() > 200 ? "..." + result.substring(result.length() - 200) : result));
                
                if ("MAX_TOKENS".equals(finishReason)) {
                    System.out.println("⚠️ Réponse tronquée - finishReason: MAX_TOKENS - Réparation automatique");
                    result = repairPartialJson(result);
                    System.out.println("✅ JSON partiel réparé et accepté");
                    return result;
                }
                
                // Pour finishReason: STOP - vérifier si le JSON est vraiment incomplet
                if ("STOP".equals(finishReason)) {
                    // Essayer de nettoyer et parser directement
                    String cleanedResult = cleanJsonContent(result);
                    
                    // Vérification rapide si la réponse semble tronquée
                    if (isTruncatedContent(result)) {
                        System.out.println("⚠️ Réponse STOP mais contenu visiblement tronqué - Utilisation fallback direct");
                        return createMinimalFallbackJson();
                    }
                    
                    try {
                        objectMapper.readTree(cleanedResult);
                        System.out.println("✅ Réponse STOP avec JSON valide - Acceptée directement");
                        return cleanedResult;
                    } catch (Exception e) {
                        System.out.println("⚠️ Réponse STOP mais JSON invalide: " + e.getMessage());
                        
                        // Si c'est clairement tronqué (caractère inattendu), ne pas essayer de réparer
                        if (e.getMessage().contains("Unexpected character") || 
                            e.getMessage().contains("was expecting")) {
                            System.out.println("🚫 JSON clairement tronqué - Fallback direct (pas de réparation)");
                            return createMinimalFallbackJson();
                        }
                    }
                }
                
                // STRATEGIE: Toujours tenter de réparer le JSON s'il est incomplet
                if (!isCompleteJson(result)) {
                    System.out.println("⚠️ JSON incomplet détecté - Réparation automatique (finishReason: " + finishReason + ")");
                    result = repairPartialJson(result);
                    
                    // Vérifier si la réparation a réussi
                    if (isCompleteJson(result)) {
                        System.out.println("✅ JSON réparé avec succès");
                        return result;
                    } else if (attempt < maxRetries) {
                        System.out.println("⚠️ Réparation échouée - Retry " + (attempt + 1));
                        continue;
                    } else {
                        System.out.println("⚠️ Réparation échouée - Utilisation fallback");
                        return createMinimalFallbackJson();
                    }
                }
                
                System.out.println("✅ Réponse Gemini parsée avec succès");
                return result;

            } catch (Exception e) {
                lastException = e;
                System.err.println("❌ Erreur tentative " + attempt + ": " + e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(attempt * 2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        // Toutes les tentatives ont échoué
        System.err.println("❌ Toutes les tentatives Gemini ont échoué");
        return createFallbackResponse("Échec après " + maxRetries + " tentatives: " + 
                (lastException != null ? lastException.getMessage() : "Erreur inconnue"));
    }

    /**
     * Détecte si la réponse Gemini contient une erreur (pas les réponses tronquées valides)
     */
    private boolean isGeminiError(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return true;
        }
        
        String lowerBody = responseBody.toLowerCase();
        // Ne détecter que les vraies erreurs, pas les réponses valides
        return lowerBody.contains("toomanyrequests") ||
               lowerBody.contains("quota exceeded") ||
               lowerBody.contains("api key") ||
               (lowerBody.contains("error") && !lowerBody.contains("candidates"));
    }

    /**
     * Détecte si la réponse est tronquée à cause de MAX_TOKENS
     */
    private boolean isTruncatedResponse(String responseBody) {
        if (responseBody == null) return true;
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String finishReason = root.path("candidates")
                    .get(0)
                    .path("finishReason")
                    .asText();
            return "MAX_TOKENS".equals(finishReason);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si le JSON dans le champ text est complet
     */
    private boolean isCompleteJson(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            return false;
        }
        
        // Nettoyer le contenu (enlever ```json, etc.)
        String cleaned = cleanJsonContent(jsonText);
        
        // Debug pour voir le contenu nettoyé
        System.out.println("🔍 DEBUG - JSON après nettoyage (" + cleaned.length() + " chars): " + 
            (cleaned.length() > 200 ? cleaned.substring(0, 200) + "..." : cleaned));
        
        try {
            // Essayer de parser le JSON nettoyé
            JsonNode parsed = objectMapper.readTree(cleaned);
            
            // Vérifier que les champs obligatoires existent
            boolean hasAnalysis = parsed.has("analysis");
            boolean hasOptimizations = parsed.has("optimizations");
            boolean hasOptimizedScript = parsed.has("optimizedScript");
            boolean hasEstimatedGain = parsed.has("estimatedGain");
            
            System.out.println("🔍 DEBUG - Champs détectés: analysis=" + hasAnalysis + 
                ", optimizations=" + hasOptimizations + ", script=" + hasOptimizedScript + 
                ", gain=" + hasEstimatedGain);
                
            return hasAnalysis && hasOptimizations && hasOptimizedScript && hasEstimatedGain;
            
        } catch (Exception e) {
            System.out.println("🔍 DEBUG - Erreur parsing JSON: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Nettoie le contenu JSON en enlevant les balises markdown et espaces
     */
    private String cleanJsonContent(String content) {
        if (content == null) return "{}";
        
        String cleaned = content.trim();
        
        // Enlever les balises markdown au début et à la fin
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }
        
        // Trouver le début et la fin du JSON
        int startBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        
        if (startBrace != -1 && lastBrace != -1 && lastBrace > startBrace) {
            cleaned = cleaned.substring(startBrace, lastBrace + 1);
        }
        
        // NE PAS appliquer fixJsonEscaping - cela corrompt le JSON valide de Gemini !
        // cleaned = fixJsonEscaping(cleaned);
        
        return cleaned.trim();
    }
    
    /**
     * Corrige les échappements invalides dans le JSON (comme \$ dans les scripts Groovy)
     */
    private String fixJsonEscaping(String json) {
        if (json == null) return "{}";
        
        System.out.println("🔧 Correction robuste des échappements JSON...");
        
        // Approche plus ciblée : corriger seulement dans les valeurs de strings, pas les noms de propriétés
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean inPropertyName = false;
        boolean escaped = false;
        
        for (int i = 0; i < json.length(); i++) {
            char current = json.charAt(i);
            char next = (i + 1 < json.length()) ? json.charAt(i + 1) : '\0';
            char prev = (i > 0) ? json.charAt(i - 1) : '\0';
            
            if (escaped) {
                // On était dans un échappement, ajouter le caractère tel quel
                result.append(current);
                escaped = false;
                continue;
            }
            
            if (current == '\\') {
                // Début d'un échappement potentiel
                if (inString && !inPropertyName) {
                    // On est dans une valeur de string (pas un nom de propriété)
                    // Vérifier si c'est un échappement problématique
                    if (next == '$' || next == '{' || next == '}') {
                        // Doubler le backslash pour rendre l'échappement valide en JSON
                        result.append("\\\\");
                        continue;
                    } else if (next == 'n' || next == 't' || next == 'r') {
                        // Vérifier si c'est un vrai échappement ou une séquence invalide
                        result.append("\\\\");
                        continue;
                    }
                }
                escaped = true;
                result.append(current);
                continue;
            }
            
            if (current == '"' && !escaped) {
                // Début ou fin d'une string
                if (!inString) {
                    // Début d'une string - déterminer si c'est un nom de propriété ou une valeur
                    inString = true;
                    // Vérifier si on suit un caractère d'ouverture d'objet ou une virgule (nom de propriété)
                    int j = i - 1;
                    while (j >= 0 && Character.isWhitespace(json.charAt(j))) j--;
                    inPropertyName = (j >= 0 && (json.charAt(j) == '{' || json.charAt(j) == ','));
                } else {
                    // Fin d'une string
                    inString = false;
                    inPropertyName = false;
                }
            }
            
            result.append(current);
        }
        
        String fixed = result.toString();
        
        // Post-traitement pour nettoyer les caractères de contrôle réels
        fixed = fixed.replace("\n", "\\n");
        fixed = fixed.replace("\r", "\\r");
        fixed = fixed.replace("\t", "\\t");
        
        System.out.println("🔧 Correction robuste terminée");
        return fixed;
    }
    
    /**
     * Essaie de réparer un JSON partiel en fermant les objets/arrays ouverts
     */
    private String repairPartialJson(String partialJson) {
        if (partialJson == null) return createMinimalFallbackJson();
        
        // Utiliser la fonction de nettoyage améliorée
        String cleaned = cleanJsonContent(partialJson);
        
        System.out.println("🔧 Réparation intelligente du JSON partiel (" + cleaned.length() + " chars)...");
        
        try {
            // Essayer de parser tel quel d'abord
            JsonNode parsed = objectMapper.readTree(cleaned);
            System.out.println("✅ JSON déjà valide, aucune réparation nécessaire");
            return cleaned;
        } catch (Exception e) {
            System.out.println("🔧 JSON invalide, tentative de réparation: " + e.getMessage());
            
            // Si c'est un problème d'échappement, essayer UNE correction ciblée
            if (e.getMessage().contains("Unrecognized character escape")) {
                System.out.println("🔧 Problème d'échappement détecté - correction ciblée...");
                
                // Correction MINIMALE : seulement les \$ non échappés dans les strings JSON
                String fixedJson = cleaned.replaceAll("(?<!\\\\)\\\\\\$", "\\\\\\\\\\$");
                
                try {
                    JsonNode reparsed = objectMapper.readTree(fixedJson);
                    System.out.println("✅ Correction ciblée réussie - JSON valide");
                    return fixedJson;
                } catch (Exception e2) {
                    System.out.println("⚠️ Correction ciblée échouée: " + e2.getMessage());
                    System.out.println("⚠️ Utilisation du fallback pour éviter plus de corruption");
                    return createMinimalFallbackJson();
                }
            }
        }
        
        StringBuilder repaired = new StringBuilder(cleaned);
        
        // 1. Fermer les strings ouvertes
        boolean inString = false;
        boolean escaped = false;
        char lastNonWhitespace = ' ';
        
        for (int i = cleaned.length() - 1; i >= 0; i--) {
            char c = cleaned.charAt(i);
            if (!Character.isWhitespace(c)) {
                lastNonWhitespace = c;
                break;
            }
        }
        
        // Si se termine au milieu d'une string
        if (lastNonWhitespace != '"' && lastNonWhitespace != '}' && lastNonWhitespace != ']') {
            // Trouver la dernière clé pour fermer proprement
            if (cleaned.contains("\"issue\": \"") || cleaned.contains("\"description\": \"") || 
                cleaned.contains("\"title\": \"") || cleaned.contains("\"stage\": \"")) {
                repaired.append("\"");
            }
        }
        
        // 2. Équilibrer les brackets et braces
        int openBraces = 0, closeBraces = 0;
        int openBrackets = 0, closeBrackets = 0;
        inString = false;
        escaped = false;
        
        for (char c : repaired.toString().toCharArray()) {
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == '{') openBraces++;
                else if (c == '}') closeBraces++;
                else if (c == '[') openBrackets++;
                else if (c == ']') closeBrackets++;
            }
        }
        
        // 3. Fermer les structures ouvertes
        // Fermer d'abord les arrays, puis les objets
        for (int i = 0; i < (openBrackets - closeBrackets); i++) {
            repaired.append("]");
        }
        
        for (int i = 0; i < (openBraces - closeBraces); i++) {
            repaired.append("}");
        }
        
        String result = repaired.toString();
        
        // 4. Validation finale
        try {
            objectMapper.readTree(result);
            System.out.println("✅ JSON réparé avec succès (" + result.length() + " chars)");
            return result;
        } catch (Exception e) {
            System.err.println("❌ Échec réparation JSON, utilisation du fallback");
            return createMinimalFallbackJson();
        }
    }
    
    /**
     * Détecte si le contenu semble tronqué (finit au milieu d'un mot/phrase)
     */
    private boolean isTruncatedContent(String content) {
        if (content == null || content.length() < 50) return true;
        
        String trimmed = content.trim();
        
        // Vérifications de troncature évidente
        if (trimmed.endsWith("...")) return false; // Ellipses volontaires
        if (trimmed.endsWith("}")) return false;   // JSON potentiellement complet
        if (trimmed.endsWith("```")) return false; // Markdown fermé
        
        // Vérifie les 50 derniers caractères
        String lastPart = trimmed.substring(Math.max(0, trimmed.length() - 50));
        
        // Signes de troncature au milieu d'un mot
        if (lastPart.matches(".*[a-zA-Z]{3,}$")) {
            System.out.println("🔍 Détection troncature: finit au milieu d'un mot");
            return true;
        }
        
        // Signes de troncature au milieu d'une phrase
        if (lastPart.matches(".*\\s[a-zA-Z]{1,3}$")) {
            System.out.println("🔍 Détection troncature: finit au milieu d'une phrase");
            return true;
        }
        
        // JSON manifestement incomplet
        long openBraces = lastPart.chars().filter(c -> c == '{').count();
        long closeBraces = lastPart.chars().filter(c -> c == '}').count();
        if (openBraces > closeBraces + 2) {
            System.out.println("🔍 Détection troncature: trop d'accolades ouvertes");
            return true;
        }
        
        return false;
    }
    private String createMinimalFallbackJson() {
        return "{"
            + "\"analysis\": {"
                + "\"bottlenecks\": [{"
                    + "\"stage\": \"Réponse partielle\","
                    + "\"duration\": 0,"
                    + "\"percentage\": 0,"
                    + "\"issue\": \"Réponse tronquée - contenu partiel récupéré\""
                + "}],"
                + "\"detectedIssues\": [\"Réponse Gemini tronquée - analyse partielle\"]"
            + "},"
            + "\"optimizations\": [{"
                + "\"priority\": 1,"
                + "\"title\": \"Réponse partielle\","
                + "\"description\": \"Optimisations disponibles mais réponse tronquée\","
                + "\"estimatedGain\": \"Estimation incomplète\""
            + "}],"
            + "\"optimizedScript\": \"pipeline {\\n    agent any\\n    stages {\\n        stage('Optimisation partielle') {\\n            steps {\\n                echo 'Réponse Gemini tronquée - script original conservé'\\n            }\\n        }\\n    }\\n}\","
            + "\"estimatedGain\": {"
                + "\"currentDuration\": 0,"
                + "\"estimatedDuration\": 0,"
                + "\"reductionPercentage\": \"0%\""
            + "}"
        + "}";
    }

    /**
     * Valide si la réponse est du JSON
     */
    private boolean isValidJson(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return false;
        }
        
        try {
            objectMapper.readTree(responseBody);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Crée une réponse de fallback structurée
     */
    private String createFallbackResponse(String errorMessage) {
        try {
            ObjectNode fallback = objectMapper.createObjectNode();
            
            // Analysis
            ObjectNode analysis = objectMapper.createObjectNode();
            ArrayNode bottlenecks = objectMapper.createArrayNode();
            ObjectNode bottleneck = objectMapper.createObjectNode();
            bottleneck.put("stage", "Analyse indisponible");
            bottleneck.put("duration", 0);
            bottleneck.put("percentage", 0);
            bottleneck.put("issue", "Service temporairement indisponible");
            bottlenecks.add(bottleneck);
            analysis.set("bottlenecks", bottlenecks);
            
            ArrayNode issues = objectMapper.createArrayNode();
            issues.add("Impossible d'analyser: " + errorMessage);
            analysis.set("detectedIssues", issues);
            
            fallback.set("analysis", analysis);
            
            // Optimizations
            ArrayNode optimizations = objectMapper.createArrayNode();
            ObjectNode optimization = objectMapper.createObjectNode();
            optimization.put("priority", 1);
            optimization.put("title", "Service temporairement indisponible");
            optimization.put("description", "Réessayez dans quelques minutes");
            optimization.put("estimatedGain", "0s");
            optimizations.add(optimization);
            fallback.set("optimizations", optimizations);
            
            // Script optimisé (script par défaut)
            fallback.put("optimizedScript", "pipeline {\\n    agent any\\n    stages {\\n        stage('Build') {\\n            steps {\\n                echo 'Service d\\'optimisation temporairement indisponible'\\n            }\\n        }\\n    }\\n}");
            
            // Estimated gain
            ObjectNode estimatedGain = objectMapper.createObjectNode();
            estimatedGain.put("currentDuration", 0);
            estimatedGain.put("estimatedDuration", 0);
            estimatedGain.put("reductionPercentage", "0%");
            fallback.set("estimatedGain", estimatedGain);
            
            return objectMapper.writeValueAsString(fallback);
            
        } catch (Exception e) {
            // Fallback du fallback - JSON minimal
            return "{\"analysis\":{\"bottlenecks\":[],\"detectedIssues\":[\"Service indisponible\"]},\"optimizations\":[],\"optimizedScript\":\"pipeline { agent any; stages { stage('Default') { steps { echo 'Service indisponible' } } } }\",\"estimatedGain\":{\"currentDuration\":0,\"estimatedDuration\":0,\"reductionPercentage\":\"0%\"}}";
        }
    }

    @Override
    public String getProviderName() {
        return "Gemini";
    }
}
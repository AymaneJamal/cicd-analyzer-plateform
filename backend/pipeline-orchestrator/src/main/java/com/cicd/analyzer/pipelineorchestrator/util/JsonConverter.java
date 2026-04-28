package com.cicd.analyzer.pipelineorchestrator.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Utilitaire pour convertir des objets Java en JSON et vice-versa
 * Wrapper autour de Jackson ObjectMapper pour simplifier les opérations courantes
 *
 * Note: Utilise Jackson 3.x (tools.jackson.*) pour Spring Boot 4
 */
@Component
@RequiredArgsConstructor
public class JsonConverter {

    private final ObjectMapper objectMapper;

    /**
     * Convertir un objet Java en JSON string
     *
     * @param object Objet à convertir
     * @return JSON string
     * @throws RuntimeException si la conversion échoue
     */
    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Convertir un objet Java en JSON formatté (pretty print)
     *
     * @param object Objet à convertir
     * @return JSON string formatté
     * @throws RuntimeException si la conversion échoue
     */
    public String toJsonPretty(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to pretty JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Convertir un JSON string en objet Java
     *
     * @param json JSON string
     * @param clazz Classe cible
     * @param <T> Type de l'objet
     * @return Objet Java
     * @throws RuntimeException si la conversion échoue
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object: " + e.getMessage(), e);
        }
    }

    /**
     * Convertir un JSON string en JsonNode (arbre JSON)
     *
     * @param json JSON string
     * @return JsonNode
     * @throws RuntimeException si la conversion échoue
     */
    public JsonNode toJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Extraire une valeur depuis un JSON string
     *
     * @param json JSON string
     * @param path Chemin vers la valeur (ex: "data.user.name")
     * @return Valeur sous forme de string, ou null si non trouvée
     */
    public String extractValue(String json, String path) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode node = root;

            for (String key : path.split("\\.")) {
                node = node.path(key);
                if (node.isMissingNode()) {
                    return null;
                }
            }

            return node.asText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extraire une valeur entière depuis un JSON string
     *
     * @param json JSON string
     * @param path Chemin vers la valeur (ex: "data.count")
     * @return Valeur sous forme d'entier, ou null si non trouvée
     */
    public Long extractLongValue(String json, String path) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode node = root;

            for (String key : path.split("\\.")) {
                node = node.path(key);
                if (node.isMissingNode()) {
                    return null;
                }
            }

            return node.asLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extraire une valeur double depuis un JSON string
     *
     * @param json JSON string
     * @param path Chemin vers la valeur (ex: "data.percentage")
     * @return Valeur sous forme de double, ou null si non trouvée
     */
    public Double extractDoubleValue(String json, String path) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode node = root;

            for (String key : path.split("\\.")) {
                node = node.path(key);
                if (node.isMissingNode()) {
                    return null;
                }
            }

            return node.asDouble();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Vérifier si un JSON string est valide
     *
     * @param json JSON string
     * @return true si le JSON est valide
     */
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convertir un JSON string en Map
     * Utilise une conversion simple sans TypeReference
     *
     * @param json JSON string
     * @return Map<String, Object>
     * @throws RuntimeException si la conversion échoue
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to Map: " + e.getMessage(), e);
        }
    }

    /**
     * Convertir un JSON string en List
     * Utilise une conversion simple sans TypeReference
     *
     * @param json JSON string
     * @return List<Object>
     * @throws RuntimeException si la conversion échoue
     */
    @SuppressWarnings("unchecked")
    public List<Object> toList(String json) {
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to List: " + e.getMessage(), e);
        }
    }

    /**
     * Convertir un Map en JSON string
     *
     * @param map Map à convertir
     * @return JSON string
     * @throws RuntimeException si la conversion échoue
     */
    public String mapToJson(Map<String, Object> map) {
        return toJson(map);
    }

    /**
     * Convertir une List en JSON string
     *
     * @param list List à convertir
     * @return JSON string
     * @throws RuntimeException si la conversion échoue
     */
    public String listToJson(List<?> list) {
        return toJson(list);
    }

    /**
     * Merger deux JSON strings
     * Le second JSON écrase les valeurs du premier en cas de conflit
     *
     * @param json1 Premier JSON
     * @param json2 Second JSON
     * @return JSON mergé
     * @throws RuntimeException si la conversion échoue
     */
    public String mergeJson(String json1, String json2) {
        try {
            JsonNode node1 = objectMapper.readTree(json1);
            JsonNode node2 = objectMapper.readTree(json2);

            JsonNode merged = objectMapper.readerForUpdating(node1).readValue(node2);

            return objectMapper.writeValueAsString(merged);
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Nettoyer un JSON string (enlever espaces, retours à la ligne)
     *
     * @param json JSON string
     * @return JSON nettoyé (compact)
     */
    public String cleanJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return json;
        }

        try {
            JsonNode node = objectMapper.readTree(json);
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            // Si pas du JSON valide, retourner tel quel
            return json;
        }
    }
}
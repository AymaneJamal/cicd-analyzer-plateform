package com.cicd.analyzer.aianalyzerservice.service.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ArrayNode;

@Service("groq")
public class GroqService implements LLMService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String analyze(String prompt) {
        try {
            System.out.println("🤖 Appel Groq API avec modèle: " + model);

            // Build request (OpenAI-compatible format)
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 8000);

            ArrayNode messages = objectMapper.createArrayNode();

            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Tu es un expert DevOps spécialisé en optimisation de pipelines Jenkins CI/CD.");
            messages.add(systemMessage);

            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);

            requestBody.set("messages", messages);

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            // Call API
            System.out.println("📡 Envoi requête à: " + apiUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            System.out.println("✅ Réponse reçue de Groq");

            // Parse response (same format as OpenAI)
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            System.err.println("❌ Erreur Groq: " + e.getMessage());
            throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "Groq (Llama 3.1)";
    }
}
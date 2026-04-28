package com.cicd.analyzer.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PipelineOrchestratorClient {

    private final RestTemplate restTemplate;

    @Value("${microservices.pipeline-orchestrator.url}")
    private String pipelineOrchestratorUrl;

    public void createUser(String email, String username) {
        try {
            String endpoint = pipelineOrchestratorUrl + "/api/users";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("username", username);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync user with pipeline-orchestrator: " + e.getMessage(), e);
        }
    }
}
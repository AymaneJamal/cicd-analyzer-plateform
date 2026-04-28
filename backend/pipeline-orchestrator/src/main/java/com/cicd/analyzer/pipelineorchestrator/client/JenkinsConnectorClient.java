package com.cicd.analyzer.pipelineorchestrator.client;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JenkinsConnectorClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${microservices.jenkins-connector.url}")
    private String jenkinsConnectorUrl;

    private Map<String, String> buildConnectionBody(String url, String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("url", url);
        body.put("username", username);
        body.put("password", password);
        return body;
    }

    public String getServerInfo(String url, String username, String password) {
        try {
            String endpoint = jenkinsConnectorUrl + "/api/jenkins/server-info";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    buildConnectionBody(url, username, password),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get server info from jenkins-connector: " + e.getMessage(), e);
        }
    }

    public String listPipelines(String url, String username, String password) {
        try {
            String endpoint = jenkinsConnectorUrl + "/api/jenkins/pipelines";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    buildConnectionBody(url, username, password),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to list pipelines from jenkins-connector: " + e.getMessage(), e);
        }
    }

    public String getPipelineBuilds(String url, String username, String password, String pipelineName) {
        try {
            String endpoint = jenkinsConnectorUrl + "/api/jenkins/pipelines/" + pipelineName + "/builds";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    buildConnectionBody(url, username, password),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get pipeline builds from jenkins-connector: " + e.getMessage(), e);
        }
    }

    public String getBuildLogs(String url, String username, String password, String pipelineName, Integer buildNumber) {
        try {
            String endpoint = jenkinsConnectorUrl + "/api/jenkins/pipelines/" + pipelineName + "/builds/" + buildNumber + "/logs";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    buildConnectionBody(url, username, password),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get build logs from jenkins-connector: " + e.getMessage(), e);
        }
    }

    public String getPipelineConfig(String url, String username, String password, String pipelineName) {
        try {
            String endpoint = jenkinsConnectorUrl + "/api/jenkins/pipelines/" + pipelineName + "/config";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    buildConnectionBody(url, username, password),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get pipeline config from jenkins-connector: " + e.getMessage(), e);
        }
    }

    public String getBuildStatistics(String url, String username, String password, String pipelineName, Integer buildNumber) {
        try {
            String endpoint = jenkinsConnectorUrl + "/api/jenkins/pipelines/" + pipelineName + "/builds/" + buildNumber + "/statistics";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    buildConnectionBody(url, username, password),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get build statistics from jenkins-connector: " + e.getMessage(), e);
        }
    }

    public String getAgents(String url, String username, String password) {
        try {
            String endpoint = jenkinsConnectorUrl + "/api/jenkins/agents";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(
                    buildConnectionBody(url, username, password),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get agents from jenkins-connector: " + e.getMessage(), e);
        }
    }
}
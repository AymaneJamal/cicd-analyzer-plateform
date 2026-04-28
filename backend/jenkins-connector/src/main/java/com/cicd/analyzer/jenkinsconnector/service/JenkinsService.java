package com.cicd.analyzer.jenkinsconnector.service;

import com.cicd.analyzer.jenkinsconnector.dto.JenkinsConnectionDto;
import com.cicd.analyzer.jenkinsconnector.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JenkinsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpHeaders createHeaders(JenkinsConnectionDto connection) {
        HttpHeaders headers = new HttpHeaders();
        String auth = connection.getUsername() + ":" + connection.getPassword();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }

    public ServerInfo getServerInfo(JenkinsConnectionDto connection) {
        String url = connection.getUrl() + "/api/json";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(connection));

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            ServerInfo info = new ServerInfo();
            info.setNodeName(root.path("nodeName").asText());
            info.setNodeDescription(root.path("nodeDescription").asText());
            info.setNumExecutors(root.path("numExecutors").asInt());
            info.setMode(root.path("mode").asText());
            info.setUseSecurity(root.path("useSecurity").asBoolean());
            return info;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse server info", e);
        }
    }

    public List<Pipeline> listPipelines(JenkinsConnectionDto connection) {
        String url = connection.getUrl() + "/api/json";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(connection));

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode jobs = root.path("jobs");
            List<Pipeline> pipelines = new ArrayList<>();

            for (JsonNode job : jobs) {
                Pipeline pipeline = new Pipeline();
                pipeline.setName(job.path("name").asText());
                pipeline.setUrl(job.path("url").asText());
                pipeline.setColor(job.path("color").asText());
                pipelines.add(pipeline);
            }

            return pipelines;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse pipelines", e);
        }
    }

    public List<Build> getPipelineBuilds(JenkinsConnectionDto connection, String jobName) {
        String url = connection.getUrl() + "/job/" + jobName + "/api/json?tree=builds[number,url,result,duration,timestamp]";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(connection));

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode builds = root.path("builds");
            List<Build> buildList = new ArrayList<>();

            for (JsonNode buildNode : builds) {
                Build build = new Build();
                build.setNumber(buildNode.path("number").asInt());
                build.setUrl(buildNode.path("url").asText());
                build.setResult(buildNode.path("result").asText(null));
                build.setDuration(buildNode.path("duration").asLong());
                build.setTimestamp(buildNode.path("timestamp").asLong());
                buildList.add(build);
            }

            return buildList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse builds", e);
        }
    }

    public BuildLog getBuildLogs(JenkinsConnectionDto connection, String jobName, Integer buildNumber) {
        String url = connection.getUrl() + "/job/" + jobName + "/" + buildNumber + "/consoleText";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(connection));

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return new BuildLog(jobName, buildNumber, response.getBody());
    }

    public PipelineConfig getPipelineConfig(JenkinsConnectionDto connection, String jobName) {
        String url = connection.getUrl() + "/job/" + jobName + "/config.xml";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(connection));

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        String configXml = response.getBody();
        String script = extractScriptFromXml(configXml);

        return new PipelineConfig(jobName, script);
    }

    public BuildStatistics getBuildStatistics(JenkinsConnectionDto connection, String jobName, Integer buildNumber) {
        String url = connection.getUrl() + "/job/" + jobName + "/" + buildNumber + "/wfapi/describe";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(connection));

        BuildStatistics stats = new BuildStatistics();
        stats.setBuildNumber(buildNumber);
        stats.setJobName(jobName);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            stats.setResult(root.path("status").asText());
            stats.setDuration(root.path("durationMillis").asLong());
            stats.setQueueDuration(root.path("queueDurationMillis").asLong());
            stats.setTimestamp(root.path("startTimeMillis").asLong());

            List<BuildStatistics.Stage> stages = new ArrayList<>();
            JsonNode stagesNode = root.path("stages");
            for (JsonNode stageNode : stagesNode) {
                BuildStatistics.Stage stage = new BuildStatistics.Stage();
                stage.setName(stageNode.path("name").asText());
                stage.setDuration(stageNode.path("durationMillis").asLong());
                stage.setStatus(stageNode.path("status").asText());
                stages.add(stage);
            }
            stats.setStages(stages);

        } catch (Exception e) {
            // Workflow API might not be available, fallback to basic info
        }

        // Get basic build info
        try {
            String buildUrl = connection.getUrl() + "/job/" + jobName + "/" + buildNumber + "/api/json";
            ResponseEntity<String> buildResponse = restTemplate.exchange(buildUrl, HttpMethod.GET, entity, String.class);
            JsonNode buildRoot = objectMapper.readTree(buildResponse.getBody());

            if (stats.getResult() == null) {
                stats.setResult(buildRoot.path("result").asText());
            }
            if (stats.getDuration() == null) {
                stats.setDuration(buildRoot.path("duration").asLong());
            }
            if (stats.getTimestamp() == null) {
                stats.setTimestamp(buildRoot.path("timestamp").asLong());
            }

            // Get triggered by
            JsonNode actions = buildRoot.path("actions");
            for (JsonNode action : actions) {
                if (action.has("causes")) {
                    JsonNode causes = action.path("causes");
                    if (causes.isArray() && causes.size() > 0) {
                        stats.setTriggeredBy(causes.get(0).path("userName").asText("system"));
                    }
                }
            }

            // Get commit info
            JsonNode changeSet = buildRoot.path("changeSet");
            JsonNode items = changeSet.path("items");
            if (items.isArray() && items.size() > 0) {
                JsonNode firstCommit = items.get(0);
                BuildStatistics.CommitInfo commitInfo = new BuildStatistics.CommitInfo();
                commitInfo.setId(firstCommit.path("commitId").asText().substring(0, Math.min(7, firstCommit.path("commitId").asText().length())));
                commitInfo.setMessage(firstCommit.path("msg").asText());
                stats.setCommit(commitInfo);
            }

        } catch (Exception e) {
            // Continue with partial data
        }

        // Get test results
        try {
            String testUrl = connection.getUrl() + "/job/" + jobName + "/" + buildNumber + "/testReport/api/json";
            ResponseEntity<String> testResponse = restTemplate.exchange(testUrl, HttpMethod.GET, entity, String.class);
            JsonNode testRoot = objectMapper.readTree(testResponse.getBody());

            BuildStatistics.TestResults testResults = new BuildStatistics.TestResults();
            testResults.setTotal(testRoot.path("totalCount").asInt());
            testResults.setPassed(testRoot.path("passCount").asInt());
            testResults.setFailed(testRoot.path("failCount").asInt());
            testResults.setSkipped(testRoot.path("skipCount").asInt());
            stats.setTestResults(testResults);

        } catch (Exception e) {
            // No test results available
        }

        return stats;
    }


    public List<AgentInfo> getAgents(JenkinsConnectionDto connection) {
        String url = connection.getUrl() + "/computer/api/json";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(connection));

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode computers = root.path("computer");
            List<AgentInfo> agents = new ArrayList<>();

            for (JsonNode computer : computers) {
                AgentInfo agent = new AgentInfo();
                agent.setDisplayName(computer.path("displayName").asText());
                agent.setNumExecutors(computer.path("numExecutors").asInt());
                agent.setOffline(computer.path("offline").asBoolean());

                List<String> labels = new ArrayList<>();
                JsonNode assignedLabels = computer.path("assignedLabels");
                for (JsonNode label : assignedLabels) {
                    labels.add(label.path("name").asText());
                }
                agent.setAssignedLabels(labels);

                agents.add(agent);
            }

            return agents;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse agents", e);
        }
    }

    private String extractScriptFromXml(String xml) {
        try {
            int startIndex = xml.indexOf("<script>");
            int endIndex = xml.indexOf("</script>");

            if (startIndex != -1 && endIndex != -1) {
                return xml.substring(startIndex + 8, endIndex).trim();
            }

            return xml;
        } catch (Exception e) {
            return xml;
        }
    }



}
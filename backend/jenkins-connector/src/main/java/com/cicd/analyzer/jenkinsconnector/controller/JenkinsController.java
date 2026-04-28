package com.cicd.analyzer.jenkinsconnector.controller;

import com.cicd.analyzer.jenkinsconnector.dto.JenkinsConnectionDto;
import com.cicd.analyzer.jenkinsconnector.model.*;
import com.cicd.analyzer.jenkinsconnector.service.JenkinsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jenkins")
@RequiredArgsConstructor
public class JenkinsController {

    private final JenkinsService jenkinsService;

    @PostMapping("/server-info")
    public ServerInfo getServerInfo(@RequestBody JenkinsConnectionDto connection) {
        return jenkinsService.getServerInfo(connection);
    }

    @PostMapping("/pipelines")
    public List<Pipeline> listPipelines(@RequestBody JenkinsConnectionDto connection) {
        return jenkinsService.listPipelines(connection);
    }

    @PostMapping("/pipelines/{jobName}/builds")
    public List<Build> getPipelineBuilds(
            @RequestBody JenkinsConnectionDto connection,
            @PathVariable String jobName) {
        return jenkinsService.getPipelineBuilds(connection, jobName);
    }

    @PostMapping("/pipelines/{jobName}/builds/{buildNumber}/logs")
    public BuildLog getBuildLogs(
            @RequestBody JenkinsConnectionDto connection,
            @PathVariable String jobName,
            @PathVariable Integer buildNumber) {
        return jenkinsService.getBuildLogs(connection, jobName, buildNumber);
    }

    @PostMapping("/pipelines/{jobName}/config")
    public PipelineConfig getPipelineConfig(
            @RequestBody JenkinsConnectionDto connection,
            @PathVariable String jobName) {
        return jenkinsService.getPipelineConfig(connection, jobName);
    }

    @PostMapping("/pipelines/{jobName}/builds/{buildNumber}/statistics")
    public BuildStatistics getBuildStatistics(
            @RequestBody JenkinsConnectionDto connection,
            @PathVariable String jobName,
            @PathVariable Integer buildNumber) {
        return jenkinsService.getBuildStatistics(connection, jobName, buildNumber);
    }

    @PostMapping("/agents")
    public List<AgentInfo> getAgents(@RequestBody JenkinsConnectionDto connection) {
        return jenkinsService.getAgents(connection);
    }
}
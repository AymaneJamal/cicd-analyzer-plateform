package com.cicd.analyzer.jenkinsconnector.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PipelineConfig {
    private String jobName;
    private String script;
}
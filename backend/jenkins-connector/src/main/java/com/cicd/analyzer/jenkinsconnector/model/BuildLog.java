package com.cicd.analyzer.jenkinsconnector.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BuildLog {
    private String jobName;
    private Integer buildNumber;
    private String logs;
}
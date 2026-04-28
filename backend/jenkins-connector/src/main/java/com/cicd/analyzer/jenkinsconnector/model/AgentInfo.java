package com.cicd.analyzer.jenkinsconnector.model;

import lombok.Data;
import java.util.List;

@Data
public class AgentInfo {
    private String displayName;
    private Integer numExecutors;
    private Boolean offline;
    private List<String> assignedLabels;
}

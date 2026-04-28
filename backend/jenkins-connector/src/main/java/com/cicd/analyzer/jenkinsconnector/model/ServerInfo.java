package com.cicd.analyzer.jenkinsconnector.model;

import lombok.Data;

@Data
public class ServerInfo {
    private String nodeName;
    private String nodeDescription;
    private Integer numExecutors;
    private String mode;
    private Boolean useSecurity;
}

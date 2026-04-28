package com.cicd.analyzer.jenkinsconnector.dto;

import lombok.Data;

@Data
public class JenkinsConnectionDto {
    private String url;
    private String username;
    private String password;
}
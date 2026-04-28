package com.cicd.analyzer.jenkinsconnector.model;

import lombok.Data;

@Data
public class Pipeline {
    private String name;
    private String url;
    private String color;
}
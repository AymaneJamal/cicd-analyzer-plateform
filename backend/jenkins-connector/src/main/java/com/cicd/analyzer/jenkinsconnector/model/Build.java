package com.cicd.analyzer.jenkinsconnector.model;

import lombok.Data;

@Data
public class Build {
    private Integer number;
    private String url;
    private String result;
    private Long duration;
    private Long timestamp;
}

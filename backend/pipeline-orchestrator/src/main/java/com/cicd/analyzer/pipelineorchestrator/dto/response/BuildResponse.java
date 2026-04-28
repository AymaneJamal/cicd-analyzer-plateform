package com.cicd.analyzer.pipelineorchestrator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour Build (depuis jenkins-connector)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildResponse {
    private Integer number;
    private String url;
    private String result; // SUCCESS, FAILURE, UNSTABLE, ABORTED, null (en cours)
    private Long duration; // en millisecondes
    private Long timestamp; // timestamp Unix
}
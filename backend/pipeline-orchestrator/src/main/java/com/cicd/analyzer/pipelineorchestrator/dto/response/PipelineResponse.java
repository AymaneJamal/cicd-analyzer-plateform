package com.cicd.analyzer.pipelineorchestrator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de réponse pour Pipeline (depuis jenkins-connector)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineResponse {
    private String name;
    private String url;
    private String color; // Jenkins status color (blue=success, red=failed, etc.)
}
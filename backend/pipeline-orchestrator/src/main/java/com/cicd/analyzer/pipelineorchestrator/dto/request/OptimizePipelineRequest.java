package com.cicd.analyzer.pipelineorchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la demande d'optimisation de pipeline
 * POST /api/optimizations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizePipelineRequest {

    @NotNull(message = "Connection ID obligatoire")
    @Positive(message = "Connection ID doit être positif")
    private Long connectionId;

    @NotBlank(message = "Pipeline name obligatoire")
    private String pipelineName;

    @NotNull(message = "Build number obligatoire")
    @Positive(message = "Build number doit être positif")
    private Integer buildNumber;
}
package com.cicd.analyzer.pipelineorchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'une connexion Jenkins
 * POST /api/jenkins-connections
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJenkinsConnectionRequest {

    @NotBlank(message = "Nom de la connexion obligatoire")
    private String name;

    @NotBlank(message = "URL Jenkins obligatoire")
    private String url;

    @NotBlank(message = "Username Jenkins obligatoire")
    private String username;

    @NotBlank(message = "Password Jenkins obligatoire")
    private String password;
}
package com.cicd.analyzer.pipelineorchestrator.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'un utilisateur
 * POST /api/users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Email obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Username obligatoire")
    @Size(min = 3, max = 100, message = "Username doit avoir entre 3 et 100 caractères")
    private String username;
}
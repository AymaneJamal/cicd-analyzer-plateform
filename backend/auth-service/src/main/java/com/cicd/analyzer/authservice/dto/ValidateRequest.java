package com.cicd.analyzer.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateRequest {

    @NotBlank(message = "Token obligatoire")
    private String token;

    private String csrfToken;

    @NotBlank(message = "Request method obligatoire")
    private String requestMethod;
}
package com.cicd.analyzer.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "Email invalide")
    @NotBlank(message = "Email obligatoire")
    private String email;

    @NotBlank(message = "Password obligatoire")
    private String password;
}
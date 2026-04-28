package com.cicd.analyzer.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Email(message = "Email invalide")
    @NotBlank(message = "Email obligatoire")
    private String email;

    @NotBlank(message = "Username obligatoire")
    @Size(min = 3, max = 100, message = "Username entre 3 et 100 caractères")
    private String username;

    @NotBlank(message = "Password obligatoire")
    @Size(min = 8, message = "Password minimum 8 caractères")
    private String password;
}
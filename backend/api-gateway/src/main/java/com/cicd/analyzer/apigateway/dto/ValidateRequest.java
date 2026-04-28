package com.cicd.analyzer.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateRequest {
    private String token;
    private String csrfToken;
    private String requestMethod;
}
package com.cicd.analyzer.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateResponse {

    private boolean valid;
    private String email;
    private String reason;
}
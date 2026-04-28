package com.cicd.analyzer.pipelineorchestrator.dto.response;

import com.cicd.analyzer.pipelineorchestrator.enums.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour JenkinsConnection
 * IMPORTANT: Ne pas exposer le password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JenkinsConnectionResponse {
    private Long id;
    private String name;
    private String url;
    private String username;
    // password NOT included for security
    private Boolean isActive;
    private TestStatus testStatus;
    private LocalDateTime lastTestedAt;
    private LocalDateTime createdAt;
}
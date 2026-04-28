package com.cicd.analyzer.pipelineorchestrator.entity;

import com.cicd.analyzer.pipelineorchestrator.enums.OptimizationStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité OptimizationHistory - Historique des optimisations de pipelines
 * Stocke les résultats d'analyse IA et les scripts optimisés
 */
@Entity
@Table(name = "optimization_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Connexion Jenkins utilisée pour cette optimisation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jenkins_connection_id", nullable = false)
    @NotNull(message = "JenkinsConnection obligatoire")
    @JsonBackReference("connection-history")
    private JenkinsConnection jenkinsConnection;

    /**
     * Nom du pipeline optimisé
     */
    @Column(name = "pipeline_name", nullable = false, length = 255)
    @NotBlank(message = "Pipeline name obligatoire")
    private String pipelineName;

    /**
     * Numéro du build analysé
     */
    @Column(name = "build_number", nullable = false)
    @NotNull(message = "Build number obligatoire")
    private Integer buildNumber;

    /**
     * Date/heure de fin du traitement
     */
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    /**
     * État du traitement (COMPLETED, FAILED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status obligatoire")
    private OptimizationStatus status;

    /**
     * Provider LLM utilisé (Groq, Gemini, OpenAI)
     */
    @Column(name = "llm_provider", nullable = false, length = 50)
    @NotBlank(message = "LLM provider obligatoire")
    private String llmProvider;

    /**
     * Script Groovy ORIGINAL du pipeline
     */
    @Column(name = "original_script", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Original script obligatoire")
    private String originalScript;

    /**
     * Script Groovy OPTIMISÉ généré par l'IA (null si FAILED)
     */
    @Column(name = "optimized_script", columnDefinition = "TEXT")
    private String optimizedScript;

    /**
     * Analyse des bottlenecks en JSON
     */
    @Column(name = "analysis_json", columnDefinition = "TEXT")
    private String analysisJson;

    /**
     * Liste des optimisations suggérées en JSON
     */
    @Column(name = "optimizations_json", columnDefinition = "TEXT")
    private String optimizationsJson;

    /**
     * Gains estimés (durées, pourcentages) en JSON
     */
    @Column(name = "estimated_gain_json", columnDefinition = "TEXT")
    private String estimatedGainJson;

    /**
     * Durée actuelle du build en millisecondes
     */
    @Column(name = "current_duration_ms", nullable = false)
    @NotNull(message = "Current duration obligatoire")
    private Long currentDurationMs;

    /**
     * Durée estimée après optimisation (ms)
     */
    @Column(name = "estimated_duration_ms")
    private Long estimatedDurationMs;

    /**
     * Pourcentage de réduction estimé (ex: 40.5)
     */
    @Column(name = "reduction_percentage")
    private Double reductionPercentage;

    /**
     * Message d'erreur si status = FAILED
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}
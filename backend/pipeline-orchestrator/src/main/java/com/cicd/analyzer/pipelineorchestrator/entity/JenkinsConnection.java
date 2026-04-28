package com.cicd.analyzer.pipelineorchestrator.entity;

import com.cicd.analyzer.pipelineorchestrator.enums.TestStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JenkinsConnection - Représente une connexion à un serveur Jenkins
 * Stocke les credentials en clair (nécessaire pour transmission au jenkins-connector)
 */
@Entity
@Table(name = "jenkins_connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JenkinsConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Propriétaire de cette connexion Jenkins
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User obligatoire")
    @JsonBackReference("user-connections")
    private User user;

    /**
     * Nom donné à la connexion (ex: "Jenkins Prod", "Jenkins Dev")
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nom de connexion obligatoire")
    private String name;

    /**
     * URL du serveur Jenkins
     */
    @Column(nullable = false, length = 255)
    @NotBlank(message = "URL Jenkins obligatoire")
    private String url;

    /**
     * Nom d'utilisateur Jenkins
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Username Jenkins obligatoire")
    private String username;

    /**
     * Mot de passe Jenkins - Stocké en CLAIR (pour transmission au microservice)
     */
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Password Jenkins obligatoire")
    private String password;

    /**
     * Active/inactive la connexion
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Dernière vérification de connectivité
     */
    @Column(name = "last_tested_at")
    private LocalDateTime lastTestedAt;

    /**
     * Résultat du dernier test de connexion
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "test_status", length = 20)
    private TestStatus testStatus = TestStatus.NOT_TESTED;

    /**
     * Date de création de la connexion
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Dernière modification
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Historique des optimisations effectuées via cette connexion
     */
    @OneToMany(mappedBy = "jenkinsConnection", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("connection-history")
    private List<OptimizationHistory> optimizationHistories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
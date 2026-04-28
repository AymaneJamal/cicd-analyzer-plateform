package com.cicd.analyzer.pipelineorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité User - Représente un utilisateur du système
 * Identifié par email (header X-User-Email)
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email de l'utilisateur - IDENTIFIANT UNIQUE
     * Reçu via header X-User-Email
     */
    @Column(nullable = false, unique = true, length = 255)
    @Email(message = "Email invalide")
    @NotBlank(message = "Email obligatoire")
    private String email;

    /**
     * Nom d'utilisateur choisi par l'utilisateur
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Username obligatoire")
    @Size(min = 3, max = 100, message = "Username doit avoir entre 3 et 100 caractères")
    private String username;

    /**
     * Date de création du compte
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Liste des connexions Jenkins de cet utilisateur
     * Relation OneToMany avec JenkinsConnection
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-connections")
    private List<JenkinsConnection> jenkinsConnections = new ArrayList<>();

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
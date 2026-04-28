package com.cicd.analyzer.pipelineorchestrator.service;

import com.cicd.analyzer.pipelineorchestrator.dto.request.CreateJenkinsConnectionRequest;
import com.cicd.analyzer.pipelineorchestrator.dto.response.JenkinsConnectionResponse;
import com.cicd.analyzer.pipelineorchestrator.entity.JenkinsConnection;
import com.cicd.analyzer.pipelineorchestrator.entity.User;
import com.cicd.analyzer.pipelineorchestrator.enums.TestStatus;
import com.cicd.analyzer.pipelineorchestrator.exception.JenkinsConnectionException;
import com.cicd.analyzer.pipelineorchestrator.repository.JenkinsConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des connexions Jenkins
 * CRUD + logique métier simple
 */
@Service
@RequiredArgsConstructor
@Transactional
public class JenkinsConnectionService {

    private final JenkinsConnectionRepository jenkinsConnectionRepository;
    private final UserService userService;

    /**
     * Créer une nouvelle connexion Jenkins pour un utilisateur
     * @param userEmail Email de l'utilisateur (du header)
     * @param request CreateJenkinsConnectionRequest
     * @return JenkinsConnectionResponse
     */
    public JenkinsConnectionResponse createConnection(String userEmail, CreateJenkinsConnectionRequest request) {
        // Récupérer l'utilisateur
        User user = userService.findByEmail(userEmail);

        // Créer l'entité JenkinsConnection
        JenkinsConnection connection = new JenkinsConnection();
        connection.setUser(user);
        connection.setName(request.getName());
        connection.setUrl(request.getUrl());
        connection.setUsername(request.getUsername());
        connection.setPassword(request.getPassword());
        connection.setIsActive(true);
        connection.setTestStatus(TestStatus.NOT_TESTED);

        // Sauvegarder
        JenkinsConnection saved = jenkinsConnectionRepository.save(connection);

        return toJenkinsConnectionResponse(saved);
    }

    /**
     * Récupérer une connexion par ID (avec vérification de propriété)
     * @param connectionId ID de la connexion
     * @param userEmail Email de l'utilisateur
     * @return JenkinsConnection entity
     */
    public JenkinsConnection findById(Long connectionId, String userEmail) {
        User user = userService.findByEmail(userEmail);

        return jenkinsConnectionRepository.findByIdAndUserId(connectionId, user.getId())
                .orElseThrow(() -> new JenkinsConnectionException(connectionId, user.getId()));
    }

    /**
     * Récupérer une connexion par ID uniquement (sans vérification user)
     * @param connectionId ID de la connexion
     * @return JenkinsConnection entity
     */
    public JenkinsConnection findById(Long connectionId) {
        return jenkinsConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new JenkinsConnectionException(connectionId));
    }

    /**
     * Récupérer toutes les connexions d'un utilisateur
     * @param userEmail Email de l'utilisateur
     * @return Liste de JenkinsConnectionResponse
     */
    public List<JenkinsConnectionResponse> findByUser(String userEmail) {
        User user = userService.findByEmail(userEmail);

        return jenkinsConnectionRepository.findByUserId(user.getId())
                .stream()
                .map(this::toJenkinsConnectionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les connexions actives d'un utilisateur
     * @param userEmail Email de l'utilisateur
     * @return Liste de JenkinsConnectionResponse
     */
    public List<JenkinsConnectionResponse> findActiveByUser(String userEmail) {
        User user = userService.findByEmail(userEmail);

        return jenkinsConnectionRepository.findByUserIdAndIsActive(user.getId(), true)
                .stream()
                .map(this::toJenkinsConnectionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour le statut de test d'une connexion
     * @param connectionId ID de la connexion
     * @param testStatus Nouveau statut de test
     */
    public void updateTestStatus(Long connectionId, TestStatus testStatus) {
        JenkinsConnection connection = findById(connectionId);
        connection.setTestStatus(testStatus);
        connection.setLastTestedAt(java.time.LocalDateTime.now());
        jenkinsConnectionRepository.save(connection);
    }

    /**
     * Activer/désactiver une connexion
     * @param connectionId ID de la connexion
     * @param userEmail Email de l'utilisateur
     * @param isActive Nouveau statut
     */
    public JenkinsConnectionResponse toggleActive(Long connectionId, String userEmail, boolean isActive) {
        JenkinsConnection connection = findById(connectionId, userEmail);
        connection.setIsActive(isActive);
        JenkinsConnection updated = jenkinsConnectionRepository.save(connection);
        return toJenkinsConnectionResponse(updated);
    }

    /**
     * Supprimer une connexion
     * @param connectionId ID de la connexion
     * @param userEmail Email de l'utilisateur
     */
    public void deleteConnection(Long connectionId, String userEmail) {
        JenkinsConnection connection = findById(connectionId, userEmail);
        jenkinsConnectionRepository.delete(connection);
    }

    /**
     * Convertir JenkinsConnection entity en JenkinsConnectionResponse DTO
     * IMPORTANT: Ne pas exposer le password
     */
    private JenkinsConnectionResponse toJenkinsConnectionResponse(JenkinsConnection connection) {
        return new JenkinsConnectionResponse(
                connection.getId(),
                connection.getName(),
                connection.getUrl(),
                connection.getUsername(),
                // password NOT included
                connection.getIsActive(),
                connection.getTestStatus(),
                connection.getLastTestedAt(),
                connection.getCreatedAt()
        );
    }
}

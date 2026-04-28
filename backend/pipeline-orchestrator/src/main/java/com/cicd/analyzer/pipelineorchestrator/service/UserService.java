package com.cicd.analyzer.pipelineorchestrator.service;

import com.cicd.analyzer.pipelineorchestrator.dto.request.CreateUserRequest;
import com.cicd.analyzer.pipelineorchestrator.dto.response.UserResponse;
import com.cicd.analyzer.pipelineorchestrator.entity.User;
import com.cicd.analyzer.pipelineorchestrator.exception.UserNotFoundException;
import com.cicd.analyzer.pipelineorchestrator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier pour la gestion des utilisateurs
 * CRUD simple focalisé sur la base de données
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * Trouver un utilisateur par email
     * @param email Email de l'utilisateur
     * @return User entity
     * @throws UserNotFoundException si l'utilisateur n'existe pas
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    /**
     * Trouver un utilisateur par ID
     * @param id ID de l'utilisateur
     * @return User entity
     * @throws UserNotFoundException si l'utilisateur n'existe pas
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Créer un nouvel utilisateur
     * @param request CreateUserRequest (email, username)
     * @return UserResponse
     */
    public UserResponse createUser(CreateUserRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        // Créer l'entité User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());

        // Sauvegarder (timestamps automatiques via @PrePersist)
        User savedUser = userRepository.save(user);

        // Convertir en UserResponse
        return toUserResponse(savedUser);
    }

    /**
     * Mettre à jour le username d'un utilisateur
     * @param email Email de l'utilisateur
     * @param newUsername Nouveau username
     * @return UserResponse
     */
    public UserResponse updateUsername(String email, String newUsername) {
        User user = findByEmail(email);
        user.setUsername(newUsername);
        User updated = userRepository.save(user);
        return toUserResponse(updated);
    }

    /**
     * Vérifier si un utilisateur existe par email
     * @param email Email à vérifier
     * @return true si existe, false sinon
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Récupérer un utilisateur par email et le retourner en UserResponse
     * @param email Email de l'utilisateur
     * @return UserResponse
     */
    public UserResponse getUserByEmail(String email) {
        User user = findByEmail(email);
        return toUserResponse(user);
    }

    /**
     * Convertir User entity en UserResponse DTO
     */
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getCreatedAt()
        );
    }
}

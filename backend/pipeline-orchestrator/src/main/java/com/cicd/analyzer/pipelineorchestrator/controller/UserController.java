package com.cicd.analyzer.pipelineorchestrator.controller;

import com.cicd.analyzer.pipelineorchestrator.dto.request.CreateUserRequest;
import com.cicd.analyzer.pipelineorchestrator.dto.response.UserResponse;
import com.cicd.analyzer.pipelineorchestrator.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST pour la gestion des utilisateurs
 * Endpoints: POST /api/users, GET /api/users/me
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * POST /api/users
     * Créer un nouveau compte utilisateur
     *
     * @param request CreateUserRequest (email, username)
     * @return UserResponse (201 CREATED)
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/users/me
     * Récupérer les informations de l'utilisateur connecté
     *
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return UserResponse (200 OK)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @RequestHeader("X-User-Email") String userEmail
    ) {
        UserResponse response = userService.getUserByEmail(userEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/users/me/username
     * Mettre à jour le username de l'utilisateur connecté
     *
     * @param userEmail Email de l'utilisateur (header X-User-Email)
     * @return UserResponse (200 OK)
     */
    @PutMapping("/me/username")
    public ResponseEntity<UserResponse> updateUsername(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestBody UpdateUsernameRequest request
    ) {
        UserResponse response = userService.updateUsername(userEmail, request.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * DTO interne pour la requête de mise à jour du username
     */
    private static class UpdateUsernameRequest {
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
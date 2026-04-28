package com.cicd.analyzer.pipelineorchestrator.exception;

/**
 * Exception levée quand un utilisateur n'est pas trouvé
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
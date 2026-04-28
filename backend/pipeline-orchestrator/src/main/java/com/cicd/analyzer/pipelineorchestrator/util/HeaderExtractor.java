package com.cicd.analyzer.pipelineorchestrator.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilitaire pour extraire et valider les headers HTTP
 * Principalement utilisé pour le header X-User-Email
 */
@Component
public class HeaderExtractor {

    /**
     * Nom du header contenant l'email de l'utilisateur
     */
    public static final String USER_EMAIL_HEADER = "X-User-Email";

    /**
     * Extraire l'email de l'utilisateur depuis le header X-User-Email
     *
     * @param request HttpServletRequest
     * @return Email de l'utilisateur
     * @throws IllegalArgumentException si le header est absent ou vide
     */
    public String extractUserEmail(HttpServletRequest request) {
        String email = request.getHeader(USER_EMAIL_HEADER);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing or empty " + USER_EMAIL_HEADER + " header. " +
                            "This header is required to identify the user."
            );
        }

        return email.trim();
    }

    /**
     * Extraire l'email de l'utilisateur depuis le header X-User-Email
     * avec valeur par défaut si absent
     *
     * @param request HttpServletRequest
     * @param defaultValue Valeur par défaut si header absent
     * @return Email de l'utilisateur ou valeur par défaut
     */
    public String extractUserEmailOrDefault(HttpServletRequest request, String defaultValue) {
        String email = request.getHeader(USER_EMAIL_HEADER);

        if (email == null || email.trim().isEmpty()) {
            return defaultValue;
        }

        return email.trim();
    }

    /**
     * Vérifier si le header X-User-Email est présent
     *
     * @param request HttpServletRequest
     * @return true si le header est présent et non vide
     */
    public boolean hasUserEmail(HttpServletRequest request) {
        String email = request.getHeader(USER_EMAIL_HEADER);
        return email != null && !email.trim().isEmpty();
    }

    /**
     * Extraire un header personnalisé
     *
     * @param request HttpServletRequest
     * @param headerName Nom du header
     * @return Valeur du header
     * @throws IllegalArgumentException si le header est absent ou vide
     */
    public String extractHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing or empty " + headerName + " header"
            );
        }

        return value.trim();
    }

    /**
     * Extraire un header personnalisé avec valeur par défaut
     *
     * @param request HttpServletRequest
     * @param headerName Nom du header
     * @param defaultValue Valeur par défaut si header absent
     * @return Valeur du header ou valeur par défaut
     */
    public String extractHeaderOrDefault(HttpServletRequest request, String headerName, String defaultValue) {
        String value = request.getHeader(headerName);

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.trim();
    }

    /**
     * Valider un email (format basique)
     *
     * @param email Email à valider
     * @return true si l'email est valide
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Validation basique : contient @ et un point après @
        String trimmedEmail = email.trim();
        int atIndex = trimmedEmail.indexOf('@');
        int lastDotIndex = trimmedEmail.lastIndexOf('.');

        return atIndex > 0 &&
                lastDotIndex > atIndex &&
                lastDotIndex < trimmedEmail.length() - 1;
    }

    /**
     * Extraire et valider l'email de l'utilisateur
     *
     * @param request HttpServletRequest
     * @return Email de l'utilisateur validé
     * @throws IllegalArgumentException si le header est absent, vide ou invalide
     */
    public String extractAndValidateUserEmail(HttpServletRequest request) {
        String email = extractUserEmail(request);

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(
                    "Invalid email format in " + USER_EMAIL_HEADER + " header: " + email
            );
        }

        return email;
    }
}
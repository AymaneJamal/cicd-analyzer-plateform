package com.cicd.analyzer.authservice.service;

import com.cicd.analyzer.authservice.dto.*;
import com.cicd.analyzer.authservice.entity.AuthUser;
import com.cicd.analyzer.authservice.exception.*;
import com.cicd.analyzer.authservice.repository.AuthUserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final PipelineOrchestratorClient pipelineOrchestratorClient;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public void register(RegisterRequest request) {
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        AuthUser user = new AuthUser();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        authUserRepository.save(user);

        pipelineOrchestratorClient.createUser(request.getEmail(), request.getUsername());
    }

    public LoginResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String csrfToken = UUID.randomUUID().toString();

        redisService.storeCsrfToken(csrfToken, user.getEmail(), jwtExpiration);

        return new LoginResponse(accessToken, csrfToken);
    }

    public void logout(String token) {
        Claims claims = jwtService.validateToken(token);
        String jti = claims.getId();
        long remainingTtl = claims.getExpiration().getTime() - new Date().getTime();

        if (remainingTtl > 0) {
            redisService.revokeToken(jti, claims.getSubject(), remainingTtl);
        }
    }

    public ValidateResponse validate(ValidateRequest request) {
        try {
            Claims claims = jwtService.validateToken(request.getToken());
            String jti = claims.getId();
            String email = claims.getSubject();

            if (redisService.isTokenRevoked(jti)) {
                return new ValidateResponse(false, null, "Token revoked");
            }

            if (isMutatingMethod(request.getRequestMethod())) {
                if (request.getCsrfToken() == null || request.getCsrfToken().isBlank()) {
                    return new ValidateResponse(false, null, "CSRF token required");
                }

                String csrfEmail = redisService.getCsrfTokenEmail(request.getCsrfToken());
                if (csrfEmail == null || !csrfEmail.equals(email)) {
                    return new ValidateResponse(false, null, "Invalid CSRF token");
                }
            }

            return new ValidateResponse(true, email, null);

        } catch (Exception e) {
            return new ValidateResponse(false, null, "Invalid token: " + e.getMessage());
        }
    }

    private boolean isMutatingMethod(String method) {
        return "POST".equalsIgnoreCase(method) ||
                "PUT".equalsIgnoreCase(method) ||
                "DELETE".equalsIgnoreCase(method) ||
                "PATCH".equalsIgnoreCase(method);
    }
}
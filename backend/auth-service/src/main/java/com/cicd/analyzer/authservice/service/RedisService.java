package com.cicd.analyzer.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void storeCsrfToken(String csrfToken, String email, long ttlMillis) {
        String key = "csrf:" + csrfToken;
        redisTemplate.opsForValue().set(key, email, ttlMillis, TimeUnit.MILLISECONDS);
    }

    public String getCsrfTokenEmail(String csrfToken) {
        String key = "csrf:" + csrfToken;
        return redisTemplate.opsForValue().get(key);
    }

    public void revokeToken(String jti, String email, long ttlMillis) {
        String key = "revoked:jwt:" + jti;
        redisTemplate.opsForValue().set(key, email, ttlMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenRevoked(String jti) {
        String key = "revoked:jwt:" + jti;
        return redisTemplate.hasKey(key);
    }
}
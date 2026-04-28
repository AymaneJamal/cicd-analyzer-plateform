package com.cicd.analyzer.apigateway.filter;

import com.cicd.analyzer.apigateway.dto.ValidateRequest;
import com.cicd.analyzer.apigateway.dto.ValidateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final WebClient webClient;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    public AuthenticationFilter() {
        super(Config.class);
        this.webClient = WebClient.create();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String token = extractToken(request);
            String csrfToken = request.getHeaders().getFirst("X-CSRF-Token");
            String method = request.getMethod().name();

            if (token == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            ValidateRequest validateRequest = new ValidateRequest(token, csrfToken, method);

            return webClient
                    .post()
                    .uri(authServiceUrl + "/api/auth/validate")
                    .bodyValue(validateRequest)
                    .retrieve()
                    .bodyToMono(ValidateResponse.class)
                    .flatMap(validateResponse -> {
                        if (validateResponse.isValid()) {
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Email", validateResponse.getEmail())
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        } else {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                    })
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        String cookie = request.getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookie != null && cookie.contains("auth_session=")) {
            int start = cookie.indexOf("auth_session=") + 13;
            int end = cookie.indexOf(";", start);
            return end > start ? cookie.substring(start, end) : cookie.substring(start);
        }

        return null;
    }

    public static class Config {
    }
}
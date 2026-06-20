package com.escuelaing.usuarios.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Parsea tokens JWT emitidos por auth-service. usuarios-service NO
 * reimplementa autenticación: solo valida la firma con el mismo JWT_SECRET
 * compartido y extrae:
 * - sub  -> userId
 * - roles -> lista de roles
 */
@Component
public class JwtTokenParser {

    private final SecretKey secretKey;

    public JwtTokenParser(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public ClaimsJwt parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID userId = UUID.fromString(claims.getSubject());

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return new ClaimsJwt(userId, roles == null ? List.of() : roles);
    }

    public record ClaimsJwt(UUID userId, List<String> roles) {}
}

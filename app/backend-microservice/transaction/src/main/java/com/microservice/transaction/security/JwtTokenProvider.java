package com.microservice.transaction.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Token Provider para el microservicio de transacciones.
 * 
 * Valida tokens JWT emitidos por el servicio de autenticación.
 * El token contiene el userId como subject, que se utiliza para
 * filtrar transacciones por usuario.
 * 
 * @see JwtAuthenticationFilter
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())));
        this.expirationMs = expirationMs;
        log.info(">>> [TRANSACTION] JwtTokenProvider initialized. Secret length: {} chars", secret.length());
    }

    /**
     * Extrae el ID del usuario (subject) de un token JWT válido.
     *
     * @param token token JWT en formato string
     * @return ID del usuario almacenado como subject
     */
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae el email (claim) de un token JWT válido.
     *
     * @param token token JWT en formato string
     * @return email almacenado como claim
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).get("email", String.class);
    }

    /**
     * Valida la firma y expiración del token.
     *
     * @param token token JWT en formato string
     * @return true si el token es válido y no ha expirado
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            log.debug(">>> [TRANSACTION] JWT token validated successfully");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn(">>> [TRANSACTION] JWT token EXPIRED: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error(">>> [TRANSACTION] JWT SIGNATURE MISMATCH — the secret key does not match the one used to sign the token: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.error(">>> [TRANSACTION] JWT validation FAILED ({}): {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

package com.microservice.auth.dto;

public record AuthResponse(
        String userId,
        String email,
        String displayName,
        String token) {
}

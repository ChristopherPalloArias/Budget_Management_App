package com.microservice.auth.dto;

public record UserResponse(
        String userId,
        String email,
        String displayName,
        String photoURL) {
}

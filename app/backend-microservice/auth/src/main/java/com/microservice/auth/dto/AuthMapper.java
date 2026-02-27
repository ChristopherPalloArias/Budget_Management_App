package com.microservice.auth.dto;

import com.microservice.auth.model.User;

public class AuthMapper {

    private AuthMapper() {
        // Utility class — prevent instantiation
    }

    public static AuthResponse toAuthResponse(User user, String token) {
        return new AuthResponse(
                user.getUserId(),
                user.getEmail(),
                user.getDisplayName(),
                token);
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPhotoUrl());
    }
}

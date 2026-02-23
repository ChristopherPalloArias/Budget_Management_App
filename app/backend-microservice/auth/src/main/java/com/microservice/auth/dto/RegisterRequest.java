package com.microservice.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Display name is required")
        @Size(min = 3, max = 100, message = "Display name must be between 3 and 100 characters")
        String displayName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password) {
}

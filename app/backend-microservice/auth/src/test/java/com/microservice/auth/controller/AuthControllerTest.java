package com.microservice.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.auth.dto.AuthResponse;
import com.microservice.auth.dto.LoginRequest;
import com.microservice.auth.dto.RegisterRequest;
import com.microservice.auth.dto.UserResponse;
import com.microservice.auth.exception.AuthException;
import com.microservice.auth.exception.EmailAlreadyExistsException;
import com.microservice.auth.exception.GlobalExceptionHandler;
import com.microservice.auth.security.JwtTokenProvider;
import com.microservice.auth.service.AuthService;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, AuthControllerTest.TestSecurityConfig.class})
class AuthControllerTest {

    /**
     * Minimal security config for @WebMvcTest that permits all unauthenticated
     * requests. The real SecurityConfig + JwtAuthenticationFilter are NOT loaded
     * in this slice because we only want to test the controller layer in isolation.
     */
    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String BASE_URL = "/api/v1/auth";
    private static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_DISPLAY_NAME = "Test User";
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.token";

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("should return 201 CREATED with AuthResponse on successful registration")
        void shouldReturn201OnSuccessfulRegistration() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest(TEST_DISPLAY_NAME, TEST_EMAIL, "SecurePass123");
            AuthResponse response = new AuthResponse(TEST_USER_ID, TEST_EMAIL, TEST_DISPLAY_NAME, TEST_TOKEN);
            when(authService.register(any(RegisterRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.displayName").value(TEST_DISPLAY_NAME))
                    .andExpect(jsonPath("$.token").value(TEST_TOKEN));
        }

        @Test
        @DisplayName("should return 409 CONFLICT when email already exists")
        void shouldReturn409WhenEmailExists() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest(TEST_DISPLAY_NAME, TEST_EMAIL, "SecurePass123");
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new EmailAlreadyExistsException("El correo electrónico ya está registrado"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("El correo electrónico ya está registrado"));
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when email is missing")
        void shouldReturn400WhenEmailMissing() throws Exception {
            // Arrange — missing email
            String body = """
                    {"displayName": "Test", "password": "SecurePass123"}
                    """;

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when password is too short")
        void shouldReturn400WhenPasswordTooShort() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest(TEST_DISPLAY_NAME, TEST_EMAIL, "short");

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when displayName is too short")
        void shouldReturn400WhenDisplayNameTooShort() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("Ab", TEST_EMAIL, "SecurePass123");

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("should return 200 OK with AuthResponse on successful login")
        void shouldReturn200OnSuccessfulLogin() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest(TEST_EMAIL, "SecurePass123");
            AuthResponse response = new AuthResponse(TEST_USER_ID, TEST_EMAIL, TEST_DISPLAY_NAME, TEST_TOKEN);
            when(authService.login(any(LoginRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.token").value(TEST_TOKEN));
        }

        @Test
        @DisplayName("should return 401 UNAUTHORIZED when credentials are invalid")
        void shouldReturn401WhenCredentialsInvalid() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongpassword");
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new AuthException("Credenciales incorrectas"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when email format is invalid")
        void shouldReturn400WhenEmailFormatInvalid() throws Exception {
            // Arrange
            String body = """
                    {"email": "not-an-email", "password": "SecurePass123"}
                    """;

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/me")
    class MeEndpoint {

        @Test
        @DisplayName("should return 200 OK with UserResponse for authenticated user")
        void shouldReturn200WithUserResponse() throws Exception {
            // Arrange
            UserResponse response = new UserResponse(TEST_USER_ID, TEST_EMAIL, TEST_DISPLAY_NAME, null);
            when(authService.getCurrentUser(TEST_USER_ID)).thenReturn(response);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(TEST_USER_ID, null, Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/me")
                            .with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.displayName").value(TEST_DISPLAY_NAME))
                    .andExpect(jsonPath("$.photoURL").doesNotExist());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class LogoutEndpoint {

        @Test
        @DisplayName("should return 204 NO CONTENT")
        void shouldReturn204NoContent() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(TEST_USER_ID, null, Collections.emptyList());

            mockMvc.perform(post(BASE_URL + "/logout")
                            .with(authentication(auth)))
                    .andExpect(status().isNoContent());
        }
    }
}

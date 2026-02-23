package com.microservice.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.microservice.auth.dto.AuthResponse;
import com.microservice.auth.dto.LoginRequest;
import com.microservice.auth.dto.RegisterRequest;
import com.microservice.auth.dto.UserResponse;
import com.microservice.auth.exception.AuthException;
import com.microservice.auth.exception.EmailAlreadyExistsException;
import com.microservice.auth.model.User;
import com.microservice.auth.repository.UserRepository;
import com.microservice.auth.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "SecurePass123";
    private static final String TEST_DISPLAY_NAME = "Test User";
    private static final String TEST_HASHED_PASSWORD = "$2a$10$hashedpassword";
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.token";

    private User createTestUser() {
        return User.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .passwordHash(TEST_HASHED_PASSWORD)
                .displayName(TEST_DISPLAY_NAME)
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("should register a new user successfully and return AuthResponse with token")
        void shouldRegisterNewUserSuccessfully() {
            // Arrange
            RegisterRequest request = new RegisterRequest(TEST_DISPLAY_NAME, TEST_EMAIL, TEST_PASSWORD);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_HASHED_PASSWORD);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn(TEST_TOKEN);

            // Act
            AuthResponse response = authService.register(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo(TEST_EMAIL);
            assertThat(response.displayName()).isEqualTo(TEST_DISPLAY_NAME);
            assertThat(response.token()).isEqualTo(TEST_TOKEN);
            assertThat(response.userId()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should hash the password before saving")
        void shouldHashPasswordBeforeSaving() {
            // Arrange
            RegisterRequest request = new RegisterRequest(TEST_DISPLAY_NAME, TEST_EMAIL, TEST_PASSWORD);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_HASHED_PASSWORD);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn(TEST_TOKEN);

            // Act
            authService.register(request);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getPasswordHash()).isEqualTo(TEST_HASHED_PASSWORD);
            assertThat(savedUser.getPasswordHash()).isNotEqualTo(TEST_PASSWORD);
        }

        @Test
        @DisplayName("should generate a UUID as userId")
        void shouldGenerateUUIDAsUserId() {
            // Arrange
            RegisterRequest request = new RegisterRequest(TEST_DISPLAY_NAME, TEST_EMAIL, TEST_PASSWORD);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_HASHED_PASSWORD);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn(TEST_TOKEN);

            // Act
            AuthResponse response = authService.register(request);

            // Assert — UUID format: 8-4-4-4-12 hex chars
            assertThat(response.userId()).matches(
                    "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        }

        @Test
        @DisplayName("should throw EmailAlreadyExistsException when email is already registered")
        void shouldThrowWhenEmailAlreadyExists() {
            // Arrange
            RegisterRequest request = new RegisterRequest(TEST_DISPLAY_NAME, TEST_EMAIL, TEST_PASSWORD);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining(TEST_EMAIL);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should authenticate user successfully with correct credentials")
        void shouldLoginSuccessfully() {
            // Arrange
            LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            User user = createTestUser();
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(TEST_PASSWORD, TEST_HASHED_PASSWORD)).thenReturn(true);
            when(jwtTokenProvider.generateToken(TEST_USER_ID, TEST_EMAIL)).thenReturn(TEST_TOKEN);

            // Act
            AuthResponse response = authService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(TEST_USER_ID);
            assertThat(response.email()).isEqualTo(TEST_EMAIL);
            assertThat(response.displayName()).isEqualTo(TEST_DISPLAY_NAME);
            assertThat(response.token()).isEqualTo(TEST_TOKEN);
        }

        @Test
        @DisplayName("should throw AuthException when email does not exist")
        void shouldThrowWhenEmailNotFound() {
            // Arrange
            LoginRequest request = new LoginRequest("nonexistent@example.com", TEST_PASSWORD);
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("Credenciales incorrectas");
        }

        @Test
        @DisplayName("should throw AuthException when password is incorrect")
        void shouldThrowWhenPasswordIsIncorrect() {
            // Arrange
            LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongpassword");
            User user = createTestUser();
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpassword", TEST_HASHED_PASSWORD)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("Credenciales incorrectas");

            verify(jwtTokenProvider, never()).generateToken(anyString(), anyString());
        }

        @Test
        @DisplayName("should throw AuthException when account is disabled")
        void shouldThrowWhenAccountIsDisabled() {
            // Arrange
            LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
            User user = createTestUser();
            user.setEnabled(false);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("Esta cuenta ha sido deshabilitada");

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUser {

        @Test
        @DisplayName("should return UserResponse for a valid userId")
        void shouldReturnUserForValidId() {
            // Arrange
            User user = createTestUser();
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

            // Act
            UserResponse response = authService.getCurrentUser(TEST_USER_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(TEST_USER_ID);
            assertThat(response.email()).isEqualTo(TEST_EMAIL);
            assertThat(response.displayName()).isEqualTo(TEST_DISPLAY_NAME);
        }

        @Test
        @DisplayName("should throw AuthException when userId does not exist")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            when(userRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.getCurrentUser("nonexistent-id"))
                    .isInstanceOf(AuthException.class)
                    .hasMessage("Usuario no encontrado");
        }
    }
}

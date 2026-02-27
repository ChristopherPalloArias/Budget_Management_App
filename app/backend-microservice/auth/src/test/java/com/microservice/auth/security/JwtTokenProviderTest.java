package com.microservice.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "testSecretKeyForTestingOnlyMustBeAtLeast256BitsLongEnoughForHS256!!";
    private static final long EXPIRATION_MS = 3600000; // 1 hour
    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, EXPIRATION_MS);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("should generate a non-null, non-empty token")
        void shouldGenerateNonNullToken() {
            String token = jwtTokenProvider.generateToken(USER_ID, EMAIL);

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should generate a token with three parts (header.payload.signature)")
        void shouldGenerateTokenWithThreeParts() {
            String token = jwtTokenProvider.generateToken(USER_ID, EMAIL);
            String[] parts = token.split("\\.");

            assertThat(parts).hasSize(3);
        }

        @Test
        @DisplayName("should generate different tokens for different users")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            String token1 = jwtTokenProvider.generateToken(USER_ID, EMAIL);
            String token2 = jwtTokenProvider.generateToken("another-user-id", "other@example.com");

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("getUserIdFromToken")
    class GetUserIdFromToken {

        @Test
        @DisplayName("should extract the correct userId from a valid token")
        void shouldExtractUserIdFromToken() {
            String token = jwtTokenProvider.generateToken(USER_ID, EMAIL);

            String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

            assertThat(extractedUserId).isEqualTo(USER_ID);
        }
    }

    @Nested
    @DisplayName("getEmailFromToken")
    class GetEmailFromToken {

        @Test
        @DisplayName("should extract the correct email from a valid token")
        void shouldExtractEmailFromToken() {
            String token = jwtTokenProvider.generateToken(USER_ID, EMAIL);

            String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

            assertThat(extractedEmail).isEqualTo(EMAIL);
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("should return true for a valid token")
        void shouldReturnTrueForValidToken() {
            String token = jwtTokenProvider.generateToken(USER_ID, EMAIL);

            boolean isValid = jwtTokenProvider.validateToken(token);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("should return false for a tampered token")
        void shouldReturnFalseForTamperedToken() {
            String token = jwtTokenProvider.generateToken(USER_ID, EMAIL);
            String tamperedToken = token + "tampered";

            boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should return false for a null token")
        void shouldReturnFalseForNullToken() {
            boolean isValid = jwtTokenProvider.validateToken(null);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should return false for an empty token")
        void shouldReturnFalseForEmptyToken() {
            boolean isValid = jwtTokenProvider.validateToken("");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should return false for a completely invalid string")
        void shouldReturnFalseForInvalidString() {
            boolean isValid = jwtTokenProvider.validateToken("not.a.jwt");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should return false for an expired token")
        void shouldReturnFalseForExpiredToken() {
            // Create a provider with 0ms expiration so token expires immediately
            JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, 0);
            String token = expiredProvider.generateToken(USER_ID, EMAIL);

            // Wait a tiny bit to ensure expiration
            try { Thread.sleep(10); } catch (InterruptedException ignored) { }

            boolean isValid = expiredProvider.validateToken(token);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should return false for a token signed with a different secret")
        void shouldReturnFalseForTokenWithDifferentSecret() {
            JwtTokenProvider otherProvider = new JwtTokenProvider(
                    "aCompletelyDifferentSecretKeyThatIsLongEnoughForHS256AlgorithmRequired!!", EXPIRATION_MS);
            String token = otherProvider.generateToken(USER_ID, EMAIL);

            boolean isValid = jwtTokenProvider.validateToken(token);

            assertThat(isValid).isFalse();
        }
    }
}

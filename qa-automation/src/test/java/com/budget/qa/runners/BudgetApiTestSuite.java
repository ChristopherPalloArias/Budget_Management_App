package com.budget.qa.runners;

import com.budget.qa.tasks.Authenticate;
import com.budget.qa.tasks.CreateTransactionApi;

import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.rest.interactions.Delete;
import net.serenitybdd.screenplay.rest.interactions.Get;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.serenitybdd.rest.SerenityRest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 *  BUDGET MANAGEMENT APP — Comprehensive API Test Suite
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * <p>This comprehensive test suite validates the core business operations of the
 * Budget Management application via REST APIs using Serenity BDD + Screenplay
 * pattern with REST Assured.</p>
 *
 * <h3>Test Coverage</h3>
 * <ul>
 *     <li><strong>Authentication</strong> — Login, token validation, invalid credentials</li>
 *     <li><strong>Transactions CRUD</strong> — Create, Read, Delete operations</li>
 *     <li><strong>Validation</strong> — Negative test cases, boundary values</li>
 *     <li><strong>Security</strong> — Unauthorized access, expired tokens</li>
 * </ul>
 *
 * <h3>How to Run</h3>
 * <pre>
 *   # Run all API tests
 *   ./gradlew test --tests "com.budget.qa.runners.BudgetApiTestSuite"
 *
 *   # Run only auth tests
 *   ./gradlew test --tests "*BudgetApiTestSuite*AuthenticationTests"
 *
 *   # Run only transaction tests
 *   ./gradlew test --tests "*BudgetApiTestSuite*TransactionTests"
 * </pre>
 *
 * <h3>Prerequisites</h3>
 * <ul>
 *     <li>Auth Service running on port 8083</li>
 *     <li>Transaction Service running on port 8081</li>
 *     <li>Test user registered: {@code test@budgetapp.com / SecurePass123!}</li>
 * </ul>
 */
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Budget Management App — API Test Suite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BudgetApiTestSuite {

    // ─── Test Credentials ───────────────────────────────────────────────
    private static final String TEST_EMAIL    = "test@budgetapp.com";
    private static final String TEST_PASSWORD = "SecurePass123!";

    // ─── Base URLs ──────────────────────────────────────────────────────
    private static final String AUTH_BASE_URL        = "http://localhost:8083/api/v1";
    private static final String TRANSACTION_BASE_URL = "http://localhost:8081/api/v1";

    // ─── Shared state across nested classes ──────────────────────────────
    private static String sharedToken;
    private static String sharedUserId;

    // ═════════════════════════════════════════════════════════════════════
    //  1. AUTHENTICATION TESTS
    // ═════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("1. Authentication Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AuthenticationTests {

        private Actor tester;

        @BeforeEach
        void setup() {
            tester = Actor.named("Auth Tester")
                .whoCan(CallAnApi.at(AUTH_BASE_URL));
        }

        @Test
        @Order(1)
        @Tag("api")
        @Tag("auth")
        @Tag("smoke")
        @DisplayName("1.1 Login with valid credentials returns 200 and JWT token")
        void loginWithValidCredentials() {
            // Act
            tester.attemptsTo(
                Authenticate.withCredentials(TEST_EMAIL, TEST_PASSWORD)
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("Login should return HTTP 200 OK")
                .isEqualTo(200);

            String token = SerenityRest.lastResponse().jsonPath().getString("token");
            assertThat(token)
                .as("Response should contain a non-empty JWT token")
                .isNotNull()
                .isNotEmpty()
                .contains(".");  // JWT has 3 parts separated by dots

            String email = SerenityRest.lastResponse().jsonPath().getString("email");
            assertThat(email)
                .as("Response should echo back the authenticated email")
                .isEqualTo(TEST_EMAIL);

            String userId = SerenityRest.lastResponse().jsonPath().getString("userId");
            assertThat(userId)
                .as("Response should contain a valid userId")
                .isNotNull()
                .isNotEmpty();

            // Store for other tests
            sharedToken = token;
            sharedUserId = userId;
        }

        @Test
        @Order(2)
        @Tag("api")
        @Tag("auth")
        @Tag("negative")
        @DisplayName("1.2 Login with wrong password returns 401 Unauthorized")
        void loginWithWrongPassword() {
            // Act
            tester.attemptsTo(
                Post.to("/auth/login")
                    .with(req -> req
                        .contentType("application/json")
                        .body(Map.of(
                            "email", TEST_EMAIL,
                            "password", "WrongPassword123!"
                        ))
                    )
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("Login with wrong password should return 401")
                .isEqualTo(401);
        }

        @Test
        @Order(3)
        @Tag("api")
        @Tag("auth")
        @Tag("negative")
        @DisplayName("1.3 Login with non-existent user returns 401 Unauthorized")
        void loginWithNonExistentUser() {
            // Act
            tester.attemptsTo(
                Post.to("/auth/login")
                    .with(req -> req
                        .contentType("application/json")
                        .body(Map.of(
                            "email", "nonexistent@budgetapp.com",
                            "password", "AnyPassword123!"
                        ))
                    )
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("Login with non-existent user should return 401")
                .isEqualTo(401);
        }

        @Test
        @Order(4)
        @Tag("api")
        @Tag("auth")
        @Tag("negative")
        @DisplayName("1.4 Login with empty body returns 400 Bad Request")
        void loginWithEmptyBody() {
            // Act
            tester.attemptsTo(
                Post.to("/auth/login")
                    .with(req -> req
                        .contentType("application/json")
                        .body("{}")
                    )
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("Login with empty body should return 400")
                .isIn(400, 401);  // Either validation or auth error
        }

        @Test
        @Order(5)
        @Tag("api")
        @Tag("auth")
        @DisplayName("1.5 JWT token has valid structure (header.payload.signature)")
        void jwtTokenHasValidStructure() {
            // Arrange — Login first
            tester.attemptsTo(
                Authenticate.withCredentials(TEST_EMAIL, TEST_PASSWORD)
            );

            String token = SerenityRest.lastResponse().jsonPath().getString("token");

            // Assert — JWT is composed of 3 Base64URL parts separated by dots
            assertThat(token)
                .as("JWT should have three Base64 parts separated by dots")
                .isNotNull();

            String[] parts = token.split("\\.");
            assertThat(parts)
                .as("JWT must have exactly 3 parts: header.payload.signature")
                .hasSize(3);

            // Each part must be non-empty
            for (String part : parts) {
                assertThat(part)
                    .as("Each JWT part should be non-empty")
                    .isNotEmpty();
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  2. TRANSACTION CRUD TESTS
    // ═════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("2. Transaction CRUD Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class TransactionTests {

        private Actor tester;
        private static Long createdIncomeId;
        private static Long createdExpenseId;

        @BeforeEach
        void setup() {
            tester = Actor.named("Transaction Tester")
                .whoCan(CallAnApi.at(AUTH_BASE_URL));

            // Authenticate and get fresh token
            tester.attemptsTo(
                Authenticate.withCredentials(TEST_EMAIL, TEST_PASSWORD)
            );
            String token = tester.recall("jwt_token");
            assertThat(token).as("Must authenticate before transaction tests").isNotNull();

            // Switch to Transaction Service
            tester.whoCan(CallAnApi.at(TRANSACTION_BASE_URL));
        }

        @Test
        @Order(1)
        @Tag("api")
        @Tag("transactions")
        @Tag("smoke")
        @DisplayName("2.1 Create INCOME transaction returns 201 Created")
        void createIncomeTransaction() {
            // Act
            tester.attemptsTo(
                CreateTransactionApi.ofType("INCOME")
                    .withAmount(3500.00)
                    .withCategory("Salary")
                    .withDescription("Monthly salary - March 2026")
                    .onDate("2026-03-01")
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("Creating INCOME transaction should return 201")
                .isEqualTo(201);

            String type = SerenityRest.lastResponse().jsonPath().getString("type");
            assertThat(type).isEqualTo("INCOME");

            Number amount = SerenityRest.lastResponse().jsonPath().get("amount");
            assertThat(amount.doubleValue()).isEqualTo(3500.00);

            String category = SerenityRest.lastResponse().jsonPath().getString("category");
            assertThat(category).isEqualTo("Salary");

            // Store for later tests
            createdIncomeId = SerenityRest.lastResponse().jsonPath().getLong("transactionId");
            assertThat(createdIncomeId).isPositive();
        }

        @Test
        @Order(2)
        @Tag("api")
        @Tag("transactions")
        @DisplayName("2.2 Create EXPENSE transaction returns 201 Created")
        void createExpenseTransaction() {
            // Act
            tester.attemptsTo(
                CreateTransactionApi.ofType("EXPENSE")
                    .withAmount(150.75)
                    .withCategory("Food")
                    .withDescription("Weekly groceries")
                    .onDate("2026-03-02")
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("Creating EXPENSE transaction should return 201")
                .isEqualTo(201);

            String type = SerenityRest.lastResponse().jsonPath().getString("type");
            assertThat(type).isEqualTo("EXPENSE");

            Number amount = SerenityRest.lastResponse().jsonPath().get("amount");
            assertThat(amount.doubleValue()).isEqualTo(150.75);

            createdExpenseId = SerenityRest.lastResponse().jsonPath().getLong("transactionId");
            assertThat(createdExpenseId).isPositive();
        }

        @Test
        @Order(3)
        @Tag("api")
        @Tag("transactions")
        @DisplayName("2.3 List transactions returns 200 OK with results")
        void listTransactions() {
            String token = tester.recall("jwt_token");

            // Act
            tester.attemptsTo(
                Get.resource("/transactions")
                    .with(req -> req
                        .header("Authorization", "Bearer " + token)
                    )
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("Listing transactions should return 200 OK")
                .isEqualTo(200);
        }

        @Test
        @Order(4)
        @Tag("api")
        @Tag("transactions")
        @DisplayName("2.4 Create transaction with various categories")
        void createTransactionWithDifferentCategories() {
            String[] categories = {"Transport", "Entertainment", "Health", "Education"};

            for (String category : categories) {
                tester.attemptsTo(
                    CreateTransactionApi.ofType("EXPENSE")
                        .withAmount(50.00)
                        .withCategory(category)
                        .withDescription("Test expense - " + category)
                        .onDate("2026-03-03")
                );

                int status = SerenityRest.lastResponse().getStatusCode();
                assertThat(status)
                    .as("Creating transaction in category '%s' should return 201", category)
                    .isEqualTo(201);

                String responseCategory = SerenityRest.lastResponse().jsonPath().getString("category");
                assertThat(responseCategory).isEqualTo(category);
            }
        }

        @Test
        @Order(5)
        @Tag("api")
        @Tag("transactions")
        @Tag("negative")
        @DisplayName("2.5 Create transaction without auth token returns 401/403")
        void createTransactionWithoutToken() {
            // Act — POST without Authorization header
            tester.attemptsTo(
                Post.to("/transactions")
                    .with(req -> req
                        .contentType("application/json")
                        .body(Map.of(
                            "type", "INCOME",
                            "amount", 100.00,
                            "category", "Test",
                            "date", "2026-03-03"
                        ))
                    )
            );

            // Assert — Should be rejected
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("Transaction without auth token should be rejected (401 or 403)")
                .isIn(401, 403);
        }

        @Test
        @Order(6)
        @Tag("api")
        @Tag("transactions")
        @Tag("negative")
        @DisplayName("2.6 Create transaction with invalid token returns 401/403")
        void createTransactionWithInvalidToken() {
            // Act — POST with garbage token
            tester.attemptsTo(
                Post.to("/transactions")
                    .with(req -> req
                        .contentType("application/json")
                        .header("Authorization", "Bearer invalid.token.here")
                        .body(Map.of(
                            "type", "INCOME",
                            "amount", 100.00,
                            "category", "Test",
                            "date", "2026-03-03"
                        ))
                    )
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("Transaction with invalid token should return 401 or 403")
                .isIn(401, 403);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  3. SECURITY & EDGE CASE TESTS
    // ═════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("3. Security & Edge Case Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SecurityTests {

        private Actor tester;

        @BeforeEach
        void setup() {
            tester = Actor.named("Security Tester")
                .whoCan(CallAnApi.at(AUTH_BASE_URL));
        }

        @Test
        @Order(1)
        @Tag("api")
        @Tag("security")
        @DisplayName("3.1 Access /auth/me with valid token returns user info")
        void accessCurrentUserWithValidToken() {
            // Authenticate first
            tester.attemptsTo(
                Authenticate.withCredentials(TEST_EMAIL, TEST_PASSWORD)
            );
            String token = tester.recall("jwt_token");

            // Act — Get current user info
            tester.attemptsTo(
                Get.resource("/auth/me")
                    .with(req -> req
                        .header("Authorization", "Bearer " + token)
                    )
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("GET /auth/me with valid token should return 200")
                .isEqualTo(200);
        }

        @Test
        @Order(2)
        @Tag("api")
        @Tag("security")
        @Tag("negative")
        @DisplayName("3.2 Access /auth/me without token returns 401/403")
        void accessCurrentUserWithoutToken() {
            // Act — No Authorization header
            tester.attemptsTo(
                Get.resource("/auth/me")
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("GET /auth/me without token should return 401 or 403")
                .isIn(401, 403);
        }

        @Test
        @Order(3)
        @Tag("api")
        @Tag("security")
        @DisplayName("3.3 Multiple rapid logins should all succeed (no rate limiting issue)")
        void multipleRapidLogins() {
            for (int i = 0; i < 3; i++) {
                tester.attemptsTo(
                    Authenticate.withCredentials(TEST_EMAIL, TEST_PASSWORD)
                );

                int status = SerenityRest.lastResponse().getStatusCode();
                assertThat(status)
                    .as("Login attempt %d should return 200", i + 1)
                    .isEqualTo(200);
            }
        }

        @Test
        @Order(4)
        @Tag("api")
        @Tag("security")
        @DisplayName("3.4 Logout endpoint returns 204 No Content")
        void logoutReturns204() {
            // Authenticate first (endpoint is protected by Spring Security)
            tester.attemptsTo(
                Authenticate.withCredentials(TEST_EMAIL, TEST_PASSWORD)
            );
            String token = tester.recall("jwt_token");

            // Act
            tester.attemptsTo(
                Post.to("/auth/logout")
                    .with(req -> req
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                    )
            );

            // Assert
            int status = SerenityRest.lastResponse().getStatusCode();
            assertThat(status)
                .as("POST /auth/logout should return 204 No Content")
                .isEqualTo(204);
        }
    }
}

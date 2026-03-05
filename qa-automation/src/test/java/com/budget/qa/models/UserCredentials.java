package com.budget.qa.models;

/**
 * Test data model representing user credentials for authentication tests.
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   UserCredentials creds = UserCredentials.of("test@example.com", "Password123!");
 *   actor.attemptsTo(Authenticate.withCredentials(creds.email(), creds.password()));
 * }</pre>
 */
public record UserCredentials(
        String email,
        String password,
        String name
) {

    /**
     * Factory for login-only credentials (no name needed).
     */
    public static UserCredentials of(String email, String password) {
        return new UserCredentials(email, password, null);
    }

    /**
     * Factory for registration credentials (includes display name).
     */
    public static UserCredentials forRegistration(String name, String email, String password) {
        return new UserCredentials(email, password, name);
    }
}

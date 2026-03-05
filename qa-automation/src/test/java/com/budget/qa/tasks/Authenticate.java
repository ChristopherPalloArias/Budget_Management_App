package com.budget.qa.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.serenitybdd.rest.SerenityRest;

import java.util.Map;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Screenplay Task: Authenticate a user via the Auth microservice (API).
 *
 * <p>This is a high-level business task that encapsulates the login flow:</p>
 * <ol>
 *     <li>POST credentials to {@code /auth/login}</li>
 *     <li>Extract JWT token from the {@code AuthResponse.token} field</li>
 *     <li>Store token in actor memory as {@code "jwt_token"} for subsequent API calls</li>
 * </ol>
 *
 * <p><strong>Backend contract (AuthController):</strong></p>
 * <ul>
 *     <li>Request:  {@code POST /api/v1/auth/login} with {@code {"email":"...", "password":"..."}}</li>
 *     <li>Response: {@code {"userId":"...", "email":"...", "displayName":"...", "token":"jwt-here"}}</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   actor.attemptsTo(
 *       Authenticate.withCredentials("user@test.com", "password123")
 *   );
 *   String jwt = actor.recall("jwt_token");
 * }</pre>
 */
public class Authenticate implements Task {

    private final String email;
    private final String password;

    public Authenticate(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static Authenticate withCredentials(String email, String password) {
        return instrumented(Authenticate.class, email, password);
    }

    @Override
    @Step("{0} authenticates with email '#email'")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            Post.to("/auth/login")
                .with(req -> req
                    .contentType("application/json")
                    .body(Map.of(
                        "email", email,
                        "password", password
                    ))
                )
        );

        int statusCode = SerenityRest.lastResponse().getStatusCode();

        if (statusCode == 200) {
            String token = SerenityRest.lastResponse().jsonPath().getString("token");
            String userId = SerenityRest.lastResponse().jsonPath().getString("userId");

            actor.remember("jwt_token", token);
            actor.remember("user_id", userId);
            actor.remember("user_email", email);
        }
    }
}

package com.budget.qa.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.rest.SerenityRest;

/**
 * Screenplay Question: Extract a value from the JSON response body.
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   String token = actor.asksFor(
 *       TheResponseBody.field("token")
 *   );
 *
 *   BigDecimal amount = actor.asksFor(
 *       TheResponseBody.field("amount")
 *   );
 * }</pre>
 *
 * @param <T> the expected return type (inferred from JsonPath)
 */
public class TheResponseBody<T> implements Question<T> {

    private final String jsonPath;

    private TheResponseBody(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    /**
     * Creates a Question that extracts a field from the JSON response.
     *
     * @param jsonPathExpression the JsonPath expression (e.g., "token", "data.id", "content[0].amount")
     * @param <R> the expected return type
     * @return a configured Question
     */
    public static <R> TheResponseBody<R> field(String jsonPathExpression) {
        return new TheResponseBody<>(jsonPathExpression);
    }

    @Override
    public T answeredBy(Actor actor) {
        return SerenityRest.lastResponse().jsonPath().get(jsonPath);
    }
}

package com.budget.qa.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.LastResponse;
import net.serenitybdd.rest.SerenityRest;

/**
 * Screenplay Question: What HTTP status code was returned?
 *
 * <p>Questions observe the outcome of an Actor's actions without side effects.
 * They are used in assertions to verify expected behavior.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   actor.attemptsTo(CreateTransaction.ofType("INCOME").withAmount(100));
 *   int status = actor.asksFor(TheResponseStatus.returned());
 *   assertThat(status).isEqualTo(201);
 * }</pre>
 *
 * <p>Or with Serenity's fluent assertions:</p>
 * <pre>{@code
 *   actor.should(
 *       seeThatResponse("transaction was created",
 *           response -> response.statusCode(201))
 *   );
 * }</pre>
 */
public class TheResponseStatus implements Question<Integer> {

    public static TheResponseStatus returned() {
        return new TheResponseStatus();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        return SerenityRest.lastResponse().getStatusCode();
    }
}

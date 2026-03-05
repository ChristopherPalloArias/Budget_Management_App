package com.budget.qa.actions;

/**
 * Low-level Screenplay Interactions for API calls.
 *
 * <p>Actions are atomic, reusable interactions that Tasks compose.
 * They map directly to HTTP operations or WebDriver actions.</p>
 *
 * <p><strong>Naming convention:</strong></p>
 * <ul>
 *     <li>API actions: {@code PostTo}, {@code GetFrom}, {@code PutTo}, {@code DeleteFrom}</li>
 *     <li>UI actions: {@code ClickOn}, {@code EnterText}, {@code SelectOption}</li>
 * </ul>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * public class PostTransaction implements Interaction {
 *     @Override
 *     public <T extends Actor> void performAs(T actor) {
 *         actor.attemptsTo(
 *             Post.to("/transactions")
 *                 .with(req -> req.body(payload))
 *         );
 *     }
 * }
 * }</pre>
 *
 * @see net.serenitybdd.screenplay.Interaction
 */
// Package-level documentation. Concrete actions will be added in Sprint 1.

package com.budget.qa.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Screenplay Task: Create a financial transaction (income or expense).
 *
 * <p>Encapsulates the business action of recording a new transaction:</p>
 * <ol>
 *     <li>POST transaction data to {@code /transactions}</li>
 *     <li>Include JWT token from actor memory</li>
 *     <li>Validate 201 Created response</li>
 * </ol>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   actor.attemptsTo(
 *       CreateTransaction.ofType("INCOME")
 *           .withAmount(1500.00)
 *           .withCategory("Salary")
 *           .withDescription("Monthly salary")
 *           .onDate("2026-03-01")
 *   );
 * }</pre>
 */
public class CreateTransaction implements Task {

    private final String type;
    private double amount;
    private String category;
    private String description;
    private String date;

    public CreateTransaction(String type) {
        this.type = type;
    }

    public static CreateTransaction ofType(String type) {
        return instrumented(CreateTransaction.class, type);
    }

    public CreateTransaction withAmount(double amount) {
        this.amount = amount;
        return this;
    }

    public CreateTransaction withCategory(String category) {
        this.category = category;
        return this;
    }

    public CreateTransaction withDescription(String description) {
        this.description = description;
        return this;
    }

    public CreateTransaction onDate(String date) {
        this.date = date;
        return this;
    }

    @Override
    @Step("{0} creates a #type transaction of #amount in category '#category'")
    public <T extends Actor> void performAs(T actor) {
        // TODO: Implement in Sprint 1
        // String token = actor.recall("jwt_token");
        // actor.attemptsTo(
        //     Post.to("/transactions")
        //         .with(req -> req
        //             .contentType("application/json")
        //             .header("Authorization", "Bearer " + token)
        //             .body(Map.of(
        //                 "type", type,
        //                 "amount", amount,
        //                 "category", category,
        //                 "description", description,
        //                 "date", date
        //             ))
        //         )
        // );
    }
}

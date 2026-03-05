package com.budget.qa.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.rest.interactions.Post;
import net.serenitybdd.rest.SerenityRest;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Screenplay Task: Create a financial transaction via the REST API.
 *
 * <p><strong>This is the "High ROI" path.</strong> Direct API calls bypass the
 * entire UI stack — no browser, no DOM, no rendering. This makes tests:</p>
 * <ul>
 *     <li><strong>~50x faster</strong> (~200ms vs ~10s for UI)</li>
 *     <li><strong>More stable</strong> — no CSS selector breakage, no animation waits</li>
 *     <li><strong>More precise</strong> — test the business logic, not the UI framework</li>
 * </ul>
 *
 * <p><strong>Backend contract (TransactionController):</strong></p>
 * <pre>
 *   POST /api/v1/transactions
 *   Headers: Authorization: Bearer {jwt_token}
 *   Body: {
 *     "type": "INCOME" | "EXPENSE",
 *     "amount": 1500.00,
 *     "category": "Salary",
 *     "date": "2026-03-01",
 *     "description": "Monthly salary"
 *   }
 *   Response (201): {
 *     "transactionId": 1,
 *     "userId": "...",
 *     "type": "INCOME",
 *     "amount": 1500.00,
 *     "category": "Salary",
 *     "date": "2026-03-01",
 *     "description": "Monthly salary",
 *     "createdAt": "..."
 *   }
 * </pre>
 *
 * <p><strong>Prerequisites:</strong> The actor must have a {@code "jwt_token"}
 * in memory (obtained from {@link Authenticate}).</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   actor.attemptsTo(
 *       CreateTransactionApi.ofType("INCOME")
 *           .withAmount(1500.00)
 *           .withCategory("Salary")
 *           .withDescription("Monthly salary")
 *           .onDate("2026-03-01")
 *   );
 * }</pre>
 */
public class CreateTransactionApi implements Task {

    private final String type;
    private double amount;
    private String category = "General";
    private String description = "";
    private String date;

    // ─── Constructor ────────────────────────────────────────────────────

    public CreateTransactionApi(String type) {
        this.type = type;
        // Default date to today if not specified
        this.date = java.time.LocalDate.now().toString();
    }

    // ─── Fluent Builder Methods ─────────────────────────────────────────

    /**
     * Factory: create a transaction of the given type (INCOME or EXPENSE).
     */
    public static CreateTransactionApi ofType(String type) {
        return instrumented(CreateTransactionApi.class, type);
    }

    /**
     * Sets the transaction amount (must be positive, per backend validation).
     */
    public CreateTransactionApi withAmount(double amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Sets the transaction category (max 100 chars).
     */
    public CreateTransactionApi withCategory(String category) {
        this.category = category;
        return this;
    }

    /**
     * Sets the transaction description (optional, max 500 chars).
     */
    public CreateTransactionApi withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the transaction date in ISO format (yyyy-MM-dd).
     */
    public CreateTransactionApi onDate(String date) {
        this.date = date;
        return this;
    }

    // ─── Task Execution ─────────────────────────────────────────────────

    @Override
    @Step("{0} creates a #type transaction of $#amount in category '#category' via API")
    public <T extends Actor> void performAs(T actor) {

        // Retrieve JWT token from actor memory (set by Authenticate task)
        String token = actor.recall("jwt_token");

        if (token == null || token.isEmpty()) {
            throw new IllegalStateException(
                "No JWT token found in actor memory. " +
                "The actor must authenticate first using: " +
                "actor.attemptsTo(Authenticate.withCredentials(email, password))"
            );
        }

        // Build the request body matching TransactionRequest record
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("type", type);
        requestBody.put("amount", amount);
        requestBody.put("category", category);
        requestBody.put("date", date);
        if (description != null && !description.isEmpty()) {
            requestBody.put("description", description);
        }

        // Execute the POST request
        actor.attemptsTo(
            Post.to("/transactions")
                .with(req -> req
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(requestBody)
                )
        );

        // Store the created transaction ID for downstream use (update, delete, verify)
        int statusCode = SerenityRest.lastResponse().getStatusCode();
        if (statusCode == 201) {
            Long transactionId = SerenityRest.lastResponse()
                .jsonPath().getLong("transactionId");
            actor.remember("last_transaction_id", transactionId);
        }
    }
}

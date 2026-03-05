package com.budget.qa.models;

/**
 * Test data model representing a Transaction request payload.
 *
 * <p>Used to build request bodies for API tests. This is a test-only DTO
 * that mirrors the backend's {@code TransactionRequest} without coupling
 * to production code.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   TransactionData data = new TransactionData(
 *       "INCOME", 1500.00, "Salary", "Monthly salary", "2026-03-01"
 *   );
 * }</pre>
 */
public record TransactionData(
        String type,
        double amount,
        String category,
        String description,
        String date
) {

    /**
     * Factory method for creating an income transaction.
     */
    public static TransactionData income(double amount, String category, String description, String date) {
        return new TransactionData("INCOME", amount, category, description, date);
    }

    /**
     * Factory method for creating an expense transaction.
     */
    public static TransactionData expense(double amount, String category, String description, String date) {
        return new TransactionData("EXPENSE", amount, category, description, date);
    }
}

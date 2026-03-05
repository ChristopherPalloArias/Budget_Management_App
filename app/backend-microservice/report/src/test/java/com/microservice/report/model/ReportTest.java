package com.microservice.report.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Enterprise-grade unit tests for the {@link Report} domain model.
 * Tests pure domain logic (business rules) without Spring context or Mocks.
 */
@DisplayName("Report Domain Model Tests")
class ReportTest {

    @Test
    @DisplayName("should calculate balance correctly when generating single income and expense (Happy Path)")
    void shouldCalculateBalanceCorrectly_WhenAddingIncomeAndExpense() {
        // Arrange
        Report report = Report.builder()
                .userId("user-001")
                .period("2026-03")
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .build();

        BigDecimal income = new BigDecimal("2000.00");
        BigDecimal expense = new BigDecimal("500.0");

        // Act
        report.addIncome(income);
        report.addExpense(expense);

        // Assert
        // We use compareTo(BigDecimal) == 0 to avoid false negatives due to scale differences (e.g. 1500.0 vs 1500.00)
        assertAll("Verify financial accuracy of the balance calculations",
                () -> assertEquals(0, new BigDecimal("2000").compareTo(report.getTotalIncome()),
                        "Total income should be exactly 2000"),
                () -> assertEquals(0, new BigDecimal("500").compareTo(report.getTotalExpense()),
                        "Total expense should be exactly 500"),
                () -> assertEquals(0, new BigDecimal("1500").compareTo(report.getBalance()),
                        "Balance should be exactly 1500 (2000 - 500)")
        );
    }

    @Test
    @DisplayName("should calculate negative balance when expenses exceed income")
    void shouldCalculateNegativeBalance_WhenExpensesExceedIncome() {
        // Arrange
        Report report = Report.builder()
                .userId("user-002")
                .period("2026-04")
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .build();

        BigDecimal income = new BigDecimal("500.00");
        BigDecimal expense = new BigDecimal("1200.50");

        // Act
        report.addIncome(income);
        report.addExpense(expense);

        // Assert
        assertAll("Verify negative balance behavior",
                () -> assertEquals(0, new BigDecimal("500").compareTo(report.getTotalIncome()),
                        "Total income should be exactly 500"),
                () -> assertEquals(0, new BigDecimal("1200.50").compareTo(report.getTotalExpense()),
                        "Total expense should be exactly 1200.50"),
                () -> assertEquals(0, new BigDecimal("-700.50").compareTo(report.getBalance()),
                        "Balance should be negative (-700.50)")
        );
    }

    @Test
    @DisplayName("should accumulate totals and balance correctly when adding multiple transactions sequentially")
    void shouldAccumulateCorrectly_WhenAddingMultipleTransactionsSequentially() {
        // Arrange
        Report report = new Report(); // Testing the default no-args scenario too
        // Initial state leaves BigDecimals as null, testing the robust null-handling of addIncome/addExpense

        // Act
        report.addIncome(new BigDecimal("1000.00"));
        report.addExpense(new BigDecimal("200.00"));
        report.addIncome(new BigDecimal("500.50"));
        report.addExpense(new BigDecimal("150.25"));
        report.addExpense(new BigDecimal("50.25"));

        // Assert
        // Total Income: 1000.00 + 500.50 = 1500.50
        // Total Expense: 200.00 + 150.25 + 50.25 = 400.50
        // Balance: 1500.50 - 400.50 = 1100.00
        assertAll("Verify sequential accumulation of multiple transactions",
                () -> assertEquals(0, new BigDecimal("1500.50").compareTo(report.getTotalIncome()),
                        "Total income should accumulate perfectly to 1500.50"),
                () -> assertEquals(0, new BigDecimal("400.50").compareTo(report.getTotalExpense()),
                        "Total expense should accumulate perfectly to 400.50"),
                () -> assertEquals(0, new BigDecimal("1100.00").compareTo(report.getBalance()),
                        "Sequential balance should be exactly 1100.00")
        );
    }

    @Test
    @DisplayName("should handle null inputs gracefully without throwing exceptions or changing totals")
    void shouldIgnoreNullAmounts() {
        // Arrange
        Report report = Report.builder()
                .totalIncome(new BigDecimal("100"))
                .totalExpense(new BigDecimal("50"))
                .balance(new BigDecimal("50"))
                .build();

        // Act
        report.addIncome(null);
        report.addExpense(null);

        // Assert
        assertAll("Verify null handling protection",
                () -> assertEquals(0, new BigDecimal("100").compareTo(report.getTotalIncome()),
                        "Total income should not change after null input"),
                () -> assertEquals(0, new BigDecimal("50").compareTo(report.getTotalExpense()),
                        "Total expense should not change after null input"),
                () -> assertEquals(0, new BigDecimal("50").compareTo(report.getBalance()),
                        "Balance should remain unaffected")
        );
    }
}

package com.budget.qa.questions;

import com.budget.qa.ui.BudgetAppTargets;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.serenitybdd.annotations.Step;

import org.openqa.selenium.WebElement;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/**
 * Screenplay Question: Read the Balance Total from the Dashboard.
 *
 * <p>Extracts the formatted currency value from the "Balance Total" card
 * in the ReportSummaryCards component and parses it to a double.</p>
 *
 * <p>The value displayed uses {@code formatCurrency(balance)} from the
 * frontend, which renders as "$1,500.00" or "$-500.00".</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   Double balance = actor.asksFor(DashboardBalance.displayed());
 * }</pre>
 */
public class DashboardBalance implements Question<Double> {

    private static final int WAIT_TIMEOUT = 15;

    public static DashboardBalance displayed() {
        return new DashboardBalance();
    }

    @Override
    public Double answeredBy(Actor actor) {
        // Wait for the balance card to be visible
        actor.attemptsTo(
            WaitUntil.the(BudgetAppTargets.BALANCE_TOTAL_VALUE, isVisible())
                .forNoMoreThan(WAIT_TIMEOUT).seconds()
        );

        // Read the text content from the balance card
        WebElement balanceElement = BrowseTheWeb.as(actor)
            .getDriver()
            .findElement(BudgetAppTargets.BALANCE_TOTAL_VALUE);

        String rawText = balanceElement.getText().trim();

        // Parse the currency string (e.g., "$1,500.00" or "-$500.00" or "$-500.00")
        return parseCurrency(rawText);
    }

    /**
     * Parses a formatted COP currency string to a double.
     *
     * <p><strong>Colombian format (es-CO / COP):</strong></p>
     * <ul>
     *     <li>{@code $ 30.448} → 30448.0 (dot = thousands separator, no decimals)</li>
     *     <li>{@code $ 1.500} → 1500.0</li>
     *     <li>{@code -$ 500} → -500.0</li>
     *     <li>{@code $ 0} → 0.0</li>
     * </ul>
     *
     * <p>The frontend uses {@code Intl.NumberFormat('es-CO', {currency:'COP',
     * maximumFractionDigits:0})} which formats with dots as thousands
     * separators and NO decimal digits.</p>
     */
    private double parseCurrency(String text) {
        if (text == null || text.isEmpty()) return 0.0;

        System.out.println("      [DashboardBalance] Raw text from UI: '" + text + "'");

        // Detect if negative (prefixed with - or containing -)
        boolean isNegative = text.contains("-");

        // Remove everything except digits, dots, and commas
        String cleaned = text.replaceAll("[^0-9.,]", "");

        // Colombian COP format: dot = thousands separator, comma = decimal
        // Since maximumFractionDigits=0, there are NO decimals
        // So all dots are thousands separators — remove them
        cleaned = cleaned.replace(".", "");

        // If there's a comma, it's the decimal separator — replace with dot
        cleaned = cleaned.replace(",", ".");

        if (cleaned.isEmpty()) return 0.0;

        try {
            double value = Double.parseDouble(cleaned);
            System.out.println("      [DashboardBalance] Parsed value: " + (isNegative ? -value : value));
            return isNegative ? -value : value;
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                "Could not parse dashboard balance from text: '" + text +
                "' (cleaned: '" + cleaned + "')", e
            );
        }
    }
}

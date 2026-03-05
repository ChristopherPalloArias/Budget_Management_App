package com.budget.qa.tasks;

import com.budget.qa.ui.BudgetAppTargets;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.serenitybdd.annotations.Step;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isNotVisible;

/**
 * Screenplay Task: Create a transaction via the Web UI modal form.
 *
 * <p>This task fills out the "Nueva Transacción" dialog form from the
 * Transactions page ({@code /transactions}). It handles Radix UI Select
 * components using raw WebDriver for portal-rendered dropdowns.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   actor.attemptsTo(
 *       CreateTransactionViaUI.ofType("INCOME")
 *           .withDescription("Salario mensual")
 *           .withAmount(2000.00)
 *           .withCategory("Salario")
 *           .onDate("2026-03-01")
 *   );
 * }</pre>
 */
public class CreateTransactionViaUI implements Task {

    private static final int WAIT_TIMEOUT = 10;

    private final String type;
    private String description = "";
    private double amount;
    private String category = "";
    private String date;

    public CreateTransactionViaUI(String type) {
        this.type = type;
        this.date = java.time.LocalDate.now().toString();
    }

    public static CreateTransactionViaUI ofType(String type) {
        return instrumented(CreateTransactionViaUI.class, type);
    }

    public CreateTransactionViaUI withDescription(String description) {
        this.description = description;
        return this;
    }

    public CreateTransactionViaUI withAmount(double amount) {
        this.amount = amount;
        return this;
    }

    public CreateTransactionViaUI withCategory(String category) {
        this.category = category;
        return this;
    }

    public CreateTransactionViaUI onDate(String date) {
        this.date = date;
        return this;
    }

    @Override
    @Step("{0} creates a #type transaction of $#amount via the UI form")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));

        // Step 1: Click "Nueva Transacción" button to open modal
        actor.attemptsTo(
            WaitUntil.the(BudgetAppTargets.NEW_TRANSACTION_BUTTON, isVisible())
                .forNoMoreThan(WAIT_TIMEOUT).seconds(),
            Click.on(BudgetAppTargets.NEW_TRANSACTION_BUTTON)
        );

        // Step 2: Wait for modal dialog to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(BudgetAppTargets.MODAL_DIALOG));

        // Small pause for Radix animation to complete
        sleep(500);

        // Step 3: Select transaction type (Radix UI Select — portal-based)
        String typeLabel = "INCOME".equals(type) ? "Ingreso" : "Egreso";
        selectRadixOption(driver, wait, BudgetAppTargets.TYPE_SELECT_TRIGGER, typeLabel);

        // Step 4: Fill description
        WebElement descInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(BudgetAppTargets.DESCRIPTION_INPUT)
        );
        descInput.clear();
        descInput.sendKeys(description);

        // Step 5: Fill amount
        WebElement amountInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(BudgetAppTargets.AMOUNT_INPUT)
        );
        amountInput.click();
        amountInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        amountInput.sendKeys(String.valueOf(amount));

        // Step 6: Select category (Radix UI Select — portal-based)
        selectRadixOption(driver, wait, BudgetAppTargets.CATEGORY_SELECT_TRIGGER, category);

        // Step 7: Fill date
        WebElement dateInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(BudgetAppTargets.DATE_INPUT)
        );
        // Use JavaScript to set date value (more reliable for input[type=date])
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].value = arguments[1]; " +
            "arguments[0].dispatchEvent(new Event('input', {bubbles: true})); " +
            "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
            dateInput, date
        );

        // Step 8: Submit the form
        WebElement submitBtn = wait.until(
            ExpectedConditions.elementToBeClickable(BudgetAppTargets.FORM_SUBMIT_BUTTON)
        );
        submitBtn.click();

        // Step 9: Wait for modal to close (indicates success)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(BudgetAppTargets.MODAL_DIALOG));

        // Small pause to let React state settle
        sleep(500);
    }

    /**
     * Interacts with a Radix UI Select component by:
     * 1. Clicking the trigger (combobox button)
     * 2. Waiting for the listbox portal to appear
     * 3. Clicking the desired option
     * 4. Waiting for the listbox to close
     */
    private void selectRadixOption(WebDriver driver, WebDriverWait wait, By triggerLocator, String optionText) {
        // Click the select trigger
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(triggerLocator));
        trigger.click();

        // Wait for the Radix portal listbox to appear
        sleep(300);

        // Find and click the option in the portal
        By optionLocator = BudgetAppTargets.selectOptionByText(optionText);

        try {
            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(optionLocator));
            option.click();
        } catch (Exception e) {
            // Fallback: try clicking by text content with JavaScript
            ((JavascriptExecutor) driver).executeScript(
                "const options = document.querySelectorAll('[role=\"option\"]');" +
                "for (const opt of options) {" +
                "  if (opt.textContent.trim() === arguments[0]) {" +
                "    opt.click();" +
                "    return;" +
                "  }" +
                "}",
                optionText
            );
        }

        // Wait for dropdown to close
        sleep(300);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

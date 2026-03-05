package com.budget.qa.tasks;

import com.budget.qa.ui.BudgetAppTargets;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/**
 * Screenplay Task: Navigate to the Transactions page via the sidebar.
 *
 * <p>Clicks the "Transacciones" link in the AppSidebar and waits for the
 * transaction list heading to appear.</p>
 */
public class NavigateToTransactions implements Task {

    private static final int WAIT_TIMEOUT_SECONDS = 15;

    public static NavigateToTransactions page() {
        return instrumented(NavigateToTransactions.class);
    }

    @Override
    @Step("{0} navigates to the Transactions page")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            WaitUntil.the(BudgetAppTargets.NAV_TRANSACTIONS, isVisible())
                .forNoMoreThan(WAIT_TIMEOUT_SECONDS).seconds(),
            Click.on(BudgetAppTargets.NAV_TRANSACTIONS)
        );

        // Wait for the transaction list to load
        actor.attemptsTo(
            WaitUntil.the(BudgetAppTargets.TRANSACTION_LIST_HEADING, isVisible())
                .forNoMoreThan(WAIT_TIMEOUT_SECONDS).seconds()
        );
    }
}

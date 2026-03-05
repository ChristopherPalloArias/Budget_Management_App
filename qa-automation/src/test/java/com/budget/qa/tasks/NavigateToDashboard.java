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
 * Screenplay Task: Navigate to the Dashboard (Reports) page via the sidebar.
 *
 * <p>Clicks the "Reportes" link in the AppSidebar and waits for the
 * "Reportes Financieros" heading and summary cards to appear.</p>
 */
public class NavigateToDashboard implements Task {

    private static final int WAIT_TIMEOUT_SECONDS = 15;

    public static NavigateToDashboard page() {
        return instrumented(NavigateToDashboard.class);
    }

    @Override
    @Step("{0} navigates to the Dashboard (Reports) page")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            WaitUntil.the(BudgetAppTargets.NAV_DASHBOARD, isVisible())
                .forNoMoreThan(WAIT_TIMEOUT_SECONDS).seconds(),
            Click.on(BudgetAppTargets.NAV_DASHBOARD)
        );

        // Wait for the Reports page heading to appear
        actor.attemptsTo(
            WaitUntil.the(BudgetAppTargets.DASHBOARD_HEADING, isVisible())
                .forNoMoreThan(WAIT_TIMEOUT_SECONDS).seconds()
        );
    }
}

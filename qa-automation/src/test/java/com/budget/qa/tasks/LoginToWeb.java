package com.budget.qa.tasks;

import com.budget.qa.ui.BudgetAppTargets;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.serenitybdd.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

/**
 * Screenplay Task: Log in to the Budget Management App via the Web UI.
 *
 * <p><strong>Flow:</strong></p>
 * <ol>
 *     <li>Navigate to {@code http://localhost:3000/login}</li>
 *     <li>Wait for the login form to render (React lazy-load)</li>
 *     <li>Enter email and password credentials</li>
 *     <li>Click "Iniciar Sesión" submit button</li>
 *     <li>Wait for sidebar to appear (confirms successful login + redirect)</li>
 * </ol>
 */
public class LoginToWeb implements Task {

    private static final String FRONTEND_BASE_URL = "http://localhost:3000";
    private static final String LOGIN_URL = FRONTEND_BASE_URL + "/login";
    private static final int WAIT_TIMEOUT_SECONDS = 20;

    private final String email;
    private final String password;

    public LoginToWeb(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static LoginToWeb withCredentials(String email, String password) {
        return instrumented(LoginToWeb.class, email, password);
    }

    @Override
    @Step("{0} logs in via the Web UI with email '#email'")
    public <T extends Actor> void performAs(T actor) {

        // Step 1: Navigate to the login page
        actor.attemptsTo(
            Open.url(LOGIN_URL)
        );

        // Step 2: Wait for the login form to be rendered
        actor.attemptsTo(
            WaitUntil.the(BudgetAppTargets.EMAIL_INPUT, isVisible())
                .forNoMoreThan(WAIT_TIMEOUT_SECONDS).seconds()
        );

        // Step 3: Fill in credentials
        actor.attemptsTo(
            Enter.theValue(email).into(BudgetAppTargets.EMAIL_INPUT),
            Enter.theValue(password).into(BudgetAppTargets.PASSWORD_INPUT)
        );

        // Step 4: Submit the form
        actor.attemptsTo(
            Click.on(BudgetAppTargets.LOGIN_BUTTON)
        );

        // Step 5: Wait for post-login redirect (sidebar appears = dashboard loaded)
        actor.attemptsTo(
            WaitUntil.the(BudgetAppTargets.SIDEBAR, isVisible())
                .forNoMoreThan(WAIT_TIMEOUT_SECONDS).seconds()
        );

        // Store login state in actor memory
        actor.remember("web_logged_in", true);
        actor.remember("user_email", email);
    }
}

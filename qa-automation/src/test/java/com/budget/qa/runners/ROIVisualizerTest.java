package com.budget.qa.runners;

import com.budget.qa.questions.DashboardBalance;
import com.budget.qa.tasks.Authenticate;
import com.budget.qa.tasks.CreateTransactionApi;
import com.budget.qa.tasks.CreateTransactionViaUI;
import com.budget.qa.tasks.LoginToWeb;
import com.budget.qa.tasks.NavigateToDashboard;
import com.budget.qa.tasks.NavigateToTransactions;

import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.rest.SerenityRest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

import net.serenitybdd.annotations.Managed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 *  ROI VISUALIZER — FLUJO CONTABLE COMPLETO (Nivel Pro)
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * <p>This pro-level test suite demonstrates a <strong>Complete Accounting
 * Flow</strong> through both UI and API pathways:</p>
 *
 * <h3>Test Flow</h3>
 * <ol>
 *     <li><strong>Login</strong> via Chrome (visible mode)</li>
 *     <li><strong>Navigate</strong> to Transactions page</li>
 *     <li><strong>Create INCOME</strong> ($2,000) via the UI form</li>
 *     <li><strong>Create EXPENSE</strong> ($500) via the UI form</li>
 *     <li><strong>Navigate</strong> to Dashboard</li>
 *     <li><strong>Validate Balance</strong> = $1,500 via UI Question (seeThat)</li>
 *     <li><strong>Cross-validate</strong> via API (optional but recommended)</li>
 * </ol>
 *
 * <h3>How to Run</h3>
 * <pre>
 *   ./gradlew test --tests "com.budget.qa.runners.ROIVisualizerTest"
 * </pre>
 *
 * <h3>Prerequisites</h3>
 * <ul>
 *     <li>Google Chrome installed</li>
 *     <li>Auth Service running on port 8083</li>
 *     <li>Transaction Service running on port 8081</li>
 *     <li>Report Service running on port 8082</li>
 *     <li>Frontend running on port 3000</li>
 *     <li>Registered test user: {@code test@budgetapp.com / SecurePass123!}</li>
 * </ul>
 */
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("ROI Visualizer — Flujo Contable Completo")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ROIVisualizerTest {

    // ─── Test Credentials ───────────────────────────────────────────────
    private static final String TEST_EMAIL    = "test@budgetapp.com";
    private static final String TEST_PASSWORD = "SecurePass123!";

    // ─── Base URLs ──────────────────────────────────────────────────────
    private static final String AUTH_BASE_URL        = "http://localhost:8083/api/v1";
    private static final String TRANSACTION_BASE_URL = "http://localhost:8081/api/v1";
    private static final String REPORT_BASE_URL      = "http://localhost:8082/api/v1";

    // ─── Transaction test data ──────────────────────────────────────────
    private static final double INCOME_AMOUNT  = 2000.00;
    private static final double EXPENSE_AMOUNT = 500.00;
    private static final double EXPECTED_BALANCE = INCOME_AMOUNT - EXPENSE_AMOUNT; // $1,500.00

    // ─── WebDriver (Chrome — Modo Visible) ──────────────────────────────
    @Managed(driver = "chrome")
    WebDriver browser;

    // ─── Actors ─────────────────────────────────────────────────────────
    private Actor accountant;
    private Actor apiAuditor;

    @BeforeEach
    void setupActors() {
        // UI Actor: browses the web with Chrome
        accountant = Actor.named("Contador (UI Tester)")
            .whoCan(BrowseTheWeb.with(browser));

        // API Actor: calls REST APIs for cross-validation
        apiAuditor = Actor.named("Auditor (API Tester)")
            .whoCan(CallAnApi.at(AUTH_BASE_URL));
    }

    // ═════════════════════════════════════════════════════════════════════
    //  TEST: FLUJO CONTABLE COMPLETO VIA UI
    // ═════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @Tag("ui")
    @Tag("e2e")
    @Tag("accounting-flow")
    @DisplayName("Flujo Contable Completo: Login → Crear Transacciones → Validar Balance en Dashboard")
    void completeAccountingFlowViaUI() {
        long startTime = System.currentTimeMillis();

        // ═════════════════════════════════════════════════════════════════
        //  STEP 1: LOGIN VIA THE WEB UI
        // ═════════════════════════════════════════════════════════════════
        System.out.println("\n══ STEP 1: Iniciando sesión en http://localhost:3000 ══");

        accountant.attemptsTo(
            LoginToWeb.withCredentials(TEST_EMAIL, TEST_PASSWORD)
        );

        Boolean loggedIn = accountant.recall("web_logged_in");
        assertThat(loggedIn)
            .as("El usuario debe estar logueado después del flujo de login")
            .isTrue();

        System.out.println("   ✅ Login exitoso — Dashboard cargado");

        // ═════════════════════════════════════════════════════════════════
        //  STEP 2: CAPTURAR BALANCE INICIAL DEL DASHBOARD
        // ═════════════════════════════════════════════════════════════════
        System.out.println("\n══ STEP 2: Capturando balance inicial del Dashboard ══");

        accountant.attemptsTo(
            NavigateToDashboard.page()
        );

        Double balanceBefore = accountant.asksFor(
            DashboardBalance.displayed()
        );

        System.out.println("   Balance inicial: $" + balanceBefore);

        // ═════════════════════════════════════════════════════════════════
        //  STEP 3: NAVEGAR A TRANSACCIONES
        // ═════════════════════════════════════════════════════════════════
        System.out.println("\n══ STEP 3: Navegando a la página de Transacciones ══");

        accountant.attemptsTo(
            NavigateToTransactions.page()
        );

        System.out.println("   ✅ Página de Transacciones cargada");

        // ═════════════════════════════════════════════════════════════════
        //  STEP 4: CREAR TRANSACCIÓN — INCOME ($2,000)
        // ═════════════════════════════════════════════════════════════════
        System.out.println("\n══ STEP 4: Creando INGRESO de $" + INCOME_AMOUNT + " ══");

        accountant.attemptsTo(
            CreateTransactionViaUI.ofType("INCOME")
                .withDescription("Salario mensual - Marzo 2026")
                .withAmount(INCOME_AMOUNT)
                .withCategory("Salario")
                .onDate("2026-03-01")
        );

        System.out.println("   ✅ Transacción INCOME creada exitosamente");

        // ═════════════════════════════════════════════════════════════════
        //  STEP 5: CREAR TRANSACCIÓN — EXPENSE ($500)
        // ═════════════════════════════════════════════════════════════════
        System.out.println("\n══ STEP 5: Creando EGRESO de $" + EXPENSE_AMOUNT + " ══");

        accountant.attemptsTo(
            CreateTransactionViaUI.ofType("EXPENSE")
                .withDescription("Arriendo oficina - Marzo 2026")
                .withAmount(EXPENSE_AMOUNT)
                .withCategory("Vivienda")
                .onDate("2026-03-01")
        );

        System.out.println("   ✅ Transacción EXPENSE creada exitosamente");

        // ═════════════════════════════════════════════════════════════════
        //  STEP 6: NAVEGAR AL DASHBOARD Y VALIDAR BALANCE
        // ═════════════════════════════════════════════════════════════════
        System.out.println("\n══ STEP 6: Navegando al Dashboard para validar balance ══");

        accountant.attemptsTo(
            NavigateToDashboard.page()
        );

        // Ask the Question: ¿Cuál es el balance mostrado ahora?
        Double balanceAfter = accountant.asksFor(
            DashboardBalance.displayed()
        );

        double balanceDelta = balanceAfter - balanceBefore;

        System.out.println("   Balance antes:     $" + balanceBefore);
        System.out.println("   Balance después:   $" + balanceAfter);
        System.out.println("   Delta:             $" + balanceDelta);
        System.out.println("   Delta esperado:    $" + EXPECTED_BALANCE);

        assertThat(balanceDelta)
            .as("El delta del balance debe ser INCOME($%.0f) - EXPENSE($%.0f) = $%.2f",
                INCOME_AMOUNT, EXPENSE_AMOUNT, EXPECTED_BALANCE)
            .isEqualTo(EXPECTED_BALANCE);

        System.out.println("   ✅ Balance validado correctamente — Delta = $" + balanceDelta);

        // ── Timing ──────────────────────────────────────────────────────
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println();
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.println("  FLUJO CONTABLE COMPLETO (UI) finalizado en: " + elapsed + " ms");
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.println();
    }

    // ═════════════════════════════════════════════════════════════════════
    //  TEST: CROSS-VALIDATION VIA API
    // ═════════════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @Tag("api")
    @Tag("high-roi")
    @Tag("cross-validation")
    @DisplayName("Cross-Validation: Verificar mismo flujo contable via API (High ROI)")
    void crossValidateAccountingFlowViaAPI() {
        long startTime = System.currentTimeMillis();

        System.out.println("\n══ API CROSS-VALIDATION: Replicando flujo contable ══");

        // ── Step 1: Authenticate via API ────────────────────────────────
        apiAuditor.attemptsTo(
            Authenticate.withCredentials(TEST_EMAIL, TEST_PASSWORD)
        );

        int authStatus = SerenityRest.lastResponse().getStatusCode();
        assertThat(authStatus)
            .as("Authentication should return 200 OK")
            .isEqualTo(200);

        String token = apiAuditor.recall("jwt_token");
        assertThat(token).isNotNull().isNotEmpty();

        System.out.println("   ✅ API Auth exitoso — JWT obtenido");

        // ── Pre-Step: Capture initial balance from Report Service ───────
        io.restassured.response.Response initialSummaryResponse = SerenityRest.given()
            .header("Authorization", "Bearer " + token)
            .queryParam("startPeriod", "2026-03")
            .queryParam("endPeriod", "2026-03")
            .get(REPORT_BASE_URL + "/reports/summary");
        
        double initialBalance = 0.0;
        if (initialSummaryResponse.statusCode() == 200) {
            initialBalance = initialSummaryResponse.jsonPath().getDouble("totalBalance");
        }
        System.out.println("   Balance inicial API: $" + initialBalance);

        // ── Step 2: Create INCOME transaction via API ───────────────────
        apiAuditor.whoCan(CallAnApi.at(TRANSACTION_BASE_URL));

        apiAuditor.attemptsTo(
            CreateTransactionApi.ofType("INCOME")
                .withAmount(INCOME_AMOUNT)
                .withCategory("Salary")
                .withDescription("API Cross-Validation — INCOME")
                .onDate("2026-03-03")
        );

        int incomeStatus = SerenityRest.lastResponse().getStatusCode();
        assertThat(incomeStatus)
            .as("INCOME transaction should return 201 Created")
            .isEqualTo(201);

        System.out.println("   ✅ INCOME transaction created via API: $" + INCOME_AMOUNT);

        // ── Step 3: Create EXPENSE transaction via API ──────────────────
        apiAuditor.attemptsTo(
            CreateTransactionApi.ofType("EXPENSE")
                .withAmount(EXPENSE_AMOUNT)
                .withCategory("Transport")
                .withDescription("API Cross-Validation — EXPENSE")
                .onDate("2026-03-03")
        );

        int expenseStatus = SerenityRest.lastResponse().getStatusCode();
        assertThat(expenseStatus)
            .as("EXPENSE transaction should return 201 Created")
            .isEqualTo(201);

        System.out.println("   ✅ EXPENSE transaction created via API: $" + EXPENSE_AMOUNT);

        // ── Step 4: Validate via Report Service (Real Full-Stack) ────────
        // Give RabbitMQ a split second to process the async events reliably
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        io.restassured.response.Response finalSummaryResponse = SerenityRest.given()
            .header("Authorization", "Bearer " + token)
            .queryParam("startPeriod", "2026-03")
            .queryParam("endPeriod", "2026-03")
            .get(REPORT_BASE_URL + "/reports/summary");

        assertThat(finalSummaryResponse.statusCode())
            .as("Report summary should return 200 OK")
            .isEqualTo(200);

        double finalBalance = finalSummaryResponse.jsonPath().getDouble("totalBalance");
        double delta = finalBalance - initialBalance;
        
        assertThat(delta)
            .as("API real delta balance should match expected: $%.2f", EXPECTED_BALANCE)
            .isEqualTo(EXPECTED_BALANCE);

        System.out.println("   Balance API antes:     $" + initialBalance);
        System.out.println("   Balance API después:   $" + finalBalance);
        System.out.println("   => Delta API validado = $" + delta + " — Matches expected $" + EXPECTED_BALANCE);

        // ── Timing ──────────────────────────────────────────────────────
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println();
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.println("  API CROSS-VALIDATION completada en: " + elapsed + " ms");
        System.out.println("  ► UI test: ~15-30 seconds  vs  API test: " + elapsed + " ms");
        System.out.println("  ► ROI del API testing = " + (elapsed < 1000 ? "ALTO" : "MEDIO"));
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.println();
    }
}

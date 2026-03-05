package com.budget.qa.ui;

import org.openqa.selenium.By;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 *  UI Targets — Centralized CSS/XPath Selectors for Budget Management App
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * <p>Single source of truth for all UI element locators used across Screenplay
 * tasks and questions. Mapped directly from the React component source code.</p>
 *
 * <p><strong>Source mapping:</strong></p>
 * <ul>
 *     <li>Login: {@code LoginForm.tsx} — #email, #password, button[type=submit]</li>
 *     <li>Sidebar: {@code AppSidebar.tsx} — NavLink[href="/transactions"]</li>
 *     <li>Transactions: {@code DataTableToolbar.tsx} — "Nueva Transacción" button</li>
 *     <li>Transaction Form: {@code TransactionForm.tsx} — Select, Input fields</li>
 *     <li>Dashboard: {@code ReportSummaryCards.tsx} — Balance, Income, Expense cards</li>
 * </ul>
 */
public final class BudgetAppTargets {

    private BudgetAppTargets() { /* Utility class */ }

    // ─── LOGIN PAGE (/login) ────────────────────────────────────────────
    public static final By EMAIL_INPUT       = By.id("email");
    public static final By PASSWORD_INPUT    = By.id("password");
    public static final By LOGIN_BUTTON      = By.cssSelector("button[type='submit']");

    // ─── SIDEBAR NAVIGATION ────────────────────────────────────────────
    public static final By NAV_TRANSACTIONS  = By.cssSelector("a[href='/transactions']");
    public static final By NAV_DASHBOARD     = By.cssSelector("a[href='/dashboard']");
    public static final By SIDEBAR           = By.cssSelector("[data-sidebar='sidebar']");

    // ─── TRANSACTION PAGE (/transactions) ──────────────────────────────
    // DataTableToolbar.tsx — "Nueva Transacción" button (has <Plus> icon + span)
    public static final By NEW_TRANSACTION_BUTTON = By.xpath(
        "//button[.//span[contains(text(),'Nueva Transacción')] or contains(.,'Nueva')]"
    );

    // DataTable.tsx — Transaction list heading
    public static final By TRANSACTION_LIST_HEADING = By.xpath(
        "//h2[contains(text(), 'Listado de Transacciones')]"
    );

    // ─── TRANSACTION FORM (Modal Dialog — Radix UI) ────────────────────
    // Radix Dialog renders with role="dialog" attribute
    public static final By MODAL_DIALOG = By.cssSelector("[role='dialog']");

    // Type select — first combobox inside the dialog
    // TransactionForm.tsx renders Type as the FIRST <Select> (role="combobox")
    public static final By TYPE_SELECT_TRIGGER = By.xpath(
        "(//div[@role='dialog']//button[@role='combobox'])[1]"
    );

    // Description input — placeholder "Ej: Supermercado"
    public static final By DESCRIPTION_INPUT = By.xpath(
        "//div[@role='dialog']//input[@placeholder='Ej: Supermercado']"
    );

    // Amount input — type="number"
    public static final By AMOUNT_INPUT = By.xpath(
        "//div[@role='dialog']//input[@type='number']"
    );

    // Category select — second combobox inside the dialog
    public static final By CATEGORY_SELECT_TRIGGER = By.xpath(
        "(//div[@role='dialog']//button[@role='combobox'])[2]"
    );

    // Date input — type="date"
    public static final By DATE_INPUT = By.xpath(
        "//div[@role='dialog']//input[@type='date']"
    );

    // Submit button inside dialog — "Crear Transacción" or "Creando..."
    public static final By FORM_SUBMIT_BUTTON = By.xpath(
        "//div[@role='dialog']//button[@type='submit']"
    );

    // ─── SELECT DROPDOWN OPTIONS (Radix UI Portal) ─────────────────────
    // Radix Select renders options in a portal OUTSIDE the dialog
    // each option has role="option" and a data-value or inner text
    public static By selectOptionByText(String text) {
        return By.xpath(
            String.format(
                "//div[@role='listbox']//div[@role='option'][normalize-space(.)='%s']",
                text
            )
        );
    }

    // ─── DASHBOARD / REPORTS PAGE ──────────────────────────────────────
    public static final By DASHBOARD_HEADING = By.xpath(
        "//h2[contains(text(), 'Reportes Financieros')]"
    );

    // ReportSummaryCards.tsx — Balance Total value (text-2xl font-bold, green or red)
    // The card structure is: Card > CardHeader > CardTitle("Balance Total") + CardContent > div.font-bold
    public static final By BALANCE_TOTAL_VALUE = By.xpath(
        "//*[contains(text(),'Balance Total')]/ancestor::div[contains(@class,'rounded-lg') or contains(@class,'card')]//div[contains(@class,'text-2xl') and contains(@class,'font-bold')]"
    );

    // Total Ingresos value
    public static final By TOTAL_INCOME_VALUE = By.xpath(
        "//*[contains(text(),'Total Ingresos')]/ancestor::div[contains(@class,'rounded-lg') or contains(@class,'card')]//div[contains(@class,'text-2xl') and contains(@class,'font-bold')]"
    );

    // Total Gastos value
    public static final By TOTAL_EXPENSES_VALUE = By.xpath(
        "//*[contains(text(),'Total Gastos')]/ancestor::div[contains(@class,'rounded-lg') or contains(@class,'card')]//div[contains(@class,'text-2xl') and contains(@class,'font-bold')]"
    );

    // ─── TOAST / SUCCESS NOTIFICATION ──────────────────────────────────
    public static final By SUCCESS_TOAST = By.cssSelector("[data-sonner-toast]");

    // ─── TABLE ROWS ────────────────────────────────────────────────────
    public static final By TRANSACTION_TABLE_ROWS = By.cssSelector("tbody tr");
}

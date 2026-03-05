# ══════════════════════════════════════════════════════════════════════════════
#  Feature: Financial Reports
#  Service: Report Microservice (Port 8082)
#  API Base: /api/v1/reports
# ══════════════════════════════════════════════════════════════════════════════

@api @reports
Feature: Financial Report Management
  As an authenticated user of the Budget Management App
  I want to view and manage my financial reports
  So that I can understand my income, expenses, and balance per period

  Background:
    Given the user is authenticated with valid credentials

  # ─── QUERY ─────────────────────────────────────────────────────────────

  @smoke @read
  Scenario: Retrieve report for a specific period
    Given the user has transactions in period "2026-03"
    When the user requests the report for period "2026-03"
    Then the response status code should be 200
    And the response should contain the total income
    And the response should contain the total expense
    And the response should contain the balance

  @read @pagination
  Scenario: List all reports with pagination
    When the user requests all reports with page 0 and size 10
    Then the response status code should be 200
    And the response should contain a paginated list

  @read @summary
  Scenario: Get report summary for a date range
    When the user requests a summary from "2026-01" to "2026-03"
    Then the response status code should be 200
    And the summary should contain aggregated totals

  # ─── RECALCULATE ───────────────────────────────────────────────────────

  @recalculate
  Scenario: Recalculate report for a specific period
    Given the user has transactions in period "2026-03"
    When the user recalculates the report for period "2026-03"
    Then the response status code should be 200
    And the report totals should reflect the actual transactions

  # ─── PDF DOWNLOAD ──────────────────────────────────────────────────────

  @pdf
  Scenario: Download report as PDF
    Given the user has a report for period "2026-03"
    When the user downloads the PDF for period "2026-03"
    Then the response status code should be 200
    And the response content type should be "application/pdf"

  # ─── DELETE ────────────────────────────────────────────────────────────

  @delete
  Scenario: Delete a report by period
    Given the user has a report for period "2026-03"
    When the user deletes the report for period "2026-03"
    Then the response status code should be 204

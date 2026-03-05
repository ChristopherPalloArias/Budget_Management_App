# ══════════════════════════════════════════════════════════════════════════════
#  Feature: Transaction CRUD Operations
#  Service: Transaction Microservice (Port 8081)
#  API Base: /api/v1/transactions
# ══════════════════════════════════════════════════════════════════════════════

@api @transactions
Feature: Transaction Management
  As a registered user of the Budget Management App
  I want to create, read, update, and delete financial transactions
  So that I can track my income and expenses accurately

  Background:
    Given the user is authenticated with valid credentials

  # ─── CREATE ──────────────────────────────────────────────────────────────

  @smoke @create
  Scenario: Successfully create an income transaction
    When the user creates a transaction with the following details:
      | type        | INCOME               |
      | amount      | 1500.00              |
      | category    | Salary               |
      | description | Monthly salary       |
      | date        | 2026-03-01           |
    Then the response status code should be 201
    And the response should contain the transaction with type "INCOME"
    And the response should contain the transaction with amount 1500.00

  @smoke @create
  Scenario: Successfully create an expense transaction
    When the user creates a transaction with the following details:
      | type        | EXPENSE              |
      | amount      | 45.99                |
      | category    | Food                 |
      | description | Grocery shopping     |
      | date        | 2026-03-02           |
    Then the response status code should be 201
    And the response should contain the transaction with type "EXPENSE"

  @negative @create
  Scenario: Fail to create a transaction with missing required fields
    When the user creates a transaction with the following details:
      | type        |                      |
      | amount      |                      |
      | category    |                      |
      | date        |                      |
    Then the response status code should be 400

  @negative @create
  Scenario: Fail to create a transaction with negative amount
    When the user creates a transaction with the following details:
      | type        | EXPENSE              |
      | amount      | -100.00              |
      | category    | Food                 |
      | date        | 2026-03-01           |
    Then the response status code should be 400

  # ─── READ ────────────────────────────────────────────────────────────────

  @smoke @read
  Scenario: Retrieve a transaction by ID
    Given the user has created a transaction of type "INCOME" with amount 500.00
    When the user retrieves the transaction by its ID
    Then the response status code should be 200
    And the response should contain the transaction with amount 500.00

  @read @pagination
  Scenario: List all transactions with pagination
    When the user requests all transactions with page 0 and size 10
    Then the response status code should be 200
    And the response should contain a paginated list

  @read @filter
  Scenario: List transactions filtered by period
    When the user requests transactions for period "2026-03"
    Then the response status code should be 200

  # ─── UPDATE ──────────────────────────────────────────────────────────────

  @update
  Scenario: Successfully update an existing transaction
    Given the user has created a transaction of type "EXPENSE" with amount 100.00
    When the user updates the transaction with:
      | amount      | 150.00               |
      | description | Updated expense      |
    Then the response status code should be 200
    And the response should contain the transaction with amount 150.00

  # ─── DELETE ──────────────────────────────────────────────────────────────

  @delete
  Scenario: Successfully delete a transaction
    Given the user has created a transaction of type "EXPENSE" with amount 25.00
    When the user deletes the transaction
    Then the response status code should be 204

  # ─── SECURITY ────────────────────────────────────────────────────────────

  @security @negative
  Scenario: Fail to access transactions without authentication
    Given the user is NOT authenticated
    When the user requests all transactions without a token
    Then the response status code should be 401

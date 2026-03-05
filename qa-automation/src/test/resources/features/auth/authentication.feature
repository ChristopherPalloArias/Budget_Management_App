# ══════════════════════════════════════════════════════════════════════════════
#  Feature: User Authentication
#  Service: Auth Microservice (Port 8083)
#  API Base: /api/v1/auth
# ══════════════════════════════════════════════════════════════════════════════

@api @auth
Feature: User Authentication
  As a visitor of the Budget Management App
  I want to register and log in with my credentials
  So that I can securely access my financial data

  # ─── REGISTRATION ──────────────────────────────────────────────────────

  @smoke @register
  Scenario: Successfully register a new user
    When a new user registers with the following details:
      | name     | Test User                |
      | email    | testuser@budgetapp.com   |
      | password | SecurePass123!           |
    Then the response status code should be 201
    And the response should contain a JWT token

  @negative @register
  Scenario: Fail to register with an already existing email
    Given a user with email "existing@budgetapp.com" already exists
    When a new user registers with the following details:
      | name     | Duplicate User           |
      | email    | existing@budgetapp.com   |
      | password | SecurePass123!           |
    Then the response status code should be 409

  @negative @register
  Scenario: Fail to register with invalid email format
    When a new user registers with the following details:
      | name     | Bad Email User           |
      | email    | not-an-email             |
      | password | SecurePass123!           |
    Then the response status code should be 400

  # ─── LOGIN ─────────────────────────────────────────────────────────────

  @smoke @login
  Scenario: Successfully login with valid credentials
    Given a registered user with email "login@budgetapp.com" and password "SecurePass123!"
    When the user logs in with email "login@budgetapp.com" and password "SecurePass123!"
    Then the response status code should be 200
    And the response should contain a JWT token

  @negative @login
  Scenario: Fail to login with wrong password
    Given a registered user with email "wrong@budgetapp.com" and password "CorrectPass123!"
    When the user logs in with email "wrong@budgetapp.com" and password "WrongPassword!"
    Then the response status code should be 401

  @negative @login
  Scenario: Fail to login with non-existent user
    When the user logs in with email "ghost@budgetapp.com" and password "NoUser123!"
    Then the response status code should be 401

  # ─── CURRENT USER ──────────────────────────────────────────────────────

  @smoke @me
  Scenario: Retrieve current user profile
    Given the user is authenticated with valid credentials
    When the user requests their profile via GET /auth/me
    Then the response status code should be 200
    And the response should contain the user's email

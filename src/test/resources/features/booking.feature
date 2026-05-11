Feature: Booking API tests

  Scenario: Login works with correct credentials
    When I login with username "admin" and password "password"
    Then the status code is 200
    And the response contains a token

  Scenario: Login fails with wrong password
    When I login with username "admin" and password "ishimweeeeeee-test"
    Then the status code is 401

  Scenario: Health endpoint returns UP
    When I check the health endpoint
    Then the status code is 200
    And the API status is UP

  Scenario: Create a booking with valid data
    When I create a valid booking
    Then the status code is 200
    And the response has a bookingid

  # firstname has to be between 3 and 18 chars
  Scenario: Create a booking with a firstname that is too short
    When I create a booking with firstname "Jo"
    Then the status code is 400

  Scenario: Create a booking with a firstname that is too long
    When I create a booking with firstname "Remyyyyyyyyyyyyyyyyyyyyy"
    Then the status code is 400

  Scenario: Get a booking when authenticated
    Given I am logged in as admin
    When I create a valid booking
    And I get the booking
    Then the status code is 200
    And the booking has firstname "John"

  # not sure if it returns 401 or 403, but should not be 200
  Scenario: Get a booking without a token should fail
    Given I am logged in as admin
    When I create a valid booking
    And I get the booking without a token
    Then the status code is not 200

  Scenario: Update a booking when authenticated
    Given I am logged in as admin
    When I create a valid booking
    And I update the booking
    Then the status code is 200
    And the booking has firstname "Jane"

  Scenario: Update without token should fail
    Given I am logged in as admin
    When I create a valid booking
    And I update the booking without a token
    Then the status code is not 200

  Scenario: Delete a booking
    Given I am logged in as admin
    When I create a valid booking
    And I delete the booking
    Then the status code is 201

  Scenario: Delete without token should fail
    Given I am logged in as admin
    When I create a valid booking
    And I delete the booking without a token
    Then the status code is not 200

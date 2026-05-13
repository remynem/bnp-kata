package com.booking.steps;

import com.booking.config.ApiConfig;
import com.booking.model.Booking;
import com.booking.model.BookingDates;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BookingSteps {

    private Response response;
    private String token;
    private int bookingId;
    static {
        RestAssured.baseURI = "https://automationintesting.online/api";
    }
    // 840 unique year-month slots (70 years × 12 months).
    // base shifts by the current second, so runs started at different seconds
    // never share the same slot. The API resets every 10 min (600s < 840s),
    // so collisions with data from a previous reset window can't happen.
    private static final java.util.concurrent.atomic.AtomicInteger callCounter =
            new java.util.concurrent.atomic.AtomicInteger(0);

    private Booking createTestBooking() {
        int base = (int) (System.currentTimeMillis() / 1000 % 840);
        int slot = (base + callCounter.getAndIncrement()) % 840;
        int year = 2030 + slot / 12;
        int month = slot % 12 + 1;
        Booking b = new Booking();
        b.roomid = 2;
        b.firstname = "John";
        b.lastname = "Doe";
        b.depositpaid = true;
        b.bookingdates = new BookingDates(
                String.format("%d-%02d-01", year, month),
                String.format("%d-%02d-05", year, month));
        b.email = "john@example.com";
        b.phone = "12345678901";
        return b;
    }

    @Given("I am logged in as admin")
    public void iAmLoggedInAsAdmin() {
        response = given()
                .contentType("application/json")
                .body("{\"username\":\"" + ApiConfig.USERNAME + "\",\"password\":\"" + ApiConfig.PASSWORD + "\"}")
                .post(ApiConfig.BASE_URL + "/auth/login");

        token = response.jsonPath().getString("token");
    }

    @When("I login with username {string} and password {string}")
    public void iLoginWith(String username, String password) {
        response = given()
                .contentType("application/json")
                .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .post(ApiConfig.BASE_URL + "/auth/login");
    }

    @When("I check the health endpoint")
    public void iCheckTheHealthEndpoint() {
        response = given().get(ApiConfig.BASE_URL + "/booking/actuator/health");
    }

    @When("I create a valid booking")
    public void iCreateAValidBooking() {
        response = given()
                .contentType("application/json")
                .body(createTestBooking())
                .post(ApiConfig.BASE_URL + "/booking");

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            bookingId = response.jsonPath().getInt("bookingid");
        }
    }

    @When("I create a booking with firstname {string}")
    public void iCreateABookingWithFirstname(String firstname) {
        Booking b = createTestBooking();
        b.firstname = firstname;

        response = given()
                .contentType("application/json")
                .body(b)
                .post(ApiConfig.BASE_URL + "/booking");
    }

    @When("I get the booking")
    public void iGetTheBooking() {
        response = given()
                .cookie("token", token)
                .get(ApiConfig.BASE_URL + "/booking/" + bookingId);
    }

    @When("I get the booking without a token")
    public void iGetTheBookingWithoutToken() {
        response = given().get(ApiConfig.BASE_URL + "/booking/" + bookingId);
    }

    @When("I update the booking")
    public void iUpdateTheBooking() {
        // updating with different name and dates to verify it actually changes
        String suffix = String.valueOf(System.currentTimeMillis()).substring(8);
        Booking b = createTestBooking();
        b.firstname = "Jane";
        b.lastname = "Smith";
        b.depositpaid = false;
        b.bookingdates = new BookingDates("2027-0" + (suffix.charAt(0) % 9 + 1) + "-10",
                "2027-0" + (suffix.charAt(0) % 9 + 1) + "-15");
        b.email = "jane@example.com";
        b.phone = "09876543210";

        response = given()
                .contentType("application/json")
                .cookie("token", token)
                .body(b)
                .put(ApiConfig.BASE_URL + "/booking/" + bookingId);
    }

    @When("I update the booking without a token")
    public void iUpdateTheBookingWithoutToken() {
        Booking b = createTestBooking();
        b.firstname = "Jane";

        response = given()
                .contentType("application/json")
                .body(b)
                .put(ApiConfig.BASE_URL + "/booking/" + bookingId);
    }

    @When("I delete the booking")
    public void iDeleteTheBooking() {
        response = given()
                .cookie("token", token)
                .delete(ApiConfig.BASE_URL + "/booking/" + bookingId);
    }

    @When("I delete the booking without a token")
    public void iDeleteTheBookingWithoutToken() {
        response = given().delete(ApiConfig.BASE_URL + "/booking/" + bookingId);
    }

    @Then("the status code is {int}")
    public void theStatusCodeIs(int expected) {
        assertThat(response.getStatusCode(), equalTo(expected));
    }

    @Then("the status code is not {int}")
    public void theStatusCodeIsNot(int unexpected) {
        assertThat(response.getStatusCode(), not(equalTo(unexpected)));
    }

    @Then("the response has a bookingid")
    public void theResponseHasABookingid() {
        assertThat(response.jsonPath().getInt("bookingid"), greaterThan(0));
    }

    @Then("the response contains a token")
    public void theResponseContainsAToken() {
        assertThat(response.jsonPath().getString("token"), not(emptyOrNullString()));
    }

    @Then("the booking has firstname {string}")
    public void theBookingHasFirstname(String expected) {
        // GET returns firstname at root; PUT wraps it under "booking"
        String actual = response.jsonPath().getString("firstname");
        if (actual == null) {
            actual = response.jsonPath().getString("booking.firstname");
        }
        assertThat(actual, equalTo(expected));
    }

    @Then("the API status is UP")
    public void theApiStatusIsUp() {
        assertThat(response.jsonPath().getString("status"), equalToIgnoringCase("UP"));
    }

    @Then("the response matches the {string} schema")
    public void theResponseMatchesSchema(String schemaName) {
        response.then().body(matchesJsonSchemaInClasspath("schemas/" + schemaName + ".json"));
    }
}

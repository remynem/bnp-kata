package com.booking.steps;

import com.booking.config.ApiConfig;
import com.booking.model.Booking;
import com.booking.model.BookingDates;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BookingSteps {

    private Response response;
    private String token;
    private int bookingId;

    static {
        RestAssured.baseURI = ApiConfig.BASE_URL;
    }

    private Booking createTestBooking() {
        Booking b = new Booking();
        b.roomid = 1;
        b.firstname = "John";
        b.lastname = "Doe";
        b.depositpaid = true;
        b.bookingdates = new BookingDates("2026-11-01", "2026-11-05");
        b.email = "john@example.com";
        b.phone = "12345678901";
        return b;
    }

    @Given("I am logged in as admin")
    public void iAmLoggedInAsAdmin() {
        response = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"" + ApiConfig.USERNAME + "\",\"password\":\"" + ApiConfig.PASSWORD + "\"}")
                .post("/auth/login");

        token = response.jsonPath().getString("token");
    }

    @When("I login with username {string} and password {string}")
    public void iLoginWith(String username, String password) {
        response = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .post("/auth/login");
    }

    @When("I check the health endpoint")
    public void iCheckTheHealthEndpoint() {
        response = given().get("/booking/actuator/health");
    }

    @When("I create a valid booking")
    public void iCreateAValidBooking() {
        response = given()
                .contentType(ContentType.JSON)
                .body(createTestBooking())
                .post("/booking");

        if (response.getStatusCode() == 200) {
            bookingId = response.jsonPath().getInt("bookingid");
        }
    }

    @When("I create a booking with firstname {string}")
    public void iCreateABookingWithFirstname(String firstname) {
        Booking b = createTestBooking();
        b.firstname = firstname;

        response = given()
                .contentType(ContentType.JSON)
                .body(b)
                .post("/booking");
    }

    @When("I get the booking")
    public void iGetTheBooking() {
        response = given()
                .cookie("token", token)
                .get("/booking/" + bookingId);
    }

    @When("I get the booking without a token")
    public void iGetTheBookingWithoutToken() {
        response = given().get("/booking/" + bookingId);
    }

    @When("I update the booking")
    public void iUpdateTheBooking() {
        // updating with different name and dates to verify it actually changes
        Booking b = createTestBooking();
        b.firstname = "Jane";
        b.lastname = "Smith";
        b.depositpaid = false;
        b.bookingdates = new BookingDates("2026-12-01", "2026-12-07");
        b.email = "jane@example.com";
        b.phone = "09876543210";

        response = given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(b)
                .put("/booking/" + bookingId);
    }

    @When("I update the booking without a token")
    public void iUpdateTheBookingWithoutToken() {
        Booking b = createTestBooking();
        b.firstname = "Jane";

        response = given()
                .contentType(ContentType.JSON)
                .body(b)
                .put("/booking/" + bookingId);
    }

    @When("I delete the booking")
    public void iDeleteTheBooking() {
        response = given()
                .cookie("token", token)
                .delete("/booking/" + bookingId);
    }

    @When("I delete the booking without a token")
    public void iDeleteTheBookingWithoutToken() {
        response = given().delete("/booking/" + bookingId);
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
        assertThat(response.jsonPath().getString("firstname"), equalTo(expected));
    }

    @Then("the API status is UP")
    public void theApiStatusIsUp() {
        assertThat(response.jsonPath().getString("status"), equalToIgnoringCase("UP"));
    }
}

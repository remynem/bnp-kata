package com.booking.model;

public class BookingDates {
    public String checkin;
    public String checkout;

    public BookingDates(String checkin, String checkout) {
        this.checkin = checkin;
        this.checkout = checkout;
    }

    // needed by Jackson for deserialization
    public BookingDates() {}
}

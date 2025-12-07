package com.flightapp.demo.entity;

import lombok.Data;

@Data

public class BookingEvent {
	private String type;
	private Booking booking;
	private String bookingId; 
	public BookingEvent() {
	}

	public BookingEvent(String type, Booking booking) {
		this.type = type;
		this.booking = booking;
	}
	public BookingEvent(String type, String bookingId) {
        this.type = type;
        this.bookingId = bookingId;
    }
}


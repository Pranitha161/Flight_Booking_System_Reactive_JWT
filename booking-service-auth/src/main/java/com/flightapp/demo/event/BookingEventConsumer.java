package com.flightapp.demo.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.Booking;

@Service
public class BookingEventConsumer {

    @KafkaListener(topics = "booking-created", groupId = "mail-service-group")
    public void handleBookingCreated(Booking booking) {
        System.out.println("Mail: Booking created for PNR " + booking.getPnr());
        // emailService.sendBookingCreated(booking);
    }

    @KafkaListener(topics = "booking-deleted", groupId = "mail-service-group")
    public void handleBookingDeleted(Booking booking) {
        System.out.println("Mail: Booking deleted for PNR " + booking.getPnr());
        // emailService.sendBookingDeleted(booking);
    }
}

package com.flightapp.demo.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.Booking;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void bookingCreated(Booking booking) {
        kafkaTemplate.send("booking-created", booking.getPnr(), booking);
    }

    public void bookingDeleted(Booking booking) {
        kafkaTemplate.send("booking-deleted", booking.getPnr(), booking);
    }
}

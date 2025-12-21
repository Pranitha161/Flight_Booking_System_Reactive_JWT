package com.flightapp.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.flightapp.demo.entity.Booking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {


	Mono<ResponseEntity<Booking>> getTicketsByPnr(String pnr);

	Mono<ResponseEntity<List<Booking>>> getBookingsByEmail(String email);

	Mono<ResponseEntity<String>> deleteBookingByPnr(String pnr);

	Mono<ResponseEntity<String>> bookTicket(String flightId, Booking booking);

}

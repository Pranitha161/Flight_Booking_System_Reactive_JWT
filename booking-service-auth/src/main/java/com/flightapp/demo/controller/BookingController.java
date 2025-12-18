package com.flightapp.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.demo.entity.Booking;
import com.flightapp.demo.service.BookingService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BookingController {
	private final BookingService bookingService;
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/booking/{flightId}")
	public Mono<ResponseEntity<String>> bookTicket(
			@RequestBody Booking booking, @PathVariable String flightId) {
		
		return bookingService.bookTicket(flightId, booking);
	}
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/ticket/{pnr}")
	public Mono<ResponseEntity<Booking>> getByPnr(@PathVariable String pnr) {
		
		return bookingService.getTicketsByPnr(pnr);
	}
	@PreAuthorize("#emailId == authentication.token.claims['email'] or hasRole('ADMIN')")
    @GetMapping("/history/{emailId}")
	public Mono<ResponseEntity<Booking>> getByEmailId(
			@PathVariable String emailId) {
	
		return bookingService.getBookingsByEmail(emailId);
	}
	
	@PreAuthorize("hasRole('USER')")
    @DeleteMapping("/booking/cancel/{pnr}")
	public Mono<ResponseEntity<String>> cancelBooking(
			@PathVariable String pnr) {
		
		return bookingService.deleteBookingByPnr(pnr);
	}

	@GetMapping("/debug")
	public String debugAuth() {
		return "User: ";
	}
	@GetMapping("/debug-header")
	public Mono<String> debugHeader(@RequestHeader(value = "Authorization", required = false) String auth) {
	    return Mono.just("AUTH=" + auth);
	}


}

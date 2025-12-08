package com.flightapp.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	@PostMapping("/booking/{flightId}")
	public Mono<ResponseEntity<String>> bookTicket(@RequestHeader("X-User-Id") String userId,
			@RequestHeader("X-Roles") String roles, @RequestBody Booking booking, @PathVariable String flightId) {
		System.out.println(roles);
		if (roles.contains("ROLE_ADMIN")) {
			return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
		}
		return bookingService.bookTicket(flightId, booking,roles);
	}

	@GetMapping("/ticket/{pnr}")
	public Mono<ResponseEntity<Booking>> getByPnr(@RequestHeader("X-Roles") String roles, @PathVariable String pnr) {
		if (!roles.contains("ROLE_USER")) {
			return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
		}
		return bookingService.getTicketsByPnr(pnr);
	}

	@GetMapping("/history/{emailId}")
	public Mono<ResponseEntity<Booking>> getByEmailId(@RequestHeader("X-Email") String userEmail,   @RequestHeader("X-Roles") String roles,
			@PathVariable String emailId) {
		if (!userEmail.equals(emailId)&&!roles.contains("ROLE_USER")) {
			return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
		}
		return bookingService.getBookingsByEmail(emailId);
	}

	@DeleteMapping("/booking/cancel/{pnr}")
	public Mono<ResponseEntity<String>> cancelBooking(@RequestHeader("X-User-Id") String userId,@RequestHeader("X-Roles") String roles,
			@PathVariable String pnr) {
		if (!roles.contains("ROLE_USER")) {
			return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
		}
		return bookingService.deleteBookingByPnr(pnr,roles);
	}

	@GetMapping("/debug")
	public String debugAuth(@RequestHeader("X-User-Id") String userId, @RequestHeader("X-Roles") String roles) {
		return "User: " + userId + ", Roles: " + roles;
	}

}

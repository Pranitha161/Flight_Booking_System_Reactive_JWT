package com.flightapp.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.demo.entity.Flight;
import com.flightapp.demo.entity.SearchRequest;
import com.flightapp.demo.entity.Seat;
import com.flightapp.demo.service.FlightService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flight")
public class FlightController {
	private final FlightService flightService;
	 @GetMapping("/ping")
	    public String ping() {
	        
	        return "pong";
	    }
	 @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	 @PostMapping("/search")
	    public Mono<ResponseEntity<List<Flight>>> searchFlight(
	            @Valid @RequestBody SearchRequest searchRequest) {

	        return flightService.search(searchRequest);
	    }
	 @PreAuthorize("hasRole('ADMIN')")
	 @PostMapping("/add")
	    public Mono<ResponseEntity<String>> addInventory(
	            @Valid @RequestBody Flight flight) {

	        return flightService.addFlight(flight);
	    }
	 @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	 @PutMapping("/flights/{id}")
	    public Mono<ResponseEntity<Void>> updateFlight(
	            @PathVariable String id,
	            @Valid @RequestBody Flight flight) {
		 	System.out.println("update flight req");
	        return flightService.updateFlight(id, flight);
	    }
	 @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	 @GetMapping("/get/{flightId}")
	    public Mono<ResponseEntity<Flight>> getFlight(@PathVariable String flightId) {
	        return flightService.getFlightById(flightId);
	    }
	 @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	 @GetMapping("/get/flights")
	    public Flux<Flight> getAllFlights() {
		 System.out.println("im here in flights");
	        return flightService.getFlights();
	    }
	 @GetMapping("/internal/{flightId}")
	 public Mono<ResponseEntity<Flight>> internalGetFlight(@PathVariable String flightId) {
	     return flightService.getFlightById(flightId);
	 }

//	 @GetMapping("/internal/seats/{flightId}")
//	 public Mono<ResponseEntity<List<Seat>> internalGetSeats(@PathVariable String flightId) {
//	     return flightService.getSeatsByFlightId(flightId);
//	 }

	 @PutMapping("/internal/{flightId}")
	 public Mono<ResponseEntity<Void>> internalUpdateFlight(
	         @PathVariable String flightId,
	         @RequestBody Flight flight) {
	     return flightService.updateFlight(flightId, flight);
	 }

//	 @PutMapping("/internal/seats/{flightId}")
//	 public ResponseEntity<String> internalUpdateSeats(
//	         @PathVariable String flightId,
//	         @RequestBody List<Seat> seats) {
//	     return flightService.updateSeats(flightId, seats);
//	 }
	 @PreAuthorize("hasRole('ADMIN')")
	 @DeleteMapping("/flights/{id}")
	 public Mono<ResponseEntity<String>> deleteFlight(@PathVariable String id) {
	     return flightService.deleteFlight(id);
	 }

	 @GetMapping("/debug")
	    public String debug() {
	        return "Flight Service Debug OK";
	    }

}
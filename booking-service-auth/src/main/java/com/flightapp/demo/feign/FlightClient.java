package com.flightapp.demo.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.flightapp.demo.entity.Flight;
import com.flightapp.demo.entity.Seat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//@FeignClient(name = "flight-service")
//public interface FlightClient {
//	@GetMapping("/api/flight/get/{flightId}")
//	ResponseEntity<Flight> getFlightById(@PathVariable String flightId);
//
//	@PutMapping("/api/flight/flights/{id}")
//	ResponseEntity<Void> updateFlight(@PathVariable String id, @RequestBody Flight flight);
//
//	@GetMapping("/api/seats/flight/{flightId}")
//	ResponseEntity<List<Seat>> getSeatsByFlightId(@PathVariable String flightId);
//
//	@PutMapping("/api/seats/flights/{id}/seats")
//	ResponseEntity<Void> updateSeats(@PathVariable String id, @RequestBody List<Seat> seats);
//}
@FeignClient(name = "flight-service")
public interface FlightClient {

	
	   

		@GetMapping("/get/{flightId}")
		Mono<ResponseEntity<Flight>> getFlight(@PathVariable String flightId);

	    @PutMapping("/api/flight/flights/{id}")
	    Mono<ResponseEntity<Void>> updateFlight(@PathVariable("id") String id, @RequestBody Flight flight);
	    
	    @GetMapping("/api/seats/flight/{flightId}")
		Mono<ResponseEntity<List<Seat>>> getSeatsByFlightId(@PathVariable String flightId);

		@PutMapping("/api/seats/flights/{id}/seats")
		ResponseEntity<Void> updateSeats(@PathVariable String id,@RequestBody List<Seat> seats);
	   
	}




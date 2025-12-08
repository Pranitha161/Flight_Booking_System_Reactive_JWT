package com.flightapp.demo.service;

import org.springframework.http.ResponseEntity;

import com.flightapp.demo.entity.AirLine;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AirLineService {
	Flux<AirLine> getAllAirlines();

	Mono<ResponseEntity<AirLine>> getById(String id);

	Mono<AirLine> addFlightToAirline(String airlineId, String flightId);

}

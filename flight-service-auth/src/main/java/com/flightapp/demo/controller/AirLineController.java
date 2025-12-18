package com.flightapp.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.demo.entity.AirLine;
import com.flightapp.demo.service.AirLineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/flight/airline")
@RequiredArgsConstructor
public class AirLineController {

	private final AirLineService airlineService;
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/get")
	public Flux<AirLine> getAirlines() {
		
		return airlineService.getAllAirlines();
	}
	@PreAuthorize(" hasRole('ADMIN')")
	@GetMapping("/get/{id}")
	public Mono<ResponseEntity<AirLine>> getAirlineById(@Valid @PathVariable String id) {
		return airlineService.getById(id);
	}
	@PreAuthorize(" hasRole('ADMIN')")
	@PostMapping("/add")
	public Mono<ResponseEntity<String>> addAirline(
	       
	        @Valid @RequestBody AirLine airLine) {

		airLine.setId(null); 

	    return airlineService.addAirline(airLine);
	            
	}


}

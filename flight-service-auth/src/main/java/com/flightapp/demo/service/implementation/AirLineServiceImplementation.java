package com.flightapp.demo.service.implementation;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.AirLine;
import com.flightapp.demo.repository.AirLineRepository;
import com.flightapp.demo.service.AirLineService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AirLineServiceImplementation implements AirLineService {
	private final AirLineRepository airlineRepo;

	@Override
	public Flux<AirLine> getAllAirlines() {
		return airlineRepo.findAll();
	}
	@Override
	public Mono<AirLine> addFlightToAirline(String airlineId, String flightId) {
	    return airlineRepo.findById(airlineId) // Mono<Airline>
	        .flatMap(airline -> {
	            if (airline.getFlightIds() == null) {
	                airline.setFlightIds(new ArrayList<>());
	            }
	            airline.getFlightIds().add(flightId);
	            return airlineRepo.save(airline); // Mono<Airline>
	        });
	}


	@Override
	public Mono<ResponseEntity<AirLine>> getById(String id) {
		return airlineRepo.findById(id).map(ResponseEntity::ok)
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

}

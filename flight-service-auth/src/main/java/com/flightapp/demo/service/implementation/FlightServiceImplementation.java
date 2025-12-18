package com.flightapp.demo.service.implementation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.Flight;
import com.flightapp.demo.entity.SearchRequest;
import com.flightapp.demo.repository.AirLineRepository;
import com.flightapp.demo.repository.FlightRepository;
import com.flightapp.demo.service.AirLineService;
import com.flightapp.demo.service.FlightService;
import com.flightapp.demo.service.SeatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FlightServiceImplementation implements FlightService {
	private final FlightRepository flightRepo;
	private final AirLineRepository airlineRepo;
	private final SeatService seatService;
	private final AirLineService airlineService;

	@Override
	public Mono<ResponseEntity<List<Flight>>> search(SearchRequest searchRequest) {
		return flightRepo.getFightByFromPlaceAndToPlace(searchRequest.getFromPlace(), searchRequest.getToPlace())
				.filter(flight -> flight.getArrivalTime().toLocalDate().equals(searchRequest.getDate())).collectList()
				.map(list -> list.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(list));
	}


	@Override
	public Mono<ResponseEntity<String>> addFlight(Flight flight) {

		final int cols = 6;

		if (flight.getAvailableSeats() <= 0 || flight.getAvailableSeats() % cols != 0) {
			return Mono.just(
					ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Seats should be positive and divisible by 6"));
		}

		final int rows = flight.getAvailableSeats() / cols;

		return flightRepo.existsByFromPlaceAndToPlace(flight.getFromPlace(), flight.getToPlace()).flatMap(exists -> {
			if (exists) {
				return Mono.just(
						ResponseEntity.status(HttpStatus.CONFLICT).body("Already present with same from and to place"));
			}

			return airlineRepo.findById(flight.getAirlineId()).flatMap(existingAirline ->

			flightRepo.save(flight).flatMap(savedFlight ->

			seatService.initialiszeSeats(savedFlight.getId(), rows, cols)

					.then(airlineService.addFlightToAirline(savedFlight.getAirlineId(), savedFlight.getId()))

					.thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("{\"message\":\"Successfully created\"}"))))
					.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Airline not found")));
		}).onErrorResume(e -> Mono
				.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage())));
	}

	@Override
	public Flux<Flight> getFlights() {
		System.out.println("im in flight service impl");
		return flightRepo.findAll();
	}

	@Override
	public Mono<ResponseEntity<Flight>> getFlightById(String flightId) {
		return flightRepo.findById(flightId).map(ResponseEntity::ok)
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@Override
	public Mono<ResponseEntity<Void>> updateFlight(String id, Flight flightTest) {
		return flightRepo.findById(id).map(existing -> {
			existing.setAirlineId(flightTest.getAirlineId());
			existing.setArrivalTime(flightTest.getArrivalTime());
			existing.setDepartureTime(flightTest.getDepartureTime());
			existing.setPrice(flightTest.getPrice());
			existing.setId(id);
			return existing;
		}).flatMap(flightRepo::save).map(saved -> ResponseEntity.ok().<Void>build())
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}
	@Override
	public Mono<ResponseEntity<String>> deleteFlight(String id) {
		return flightRepo.findById(id)
	            .flatMap(flight -> flightRepo.delete(flight)
	                    .then(Mono.just(ResponseEntity.ok("Flight deleted successfully"))))
	            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

}

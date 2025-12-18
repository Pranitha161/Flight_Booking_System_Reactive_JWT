package com.flightapp.demo.service.implementation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.Booking;
import com.flightapp.demo.entity.Flight;
import com.flightapp.demo.entity.Seat;
import com.flightapp.demo.entity.User;
import com.flightapp.demo.event.BookingEventProducer;
import com.flightapp.demo.feign.FlightClient;
import com.flightapp.demo.feign.UserClient;
import com.flightapp.demo.repository.BookingRepository;
import com.flightapp.demo.service.BookingService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class BookingServiceImplementation implements BookingService {

	private final BookingRepository bookingRepo;
	private final BookingEventProducer eventProducer;
	private final FlightClient flightClient;
	private final UserClient userClient;

	public BookingServiceImplementation(BookingRepository bookingRepo, BookingEventProducer eventProducer,
			FlightClient flightClient, UserClient userClient) {
		this.bookingRepo = bookingRepo;
		this.eventProducer = eventProducer;
		this.flightClient = flightClient;
		this.userClient = userClient;
	}

	@Override
	public Mono<ResponseEntity<Booking>> getTicketsByPnr(String pnr) {
		return bookingRepo.findByPnr(pnr).map(ResponseEntity::ok)
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@Override
	public Mono<ResponseEntity<Booking>> getBookingsByEmail(String email) {
		return bookingRepo.findByEmail(email).map(ResponseEntity::ok)
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@Override
	@CircuitBreaker(name = "flightServiceCircuitBreaker", fallbackMethod = "fallbackDeleteBooking")
	public Mono<ResponseEntity<String>> deleteBookingByPnr(String pnr) {
		return bookingRepo.findByPnr(pnr)
				.flatMap(booking -> Mono.fromCallable(() -> flightClient.internalGetFlight(booking.getFlightId()))
						.subscribeOn(Schedulers.boundedElastic()).flatMap(flightResp -> {
							if (!flightResp.getStatusCode().is2xxSuccessful() || flightResp.getBody() == null) {
								return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Flight not found for booking PNR: " + pnr + "\"}"));}
							Flight flight = flightResp.getBody();
							ZoneId systemZone = ZoneId.systemDefault();
							ZonedDateTime now = ZonedDateTime.now(systemZone);
							ZonedDateTime departure = flight.getDepartureTime().atZone(systemZone);
							if (departure.isBefore(now.plusHours(24))) {
								return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("{\"message\":\"Cannot delete booking within 24 hours of departure for PNR: " + pnr + "\"}"));}
							return Mono.fromCallable(() -> flightClient.getSeatsByFlightId(booking.getFlightId()))
									.subscribeOn(Schedulers.boundedElastic()).flatMap(seatsResp -> {
										if (!seatsResp.getStatusCode().is2xxSuccessful() || seatsResp.getBody() == null
												|| seatsResp.getBody().isEmpty()) {
											return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
													.body("{\"message\":\"Seats not found for flight: " + booking.getFlightId() + "\"}"));}
										List<Seat> seats = seatsResp.getBody();
										List<String> seatNumbers = booking.getSeatNumbers();
										seats.stream().filter(s -> seatNumbers.contains(s.getSeatNumber()))
												.forEach(s -> s.setAvailable(true));
										flight.setAvailableSeats(flight.getAvailableSeats() + seatNumbers.size());
										return Mono.fromCallable(() -> {
											flightClient.updateSeats(booking.getFlightId(), seats);
											flightClient.internalUpdateFlight(booking.getFlightId(), flight);
											return true;
										}).subscribeOn(Schedulers.boundedElastic()).then(bookingRepo.delete(booking))
//                                                        .then(Mono.fromRunnable(() -> eventProducer.bookingDeleted(booking)))
												.thenReturn(ResponseEntity.ok(
													            "{\"message\":\"Booking with PNR " + pnr + " deleted successfully. Seats released.\"}"
													        )
													    );});
}))
				.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Booking not found\"}")));
	}

	public Mono<ResponseEntity<String>> fallbackDeleteBooking(String pnr, Throwable t) {
		return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Flight service unavailable while deleting booking with PNR: " + pnr));
	}

	@Override
	@CircuitBreaker(name = "flightServiceCircuitBreaker", fallbackMethod = "fallbackGetFlight")
	public Mono<ResponseEntity<String>> bookTicket(String flightId, Booking booking) {
		final List<String> seatReq = booking.getSeatNumbers();
		if (seatReq == null || seatReq.isEmpty()) {
			return Mono.just(ResponseEntity.badRequest().body("{\"message\":\"No seats requested\"}"));
		}
		return Mono.fromCallable(() -> userClient.internalGetPassenger(booking.getEmail()))
				.subscribeOn(Schedulers.boundedElastic()).flatMap(emailUserResp -> {
					if (!emailUserResp.getStatusCode().is2xxSuccessful() || emailUserResp.getBody() == null) {
						return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Invalid email\"}"));
					}
					return Mono.fromCallable(() -> userClient.internalGetUsersByIds(booking.getUserIds()))
							.subscribeOn(Schedulers.boundedElastic()).flatMap(usersByIds -> {
								if (usersByIds == null || usersByIds.size() != booking.getUserIds().size()) {
									return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
											.body("{\"message\":\"One or more passenger IDs are invalid\"}"));
								}
								return Mono.fromCallable(() -> flightClient.internalGetFlight(flightId))
								.subscribeOn(Schedulers.boundedElastic()).flatMap(flightResp -> {
								if (!flightResp.getStatusCode().is2xxSuccessful()|| flightResp.getBody() == null) {
									return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Flight not found with id: " + flightId + "\"}"));}
									Flight flight = flightResp.getBody();
									return Mono.fromCallable(() -> flightClient.getSeatsByFlightId(flightId)).subscribeOn(Schedulers.boundedElastic()).flatMap(seatsResp -> {
									if (!seatsResp.getStatusCode().is2xxSuccessful()|| seatsResp.getBody() == null|| seatsResp.getBody().isEmpty()) {
									return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"Seats not found for flight: " + flightId + "\"}"));}
									List<Seat> seats = seatsResp.getBody();
									for (String req : seatReq) {
									Seat seat = seats.stream().filter(s -> req.equals(s.getSeatNumber())).findFirst().orElse(null);
									if (seat == null) {
									return Mono.just(ResponseEntity.badRequest().body("{\"message\":\"Seat " + req + " does not exist\"}"));}
									if (!seat.isAvailable()) {
									return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("{\"message\":\"Seat " + req + " is already booked\"}"));
															}
														}
														booking.setPnr(flight.getId() + "-" + LocalDateTime.now()
																.format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));
														booking.setFlightId(flightId);
														booking.setTotalAmount(flight.getPrice().getOneWay());
														seats.stream().filter(s -> seatReq.contains(s.getSeatNumber()))
																.forEach(s -> s.setAvailable(false));
														int newAvailable = flight.getAvailableSeats() - seatReq.size();
														if (newAvailable < 0) {
															return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
																	.body("{\"message\":\"Insufficient seats available\"}")
																	);
														}
														flight.setAvailableSeats(newAvailable);
														return Mono.fromCallable(() -> {
															flightClient.updateSeats(flightId, seats);
															flightClient.internalUpdateFlight(flightId, flight);
															return true;
														}).subscribeOn(Schedulers.boundedElastic())
																.then(bookingRepo.save(booking))
//                                                                .then(Mono.fromRunnable(() ->
//                                                                        eventProducer.bookingCreated(booking)))
																.thenReturn(ResponseEntity.status(HttpStatus.CREATED)
																		.body("{\"message\":\"Booking created successfully\"," + "\"pnr\":\"" + booking.getPnr() + "\"," + "\"totalAmount\":" + booking.getTotalAmount() + "}"));
													});
										});
							});
				});
	}

	public Mono<ResponseEntity<String>> fallbackGetFlight(String flightId, Booking booking, Throwable t) {
		return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Flight service unavailable while booking flightId: " + flightId));
	}
}

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

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BookingServiceImplementation implements BookingService {

	private final BookingRepository bookingRepo;
	private final FlightClient flightClient;
	private final BookingEventProducer eventProducer;
	private final UserClient userClient;

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

	@CircuitBreaker(name = "flightServiceCircuitBreaker", fallbackMethod = "fallbackDeleteBooking")
	public Mono<ResponseEntity<String>> deleteBookingByPnr(String pnr) {

		return bookingRepo.findByPnr(pnr)
				.flatMap(booking -> flightClient.getFlight(booking.getFlightId()).flatMap(flightResp -> {
					Flight flight = flightResp.getBody();
					if (flight == null) {
						return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
								.body("Flight not found for booking PNR: " + pnr));
					}

					ZoneId systemZone = ZoneId.systemDefault();
					ZonedDateTime now = ZonedDateTime.now(systemZone);
					ZonedDateTime departure = flight.getDepartureTime().atZone(systemZone);

					if (departure.isBefore(now.plusHours(24))) {
						return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Cannot delete booking within 24 hours of departure for PNR: " + pnr));
					}
					return flightClient.getSeatsByFlightId(booking.getFlightId()).flatMap(seatResp -> {
						List<Seat> seats = seatResp.getBody();
						if (seats == null) {
							return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
									.body("Seats not found for flight: " + booking.getFlightId()));
						}

						List<String> seatNumbers = booking.getSeatNumbers();
						seats.stream().filter(s -> seatNumbers.contains(s.getSeatNumber()))
								.forEach(s -> s.setAvailable(true));

						flight.setAvailableSeats(flight.getAvailableSeats() + seatNumbers.size());

						return flightClient.updateFlight(flight.getId(), flight)
								.then(Mono.fromCallable(() -> flightClient.updateSeats(flight.getId(), seats)))
								.then(bookingRepo.delete(booking))
								.then(Mono.fromRunnable(() -> eventProducer.bookingDeleted(booking)))
								.thenReturn(ResponseEntity
										.ok("Booking with PNR " + pnr + " deleted successfully. Seats released."));
					});
				})).switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found")));
	}

	public Mono<ResponseEntity<String>> fallbackDeleteBooking(String pnr, Throwable t) {
		if (t instanceof FeignException.NotFound) {
			return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("Flight not found while deleting booking with PNR: " + pnr));
		}
		return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body("Flight service unavailable while deleting booking with PNR: " + pnr));
	}

	@CircuitBreaker(name = "flightServiceCircuitBreaker", fallbackMethod = "fallbackGetFlight")
	public Mono<ResponseEntity<String>> bookTicket(String flightId, Booking booking) {
		final List<String> seatReq = booking.getSeatNumbers();
		if (seatReq == null || seatReq.isEmpty()) {
			return Mono.just(ResponseEntity.badRequest().body("No seats requested"));
		}
		ResponseEntity<User> userByEmailResp = userClient.getPassenger(booking.getEmail());
	    if (!userByEmailResp.getStatusCode().is2xxSuccessful() || userByEmailResp.getBody() == null) {
	        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body("Invalid email"));
	    }
	    List<User> users = userClient.getUsersByIds(booking.getUserIds());
	    if (users == null || users.size() != booking.getUserIds().size()) {
	        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body("One or more passenger IDs are invalid"));
	    }
		return flightClient.getFlight(flightId).flatMap(flightResp -> {
			Flight flight = flightResp.getBody();
			if (flight == null) {
				return Mono.just(
						ResponseEntity.status(HttpStatus.NOT_FOUND).body("Flight not found with id: " + flightId));
			}
			return flightClient.getSeatsByFlightId(flightId).flatMap(seatsResp -> {
				List<Seat> seats = seatsResp.getBody();
				if (seats == null) {
					return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body("Seats not found for flight: " + flightId));
				}
				for (String req : seatReq) {
					Seat seat = seats.stream().filter(s -> req.equals(s.getSeatNumber())).findFirst().orElse(null);
					if (seat == null) {
						return Mono.just(ResponseEntity.badRequest().body("Seat " + req + " does not exist"));
					}
					if (!seat.isAvailable()) {
						return Mono.just(
								ResponseEntity.status(HttpStatus.CONFLICT).body("Seat " + req + " is already booked"));
					}
				}
				booking.setPnr(
						flight.getId() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));
				booking.setFlightId(flightId);
				booking.setTotalAmount(flight.getPrice().getOneWay());
				seats.stream().filter(s -> seatReq.contains(s.getSeatNumber())).forEach(s -> s.setAvailable(false));
				int newAvailable = flight.getAvailableSeats() - seatReq.size();
				if (newAvailable < 0) {
					return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("Insufficient seats available"));
				}
				flight.setAvailableSeats(newAvailable);
				return bookingRepo.save(booking)
						.then(Mono.fromCallable(() -> flightClient.updateFlight(flightId, flight)))
						.then(Mono.fromCallable(() -> flightClient.updateSeats(flightId, seats)))
						.then(Mono.fromRunnable(() -> eventProducer.bookingCreated(booking)))
						.thenReturn(ResponseEntity.status(HttpStatus.CREATED)
								.body("Booking created successfully with PNR: " + booking.getPnr()));
			});
		}).switchIfEmpty(
				Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Flight not found with id: " + flightId)));
	}

	public Mono<ResponseEntity<String>> fallbackGetFlight(String flightId, Booking booking, Throwable t) {
		if (t instanceof FeignException.NotFound) {
			return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Flight not found with id: " + flightId));
		} else if (t instanceof FeignException.ServiceUnavailable) {
			return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body("Flight service unavailable while booking flightId: " + flightId));
		} else {
			return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Unexpected error while booking flightId: " + flightId));
		}
	}

}

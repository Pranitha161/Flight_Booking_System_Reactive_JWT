package com.flightapp.demo.service.implementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.Booking;
import com.flightapp.demo.repository.BookingRepository;
import com.flightapp.demo.service.BookingService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BookingServiceImplementation implements BookingService {
	private final BookingRepository bookingRepo;
	private final FlightClient flightClient;
	private final UserClient userClient;

	public Mono<ResponseEntity<Booking>> getTicketsByPnr(String pnr) {
		return bookingRepo.findByPnr(pnr).map(ResponseEntity::ok)
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().<Booking>build()));
	}

	public Mono<ResponseEntity<Booking>> getBookingsByEmail(String email) {
		return bookingRepo.findByEmail(email).map(ResponseEntity::ok)
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().<Booking>build()));
		
	}

	public Mono<ResponseEntity<String>> deleteBookingByPnr(String pnr) {
		return bookingRepo.findByPnr(pnr).flatMap(
				booking -> bookingRepo.delete(booking).then(Mono.just(ResponseEntity.noContent().<Void>build())))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

//	public Mono<ResponseEntity<String>> bookTicket(String flightId, Booking booking) {
//		return flightRepo.findById(flightId)
//				.flatMap(flight -> seatRepo.findByFlightId(flightId).collectList().flatMap(seats -> {
//					List<String> seatReq = booking.getSeatNumbers();
//					for (String req : seatReq) {
//						Seat seat = seats.stream().filter(s -> s.getSeatNumber().equals(req)).findFirst().orElse(null);
//						if (seat == null || !seat.isAvailable()) {
//							return Mono.just(ResponseEntity.notFound().<Void>build());
//						}
//					}
//					seats.stream().filter(s -> seatReq.contains(s.getSeatNumber())).forEach(s -> s.setAvailable(false));
//					booking.setPnr(flight.getId() + "-"
//							+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));
//					float price = booking.getTripType().equals("ROUND_TRIP") ? flight.getPrice().getRoundTrip()
//							: flight.getPrice().getOneWay();
//					booking.setTotalAmount(price);
//					booking.setFlightId(flightId);
//					flight.setAvailableSeats(flight.getAvailableSeats() - seatReq.size());
//
//					return flightRepo.save(flight).thenMany(seatRepo.saveAll(seats))
//							.thenMany(passengerRepo.saveAll(booking.getPassengers())).then(bookingRepo.save(booking))
//							.thenReturn(ResponseEntity.status(HttpStatus.CREATED).<Void>build());
//				})).switchIfEmpty(Mono.just(ResponseEntity.badRequest().<Void>build()));
//	}
}

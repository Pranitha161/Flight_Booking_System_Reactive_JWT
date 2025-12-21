package com.flightapp.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightapp.demo.entity.Booking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingRepository extends ReactiveMongoRepository<Booking, String>{
	
	Mono<Booking> findByPnr(String pnr);

	Flux<Booking> findByEmail(String email);

}

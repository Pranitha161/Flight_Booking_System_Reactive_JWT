package com.flightapp.demo.service;

import org.springframework.http.ResponseEntity;

import com.flightapp.demo.entity.AuthResponse;
import com.flightapp.demo.entity.User;

import reactor.core.publisher.Mono;

public interface UserService {

	

	Mono<ResponseEntity<AuthResponse>> getPassengerById(String passengerId);

	Mono<ResponseEntity<AuthResponse>> getPassengerByEmail(String email);

	Mono<ResponseEntity<User>> updateById(String id, AuthResponse passenger);

	Mono<ResponseEntity<String>> deleteById(String passengerId);

	Mono<ResponseEntity<String>> logout(String token);

	
}

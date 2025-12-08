package com.flightapp.demo.service;

import org.springframework.http.ResponseEntity;
import com.flightapp.demo.entity.LoginRequest;
import com.flightapp.demo.entity.User;
import reactor.core.publisher.Mono;

public interface UserService {

	Mono<ResponseEntity<String>> addUser(User user);

	Mono<ResponseEntity<String>> login(LoginRequest loginRequest);

	Mono<ResponseEntity<User>> getPassengerById(String passengerId);

	Mono<ResponseEntity<User>> getPassengerByEmail(String email);

	Mono<ResponseEntity<User>> updateById(String id, User passenger);

	Mono<ResponseEntity<String>> deleteById(String passengerId);

	Mono<ResponseEntity<String>> logout(String token);

	Mono<User> signup(User user);
}

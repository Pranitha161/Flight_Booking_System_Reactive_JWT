package com.flightapp.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.demo.entity.LoginRequest;
import com.flightapp.demo.entity.User;
import com.flightapp.demo.service.implementation.UserServiceImplementation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

	private final UserServiceImplementation userService;

	@PostMapping("/signup")
	public Mono<ResponseEntity<String>> signup(@Valid @RequestBody User user) {
		return userService.addUser(user);
	}

	@PostMapping("/login")
	public Mono<ResponseEntity<String>> login(@Valid @RequestBody LoginRequest loginRequest) {
		return userService.login(loginRequest);
	}

	@GetMapping("/get/{passengerId}")
	public Mono<ResponseEntity<User>> getPassengers(@RequestHeader("X-User-Id") String userId,
			@RequestHeader("X-Roles") String roles, @PathVariable String passengerId) {
		if (!userId.equals(passengerId) && !roles.contains("ROLE_ADMIN")) {
			return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
		}
		return userService.getPassengerById(passengerId);
	}

	@GetMapping("/get/email/{email}")
	public Mono<ResponseEntity<User>> getPassenger(@RequestHeader("X-Email") String userEmail,
			@RequestHeader("X-Roles") String roles, @PathVariable String email) {
		if (!userEmail.equals(email) && !roles.contains("ROLE_ADMIN")) {
			return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
		}
		return userService.getPassengerByEmail(email);
	}

	@PostMapping("/update/{passengerId}")
	public Mono<ResponseEntity<User>> updatePassenger(@RequestHeader("X-User-Id") String userId,
			@RequestHeader("X-Roles") String roles, @PathVariable String passengerId, @RequestBody @Valid User p) {
		if (!userId.equals(passengerId) && !roles.contains("ROLE_ADMIN")) {
			return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
		}
		return userService.updateById(passengerId, p);
	}

	@DeleteMapping("/delete/{passengerId}")
	public Mono<ResponseEntity<String>> deletePassenger(@RequestHeader("X-Roles") String roles,
			@PathVariable String passengerId) {
		if (!roles.contains("ROLE_ADMIN")) {
			return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
		}
		return userService.deleteById(passengerId);
	}

	@PostMapping("/logout")
	public Mono<ResponseEntity<String>> logout(@RequestHeader("Authorization") String token) {
		return userService.logout(token);
	}
}

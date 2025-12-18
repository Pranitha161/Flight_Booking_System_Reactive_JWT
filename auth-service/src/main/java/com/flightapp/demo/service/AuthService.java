package com.flightapp.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.ApiResponse;
import com.flightapp.demo.entity.LoginRequest;
import com.flightapp.demo.entity.User;
import com.flightapp.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public Mono<ResponseEntity<ApiResponse>> signup(User user) {

		return userRepo.findByEmail(user.getEmail()).flatMap(existing -> {
			return Mono.just(
					ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "Email already exists")));
		}).switchIfEmpty(userRepo.findByUsername(user.getUsername()).flatMap(existing -> {
			return Mono.just(
					ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "Username already exists")));
		}).switchIfEmpty(Mono.defer(() -> {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			return userRepo.save(user).map(saved -> {
				return ResponseEntity.status(HttpStatus.CREATED)
						.body(new ApiResponse(true, "User created with id: " + saved.getId()));
			});
		})));
	}

	public Mono<ResponseEntity<ApiResponse>> login(LoginRequest request) {
		return userRepo.findByUsername(request.getUsername()).flatMap(existing -> {
			if (!passwordEncoder.matches(request.getPassword(), existing.getPassword())) {
				return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new ApiResponse(false, "Invalid credentials")));
			}

			String token = jwtService.generateToken(existing);
			return Mono.just(ResponseEntity.ok(new ApiResponse(true, token)));
		}).switchIfEmpty(
				Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found"))));
	}

}

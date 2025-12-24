package com.flightapp.demo.service.implementation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.AuthResponse;
import com.flightapp.demo.entity.User;
import com.flightapp.demo.repository.UserRepository;
import com.flightapp.demo.service.UserService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;

	private AuthResponse toResponse(User user) {
		return new AuthResponse(user.getId(), user.getEmail(), user.getUsername(), user.getRole());
	}

	@Override
	public Mono<ResponseEntity<AuthResponse>> getPassengerById(String passengerId) {
		return userRepo.findById(passengerId).map(user -> ResponseEntity.ok(toResponse(user)))
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@Override
	public Mono<ResponseEntity<AuthResponse>> getPassengerByEmail(String email) {
		System.out.println(email);
		return userRepo.findByEmail(email).map(user -> ResponseEntity.ok(toResponse(user)))
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

//	@Override
//	public Mono<ResponseEntity<User>> updateById(String id, AuthResponse passenger) {
//		return userRepo.findById(id).flatMap(existing -> {
//			existing.setRole(passenger.getRole());
//			existing.setEmail(passenger.getEmail());
//			existing.setUsername(passenger.getUsername());
//			return userRepo.save(existing).map(ResponseEntity::ok);
//		}).switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
//	}

	@Override
	public Mono<ResponseEntity<User>> updateById(String id, AuthResponse passenger) {

		return userRepo.findById(id).flatMap(existing ->

		userRepo.findByUsername(passenger.getUsername()).filter(other -> !other.getId().equals(id))
				.flatMap(conflict -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).<User>build())).switchIfEmpty(

						Mono.defer(() -> {
							existing.setRole(passenger.getRole());
							existing.setEmail(passenger.getEmail());
							existing.setUsername(passenger.getUsername());

							return userRepo.save(existing).map(ResponseEntity::ok);
						})))
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@Override
	public Mono<ResponseEntity<String>> deleteById(String passengerId) {
		return userRepo.findById(passengerId).flatMap(
				existing -> userRepo.delete(existing).then(Mono.just(ResponseEntity.ok("Deleted successfully"))))
				.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")));
	}

	@Override
	public Mono<ResponseEntity<String>> logout(String token) {
		token = token.replace("Bearer ", "");
		ResponseCookie cookie = ResponseCookie.from("jwt", "").httpOnly(true).secure(true).path("/").maxAge(0).build();
		return Mono.just(
				ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("Logged out successfully"));
	}

	public Mono<List<User>> getUsersByIds(List<String> ids) {
		return userRepo.findAllById(ids).collectList();
	}

	@Override
	public Mono<ResponseEntity<String>> changePassword(String userName, String oldPassword, String newPassword) {

		
		return userRepo.findByUsername(userName).flatMap(user->{
			System.out.println("Raw oldPassword: " + oldPassword);
			System.out.println("Stored hash: " + user.getPassword());
			System.out.println("Matches? " + passwordEncoder.matches(oldPassword, user.getPassword()));
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			System.out.println(encoder.matches("Pranitha@123", "$2a$10$dw1kM8J52.cp9sL071uNhuEkXOP1IKnKatEaP7Yq6rk0P1mH0Wnhq"));
			System.out.println(encoder.matches("secret123", "$2a$10$dw1kM8J52.cp9sL071uNhuEkXOP1IKnKatEaP7Yq6rk0P1mH0Wnhq"));

			if(!passwordEncoder.matches(oldPassword,user.getPassword())) {
				System.out.println("hello");
				return Mono.just(ResponseEntity.badRequest().body("{\"message\":\"Invalid current password\"}"));
			}
			user.setPassword(passwordEncoder.encode(newPassword));
			user.setPasswordLastChanged(LocalDateTime.now());
			return userRepo.save(user).thenReturn(ResponseEntity.ok("{\"message\":\"Password changed successfully\"}"));
		}).switchIfEmpty(Mono.just(ResponseEntity.badRequest().body("{\"message\":\"User not found\"}")));
	}

}

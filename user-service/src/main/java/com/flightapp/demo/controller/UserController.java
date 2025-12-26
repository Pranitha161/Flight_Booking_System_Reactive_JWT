package com.flightapp.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.demo.entity.AuthResponse;
import com.flightapp.demo.entity.ChangePasswordRequest;
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

	@GetMapping("/ping")
	public String ping() {
		System.out.println("Ping endpoint hit");
		return "pong";
	}

	@PreAuthorize("#passengerId == authentication.name or hasRole('ADMIN')")
	@GetMapping("/get/{passengerId}")
	public Mono<ResponseEntity<AuthResponse>> getPassengers(@PathVariable String passengerId) {

		return userService.getPassengerById(passengerId);
	}

	@PreAuthorize("hasRole('USER') or hasRole('ADMIN') or #email == authentication.token.claims['email']")
	@GetMapping("/get/email/{email:.+}")
	public Mono<ResponseEntity<AuthResponse>> getPassenger(@PathVariable String email) {

		return userService.getPassengerByEmail(email);
	}

	@PutMapping("/me")
	public Mono<ResponseEntity<AuthResponse>> updateLoggedInUser(JwtAuthenticationToken auth,
			@RequestBody AuthResponse req) {

		String userId = auth.getName();

		return userService.updateById(userId, req).flatMap(responseEntity -> {

			User updatedUser = responseEntity.getBody();

			if (updatedUser == null) {
				return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
			}

			AuthResponse authResponse = new AuthResponse(updatedUser.getId(), updatedUser.getEmail(),
					updatedUser.getUsername(), updatedUser.getRole());

			return Mono.just(ResponseEntity.ok(authResponse));
		});
	}

	@PreAuthorize("#passengerId == authentication.name or hasRole('ADMIN')")
	@PostMapping("/update/{passengerId}")
	public Mono<ResponseEntity<User>> updatePassenger(@PathVariable String passengerId,
			@RequestBody @Valid AuthResponse p) {

		return userService.updateById(passengerId, p);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/me")
	public Mono<ResponseEntity<AuthResponse>> getLoggedInUser(JwtAuthenticationToken authentication) {
		System.out.println("heyy");
		String email = authentication.getTokenAttributes().get("email").toString();
		System.out.println(email);
		return userService.getPassengerByEmail(email);

	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/delete/{passengerId}")
	public Mono<ResponseEntity<String>> deletePassenger(@PathVariable String passengerId) {

		return userService.deleteById(passengerId);
	}

	@GetMapping("/internal/email/{email:.+}")
	public Mono<ResponseEntity<AuthResponse>> internalGetPassenger(@PathVariable String email) {
		return userService.getPassengerByEmail(email);
	}

	@PostMapping("/internal/users/byIds")
	public Mono<ResponseEntity<List<User>>> internalGetUsersByIds(@RequestBody List<String> ids) {
		return userService.getUsersByIds(ids).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.ok(List.of()));
	}

	@PostMapping("/change-password")
	public Mono<ResponseEntity<String>> changePassword(@RequestBody ChangePasswordRequest request) {

		return userService.changePassword(request.getUserName(), request.getOldPassword(), request.getNewPassword());
	}

	@PostMapping("/request-reset")
	public Mono<ResponseEntity<String>> requestReset(@RequestBody Map<String, String> body) {
		return userService.requestPasswordReset(body.get("email"));
	}

	@PostMapping("/reset-password")
	public Mono<ResponseEntity<String>> resetPassword(@RequestBody Map<String, String> body) {
		return userService.resetPassword(body.get("token"), body.get("newPassword"));
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/users/byIds")
	public Mono<ResponseEntity<List<User>>> getUsersByIds(

			@RequestBody List<String> ids) {

		return userService.getUsersByIds(ids).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.ok(List.of()));
	}

}

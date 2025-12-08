package com.flightapp.demo.service.implementation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.LoginRequest;
import com.flightapp.demo.entity.User;
import com.flightapp.demo.repository.UserRepository;
import com.flightapp.demo.service.UserService;
import com.flightapp.demo.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final PasswordEncoder passwordEncoder;  
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<User> signup(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    @Override
    public Mono<ResponseEntity<String>> addUser(User user) {
        return userRepo.findByUsername(user.getUsername())
                .flatMap(existing -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists")))
                .switchIfEmpty(userRepo.findByEmail(user.getEmail())
                        .flatMap(existing -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists")))
                        .switchIfEmpty(Mono.defer(() -> {
                            user.setPassword(passwordEncoder.encode(user.getPassword()));
                            return userRepo.save(user)
                                    .map(saved -> ResponseEntity.status(HttpStatus.CREATED)
                                            .body("User created successfully with id: " + saved.getId()));
                        })));
    }

    @Override
    public Mono<ResponseEntity<String>> login(LoginRequest loginRequest) {
        return userRepo.findByUsername(loginRequest.getUsername()).flatMap(existing -> {
            if (passwordEncoder.matches(loginRequest.getPassword(), existing.getPassword())) {
               
                String token = jwtUtil.generateToken(existing.getId(), existing.getEmail(), existing.getRole());
                ResponseCookie cookie = ResponseCookie.from("jwt", token)
                        .httpOnly(true).secure(true).path("/")
                        .maxAge(3600).build();
                return Mono.just(ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body("Login successful\n" + token));
            } else {
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials"));
            }
        }).switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")));
    }

    @Override
    public Mono<ResponseEntity<User>> getPassengerById(String passengerId) {
        return userRepo.findById(passengerId).map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<User>> getPassengerByEmail(String email) {
        return userRepo.findByEmail(email).map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<User>> updateById(String id, User passenger) {
        return userRepo.findById(id).flatMap(existing -> {
            existing.setAge(passenger.getAge());
            existing.setEmail(passenger.getEmail());
            existing.setUsername(passenger.getUsername());
            return userRepo.save(existing).map(ResponseEntity::ok);
        }).switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    @Override
    public Mono<ResponseEntity<String>> deleteById(String passengerId) {
        return userRepo.findById(passengerId)
                .flatMap(existing -> userRepo.delete(existing)
                        .then(Mono.just(ResponseEntity.ok("Deleted successfully"))))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")));
    }

    @Override
    public Mono<ResponseEntity<String>> logout(String token) {
        token = token.replace("Bearer ", "");
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true).secure(true).path("/")
                .maxAge(0).build();
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out successfully"));
    }
}

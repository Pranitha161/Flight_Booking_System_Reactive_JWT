package com.flightapp.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.demo.entity.ApiResponse;
import com.flightapp.demo.entity.LoginRequest;
import com.flightapp.demo.entity.User;
import com.flightapp.demo.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public Mono<ResponseEntity<ApiResponse>> signup(@RequestBody @Valid User user) {
    	System.out.println("im here");
        return authService.signup(user);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse>> login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}


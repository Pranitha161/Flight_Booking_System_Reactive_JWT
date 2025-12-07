package com.flightapp.demo.repository;

import com.flightapp.demo.entity.User;

import reactor.core.publisher.Mono;

public interface UserRepository {

	Mono<User> findByUsername(String username);

	Mono<User> findByEmail(String email);
}

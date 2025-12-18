package com.flightapp.demo.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.flightapp.demo.entity.AuthResponse;
import com.flightapp.demo.entity.User;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/auth/internal/email/{email}")
    ResponseEntity<AuthResponse> internalGetPassenger(@PathVariable String email);

    @PostMapping("/auth/internal/users/byIds")
    List<User> internalGetUsersByIds(@RequestBody List<String> ids);
}



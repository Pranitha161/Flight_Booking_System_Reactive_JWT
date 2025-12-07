package com.flightapp.demo.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.flightapp.demo.config.FeignConfig;
import com.flightapp.demo.entity.User;

@FeignClient(name = "user-service-auth", configuration = FeignConfig.class)
public interface UserClient {
	@GetMapping("/auth/get/{passengerId}")
	ResponseEntity<User> getPassengers(@PathVariable String passengerId);

	@PostMapping("/auth/users/byIds")
	List<User> getUsersByIds(@RequestBody List<String> ids);

	@GetMapping("/auth/get/email/{email}")
	ResponseEntity<User> getPassenger(@PathVariable String email);
}

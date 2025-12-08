package com.flightapp.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BookingServiceAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingServiceAuthApplication.class, args);
	}

}

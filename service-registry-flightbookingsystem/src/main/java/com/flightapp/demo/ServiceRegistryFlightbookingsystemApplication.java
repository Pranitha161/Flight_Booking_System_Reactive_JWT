package com.flightapp.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryFlightbookingsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRegistryFlightbookingsystemApplication.class, args);
	}

}

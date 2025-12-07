package com.flightapp.demo.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Flight {
	private String id;
	private String airlineId;
	private String flightNumber;
	private String fromPlace;
	private String toPlace;
	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;
	private int availableSeats;
	private Price price;
}

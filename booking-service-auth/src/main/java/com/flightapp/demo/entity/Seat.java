package com.flightapp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Seat {
	private String id;
	private String seatNumber;
	private boolean available;
	private String flightId;
}

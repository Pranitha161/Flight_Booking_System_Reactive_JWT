package com.flightapp.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Price {
	private float oneWay;
	private float roundTrip;
}

package com.flightapp.demo.entity;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.flightapp.demo.enums.BOOKING_STATUS;
import com.flightapp.demo.enums.BOOKING_STATUS;
import com.flightapp.demo.enums.MEAL_PREFERENCE;
import com.flightapp.demo.enums.TRIP_TYPE;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {
	@Id
	private String id;
	private String pnr;
	@Email(message = "Invalid email format")
	@NotBlank(message = "Email cannot be blank")
	private String email;
	@Positive(message = "Seat count must be positive")
	@Max(value = 10, message = "cannot book more than 10 tickets")
	private int seatCount;
	private BOOKING_STATUS status;
	@NotNull(message = "Trip type is required")
	private TRIP_TYPE tripType;
	@NotNull(message = "Meal preference is required")
	private MEAL_PREFERENCE mealPrefernce;
	private String flightId;
	@NotNull(message = "Passengers list cannot be null")
	@Size(min = 1, message = "At least one passenger must be included")
	private List<String> userIds;
	@NotNull(message = "Seat numbers list cannot be null")
	@Size(min = 1, message = "At least one seat number must be provided")
	private List<String> seatNumbers;
	private float totalAmount;
}

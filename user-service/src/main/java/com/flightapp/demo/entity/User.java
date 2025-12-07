package com.flightapp.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Document(collection = "users")
@Data
public class User {
	@Id
	private String id;
	private String email;
	private String username;
	private String password;
	private String role;
	@Positive(message = "Age must be positive")
	@Max(value = 90, message = "Age cannot exceed 90")
	private int age;
	@NotBlank(message = "Gender is required of type (Male/Female)")
	private String gender;

}

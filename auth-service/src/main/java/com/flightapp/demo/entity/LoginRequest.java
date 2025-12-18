package com.flightapp.demo.entity;

import lombok.Data;

@Data
public class LoginRequest {
	
	private String username;

	private String password;
}

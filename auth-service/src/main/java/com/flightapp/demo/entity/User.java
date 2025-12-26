package com.flightapp.demo.entity;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private int age;
    private String gender;
    private LocalDateTime passwordLastChanged;
    private String resetToken;
    private Instant resetTokenExpiry;

}

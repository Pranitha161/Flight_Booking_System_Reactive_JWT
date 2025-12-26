package com.flightapp.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired 
    private JavaMailSender mailSender;

    public void sendResetLink(String to, String token) {
        String resetUrl = "http://localhost:4200/reset-password?token=" + token;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Password Reset");
        msg.setText("Click here to reset: " + resetUrl);
        mailSender.send(msg);
    }
}

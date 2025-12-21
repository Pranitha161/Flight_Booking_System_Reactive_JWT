package com.flightapp.demo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.flightapp.demo.entity.Booking;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBookingCreated(Booking booking) {
        sendMail(booking.getEmail(),
                 "Booking Confirmed: " + booking.getPnr(),
                 "Your booking has been confirmed.\nPNR: " + booking.getPnr());
    }

    public void sendBookingDeleted(Booking booking) {
        sendMail(booking.getEmail(),
                 "Booking Cancelled: " + booking.getPnr(),
                 "Your booking has been cancelled.\nPNR: " + booking.getPnr());
    }

    private void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            System.out.println("Mail sent to " + to);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


package edu.fdzcxy.bloggg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final JavaMailSender mailSender;
    public NotificationService(JavaMailSender mailSender) { this.mailSender = mailSender; }

    @Value("${spring.mail.from:${spring.mail.username}}")
    private String fromAddress;

    public void sendUpdateEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}


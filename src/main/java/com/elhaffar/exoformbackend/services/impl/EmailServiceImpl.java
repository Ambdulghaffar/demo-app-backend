package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe StockFlow");
        message.setText(
                "Bonjour " + username + ",\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe StockFlow.\n\n" +
                "Cliquez sur le lien ci-dessous pour choisir un nouveau mot de passe (valable 1 heure) :\n\n" +
                resetLink + "\n\n" +
                "Si vous n'avez pas effectué cette demande, ignorez simplement cet email.\n\n" +
                "L'équipe StockFlow"
        );
        mailSender.send(message);
    }
}

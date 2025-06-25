package com.colorrun.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    
    // Configuration pour Gmail (à adapter selon votre fournisseur)
    private static final String FROM_EMAIL = "colorrun.app@gmail.com"; // Changez par votre email
    private static final String PASSWORD = "votre_mot_de_passe_app"; // Changez par votre mot de passe d'application
    
    // Configuration pour MailHog (SMTP local)
    private static final String MAILHOG_HOST = "localhost";
    private static final int MAILHOG_PORT = 1025;
    
    public void sendEmail(String toEmail, String subject, String message) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM_EMAIL));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        msg.setSubject(subject);
        msg.setText(message);

        Transport.send(msg);
    }
    
    // Version pour MailHog (envoi vers le serveur SMTP local)
    public void sendEmailTest(String toEmail, String subject, String message) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", MAILHOG_HOST);
            props.put("mail.smtp.port", MAILHOG_PORT);
            props.put("mail.smtp.auth", "false");
            props.put("mail.smtp.starttls.enable", "false");

            Session session = Session.getInstance(props, null);

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("noreply@colorrun.com"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject(subject);
            msg.setText(message);

            Transport.send(msg);
            
            System.out.println("=== EMAIL ENVOYÉ VERS MAILHOG ===");
            System.out.println("À: " + toEmail);
            System.out.println("Sujet: " + subject);
            System.out.println("Message: " + message);
            System.out.println("Consultez http://localhost:8025 pour voir l'email");
            System.out.println("=================================");
            
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi vers MailHog: " + e.getMessage());
            System.err.println("Assurez-vous que MailHog est démarré sur localhost:1025");
            // Fallback vers l'affichage console si MailHog n'est pas disponible
            System.out.println("=== EMAIL SIMULÉ (MailHog non disponible) ===");
            System.out.println("À: " + toEmail);
            System.out.println("Sujet: " + subject);
            System.out.println("Message: " + message);
            System.out.println("============================================");
        }
    }
}
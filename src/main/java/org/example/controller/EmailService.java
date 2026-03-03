package org.example.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private String smtpUser="leila.bellakhdhar@aiesec.net";     // your gmail address
    private String appPassword="wcdn mijt ntqr cnob";  // gmail app password (NOT normal password)

    public EmailService(String smtpUser, String appPassword) {
        this.smtpUser = smtpUser;
        this.appPassword = appPassword;
    }

    public void send(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, appPassword);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(smtpUser, "FarmIADesk"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setText(body);

            Transport.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public static String buildStockZeroEmail(String serviceName, int serviceId) {
        return """
Bonjour Leila,

Alerte stock : le stock d’un service vient d’atteindre 0.

Détails :
- Service : %s
- ID : %d
- Statut : Stock épuisé (0)

Action recommandée :
Merci de procéder au réapprovisionnement dès que possible afin d’éviter toute rupture de vente.

Cordialement,
FarmIADesk (système)
""".formatted(serviceName, serviceId);
    }
}
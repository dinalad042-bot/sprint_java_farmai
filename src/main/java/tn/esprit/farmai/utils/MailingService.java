package tn.esprit.farmai.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class MailingService {
    private static final String username = "aymen.bensalem2002@gmail.com"; // Recommended sender
    private static final String password = "jjnwqquxsqcpxbog"; // Use the App Password provided
    private static final String host = "smtp.gmail.com";
    private static final String port = "587";

    public static void sendMail(String to, String subject, String body) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}

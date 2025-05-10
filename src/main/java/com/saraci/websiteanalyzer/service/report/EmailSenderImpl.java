package com.saraci.websiteanalyzer.service.report;

import com.saraci.websiteanalyzer.config.EmailConfig;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Implementierung des E-Mail-Senders mit JavaMail.
 */
public class EmailSenderImpl implements EmailSender {
    private static final Logger logger = Logger.getLogger(EmailSenderImpl.class.getName());

    private final EmailConfig emailConfig;

    /**
     * Konstruktor mit Dependency Injection für die E-Mail-Konfiguration.
     */
    public EmailSenderImpl(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    @Override
    public void sendEmail(String to, String subject, String text, String attachmentPath) throws Exception {
        logger.info("Sende E-Mail an: " + to + ", Betreff: " + subject);

        // Validiere E-Mail-Adresse
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("E-Mail-Adresse darf nicht leer sein");
        }

        try {
            // E-Mail-Eigenschaften konfigurieren
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", emailConfig.getHost());
            props.put("mail.smtp.port", emailConfig.getPort());

            // Session erstellen mit Authentifizierung
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailConfig.getUsername(), emailConfig.getPassword());
                }
            });

            // E-Mail-Nachricht erstellen
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfig.getUsername()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Multipart-Nachricht erstellen (Text + Anhang)
            Multipart multipart = new MimeMultipart();

            // Text-Teil hinzufügen
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(createEmailText(text));
            multipart.addBodyPart(messageBodyPart);

            // HTML-Teil hinzufügen
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(createEmailHtml(text), "text/html");
            multipart.addBodyPart(messageBodyPart);

            // Anhang hinzufügen, falls vorhanden
            if (attachmentPath != null && !attachmentPath.isEmpty()) {
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachmentPath);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(attachmentPath.substring(attachmentPath.lastIndexOf('/') + 1));
                multipart.addBodyPart(messageBodyPart);
            }

            // Setze die Teile in die Nachricht
            message.setContent(multipart);

            // E-Mail senden
            Transport.send(message);

            logger.info("E-Mail erfolgreich gesendet");
        } catch (Exception e) {
            logger.severe("Fehler beim Senden der E-Mail: " + e.getMessage());
            throw new Exception("Fehler beim Senden der E-Mail: " + e.getMessage(), e);
        }
    }

    /**
     * Erstellt den Textinhalt der E-Mail.
     */
    private String createEmailText(String text) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hallo,\n\n");
        sb.append(text).append("\n\n");
        sb.append("Mit freundlichen Grüßen,\n");
        sb.append("Website Analyzer - Saraci Design\n\n");
        sb.append("--\n");
        sb.append("Diese E-Mail wurde automatisch generiert. Bitte antworten Sie nicht auf diese E-Mail.");

        return sb.toString();
    }

    /**
     * Erstellt den HTML-Inhalt der E-Mail.
     */
    private String createEmailHtml(String text) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; line-height: 1.6; }");
        sb.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        sb.append(".header { background-color: #000000; color: #ffffff; padding: 20px; text-align: center; }");
        sb.append(".header h1 { margin: 0; font-size: 24px; }");
        sb.append(".content { padding: 20px; background-color: #f5f5f5; }");
        sb.append(".footer { margin-top: 20px; font-size: 12px; color: #999999; text-align: center; }");
        sb.append(".accent { color: #e81818; }");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div class='container'>");
        sb.append("<div class='header'>");
        sb.append("<h1>Website <span class='accent'>Analyzer</span></h1>");
        sb.append("</div>");
        sb.append("<div class='content'>");
        sb.append("<p>Hallo,</p>");
        sb.append("<p>").append(text.replace("\n", "<br>")).append("</p>");
        sb.append("<p>Mit freundlichen Grüßen,<br>Website Analyzer - Saraci Design</p>");
        sb.append("</div>");
        sb.append("<div class='footer'>");
        sb.append("<p>Diese E-Mail wurde automatisch generiert. Bitte antworten Sie nicht auf diese E-Mail.</p>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }
}
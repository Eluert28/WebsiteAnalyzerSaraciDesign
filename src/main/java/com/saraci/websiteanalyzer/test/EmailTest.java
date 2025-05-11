package com.saraci.websiteanalyzer.test;

import com.saraci.websiteanalyzer.config.EmailConfig;
import com.saraci.websiteanalyzer.service.report.EmailSender;
import com.saraci.websiteanalyzer.service.report.EmailSenderImpl;

public class EmailTest {
    public static void main(String[] args) {
        try {
            EmailConfig config = new EmailConfig(
                    "smtp.gmail.com",
                    587,
                    "lukas.e.saraci@gmail.com",
                    "upmf ovqb cwez jeev" // Ihr App-Passwort
            );

            EmailSender sender = new EmailSenderImpl(config);
            sender.sendEmail(
                    "lukassaraci@gmail.com", // Empfänger
                    "Test E-Mail", // Betreff
                    "Dies ist eine Test-E-Mail vom Website Analyzer.", // Text
                    null // Kein Anhang
            );

            System.out.println("E-Mail erfolgreich gesendet!");
        } catch (Exception e) {
            System.err.println("Fehler beim Senden der E-Mail: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
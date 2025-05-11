package com.saraci.websiteanalyzer.test;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvTest {
    public static void main(String[] args) {
        // Laden der .env-Datei
        Dotenv dotenv = Dotenv.configure().load();

        // Werte ausgeben
        String username = dotenv.get("EMAIL_USERNAME");
        String password = dotenv.get("EMAIL_PASSWORD");
        String host = dotenv.get("EMAIL_HOST");
        String port = dotenv.get("EMAIL_PORT");

        // Ergebnisse anzeigen
        System.out.println("E-Mail-Konfiguration aus .env:");
        System.out.println("Username: " + username);
        System.out.println("Password Länge: " + (password != null ? password.length() : "null"));
        System.out.println("Password erste 2 Zeichen: " + (password != null && password.length() >= 2 ? password.substring(0, 2) : "null"));
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
    }
}
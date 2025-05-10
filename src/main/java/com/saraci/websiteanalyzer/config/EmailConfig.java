package com.saraci.websiteanalyzer.config;

/**
 * Konfigurationsklasse für E-Mail-Einstellungen.
 */
public class EmailConfig {
    private String host;
    private int port;
    private String username;
    private String password;

    /**
     * Standard-Konstruktor mit den Gmail-SMTP-Einstellungen.
     */
    public EmailConfig() {
        this.host = "smtp.gmail.com";
        this.port = 587;
        this.username = "your-email@gmail.com"; // Standardwert, der überschrieben werden sollte
        this.password = "your-app-password"; // Standardwert, der überschrieben werden sollte
    }

    /**
     * Konstruktor mit benutzerdefinierten Einstellungen.
     */
    public EmailConfig(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    // Getter und Setter
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
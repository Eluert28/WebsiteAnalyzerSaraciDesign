package com.saraci.websiteanalyzer.config;

import com.saraci.websiteanalyzer.repository.*;
import com.saraci.websiteanalyzer.repository.impl.*;
import com.saraci.websiteanalyzer.service.WebsiteAnalyzerService;
import com.saraci.websiteanalyzer.service.WebsiteAnalyzerServiceImpl;
import com.saraci.websiteanalyzer.service.analyzer.*;
import com.saraci.websiteanalyzer.service.report.EmailSender;
import com.saraci.websiteanalyzer.service.report.EmailSenderImpl;
import com.saraci.websiteanalyzer.service.report.PdfReportGenerator;
import com.saraci.websiteanalyzer.service.report.PdfReportGeneratorImpl;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zentrale Konfigurationsklasse für die Anwendung.
 * Diese Klasse dient als einfache Dependency Injection-Lösung.
 */
public class AppConfig {
    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    private static AppConfig instance;
    private static Dotenv dotenv;

    // Repositories
    private final WebsiteRepository websiteRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final ScheduleRepository scheduleRepository;

    // Services
    private final SeoAnalyzer seoAnalyzer;
    private final PerformanceAnalyzer performanceAnalyzer;
    private final SecurityAnalyzer securityAnalyzer;
    private final ContentAnalyzer contentAnalyzer;
    private final PdfReportGenerator reportGenerator;
    private final EmailSender emailSender;
    private final WebsiteAnalyzerService websiteAnalyzerService;

    // Umgebungsvariablen initialisieren
    static {
        try {
            // Überprüfen, ob .env-Datei existiert
            File envFile = new File(".env");
            if (envFile.exists()) {
                logger.info(".env-Datei gefunden am Pfad: " + envFile.getAbsolutePath());
            } else {
                logger.warning(".env-Datei wurde nicht gefunden. Suche an: " + new File(".").getAbsolutePath());
            }

            // Dotenv laden, ignoreIfMissing verhindert Absturz bei fehlender .env-Datei
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // Überprüfen, ob dotenv erfolgreich geladen wurde
            if (dotenv != null) {
                // Teste, ob eine Umgebungsvariable gelesen werden kann
                String testVar = dotenv.get("EMAIL_HOST");
                if (testVar != null) {
                    logger.info("Umgebungsvariablen aus .env-Datei wurden erfolgreich geladen.");
                } else {
                    logger.warning("Umgebungsvariablen konnten nicht aus der .env-Datei gelesen werden oder sind nicht vorhanden.");
                }
            } else {
                logger.warning("dotenv konnte nicht initialisiert werden.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler beim Laden der Umgebungsvariablen", e);
            // Dotenv auf null setzen, damit Standard-Werte verwendet werden
            dotenv = null;
        }
    }

    private AppConfig() {
        try {
            // Initialisiere die Datenbank
            DatabaseConfig.initialize();

            // Initialisiere Repositories
            this.websiteRepository = new WebsiteRepositoryImpl();
            this.analysisResultRepository = new AnalysisResultRepositoryImpl();
            this.scheduleRepository = new ScheduleRepositoryImpl();

            // E-Mail-Konfiguration aus Umgebungsvariablen laden
            String emailHost = getEnv("EMAIL_HOST", "smtp.gmail.com");
            int emailPort = Integer.parseInt(getEnv("EMAIL_PORT", "587"));
            String emailUsername = getEnv("EMAIL_USERNAME", "your-email@gmail.com");
            String emailPassword = getEnv("EMAIL_PASSWORD", "your-app-password");

            // Loggen der E-Mail-Konfiguration (Passwort maskieren)
            String maskedPassword = emailPassword.length() > 4
                    ? emailPassword.substring(0, 2) + "..." + emailPassword.substring(emailPassword.length() - 2)
                    : "***";

            logger.info("E-Mail-Konfiguration: " +
                    "Host=" + emailHost + ", " +
                    "Port=" + emailPort + ", " +
                    "Username=" + emailUsername + ", " +
                    "Password=" + maskedPassword);

            EmailConfig emailConfig = new EmailConfig(emailHost, emailPort, emailUsername, emailPassword);

            // Komponenten initialisieren
            this.seoAnalyzer = new SeoAnalyzerImpl();
            this.performanceAnalyzer = new PerformanceAnalyzerImpl();
            this.securityAnalyzer = new SecurityAnalyzerImpl();
            this.contentAnalyzer = new ContentAnalyzerImpl();
            this.reportGenerator = new PdfReportGeneratorImpl();
            this.emailSender = new EmailSenderImpl(emailConfig);

            // Hauptservice erstellen
            this.websiteAnalyzerService = new WebsiteAnalyzerServiceImpl(
                    seoAnalyzer,
                    performanceAnalyzer,
                    securityAnalyzer,
                    contentAnalyzer,
                    reportGenerator,
                    emailSender
            );

            logger.info("AppConfig wurde erfolgreich initialisiert");
        } catch (SQLException e) {
            logger.severe("Fehler bei der Initialisierung der Datenbank: " + e.getMessage());
            throw new RuntimeException("Fehler bei der Initialisierung der Datenbank", e);
        }
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    // Getters für Repositories
    public WebsiteRepository getWebsiteRepository() {
        return websiteRepository;
    }

    public AnalysisResultRepository getAnalysisResultRepository() {
        return analysisResultRepository;
    }

    public ScheduleRepository getScheduleRepository() {
        return scheduleRepository;
    }

    // Getters für Services
    public WebsiteAnalyzerService getWebsiteAnalyzerService() {
        return websiteAnalyzerService;
    }

    public SeoAnalyzer getSeoAnalyzer() {
        return seoAnalyzer;
    }

    public PerformanceAnalyzer getPerformanceAnalyzer() {
        return performanceAnalyzer;
    }

    public SecurityAnalyzer getSecurityAnalyzer() {
        return securityAnalyzer;
    }

    public ContentAnalyzer getContentAnalyzer() {
        return contentAnalyzer;
    }

    public PdfReportGenerator getReportGenerator() {
        return reportGenerator;
    }

    public EmailSender getEmailSender() {
        return emailSender;
    }

    /**
     * Gibt den Wert einer Umgebungsvariable zurück oder den Standardwert, wenn die Variable nicht definiert ist.
     *
     * @param key Der Name der Umgebungsvariable
     * @param defaultValue Der Standardwert, wenn die Variable nicht definiert ist
     * @return Der Wert der Umgebungsvariable oder der Standardwert
     */
    public static String getEnv(String key, String defaultValue) {
        // Prüfen, ob dotenv erfolgreich initialisiert wurde
        if (dotenv != null) {
            try {
                String value = dotenv.get(key);
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            } catch (Exception e) {
                logger.warning("Fehler beim Lesen der Umgebungsvariable " + key + ": " + e.getMessage());
            }
        }

        // Alternativ versuchen, aus System-Umgebungsvariablen zu lesen
        try {
            String systemEnv = System.getenv(key);
            if (systemEnv != null && !systemEnv.isEmpty()) {
                return systemEnv;
            }
        } catch (Exception e) {
            logger.warning("Fehler beim Lesen der System-Umgebungsvariable " + key + ": " + e.getMessage());
        }

        // Wenn alles fehlschlägt, Standardwert zurückgeben
        logger.fine("Umgebungsvariable " + key + " nicht gefunden, verwende Standardwert: " + defaultValue);
        return defaultValue;
    }

    /**
     * Gibt den Wert einer Umgebungsvariable zurück oder null, wenn die Variable nicht definiert ist.
     *
     * @param key Der Name der Umgebungsvariable
     * @return Der Wert der Umgebungsvariable oder null
     */
    public static String getEnv(String key) {
        return getEnv(key, null);
    }

    /**
     * Überprüft, ob die Umgebungsvariable existiert.
     *
     * @param key Der Name der Umgebungsvariable
     * @return true, wenn die Variable existiert und nicht leer ist, sonst false
     */
    public static boolean hasEnv(String key) {
        return getEnv(key) != null;
    }
}
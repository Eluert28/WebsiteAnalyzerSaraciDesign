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

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Zentrale Konfigurationsklasse für die Anwendung.
 * Diese Klasse dient als einfache Dependency Injection-Lösung.
 */
public class AppConfig {
    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    private static AppConfig instance;

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

    private AppConfig() {
        try {
            // Initialisiere die Datenbank
            DatabaseConfig.initialize();

            // Initialisiere Repositories
            this.websiteRepository = new WebsiteRepositoryImpl();
            this.analysisResultRepository = new AnalysisResultRepositoryImpl();
            this.scheduleRepository = new ScheduleRepositoryImpl();

            // E-Mail-Konfiguration erstellen
            EmailConfig emailConfig = new EmailConfig(
                    "smtp.gmail.com",
                    587,
                    "your-email@gmail.com", // In der Produktion aus einer sicheren Quelle laden
                    "your-app-password"     // In der Produktion aus einer sicheren Quelle laden
            );

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

            logger.info("AppConfig initialisiert");
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
}
package com.saraci.websiteanalyzer;

import com.saraci.websiteanalyzer.config.AppConfig;
import com.saraci.websiteanalyzer.config.DatabaseConfig;
import com.saraci.websiteanalyzer.controller.ControllerRegistry;
import com.saraci.websiteanalyzer.service.scheduler.SchedulerService;
import com.saraci.websiteanalyzer.service.scheduler.SchedulerServiceImpl;
import com.saraci.websiteanalyzer.util.JsonUtil;
import spark.Spark;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import java.util.List;
import com.saraci.websiteanalyzer.model.AnalysisResult;
import com.saraci.websiteanalyzer.model.SeoResult;
import com.saraci.websiteanalyzer.model.Website;
import com.saraci.websiteanalyzer.repository.AnalysisResultRepository;
import com.saraci.websiteanalyzer.repository.WebsiteRepository;

import static spark.Spark.*;

/**
 * Hauptklasse für die Website-Analyzer-Anwendung.
 */
public class WebsiteAnalyzer {
    private static final Logger logger = Logger.getLogger(WebsiteAnalyzer.class.getName());
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            // Initialisiere die Konfiguration (die Datenbank wird dabei automatisch initialisiert)
            AppConfig appConfig = AppConfig.getInstance();

            // Starte den Server
            initializeServer();

            // Stelle sicher, dass notwendige Verzeichnisse existieren
            createDirectories();

            // Initialisiere den Scheduler-Service
            SchedulerService schedulerService = new SchedulerServiceImpl(
                    appConfig.getWebsiteAnalyzerService(),
                    appConfig.getWebsiteRepository(),
                    appConfig.getAnalysisResultRepository(),
                    appConfig.getScheduleRepository()
            );

            // Prüfe, ob Zeitpläne automatisch ausgeführt werden sollen
            boolean enableSchedulerAtStartup = Boolean.parseBoolean(AppConfig.getEnv("ENABLE_SCHEDULER_STARTUP", "false"));
            if (enableSchedulerAtStartup) {
                logger.info("Scheduler wird initialisiert und startet geplante Aufgaben...");
                schedulerService.initialize();
            } else {
                logger.info("Scheduler wird nur initialisiert, keine automatische Ausführung von Zeitplänen.");
                schedulerService.initializeWithoutExecution();
            }

            // Initialisiere die Controller und registriere die Routen
            ControllerRegistry controllerRegistry = new ControllerRegistry(appConfig, schedulerService);
            controllerRegistry.registerAllRoutes();

            logger.info("Website Analyzer gestartet: http://localhost:" + PORT);

            testLatestResults();

            // Füge einen Shutdown-Hook hinzu, um Ressourcen freizugeben
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Anwendung wird heruntergefahren...");
                schedulerService.shutdown();
                DatabaseConfig.closeConnection();
                Spark.stop();
            }));
        } catch (Exception e) {
            logger.severe("Fehler beim Starten der Anwendung: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void testLatestResults() {
        try {
            // Kurze Verzögerung, damit der Server starten kann
            Thread.sleep(2000);

            logger.info("=== STARTE TEST DER NEUEN FUNKTIONEN ===");

            WebsiteRepository websiteRepo = AppConfig.getInstance().getWebsiteRepository();
            AnalysisResultRepository repo = AppConfig.getInstance().getAnalysisResultRepository();

            // Alle Websites abrufen
            List<Website> websites = websiteRepo.findAll();
            if (websites.isEmpty()) {
                logger.info("Keine Websites in der Datenbank gefunden. Führen Sie zuerst eine Analyse durch.");
                return;
            }

            // Für jede Website die neuesten Ergebnisse prüfen
            for (Website website : websites) {
                logger.info("Prüfe Website: " + website.getUrl());

                List<AnalysisResult> results = repo.findByWebsiteId(website.getId());
                if (results.isEmpty()) {
                    logger.info("Keine Analyseergebnisse für diese Website gefunden.");
                    continue;
                }

                AnalysisResult latestResult = results.get(0);
                SeoResult seo = latestResult.getSeoResult();

                if (seo != null) {
                    logger.info("=== TEST: Canonical-Tag-Informationen ===");
                    logger.info("Absolute URL: " + seo.isCanonicalUrlAbsolute());
                    logger.info("Selbstreferenzierend: " + seo.isCanonicalUrlSelfReferential());

                    logger.info("=== TEST: Strukturierte Daten ===");
                    logger.info("Strukturierte Daten vorhanden: " + seo.isStructuredDataPresent());
                    logger.info("Anzahl strukturierter Daten: " + seo.getStructuredDataCount());
                    logger.info("JSON-LD: " + seo.getJsonLdCount());
                    logger.info("Microdata: " + seo.getMicrodataCount());
                    logger.info("RDFa: " + seo.getRdfaCount());
                    logger.info("Schema-Typen: " + seo.getSchemaTypes());
                } else {
                    logger.info("Keine SEO-Ergebnisse für diese Website gefunden.");
                }
            }

            logger.info("=== TEST ABGESCHLOSSEN ===");
        } catch (Exception e) {
            logger.severe("Fehler beim Testen der Ergebnisse: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Initialisiert den Spark-Webserver
     */
    private static void initializeServer() {
        // Setze den Port
        port(PORT);

        // Statische Dateien aus dem resources/public-Verzeichnis bereitstellen
        staticFiles.location("/public");

        // Aktiviere CORS für die API
        enableCORS();

        // Exception Handler für API-Fehler
        setupExceptionHandling();
    }

    /**
     * Erstellt die notwendigen Verzeichnisse für Daten und Berichte
     */
    private static void createDirectories() {
        // Erstelle Verzeichnisse für Daten und Berichte
        createDirectoryIfNotExists("data");
        createDirectoryIfNotExists("reports");
    }

    /**
     * Erstellt ein Verzeichnis, falls es nicht existiert
     */
    private static void createDirectoryIfNotExists(String directoryPath) {
        try {
            if (!Files.exists(Paths.get(directoryPath))) {
                Files.createDirectories(Paths.get(directoryPath));
                logger.info("Verzeichnis erstellt: " + directoryPath);
            }
        } catch (Exception e) {
            logger.warning("Konnte Verzeichnis nicht erstellen: " + directoryPath + ", " + e.getMessage());
        }
    }

    /**
     * Konfiguriert CORS für die API
     */
    private static void enableCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Content-Length, Accept, Origin");
            response.header("Access-Control-Allow-Credentials", "true");
        });
    }
    /**
     * Richtet die Exception-Handler ein
     */
    private static void setupExceptionHandling() {
        // Allgemeiner Exception-Handler
        exception(Exception.class, (exception, request, response) -> {
            response.status(500);
            response.type("application/json");
            response.body(JsonUtil.toJson(JsonUtil.error(exception.getMessage())));
            logger.severe("Unbehandelte Ausnahme: " + exception.getMessage());
            exception.printStackTrace();
        });
    }
}
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

import static spark.Spark.*;

/**
 * Hauptklasse für die Website-Analyzer-Anwendung.
 */
public class WebsiteAnalyzer {
    private static final Logger logger = Logger.getLogger(WebsiteAnalyzer.class.getName());
    private static final int DEFAULT_PORT = 8080;

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

            // Hole den verwendeten Port für die Logging-Meldung
            int port = port();
            logger.info("Website Analyzer gestartet: http://localhost:" + port);
            logger.info("  - Status-Endpunkt: http://localhost:" + port + "/status");
            logger.info("  - Hauptseite: http://localhost:" + port + "/");

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

    /**
     * Initialisiert den Spark-Webserver
     */
    private static void initializeServer() {
        // Setze den Port - lese den Port aus der Umgebungsvariablen oder verwende den Standardport
        int port = Integer.parseInt(AppConfig.getEnv("PORT", String.valueOf(DEFAULT_PORT)));
        port(port);

        logger.info("Server wird auf Port " + port + " gestartet");

        // Statische Dateien aus dem resources/public-Verzeichnis bereitstellen
        staticFiles.location("/public");

        // Aktiviere CORS für die API
        enableCORS();

        // Exception Handler für API-Fehler
        setupExceptionHandling();

        // Redirect von / zur index.html, falls die Standardseite nicht automatisch geladen wird
        get("/", (req, res) -> {
            res.redirect("/index.html");
            return null;
        });

        // Einfacher Endpoint zum Testen, ob der Server läuft
        get("/status", (req, res) -> {
            res.type("application/json");
            return JsonUtil.toJson(JsonUtil.success("Website Analyzer läuft!",
                    "version", "1.0",
                    "timestamp", System.currentTimeMillis(),
                    "status", "online"));
        });
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
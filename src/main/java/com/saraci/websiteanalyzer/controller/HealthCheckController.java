package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.config.DatabaseConfig;
import com.saraci.websiteanalyzer.repository.WebsiteRepository;
import com.saraci.websiteanalyzer.util.JsonUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static spark.Spark.*;

/**
 * Controller für den Health-Check-Endpunkt.
 * Dies ist nützlich für Cloud-Plattformen, um die Verfügbarkeit des Dienstes zu überprüfen.
 */
public class HealthCheckController implements Controller {
    private static final Logger logger = Logger.getLogger(HealthCheckController.class.getName());

    private final WebsiteRepository websiteRepository;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public HealthCheckController(WebsiteRepository websiteRepository) {
        this.websiteRepository = websiteRepository;
    }

    @Override
    public void registerRoutes() {
        // Route für den Health-Check
        get("/api/health", (req, res) -> {
            res.type("application/json");

            try {
                Map<String, Object> healthInfo = new HashMap<>();
                Map<String, Object> components = new HashMap<>();
                boolean allHealthy = true;

                // Überprüfe Datenbank-Verbindung
                Map<String, Object> dbStatus = checkDatabaseHealth();
                components.put("database", dbStatus);
                allHealthy = allHealthy && (boolean) dbStatus.get("healthy");

                // Überprüfe Dateisystem-Zugriff
                Map<String, Object> fsStatus = checkFileSystemHealth();
                components.put("filesystem", fsStatus);
                allHealthy = allHealthy && (boolean) fsStatus.get("healthy");

                // Überprüfe Repository-Zugriff
                Map<String, Object> repoStatus = checkRepositoryHealth();
                components.put("repository", repoStatus);
                allHealthy = allHealthy && (boolean) repoStatus.get("healthy");

                // Gesamtstatus
                healthInfo.put("status", allHealthy ? "UP" : "DOWN");
                healthInfo.put("components", components);
                healthInfo.put("version", "1.0");
                healthInfo.put("timestamp", System.currentTimeMillis());

                if (!allHealthy) {
                    res.status(503); // Service Unavailable
                }

                return JsonUtil.toJson(healthInfo);
            } catch (Exception e) {
                logger.severe("Fehler beim Health-Check: " + e.getMessage());

                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Health-Check: " + e.getMessage()));
            }
        });
    }

    /**
     * Überprüft die Gesundheit der Datenbank-Verbindung.
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> status = new HashMap<>();

        try {
            Connection conn = DatabaseConfig.getConnection();
            boolean isValid = conn != null && !conn.isClosed() && conn.isValid(5); // 5 Sekunden Timeout

            status.put("healthy", isValid);
            status.put("message", isValid ? "Datenbank-Verbindung erfolgreich" : "Datenbank-Verbindung fehlgeschlagen");

        } catch (Exception e) {
            status.put("healthy", false);
            status.put("message", "Datenbank-Fehler: " + e.getMessage());
            status.put("error", e.getClass().getSimpleName());
        }

        return status;
    }

    /**
     * Überprüft die Gesundheit des Dateisystems.
     */
    private Map<String, Object> checkFileSystemHealth() {
        Map<String, Object> status = new HashMap<>();

        try {
            boolean dataDirectoryExists = Files.exists(Paths.get("data"));
            boolean reportsDirectoryExists = Files.exists(Paths.get("reports"));
            boolean canWrite = Files.isWritable(Paths.get("data")) && Files.isWritable(Paths.get("reports"));

            boolean isHealthy = dataDirectoryExists && reportsDirectoryExists && canWrite;

            status.put("healthy", isHealthy);
            status.put("message", isHealthy ? "Dateisystem-Zugriff erfolgreich" : "Dateisystem-Probleme");
            status.put("details", Map.of(
                    "dataDirectory", dataDirectoryExists ? "exists" : "missing",
                    "reportsDirectory", reportsDirectoryExists ? "exists" : "missing",
                    "writable", canWrite ? "yes" : "no"
            ));

        } catch (Exception e) {
            status.put("healthy", false);
            status.put("message", "Dateisystem-Fehler: " + e.getMessage());
            status.put("error", e.getClass().getSimpleName());
        }

        return status;
    }

    /**
     * Überprüft die Gesundheit des Repository-Zugriffs.
     */
    private Map<String, Object> checkRepositoryHealth() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Versuche, alle Websites abzurufen (ohne sie tatsächlich zu laden)
            websiteRepository.findAll().size();

            status.put("healthy", true);
            status.put("message", "Repository-Zugriff erfolgreich");

        } catch (Exception e) {
            status.put("healthy", false);
            status.put("message", "Repository-Fehler: " + e.getMessage());
            status.put("error", e.getClass().getSimpleName());
        }

        return status;
    }
}
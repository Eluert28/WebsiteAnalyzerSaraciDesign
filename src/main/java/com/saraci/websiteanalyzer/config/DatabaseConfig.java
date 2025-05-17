package com.saraci.websiteanalyzer.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Konfigurationsklasse für die Datenbankverbindung.
 */
public class DatabaseConfig {
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String DB_FILE = "data/website_analyzer.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;
    private static Connection connection;

    /**
     * Initialisiert die Datenbankverbindung und erstellt die Tabellen.
     */
    public static void initialize() throws SQLException {
        try {
            // Stelle sicher, dass das Datenverzeichnis existiert
            Files.createDirectories(Paths.get("data"));

            // Lade den SQLite-JDBC-Treiber
            Class.forName("org.sqlite.JDBC");

            // Erstelle die Verbindung
            connection = DriverManager.getConnection(DB_URL);
            logger.info("Datenbankverbindung hergestellt: " + DB_URL);

            // Erstelle die Tabellen
            createTables();

            logger.info("Datenbank initialisiert");
        } catch (ClassNotFoundException e) {
            logger.severe("SQLite JDBC-Treiber nicht gefunden: " + e.getMessage());
            throw new SQLException("SQLite JDBC-Treiber nicht gefunden", e);
        } catch (Exception e) {
            logger.severe("Fehler bei der Datenbankinitialisierung: " + e.getMessage());
            throw new SQLException("Fehler bei der Datenbankinitialisierung", e);
        }
    }

    /**
     * Erstellt die Datenbanktabellen, falls sie noch nicht existieren.
     */
    private static void createTables() throws SQLException {
        // Websites-Tabelle
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS websites (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "url TEXT NOT NULL UNIQUE, " +
                        "first_analysis_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "last_analysis_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        );

        // Analyseergebnisse-Tabelle
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS analysis_results (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "website_id INTEGER NOT NULL, " +
                        "url TEXT NOT NULL, " + // Neue Spalte für die URL
                        "analysis_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "pdf_report_path TEXT, " +
                        "FOREIGN KEY (website_id) REFERENCES websites(id)" +
                        ")"
        );
        // Die SEO-Ergebnisse-Tabelle aktualisieren
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS seo_results (" +
                        "analysis_id INTEGER PRIMARY KEY, " +
                        "title TEXT, " +
                        "title_length INTEGER, " +
                        "description TEXT, " +
                        "description_length INTEGER, " +
                        "keywords TEXT, " +
                        "h1_count INTEGER, " +
                        "h2_count INTEGER, " +
                        "h3_count INTEGER, " +
                        "images_total INTEGER, " +
                        "images_with_alt INTEGER, " +
                        "images_without_alt INTEGER, " +
                        "alt_image_percentage REAL, " +
                        "internal_links INTEGER, " +
                        "external_links INTEGER, " +
                        "score INTEGER, " +
                        "canonical_url TEXT, " +
                        "canonical_url_absolute BOOLEAN, " +
                        "canonical_url_self_referential BOOLEAN, " +
                        "structured_data_present BOOLEAN, " +
                        "structured_data_count INTEGER, " +
                        "jsonld_count INTEGER, " +
                        "microdata_count INTEGER, " +
                        "rdfa_count INTEGER, " +
                        "schema_types TEXT, " +
                        "FOREIGN KEY (analysis_id) REFERENCES analysis_results(id)" +
                        ")"
        );

        // Performance-Ergebnisse-Tabelle
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS performance_results (" +
                        "analysis_id INTEGER PRIMARY KEY, " +
                        "lighthouse_score INTEGER, " +
                        "first_contentful_paint TEXT, " +
                        "largest_contentful_paint TEXT, " +
                        "time_to_interactive TEXT, " +
                        "total_blocking_time TEXT, " +
                        "cumulative_layout_shift TEXT, " +
                        "load_time INTEGER, " +
                        "FOREIGN KEY (analysis_id) REFERENCES analysis_results(id)" +
                        ")"
        );

        // Sicherheits-Ergebnisse-Tabelle
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS security_results (" +
                        "analysis_id INTEGER PRIMARY KEY, " +
                        "https_enabled BOOLEAN, " +
                        "security_headers_score INTEGER, " +
                        "cookies_security_score INTEGER, " +
                        "security_headers TEXT, " +
                        "FOREIGN KEY (analysis_id) REFERENCES analysis_results(id)" +
                        ")"
        );

        // Inhalts-Ergebnisse-Tabelle
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS content_results (" +
                        "analysis_id INTEGER PRIMARY KEY, " +
                        "word_count INTEGER, " +
                        "character_count INTEGER, " +
                        "average_word_length REAL, " +
                        "paragraph_count INTEGER, " +
                        "image_count INTEGER, " +
                        "video_count INTEGER, " +
                        "list_count INTEGER, " +
                        "table_count INTEGER, " +
                        "FOREIGN KEY (analysis_id) REFERENCES analysis_results(id)" +
                        ")"
        );

        // Zeitplan-Tabelle
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS schedules (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "website_id INTEGER NOT NULL, " +
                        "cron_expression TEXT NOT NULL, " +
                        "recipients TEXT NOT NULL, " +
                        "report_type TEXT NOT NULL, " +
                        "is_active BOOLEAN DEFAULT 1, " +
                        "last_run TIMESTAMP, " +
                        "next_run TIMESTAMP, " +
                        "FOREIGN KEY (website_id) REFERENCES websites(id)" +
                        ")"
        );

        logger.info("Tabellen erfolgreich erstellt/überprüft");
    }

    /**
     * Gibt die Datenbankverbindung zurück.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    /**
     * Schließt die Datenbankverbindung.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Datenbankverbindung geschlossen");
            }
        } catch (SQLException e) {
            logger.warning("Fehler beim Schließen der Datenbankverbindung: " + e.getMessage());
        }
    }
}
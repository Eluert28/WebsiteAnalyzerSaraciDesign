package com.saraci.websiteanalyzer.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

            // Führe Migrations aus
            runMigrations();

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
                        "url TEXT NOT NULL, " +
                        "analysis_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "pdf_report_path TEXT, " +
                        "FOREIGN KEY (website_id) REFERENCES websites(id)" +
                        ")"
        );

        // SEO-Ergebnisse-Tabelle
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

        // Leads-Tabelle (vollständig neu erstellt)
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS leads (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "email TEXT NOT NULL UNIQUE, " +
                        "company TEXT, " +
                        "website TEXT, " +
                        "phone TEXT, " +
                        "status TEXT DEFAULT 'NEW', " +
                        "source TEXT DEFAULT 'MANUAL', " +
                        "notes TEXT, " +
                        "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "is_deleted BOOLEAN DEFAULT 0" +
                        ")"
        );

        // E-Mail-Templates-Tabelle
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS email_templates (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "subject TEXT NOT NULL, " +
                        "body TEXT NOT NULL, " +
                        "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        );

        logger.info("Tabellen erfolgreich erstellt/überprüft");
    }

    /**
     * Führt Datenbank-Migrations aus, um bestehende Tabellen zu aktualisieren.
     */
    private static void runMigrations() throws SQLException {
        logger.info("Starte Datenbank-Migrations...");

        try (Statement stmt = connection.createStatement()) {
            // Migration 1: Leads-Tabelle erweitern (falls alte Version existiert)
            try {
                // Prüfe, ob die Spalte 'is_deleted' bereits existiert
                stmt.executeQuery("SELECT is_deleted FROM leads LIMIT 1");
                logger.info("Lead-Tabelle ist bereits aktuell");
            } catch (SQLException e) {
                // Spalte existiert nicht, führe Migration aus
                logger.info("Migriere Lead-Tabelle zu neuer Version...");

                // Backup der alten Daten (falls vorhanden)
                try {
                    stmt.execute("CREATE TABLE leads_backup AS SELECT * FROM leads");
                    logger.info("Backup der alten Lead-Daten erstellt");
                } catch (SQLException backupError) {
                    // Tabelle existiert vermutlich noch nicht
                    logger.info("Keine bestehende Lead-Tabelle gefunden - erstelle neue");
                }

                // Lösche alte Tabelle und erstelle neue
                try {
                    stmt.execute("DROP TABLE IF EXISTS leads");
                    logger.info("Alte Lead-Tabelle gelöscht");
                } catch (SQLException dropError) {
                    logger.warning("Fehler beim Löschen der alten Lead-Tabelle: " + dropError.getMessage());
                }

                // Erstelle neue Lead-Tabelle mit vollständigem Schema
                stmt.execute(
                        "CREATE TABLE leads (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "name TEXT NOT NULL, " +
                                "email TEXT NOT NULL UNIQUE, " +
                                "company TEXT, " +
                                "website TEXT, " +
                                "phone TEXT, " +
                                "status TEXT DEFAULT 'NEW', " +
                                "source TEXT DEFAULT 'MANUAL', " +
                                "notes TEXT, " +
                                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "is_deleted BOOLEAN DEFAULT 0" +
                                ")"
                );
                logger.info("Neue Lead-Tabelle mit vollständigem Schema erstellt");

                // Versuche alte Daten zu migrieren (falls Backup existiert)
                try {
                    stmt.execute(
                            "INSERT INTO leads (name, email, company, website, created_date, updated_date, is_deleted) " +
                                    "SELECT name, email, company, website, " +
                                    "COALESCE(created_date, CURRENT_TIMESTAMP), " +
                                    "COALESCE(updated_date, CURRENT_TIMESTAMP), " +
                                    "0 FROM leads_backup"
                    );
                    logger.info("Alte Lead-Daten erfolgreich migriert");

                    // Lösche Backup-Tabelle
                    stmt.execute("DROP TABLE leads_backup");
                } catch (SQLException migrateError) {
                    logger.info("Keine alten Daten zu migrieren: " + migrateError.getMessage());
                }
            }

            // Migration 2: Weitere Migrations können hier hinzugefügt werden
            logger.info("Alle Migrations erfolgreich abgeschlossen");

        } catch (SQLException e) {
            logger.severe("Fehler bei der Migration: " + e.getMessage());
            throw e;
        }
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
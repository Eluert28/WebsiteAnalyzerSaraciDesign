package com.saraci.websiteanalyzer.repository.impl;

import com.saraci.websiteanalyzer.config.DatabaseConfig;
import com.saraci.websiteanalyzer.model.Website;
import com.saraci.websiteanalyzer.repository.WebsiteRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * SQLite-Implementierung des Website-Repositories.
 */
public class WebsiteRepositoryImpl implements WebsiteRepository {
    private static final Logger logger = Logger.getLogger(WebsiteRepositoryImpl.class.getName());

    @Override
    public Website save(Website website) throws Exception {
        String sql = "INSERT OR IGNORE INTO websites (url, first_analysis_date, last_analysis_date) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, website.getUrl());
            pstmt.setTimestamp(2, Timestamp.valueOf(website.getFirstAnalysisDate()));
            pstmt.setTimestamp(3, Timestamp.valueOf(website.getLastAnalysisDate()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                // Website existiert bereits, hole sie aus der Datenbank
                return findByUrl(website.getUrl());
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    website.setId(generatedKeys.getLong(1));
                } else {
                    throw new Exception("Konnte keine ID für die neue Website erzeugen");
                }
            }

            logger.info("Website gespeichert: " + website.getUrl() + ", ID: " + website.getId());
            return website;
        }
    }

    @Override
    public Website findByUrl(String url) throws Exception {
        String sql = "SELECT * FROM websites WHERE url = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, url);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Website website = new Website();
                website.setId(rs.getLong("id"));
                website.setUrl(rs.getString("url"));
                website.setFirstAnalysisDate(rs.getTimestamp("first_analysis_date").toLocalDateTime());
                website.setLastAnalysisDate(rs.getTimestamp("last_analysis_date").toLocalDateTime());
                return website;
            } else {
                return null;
            }
        }
    }

    @Override
    public Website findById(Long id) throws Exception {
        String sql = "SELECT * FROM websites WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Website website = new Website();
                website.setId(rs.getLong("id"));
                website.setUrl(rs.getString("url"));
                website.setFirstAnalysisDate(rs.getTimestamp("first_analysis_date").toLocalDateTime());
                website.setLastAnalysisDate(rs.getTimestamp("last_analysis_date").toLocalDateTime());
                return website;
            } else {
                return null;
            }
        }
    }

    @Override
    public List<Website> findAll() throws Exception {
        String sql = "SELECT * FROM websites ORDER BY last_analysis_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Website> websites = new ArrayList<>();

            while (rs.next()) {
                Website website = new Website();
                website.setId(rs.getLong("id"));
                website.setUrl(rs.getString("url"));
                website.setFirstAnalysisDate(rs.getTimestamp("first_analysis_date").toLocalDateTime());
                website.setLastAnalysisDate(rs.getTimestamp("last_analysis_date").toLocalDateTime());
                websites.add(website);
            }

            return websites;
        }
    }

    @Override
    public void updateLastAnalysisDate(Long id) throws Exception {
        String sql = "UPDATE websites SET last_analysis_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(2, id);

            pstmt.executeUpdate();
            logger.info("Letztes Analysedatum für Website-ID " + id + " aktualisiert");
        }
    }
}
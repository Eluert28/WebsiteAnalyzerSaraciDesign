package com.saraci.websiteanalyzer.repository.impl;

import com.saraci.websiteanalyzer.config.DatabaseConfig;
import com.saraci.websiteanalyzer.model.EmailTemplate;
import com.saraci.websiteanalyzer.repository.EmailTemplateRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * SQLite-Implementierung des EmailTemplate-Repositories.
 */
public class EmailTemplateRepositoryImpl implements EmailTemplateRepository {
    private static final Logger logger = Logger.getLogger(EmailTemplateRepositoryImpl.class.getName());

    @Override
    public EmailTemplate save(EmailTemplate template) throws Exception {
        String sql = "INSERT INTO email_templates (name, subject, body, category, active, created_date, usage_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, template.getName());
            pstmt.setString(2, template.getSubject());
            pstmt.setString(3, template.getBody());
            pstmt.setString(4, template.getCategory());
            pstmt.setBoolean(5, template.isActive());
            pstmt.setTimestamp(6, Timestamp.valueOf(template.getCreatedDate()));
            pstmt.setInt(7, template.getUsageCount());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Template erstellen fehlgeschlagen, keine Zeilen betroffen.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    template.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Template erstellen fehlgeschlagen, keine ID generiert.");
                }
            }

            logger.info("E-Mail-Template gespeichert: " + template.getName() + ", ID: " + template.getId());
            return template;
        }
    }

    @Override
    public EmailTemplate findById(Long id) throws Exception {
        String sql = "SELECT * FROM email_templates WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTemplate(rs);
            } else {
                return null;
            }
        }
    }

    @Override
    public EmailTemplate findByName(String name) throws Exception {
        String sql = "SELECT * FROM email_templates WHERE name = ? AND active = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTemplate(rs);
            } else {
                return null;
            }
        }
    }

    @Override
    public List<EmailTemplate> findAllActive() throws Exception {
        String sql = "SELECT * FROM email_templates WHERE active = 1 ORDER BY created_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<EmailTemplate> templates = new ArrayList<>();

            while (rs.next()) {
                templates.add(mapResultSetToTemplate(rs));
            }

            return templates;
        }
    }

    @Override
    public List<EmailTemplate> findByCategory(String category) throws Exception {
        String sql = "SELECT * FROM email_templates WHERE category = ? AND active = 1 ORDER BY usage_count DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            List<EmailTemplate> templates = new ArrayList<>();

            while (rs.next()) {
                templates.add(mapResultSetToTemplate(rs));
            }

            return templates;
        }
    }

    @Override
    public List<EmailTemplate> findAll() throws Exception {
        String sql = "SELECT * FROM email_templates ORDER BY created_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<EmailTemplate> templates = new ArrayList<>();

            while (rs.next()) {
                templates.add(mapResultSetToTemplate(rs));
            }

            return templates;
        }
    }

    @Override
    public void update(EmailTemplate template) throws Exception {
        String sql = "UPDATE email_templates SET name = ?, subject = ?, body = ?, category = ?, active = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, template.getName());
            pstmt.setString(2, template.getSubject());
            pstmt.setString(3, template.getBody());
            pstmt.setString(4, template.getCategory());
            pstmt.setBoolean(5, template.isActive());
            pstmt.setLong(6, template.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("E-Mail-Template aktualisiert: ID = " + template.getId());
            } else {
                logger.warning("Kein Template mit ID = " + template.getId() + " gefunden zum Aktualisieren");
            }
        }
    }

    @Override
    public void markAsUsed(Long id) throws Exception {
        String sql = "UPDATE email_templates SET usage_count = usage_count + 1, last_used = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(2, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Template als verwendet markiert: ID = " + id);
            } else {
                logger.warning("Kein Template mit ID = " + id + " gefunden zum Markieren");
            }
        }
    }

    @Override
    public void deactivate(Long id) throws Exception {
        String sql = "UPDATE email_templates SET active = 0 WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("E-Mail-Template deaktiviert: ID = " + id);
            } else {
                logger.warning("Kein Template mit ID = " + id + " gefunden zum Deaktivieren");
            }
        }
    }

    @Override
    public void activate(Long id) throws Exception {
        String sql = "UPDATE email_templates SET active = 1 WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("E-Mail-Template aktiviert: ID = " + id);
            } else {
                logger.warning("Kein Template mit ID = " + id + " gefunden zum Aktivieren");
            }
        }
    }

    /**
     * Konvertiert ein ResultSet in ein EmailTemplate-Objekt.
     */
    private EmailTemplate mapResultSetToTemplate(ResultSet rs) throws SQLException {
        EmailTemplate template = new EmailTemplate();
        template.setId(rs.getLong("id"));
        template.setName(rs.getString("name"));
        template.setSubject(rs.getString("subject"));
        template.setBody(rs.getString("body"));
        template.setCategory(rs.getString("category"));
        template.setActive(rs.getBoolean("active"));
        template.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());

        Timestamp lastUsed = rs.getTimestamp("last_used");
        if (lastUsed != null) {
            template.setLastUsed(lastUsed.toLocalDateTime());
        }

        template.setUsageCount(rs.getInt("usage_count"));

        return template;
    }
}
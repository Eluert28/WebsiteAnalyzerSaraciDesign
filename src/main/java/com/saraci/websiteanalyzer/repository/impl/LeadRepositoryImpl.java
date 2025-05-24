package com.saraci.websiteanalyzer.repository.impl;

import com.saraci.websiteanalyzer.config.DatabaseConfig;
import com.saraci.websiteanalyzer.model.Lead;
import com.saraci.websiteanalyzer.repository.LeadRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * SQLite-Implementierung des Lead-Repositories.
 */
public class LeadRepositoryImpl implements LeadRepository {
    private static final Logger logger = Logger.getLogger(LeadRepositoryImpl.class.getName());

    @Override
    public Lead save(Lead lead) throws Exception {
        // Prüfe zuerst, ob Lead bereits existiert
        Lead existingLead = findByEmail(lead.getEmail());
        if (existingLead != null) {
            // Update existing lead
            return updateLead(lead, existingLead.getId());
        }

        // Insert new lead
        String sql = "INSERT INTO leads (email, name, company, website, created_date, active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, lead.getEmail());
            pstmt.setString(2, lead.getName());
            pstmt.setString(3, lead.getCompany());
            pstmt.setString(4, lead.getWebsite());
            pstmt.setTimestamp(5, Timestamp.valueOf(lead.getCreatedDate()));
            pstmt.setBoolean(6, lead.isActive());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    lead.setId(generatedKeys.getLong(1));
                } else {
                    throw new Exception("Konnte keine ID für den neuen Lead erzeugen");
                }
            }

            logger.info("Lead gespeichert: " + lead.getEmail() + ", ID: " + lead.getId());
            return lead;
        }
    }

    private Lead updateLead(Lead lead, Long existingId) throws Exception {
        String sql = "UPDATE leads SET name = ?, company = ?, website = ?, active = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lead.getName());
            pstmt.setString(2, lead.getCompany());
            pstmt.setString(3, lead.getWebsite());
            pstmt.setBoolean(4, lead.isActive());
            pstmt.setLong(5, existingId);

            pstmt.executeUpdate();
            lead.setId(existingId);

            logger.info("Lead aktualisiert: " + lead.getEmail() + ", ID: " + existingId);
            return lead;
        }
    }

    @Override
    public Lead findById(Long id) throws Exception {
        String sql = "SELECT * FROM leads WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToLead(rs);
            } else {
                return null;
            }
        }
    }

    @Override
    public Lead findByEmail(String email) throws Exception {
        String sql = "SELECT * FROM leads WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToLead(rs);
            } else {
                return null;
            }
        }
    }

    @Override
    public List<Lead> findAllActive() throws Exception {
        String sql = "SELECT * FROM leads WHERE active = 1 ORDER BY created_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Lead> leads = new ArrayList<>();

            while (rs.next()) {
                leads.add(mapResultSetToLead(rs));
            }

            return leads;
        }
    }

    @Override
    public void deactivate(Long id) throws Exception {
        String sql = "UPDATE leads SET active = 0 WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Lead deaktiviert: ID = " + id);
            } else {
                logger.warning("Kein Lead mit ID = " + id + " gefunden zum Deaktivieren");
            }
        }
    }

    /**
     * Konvertiert ein ResultSet in ein Lead-Objekt.
     */
    private Lead mapResultSetToLead(ResultSet rs) throws SQLException {
        Lead lead = new Lead();
        lead.setId(rs.getLong("id"));
        lead.setEmail(rs.getString("email"));
        lead.setName(rs.getString("name"));
        lead.setCompany(rs.getString("company"));
        lead.setWebsite(rs.getString("website"));
        lead.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
        lead.setActive(rs.getBoolean("active"));
        return lead;
    }
}
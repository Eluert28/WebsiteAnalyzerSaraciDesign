package com.saraci.websiteanalyzer.repository.impl;

import com.saraci.websiteanalyzer.config.DatabaseConfig;
import com.saraci.websiteanalyzer.model.Lead;
import com.saraci.websiteanalyzer.repository.LeadRepository;

import java.sql.*;
import java.time.LocalDateTime;
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
        String sql = "INSERT INTO leads (name, email, company, website, phone, status, source, notes, " +
                "created_date, updated_date, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, lead.getName());
            pstmt.setString(2, lead.getEmail());
            pstmt.setString(3, lead.getCompany());
            pstmt.setString(4, lead.getWebsite());
            pstmt.setString(5, lead.getPhone());
            pstmt.setString(6, lead.getStatus());
            pstmt.setString(7, lead.getSource());
            pstmt.setString(8, lead.getNotes());
            pstmt.setTimestamp(9, Timestamp.valueOf(lead.getCreatedDate()));
            pstmt.setTimestamp(10, Timestamp.valueOf(lead.getUpdatedDate()));
            pstmt.setBoolean(11, lead.isDeleted());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    lead.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Konnte keine ID für den neuen Lead erzeugen");
                }
            }

            logger.info("Lead gespeichert: " + lead.getEmail() + ", ID: " + lead.getId());
            return lead;
        }
    }

    @Override
    public Lead update(Lead lead) throws Exception {
        if (lead.getId() == null) {
            throw new IllegalArgumentException("Lead-ID darf nicht null sein für Update-Operation");
        }

        String sql = "UPDATE leads SET name = ?, email = ?, company = ?, website = ?, phone = ?, " +
                "status = ?, source = ?, notes = ?, updated_date = ? WHERE id = ? AND is_deleted = 0";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Aktualisiere das updated_date
            lead.setUpdatedDate(LocalDateTime.now());

            pstmt.setString(1, lead.getName());
            pstmt.setString(2, lead.getEmail());
            pstmt.setString(3, lead.getCompany());
            pstmt.setString(4, lead.getWebsite());
            pstmt.setString(5, lead.getPhone());
            pstmt.setString(6, lead.getStatus());
            pstmt.setString(7, lead.getSource());
            pstmt.setString(8, lead.getNotes());
            pstmt.setTimestamp(9, Timestamp.valueOf(lead.getUpdatedDate()));
            pstmt.setLong(10, lead.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception("Lead mit ID " + lead.getId() + " nicht gefunden oder bereits gelöscht");
            }

            logger.info("Lead aktualisiert: " + lead.getEmail() + ", ID: " + lead.getId());
            return lead;
        }
    }

    @Override
    public Lead findById(Long id) throws Exception {
        String sql = "SELECT * FROM leads WHERE id = ? AND is_deleted = 0";

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
        String sql = "SELECT * FROM leads WHERE email = ? AND is_deleted = 0";

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
    public List<Lead> findAll() throws Exception {
        String sql = "SELECT * FROM leads WHERE is_deleted = 0 ORDER BY created_date DESC";

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
    public List<Lead> findByStatus(String status) throws Exception {
        String sql = "SELECT * FROM leads WHERE status = ? AND is_deleted = 0 ORDER BY created_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            List<Lead> leads = new ArrayList<>();

            while (rs.next()) {
                leads.add(mapResultSetToLead(rs));
            }

            return leads;
        }
    }

    @Override
    public void deleteById(Long id) throws Exception {
        String sql = "UPDATE leads SET is_deleted = 1, updated_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(2, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Lead soft-deleted: ID = " + id);
            } else {
                logger.warning("Kein Lead mit ID = " + id + " gefunden zum Löschen");
            }
        }
    }

    @Override
    public void deactivate(Long id) throws Exception {
        // Alias für deleteById - macht das gleiche (Soft Delete)
        deleteById(id);
        logger.info("Lead deaktiviert: ID = " + id);
    }

    @Override
    public List<Lead> findAllActive() throws Exception {
        // Alias für findAll() - beide machen das gleiche (nur aktive Leads)
        return findAll();
    }

    @Override
    public long countAll() throws Exception {
        String sql = "SELECT COUNT(*) FROM leads WHERE is_deleted = 0";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        }
    }

    /**
     * Konvertiert ein ResultSet in ein Lead-Objekt.
     */
    private Lead mapResultSetToLead(ResultSet rs) throws SQLException {
        Lead lead = new Lead();
        lead.setId(rs.getLong("id"));
        lead.setName(rs.getString("name"));
        lead.setEmail(rs.getString("email"));
        lead.setCompany(rs.getString("company"));
        lead.setWebsite(rs.getString("website"));
        lead.setPhone(rs.getString("phone"));
        lead.setStatus(rs.getString("status"));
        lead.setSource(rs.getString("source"));
        lead.setNotes(rs.getString("notes"));

        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            lead.setCreatedDate(createdDate.toLocalDateTime());
        }

        Timestamp updatedDate = rs.getTimestamp("updated_date");
        if (updatedDate != null) {
            lead.setUpdatedDate(updatedDate.toLocalDateTime());
        }

        lead.setDeleted(rs.getBoolean("is_deleted"));

        return lead;
    }
}
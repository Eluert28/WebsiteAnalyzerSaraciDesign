package com.saraci.websiteanalyzer.repository.impl;

import com.saraci.websiteanalyzer.config.DatabaseConfig;
import com.saraci.websiteanalyzer.model.AnalysisSchedule;
import com.saraci.websiteanalyzer.repository.ScheduleRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * SQLite-Implementierung des Schedule-Repositories.
 */
public class ScheduleRepositoryImpl implements ScheduleRepository {
    private static final Logger logger = Logger.getLogger(ScheduleRepositoryImpl.class.getName());

    @Override
    public AnalysisSchedule save(AnalysisSchedule schedule) throws Exception {
        String sql = "INSERT INTO schedules (website_id, cron_expression, recipients, report_type, " +
                "is_active, last_run, next_run) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, schedule.getWebsiteId());
            pstmt.setString(2, schedule.getCronExpression());
            pstmt.setString(3, schedule.getRecipients());
            pstmt.setString(4, schedule.getReportType());
            pstmt.setBoolean(5, schedule.isActive());

            if (schedule.getLastRun() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(schedule.getLastRun()));
            } else {
                pstmt.setNull(6, Types.TIMESTAMP);
            }

            if (schedule.getNextRun() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(schedule.getNextRun()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    schedule.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Konnte keine ID für den neuen Zeitplan erzeugen");
                }
            }

            logger.info("Zeitplan gespeichert: ID = " + schedule.getId());
            return schedule;
        }
    }

    @Override
    public AnalysisSchedule findById(Long id) throws Exception {
        String sql = "SELECT * FROM schedules WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSchedule(rs);
            } else {
                return null;
            }
        }
    }

    @Override
    public List<AnalysisSchedule> findByWebsiteId(Long websiteId) throws Exception {
        String sql = "SELECT * FROM schedules WHERE website_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, websiteId);
            ResultSet rs = pstmt.executeQuery();

            List<AnalysisSchedule> schedules = new ArrayList<>();

            while (rs.next()) {
                schedules.add(mapResultSetToSchedule(rs));
            }

            return schedules;
        }
    }

    @Override
    public List<AnalysisSchedule> findAllActive() throws Exception {
        String sql = "SELECT * FROM schedules WHERE is_active = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<AnalysisSchedule> schedules = new ArrayList<>();

            while (rs.next()) {
                schedules.add(mapResultSetToSchedule(rs));
            }

            return schedules;
        }
    }

    @Override
    public void update(AnalysisSchedule schedule) throws Exception {
        String sql = "UPDATE schedules SET cron_expression = ?, recipients = ?, report_type = ?, " +
                "is_active = ?, last_run = ?, next_run = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getCronExpression());
            pstmt.setString(2, schedule.getRecipients());
            pstmt.setString(3, schedule.getReportType());
            pstmt.setBoolean(4, schedule.isActive());

            if (schedule.getLastRun() != null) {
                pstmt.setTimestamp(5, Timestamp.valueOf(schedule.getLastRun()));
            } else {
                pstmt.setNull(5, Types.TIMESTAMP);
            }

            if (schedule.getNextRun() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(schedule.getNextRun()));
            } else {
                pstmt.setNull(6, Types.TIMESTAMP);
            }

            pstmt.setLong(7, schedule.getId());

            pstmt.executeUpdate();
            logger.info("Zeitplan aktualisiert: ID = " + schedule.getId());
        }
    }

    @Override
    public void deleteById(Long id) throws Exception {
        String sql = "DELETE FROM schedules WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Zeitplan gelöscht: ID = " + id);
            } else {
                logger.warning("Kein Zeitplan mit ID = " + id + " gefunden zum Löschen");
            }
        }
    }

    /**
     * Konvertiert ein ResultSet in ein AnalysisSchedule-Objekt.
     */
    private AnalysisSchedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
        AnalysisSchedule schedule = new AnalysisSchedule();
        schedule.setId(rs.getLong("id"));
        schedule.setWebsiteId(rs.getLong("website_id"));
        schedule.setCronExpression(rs.getString("cron_expression"));
        schedule.setRecipients(rs.getString("recipients"));
        schedule.setReportType(rs.getString("report_type"));
        schedule.setActive(rs.getBoolean("is_active"));

        Timestamp lastRun = rs.getTimestamp("last_run");
        if (lastRun != null) {
            schedule.setLastRun(lastRun.toLocalDateTime());
        }

        Timestamp nextRun = rs.getTimestamp("next_run");
        if (nextRun != null) {
            schedule.setNextRun(nextRun.toLocalDateTime());
        }

        return schedule;
    }
}
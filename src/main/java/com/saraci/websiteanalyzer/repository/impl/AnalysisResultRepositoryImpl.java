package com.saraci.websiteanalyzer.repository.impl;

import com.saraci.websiteanalyzer.config.DatabaseConfig;
import com.saraci.websiteanalyzer.model.*;
import com.saraci.websiteanalyzer.repository.AnalysisResultRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * SQLite-Implementierung des AnalysisResult-Repositories.
 */
public class AnalysisResultRepositoryImpl implements AnalysisResultRepository {
    private static final Logger logger = Logger.getLogger(AnalysisResultRepositoryImpl.class.getName());

    @Override
    public AnalysisResult save(AnalysisResult result) throws Exception {
        Connection conn = null;

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Speichere das Hauptergebnis
            saveMainResult(conn, result);

            // Speichere die Teilresultate
            if (result.getSeoResult() != null) {
                saveSeoResult(conn, result.getSeoResult(), result.getId());
            }

            if (result.getPerformanceResult() != null) {
                savePerformanceResult(conn, result.getPerformanceResult(), result.getId());
            }

            if (result.getSecurityResult() != null) {
                saveSecurityResult(conn, result.getSecurityResult(), result.getId());
            }

            if (result.getContentResult() != null) {
                saveContentResult(conn, result.getContentResult(), result.getId());
            }

            conn.commit();
            logger.info("Analyseergebnis gespeichert: ID = " + result.getId());

            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.severe("Fehler beim Rollback: " + rollbackEx.getMessage());
                }
            }
            logger.severe("Fehler beim Speichern des Analyseergebnisses: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.warning("Fehler beim Schließen der Datenbankverbindung: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Speichert das Hauptergebnis in der Datenbank.
     */
    private void saveMainResult(Connection conn, AnalysisResult result) throws SQLException {
        String sql = "INSERT INTO analysis_results (website_id, analysis_date, pdf_report_path) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, result.getWebsiteId());
            pstmt.setTimestamp(2, Timestamp.valueOf(result.getAnalysisDate()));
            pstmt.setString(3, result.getPdfReportPath());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    result.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Konnte keine ID für das neue Analyseergebnis erzeugen");
                }
            }
        }
    }

    /**
     * Speichert das SEO-Ergebnis in der Datenbank.
     */
    private void saveSeoResult(Connection conn, SeoResult result, Long analysisId) throws SQLException {
        String sql = "INSERT INTO seo_results " +
                "(analysis_id, title, title_length, description, description_length, keywords, " +
                "h1_count, h2_count, h3_count, images_total, images_with_alt, images_without_alt, " +
                "alt_image_percentage, internal_links, external_links, score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, analysisId);
            pstmt.setString(2, result.getTitle());
            pstmt.setInt(3, result.getTitleLength());
            pstmt.setString(4, result.getDescription());
            pstmt.setInt(5, result.getDescriptionLength());
            pstmt.setString(6, result.getKeywords());
            pstmt.setInt(7, result.getH1Count());
            pstmt.setInt(8, result.getH2Count());
            pstmt.setInt(9, result.getH3Count());
            pstmt.setInt(10, result.getImagesTotal());
            pstmt.setInt(11, result.getImagesWithAlt());
            pstmt.setInt(12, result.getImagesWithoutAlt());
            pstmt.setDouble(13, result.getAltImagePercentage());
            pstmt.setInt(14, result.getInternalLinks());
            pstmt.setInt(15, result.getExternalLinks());
            pstmt.setInt(16, result.getScore());

            pstmt.executeUpdate();
            result.setAnalysisId(analysisId);
        }
    }

    /**
     * Speichert das Performance-Ergebnis in der Datenbank.
     */
    private void savePerformanceResult(Connection conn, PerformanceResult result, Long analysisId) throws SQLException {
        String sql = "INSERT INTO performance_results " +
                "(analysis_id, lighthouse_score, first_contentful_paint, largest_contentful_paint, " +
                "time_to_interactive, total_blocking_time, cumulative_layout_shift, load_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, analysisId);
            pstmt.setInt(2, result.getLighthouseScore());
            pstmt.setString(3, result.getFirstContentfulPaint());
            pstmt.setString(4, result.getLargestContentfulPaint());
            pstmt.setString(5, result.getTimeToInteractive());
            pstmt.setString(6, result.getTotalBlockingTime());
            pstmt.setString(7, result.getCumulativeLayoutShift());
            pstmt.setInt(8, result.getLoadTime());

            pstmt.executeUpdate();
            result.setAnalysisId(analysisId);
        }
    }

    /**
     * Speichert das Sicherheits-Ergebnis in der Datenbank.
     */
    private void saveSecurityResult(Connection conn, SecurityResult result, Long analysisId) throws SQLException {
        String sql = "INSERT INTO security_results " +
                "(analysis_id, https_enabled, security_headers_score, cookies_security_score, security_headers) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, analysisId);
            pstmt.setBoolean(2, result.isHttpsEnabled());
            pstmt.setInt(3, result.getSecurityHeadersScore());
            pstmt.setInt(4, result.getCookiesSecurityScore());
            pstmt.setString(5, result.getSecurityHeaders());

            pstmt.executeUpdate();
            result.setAnalysisId(analysisId);
        }
    }

    /**
     * Speichert das Inhalts-Ergebnis in der Datenbank.
     */
    private void saveContentResult(Connection conn, ContentResult result, Long analysisId) throws SQLException {
        String sql = "INSERT INTO content_results " +
                "(analysis_id, word_count, character_count, average_word_length, " +
                "paragraph_count, image_count, video_count, list_count, table_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, analysisId);
            pstmt.setInt(2, result.getWordCount());
            pstmt.setInt(3, result.getCharacterCount());
            pstmt.setDouble(4, result.getAverageWordLength());
            pstmt.setInt(5, result.getParagraphCount());
            pstmt.setInt(6, result.getImageCount());
            pstmt.setInt(7, result.getVideoCount());
            pstmt.setInt(8, result.getListCount());
            pstmt.setInt(9, result.getTableCount());

            pstmt.executeUpdate();
            result.setAnalysisId(analysisId);
        }
    }

    @Override
    public AnalysisResult findById(Long id) throws Exception {
        String sql = "SELECT * FROM analysis_results WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                AnalysisResult result = new AnalysisResult();
                result.setId(rs.getLong("id"));
                result.setWebsiteId(rs.getLong("website_id"));
                result.setAnalysisDate(rs.getTimestamp("analysis_date").toLocalDateTime());
                result.setPdfReportPath(rs.getString("pdf_report_path"));

                // Lade die Teilresultate
                result.setSeoResult(findSeoResultByAnalysisId(conn, id));
                result.setPerformanceResult(findPerformanceResultByAnalysisId(conn, id));
                result.setSecurityResult(findSecurityResultByAnalysisId(conn, id));
                result.setContentResult(findContentResultByAnalysisId(conn, id));

                return result;
            } else {
                return null;
            }
        }
    }

    @Override
    public List<AnalysisResult> findByWebsiteId(Long websiteId) throws Exception {
        String sql = "SELECT * FROM analysis_results WHERE website_id = ? ORDER BY analysis_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, websiteId);
            ResultSet rs = pstmt.executeQuery();

            List<AnalysisResult> results = new ArrayList<>();

            while (rs.next()) {
                AnalysisResult result = new AnalysisResult();
                result.setId(rs.getLong("id"));
                result.setWebsiteId(rs.getLong("website_id"));
                result.setAnalysisDate(rs.getTimestamp("analysis_date").toLocalDateTime());
                result.setPdfReportPath(rs.getString("pdf_report_path"));

                // Lade nur die Scores für die Übersicht (effizienter)
                loadScores(conn, result);

                results.add(result);
            }

            return results;
        }
    }

    /**
     * Lädt nur die Scores für die Übersicht.
     */
    private void loadScores(Connection conn, AnalysisResult result) throws SQLException {
        // SEO-Score
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT score FROM seo_results WHERE analysis_id = ?")) {
            pstmt.setLong(1, result.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SeoResult seoResult = new SeoResult();
                seoResult.setScore(rs.getInt("score"));
                result.setSeoResult(seoResult);
            }
        }

        // Performance-Score
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT lighthouse_score FROM performance_results WHERE analysis_id = ?")) {
            pstmt.setLong(1, result.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PerformanceResult performanceResult = new PerformanceResult();
                performanceResult.setLighthouseScore(rs.getInt("lighthouse_score"));
                result.setPerformanceResult(performanceResult);
            }
        }

        // Sicherheits-Score
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT security_headers_score FROM security_results WHERE analysis_id = ?")) {
            pstmt.setLong(1, result.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SecurityResult securityResult = new SecurityResult();
                securityResult.setSecurityHeadersScore(rs.getInt("security_headers_score"));
                result.setSecurityResult(securityResult);
            }
        }
    }

    /**
     * Findet das SEO-Ergebnis für eine Analyse-ID.
     */
    private SeoResult findSeoResultByAnalysisId(Connection conn, Long analysisId) throws SQLException {
        String sql = "SELECT * FROM seo_results WHERE analysis_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, analysisId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SeoResult result = new SeoResult();
                result.setAnalysisId(rs.getLong("analysis_id"));
                result.setTitle(rs.getString("title"));
                result.setTitleLength(rs.getInt("title_length"));
                result.setDescription(rs.getString("description"));
                result.setDescriptionLength(rs.getInt("description_length"));
                result.setKeywords(rs.getString("keywords"));
                result.setH1Count(rs.getInt("h1_count"));
                result.setH2Count(rs.getInt("h2_count"));
                result.setH3Count(rs.getInt("h3_count"));
                result.setImagesTotal(rs.getInt("images_total"));
                result.setImagesWithAlt(rs.getInt("images_with_alt"));
                result.setImagesWithoutAlt(rs.getInt("images_without_alt"));
                result.setAltImagePercentage(rs.getDouble("alt_image_percentage"));
                result.setInternalLinks(rs.getInt("internal_links"));
                result.setExternalLinks(rs.getInt("external_links"));
                result.setScore(rs.getInt("score"));
                return result;
            } else {
                return null;
            }
        }
    }

    /**
     * Findet das Performance-Ergebnis für eine Analyse-ID.
     */
    private PerformanceResult findPerformanceResultByAnalysisId(Connection conn, Long analysisId) throws SQLException {
        String sql = "SELECT * FROM performance_results WHERE analysis_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, analysisId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PerformanceResult result = new PerformanceResult();
                result.setAnalysisId(rs.getLong("analysis_id"));
                result.setLighthouseScore(rs.getInt("lighthouse_score"));
                result.setFirstContentfulPaint(rs.getString("first_contentful_paint"));
                result.setLargestContentfulPaint(rs.getString("largest_contentful_paint"));
                result.setTimeToInteractive(rs.getString("time_to_interactive"));
                result.setTotalBlockingTime(rs.getString("total_blocking_time"));
                result.setCumulativeLayoutShift(rs.getString("cumulative_layout_shift"));
                result.setLoadTime(rs.getInt("load_time"));
                return result;
            } else {
                return null;
            }
        }
    }

    /**
     * Findet das Sicherheits-Ergebnis für eine Analyse-ID.
     */
    private SecurityResult findSecurityResultByAnalysisId(Connection conn, Long analysisId) throws SQLException {
        String sql = "SELECT * FROM security_results WHERE analysis_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, analysisId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SecurityResult result = new SecurityResult();
                result.setAnalysisId(rs.getLong("analysis_id"));
                result.setHttpsEnabled(rs.getBoolean("https_enabled"));
                result.setSecurityHeadersScore(rs.getInt("security_headers_score"));
                result.setCookiesSecurityScore(rs.getInt("cookies_security_score"));
                result.setSecurityHeaders(rs.getString("security_headers"));
                return result;
            } else {
                return null;
            }
        }
    }

    /**
     * Findet das Inhalts-Ergebnis für eine Analyse-ID.
     */
    private ContentResult findContentResultByAnalysisId(Connection conn, Long analysisId) throws SQLException {
        String sql = "SELECT * FROM content_results WHERE analysis_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, analysisId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                ContentResult result = new ContentResult();
                result.setAnalysisId(rs.getLong("analysis_id"));
                result.setWordCount(rs.getInt("word_count"));
                result.setCharacterCount(rs.getInt("character_count"));
                result.setAverageWordLength(rs.getDouble("average_word_length"));
                result.setParagraphCount(rs.getInt("paragraph_count"));
                result.setImageCount(rs.getInt("image_count"));
                result.setVideoCount(rs.getInt("video_count"));
                result.setListCount(rs.getInt("list_count"));
                result.setTableCount(rs.getInt("table_count"));
                return result;
            } else {
                return null;
            }
        }
    }

    @Override
    public void updatePdfReportPath(Long id, String pdfPath) throws Exception {
        String sql = "UPDATE analysis_results SET pdf_report_path = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pdfPath);
            pstmt.setLong(2, id);

            pstmt.executeUpdate();
            logger.info("PDF-Berichtspfad für Analyse-ID " + id + " aktualisiert: " + pdfPath);
        }
    }
}
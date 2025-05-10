package com.saraci.websiteanalyzer.repository;

import com.saraci.websiteanalyzer.model.AnalysisResult;

import java.util.List;

/**
 * Repository-Schnittstelle für die AnalysisResult-Entität.
 */
public interface AnalysisResultRepository {

    /**
     * Speichert ein Analyseergebnis in der Datenbank.
     *
     * @param result Das zu speichernde Analyseergebnis
     * @return Das gespeicherte Analyseergebnis mit aktualisierter ID
     * @throws Exception Bei Datenbankfehlern
     */
    AnalysisResult save(AnalysisResult result) throws Exception;

    /**
     * Findet ein Analyseergebnis anhand seiner ID.
     *
     * @param id Die ID des Analyseergebnisses
     * @return Das gefundene Analyseergebnis oder null, falls nicht vorhanden
     * @throws Exception Bei Datenbankfehlern
     */
    AnalysisResult findById(Long id) throws Exception;

    /**
     * Findet alle Analyseergebnisse für eine Website.
     *
     * @param websiteId Die ID der Website
     * @return Eine Liste aller Analyseergebnisse für die Website
     * @throws Exception Bei Datenbankfehlern
     */
    List<AnalysisResult> findByWebsiteId(Long websiteId) throws Exception;

    /**
     * Aktualisiert den PDF-Berichtspfad eines Analyseergebnisses.
     *
     * @param id Die ID des Analyseergebnisses
     * @param pdfPath Der neue PDF-Berichtspfad
     * @throws Exception Bei Datenbankfehlern
     */
    void updatePdfReportPath(Long id, String pdfPath) throws Exception;
}
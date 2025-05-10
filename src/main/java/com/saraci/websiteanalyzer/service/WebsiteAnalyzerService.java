package com.saraci.websiteanalyzer.service;

import com.saraci.websiteanalyzer.model.AnalysisResult;

/**
 * Hauptschnittstelle für den Website-Analyse-Service.
 */
public interface WebsiteAnalyzerService {

    /**
     * Führt eine vollständige Analyse einer Website durch.
     *
     * @param url Die URL der zu analysierenden Website
     * @return Das Analyseergebnis
     * @throws Exception Wenn bei der Analyse ein Fehler auftritt
     */
    AnalysisResult analyzeWebsite(String url) throws Exception;

    /**
     * Generiert einen PDF-Bericht für ein Analyseergebnis.
     *
     * @param result Das Analyseergebnis
     * @return Der Pfad zur generierten PDF-Datei
     * @throws Exception Wenn bei der Berichtserstellung ein Fehler auftritt
     */
    String generatePdfReport(AnalysisResult result) throws Exception;

    /**
     * Sendet einen Bericht per E-Mail.
     *
     * @param pdfPath Der Pfad zur PDF-Datei
     * @param email Die E-Mail-Adresse des Empfängers
     * @param websiteUrl Die URL der analysierten Website
     * @throws Exception Wenn beim E-Mail-Versand ein Fehler auftritt
     */
    void sendReportByEmail(String pdfPath, String email, String websiteUrl) throws Exception;
}
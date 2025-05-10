package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.model.AnalysisResult;
import com.saraci.websiteanalyzer.model.Website;
import com.saraci.websiteanalyzer.repository.AnalysisResultRepository;
import com.saraci.websiteanalyzer.repository.WebsiteRepository;
import com.saraci.websiteanalyzer.service.WebsiteAnalyzerService;
import com.saraci.websiteanalyzer.util.JsonUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static spark.Spark.*;

/**
 * Controller für die Analyse-Funktionalität.
 */
public class AnalysisController implements Controller {
    private static final Logger logger = Logger.getLogger(AnalysisController.class.getName());

    private final WebsiteAnalyzerService analyzerService;
    private final WebsiteRepository websiteRepository;
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public AnalysisController(WebsiteAnalyzerService analyzerService,
                              WebsiteRepository websiteRepository,
                              AnalysisResultRepository analysisResultRepository) {
        this.analyzerService = analyzerService;
        this.websiteRepository = websiteRepository;
        this.analysisResultRepository = analysisResultRepository;
    }

    @Override
    public void registerRoutes() {
        // Route zum Analysieren einer Website
        post("/api/analyze", (req, res) -> {
            res.type("application/json");

            try {
                // Hole die Parameter
                String requestBody = req.body();
                String url;
                String email = null;

                // Wenn ein JSON-Body vorhanden ist, hole die Parameter daraus
                if (requestBody != null && !requestBody.isEmpty()) {
                    // Einfaches JSON-Parsing mit unserem JsonUtil
                    url = JsonUtil.getStringValue(requestBody, "url");
                    email = JsonUtil.getStringValue(requestBody, "email");
                } else {
                    // Ansonsten hole die Parameter aus den Query-Parametern
                    url = req.queryParams("url");
                    email = req.queryParams("email");
                }

                // URL validieren
                if (url == null || url.isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("URL ist erforderlich"));
                }

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }

                // Speichere oder hole die Website aus der Datenbank
                Website website = websiteRepository.findByUrl(url);
                if (website == null) {
                    website = new Website(url);
                    website = websiteRepository.save(website);
                    logger.info("Neue Website erstellt: " + website.getUrl() + ", ID: " + website.getId());
                }

                // Website analysieren
                logger.info("Starte Analyse für: " + url);
                AnalysisResult result = analyzerService.analyzeWebsite(url);
                result.setWebsiteId(website.getId());
                result = analysisResultRepository.save(result);

                // Debug-Ausgabe der Ergebnisse
                logger.info("Analyseergebnis gespeichert: ID = " + result.getId());
                logger.info("SEO-Score: " + (result.getSeoResult() != null ? result.getSeoResult().getScore() : "N/A"));
                logger.info("Performance-Score: " + (result.getPerformanceResult() != null ? result.getPerformanceResult().getLighthouseScore() : "N/A"));
                logger.info("Security-Score: " + (result.getSecurityResult() != null ? result.getSecurityResult().getSecurityHeadersScore() : "N/A"));

                // Aktualisiere das letzte Analysedatum der Website
                websiteRepository.updateLastAnalysisDate(website.getId());

                // PDF-Bericht generieren
                String pdfPath = analyzerService.generatePdfReport(result);
                analysisResultRepository.updatePdfReportPath(result.getId(), pdfPath);

                // E-Mail senden, falls eine E-Mail-Adresse angegeben wurde
                if (email != null && !email.isEmpty()) {
                    analyzerService.sendReportByEmail(pdfPath, email, url);
                    logger.info("Bericht per E-Mail an " + email + " gesendet");
                }

                // Erstelle eine direkte Antwort mit allen Ergebnissen
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Analyse erfolgreich abgeschlossen");
                response.put("analysisId", result.getId());
                response.put("url", url);
                response.put("pdfPath", pdfPath);
                response.put("seoResult", result.getSeoResult());
                response.put("performanceResult", result.getPerformanceResult());
                response.put("securityResult", result.getSecurityResult());
                response.put("contentResult", result.getContentResult());

                // Erfolg zurückgeben
                return JsonUtil.toJson(response);
            } catch (Exception e) {
                logger.severe("Fehler bei der Analyse: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler bei der Analyse: " + e.getMessage()));
            }
        });

        // Route zum Abrufen eines Analyseergebnisses
        get("/api/analysis/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                AnalysisResult result = analysisResultRepository.findById(id);

                if (result == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Analyseergebnis nicht gefunden"));
                }

                // Manuell eine Map erstellen, um Probleme mit zyklischen Referenzen und DateTime zu vermeiden
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("id", result.getId());
                responseData.put("websiteId", result.getWebsiteId());
                responseData.put("url", result.getUrl());
                responseData.put("analysisDate", result.getAnalysisDate().toString());
                responseData.put("pdfReportPath", result.getPdfReportPath());

                // Manuell auch Unterobjekte konvertieren
                if (result.getSeoResult() != null) {
                    Map<String, Object> seoMap = new HashMap<>();
                    seoMap.put("id", result.getSeoResult().getAnalysisId());
                    seoMap.put("score", result.getSeoResult().getScore());
                    seoMap.put("title", result.getSeoResult().getTitle());
                    seoMap.put("titleLength", result.getSeoResult().getTitleLength());
                    seoMap.put("description", result.getSeoResult().getDescription());
                    seoMap.put("descriptionLength", result.getSeoResult().getDescriptionLength());
                    seoMap.put("h1Count", result.getSeoResult().getH1Count());
                    seoMap.put("h2Count", result.getSeoResult().getH2Count());
                    seoMap.put("h3Count", result.getSeoResult().getH3Count());
                    seoMap.put("imagesTotal", result.getSeoResult().getImagesTotal());
                    seoMap.put("imagesWithAlt", result.getSeoResult().getImagesWithAlt());
                    seoMap.put("imagesWithoutAlt", result.getSeoResult().getImagesWithoutAlt());
                    seoMap.put("altImagePercentage", result.getSeoResult().getAltImagePercentage());
                    seoMap.put("internalLinks", result.getSeoResult().getInternalLinks());
                    seoMap.put("externalLinks", result.getSeoResult().getExternalLinks());
                    responseData.put("seoResult", seoMap);
                }

                // Ähnlich für die anderen Ergebnisse
                if (result.getPerformanceResult() != null) {
                    Map<String, Object> perfMap = new HashMap<>();
                    perfMap.put("id", result.getPerformanceResult().getAnalysisId());
                    perfMap.put("lighthouseScore", result.getPerformanceResult().getLighthouseScore());
                    perfMap.put("firstContentfulPaint", result.getPerformanceResult().getFirstContentfulPaint());
                    perfMap.put("largestContentfulPaint", result.getPerformanceResult().getLargestContentfulPaint());
                    perfMap.put("timeToInteractive", result.getPerformanceResult().getTimeToInteractive());
                    perfMap.put("totalBlockingTime", result.getPerformanceResult().getTotalBlockingTime());
                    perfMap.put("cumulativeLayoutShift", result.getPerformanceResult().getCumulativeLayoutShift());
                    perfMap.put("loadTime", result.getPerformanceResult().getLoadTime());
                    responseData.put("performanceResult", perfMap);
                }

                if (result.getSecurityResult() != null) {
                    Map<String, Object> secMap = new HashMap<>();
                    secMap.put("id", result.getSecurityResult().getAnalysisId());
                    secMap.put("httpsEnabled", result.getSecurityResult().isHttpsEnabled());
                    secMap.put("securityHeadersScore", result.getSecurityResult().getSecurityHeadersScore());
                    secMap.put("cookiesSecurityScore", result.getSecurityResult().getCookiesSecurityScore());
                    secMap.put("securityHeaders", result.getSecurityResult().getSecurityHeaders());
                    responseData.put("securityResult", secMap);
                }

                if (result.getContentResult() != null) {
                    Map<String, Object> contentMap = new HashMap<>();
                    contentMap.put("id", result.getContentResult().getAnalysisId());
                    contentMap.put("wordCount", result.getContentResult().getWordCount());
                    contentMap.put("characterCount", result.getContentResult().getCharacterCount());
                    contentMap.put("averageWordLength", result.getContentResult().getAverageWordLength());
                    contentMap.put("paragraphCount", result.getContentResult().getParagraphCount());
                    contentMap.put("imageCount", result.getContentResult().getImageCount());
                    contentMap.put("videoCount", result.getContentResult().getVideoCount());
                    contentMap.put("listCount", result.getContentResult().getListCount());
                    contentMap.put("tableCount", result.getContentResult().getTableCount());
                    responseData.put("contentResult", contentMap);
                }

                return JsonUtil.toJson(responseData);
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen des Analyseergebnisses: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen des Analyseergebnisses: " + e.getMessage()));
            }
        });

        // Route zum Herunterladen eines PDF-Berichts
        get("/api/analysis/:id/report", (req, res) -> {
            try {
                Long id = Long.parseLong(req.params(":id"));
                AnalysisResult result = analysisResultRepository.findById(id);

                if (result == null || result.getPdfReportPath() == null) {
                    res.status(404);
                    return "Bericht nicht gefunden";
                }

                // Datei als Download anbieten
                String filePath = result.getPdfReportPath();
                java.io.File file = new java.io.File(filePath);

                if (!file.exists()) {
                    res.status(404);
                    return "Berichtsdatei nicht gefunden";
                }

                res.type("application/pdf");
                res.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

                // Datei streamen
                return new java.io.FileInputStream(file);
            } catch (NumberFormatException e) {
                res.status(400);
                return "Ungültige ID";
            } catch (Exception e) {
                logger.severe("Fehler beim Herunterladen des Berichts: " + e.getMessage());
                res.status(500);
                return "Fehler beim Herunterladen des Berichts: " + e.getMessage();
            }
        });

        // Route für direkten Zugriff auf PDF-Berichte im Reports-Verzeichnis
        get("/api/reports/:filename", (req, res) -> {
            try {
                String filename = req.params(":filename");
                String filePath = Paths.get("reports", filename).toString();
                File file = new File(filePath);

                if (!file.exists()) {
                    logger.warning("Datei nicht gefunden: " + filePath);
                    res.status(404);
                    return "Berichtsdatei nicht gefunden";
                }

                logger.info("PDF-Datei gefunden: " + filePath);
                res.type("application/pdf");
                res.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");

                // Datei streamen
                return new java.io.FileInputStream(file);
            } catch (Exception e) {
                logger.severe("Fehler beim Herunterladen des Berichts: " + e.getMessage());
                res.status(500);
                return "Fehler beim Herunterladen des Berichts: " + e.getMessage();
            }
        });
    }
}
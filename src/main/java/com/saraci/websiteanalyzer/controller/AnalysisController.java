package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.model.AnalysisResult;
import com.saraci.websiteanalyzer.model.Website;
import com.saraci.websiteanalyzer.repository.AnalysisResultRepository;
import com.saraci.websiteanalyzer.repository.WebsiteRepository;
import com.saraci.websiteanalyzer.service.WebsiteAnalyzerService;
import com.saraci.websiteanalyzer.util.JsonUtil;

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

                // Erfolg zurückgeben
                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Analyse erfolgreich abgeschlossen",
                                "analysisId", result.getId(),
                                "url", url,
                                "pdfPath", pdfPath
                        )
                );
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

                return JsonUtil.toJson(result);
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen des Analyseergebnisses: " + e.getMessage());
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
    }
}
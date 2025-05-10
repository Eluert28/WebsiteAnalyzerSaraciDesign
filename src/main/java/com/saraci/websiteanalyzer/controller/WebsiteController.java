package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.model.AnalysisResult;
import com.saraci.websiteanalyzer.model.Website;
import com.saraci.websiteanalyzer.repository.AnalysisResultRepository;
import com.saraci.websiteanalyzer.repository.WebsiteRepository;
import com.saraci.websiteanalyzer.util.JsonUtil;

import java.util.List;
import java.util.logging.Logger;

import static spark.Spark.*;

/**
 * Controller für die Website-Funktionalität.
 */
public class WebsiteController implements Controller {
    private static final Logger logger = Logger.getLogger(WebsiteController.class.getName());

    private final WebsiteRepository websiteRepository;
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public WebsiteController(WebsiteRepository websiteRepository,
                             AnalysisResultRepository analysisResultRepository) {
        this.websiteRepository = websiteRepository;
        this.analysisResultRepository = analysisResultRepository;
    }

    @Override
    public void registerRoutes() {
        // Route zum Abrufen aller Websites
        get("/api/websites", (req, res) -> {
            res.type("application/json");

            try {
                List<Website> websites = websiteRepository.findAll();
                return JsonUtil.toJson(websites);
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen der Websites: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Websites: " + e.getMessage()));
            }
        });

        // Route zum Abrufen einer Website nach ID
        get("/api/websites/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                Website website = websiteRepository.findById(id);

                if (website == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Website nicht gefunden"));
                }

                return JsonUtil.toJson(website);
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen der Website: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Website: " + e.getMessage()));
            }
        });

        // Route zum Abrufen der Analyse-Historie einer Website nach URL
        get("/api/websites/url/:url/history", (req, res) -> {
            res.type("application/json");

            try {
                String url = req.params(":url");

                // URL-Dekodierung
                url = java.net.URLDecoder.decode(url, "UTF-8");
                logger.info("Verarbeite Anfrage für URL-Historie: " + url);

                Website website = websiteRepository.findByUrl(url);

                if (website == null) {
                    logger.warning("Website nicht gefunden: " + url);
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Website nicht gefunden"));
                }

                logger.info("Website gefunden mit ID: " + website.getId());
                List<AnalysisResult> results = analysisResultRepository.findByWebsiteId(website.getId());
                logger.info("Anzahl der gefundenen Analyseergebnisse: " + results.size());

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Analyse-Historie abgerufen",
                                "url", url,
                                "website", website,
                                "analyses", results
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen der Analyse-Historie: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Analyse-Historie: " + e.getMessage()));
            }
        });

        // Route zum Abrufen der Analyse-Historie einer Website nach ID
        get("/api/websites/:id/history", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                Website website = websiteRepository.findById(id);

                if (website == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Website nicht gefunden"));
                }

                List<AnalysisResult> results = analysisResultRepository.findByWebsiteId(id);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Analyse-Historie abgerufen",
                                "website", website,
                                "analyses", results
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen der Analyse-Historie: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Analyse-Historie: " + e.getMessage()));
            }
        });
    }
}
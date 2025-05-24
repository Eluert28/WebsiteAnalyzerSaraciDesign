package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.service.email.EmailCampaignService;
import com.saraci.websiteanalyzer.util.JsonUtil;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * Controller für E-Mail-Kampagnen.
 */
public class EmailCampaignController implements Controller {
    private static final Logger logger = Logger.getLogger(EmailCampaignController.class.getName());

    private final EmailCampaignService campaignService;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public EmailCampaignController(EmailCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Override
    public void registerRoutes() {
        // Route zum Senden einer Kampagne an alle aktiven Leads
        post("/api/email-campaigns/send-to-all", (req, res) -> {
            res.type("application/json");

            try {
                String requestBody = req.body();
                Long templateId = JsonUtil.getLongValue(requestBody, "templateId");

                // Pflichtfelder prüfen
                if (templateId == null) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Template-ID ist erforderlich"));
                }

                // Kampagne senden
                EmailCampaignService.CampaignResult result = campaignService.sendCampaignToAllLeads(templateId);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Kampagne erfolgreich gesendet",
                                "result", result
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Senden der Kampagne: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Senden der Kampagne: " + e.getMessage()));
            }
        });

        // Route zum Senden einer Kampagne an spezifische Leads
        post("/api/email-campaigns/send-to-leads", (req, res) -> {
            res.type("application/json");

            try {
                String requestBody = req.body();
                Long templateId = JsonUtil.getLongValue(requestBody, "templateId");
                String leadIdsString = JsonUtil.getStringValue(requestBody, "leadIds");

                // Pflichtfelder prüfen
                if (templateId == null || leadIdsString == null || leadIdsString.trim().isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Template-ID und Lead-IDs sind erforderlich"));
                }

                // Lead-IDs parsen
                List<Long> leadIds;
                try {
                    leadIds = Arrays.stream(leadIdsString.split(","))
                            .map(String::trim)
                            .map(Long::parseLong)
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Ungültige Lead-IDs Format. Verwenden Sie: '1,2,3'"));
                }

                if (leadIds.isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Mindestens eine Lead-ID ist erforderlich"));
                }

                // Kampagne senden
                EmailCampaignService.CampaignResult result = campaignService.sendCampaignToSpecificLeads(templateId, leadIds);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Gezielte Kampagne erfolgreich gesendet",
                                "result", result
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Senden der gezielten Kampagne: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Senden der Kampagne: " + e.getMessage()));
            }
        });

        // Route zum Senden einer Test-E-Mail
        post("/api/email-campaigns/send-test", (req, res) -> {
            res.type("application/json");

            try {
                String requestBody = req.body();
                Long templateId = JsonUtil.getLongValue(requestBody, "templateId");
                String testEmail = JsonUtil.getStringValue(requestBody, "testEmail");
                String testName = JsonUtil.getStringValue(requestBody, "testName");
                String testCompany = JsonUtil.getStringValue(requestBody, "testCompany");
                String testWebsite = JsonUtil.getStringValue(requestBody, "testWebsite");

                // Pflichtfelder prüfen
                if (templateId == null || testEmail == null || testEmail.trim().isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Template-ID und Test-E-Mail sind erforderlich"));
                }

                // E-Mail-Format validieren (einfache Validierung)
                if (!testEmail.contains("@") || !testEmail.contains(".")) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Ungültige E-Mail-Adresse"));
                }

                // Test-E-Mail senden
                campaignService.sendTestEmail(
                        templateId,
                        testEmail.trim(),
                        testName != null ? testName.trim() : null,
                        testCompany != null ? testCompany.trim() : null,
                        testWebsite != null ? testWebsite.trim() : null
                );

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Test-E-Mail erfolgreich gesendet",
                                "templateId", templateId,
                                "testEmail", testEmail
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Senden der Test-E-Mail: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Senden der Test-E-Mail: " + e.getMessage()));
            }
        });

        // Route für Kampagnen-Status (für zukünftige Erweiterungen)
        get("/api/email-campaigns/status", (req, res) -> {
            res.type("application/json");

            try {
                // Hier könnten wir zukünftig laufende Kampagnen verfolgen
                return JsonUtil.toJson(
                        JsonUtil.success(
                                "E-Mail-Kampagnen-Service ist aktiv",
                                "status", "active",
                                "info", "Bereit für Kampagnen-Versendung"
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen des Kampagnen-Status: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen des Status: " + e.getMessage()));
            }
        });

        // Route für Kampagnen-Hilfe (API-Dokumentation)
        get("/api/email-campaigns/help", (req, res) -> {
            res.type("application/json");

            return JsonUtil.toJson(
                    JsonUtil.success(
                            "E-Mail-Kampagnen API-Hilfe",
                            "endpoints", Arrays.asList(
                                    "POST /api/email-campaigns/send-to-all - Kampagne an alle aktiven Leads",
                                    "POST /api/email-campaigns/send-to-leads - Kampagne an spezifische Leads",
                                    "POST /api/email-campaigns/send-test - Test-E-Mail senden",
                                    "GET /api/email-campaigns/status - Service-Status abrufen"
                            ),
                            "example", "curl -X POST /api/email-campaigns/send-test -d '{\"templateId\":1,\"testEmail\":\"test@example.com\"}'"
                    )
            );
        });
    }
}
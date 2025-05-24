package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.model.EmailTemplate;
import com.saraci.websiteanalyzer.repository.EmailTemplateRepository;
import com.saraci.websiteanalyzer.util.JsonUtil;

import java.util.List;
import java.util.logging.Logger;

import static spark.Spark.*;

/**
 * Controller für die E-Mail-Template-Funktionalität.
 */
public class EmailTemplateController implements Controller {
    private static final Logger logger = Logger.getLogger(EmailTemplateController.class.getName());

    private final EmailTemplateRepository templateRepository;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public EmailTemplateController(EmailTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public void registerRoutes() {
        // Route zum Erstellen eines neuen E-Mail-Templates
        post("/api/email-templates", (req, res) -> {
            res.type("application/json");

            try {
                String requestBody = req.body();
                String name = JsonUtil.getStringValue(requestBody, "name");
                String subject = JsonUtil.getStringValue(requestBody, "subject");
                String body = JsonUtil.getStringValue(requestBody, "body");
                String category = JsonUtil.getStringValue(requestBody, "category");

                // Pflichtfelder prüfen
                if (name == null || name.trim().isEmpty() ||
                        subject == null || subject.trim().isEmpty() ||
                        body == null || body.trim().isEmpty() ||
                        category == null || category.trim().isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Name, Subject, Body und Category sind erforderlich"));
                }

                // Prüfen, ob Template mit diesem Namen bereits existiert
                EmailTemplate existingTemplate = templateRepository.findByName(name.trim());
                if (existingTemplate != null) {
                    res.status(409);
                    return JsonUtil.toJson(JsonUtil.error("Ein Template mit diesem Namen existiert bereits"));
                }

                // Template erstellen
                EmailTemplate template = new EmailTemplate(name.trim(), subject.trim(), body.trim(), category.trim());
                template = templateRepository.save(template);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "E-Mail-Template erfolgreich erstellt",
                                "template", template
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Erstellen des E-Mail-Templates: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Erstellen des Templates: " + e.getMessage()));
            }
        });

        // Route zum Abrufen aller aktiven E-Mail-Templates
        get("/api/email-templates", (req, res) -> {
            res.type("application/json");

            try {
                List<EmailTemplate> templates = templateRepository.findAllActive();
                return JsonUtil.toJson(templates);
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen der E-Mail-Templates: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Templates: " + e.getMessage()));
            }
        });

        // Route zum Abrufen aller E-Mail-Templates (inkl. inaktive)
        get("/api/email-templates/all", (req, res) -> {
            res.type("application/json");

            try {
                List<EmailTemplate> templates = templateRepository.findAll();
                return JsonUtil.toJson(templates);
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen aller E-Mail-Templates: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Templates: " + e.getMessage()));
            }
        });

        // Route zum Abrufen von Templates nach Kategorie
        get("/api/email-templates/category/:category", (req, res) -> {
            res.type("application/json");

            try {
                String category = req.params(":category");
                List<EmailTemplate> templates = templateRepository.findByCategory(category);
                return JsonUtil.toJson(templates);
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen der Templates nach Kategorie: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Templates: " + e.getMessage()));
            }
        });

        // Route zum Abrufen eines E-Mail-Templates nach ID
        get("/api/email-templates/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                EmailTemplate template = templateRepository.findById(id);

                if (template == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("E-Mail-Template nicht gefunden"));
                }

                return JsonUtil.toJson(template);
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen des E-Mail-Templates: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen des Templates: " + e.getMessage()));
            }
        });

        // Route zum Aktualisieren eines E-Mail-Templates
        put("/api/email-templates/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                EmailTemplate template = templateRepository.findById(id);

                if (template == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("E-Mail-Template nicht gefunden"));
                }

                String requestBody = req.body();
                String name = JsonUtil.getStringValue(requestBody, "name");
                String subject = JsonUtil.getStringValue(requestBody, "subject");
                String body = JsonUtil.getStringValue(requestBody, "body");
                String category = JsonUtil.getStringValue(requestBody, "category");
                Boolean active = JsonUtil.getBooleanValue(requestBody, "active");

                // Optionale Felder aktualisieren
                if (name != null && !name.trim().isEmpty()) {
                    // Prüfen, ob ein anderes Template mit diesem Namen existiert
                    EmailTemplate existingTemplate = templateRepository.findByName(name.trim());
                    if (existingTemplate != null && !existingTemplate.getId().equals(id)) {
                        res.status(409);
                        return JsonUtil.toJson(JsonUtil.error("Ein Template mit diesem Namen existiert bereits"));
                    }
                    template.setName(name.trim());
                }

                if (subject != null && !subject.trim().isEmpty()) {
                    template.setSubject(subject.trim());
                }

                if (body != null && !body.trim().isEmpty()) {
                    template.setBody(body.trim());
                }

                if (category != null && !category.trim().isEmpty()) {
                    template.setCategory(category.trim());
                }

                if (active != null) {
                    template.setActive(active);
                }

                // Template aktualisieren
                templateRepository.update(template);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "E-Mail-Template erfolgreich aktualisiert",
                                "template", template
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Aktualisieren des E-Mail-Templates: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Aktualisieren des Templates: " + e.getMessage()));
            }
        });

        // Route zum Deaktivieren eines E-Mail-Templates
        delete("/api/email-templates/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                EmailTemplate template = templateRepository.findById(id);

                if (template == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("E-Mail-Template nicht gefunden"));
                }

                // Template deaktivieren (Soft Delete)
                templateRepository.deactivate(id);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "E-Mail-Template erfolgreich deaktiviert",
                                "id", id
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Deaktivieren des E-Mail-Templates: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Deaktivieren des Templates: " + e.getMessage()));
            }
        });

        // Route zum Aktivieren eines E-Mail-Templates
        post("/api/email-templates/:id/activate", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                EmailTemplate template = templateRepository.findById(id);

                if (template == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("E-Mail-Template nicht gefunden"));
                }

                // Template aktivieren
                templateRepository.activate(id);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "E-Mail-Template erfolgreich aktiviert",
                                "id", id
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Aktivieren des E-Mail-Templates: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Aktivieren des Templates: " + e.getMessage()));
            }
        });

        // Route zum Testen/Verarbeiten eines Templates
        post("/api/email-templates/:id/preview", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                EmailTemplate template = templateRepository.findById(id);

                if (template == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("E-Mail-Template nicht gefunden"));
                }

                String requestBody = req.body();
                String name = JsonUtil.getStringValue(requestBody, "name");
                String email = JsonUtil.getStringValue(requestBody, "email");
                String company = JsonUtil.getStringValue(requestBody, "company");
                String website = JsonUtil.getStringValue(requestBody, "website");

                // Template verarbeiten
                String processedSubject = template.processSubject(name, email, company, website);
                String processedBody = template.processTemplate(name, email, company, website);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Template-Vorschau generiert",
                                "subject", processedSubject,
                                "body", processedBody,
                                "originalTemplate", template
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Verarbeiten des Templates: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Verarbeiten des Templates: " + e.getMessage()));
            }
        });
    }
}
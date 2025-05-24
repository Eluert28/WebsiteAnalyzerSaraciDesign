package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.model.Lead;
import com.saraci.websiteanalyzer.repository.LeadRepository;
import com.saraci.websiteanalyzer.util.JsonUtil;

import java.util.List;
import java.util.logging.Logger;

import static spark.Spark.*;

/**
 * Controller für Lead-Management.
 */
public class LeadController implements Controller {
    private static final Logger logger = Logger.getLogger(LeadController.class.getName());

    private final LeadRepository leadRepository;

    public LeadController(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Override
    public void registerRoutes() {
        // Route zum Erstellen/Aktualisieren eines Leads
        post("/api/leads", (req, res) -> {
            res.type("application/json");

            try {
                String requestBody = req.body();
                String email = JsonUtil.getStringValue(requestBody, "email");
                String name = JsonUtil.getStringValue(requestBody, "name");
                String company = JsonUtil.getStringValue(requestBody, "company");
                String website = JsonUtil.getStringValue(requestBody, "website");

                // Validierung
                if (email == null || email.trim().isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("E-Mail-Adresse ist erforderlich"));
                }

                // Lead erstellen
                Lead lead = new Lead(email, name);
                lead.setCompany(company);
                lead.setWebsite(website);

                // Lead speichern
                lead = leadRepository.save(lead);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Lead erfolgreich gespeichert",
                                "lead", lead
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Speichern des Leads: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Speichern des Leads: " + e.getMessage()));
            }
        });

        // Route zum Abrufen aller aktiven Leads
        get("/api/leads", (req, res) -> {
            res.type("application/json");

            try {
                List<Lead> leads = leadRepository.findAllActive();
                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Leads erfolgreich abgerufen",
                                "leads", leads,
                                "count", leads.size()
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen der Leads: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Leads: " + e.getMessage()));
            }
        });

        // Route zum Abrufen eines Leads nach ID
        get("/api/leads/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                Lead lead = leadRepository.findById(id);

                if (lead == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Lead nicht gefunden"));
                }

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Lead erfolgreich abgerufen",
                                "lead", lead
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen des Leads: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen des Leads: " + e.getMessage()));
            }
        });

        // Route zum Deaktivieren eines Leads (Abmeldung)
        delete("/api/leads/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                Lead lead = leadRepository.findById(id);

                if (lead == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Lead nicht gefunden"));
                }

                leadRepository.deactivate(id);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Lead erfolgreich deaktiviert",
                                "id", id
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Deaktivieren des Leads: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Deaktivieren des Leads: " + e.getMessage()));
            }
        });

        logger.info("Lead-Controller-Routen registriert");
    }
}
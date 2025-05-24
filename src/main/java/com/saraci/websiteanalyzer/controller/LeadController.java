package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.model.Lead;
import com.saraci.websiteanalyzer.repository.LeadRepository;
import com.saraci.websiteanalyzer.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import static spark.Spark.*;

/**
 * Controller für die Lead-Funktionalität.
 */
public class LeadController implements Controller {
    private static final Logger logger = Logger.getLogger(LeadController.class.getName());

    private final LeadRepository leadRepository;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public LeadController(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Override
    public void registerRoutes() {
        // Route zum Erstellen eines neuen Leads
        post("/api/leads", (req, res) -> {
            res.type("application/json");

            try {
                String requestBody = req.body();
                String name = JsonUtil.getStringValue(requestBody, "name");
                String email = JsonUtil.getStringValue(requestBody, "email");
                String company = JsonUtil.getStringValue(requestBody, "company");
                String website = JsonUtil.getStringValue(requestBody, "website");
                String phone = JsonUtil.getStringValue(requestBody, "phone");
                String status = JsonUtil.getStringValue(requestBody, "status");
                String source = JsonUtil.getStringValue(requestBody, "source");
                String notes = JsonUtil.getStringValue(requestBody, "notes");

                // Validierung
                if (name == null || name.trim().isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Name ist erforderlich"));
                }

                if (email == null || email.trim().isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("E-Mail ist erforderlich"));
                }

                // Prüfen, ob E-Mail bereits existiert
                Lead existingLead = leadRepository.findByEmail(email);
                if (existingLead != null) {
                    res.status(409);
                    return JsonUtil.toJson(JsonUtil.error("Ein Lead mit dieser E-Mail-Adresse existiert bereits"));
                }

                // Lead erstellen
                Lead lead = new Lead();
                lead.setName(name.trim());
                lead.setEmail(email.trim().toLowerCase());
                lead.setCompany(company != null ? company.trim() : null);
                lead.setWebsite(website != null ? website.trim() : null);
                lead.setPhone(phone != null ? phone.trim() : null);
                lead.setStatus(status != null ? status.trim() : "NEW");
                lead.setSource(source != null ? source.trim() : "MANUAL");
                lead.setNotes(notes != null ? notes.trim() : null);

                LocalDateTime now = LocalDateTime.now();
                lead.setCreatedDate(now);
                lead.setUpdatedDate(now);
                lead.setDeleted(false);

                // Lead speichern
                lead = leadRepository.save(lead);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Lead erfolgreich erstellt",
                                "lead", lead
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Erstellen des Leads: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Erstellen des Leads: " + e.getMessage()));
            }
        });

        // Route zum Aktualisieren eines bestehenden Leads
        put("/api/leads/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));

                // Prüfen, ob Lead existiert
                Lead existingLead = leadRepository.findById(id);
                if (existingLead == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Lead nicht gefunden"));
                }

                String requestBody = req.body();
                String name = JsonUtil.getStringValue(requestBody, "name");
                String email = JsonUtil.getStringValue(requestBody, "email");
                String company = JsonUtil.getStringValue(requestBody, "company");
                String website = JsonUtil.getStringValue(requestBody, "website");
                String phone = JsonUtil.getStringValue(requestBody, "phone");
                String status = JsonUtil.getStringValue(requestBody, "status");
                String source = JsonUtil.getStringValue(requestBody, "source");
                String notes = JsonUtil.getStringValue(requestBody, "notes");

                // Validierung
                if (name == null || name.trim().isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Name ist erforderlich"));
                }

                if (email == null || email.trim().isEmpty()) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("E-Mail ist erforderlich"));
                }

                // Prüfen, ob E-Mail bereits von einem anderen Lead verwendet wird
                Lead leadWithSameEmail = leadRepository.findByEmail(email.trim().toLowerCase());
                if (leadWithSameEmail != null && !leadWithSameEmail.getId().equals(id)) {
                    res.status(409);
                    return JsonUtil.toJson(JsonUtil.error("Ein anderer Lead verwendet bereits diese E-Mail-Adresse"));
                }

                // Lead aktualisieren
                existingLead.setName(name.trim());
                existingLead.setEmail(email.trim().toLowerCase());
                existingLead.setCompany(company != null ? company.trim() : null);
                existingLead.setWebsite(website != null ? website.trim() : null);
                existingLead.setPhone(phone != null ? phone.trim() : null);
                existingLead.setStatus(status != null ? status.trim() : existingLead.getStatus());
                existingLead.setSource(source != null ? source.trim() : existingLead.getSource());
                existingLead.setNotes(notes != null ? notes.trim() : null);

                // Lead in Datenbank aktualisieren
                Lead updatedLead = leadRepository.update(existingLead);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Lead erfolgreich aktualisiert",
                                "lead", updatedLead
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Aktualisieren des Leads: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Aktualisieren des Leads: " + e.getMessage()));
            }
        });

        // Route zum Abrufen aller Leads
        get("/api/leads", (req, res) -> {
            res.type("application/json");

            try {
                String status = req.queryParams("status");
                List<Lead> leads;

                if (status != null && !status.isEmpty()) {
                    leads = leadRepository.findByStatus(status);
                } else {
                    leads = leadRepository.findAll();
                }

                return JsonUtil.toJson(leads);
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

                return JsonUtil.toJson(lead);
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen des Leads: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen des Leads: " + e.getMessage()));
            }
        });

        // Route zum Löschen eines Leads (Soft Delete)
        delete("/api/leads/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));

                // Prüfen, ob Lead existiert
                Lead lead = leadRepository.findById(id);
                if (lead == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Lead nicht gefunden"));
                }

                // Lead löschen (Soft Delete)
                leadRepository.deleteById(id);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Lead erfolgreich gelöscht",
                                "id", id
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Löschen des Leads: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Löschen des Leads: " + e.getMessage()));
            }
        });

        // Route zum Abrufen der Lead-Statistiken
        get("/api/leads/stats", (req, res) -> {
            res.type("application/json");

            try {
                long totalLeads = leadRepository.countAll();

                // Leads nach Status gruppieren
                List<Lead> newLeads = leadRepository.findByStatus("NEW");
                List<Lead> qualifiedLeads = leadRepository.findByStatus("QUALIFIED");
                List<Lead> contactedLeads = leadRepository.findByStatus("CONTACTED");
                List<Lead> convertedLeads = leadRepository.findByStatus("CONVERTED");

                return JsonUtil.toJson(JsonUtil.success(
                        "Lead-Statistiken abgerufen",
                        "total", totalLeads,
                        "new", newLeads.size(),
                        "qualified", qualifiedLeads.size(),
                        "contacted", contactedLeads.size(),
                        "converted", convertedLeads.size()
                ));
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen der Lead-Statistiken: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Lead-Statistiken: " + e.getMessage()));
            }
        });
    }
}
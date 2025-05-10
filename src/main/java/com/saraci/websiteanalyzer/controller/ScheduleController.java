package com.saraci.websiteanalyzer.controller;

import com.saraci.websiteanalyzer.model.AnalysisSchedule;
import com.saraci.websiteanalyzer.model.Website;
import com.saraci.websiteanalyzer.repository.ScheduleRepository;
import com.saraci.websiteanalyzer.repository.WebsiteRepository;
import com.saraci.websiteanalyzer.service.scheduler.SchedulerService;
import com.saraci.websiteanalyzer.util.JsonUtil;

import java.util.List;
import java.util.logging.Logger;

import static spark.Spark.*;

/**
 * Controller für die Zeitplan-Funktionalität.
 */
public class ScheduleController implements Controller {
    private static final Logger logger = Logger.getLogger(ScheduleController.class.getName());

    private final ScheduleRepository scheduleRepository;
    private final WebsiteRepository websiteRepository;
    private final SchedulerService schedulerService;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public ScheduleController(ScheduleRepository scheduleRepository,
                              WebsiteRepository websiteRepository,
                              SchedulerService schedulerService) {
        this.scheduleRepository = scheduleRepository;
        this.websiteRepository = websiteRepository;
        this.schedulerService = schedulerService;
    }

    @Override
    public void registerRoutes() {
        // Route zum Erstellen eines neuen Zeitplans
        post("/api/schedules", (req, res) -> {
            res.type("application/json");

            try {
                String requestBody = req.body();
                Long websiteId = JsonUtil.getLongValue(requestBody, "websiteId");
                String cronExpression = JsonUtil.getStringValue(requestBody, "cronExpression");
                String recipients = JsonUtil.getStringValue(requestBody, "recipients");
                String reportType = JsonUtil.getStringValue(requestBody, "reportType");

                // Pflichtfelder prüfen
                if (websiteId == null || cronExpression == null || recipients == null || reportType == null) {
                    res.status(400);
                    return JsonUtil.toJson(JsonUtil.error("Alle Pflichtfelder müssen ausgefüllt sein"));
                }

                // Prüfen, ob die Website existiert
                Website website = websiteRepository.findById(websiteId);
                if (website == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Website nicht gefunden"));
                }

                // Zeitplan erstellen
                AnalysisSchedule schedule = new AnalysisSchedule();
                schedule.setWebsiteId(websiteId);
                schedule.setCronExpression(cronExpression);
                schedule.setRecipients(recipients);
                schedule.setReportType(reportType);
                schedule.setActive(true);

                // Zeitplan speichern
                schedule = scheduleRepository.save(schedule);

                // Zeitplan beim Scheduler registrieren
                schedulerService.scheduleAnalysis(schedule);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Zeitplan erfolgreich erstellt",
                                "schedule", schedule
                        )
                );
            } catch (Exception e) {
                logger.severe("Fehler beim Erstellen des Zeitplans: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Erstellen des Zeitplans: " + e.getMessage()));
            }
        });

        // Route zum Abrufen aller Zeitpläne
        get("/api/schedules", (req, res) -> {
            res.type("application/json");

            try {
                List<AnalysisSchedule> schedules = scheduleRepository.findAllActive();
                return JsonUtil.toJson(schedules);
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen der Zeitpläne: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen der Zeitpläne: " + e.getMessage()));
            }
        });

        // Route zum Abrufen eines Zeitplans nach ID
        get("/api/schedules/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                AnalysisSchedule schedule = scheduleRepository.findById(id);

                if (schedule == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Zeitplan nicht gefunden"));
                }

                return JsonUtil.toJson(schedule);
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Abrufen des Zeitplans: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Abrufen des Zeitplans: " + e.getMessage()));
            }
        });

        // Route zum Aktualisieren eines Zeitplans
        put("/api/schedules/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                AnalysisSchedule schedule = scheduleRepository.findById(id);

                if (schedule == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Zeitplan nicht gefunden"));
                }

                String requestBody = req.body();
                String cronExpression = JsonUtil.getStringValue(requestBody, "cronExpression");
                String recipients = JsonUtil.getStringValue(requestBody, "recipients");
                String reportType = JsonUtil.getStringValue(requestBody, "reportType");
                Boolean isActive = JsonUtil.getBooleanValue(requestBody, "isActive");

                // Optionale Felder aktualisieren
                if (cronExpression != null) {
                    schedule.setCronExpression(cronExpression);
                }

                if (recipients != null) {
                    schedule.setRecipients(recipients);
                }

                if (reportType != null) {
                    schedule.setReportType(reportType);
                }

                if (isActive != null) {
                    schedule.setActive(isActive);
                }

                // Zeitplan aktualisieren
                scheduleRepository.update(schedule);

                // Zeitplan beim Scheduler aktualisieren
                if (schedule.isActive()) {
                    schedulerService.rescheduleAnalysis(schedule);
                } else {
                    schedulerService.unscheduleAnalysis(schedule.getId());
                }

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Zeitplan erfolgreich aktualisiert",
                                "schedule", schedule
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Aktualisieren des Zeitplans: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Aktualisieren des Zeitplans: " + e.getMessage()));
            }
        });

        // Route zum Löschen eines Zeitplans
        delete("/api/schedules/:id", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                AnalysisSchedule schedule = scheduleRepository.findById(id);

                if (schedule == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Zeitplan nicht gefunden"));
                }

                // Zeitplan beim Scheduler entfernen
                schedulerService.unscheduleAnalysis(id);

                // Zeitplan aus der Datenbank löschen
                scheduleRepository.deleteById(id);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Zeitplan erfolgreich gelöscht",
                                "id", id
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Löschen des Zeitplans: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Löschen des Zeitplans: " + e.getMessage()));
            }
        });

        // Route zum manuellen Ausführen eines Zeitplans
        post("/api/schedules/:id/execute", (req, res) -> {
            res.type("application/json");

            try {
                Long id = Long.parseLong(req.params(":id"));
                AnalysisSchedule schedule = scheduleRepository.findById(id);

                if (schedule == null) {
                    res.status(404);
                    return JsonUtil.toJson(JsonUtil.error("Zeitplan nicht gefunden"));
                }

                // Zeitplan manuell ausführen
                schedulerService.executeNow(schedule);

                return JsonUtil.toJson(
                        JsonUtil.success(
                                "Zeitplan wird ausgeführt",
                                "id", id
                        )
                );
            } catch (NumberFormatException e) {
                res.status(400);
                return JsonUtil.toJson(JsonUtil.error("Ungültige ID"));
            } catch (Exception e) {
                logger.severe("Fehler beim Ausführen des Zeitplans: " + e.getMessage());
                res.status(500);
                return JsonUtil.toJson(JsonUtil.error("Fehler beim Ausführen des Zeitplans: " + e.getMessage()));
            }
        });
    }
}
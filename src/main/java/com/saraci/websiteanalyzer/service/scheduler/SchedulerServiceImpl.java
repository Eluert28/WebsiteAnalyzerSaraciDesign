package com.saraci.websiteanalyzer.service.scheduler;

import com.saraci.websiteanalyzer.model.AnalysisResult;
import com.saraci.websiteanalyzer.model.AnalysisSchedule;
import com.saraci.websiteanalyzer.model.Website;
import com.saraci.websiteanalyzer.repository.AnalysisResultRepository;
import com.saraci.websiteanalyzer.repository.ScheduleRepository;
import com.saraci.websiteanalyzer.repository.WebsiteRepository;
import com.saraci.websiteanalyzer.service.WebsiteAnalyzerService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Implementierung des Scheduler-Services mit ScheduledExecutorService.
 * In einer echten Anwendung würde man hier eine Bibliothek wie Quartz oder cron4j verwenden.
 */
public class SchedulerServiceImpl implements SchedulerService {
    private static final Logger logger = Logger.getLogger(SchedulerServiceImpl.class.getName());

    private final WebsiteAnalyzerService analyzerService;
    private final WebsiteRepository websiteRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final ScheduleRepository scheduleRepository;

    private final ScheduledExecutorService executorService;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public SchedulerServiceImpl(WebsiteAnalyzerService analyzerService,
                                WebsiteRepository websiteRepository,
                                AnalysisResultRepository analysisResultRepository,
                                ScheduleRepository scheduleRepository) {
        this.analyzerService = analyzerService;
        this.websiteRepository = websiteRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.scheduleRepository = scheduleRepository;

        this.executorService = Executors.newScheduledThreadPool(5);
        this.scheduledTasks = new HashMap<>();
    }

    @Override
    public void initialize() throws Exception {
        // Lade alle aktiven Zeitpläne aus der Datenbank
        List<AnalysisSchedule> schedules = scheduleRepository.findAllActive();
        logger.info("Lade " + schedules.size() + " aktive Zeitpläne");

        // Plane jeden Zeitplan
        for (AnalysisSchedule schedule : schedules) {
            scheduleAnalysis(schedule);
        }
    }

    @Override
    public void scheduleAnalysis(AnalysisSchedule schedule) throws Exception {
        // Validiere den Zeitplan
        if (schedule == null || schedule.getCronExpression() == null) {
            throw new IllegalArgumentException("Ungültiger Zeitplan: Cron-Ausdruck ist erforderlich");
        }

        // In einer echten Anwendung würden wir hier eine Cron-Parser-Bibliothek verwenden,
        // um den nächsten Ausführungszeitpunkt basierend auf dem Cron-Ausdruck zu berechnen.
        // Für diese einfache Implementierung verwenden wir einen festen Zeitplan (alle 24 Stunden).

        // Einfache Beispiel-Implementierung: Führe die Analyse alle 24 Stunden aus
        Runnable task = () -> {
            try {
                executeScheduledAnalysis(schedule);
            } catch (Exception e) {
                logger.severe("Fehler bei der geplanten Analyse: " + e.getMessage());
            }
        };

        // Je nach dem Cron-Ausdruck unterschiedliche Intervalle wählen
        long interval = getIntervalFromCronExpression(schedule.getCronExpression());

        // Task planen
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(
                task, 0, interval, TimeUnit.SECONDS
        );

        // Task in der Map speichern
        if (scheduledTasks.containsKey(schedule.getId())) {
            // Falls der Task bereits existiert, abbrechen und neu planen
            scheduledTasks.get(schedule.getId()).cancel(false);
        }

        scheduledTasks.put(schedule.getId(), future);
        logger.info("Analyse für Website-ID " + schedule.getWebsiteId() +
                " geplant mit Intervall " + (interval / 3600) + " Stunden, Zeitplan-ID: " + schedule.getId());

        // Nächsten Ausführungszeitpunkt berechnen und speichern
        schedule.setNextRun(LocalDateTime.now().plusSeconds(interval));
        scheduleRepository.update(schedule);
    }

    /**
     * Hilfsmethode, um ein Intervall aus einem Cron-Ausdruck zu berechnen.
     * In einer echten Anwendung würde man eine Bibliothek wie Quartz oder cron4j verwenden.
     */
    private long getIntervalFromCronExpression(String cronExpression) {
        // Verarbeite einige Standard-Cron-Ausdrücke
        switch (cronExpression) {
            case "0 * * * *":     // Stündlich
                return 3600;
            case "0 0 * * *":     // Täglich (Mitternacht)
                return 86400;
            case "0 8 * * *":     // Täglich (8 Uhr)
                return 86400;
            case "0 0 * * 1":     // Wöchentlich (Montag, Mitternacht)
            case "0 8 * * 1":     // Wöchentlich (Montag, 8 Uhr)
                return 604800;
            case "0 0 1 * *":     // Monatlich (1. Tag, Mitternacht)
            case "0 8 1 * *":     // Monatlich (1. Tag, 8 Uhr)
                return 2592000;
            default:              // Standard: Täglich
                return 86400;
        }
    }

    @Override
    public void rescheduleAnalysis(AnalysisSchedule schedule) throws Exception {
        // Einfach den alten Zeitplan entfernen und einen neuen hinzufügen
        unscheduleAnalysis(schedule.getId());
        scheduleAnalysis(schedule);
    }

    @Override
    public void unscheduleAnalysis(Long scheduleId) throws Exception {
        if (scheduledTasks.containsKey(scheduleId)) {
            scheduledTasks.get(scheduleId).cancel(false);
            scheduledTasks.remove(scheduleId);
            logger.info("Zeitplan mit ID " + scheduleId + " entfernt");
        }
    }

    @Override
    public void executeNow(AnalysisSchedule schedule) throws Exception {
        // Führe die Analyse sofort in einem separaten Thread aus
        executorService.submit(() -> {
            try {
                executeScheduledAnalysis(schedule);
                logger.info("Manuelle Ausführung des Zeitplans mit ID " + schedule.getId() + " abgeschlossen");
            } catch (Exception e) {
                logger.severe("Fehler bei der manuellen Ausführung des Zeitplans: " + e.getMessage());
            }
        });
    }

    /**
     * Führt eine geplante Analyse aus.
     */
    private void executeScheduledAnalysis(AnalysisSchedule schedule) throws Exception {
        logger.info("Führe geplante Analyse für Zeitplan-ID " + schedule.getId() + " aus");

        try {
            // Finde die Website
            Website website = websiteRepository.findById(schedule.getWebsiteId());

            if (website == null) {
                throw new Exception("Website mit ID " + schedule.getWebsiteId() + " nicht gefunden");
            }

            // Führe die Analyse durch
            AnalysisResult result = analyzerService.analyzeWebsite(website.getUrl());
            result.setWebsiteId(website.getId());

            // Speichere das Ergebnis
            result = analysisResultRepository.save(result);

            // Generiere einen PDF-Bericht
            String pdfPath = analyzerService.generatePdfReport(result);
            analysisResultRepository.updatePdfReportPath(result.getId(), pdfPath);

            // Aktualisiere das letzte Analysedatum der Website
            websiteRepository.updateLastAnalysisDate(website.getId());

            // Aktualisiere den Zeitplan
            schedule.setLastRun(LocalDateTime.now());
            long interval = getIntervalFromCronExpression(schedule.getCronExpression());
            schedule.setNextRun(LocalDateTime.now().plusSeconds(interval));
            scheduleRepository.update(schedule);

            // Sende den Bericht per E-Mail, falls Empfänger definiert sind
            if (schedule.getRecipients() != null && !schedule.getRecipients().isEmpty()) {
                String[] recipients = schedule.getRecipients().split(",");
                for (String recipient : recipients) {
                    analyzerService.sendReportByEmail(pdfPath, recipient.trim(), website.getUrl());
                }
            }

            logger.info("Geplante Analyse erfolgreich durchgeführt: Zeitplan-ID " + schedule.getId() +
                    ", Website: " + website.getUrl());
        } catch (Exception e) {
            logger.severe("Fehler bei der geplanten Analyse für Zeitplan-ID " +
                    schedule.getId() + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void shutdown() {
        try {
            // Alle geplanten Tasks abbrechen
            for (ScheduledFuture<?> future : scheduledTasks.values()) {
                future.cancel(true);
            }

            // ExecutorService herunterfahren
            executorService.shutdown();
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }

            logger.info("Scheduler erfolgreich heruntergefahren");
        } catch (InterruptedException e) {
            logger.warning("Fehler beim Herunterfahren des Schedulers: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
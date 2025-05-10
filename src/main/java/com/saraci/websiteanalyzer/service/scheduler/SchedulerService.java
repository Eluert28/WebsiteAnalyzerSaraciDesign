package com.saraci.websiteanalyzer.service.scheduler;

import com.saraci.websiteanalyzer.model.AnalysisSchedule;

/**
 * Schnittstelle für den Scheduler-Service.
 */
public interface SchedulerService {

    /**
     * Initialisiert den Scheduler und lädt alle aktiven Zeitpläne.
     */
    void initialize() throws Exception;

    /**
     * Plant eine Website-Analyse basierend auf einem Zeitplan.
     *
     * @param schedule Der Zeitplan
     * @throws Exception Bei Fehlern in der Planung
     */
    void scheduleAnalysis(AnalysisSchedule schedule) throws Exception;

    /**
     * Aktualisiert einen bestehenden Zeitplan.
     *
     * @param schedule Der aktualisierte Zeitplan
     * @throws Exception Bei Fehlern in der Aktualisierung
     */
    void rescheduleAnalysis(AnalysisSchedule schedule) throws Exception;

    /**
     * Entfernt einen geplanten Zeitplan.
     *
     * @param scheduleId Die ID des Zeitplans
     * @throws Exception Bei Fehlern in der Entfernung
     */
    void unscheduleAnalysis(Long scheduleId) throws Exception;

    /**
     * Führt einen Zeitplan sofort manuell aus.
     *
     * @param schedule Der auszuführende Zeitplan
     * @throws Exception Bei Fehlern in der Ausführung
     */
    void executeNow(AnalysisSchedule schedule) throws Exception;

    /**
     * Beendet den Scheduler und gibt alle Ressourcen frei.
     */
    void shutdown();
}
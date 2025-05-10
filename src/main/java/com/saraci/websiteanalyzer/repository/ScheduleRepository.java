package com.saraci.websiteanalyzer.repository;

import com.saraci.websiteanalyzer.model.AnalysisSchedule;

import java.util.List;

/**
 * Repository-Schnittstelle für die AnalysisSchedule-Entität.
 */
public interface ScheduleRepository {

    /**
     * Speichert einen Zeitplan in der Datenbank.
     *
     * @param schedule Der zu speichernde Zeitplan
     * @return Der gespeicherte Zeitplan mit aktualisierter ID
     * @throws Exception Bei Datenbankfehlern
     */
    AnalysisSchedule save(AnalysisSchedule schedule) throws Exception;

    /**
     * Findet einen Zeitplan anhand seiner ID.
     *
     * @param id Die ID des Zeitplans
     * @return Der gefundene Zeitplan oder null, falls nicht vorhanden
     * @throws Exception Bei Datenbankfehlern
     */
    AnalysisSchedule findById(Long id) throws Exception;

    /**
     * Findet alle Zeitpläne für eine Website.
     *
     * @param websiteId Die ID der Website
     * @return Eine Liste aller Zeitpläne für die Website
     * @throws Exception Bei Datenbankfehlern
     */
    List<AnalysisSchedule> findByWebsiteId(Long websiteId) throws Exception;

    /**
     * Findet alle aktiven Zeitpläne.
     *
     * @return Eine Liste aller aktiven Zeitpläne
     * @throws Exception Bei Datenbankfehlern
     */
    List<AnalysisSchedule> findAllActive() throws Exception;

    /**
     * Aktualisiert den Zeitplan in der Datenbank.
     *
     * @param schedule Der zu aktualisierende Zeitplan
     * @throws Exception Bei Datenbankfehlern
     */
    void update(AnalysisSchedule schedule) throws Exception;

    /**
     * Löscht einen Zeitplan aus der Datenbank.
     *
     * @param id Die ID des zu löschenden Zeitplans
     * @throws Exception Bei Datenbankfehlern
     */
    void deleteById(Long id) throws Exception;
}
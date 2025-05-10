package com.saraci.websiteanalyzer.repository;

import com.saraci.websiteanalyzer.model.Website;

import java.util.List;

/**
 * Repository-Schnittstelle für die Website-Entität.
 */
public interface WebsiteRepository {

    /**
     * Speichert eine Website in der Datenbank.
     *
     * @param website Die zu speichernde Website
     * @return Die gespeicherte Website mit aktualisierter ID
     * @throws Exception Bei Datenbankfehlern
     */
    Website save(Website website) throws Exception;

    /**
     * Findet eine Website anhand ihrer URL.
     *
     * @param url Die URL der Website
     * @return Die gefundene Website oder null, falls nicht vorhanden
     * @throws Exception Bei Datenbankfehlern
     */
    Website findByUrl(String url) throws Exception;

    /**
     * Findet eine Website anhand ihrer ID.
     *
     * @param id Die ID der Website
     * @return Die gefundene Website oder null, falls nicht vorhanden
     * @throws Exception Bei Datenbankfehlern
     */
    Website findById(Long id) throws Exception;

    /**
     * Gibt alle Websites zurück.
     *
     * @return Eine Liste aller Websites
     * @throws Exception Bei Datenbankfehlern
     */
    List<Website> findAll() throws Exception;

    /**
     * Aktualisiert das letzte Analysedatum einer Website.
     *
     * @param id Die ID der Website
     * @throws Exception Bei Datenbankfehlern
     */
    void updateLastAnalysisDate(Long id) throws Exception;
}
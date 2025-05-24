package com.saraci.websiteanalyzer.repository;

import com.saraci.websiteanalyzer.model.Lead;
import java.util.List;

/**
 * Repository-Schnittstelle für Lead-Entitäten.
 */
public interface LeadRepository {

    /**
     * Speichert einen Lead in der Datenbank.
     */
    Lead save(Lead lead) throws Exception;

    /**
     * Findet einen Lead anhand seiner ID.
     */
    Lead findById(Long id) throws Exception;

    /**
     * Findet einen Lead anhand seiner E-Mail-Adresse.
     */
    Lead findByEmail(String email) throws Exception;

    /**
     * Gibt alle aktiven Leads zurück.
     */
    List<Lead> findAllActive() throws Exception;

    /**
     * Deaktiviert einen Lead (für Abmeldungen).
     */
    void deactivate(Long id) throws Exception;
}
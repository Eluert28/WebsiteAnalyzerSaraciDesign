package com.saraci.websiteanalyzer.repository;

import com.saraci.websiteanalyzer.model.Lead;

import java.util.List;

/**
 * Repository-Schnittstelle für die Lead-Entität.
 */
public interface LeadRepository {

    /**
     * Speichert einen Lead in der Datenbank.
     *
     * @param lead Der zu speichernde Lead
     * @return Der gespeicherte Lead mit aktualisierter ID
     * @throws Exception Bei Datenbankfehlern
     */
    Lead save(Lead lead) throws Exception;

    /**
     * Aktualisiert einen bestehenden Lead in der Datenbank.
     *
     * @param lead Der zu aktualisierende Lead
     * @return Der aktualisierte Lead
     * @throws Exception Bei Datenbankfehlern
     */
    Lead update(Lead lead) throws Exception;

    /**
     * Findet einen Lead anhand seiner ID.
     *
     * @param id Die ID des Leads
     * @return Der gefundene Lead oder null, falls nicht vorhanden
     * @throws Exception Bei Datenbankfehlern
     */
    Lead findById(Long id) throws Exception;

    /**
     * Findet einen Lead anhand seiner E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse des Leads
     * @return Der gefundene Lead oder null, falls nicht vorhanden
     * @throws Exception Bei Datenbankfehlern
     */
    Lead findByEmail(String email) throws Exception;

    /**
     * Gibt alle aktiven Leads zurück (nicht gelöscht).
     *
     * @return Eine Liste aller aktiven Leads
     * @throws Exception Bei Datenbankfehlern
     */
    List<Lead> findAll() throws Exception;

    /**
     * Gibt alle Leads mit einem bestimmten Status zurück.
     *
     * @param status Der Status der Leads
     * @return Eine Liste der Leads mit dem angegebenen Status
     * @throws Exception Bei Datenbankfehlern
     */
    List<Lead> findByStatus(String status) throws Exception;

    /**
     * Markiert einen Lead als gelöscht (Soft Delete).
     *
     * @param id Die ID des zu löschenden Leads
     * @throws Exception Bei Datenbankfehlern
     */
    void deleteById(Long id) throws Exception;

    /**
     * Deaktiviert einen Lead (Alias für deleteById - Soft Delete).
     *
     * @param id Die ID des zu deaktivierenden Leads
     * @throws Exception Bei Datenbankfehlern
     */
    void deactivate(Long id) throws Exception;

    /**
     * Gibt alle aktiven Leads zurück (nicht gelöscht).
     * Alias für findAll() für bessere Klarheit.
     *
     * @return Eine Liste aller aktiven Leads
     * @throws Exception Bei Datenbankfehlern
     */
    List<Lead> findAllActive() throws Exception;

    /**
     * Zählt die Anzahl aller aktiven Leads.
     *
     * @return Anzahl der aktiven Leads
     * @throws Exception Bei Datenbankfehlern
     */
    long countAll() throws Exception;
}
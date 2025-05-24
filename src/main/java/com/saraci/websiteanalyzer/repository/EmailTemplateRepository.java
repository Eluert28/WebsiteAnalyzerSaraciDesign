package com.saraci.websiteanalyzer.repository;

import com.saraci.websiteanalyzer.model.EmailTemplate;

import java.util.List;

/**
 * Repository-Schnittstelle für die EmailTemplate-Entität.
 */
public interface EmailTemplateRepository {

    /**
     * Speichert ein E-Mail-Template in der Datenbank.
     *
     * @param template Das zu speichernde Template
     * @return Das gespeicherte Template mit aktualisierter ID
     * @throws Exception Bei Datenbankfehlern
     */
    EmailTemplate save(EmailTemplate template) throws Exception;

    /**
     * Findet ein E-Mail-Template anhand seiner ID.
     *
     * @param id Die ID des Templates
     * @return Das gefundene Template oder null, falls nicht vorhanden
     * @throws Exception Bei Datenbankfehlern
     */
    EmailTemplate findById(Long id) throws Exception;

    /**
     * Findet ein E-Mail-Template anhand seines Namens.
     *
     * @param name Der Name des Templates
     * @return Das gefundene Template oder null, falls nicht vorhanden
     * @throws Exception Bei Datenbankfehlern
     */
    EmailTemplate findByName(String name) throws Exception;

    /**
     * Gibt alle aktiven E-Mail-Templates zurück.
     *
     * @return Eine Liste aller aktiven Templates
     * @throws Exception Bei Datenbankfehlern
     */
    List<EmailTemplate> findAllActive() throws Exception;

    /**
     * Gibt alle E-Mail-Templates einer Kategorie zurück.
     *
     * @param category Die Kategorie
     * @return Eine Liste aller Templates der Kategorie
     * @throws Exception Bei Datenbankfehlern
     */
    List<EmailTemplate> findByCategory(String category) throws Exception;

    /**
     * Gibt alle E-Mail-Templates zurück (aktive und inaktive).
     *
     * @return Eine Liste aller Templates
     * @throws Exception Bei Datenbankfehlern
     */
    List<EmailTemplate> findAll() throws Exception;

    /**
     * Aktualisiert ein E-Mail-Template.
     *
     * @param template Das zu aktualisierende Template
     * @throws Exception Bei Datenbankfehlern
     */
    void update(EmailTemplate template) throws Exception;

    /**
     * Markiert ein Template als verwendet und aktualisiert die Statistiken.
     *
     * @param id Die ID des Templates
     * @throws Exception Bei Datenbankfehlern
     */
    void markAsUsed(Long id) throws Exception;

    /**
     * Deaktiviert ein E-Mail-Template (Soft Delete).
     *
     * @param id Die ID des zu deaktivierenden Templates
     * @throws Exception Bei Datenbankfehlern
     */
    void deactivate(Long id) throws Exception;

    /**
     * Aktiviert ein E-Mail-Template.
     *
     * @param id Die ID des zu aktivierenden Templates
     * @throws Exception Bei Datenbankfehlern
     */
    void activate(Long id) throws Exception;
}
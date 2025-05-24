package com.saraci.websiteanalyzer.test;

import com.saraci.websiteanalyzer.config.AppConfig;
import com.saraci.websiteanalyzer.model.Lead;
import com.saraci.websiteanalyzer.repository.LeadRepository;

import java.util.List;

/**
 * Test-Klasse für die Lead-Funktionalität.
 */
public class LeadTest {
    public static void main(String[] args) {
        try {
            System.out.println("=== Lead-Funktionalität Test ===");

            // AppConfig initialisieren (lädt Datenbank)
            AppConfig appConfig = AppConfig.getInstance();
            LeadRepository leadRepository = appConfig.getLeadRepository();

            // Test 1: Lead erstellen
            System.out.println("\n1. Lead erstellen...");
            Lead lead = new Lead("test@example.com", "Max Mustermann");
            lead.setCompany("Muster GmbH");
            lead.setWebsite("https://muster.de");

            lead = leadRepository.save(lead);
            System.out.println("Lead erstellt: " + lead);

            // Test 2: Lead nach E-Mail suchen
            System.out.println("\n2. Lead nach E-Mail suchen...");
            Lead foundLead = leadRepository.findByEmail("test@example.com");
            System.out.println("Gefundener Lead: " + foundLead);

            // Test 3: Alle aktiven Leads abrufen
            System.out.println("\n3. Alle aktiven Leads abrufen...");
            List<Lead> activeLeads = leadRepository.findAllActive();
            System.out.println("Anzahl aktiver Leads: " + activeLeads.size());
            activeLeads.forEach(System.out::println);

            // Test 4: Lead aktualisieren (gleiche E-Mail)
            System.out.println("\n4. Lead aktualisieren...");
            Lead updateLead = new Lead("test@example.com", "Max Mustermann (Updated)");
            updateLead.setCompany("Neue Muster GmbH");
            updateLead.setWebsite("https://neue-muster.de");

            updateLead = leadRepository.save(updateLead);
            System.out.println("Lead aktualisiert: " + updateLead);

            // Test 5: Zweiten Lead erstellen
            System.out.println("\n5. Zweiten Lead erstellen...");
            Lead lead2 = new Lead("maria@example.com", "Maria Schmidt");
            lead2.setCompany("Schmidt & Co");
            lead2 = leadRepository.save(lead2);
            System.out.println("Zweiter Lead erstellt: " + lead2);

            // Test 6: Lead deaktivieren
            System.out.println("\n6. Lead deaktivieren...");
            leadRepository.deactivate(lead2.getId());
            System.out.println("Lead mit ID " + lead2.getId() + " deaktiviert");

            // Test 7: Nur aktive Leads abrufen
            System.out.println("\n7. Nur aktive Leads abrufen...");
            List<Lead> activeLeadsAfterDeactivation = leadRepository.findAllActive();
            System.out.println("Anzahl aktiver Leads nach Deaktivierung: " + activeLeadsAfterDeactivation.size());
            activeLeadsAfterDeactivation.forEach(System.out::println);

            System.out.println("\n=== Test erfolgreich abgeschlossen! ===");

        } catch (Exception e) {
            System.err.println("Fehler beim Test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
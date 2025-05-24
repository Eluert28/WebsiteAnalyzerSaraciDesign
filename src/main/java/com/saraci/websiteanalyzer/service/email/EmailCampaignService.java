package com.saraci.websiteanalyzer.service.email;

import com.saraci.websiteanalyzer.model.EmailTemplate;
import com.saraci.websiteanalyzer.model.Lead;
import com.saraci.websiteanalyzer.repository.EmailTemplateRepository;
import com.saraci.websiteanalyzer.repository.LeadRepository;
import com.saraci.websiteanalyzer.service.report.EmailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Service für E-Mail-Kampagnen und Bulk-E-Mail-Versand.
 */
public class EmailCampaignService {
    private static final Logger logger = Logger.getLogger(EmailCampaignService.class.getName());

    private final EmailSender emailSender;
    private final EmailTemplateRepository templateRepository;
    private final LeadRepository leadRepository;
    private final ExecutorService executorService;

    /**
     * Konstruktor mit Dependency Injection.
     */
    public EmailCampaignService(EmailSender emailSender,
                                EmailTemplateRepository templateRepository,
                                LeadRepository leadRepository) {
        this.emailSender = emailSender;
        this.templateRepository = templateRepository;
        this.leadRepository = leadRepository;
        this.executorService = Executors.newFixedThreadPool(5); // Max 5 parallele E-Mails
    }

    /**
     * Sendet eine E-Mail-Kampagne an alle aktiven Leads.
     *
     * @param templateId Die ID des zu verwendenden Templates
     * @return CampaignResult mit Statistiken
     * @throws Exception Bei Fehlern in der Kampagne
     */
    public CampaignResult sendCampaignToAllLeads(Long templateId) throws Exception {
        logger.info("Starte E-Mail-Kampagne mit Template-ID: " + templateId);

        // Template laden
        EmailTemplate template = templateRepository.findById(templateId);
        if (template == null) {
            throw new Exception("E-Mail-Template mit ID " + templateId + " nicht gefunden");
        }

        // Alle aktiven Leads laden
        List<Lead> leads = leadRepository.findAllActive();
        if (leads.isEmpty()) {
            logger.warning("Keine aktiven Leads für Kampagne gefunden");
            return new CampaignResult(0, 0, 0, "Keine aktiven Leads vorhanden");
        }

        return sendCampaignToLeads(template, leads);
    }

    /**
     * Sendet eine E-Mail-Kampagne an eine spezifische Liste von Leads.
     *
     * @param templateId Die ID des zu verwendenden Templates
     * @param leadIds Liste der Lead-IDs
     * @return CampaignResult mit Statistiken
     * @throws Exception Bei Fehlern in der Kampagne
     */
    public CampaignResult sendCampaignToSpecificLeads(Long templateId, List<Long> leadIds) throws Exception {
        logger.info("Starte gezielte E-Mail-Kampagne mit Template-ID: " + templateId + " für " + leadIds.size() + " Leads");

        // Template laden
        EmailTemplate template = templateRepository.findById(templateId);
        if (template == null) {
            throw new Exception("E-Mail-Template mit ID " + templateId + " nicht gefunden");
        }

        // Spezifische Leads laden
        List<Lead> leads = new ArrayList<>();
        for (Long leadId : leadIds) {
            Lead lead = leadRepository.findById(leadId);
            if (lead != null && lead.isActive()) {
                leads.add(lead);
            } else {
                logger.warning("Lead mit ID " + leadId + " nicht gefunden oder inaktiv");
            }
        }

        if (leads.isEmpty()) {
            return new CampaignResult(0, 0, leadIds.size(), "Keine gültigen aktiven Leads gefunden");
        }

        return sendCampaignToLeads(template, leads);
    }

    /**
     * Sendet eine Test-E-Mail mit einem Template.
     *
     * @param templateId Die ID des Templates
     * @param testEmail Die Test-E-Mail-Adresse
     * @param testName Optionaler Test-Name
     * @param testCompany Optionale Test-Firma
     * @param testWebsite Optionale Test-Website
     * @throws Exception Bei Fehlern beim E-Mail-Versand
     */
    public void sendTestEmail(Long templateId, String testEmail, String testName,
                              String testCompany, String testWebsite) throws Exception {
        logger.info("Sende Test-E-Mail mit Template-ID: " + templateId + " an: " + testEmail);

        // Template laden
        EmailTemplate template = templateRepository.findById(templateId);
        if (template == null) {
            throw new Exception("E-Mail-Template mit ID " + templateId + " nicht gefunden");
        }

        // Template verarbeiten
        String processedSubject = template.processSubject(testName, testEmail, testCompany, testWebsite);
        String processedBody = template.processTemplate(testName, testEmail, testCompany, testWebsite);

        // E-Mail senden
        emailSender.sendEmail(testEmail, processedSubject, processedBody, null);

        logger.info("Test-E-Mail erfolgreich gesendet an: " + testEmail);
    }

    /**
     * Private Methode für den tatsächlichen Kampagnen-Versand.
     */
    private CampaignResult sendCampaignToLeads(EmailTemplate template, List<Lead> leads) throws Exception {
        logger.info("Sende Kampagne an " + leads.size() + " Leads");

        int totalLeads = leads.size();
        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        // Template als verwendet markieren
        templateRepository.markAsUsed(template.getId());

        // Erstelle asynchrone Tasks für parallelen E-Mail-Versand
        List<CompletableFuture<EmailResult>> futures = new ArrayList<>();

        for (Lead lead : leads) {
            CompletableFuture<EmailResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Template für diesen Lead verarbeiten
                    String processedSubject = template.processSubject(
                            lead.getName(),
                            lead.getEmail(),
                            lead.getCompany(),
                            lead.getWebsite()
                    );
                    String processedBody = template.processTemplate(
                            lead.getName(),
                            lead.getEmail(),
                            lead.getCompany(),
                            lead.getWebsite()
                    );

                    // E-Mail senden
                    emailSender.sendEmail(lead.getEmail(), processedSubject, processedBody, null);

                    logger.fine("E-Mail erfolgreich gesendet an: " + lead.getEmail());
                    return new EmailResult(true, lead.getEmail(), null);
                } catch (Exception e) {
                    logger.warning("Fehler beim Senden an " + lead.getEmail() + ": " + e.getMessage());
                    return new EmailResult(false, lead.getEmail(), e.getMessage());
                }
            }, executorService);

            futures.add(future);
        }

        // Warte auf alle E-Mail-Versendungen
        for (CompletableFuture<EmailResult> future : futures) {
            try {
                EmailResult result = future.get();
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    errorCount++;
                    errors.add("Fehler bei " + result.getEmail() + ": " + result.getErrorMessage());
                }
            } catch (Exception e) {
                errorCount++;
                errors.add("Unbekannter Fehler: " + e.getMessage());
            }
        }

        String summary = String.format("Kampagne abgeschlossen: %d erfolgreich, %d Fehler von %d gesamt",
                successCount, errorCount, totalLeads);

        logger.info(summary);

        return new CampaignResult(totalLeads, successCount, errorCount, summary, errors);
    }

    /**
     * Beendet den Executor Service.
     */
    public void shutdown() {
        executorService.shutdown();
        logger.info("EmailCampaignService heruntergefahren");
    }

    /**
     * Hilfsklasse für E-Mail-Ergebnisse.
     */
    private static class EmailResult {
        private final boolean success;
        private final String email;
        private final String errorMessage;

        public EmailResult(boolean success, String email, String errorMessage) {
            this.success = success;
            this.email = email;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getEmail() {
            return email;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Ergebnis einer E-Mail-Kampagne.
     */
    public static class CampaignResult {
        private final int totalLeads;
        private final int successCount;
        private final int errorCount;
        private final String summary;
        private final List<String> errors;

        public CampaignResult(int totalLeads, int successCount, int errorCount, String summary) {
            this(totalLeads, successCount, errorCount, summary, new ArrayList<>());
        }

        public CampaignResult(int totalLeads, int successCount, int errorCount, String summary, List<String> errors) {
            this.totalLeads = totalLeads;
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.summary = summary;
            this.errors = errors != null ? errors : new ArrayList<>();
        }

        // Getters
        public int getTotalLeads() {
            return totalLeads;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public String getSummary() {
            return summary;
        }

        public List<String> getErrors() {
            return errors;
        }

        public double getSuccessRate() {
            return totalLeads > 0 ? (double) successCount / totalLeads * 100 : 0;
        }

        @Override
        public String toString() {
            return "CampaignResult{" +
                    "totalLeads=" + totalLeads +
                    ", successCount=" + successCount +
                    ", errorCount=" + errorCount +
                    ", successRate=" + String.format("%.1f", getSuccessRate()) + "%" +
                    '}';
        }
    }
}
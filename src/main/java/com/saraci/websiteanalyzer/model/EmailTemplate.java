package com.saraci.websiteanalyzer.model;

import java.time.LocalDateTime;

/**
 * Repräsentiert ein E-Mail-Template für Lead-Kampagnen.
 */
public class EmailTemplate {
    private Long id;
    private String name;
    private String subject;
    private String body;
    private String category; // "welcome", "follow_up", "analysis_report", "newsletter"
    private boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime lastUsed;
    private int usageCount;

    // Konstruktoren
    public EmailTemplate() {
        this.active = true;
        this.createdDate = LocalDateTime.now();
        this.usageCount = 0;
    }

    public EmailTemplate(String name, String subject, String body, String category) {
        this();
        this.name = name;
        this.subject = subject;
        this.body = body;
        this.category = category;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    /**
     * Ersetzt Variablen im Template mit tatsächlichen Werten.
     * Unterstützte Variablen: {name}, {email}, {company}, {website}
     */
    public String processTemplate(String name, String email, String company, String website) {
        String processedSubject = subject
                .replace("{name}", name != null ? name : "")
                .replace("{email}", email != null ? email : "")
                .replace("{company}", company != null ? company : "")
                .replace("{website}", website != null ? website : "");

        String processedBody = body
                .replace("{name}", name != null ? name : "")
                .replace("{email}", email != null ? email : "")
                .replace("{company}", company != null ? company : "")
                .replace("{website}", website != null ? website : "");

        return processedBody;
    }

    /**
     * Verarbeitet den Betreff des Templates.
     */
    public String processSubject(String name, String email, String company, String website) {
        return subject
                .replace("{name}", name != null ? name : "")
                .replace("{email}", email != null ? email : "")
                .replace("{company}", company != null ? company : "")
                .replace("{website}", website != null ? website : "");
    }

    /**
     * Markiert das Template als verwendet und aktualisiert die Statistiken.
     */
    public void markAsUsed() {
        this.lastUsed = LocalDateTime.now();
        this.usageCount++;
    }

    @Override
    public String toString() {
        return "EmailTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", active=" + active +
                ", usageCount=" + usageCount +
                '}';
    }
}
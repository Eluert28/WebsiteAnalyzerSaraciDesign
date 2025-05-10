package com.saraci.websiteanalyzer.model;

import java.time.LocalDateTime;

/**
 * Repräsentiert einen geplanten Analysezeitplan.
 */
public class AnalysisSchedule {
    private Long id;
    private Long websiteId;
    private String cronExpression;
    private String recipients; // Komma-getrennte E-Mail-Adressen
    private String reportType; // "full", "seo", "performance", "security"
    private boolean active;
    private LocalDateTime lastRun;
    private LocalDateTime nextRun;

    // Konstruktoren
    public AnalysisSchedule() {
        this.active = true;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWebsiteId() {
        return websiteId;
    }

    public void setWebsiteId(Long websiteId) {
        this.websiteId = websiteId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastRun() {
        return lastRun;
    }

    public void setLastRun(LocalDateTime lastRun) {
        this.lastRun = lastRun;
    }

    public LocalDateTime getNextRun() {
        return nextRun;
    }

    public void setNextRun(LocalDateTime nextRun) {
        this.nextRun = nextRun;
    }

    @Override
    public String toString() {
        return "AnalysisSchedule{" +
                "id=" + id +
                ", websiteId=" + websiteId +
                ", cronExpression='" + cronExpression + '\'' +
                ", reportType='" + reportType + '\'' +
                ", active=" + active +
                ", nextRun=" + nextRun +
                '}';
    }
}
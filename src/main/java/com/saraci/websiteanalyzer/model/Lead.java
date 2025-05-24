package com.saraci.websiteanalyzer.model;

import java.time.LocalDateTime;

/**
 * Repräsentiert einen Lead im System.
 */
public class Lead {
    private Long id;
    private String name;
    private String email;
    private String company;
    private String website;
    private String phone;
    private String status;
    private String source;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private boolean deleted;

    // Konstruktoren
    public Lead() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.deleted = false;
        this.status = "NEW";
        this.source = "MANUAL";
    }

    public Lead(String name, String email) {
        this();
        this.name = name;
        this.email = email;
    }

    public Lead(String name, String email, String company) {
        this(name, email);
        this.company = company;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    // Hilfsmethoden
    public boolean isActive() {
        return !deleted;
    }

    public void markAsDeleted() {
        this.deleted = true;
        this.updatedDate = LocalDateTime.now();
    }

    public void updateTimestamp() {
        this.updatedDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Lead{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", website='" + website + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                ", source='" + source + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", deleted=" + deleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lead lead = (Lead) o;

        if (id != null ? !id.equals(lead.id) : lead.id != null) return false;
        return email != null ? email.equals(lead.email) : lead.email == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}
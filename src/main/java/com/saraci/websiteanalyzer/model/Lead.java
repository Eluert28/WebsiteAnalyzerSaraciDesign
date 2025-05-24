package com.saraci.websiteanalyzer.model;

import java.time.LocalDateTime;

/**
 * Repräsentiert einen Lead für E-Mail-Marketing.
 */
public class Lead {
    private Long id;
    private String email;
    private String name;
    private String company;
    private String website;
    private LocalDateTime createdDate;
    private boolean active;

    // Konstruktoren
    public Lead() {
        this.createdDate = LocalDateTime.now();
        this.active = true;
    }

    public Lead(String email, String name) {
        this();
        this.email = email;
        this.name = name;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Lead{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", company='" + company + '\'' +
                ", website='" + website + '\'' +
                ", createdDate=" + createdDate +
                ", active=" + active +
                '}';
    }
}
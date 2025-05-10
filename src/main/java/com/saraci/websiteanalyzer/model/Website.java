package com.saraci.websiteanalyzer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert eine analysierte Website.
 */
public class Website {
    private Long id;
    private String url;
    private LocalDateTime firstAnalysisDate;
    private LocalDateTime lastAnalysisDate;
    private List<AnalysisResult> analysisResults;

    // Konstruktoren
    public Website() {
        this.analysisResults = new ArrayList<>();
    }

    public Website(String url) {
        this();
        this.url = url;
        this.firstAnalysisDate = LocalDateTime.now();
        this.lastAnalysisDate = LocalDateTime.now();
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getFirstAnalysisDate() {
        return firstAnalysisDate;
    }

    public void setFirstAnalysisDate(LocalDateTime firstAnalysisDate) {
        this.firstAnalysisDate = firstAnalysisDate;
    }

    public LocalDateTime getLastAnalysisDate() {
        return lastAnalysisDate;
    }

    public void setLastAnalysisDate(LocalDateTime lastAnalysisDate) {
        this.lastAnalysisDate = lastAnalysisDate;
    }

    public List<AnalysisResult> getAnalysisResults() {
        return analysisResults;
    }

    public void setAnalysisResults(List<AnalysisResult> analysisResults) {
        this.analysisResults = analysisResults;
    }

    public void addAnalysisResult(AnalysisResult result) {
        this.analysisResults.add(result);
        this.lastAnalysisDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Website{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", firstAnalysisDate=" + firstAnalysisDate +
                ", lastAnalysisDate=" + lastAnalysisDate +
                ", analysisResults=" + analysisResults.size() +
                '}';
    }
}
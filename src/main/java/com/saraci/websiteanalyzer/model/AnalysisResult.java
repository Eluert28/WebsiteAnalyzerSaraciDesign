package com.saraci.websiteanalyzer.model;

import java.time.LocalDateTime;

/**
 * Repräsentiert das Ergebnis einer Websiteanalyse.
 */
public class AnalysisResult {
    private Long id;
    private Long websiteId;
    private String url;  // Neue Eigenschaft hinzugefügt
    private LocalDateTime analysisDate;
    private String pdfReportPath;
    private SeoResult seoResult;
    private PerformanceResult performanceResult;
    private SecurityResult securityResult;
    private ContentResult contentResult;

    // Konstruktoren
    public AnalysisResult() {
        this.analysisDate = LocalDateTime.now();
    }

    public AnalysisResult(Long websiteId) {
        this();
        this.websiteId = websiteId;
    }

    // Neuer Konstruktor mit URL
    public AnalysisResult(Long websiteId, String url) {
        this(websiteId);
        this.url = url;
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

    // Neue Getter und Setter für URL
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(LocalDateTime analysisDate) {
        this.analysisDate = analysisDate;
    }

    public String getPdfReportPath() {
        return pdfReportPath;
    }

    public void setPdfReportPath(String pdfReportPath) {
        this.pdfReportPath = pdfReportPath;
    }

    public SeoResult getSeoResult() {
        return seoResult;
    }

    public void setSeoResult(SeoResult seoResult) {
        this.seoResult = seoResult;
    }

    public PerformanceResult getPerformanceResult() {
        return performanceResult;
    }

    public void setPerformanceResult(PerformanceResult performanceResult) {
        this.performanceResult = performanceResult;
    }

    public SecurityResult getSecurityResult() {
        return securityResult;
    }

    public void setSecurityResult(SecurityResult securityResult) {
        this.securityResult = securityResult;
    }

    public ContentResult getContentResult() {
        return contentResult;
    }

    public void setContentResult(ContentResult contentResult) {
        this.contentResult = contentResult;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "id=" + id +
                ", websiteId=" + websiteId +
                ", url='" + url + '\'' +  // URL zur toString-Methode hinzugefügt
                ", analysisDate=" + analysisDate +
                ", pdfReportPath='" + pdfReportPath + '\'' +
                '}';
    }
}
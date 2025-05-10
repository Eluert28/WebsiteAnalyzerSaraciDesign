package com.saraci.websiteanalyzer.model;

/**
 * Speichert die Ergebnisse einer Sicherheitsanalyse.
 */
public class SecurityResult {
    private Long analysisId;
    private boolean httpsEnabled;
    private int securityHeadersScore;
    private int cookiesSecurityScore;
    private String securityHeaders;

    // Konstruktoren
    public SecurityResult() {
    }

    // Getter und Setter
    public Long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }

    public boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    public void setHttpsEnabled(boolean httpsEnabled) {
        this.httpsEnabled = httpsEnabled;
    }

    public int getSecurityHeadersScore() {
        return securityHeadersScore;
    }

    public void setSecurityHeadersScore(int securityHeadersScore) {
        this.securityHeadersScore = securityHeadersScore;
    }

    public int getCookiesSecurityScore() {
        return cookiesSecurityScore;
    }

    public void setCookiesSecurityScore(int cookiesSecurityScore) {
        this.cookiesSecurityScore = cookiesSecurityScore;
    }

    public String getSecurityHeaders() {
        return securityHeaders;
    }

    public void setSecurityHeaders(String securityHeaders) {
        this.securityHeaders = securityHeaders;
    }

    @Override
    public String toString() {
        return "SecurityResult{" +
                "analysisId=" + analysisId +
                ", httpsEnabled=" + httpsEnabled +
                ", securityHeadersScore=" + securityHeadersScore +
                '}';
    }
}
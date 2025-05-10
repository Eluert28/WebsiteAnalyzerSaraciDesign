package com.saraci.websiteanalyzer.model;

/**
 * Speichert die Ergebnisse einer SEO-Analyse.
 */
public class SeoResult {
    private Long analysisId;
    private String url;  // Neues Feld für die URL
    private String title;
    private int titleLength;
    private String description;
    private int descriptionLength;
    private String keywords;
    private int h1Count;
    private int h2Count;
    private int h3Count;
    private int imagesTotal;
    private int imagesWithAlt;
    private int imagesWithoutAlt;
    private double altImagePercentage;
    private int internalLinks;
    private int externalLinks;
    private int score;
    private String canonicalUrl;  // Aus dem anderen Code ersichtlich, dass dies benötigt wird

    // Konstruktoren
    public SeoResult() {
    }

    // Getter und Setter
    public Long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }

    // Neue Getter und Setter für das url-Feld
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTitleLength() {
        return titleLength;
    }

    public void setTitleLength(int titleLength) {
        this.titleLength = titleLength;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDescriptionLength() {
        return descriptionLength;
    }

    public void setDescriptionLength(int descriptionLength) {
        this.descriptionLength = descriptionLength;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public int getH1Count() {
        return h1Count;
    }

    public void setH1Count(int h1Count) {
        this.h1Count = h1Count;
    }

    public int getH2Count() {
        return h2Count;
    }

    public void setH2Count(int h2Count) {
        this.h2Count = h2Count;
    }

    public int getH3Count() {
        return h3Count;
    }

    public void setH3Count(int h3Count) {
        this.h3Count = h3Count;
    }

    public int getImagesTotal() {
        return imagesTotal;
    }

    public void setImagesTotal(int imagesTotal) {
        this.imagesTotal = imagesTotal;
    }

    public int getImagesWithAlt() {
        return imagesWithAlt;
    }

    public void setImagesWithAlt(int imagesWithAlt) {
        this.imagesWithAlt = imagesWithAlt;
    }

    public int getImagesWithoutAlt() {
        return imagesWithoutAlt;
    }

    public void setImagesWithoutAlt(int imagesWithoutAlt) {
        this.imagesWithoutAlt = imagesWithoutAlt;
    }

    public double getAltImagePercentage() {
        return altImagePercentage;
    }

    public void setAltImagePercentage(double altImagePercentage) {
        this.altImagePercentage = altImagePercentage;
    }

    public int getInternalLinks() {
        return internalLinks;
    }

    public void setInternalLinks(int internalLinks) {
        this.internalLinks = internalLinks;
    }

    public int getExternalLinks() {
        return externalLinks;
    }

    public void setExternalLinks(int externalLinks) {
        this.externalLinks = externalLinks;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // Getter und Setter für canonicalUrl
    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    /**
     * Berechnet einen SEO-Score basierend auf den verschiedenen Metriken.
     * @return Ein Score zwischen 0 und 100
     */
    public int calculateScore() {
        int score = 0;
        int totalPoints = 5;

        // Titel-Score (optimal: 30-60 Zeichen)
        if (title != null && !title.isEmpty()) {
            if (titleLength >= 30 && titleLength <= 60) {
                score++;
            }
        }

        // Beschreibungs-Score (optimal: 50-160 Zeichen)
        if (description != null && !description.isEmpty()) {
            if (descriptionLength >= 50 && descriptionLength <= 160) {
                score++;
            }
        }

        // H1-Score (optimal: genau ein H1-Tag)
        if (h1Count == 1) {
            score++;
        }

        // Alt-Text-Score (optimal: mindestens 80% der Bilder haben Alt-Text)
        if (imagesTotal > 0 && altImagePercentage >= 80) {
            score++;
        }

        // Link-Score (optimal: sowohl interne als auch externe Links)
        if (internalLinks > 0 && externalLinks > 0) {
            score++;
        }

        // Berechne Prozentsatz
        return (int) Math.round((double) score / totalPoints * 100);
    }

    @Override
    public String toString() {
        return "SeoResult{" +
                "analysisId=" + analysisId +
                ", url='" + url + '\'' +  // Url zum toString hinzufügen
                ", titleLength=" + titleLength +
                ", descriptionLength=" + descriptionLength +
                ", h1Count=" + h1Count +
                ", altImagePercentage=" + altImagePercentage +
                ", score=" + score +
                '}';
    }
}
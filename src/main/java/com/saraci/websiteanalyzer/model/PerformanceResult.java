package com.saraci.websiteanalyzer.model;

/**
 * Speichert die Ergebnisse einer Performance-Analyse.
 */
public class PerformanceResult {
    private Long analysisId;
    private int lighthouseScore;
    private String firstContentfulPaint;
    private String largestContentfulPaint;
    private String timeToInteractive;
    private String totalBlockingTime;
    private String cumulativeLayoutShift;
    private int loadTime; // in Millisekunden

    // Konstruktoren
    public PerformanceResult() {
    }

    // Getter und Setter
    public Long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }

    public int getLighthouseScore() {
        return lighthouseScore;
    }

    public void setLighthouseScore(int lighthouseScore) {
        this.lighthouseScore = lighthouseScore;
    }

    public String getFirstContentfulPaint() {
        return firstContentfulPaint;
    }

    public void setFirstContentfulPaint(String firstContentfulPaint) {
        this.firstContentfulPaint = firstContentfulPaint;
    }

    public String getLargestContentfulPaint() {
        return largestContentfulPaint;
    }

    public void setLargestContentfulPaint(String largestContentfulPaint) {
        this.largestContentfulPaint = largestContentfulPaint;
    }

    public String getTimeToInteractive() {
        return timeToInteractive;
    }

    public void setTimeToInteractive(String timeToInteractive) {
        this.timeToInteractive = timeToInteractive;
    }

    public String getTotalBlockingTime() {
        return totalBlockingTime;
    }

    public void setTotalBlockingTime(String totalBlockingTime) {
        this.totalBlockingTime = totalBlockingTime;
    }

    public String getCumulativeLayoutShift() {
        return cumulativeLayoutShift;
    }

    public void setCumulativeLayoutShift(String cumulativeLayoutShift) {
        this.cumulativeLayoutShift = cumulativeLayoutShift;
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    @Override
    public String toString() {
        return "PerformanceResult{" +
                "analysisId=" + analysisId +
                ", lighthouseScore=" + lighthouseScore +
                ", loadTime=" + loadTime +
                "ms}";
    }
}
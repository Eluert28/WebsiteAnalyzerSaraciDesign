package com.saraci.websiteanalyzer.model;

/**
 * Speichert die Ergebnisse einer Inhaltsanalyse.
 */
public class ContentResult {
    private Long analysisId;
    private int wordCount;
    private int characterCount;
    private double averageWordLength;
    private int paragraphCount;
    private int imageCount;
    private int videoCount;
    private int listCount;
    private int tableCount;

    // Konstruktoren
    public ContentResult() {
    }

    // Getter und Setter
    public Long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }

    public double getAverageWordLength() {
        return averageWordLength;
    }

    public void setAverageWordLength(double averageWordLength) {
        this.averageWordLength = averageWordLength;
    }

    public int getParagraphCount() {
        return paragraphCount;
    }

    public void setParagraphCount(int paragraphCount) {
        this.paragraphCount = paragraphCount;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public int getListCount() {
        return listCount;
    }

    public void setListCount(int listCount) {
        this.listCount = listCount;
    }

    public int getTableCount() {
        return tableCount;
    }

    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }

    @Override
    public String toString() {
        return "ContentResult{" +
                "analysisId=" + analysisId +
                ", wordCount=" + wordCount +
                ", paragraphCount=" + paragraphCount +
                ", imageCount=" + imageCount +
                ", videoCount=" + videoCount +
                '}';
    }
}
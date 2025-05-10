package com.saraci.websiteanalyzer.service.analyzer;

import com.saraci.websiteanalyzer.model.SeoResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.logging.Logger;

/**
 * Implementierung des SEO-Analyzers mit Jsoup.
 */
public class SeoAnalyzerImpl implements SeoAnalyzer {
    private static final Logger logger = Logger.getLogger(SeoAnalyzerImpl.class.getName());

    @Override
    public SeoResult analyze(String url) throws Exception {
        logger.info("Starte SEO-Analyse für URL: " + url);

        SeoResult result = new SeoResult();
        result.setUrl(url);  // Wichtig: URL setzen

        try {
            // Website mit Jsoup laden
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
                    .get();

            // Document an alternative Methode übergeben
            return analyzeDocument(doc, url);
        } catch (Exception e) {
            logger.severe("Fehler bei der SEO-Analyse: " + e.getMessage());
            throw new Exception("Fehler bei der SEO-Analyse: " + e.getMessage(), e);
        }
    }

    /**
     * Analyseimplementierung für ein bereits geladenes Document.
     * Diese Methode kann separat aufgerufen werden, wenn das Document bereits aus einer anderen Quelle geladen wurde.
     */
    public SeoResult analyzeDocument(Document doc, String url) {
        SeoResult result = new SeoResult();
        result.setUrl(url);  // Wichtig: URL setzen

        // Meta-Tags extrahieren
        String title = doc.title();
        String description = doc.select("meta[name=description]").attr("content");
        String keywords = doc.select("meta[name=keywords]").attr("content");

        result.setTitle(title);
        result.setTitleLength(title.length());
        result.setDescription(description);
        result.setDescriptionLength(description.length());
        result.setKeywords(keywords);

        // Überschriften analysieren
        result.setH1Count(doc.select("h1").size());
        result.setH2Count(doc.select("h2").size());
        result.setH3Count(doc.select("h3").size());

        // Bilder-Alt-Text analysieren
        Elements images = doc.select("img");
        int imagesTotal = images.size();
        int imagesWithAlt = doc.select("img[alt]").size();
        int imagesWithoutAlt = imagesTotal - imagesWithAlt;
        double altPercentage = imagesTotal > 0 ? (double) imagesWithAlt / imagesTotal * 100 : 0;

        result.setImagesTotal(imagesTotal);
        result.setImagesWithAlt(imagesWithAlt);
        result.setImagesWithoutAlt(imagesWithoutAlt);
        result.setAltImagePercentage(altPercentage);

        // Links analysieren - korrigierte Version
        int internalLinks = doc.select("a[href^=/], a[href^=" + url + "]").size();
        int externalLinks = doc.select("a[href^=http]").size() - doc.select("a[href^=" + url + "]").size();

        result.setInternalLinks(internalLinks);
        result.setExternalLinks(externalLinks);

        // Canonical-URL extrahieren
        String canonicalUrl = doc.select("link[rel=canonical]").attr("href");
        result.setCanonicalUrl(canonicalUrl);

        // SEO-Score berechnen
        int score = result.calculateScore(); // Ensure this method exists and works correctly
        result.setScore(score);

        logger.info("SEO-Analyse abgeschlossen. Score: " + result.getScore());

        return result;
    }

    /**
     * Implementiert die erwartete analyze(Document, String) Methode,
     * indem die Document-basierte Analyse aufgerufen wird
     */
    @Override
    public SeoResult analyze(Document document, String url) {
        return analyzeDocument(document, url);
    }
}
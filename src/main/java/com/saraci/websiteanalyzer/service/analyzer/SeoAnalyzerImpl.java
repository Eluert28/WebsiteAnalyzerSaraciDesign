package com.saraci.websiteanalyzer.service.analyzer;

import com.saraci.websiteanalyzer.model.SeoResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;  // Diesen Import hinzufügen
import org.jsoup.select.Elements;

import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        // Canonical-Tags analysieren (HIER den neuen Aufruf hinzufügen)
        analyzeCanonicalTags(doc, result);
        analyzeStructuredData(doc, result);

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
    /**
     * Analysiert strukturierte Daten (Schema.org) auf der Seite.
     * @param doc Das zu analysierende Jsoup-Dokument
     * @param result Das SeoResult-Objekt, das aktualisiert werden soll
     */
    private void analyzeStructuredData(Document doc, SeoResult result) {
        // Prüfe auf JSON-LD Schema.org
        Elements jsonldScripts = doc.select("script[type=application/ld+json]");
        int jsonldCount = jsonldScripts.size();

        // Prüfe auf Microdata Schema.org
        Elements microdataElements = doc.select("[itemscope]");
        int microdataCount = microdataElements.size();

        // Prüfe auf RDFa Schema.org
        Elements rdfaElements = doc.select("[property]");
        int rdfaCount = rdfaElements.size();

        // Gesamtzahl der strukturierten Datenelemente
        int totalStructuredDataCount = jsonldCount + microdataCount + rdfaCount;

        // Setze die Werte im SeoResult
        result.setStructuredDataPresent(totalStructuredDataCount > 0);
        result.setStructuredDataCount(totalStructuredDataCount);
        result.setJsonLdCount(jsonldCount);
        result.setMicrodataCount(microdataCount);
        result.setRdfaCount(rdfaCount);

        // Extrahiere Schema.org-Typen aus JSON-LD
        List<String> schemaTypes = new ArrayList<>();
        for (Element script : jsonldScripts) {
            String json = script.html();
            // Einfache Extraktion des "@type"-Werts
            Matcher typeMatcher = Pattern.compile("\"@type\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            while (typeMatcher.find()) {
                schemaTypes.add(typeMatcher.group(1));
            }
        }

        // Extrahiere Schema.org-Typen aus Microdata
        for (Element element : microdataElements) {
            String itemtype = element.attr("itemtype");
            if (itemtype.contains("schema.org/")) {
                // Extrahiere den Typ aus der URL
                String type = itemtype.substring(itemtype.lastIndexOf("/") + 1);
                if (!type.isEmpty()) {
                    schemaTypes.add(type);
                }
            }
        }

        // Schema-Typen als kommagetrennte Liste speichern
        if (!schemaTypes.isEmpty()) {
            result.setSchemaTypes(String.join(", ", schemaTypes));
        } else {
            result.setSchemaTypes("");
        }

        logger.info("Strukturierte Daten analysiert: " + totalStructuredDataCount + " Elemente gefunden.");
    }
    /**
     * Analysiert Canonical-Tags auf der Seite.
     * @param doc Das zu analysierende Jsoup-Dokument
     * @param result Das SeoResult-Objekt, das aktualisiert werden soll
     */
    private void analyzeCanonicalTags(Document doc, SeoResult result) {
        // Suche nach dem Canonical-Tag
        Element canonicalTag = doc.select("link[rel=canonical]").first();

        if (canonicalTag != null) {
            String canonicalUrl = canonicalTag.attr("href");
            result.setCanonicalUrl(canonicalUrl);

            // Prüfen, ob die kanonische URL absolut ist
            boolean isAbsoluteUrl = canonicalUrl.startsWith("http://") || canonicalUrl.startsWith("https://");
            result.setCanonicalUrlAbsolute(isAbsoluteUrl);

            // Prüfen, ob die kanonische URL auf sich selbst verweist (Self-Referential)
            boolean isSelfReferential = canonicalUrl.equals(result.getUrl()) ||
                    (isAbsoluteUrl && canonicalUrl.endsWith(result.getUrl()));
            result.setCanonicalUrlSelfReferential(isSelfReferential);

            logger.info("Canonical-Tag gefunden: " + canonicalUrl);
        } else {
            result.setCanonicalUrl(null);
            result.setCanonicalUrlAbsolute(false);
            result.setCanonicalUrlSelfReferential(false);
            logger.info("Kein Canonical-Tag gefunden");
        }
    }
}
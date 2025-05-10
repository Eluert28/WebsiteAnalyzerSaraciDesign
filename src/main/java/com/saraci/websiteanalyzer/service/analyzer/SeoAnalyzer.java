package com.saraci.websiteanalyzer.service.analyzer;

import com.saraci.websiteanalyzer.model.SeoResult;
import org.jsoup.nodes.Document;

/**
 * Interface für SEO-Analyse-Services.
 */
public interface SeoAnalyzer {

    /**
     * Analysiert eine Website anhand ihrer URL.
     *
     * @param url Die zu analysierende URL
     * @return Ergebnis der SEO-Analyse
     * @throws Exception Bei Fehlern während der Analyse
     */
    SeoResult analyze(String url) throws Exception;

    /**
     * Analysiert eine Website anhand eines bereits geladenen Jsoup-Dokuments.
     * Diese Methode ist nützlich, wenn das Dokument bereits aus einer anderen Quelle geladen wurde.
     *
     * @param document Das zu analysierende Jsoup-Dokument
     * @param url Die URL der Website (wird für relative Links benötigt)
     * @return Ergebnis der SEO-Analyse
     */
    SeoResult analyze(Document document, String url);
}
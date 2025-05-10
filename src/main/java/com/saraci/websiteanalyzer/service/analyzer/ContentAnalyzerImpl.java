package com.saraci.websiteanalyzer.service.analyzer;

import com.saraci.websiteanalyzer.model.ContentResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.logging.Logger;

public class ContentAnalyzerImpl implements ContentAnalyzer {
    private static final Logger logger = Logger.getLogger(ContentAnalyzerImpl.class.getName());

    @Override
    public ContentResult analyze(String url) throws Exception {
        logger.info("Starte Inhaltsanalyse für URL: " + url);

        ContentResult result = new ContentResult();

        try {
            // Website mit Jsoup laden
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
                    .get();

            // Text extrahieren und analysieren
            String bodyText = doc.body().text().replaceAll("\\s+", " ").trim();
            String[] words = bodyText.split("\\s+");

            result.setWordCount(words.length);
            result.setCharacterCount(bodyText.length());

            if (words.length > 0) {
                double totalWordLength = 0;
                for (String word : words) {
                    totalWordLength += word.length();
                }
                result.setAverageWordLength(totalWordLength / words.length);
            }

            // Seitenstruktur analysieren
            result.setParagraphCount(doc.select("p").size());
            result.setImageCount(doc.select("img").size());
            result.setVideoCount(doc.select("video, iframe[src*=youtube], iframe[src*=vimeo]").size());
            result.setListCount(doc.select("ul, ol").size());
            result.setTableCount(doc.select("table").size());

            logger.info("Inhaltsanalyse abgeschlossen. Wörter: " + result.getWordCount());

            return result;
        } catch (Exception e) {
            logger.severe("Fehler bei der Inhaltsanalyse: " + e.getMessage());
            throw new Exception("Fehler bei der Inhaltsanalyse: " + e.getMessage(), e);
        }
    }
}
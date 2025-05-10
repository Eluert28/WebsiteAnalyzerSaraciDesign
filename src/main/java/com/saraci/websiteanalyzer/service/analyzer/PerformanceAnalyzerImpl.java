package com.saraci.websiteanalyzer.service.analyzer;

import com.saraci.websiteanalyzer.model.PerformanceResult;
import org.jsoup.Jsoup;

import java.util.logging.Logger;

public class PerformanceAnalyzerImpl implements PerformanceAnalyzer {
    private static final Logger logger = Logger.getLogger(PerformanceAnalyzerImpl.class.getName());

    @Override
    public PerformanceResult analyze(String url) throws Exception {
        logger.info("Starte Performance-Analyse für URL: " + url);

        PerformanceResult result = new PerformanceResult();

        try {
            // Messe die Ladezeit
            long startTime = System.currentTimeMillis();
            Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
                    .get();
            long endTime = System.currentTimeMillis();
            int loadTime = (int) (endTime - startTime);

            result.setLoadTime(loadTime);

            // Für eine vollständige Performance-Analyse würden wir hier Lighthouse oder eine ähnliche Bibliothek verwenden
            // Für dieses Beispiel verwenden wir einfache Dummy-Werte
            result.setLighthouseScore(85);
            result.setFirstContentfulPaint("1.2s");
            result.setLargestContentfulPaint("2.5s");
            result.setTimeToInteractive("3.0s");
            result.setTotalBlockingTime("200ms");
            result.setCumulativeLayoutShift("0.1");

            logger.info("Performance-Analyse abgeschlossen. Ladezeit: " + loadTime + "ms");

            return result;
        } catch (Exception e) {
            logger.severe("Fehler bei der Performance-Analyse: " + e.getMessage());
            throw new Exception("Fehler bei der Performance-Analyse: " + e.getMessage(), e);
        }
    }
}
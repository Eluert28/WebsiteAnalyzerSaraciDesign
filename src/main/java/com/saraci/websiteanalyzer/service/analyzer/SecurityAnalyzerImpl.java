package com.saraci.websiteanalyzer.service.analyzer;

import com.saraci.websiteanalyzer.model.SecurityResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SecurityAnalyzerImpl implements SecurityAnalyzer {
    private static final Logger logger = Logger.getLogger(SecurityAnalyzerImpl.class.getName());

    @Override
    public SecurityResult analyze(String url) throws Exception {
        logger.info("Starte Sicherheitsanalyse für URL: " + url);

        SecurityResult result = new SecurityResult();

        try {
            // Überprüfen ob HTTPS verwendet wird
            boolean isHttps = url.startsWith("https://");
            result.setHttpsEnabled(isHttps);

            // Sicherheits-Header abrufen
            Connection.Response response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
                    .method(Connection.Method.GET)
                    .execute();

            Map<String, String> headers = response.headers();

            // Wichtige Sicherheits-Header überprüfen
            Map<String, String> securityHeaders = new HashMap<>();
            securityHeaders.put("Strict-Transport-Security", headers.get("Strict-Transport-Security"));
            securityHeaders.put("Content-Security-Policy", headers.get("Content-Security-Policy"));
            securityHeaders.put("X-XSS-Protection", headers.get("X-XSS-Protection"));
            securityHeaders.put("X-Frame-Options", headers.get("X-Frame-Options"));
            securityHeaders.put("X-Content-Type-Options", headers.get("X-Content-Type-Options"));
            securityHeaders.put("Referrer-Policy", headers.get("Referrer-Policy"));

            // Als JSON-String speichern
            StringBuilder headersJson = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String> entry : securityHeaders.entrySet()) {
                if (!first) {
                    headersJson.append(",");
                }
                first = false;
                headersJson.append("\"").append(entry.getKey()).append("\":\"")
                        .append(entry.getValue() != null ? entry.getValue().replace("\"", "\\\"") : "")
                        .append("\"");
            }
            headersJson.append("}");

            result.setSecurityHeaders(headersJson.toString());

            // Sicherheits-Score berechnen (einfache Version)
            int implementedHeaders = (int) securityHeaders.values().stream()
                    .filter(v -> v != null && !v.isEmpty())
                    .count();
            int totalHeaders = securityHeaders.size();
            int score = totalHeaders > 0 ? (implementedHeaders * 100) / totalHeaders : 0;

            result.setSecurityHeadersScore(score);

            // Cookie-Sicherheit (einfache Version)
            result.setCookiesSecurityScore(70);

            logger.info("Sicherheitsanalyse abgeschlossen. HTTPS: " + isHttps + ", Score: " + score);

            return result;
        } catch (Exception e) {
            logger.severe("Fehler bei der Sicherheitsanalyse: " + e.getMessage());
            throw new Exception("Fehler bei der Sicherheitsanalyse: " + e.getMessage(), e);
        }
    }
}
package com.saraci.websiteanalyzer.service.report;

import com.saraci.websiteanalyzer.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Implementierung des PDF-Report-Generators mit Apache PDFBox.
 */
public class PdfReportGeneratorImpl implements PdfReportGenerator {
    private static final Logger logger = Logger.getLogger(PdfReportGeneratorImpl.class.getName());

    // Konstanten für das Layout
    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 20;
    private static final float FONT_SIZE_SUBTITLE = 16;
    private static final float FONT_SIZE_TEXT = 12;
    private static final float FONT_SIZE_SMALL = 10;
    private static final float LINE_HEIGHT = 1.5f;

    @Override
    public String generateReport(AnalysisResult result) throws Exception {
        logger.info("Erstelle PDF-Bericht für Analyse-ID: " + result.getId());

        // Stelle sicher, dass das Reports-Verzeichnis existiert
        String reportsDir = "reports";
        Path reportsDirPath = Paths.get(reportsDir);
        if (!Files.exists(reportsDirPath)) {
            Files.createDirectories(reportsDirPath);
        }

        // Erstelle einen eindeutigen Dateinamen für den Bericht
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String websiteHost = result.getUrl().replaceAll("https?://", "").replaceAll("[^a-zA-Z0-9.]", "_");
        String filename = "website_analysis_" + websiteHost + "_" + timestamp + ".pdf";
        String filePath = Paths.get(reportsDir, filename).toString();

        // Erstelle ein neues PDF-Dokument
        try (PDDocument document = new PDDocument()) {
            // Füge Titelseite hinzu
            addTitlePage(document, result);

            // Füge Inhaltsverzeichnis hinzu
            addTableOfContents(document);

            // Füge die einzelnen Abschnitte hinzu
            addSummaryPage(document, result);
            addSeoAnalysisPage(document, result.getSeoResult());
            addPerformanceAnalysisPage(document, result.getPerformanceResult());
            addSecurityAnalysisPage(document, result.getSecurityResult());
            addContentAnalysisPage(document, result.getContentResult());
            addRecommendationsPage(document, result);

            // Speichere das Dokument
            document.save(filePath);

            logger.info("PDF-Bericht erstellt: " + filePath);
            return filePath;
        } catch (Exception e) {
            logger.severe("Fehler bei der PDF-Erstellung: " + e.getMessage());
            throw new Exception("Fehler bei der PDF-Erstellung: " + e.getMessage(), e);
        }
    }

    /**
     * Fügt die Titelseite zum Dokument hinzu.
     */
    private void addTitlePage(PDDocument document, AnalysisResult result) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float centerX = pageWidth / 2;
            float startY = pageHeight - MARGIN * 2;

            // Logo (falls vorhanden)
            try {
                PDImageXObject logo = PDImageXObject.createFromFile("src/main/resources/public/images/logo.png", document);
                float logoWidth = 200;
                float logoHeight = logoWidth * logo.getHeight() / logo.getWidth();
                contentStream.drawImage(logo, centerX - logoWidth / 2, startY, logoWidth, logoHeight);
                startY -= logoHeight + MARGIN;
            } catch (Exception e) {
                // Falls kein Logo vorhanden ist, ignorieren
                logger.warning("Logo konnte nicht geladen werden: " + e.getMessage());
            }

            // Titel
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TITLE);
            contentStream.setNonStrokingColor(Color.BLACK);

            String title = "Website-Analyse-Bericht";
            float titleWidth = title.length() * FONT_SIZE_TITLE * 0.5f;
            contentStream.newLineAtOffset(centerX - titleWidth / 2, startY);
            contentStream.showText(title);
            contentStream.endText();

            startY -= FONT_SIZE_TITLE * LINE_HEIGHT;

            // URL
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_SUBTITLE);

            String url = result.getUrl();
            float urlWidth = url.length() * FONT_SIZE_SUBTITLE * 0.5f;
            contentStream.newLineAtOffset(centerX - urlWidth / 2, startY);
            contentStream.showText(url);
            contentStream.endText();

            startY -= FONT_SIZE_SUBTITLE * LINE_HEIGHT * 2;

            // Datum
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);

            String date = "Erstellt am: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            float dateWidth = date.length() * FONT_SIZE_TEXT * 0.5f;
            contentStream.newLineAtOffset(centerX - dateWidth / 2, startY);
            contentStream.showText(date);
            contentStream.endText();

            // Fußzeile
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_SMALL);
            contentStream.setNonStrokingColor(Color.DARK_GRAY);

            String footer = "Erstellt mit Website Analyzer - Saraci Design";
            float footerWidth = footer.length() * FONT_SIZE_SMALL * 0.5f;
            contentStream.newLineAtOffset(centerX - footerWidth / 2, MARGIN);
            contentStream.showText(footer);
            contentStream.endText();
        }
    }

    /**
     * Fügt ein Inhaltsverzeichnis zum Dokument hinzu.
     */
    private void addTableOfContents(PDDocument document) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float startY = page.getMediaBox().getHeight() - MARGIN * 2;

            // Überschrift
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SUBTITLE);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("Inhaltsverzeichnis");
            contentStream.endText();

            startY -= FONT_SIZE_SUBTITLE * LINE_HEIGHT * 1.5f;

            // Einträge
            String[] entries = {
                    "1. Zusammenfassung",
                    "2. SEO-Analyse",
                    "3. Performance-Analyse",
                    "4. Sicherheitsanalyse",
                    "5. Inhaltsanalyse",
                    "6. Empfehlungen"
            };

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);

            for (String entry : entries) {
                contentStream.showText(entry);
                contentStream.newLineAtOffset(0, -FONT_SIZE_TEXT * LINE_HEIGHT);
            }

            contentStream.endText();
        }
    }

    /**
     * Fügt eine Zusammenfassungsseite zum Dokument hinzu.
     */
    private void addSummaryPage(PDDocument document, AnalysisResult result) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float startY = page.getMediaBox().getHeight() - MARGIN * 2;

            // Überschrift
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SUBTITLE);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("1. Zusammenfassung");
            contentStream.endText();

            startY -= FONT_SIZE_SUBTITLE * LINE_HEIGHT * 1.5f;

            // Scores
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("Gesamtbewertung:");
            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);

            SeoResult seo = result.getSeoResult();
            PerformanceResult perf = result.getPerformanceResult();
            SecurityResult sec = result.getSecurityResult();

            String[] scores = {
                    "SEO: " + (seo != null ? seo.getScore() : "N/A") + "/100",
                    "Performance: " + (perf != null ? perf.getLighthouseScore() : "N/A") + "/100",
                    "Sicherheit: " + (sec != null ? sec.getSecurityHeadersScore() : "N/A") + "/100"
            };

            for (String score : scores) {
                contentStream.showText("• " + score);
                contentStream.newLineAtOffset(0, -FONT_SIZE_TEXT * LINE_HEIGHT);
            }

            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT * (scores.length + 1);

            // Wichtigste Erkenntnisse
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("Wichtigste Erkenntnisse:");
            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);

            String[] insights = generateInsights(result);

            for (String insight : insights) {
                contentStream.showText("• " + insight);
                contentStream.newLineAtOffset(0, -FONT_SIZE_TEXT * LINE_HEIGHT);
            }

            contentStream.endText();
        }
    }

    /**
     * Generiert Erkenntnisse basierend auf den Analyseergebnissen.
     */
    private String[] generateInsights(AnalysisResult result) {
        // Hier könnten komplexere Logik zur Erstellung von Erkenntnissen implementiert werden
        // Für dieses Beispiel verwenden wir einfache Logik

        SeoResult seo = result.getSeoResult();
        PerformanceResult perf = result.getPerformanceResult();
        SecurityResult sec = result.getSecurityResult();

        java.util.List<String> insights = new java.util.ArrayList<>();

        if (seo != null) {
            if (seo.getTitleLength() < 30 || seo.getTitleLength() > 60) {
                insights.add("Der Seitentitel hat nicht die optimale Länge (30-60 Zeichen).");
            }

            if (seo.getH1Count() != 1) {
                insights.add("Die Seite hat " + seo.getH1Count() + " H1-Elemente (optimal: genau 1).");
            }

            if (seo.getImagesWithoutAlt() > 0) {
                insights.add(seo.getImagesWithoutAlt() + " Bilder haben keinen Alt-Text.");
            }
        }

        if (perf != null) {
            if (perf.getLighthouseScore() < 50) {
                insights.add("Die Website hat erhebliche Performance-Probleme.");
            } else if (perf.getLighthouseScore() < 80) {
                insights.add("Die Website-Performance könnte verbessert werden.");
            }
        }

        if (sec != null) {
            if (!sec.isHttpsEnabled()) {
                insights.add("Die Website verwendet kein HTTPS, was ein Sicherheitsrisiko darstellt.");
            }

            if (sec.getSecurityHeadersScore() < 50) {
                insights.add("Wichtige Sicherheits-Header fehlen auf der Website.");
            }
        }

        if (insights.isEmpty()) {
            insights.add("Die Website ist gut optimiert. Keine kritischen Probleme gefunden.");
        }

        return insights.toArray(new String[0]);
    }

    /**
     * Fügt eine SEO-Analyseseite zum Dokument hinzu.
     */
    private void addSeoAnalysisPage(PDDocument document, SeoResult seoResult) throws Exception {
        if (seoResult == null) {
            return;
        }

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float startY = page.getMediaBox().getHeight() - MARGIN * 2;

            // Überschrift
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SUBTITLE);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("2. SEO-Analyse");
            contentStream.endText();

            startY -= FONT_SIZE_SUBTITLE * LINE_HEIGHT * 1.5f;

            // Meta-Informationen
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("Meta-Informationen:");
            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);

            String[] metaInfo = {
                    "Titel: " + (seoResult.getTitle() != null ? seoResult.getTitle() : "Nicht gefunden"),
                    "Titellänge: " + seoResult.getTitleLength() + " Zeichen" + (seoResult.getTitleLength() < 30 || seoResult.getTitleLength() > 60 ? " (nicht optimal)" : " (gut)"),
                    "Beschreibung: " + (seoResult.getDescription() != null ? seoResult.getDescription() : "Nicht gefunden"),
                    "Beschreibungslänge: " + seoResult.getDescriptionLength() + " Zeichen" + (seoResult.getDescriptionLength() < 50 || seoResult.getDescriptionLength() > 160 ? " (nicht optimal)" : " (gut)")
            };

            for (String info : metaInfo) {
                contentStream.showText("• " + info);
                contentStream.newLineAtOffset(0, -FONT_SIZE_TEXT * LINE_HEIGHT);
            }

            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT * (metaInfo.length + 1.5f);

            // Überschriften
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("Überschriften:");
            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);

            String[] headings = {
                    "H1: " + seoResult.getH1Count() + (seoResult.getH1Count() != 1 ? " (nicht optimal)" : " (gut)"),
                    "H2: " + seoResult.getH2Count(),
                    "H3: " + seoResult.getH3Count()
            };

            for (String heading : headings) {
                contentStream.showText("• " + heading);
                contentStream.newLineAtOffset(0, -FONT_SIZE_TEXT * LINE_HEIGHT);
            }

            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT * (headings.length + 1.5f);

            // Bilder
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("Bilder:");
            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);

            String[] images = {
                    "Gesamtzahl: " + seoResult.getImagesTotal(),
                    "Mit Alt-Text: " + seoResult.getImagesWithAlt(),
                    "Ohne Alt-Text: " + seoResult.getImagesWithoutAlt(),
                    "Alt-Text-Abdeckung: " + String.format("%.1f", seoResult.getAltImagePercentage()) + "%"
            };

            for (String image : images) {
                contentStream.showText("• " + image);
                contentStream.newLineAtOffset(0, -FONT_SIZE_TEXT * LINE_HEIGHT);
            }

            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT * (images.length + 1.5f);

            // Links
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("Links:");
            contentStream.endText();

            startY -= FONT_SIZE_TEXT * LINE_HEIGHT;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);

            String[] links = {
                    "Interne Links: " + seoResult.getInternalLinks(),
                    "Externe Links: " + seoResult.getExternalLinks(),
                    "Gesamtzahl: " + (seoResult.getInternalLinks() + seoResult.getExternalLinks())
            };

            for (String link : links) {
                contentStream.showText("• " + link);
                contentStream.newLineAtOffset(0, -FONT_SIZE_TEXT * LINE_HEIGHT);
            }

            contentStream.endText();
        }
    }

    /**
     * Fügt eine Performance-Analyseseite zum Dokument hinzu.
     */
    private void addPerformanceAnalysisPage(PDDocument document, PerformanceResult performanceResult) throws Exception {
        // Ähnliche Implementierung wie für die SEO-Seite
        // Der Einfachheit halber hier nur ein Platzhalter
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float startY = page.getMediaBox().getHeight() - MARGIN * 2;

            // Überschrift
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SUBTITLE);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("3. Performance-Analyse");
            contentStream.endText();

            // Weitere Performance-Informationen hinzufügen...
        }
    }

    /**
     * Fügt eine Sicherheitsanalyseseite zum Dokument hinzu.
     */
    private void addSecurityAnalysisPage(PDDocument document, SecurityResult securityResult) throws Exception {
        // Ähnliche Implementierung wie für die SEO-Seite
        // Der Einfachheit halber hier nur ein Platzhalter
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float startY = page.getMediaBox().getHeight() - MARGIN * 2;

            // Überschrift
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SUBTITLE);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("4. Sicherheitsanalyse");
            contentStream.endText();

            // Weitere Sicherheitsinformationen hinzufügen...
        }
    }

    /**
     * Fügt eine Inhaltsanalyseseite zum Dokument hinzu.
     */
    private void addContentAnalysisPage(PDDocument document, ContentResult contentResult) throws Exception {
        // Ähnliche Implementierung wie für die SEO-Seite
        // Der Einfachheit halber hier nur ein Platzhalter
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float startY = page.getMediaBox().getHeight() - MARGIN * 2;

            // Überschrift
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SUBTITLE);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("5. Inhaltsanalyse");
            contentStream.endText();

            // Weitere Inhaltsinformationen hinzufügen...
        }
    }

    /**
     * Fügt eine Empfehlungsseite zum Dokument hinzu.
     */
    private void addRecommendationsPage(PDDocument document, AnalysisResult result) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float startY = page.getMediaBox().getHeight() - MARGIN * 2;

            // Überschrift
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_SUBTITLE);
            contentStream.setNonStrokingColor(Color.BLACK);
            contentStream.newLineAtOffset(MARGIN, startY);
            contentStream.showText("6. Empfehlungen");
            contentStream.endText();

            startY -= FONT_SIZE_SUBTITLE * LINE_HEIGHT * 1.5f;

            // Empfehlungen
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN, startY);

            String[] recommendations = generateRecommendations(result);

            for (String recommendation : recommendations) {
                // Zeilenumbruch für lange Texte
                if (recommendation.length() > 80) {
                    String[] words = recommendation.split(" ");
                    StringBuilder line = new StringBuilder();

                    contentStream.showText("• ");

                    for (String word : words) {
                        if (line.length() + word.length() + 1 <= 80) {
                            if (line.length() > 0) {
                                line.append(" ");
                            }
                            line.append(word);
                        } else {
                            contentStream.showText(line.toString());
                            contentStream.newLineAtOffset(0, -FONT_SIZE_TEXT * LINE_HEIGHT);
                            line = new StringBuilder("  " + word); // Einrückung für Fortsetzungszeilen
                        }
                    }

                    if (line.length() > 0) {
                        contentStream.showText(line.toString());
                    }
                } else {
                    contentStream.showText("• " + recommendation);
                }

                contentStream.newLineAtOffset(0, -FONT_SIZE_TEXT * LINE_HEIGHT * 1.5f);
            }

            contentStream.endText();
        }
    }

    /**
     * Generiert Empfehlungen basierend auf den Analyseergebnissen.
     */
    private String[] generateRecommendations(AnalysisResult result) {
        java.util.List<String> recommendations = new java.util.ArrayList<>();

        SeoResult seo = result.getSeoResult();
        PerformanceResult perf = result.getPerformanceResult();
        SecurityResult sec = result.getSecurityResult();

        if (seo != null) {
            if (seo.getTitleLength() < 30 || seo.getTitleLength() > 60) {
                recommendations.add("Optimieren Sie den Seitentitel auf 30-60 Zeichen für bessere SEO-Ergebnisse.");
            }

            if (seo.getDescriptionLength() < 50 || seo.getDescriptionLength() > 160) {
                recommendations.add("Passen Sie die Meta-Beschreibung auf 50-160 Zeichen an für bessere Sichtbarkeit in Suchmaschinen.");
            }

            if (seo.getH1Count() != 1) {
                recommendations.add("Verwenden Sie genau eine H1-Überschrift pro Seite für eine bessere Strukturierung des Inhalts.");
            }

            if (seo.getImagesWithoutAlt() > 0) {
                recommendations.add("Fügen Sie Alt-Texte zu allen " + seo.getImagesWithoutAlt() + " Bildern ohne Alt-Text hinzu, um die Zugänglichkeit und SEO zu verbessern.");
            }
        }

        if (perf != null) {
            if (perf.getLighthouseScore() < 70) {
                recommendations.add("Verbessern Sie die Website-Performance durch Optimierung von Bildern, Minimierung von CSS und JavaScript und Nutzung von Browser-Caching.");
            }

            if (perf.getLoadTime() > 3000) {
                recommendations.add("Reduzieren Sie die Ladezeit der Seite (aktuell: " + perf.getLoadTime() + "ms), indem Sie unnötige Ressourcen entfernen und Server-Antwortzeiten optimieren.");
            }
        }

        if (sec != null) {
            if (!sec.isHttpsEnabled()) {
                recommendations.add("Implementieren Sie HTTPS für Ihre Website, um die Sicherheit zu erhöhen und das Vertrauen der Benutzer zu stärken.");
            }

            if (sec.getSecurityHeadersScore() < 70) {
                recommendations.add("Fügen Sie fehlende Sicherheits-Header hinzu, um die Sicherheit der Website zu verbessern.");
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Ihre Website ist bereits gut optimiert. Halten Sie die Inhalte aktuell und überwachen Sie regelmäßig die Performance.");
        }

        return recommendations.toArray(new String[0]);
    }
}
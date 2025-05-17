package com.saraci.websiteanalyzer.service.report;

import com.saraci.websiteanalyzer.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Verbesserte Implementierung des PDF-Report-Generators mit Apache PDFBox.
 */
public class PdfReportGeneratorImpl implements PdfReportGenerator {
    private static final Logger logger = Logger.getLogger(PdfReportGeneratorImpl.class.getName());

    // Konstanten für das Layout
    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 24;
    private static final float FONT_SIZE_SUBTITLE = 18;
    private static final float FONT_SIZE_SECTION = 16;
    private static final float FONT_SIZE_TEXT = 11;
    private static final float FONT_SIZE_SMALL = 9;
    private static final float LINE_HEIGHT = 1.5f;

    // Farben
    private static final Color PRIMARY_COLOR = new Color(232, 24, 24); // Rot (#e81818)
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR_LIGHT = new Color(100, 100, 100);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color SUCCESS_COLOR = new Color(0, 210, 106); // Grün
    private static final Color WARNING_COLOR = new Color(255, 187, 0); // Gelb
    private static final Color ERROR_COLOR = new Color(255, 51, 51); // Rot

    // Aktuelle Seite und Position
    private int pageNumber = 1;
    private int totalPages = 1;
    private float yPosition;

    // Seitengröße
    private float pageWidth;
    private float pageHeight;

    @Override
    public String generateReport(AnalysisResult result) throws Exception {
        logger.info("Erstelle verbesserten PDF-Bericht für Analyse-ID: " + result.getId());

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
            // Seitengröße initialisieren
            PDPage firstPage = new PDPage(PDRectangle.A4);
            pageWidth = firstPage.getMediaBox().getWidth();
            pageHeight = firstPage.getMediaBox().getHeight();

            // Zuerst alle Seiten erstellen, um die Gesamtanzahl zu kennen
            List<PDPage> pages = new ArrayList<>();

            // Titelseite
            pages.add(firstPage);

            // Inhaltsverzeichnis
            pages.add(new PDPage(PDRectangle.A4));

            // Übersicht
            pages.add(new PDPage(PDRectangle.A4));

            // SEO-Analyse
            pages.add(new PDPage(PDRectangle.A4));

            // Performance-Analyse
            pages.add(new PDPage(PDRectangle.A4));

            // Sicherheitsanalyse
            pages.add(new PDPage(PDRectangle.A4));

            // Inhaltsanalyse
            pages.add(new PDPage(PDRectangle.A4));

            // Empfehlungen
            pages.add(new PDPage(PDRectangle.A4));

            // Seitenzahl aktualisieren
            totalPages = pages.size();

            // Füge alle Seiten zum Dokument hinzu
            for (PDPage page : pages) {
                document.addPage(page);
            }

            // Füge die Inhalte hinzu
            pageNumber = 1;
            addTitlePage(document, result);

            pageNumber = 2;
            addTableOfContents(document);

            pageNumber = 3;
            addSummaryPage(document, result);

            pageNumber = 4;
            addSeoAnalysisPage(document, result.getSeoResult());

            pageNumber = 5;
            addPerformanceAnalysisPage(document, result.getPerformanceResult());

            pageNumber = 6;
            addSecurityAnalysisPage(document, result.getSecurityResult());

            pageNumber = 7;
            addContentAnalysisPage(document, result.getContentResult());

            pageNumber = 8;
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
    /**
     * Fügt die Titelseite zum Dokument hinzu.
     */
    private void addTitlePage(PDDocument document, AnalysisResult result) throws Exception {
        PDPage page = document.getPage(0);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            yPosition = pageHeight - MARGIN;

            // Logo mittig oben
            try {
                File logoFile = new File("src/main/resources/public/images/logo2.png");
                if (logoFile.exists()) {
                    PDImageXObject logo = PDImageXObject.createFromFile(logoFile.getAbsolutePath(), document);
                    float logoWidth = 200;
                    float logoHeight = logoWidth * logo.getHeight() / logo.getWidth();
                    float centerX = pageWidth / 2;
                    contentStream.drawImage(logo, centerX - logoWidth / 2, yPosition - logoHeight, logoWidth, logoHeight);
                    yPosition -= logoHeight + 40;
                }
            } catch (Exception e) {
                logger.warning("Logo konnte nicht geladen werden: " + e.getMessage());
                yPosition -= 40; // Platz freilassen, falls kein Logo
            }

            // Titel
            drawCenteredText(contentStream, "Website-Analyse-Bericht", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TITLE, yPosition);
            yPosition -= FONT_SIZE_TITLE * LINE_HEIGHT;

            // URL der Website
            drawCenteredText(contentStream, result.getUrl(), PDType1Font.HELVETICA, FONT_SIZE_SUBTITLE, yPosition);
            yPosition -= FONT_SIZE_SUBTITLE * LINE_HEIGHT;

            // Trennlinie
            drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN, yPosition, 1f, PRIMARY_COLOR);
            yPosition -= 50; // Mehr Abstand nach der Linie

            // Scores visualisieren (als Kreisdiagramme)
            float centerX = pageWidth / 2;
            float circleRadius = 45; // Kleinere Kreise (war vorher 60)
            float circleSpacing = 30;
            float totalWidth = 3 * (2 * circleRadius) + 2 * circleSpacing;
            float startX = centerX - totalWidth / 2 + circleRadius;

            // SEO Score
            int seoScore = result.getSeoResult() != null ? result.getSeoResult().getScore() : 0;
            drawScoreCircle(contentStream, document, startX, yPosition, circleRadius, seoScore, "SEO");

            // Performance Score
            int perfScore = result.getPerformanceResult() != null ? result.getPerformanceResult().getLighthouseScore() : 0;
            drawScoreCircle(contentStream, document, startX + 2 * circleRadius + circleSpacing, yPosition, circleRadius, perfScore, "Performance");

            // Security Score
            int secScore = result.getSecurityResult() != null ? result.getSecurityResult().getSecurityHeadersScore() : 0;
            drawScoreCircle(contentStream, document, startX + 4 * circleRadius + 2 * circleSpacing, yPosition, circleRadius, secScore, "Sicherheit");

            yPosition -= 2 * circleRadius + 50;

            // Datum
            String date = "Erstellt am: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            drawCenteredText(contentStream, date, PDType1Font.HELVETICA, FONT_SIZE_TEXT, yPosition);

            // Fußzeile
            addFooter(contentStream);
        }
    }

    private void addTableOfContents(PDDocument document) throws Exception {
        PDPage page = document.getPage(1);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            yPosition = pageHeight - MARGIN;

            // Seitentitel
            drawText(contentStream, "Inhaltsverzeichnis", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SECTION, MARGIN, yPosition);
            yPosition -= FONT_SIZE_SECTION * LINE_HEIGHT;

            // Trennlinie
            drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN, yPosition, 1f, PRIMARY_COLOR);
            yPosition -= 30;

            // Einträge
            String[][] entries = {
                    {"1", "Zusammenfassung", "3"},
                    {"2", "SEO-Analyse", "4"},
                    {"3", "Performance-Analyse", "5"},
                    {"4", "Sicherheitsanalyse", "6"},
                    {"5", "Inhaltsanalyse", "7"},
                    {"6", "Empfehlungen", "8"}
            };

            for (String[] entry : entries) {
                // Kapitelnummer
                drawText(contentStream, entry[0], PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);

                // Titel
                drawText(contentStream, entry[1], PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN + 30, yPosition);

                // Seitenzahl
                float textWidth = PDType1Font.HELVETICA.getStringWidth(entry[2]) / 1000 * FONT_SIZE_TEXT;
                drawText(contentStream, entry[2], PDType1Font.HELVETICA, FONT_SIZE_TEXT, pageWidth - MARGIN - textWidth, yPosition);

                // Punktlinie
                float lineStartX = MARGIN + 30 + PDType1Font.HELVETICA.getStringWidth(entry[1]) / 1000 * FONT_SIZE_TEXT + 10;
                float lineEndX = pageWidth - MARGIN - textWidth - 10;
                drawDottedLine(contentStream, lineStartX, yPosition + 2, lineEndX, yPosition + 2, 1f, TEXT_COLOR_LIGHT);

                yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT;
            }

            // Fußzeile
            addFooter(contentStream);
        }
    }

    /**
     * Fügt eine Zusammenfassungsseite zum Dokument hinzu.
     */
    private void addSummaryPage(PDDocument document, AnalysisResult result) throws Exception {
        PDPage page = document.getPage(2);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            yPosition = pageHeight - MARGIN;

            // Seitentitel
            drawText(contentStream, "1. Zusammenfassung", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SECTION, MARGIN, yPosition);
            yPosition -= FONT_SIZE_SECTION * LINE_HEIGHT;

            // Trennlinie
            drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN, yPosition, 1f, PRIMARY_COLOR);
            yPosition -= 30;

            // Kurze Einleitung
            String introText = "Diese Analyse bietet einen umfassenden Überblick über die Website " + result.getUrl() +
                    " in den Bereichen SEO, Performance und Sicherheit. Die folgenden Ergebnisse zeigen Stärken und Verbesserungspotenziale der Website.";
            drawMultiLineText(contentStream, introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition, pageWidth - 2 * MARGIN);
            yPosition -= calculateTextHeight(introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, pageWidth - 2 * MARGIN) + 20;

            // Scores-Überschrift
            drawText(contentStream, "Gesamtbewertung", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            // Scores als Balkendiagramme
            int seoScore = result.getSeoResult() != null ? result.getSeoResult().getScore() : 0;
            int perfScore = result.getPerformanceResult() != null ? result.getPerformanceResult().getLighthouseScore() : 0;
            int secScore = result.getSecurityResult() != null ? result.getSecurityResult().getSecurityHeadersScore() : 0;

            // SEO Score
            drawScoreBar(contentStream, "SEO", seoScore, MARGIN, yPosition, pageWidth - 2 * MARGIN, 30, SUCCESS_COLOR);
            yPosition -= 40;

            // Performance Score
            drawScoreBar(contentStream, "Performance", perfScore, MARGIN, yPosition, pageWidth - 2 * MARGIN, 30, WARNING_COLOR);
            yPosition -= 40;

            // Security Score
            drawScoreBar(contentStream, "Sicherheit", secScore, MARGIN, yPosition, pageWidth - 2 * MARGIN, 30, ERROR_COLOR);
            yPosition -= 60;

            // Wichtigste Erkenntnisse
            drawText(contentStream, "Wichtigste Erkenntnisse", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            String[] insights = generateInsights(result);
            for (String insight : insights) {
                // Bullet Point
                drawText(contentStream, "•", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);

                // Text mit Einrückung
                drawMultiLineText(contentStream, insight, PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN + 15, yPosition, pageWidth - 2 * MARGIN - 15);
                yPosition -= calculateTextHeight(insight, PDType1Font.HELVETICA, FONT_SIZE_TEXT, pageWidth - 2 * MARGIN - 15) + 10;
            }

            // Fußzeile
            addFooter(contentStream);
        }
    }

    /**
     * Fügt eine SEO-Analyseseite zum Dokument hinzu.
     */
    private void addSeoAnalysisPage(PDDocument document, SeoResult seoResult) throws Exception {
        PDPage page = document.getPage(3);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            yPosition = pageHeight - MARGIN;

            // Seitentitel
            drawText(contentStream, "2. SEO-Analyse", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SECTION, MARGIN, yPosition);
            yPosition -= FONT_SIZE_SECTION * LINE_HEIGHT;

            // Trennlinie
            drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN, yPosition, 1f, PRIMARY_COLOR);
            yPosition -= 30;

            if (seoResult == null) {
                drawText(contentStream, "Keine SEO-Daten verfügbar", PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition);
                addFooter(contentStream);
                return;
            }

            // Einleitung
            String introText = "Die SEO-Analyse untersucht verschiedene Faktoren, die die Sichtbarkeit der Website in Suchmaschinen beeinflussen. " +
                    "Dazu gehören Meta-Informationen, Überschriften, Bilder und Links.";
            drawMultiLineText(contentStream, introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition, pageWidth - 2 * MARGIN);
            yPosition -= calculateTextHeight(introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, pageWidth - 2 * MARGIN) + 20;

            // Gesamtscore als großes Element
            drawLargeScore(contentStream, seoResult.getScore(), "SEO-Score", MARGIN, yPosition);
            yPosition -= 100;

            // Meta-Informationen
            drawText(contentStream, "Meta-Informationen", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            // Tabelle mit Meta-Informationen
            float[] columnWidths = { 150, pageWidth - 2 * MARGIN - 150 };
            String[][] metaData = {
                    {"Titel", seoResult.getTitle() != null ? seoResult.getTitle() : "Nicht vorhanden"},
                    {"Titellänge", seoResult.getTitleLength() + " Zeichen " + getOptimalityText(seoResult.getTitleLength(), 30, 60)},
                    {"Beschreibung", seoResult.getDescription() != null ? seoResult.getDescription() : "Nicht vorhanden"},
                    {"Beschreibungslänge", seoResult.getDescriptionLength() + " Zeichen " + getOptimalityText(seoResult.getDescriptionLength(), 50, 160)}
            };

            yPosition = drawTable(contentStream, metaData, columnWidths, MARGIN, yPosition, 30);
            yPosition -= 20;

            // Überschriften
            drawText(contentStream, "Überschriften", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            String[][] headingsData = {
                    {"H1", seoResult.getH1Count() + " " + getOptimalityText(seoResult.getH1Count(), 1, 1)},
                    {"H2", String.valueOf(seoResult.getH2Count())},
                    {"H3", String.valueOf(seoResult.getH3Count())}
            };

            yPosition = drawTable(contentStream, headingsData, columnWidths, MARGIN, yPosition, 30);
            yPosition -= 20;

            // Bilder
            drawText(contentStream, "Bilder", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            String[][] imagesData = {
                    {"Gesamtzahl", String.valueOf(seoResult.getImagesTotal())},
                    {"Mit Alt-Text", seoResult.getImagesWithAlt() + " (" + String.format("%.1f", seoResult.getAltImagePercentage()) + "%)"},
                    {"Ohne Alt-Text", String.valueOf(seoResult.getImagesWithoutAlt())}
            };

            yPosition = drawTable(contentStream, imagesData, columnWidths, MARGIN, yPosition, 30);
            yPosition -= 20;

            // Links
            drawText(contentStream, "Links", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            String[][] linksData = {
                    {"Interne Links", String.valueOf(seoResult.getInternalLinks())},
                    {"Externe Links", String.valueOf(seoResult.getExternalLinks())},
                    {"Gesamtzahl", String.valueOf(seoResult.getInternalLinks() + seoResult.getExternalLinks())}
            };

            // Fußzeile
            addFooter(contentStream);
        }
    }

    /**
     * Fügt eine Performance-Analyseseite zum Dokument hinzu.
     */
    private void addPerformanceAnalysisPage(PDDocument document, PerformanceResult perfResult) throws Exception {
        PDPage page = document.getPage(4);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            yPosition = pageHeight - MARGIN;

            // Seitentitel
            drawText(contentStream, "3. Performance-Analyse", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SECTION, MARGIN, yPosition);
            yPosition -= FONT_SIZE_SECTION * LINE_HEIGHT;

            // Trennlinie
            drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN, yPosition, 1f, PRIMARY_COLOR);
            yPosition -= 30;

            if (perfResult == null) {
                drawText(contentStream, "Keine Performance-Daten verfügbar", PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition);
                addFooter(contentStream);
                return;
            }

            // Einleitung
            String introText = "Die Performance-Analyse misst die Ladezeit und andere Metriken, die die Nutzererfahrung beeinflussen. " +
                    "Eine schnelle Website verbessert die Nutzerzufriedenheit und kann sich positiv auf das Ranking in Suchmaschinen auswirken.";
            drawMultiLineText(contentStream, introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition, pageWidth - 2 * MARGIN);
            yPosition -= calculateTextHeight(introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, pageWidth - 2 * MARGIN) + 20;

            // Gesamtscore als großes Element
            drawLargeScore(contentStream, perfResult.getLighthouseScore(), "Lighthouse Score", MARGIN, yPosition);
            yPosition -= 100;

            // Ladezeit als Balken
            drawText(contentStream, "Ladezeit", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            int loadTime = perfResult.getLoadTime();
            int maxLoadTime = 5000; // 5 Sekunden als Maximum für die Visualisierung
            float barWidth = Math.min(loadTime / (float)maxLoadTime, 1.0f) * (pageWidth - 2 * MARGIN);

            // Hintergrund
            contentStream.setNonStrokingColor(new Color(230, 230, 230));
            contentStream.addRect(MARGIN, yPosition - 15, pageWidth - 2 * MARGIN, 30);
            contentStream.fill();

            // Bar
            Color barColor = loadTime < 2000 ? SUCCESS_COLOR : (loadTime < 4000 ? WARNING_COLOR : ERROR_COLOR);
            contentStream.setNonStrokingColor(barColor);
            contentStream.addRect(MARGIN, yPosition - 15, barWidth, 30);
            contentStream.fill();

            // Text
            contentStream.setNonStrokingColor(Color.WHITE);
            String loadTimeText = loadTime + " ms";
            float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(loadTimeText) / 1000 * FONT_SIZE_TEXT;
            float textX = MARGIN + 10;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(textX, yPosition);
            contentStream.showText(loadTimeText);
            contentStream.endText();

            yPosition -= 45;

            // Performance-Metriken
            drawText(contentStream, "Performance-Metriken", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            float[] columnWidths = { 200, pageWidth - 2 * MARGIN - 200 };
            String[][] metricsData = {
                    {"First Contentful Paint", perfResult.getFirstContentfulPaint()},
                    {"Largest Contentful Paint", perfResult.getLargestContentfulPaint()},
                    {"Time to Interactive", perfResult.getTimeToInteractive()},
                    {"Total Blocking Time", perfResult.getTotalBlockingTime()},
                    {"Cumulative Layout Shift", perfResult.getCumulativeLayoutShift()}
            };

            yPosition = drawTable(contentStream, metricsData, columnWidths, MARGIN, yPosition, 30);

            // Fußzeile
            addFooter(contentStream);
        }
    }

    /**
     * Fügt eine Sicherheitsanalyseseite zum Dokument hinzu.
     */
    private void addSecurityAnalysisPage(PDDocument document, SecurityResult secResult) throws Exception {
        PDPage page = document.getPage(5);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            yPosition = pageHeight - MARGIN;

            // Seitentitel
            drawText(contentStream, "4. Sicherheitsanalyse", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SECTION, MARGIN, yPosition);
            yPosition -= FONT_SIZE_SECTION * LINE_HEIGHT;

            // Trennlinie
            drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN, yPosition, 1f, PRIMARY_COLOR);
            yPosition -= 30;

            if (secResult == null) {
                drawText(contentStream, "Keine Sicherheitsdaten verfügbar", PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition);
                addFooter(contentStream);
                return;
            }

            // Einleitung
            String introText = "Die Sicherheitsanalyse prüft, ob die Website grundlegende Sicherheitsmaßnahmen implementiert hat. " +
                    "Dazu gehören HTTPS, Sicherheits-Header und Cookie-Sicherheit.";
            drawMultiLineText(contentStream, introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition, pageWidth - 2 * MARGIN);
            yPosition -= calculateTextHeight(introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, pageWidth - 2 * MARGIN) + 20;

            // Gesamtscore als großes Element
            drawLargeScore(contentStream, secResult.getSecurityHeadersScore(), "Sicherheits-Score", MARGIN, yPosition);
            yPosition -= 100;

            // HTTPS-Status
            drawText(contentStream, "HTTPS-Status", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            boolean isHttps = secResult.isHttpsEnabled();
            String httpsText = isHttps ? "Aktiviert" : "Nicht aktiviert";
            Color httpsColor = isHttps ? SUCCESS_COLOR : ERROR_COLOR;

            contentStream.setNonStrokingColor(httpsColor);
            contentStream.addRect(MARGIN, yPosition - 5, 120, 30);
            contentStream.fill();

            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN + 10, yPosition + 7);
            contentStream.showText(httpsText);
            contentStream.endText();

            yPosition -= 45;

            // Sicherheits-Header-Score
            drawText(contentStream, "Sicherheits-Header", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            float headersBarWidth = (secResult.getSecurityHeadersScore() / 100.0f) * (pageWidth - 2 * MARGIN);

            // Hintergrund
            contentStream.setNonStrokingColor(new Color(230, 230, 230));
            contentStream.addRect(MARGIN, yPosition - 15, pageWidth - 2 * MARGIN, 30);
            contentStream.fill();

            // Bar
            Color headersBarColor = secResult.getSecurityHeadersScore() >= 70 ? SUCCESS_COLOR :
                    (secResult.getSecurityHeadersScore() >= 40 ? WARNING_COLOR : ERROR_COLOR);
            contentStream.setNonStrokingColor(headersBarColor);
            contentStream.addRect(MARGIN, yPosition - 15, headersBarWidth, 30);
            contentStream.fill();

            // Text
            contentStream.setNonStrokingColor(Color.WHITE);
            String headersScoreText = secResult.getSecurityHeadersScore() + "%";
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(MARGIN + 10, yPosition);
            contentStream.showText(headersScoreText);
            contentStream.endText();

            yPosition -= 45;

            // Sicherheits-Header-Details
            drawText(contentStream, "Implementierte Header", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            try {
                // Sicherheits-Header aus JSON parsen
                String headersJson = secResult.getSecurityHeaders();
                if (headersJson != null && !headersJson.isEmpty()) {
                    // Hier würde man normalerweise einen JSON-Parser verwenden
                    // Für dieses Beispiel nehmen wir an, dass wir manuell die wichtigsten Header extrahieren

                    float[] columnWidths = { 250, pageWidth - 2 * MARGIN - 250 };
                    String[][] headersData = {
                            {"Strict-Transport-Security", getValueFromJson(headersJson, "Strict-Transport-Security")},
                            {"Content-Security-Policy", getValueFromJson(headersJson, "Content-Security-Policy")},
                            {"X-XSS-Protection", getValueFromJson(headersJson, "X-XSS-Protection")},
                            {"X-Frame-Options", getValueFromJson(headersJson, "X-Frame-Options")},
                            {"X-Content-Type-Options", getValueFromJson(headersJson, "X-Content-Type-Options")},
                            {"Referrer-Policy", getValueFromJson(headersJson, "Referrer-Policy")}
                    };

                    yPosition = drawTable(contentStream, headersData, columnWidths, MARGIN, yPosition, 30);
                } else {
                    drawText(contentStream, "Keine Sicherheits-Header implementiert", PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition);
                    yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT;
                }
            } catch (Exception e) {
                drawText(contentStream, "Fehler beim Parsen der Sicherheits-Header", PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition);
                yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT;
            }

            // Fußzeile
            addFooter(contentStream);
        }
    }

    /**
     * Fügt eine Inhaltsanalyseseite zum Dokument hinzu.
     */
    private void addContentAnalysisPage(PDDocument document, ContentResult contentResult) throws Exception {
        PDPage page = document.getPage(6);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            yPosition = pageHeight - MARGIN;

            // Seitentitel
            drawText(contentStream, "5. Inhaltsanalyse", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SECTION, MARGIN, yPosition);
            yPosition -= FONT_SIZE_SECTION * LINE_HEIGHT;

            // Trennlinie
            drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN, yPosition, 1f, PRIMARY_COLOR);
            yPosition -= 30;

            if (contentResult == null) {
                drawText(contentStream, "Keine Inhaltsdaten verfügbar", PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition);
                addFooter(contentStream);
                return;
            }

            // Einleitung
            String introText = "Die Inhaltsanalyse betrachtet den Text und die strukturellen Elemente der Website. " +
                    "Gut strukturierter und informativer Inhalt verbessert die Benutzererfahrung und kann das Ranking in Suchmaschinen positiv beeinflussen.";
            drawMultiLineText(contentStream, introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition, pageWidth - 2 * MARGIN);
            yPosition -= calculateTextHeight(introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, pageWidth - 2 * MARGIN) + 20;

            // Textstatistiken
            drawText(contentStream, "Textstatistiken", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            float[] columnWidths = { 200, pageWidth - 2 * MARGIN - 200 };
            String[][] textData = {
                    {"Wortanzahl", String.valueOf(contentResult.getWordCount())},
                    {"Zeichenanzahl", String.valueOf(contentResult.getCharacterCount())},
                    {"Durchschnittliche Wortlänge", String.format("%.1f Zeichen", contentResult.getAverageWordLength())}
            };

            yPosition = drawTable(contentStream, textData, columnWidths, MARGIN, yPosition, 30);
            yPosition -= 20;

            // Strukturelemente
            drawText(contentStream, "Strukturelemente", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 10;

            String[][] structureData = {
                    {"Absätze", String.valueOf(contentResult.getParagraphCount())},
                    {"Bilder", String.valueOf(contentResult.getImageCount())},
                    {"Videos", String.valueOf(contentResult.getVideoCount())},
                    {"Listen", String.valueOf(contentResult.getListCount())},
                    {"Tabellen", String.valueOf(contentResult.getTableCount())}
            };

            yPosition = drawTable(contentStream, structureData, columnWidths, MARGIN, yPosition, 30);
            yPosition -= 20;

            // Visualisierung der Strukturelemente als Balkendiagramm
            drawText(contentStream, "Strukturelemente - Verteilung", PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT, MARGIN, yPosition);
            yPosition -= FONT_SIZE_TEXT * LINE_HEIGHT + 20;

            // Finde den maximalen Wert für die Skalierung
            int maxElements = Math.max(
                    contentResult.getParagraphCount(),
                    Math.max(contentResult.getImageCount(),
                            Math.max(contentResult.getVideoCount(),
                                    Math.max(contentResult.getListCount(),
                                            contentResult.getTableCount())))
            );

            // Wenn maxElements = 0 ist, setzen wir ihn auf 1, um Division durch Null zu vermeiden
            if (maxElements == 0) maxElements = 1;

            // Balkenbreite und -abstand
            float barWidth = 70;
            float barSpacing = 20;
            float totalWidth = 5 * barWidth + 4 * barSpacing;
            float startX = MARGIN + (pageWidth - 2 * MARGIN - totalWidth) / 2;
            float maxBarHeight = 120;

            // Farben für die verschiedenen Elementtypen
            Color[] barColors = {
                    new Color(70, 130, 180), // Absätze - Steel Blue
                    new Color(60, 179, 113), // Bilder - Medium Sea Green
                    new Color(255, 69, 0),   // Videos - Orange Red
                    new Color(147, 112, 219), // Listen - Medium Purple
                    new Color(255, 140, 0)    // Tabellen - Dark Orange
            };

            // Werte für die Balken
            int[] values = {
                    contentResult.getParagraphCount(),
                    contentResult.getImageCount(),
                    contentResult.getVideoCount(),
                    contentResult.getListCount(),
                    contentResult.getTableCount()
            };

            // Labels für die Balken
            String[] labels = {"Absätze", "Bilder", "Videos", "Listen", "Tabellen"};

            // Balken zeichnen
            for (int i = 0; i < 5; i++) {
                float x = startX + i * (barWidth + barSpacing);
                float barHeight = (values[i] / (float)maxElements) * maxBarHeight;
                if (barHeight < 5 && values[i] > 0) barHeight = 5; // Minimale Höhe für sichtbare Balken

                // Balken zeichnen
                contentStream.setNonStrokingColor(barColors[i]);
                contentStream.addRect(x, yPosition - barHeight, barWidth, barHeight);
                contentStream.fill();

                // Wert über dem Balken
                contentStream.setNonStrokingColor(TEXT_COLOR);
                String valueText = String.valueOf(values[i]);
                float textWidth = PDType1Font.HELVETICA.getStringWidth(valueText) / 1000 * FONT_SIZE_TEXT;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
                contentStream.newLineAtOffset(x + (barWidth - textWidth) / 2, yPosition - barHeight - 15);
                contentStream.showText(valueText);
                contentStream.endText();

                // Label unter dem Balken
                float labelWidth = PDType1Font.HELVETICA.getStringWidth(labels[i]) / 1000 * FONT_SIZE_SMALL;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_SMALL);
                contentStream.newLineAtOffset(x + (barWidth - labelWidth) / 2, yPosition - maxBarHeight - 30);
                contentStream.showText(labels[i]);
                contentStream.endText();
            }

            // Fußzeile
            addFooter(contentStream);
        }
    }

    /**
     * Fügt eine Empfehlungsseite zum Dokument hinzu.
     */
    private void addRecommendationsPage(PDDocument document, AnalysisResult result) throws Exception {
        PDPage page = document.getPage(7);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            yPosition = pageHeight - MARGIN;

            // Seitentitel
            drawText(contentStream, "6. Empfehlungen", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SECTION, MARGIN, yPosition);
            yPosition -= FONT_SIZE_SECTION * LINE_HEIGHT;

            // Trennlinie
            drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN, yPosition, 1f, PRIMARY_COLOR);
            yPosition -= 30;

            // Einleitung
            String introText = "Basierend auf den Ergebnissen der Analyse werden die folgenden Maßnahmen empfohlen, " +
                    "um die Performance, SEO und Sicherheit der Website zu verbessern.";
            drawMultiLineText(contentStream, introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN, yPosition, pageWidth - 2 * MARGIN);
            yPosition -= calculateTextHeight(introText, PDType1Font.HELVETICA, FONT_SIZE_TEXT, pageWidth - 2 * MARGIN) + 20;

            // Empfehlungen generieren und anzeigen
            String[] recommendations = generateRecommendations(result);

            for (int i = 0; i < recommendations.length; i++) {
                // Nummer und Empfehlung
                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText((i + 1) + ".");
                contentStream.endText();

                // Text mit Einrückung
                contentStream.setNonStrokingColor(TEXT_COLOR);
                drawMultiLineText(contentStream, recommendations[i], PDType1Font.HELVETICA, FONT_SIZE_TEXT, MARGIN + 20, yPosition, pageWidth - 2 * MARGIN - 20);
                yPosition -= calculateTextHeight(recommendations[i], PDType1Font.HELVETICA, FONT_SIZE_TEXT, pageWidth - 2 * MARGIN - 20) + 15;

                // Prüfen, ob noch genug Platz auf der Seite ist
                if (yPosition < MARGIN + 50 && i < recommendations.length - 1) {
                    // Fußzeile hinzufügen und neue Seite erstellen
                    addFooter(contentStream);
                    contentStream.close();

                    // Neue Seite erstellen
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    pageNumber = document.getNumberOfPages();
                    totalPages = pageNumber;

                    // Neuen ContentStream für die neue Seite erstellen
                    PDPageContentStream newContentStream = new PDPageContentStream(document, newPage);

                    // Seitentitel auf der neuen Seite
                    yPosition = pageHeight - MARGIN;
                    drawText(newContentStream, "6. Empfehlungen (Fortsetzung)", PDType1Font.HELVETICA_BOLD, FONT_SIZE_SECTION, MARGIN, yPosition);
                    yPosition -= FONT_SIZE_SECTION * LINE_HEIGHT;

                    // Trennlinie
                    drawLine(newContentStream, MARGIN, yPosition, pageWidth - MARGIN, yPosition, 1f, PRIMARY_COLOR);
                    yPosition -= 30;

                    // ContentStream wechseln
                    contentStream.close();
                    return;
                }
            }

            // Fußzeile
            addFooter(contentStream);
        }
    }

    /**
     * Fügt eine Fußzeile zur Seite hinzu.
     */
    private void addFooter(PDPageContentStream contentStream) throws IOException {
        // Trennlinie
        drawLine(contentStream, MARGIN, MARGIN / 2 + 10, pageWidth - MARGIN, MARGIN / 2 + 10, 0.5f, TEXT_COLOR_LIGHT);

        // Logo und Text
        contentStream.setNonStrokingColor(TEXT_COLOR_LIGHT);

        // Links: Firmennamen
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_SMALL);
        contentStream.newLineAtOffset(MARGIN, MARGIN / 2);
        contentStream.showText("Website Analyzer - Saraci Design");
        contentStream.endText();

        // Rechts: Seitenzahl
        String pageText = "Seite " + pageNumber + " von " + totalPages;
        float textWidth = PDType1Font.HELVETICA.getStringWidth(pageText) / 1000 * FONT_SIZE_SMALL;

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_SMALL);
        contentStream.newLineAtOffset(pageWidth - MARGIN - textWidth, MARGIN / 2);
        contentStream.showText(pageText);
        contentStream.endText();
    }
    /**
     * Zeichnet einen Kreis-Score.
     * Alternative Implementierung ohne addEllipse
     */
    private void drawScoreCircle(PDPageContentStream contentStream, PDDocument document, float x, float y, float radius, int score, String label) throws IOException {
        // Bestimme die Farbe basierend auf dem Score
        Color color;
        if (score >= 80) {
            color = SUCCESS_COLOR; // Grün für gute Scores
        } else if (score >= 50) {
            color = WARNING_COLOR; // Gelb für mittlere Scores
        } else {
            color = ERROR_COLOR;   // Rot für schlechte Scores
        }

        // Hintergrund-Kreis
        contentStream.setNonStrokingColor(new Color(230, 230, 230));

        // Kreis als Pfad zeichnen statt addEllipse
        drawFilledCircle(contentStream, x, y, radius, new Color(230, 230, 230));

        // Score-Segment zeichnen
        if (score > 0) {
            double angle = (score / 100.0) * 360.0;
            drawArc(contentStream, x, y, radius, 0, angle, color);
        }

        // Score-Text
        contentStream.setNonStrokingColor(TEXT_COLOR);
        String scoreText = String.valueOf(score);
        float scoreTextWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(scoreText) / 1000 * FONT_SIZE_TITLE;

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TITLE);
        contentStream.newLineAtOffset(x - scoreTextWidth / 2, y - FONT_SIZE_TITLE / 3);
        contentStream.showText(scoreText);
        contentStream.endText();

        // Label-Text
        contentStream.setNonStrokingColor(TEXT_COLOR);
        float labelTextWidth = PDType1Font.HELVETICA.getStringWidth(label) / 1000 * FONT_SIZE_TEXT;

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
        contentStream.newLineAtOffset(x - labelTextWidth / 2, y - radius - FONT_SIZE_TEXT - 10);
        contentStream.showText(label);
        contentStream.endText();
    }

    /**
     * Zeichnet einen gefüllten Kreis als Ersatz für addEllipse
     */
    private void drawFilledCircle(PDPageContentStream contentStream, float centerX, float centerY, float radius, Color color) throws IOException {
        contentStream.setNonStrokingColor(color);

        // Kreis als Pfad implementieren
        int steps = 36;  // Anzahl der Schritte für einen glatten Kreis
        double angleStep = Math.PI * 2 / steps;

        // Zum ersten Punkt auf dem Kreis bewegen
        contentStream.moveTo(centerX + radius, centerY);

        // Kreis durch verbundene Linien annähern
        for (int i = 1; i <= steps; i++) {
            double angle = i * angleStep;
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));
            contentStream.lineTo(x, y);
        }

        // Pfad füllen
        contentStream.fill();
    }

    /**
     * Zeichnet einen Kreisbogen.
     * Alternative Implementierung mit einfachen Pfaden
     */
    private void drawArc(PDPageContentStream contentStream, float centerX, float centerY, float radius, double startAngle, double endAngle, Color color) throws IOException {
        contentStream.setNonStrokingColor(color);

        // Anzahl der Segmente (höher = glatter)
        int steps = 36;
        double angleStep = Math.PI * 2 / steps;

        // Startwinkel in Radian umrechnen
        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(endAngle);

        // Anzahl der zu zeichnenden Schritte
        int stepsToRender = (int) Math.ceil((endRad - startRad) / angleStep);
        if (stepsToRender <= 0) stepsToRender = 1;

        // Beginn des Pfades im Mittelpunkt
        contentStream.moveTo(centerX, centerY);

        // Erster Punkt auf dem Bogen
        contentStream.lineTo(
                centerX + radius * (float) Math.cos(startRad),
                centerY + radius * (float) Math.sin(startRad)
        );

        // Die Kurve durch Liniensegmente zeichnen
        for (int i = 1; i <= stepsToRender; i++) {
            double angle = startRad + i * angleStep;
            if (angle > endRad) angle = endRad;

            float x = centerX + radius * (float) Math.cos(angle);
            float y = centerY + radius * (float) Math.sin(angle);
            contentStream.lineTo(x, y);
        }

        // Zurück zum Mittelpunkt
        contentStream.lineTo(centerX, centerY);

        // Pfad füllen
        contentStream.fill();
    }

    /**
     * Zeichnet einen Score-Balken.
     */
    private void drawScoreBar(PDPageContentStream contentStream, String label, int score, float x, float y, float width, float height, Color color) throws IOException {
        // Balken-Hintergrund
        contentStream.setNonStrokingColor(new Color(230, 230, 230));
        contentStream.addRect(x, y - height, width, height);
        contentStream.fill();

        // Score-Balken
        float scoreWidth = (score / 100f) * width;
        contentStream.setNonStrokingColor(color);
        contentStream.addRect(x, y - height, scoreWidth, height);
        contentStream.fill();

        // Label
        contentStream.setNonStrokingColor(TEXT_COLOR);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
        contentStream.newLineAtOffset(x + 10, y - height / 2 + FONT_SIZE_TEXT / 3);
        contentStream.showText(label);
        contentStream.endText();

        // Score
        String scoreText = score + "%";
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
        contentStream.newLineAtOffset(x + width - 10 - PDType1Font.HELVETICA_BOLD.getStringWidth(scoreText) / 1000 * FONT_SIZE_TEXT, y - height / 2 + FONT_SIZE_TEXT / 3);
        contentStream.showText(scoreText);
        contentStream.endText();
    }

    /**
     * Zeichnet einen großen Score.
     */
    private void drawLargeScore(PDPageContentStream contentStream, int score, String label, float x, float y) throws IOException {
        // Bestimme die Farbe basierend auf dem Score
        Color color;
        if (score >= 80) {
            color = SUCCESS_COLOR; // Grün für gute Scores
        } else if (score >= 50) {
            color = WARNING_COLOR; // Gelb für mittlere Scores
        } else {
            color = ERROR_COLOR;   // Rot für schlechte Scores
        }

        // Score-Kreis
        float radius = 40;
        float centerX = x + radius;
        float centerY = y - radius;

        // Hintergrund-Kreis (Ersatz für addEllipse)
        drawFilledCircle(contentStream, centerX, centerY, radius, new Color(230, 230, 230));

        // Score-Kreis
        if (score > 0) {
            // Berechne den Winkel basierend auf dem Score
            double angle = (score / 100.0) * 360.0;

            // Zeichne den Score-Bogen
            drawArc(contentStream, centerX, centerY, radius, 0, angle, color);
        }

        // Score-Text
        contentStream.setNonStrokingColor(TEXT_COLOR);
        String scoreText = String.valueOf(score);
        float scoreTextWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(scoreText) / 1000 * FONT_SIZE_TITLE;

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TITLE);
        contentStream.newLineAtOffset(centerX - scoreTextWidth / 2, centerY - FONT_SIZE_TITLE / 3);
        contentStream.showText(scoreText);
        contentStream.endText();

        // Label
        contentStream.setNonStrokingColor(TEXT_COLOR);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
        contentStream.newLineAtOffset(x + 2 * radius + 20, y - radius);
        contentStream.showText(label);
        contentStream.endText();

        // Bewertung
        String ratingText = getRatingText(score);
        contentStream.setNonStrokingColor(color);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
        contentStream.newLineAtOffset(x + 2 * radius + 20, y - radius - FONT_SIZE_TEXT * LINE_HEIGHT);
        contentStream.showText(ratingText);
        contentStream.endText();
    }

    /**
     * Zeichnet eine Tabelle.
     * @return Die neue Y-Position nach der Tabelle
     */
    private float drawTable(PDPageContentStream contentStream, String[][] data, float[] columnWidths, float x, float y, float rowHeight) throws IOException {
        float startY = y;

        for (int i = 0; i < data.length; i++) {
            // Abwechselnde Zeilenfarben
            if (i % 2 == 0) {
                contentStream.setNonStrokingColor(new Color(245, 245, 245));
                contentStream.addRect(x, startY - rowHeight, columnWidths[0] + columnWidths[1], rowHeight);
                contentStream.fill();
            }

            // Erste Spalte (Label)
            contentStream.setNonStrokingColor(TEXT_COLOR);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(x + 5, startY - rowHeight + rowHeight / 2 + FONT_SIZE_TEXT / 3);
            contentStream.showText(data[i][0]);
            contentStream.endText();

            // Zweite Spalte (Wert)
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_TEXT);
            contentStream.newLineAtOffset(x + columnWidths[0] + 5, startY - rowHeight + rowHeight / 2 + FONT_SIZE_TEXT / 3);

            // Wenn der Wert zu lang ist, kürzen
            String value = data[i][1];
            float maxWidth = columnWidths[1] - 10;
            float textWidth = PDType1Font.HELVETICA.getStringWidth(value) / 1000 * FONT_SIZE_TEXT;

            if (textWidth > maxWidth) {
                // Text kürzen
                int maxChars = (int)(value.length() * maxWidth / textWidth);
                value = value.substring(0, maxChars - 3) + "...";
            }

            contentStream.showText(value);
            contentStream.endText();

            startY -= rowHeight;
        }

        return startY;
    }

    /**
     * Zeichnet einen Text.
     */
    private void drawText(PDPageContentStream contentStream, String text, PDType1Font font, float fontSize, float x, float y) throws IOException {
        contentStream.setNonStrokingColor(TEXT_COLOR);
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    /**
     * Zeichnet einen zentrierten Text.
     */
    private void drawCenteredText(PDPageContentStream contentStream, String text, PDType1Font font, float fontSize, float y) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float centerX = pageWidth / 2 - textWidth / 2;

        contentStream.setNonStrokingColor(TEXT_COLOR);
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(centerX, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    /**
     * Zeichnet einen mehrzeiligen Text.
     */
    private void drawMultiLineText(PDPageContentStream contentStream, String text, PDType1Font font, float fontSize, float x, float y, float width) throws IOException {
        contentStream.setNonStrokingColor(TEXT_COLOR);

        List<String> lines = wrapText(text, font, fontSize, width);
        float lineHeight = fontSize * LINE_HEIGHT;
        float currentY = y;

        for (String line : lines) {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, currentY);
            contentStream.showText(line);
            contentStream.endText();

            currentY -= lineHeight;
        }
    }

    /**
     * Berechnet die Höhe eines mehrzeiligen Texts.
     */
    private float calculateTextHeight(String text, PDType1Font font, float fontSize, float width) throws IOException {
        List<String> lines = wrapText(text, font, fontSize, width);
        return lines.size() * fontSize * LINE_HEIGHT;
    }

    /**
     * Teilt einen Text in Zeilen auf.
     */
    private List<String> wrapText(String text, PDType1Font font, float fontSize, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() > 0) {
                String lineWithWord = currentLine + " " + word;
                float lineWidth = font.getStringWidth(lineWithWord) / 1000 * fontSize;

                if (lineWidth > width) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine.append(" ").append(word);
                }
            } else {
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Zeichnet eine Linie.
     */
    private void drawLine(PDPageContentStream contentStream, float x1, float y1, float x2, float y2, float lineWidth, Color color) throws IOException {
        contentStream.setStrokingColor(color);
        contentStream.setLineWidth(lineWidth);
        contentStream.moveTo(x1, y1);
        contentStream.lineTo(x2, y2);
        contentStream.stroke();
    }

    /**
     * Zeichnet eine gepunktete Linie.
     */
    private void drawDottedLine(PDPageContentStream contentStream, float x1, float y1, float x2, float y2, float lineWidth, Color color) throws IOException {
        contentStream.setStrokingColor(color);
        contentStream.setLineWidth(lineWidth);

        // Berechne Länge der Linie
        float dx = x2 - x1;
        float dy = y2 - y1;
        float lineLength = (float) Math.sqrt(dx * dx + dy * dy);

        // Normalisiere Richtungsvektor
        float nx = dx / lineLength;
        float ny = dy / lineLength;

        // Zeichne gepunktete Linie als kurze Segmente
        float dotLength = 3f;
        float gapLength = 3f;
        float currentLength = 0f;

        while (currentLength < lineLength) {
            // Zeichne einen Punkt
            float startX = x1 + nx * currentLength;
            float startY = y1 + ny * currentLength;
            float endX = x1 + nx * Math.min(currentLength + dotLength, lineLength);
            float endY = y1 + ny * Math.min(currentLength + dotLength, lineLength);

            contentStream.moveTo(startX, startY);
            contentStream.lineTo(endX, endY);
            contentStream.stroke();

            // Zum nächsten Punkt springen
            currentLength += dotLength + gapLength;
        }
    }
    /**
     * Gibt Erkenntnisse basierend auf den Analyseergebnissen zurück.
     */
    private String[] generateInsights(AnalysisResult result) {
        List<String> insights = new ArrayList<>();

        SeoResult seo = result.getSeoResult();
        PerformanceResult perf = result.getPerformanceResult();
        SecurityResult sec = result.getSecurityResult();

        if (seo != null) {
            if (seo.getTitleLength() < 30 || seo.getTitleLength() > 60) {
                insights.add("Der Seitentitel hat nicht die optimale Länge (30-60 Zeichen).");
            }

            if (seo.getH1Count() != 1) {
                insights.add("Die Seite hat " + seo.getH1Count() + " H1-Elemente (optimal: genau 1).");
            }

            if (seo.getImagesWithoutAlt() > 0) {
                insights.add(seo.getImagesWithoutAlt() + " Bilder haben keinen Alt-Text, was für Suchmaschinen und Barrierefreiheit problematisch ist.");
            }
        }

        if (perf != null) {
            if (perf.getLighthouseScore() < 50) {
                insights.add("Die Website hat erhebliche Performance-Probleme, was die Nutzererfahrung und das Suchmaschinen-Ranking negativ beeinflusst.");
            } else if (perf.getLighthouseScore() < 80) {
                insights.add("Die Website-Performance liegt im mittleren Bereich und könnte durch Optimierungen verbessert werden.");
            }

            if (perf.getLoadTime() > 3000) {
                insights.add("Die Ladezeit von " + perf.getLoadTime() + "ms überschreitet die empfohlene Grenze von 3 Sekunden.");
            }
        }

        if (sec != null) {
            if (!sec.isHttpsEnabled()) {
                insights.add("Die Website verwendet kein HTTPS, was ein erhebliches Sicherheitsrisiko darstellt und von Suchmaschinen negativ bewertet wird.");
            }

            if (sec.getSecurityHeadersScore() < 50) {
                insights.add("Wichtige Sicherheits-Header fehlen, was die Website anfällig für verschiedene Angriffe macht.");
            }
        }

        // Standard-Erkenntnis, wenn keine anderen vorhanden sind
        if (insights.isEmpty()) {
            insights.add("Die Website ist gut optimiert. Es wurden keine kritischen Probleme gefunden, die sofortige Aufmerksamkeit erfordern.");
        }

        return insights.toArray(new String[0]);
    }

    /**
     * Gibt Empfehlungen basierend auf den Analyseergebnissen zurück.
     */
    private String[] generateRecommendations(AnalysisResult result) {
        List<String> recommendations = new ArrayList<>();

        SeoResult seo = result.getSeoResult();
        PerformanceResult perf = result.getPerformanceResult();
        SecurityResult sec = result.getSecurityResult();
        ContentResult content = result.getContentResult();

        // SEO-Empfehlungen
        if (seo != null) {
            if (seo.getTitleLength() < 30 || seo.getTitleLength() > 60) {
                recommendations.add("Optimieren Sie den Seitentitel auf 30-60 Zeichen. Ein präziser Titel verbessert das Ranking in Suchmaschinen und erhöht die Klickrate in den Suchergebnissen.");
            }

            if (seo.getDescriptionLength() < 50 || seo.getDescriptionLength() > 160) {
                recommendations.add("Passen Sie die Meta-Beschreibung auf 50-160 Zeichen an. Die Beschreibung sollte einen klaren Call-to-Action enthalten und den Mehrwert der Seite kommunizieren.");
            }

            if (seo.getH1Count() != 1) {
                recommendations.add("Verwenden Sie genau eine H1-Überschrift pro Seite. Die H1-Überschrift sollte das Hauptthema der Seite klar kommunizieren und das Hauptkeyword enthalten.");
            }

            if (seo.getImagesWithoutAlt() > 0) {
                recommendations.add("Fügen Sie Alt-Texte zu allen " + seo.getImagesWithoutAlt() + " Bildern ohne Alt-Text hinzu. Beschreibende Alt-Texte verbessern die Barrierefreiheit und helfen Suchmaschinen, den Inhalt zu verstehen.");
            }

            if (seo.getExternalLinks() < 1) {
                recommendations.add("Fügen Sie externe Links zu relevanten, autoritativen Quellen hinzu. Externe Links zu vertrauenswürdigen Websites können das Vertrauen von Suchmaschinen in Ihre Inhalte stärken.");
            }
        }

        // Performance-Empfehlungen
        if (perf != null) {
            if (perf.getLighthouseScore() < 70) {
                recommendations.add("Verbessern Sie die Website-Performance durch Optimierung von Bildern (Komprimierung, richtige Größe), Minimierung von CSS und JavaScript, und Nutzung von Browser-Caching.");
            }

            if (perf.getLoadTime() > 3000) {
                recommendations.add("Reduzieren Sie die Ladezeit der Seite (aktuell: " + perf.getLoadTime() + "ms) durch Entfernen unnötiger Ressourcen, Optimierung von Bildern und Verbesserung der Server-Antwortzeiten.");
            }

            if (perf.getLargestContentfulPaint() != null && perf.getLargestContentfulPaint().compareTo("2.5s") > 0) {
                recommendations.add("Optimieren Sie das Largest Contentful Paint (aktuell: " + perf.getLargestContentfulPaint() + "). Laden Sie wichtige Inhalte priorisiert und optimieren Sie große Ressourcen wie Hero-Bilder.");
            }
        }

        // Sicherheits-Empfehlungen
        if (sec != null) {
            if (!sec.isHttpsEnabled()) {
                recommendations.add("Implementieren Sie HTTPS für Ihre Website. HTTPS verschlüsselt die Datenübertragung, schützt sensible Daten und ist ein positiver Ranking-Faktor für Suchmaschinen.");
            }

            if (sec.getSecurityHeadersScore() < 70) {
                recommendations.add("Implementieren Sie wichtige Sicherheits-Header wie Content-Security-Policy, X-XSS-Protection und Strict-Transport-Security, um die Website gegen häufige Angriffe zu schützen.");
            }

            // Überprüfen, ob bestimmte Header fehlen
            try {
                String headersJson = sec.getSecurityHeaders();
                if (headersJson != null && !headersJson.isEmpty()) {
                    if (!headersJson.contains("Content-Security-Policy") || getValueFromJson(headersJson, "Content-Security-Policy").isEmpty()) {
                        recommendations.add("Implementieren Sie eine Content-Security-Policy (CSP), um XSS-Angriffe zu verhindern und die Ausführung nicht autorisierter Skripte zu blockieren.");
                    }
                }
            } catch (Exception e) {
                // Fehler beim Parsen der Header ignorieren
            }
        }

        // Inhalts-Empfehlungen
        if (content != null) {
            if (content.getWordCount() < 300) {
                recommendations.add("Erweitern Sie den Textinhalt auf mindestens 300-500 Wörter. Ausführlichere Inhalte werden von Suchmaschinen besser bewertet und bieten mehr Mehrwert für die Nutzer.");
            }

            if (content.getImageCount() < 1) {
                recommendations.add("Fügen Sie relevante Bilder oder Grafiken hinzu, um den Text aufzulockern und das Nutzererlebnis zu verbessern.");
            }

            if (content.getListCount() < 1 && content.getWordCount() > 300) {
                recommendations.add("Strukturieren Sie längere Textabschnitte mit Listen oder Aufzählungen, um die Lesbarkeit zu verbessern und wichtige Punkte hervorzuheben.");
            }
        }

        // Wenn keine Empfehlungen generiert wurden, Standard-Empfehlung hinzufügen
        if (recommendations.isEmpty()) {
            recommendations.add("Ihre Website ist bereits gut optimiert. Halten Sie die Inhalte aktuell und überwachen Sie regelmäßig die Performance und Sicherheit.");
        }

        return recommendations.toArray(new String[0]);
    }

    /**
     * Extrahiert einen Wert aus einem JSON-String.
     * Einfache Implementierung ohne JSON-Parser.
     */
    private String getValueFromJson(String json, String key) {
        try {
            key = "\"" + key + "\":\"";
            int startIndex = json.indexOf(key);
            if (startIndex == -1) return "";

            startIndex += key.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1) return "";

            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gibt einen Text basierend auf der Optimalität eines Wertes zurück.
     */
    private String getOptimalityText(int value, int minOptimal, int maxOptimal) {
        if (value >= minOptimal && value <= maxOptimal) {
            return "(optimal)";
        } else if (value < minOptimal) {
            return "(zu kurz)";
        } else {
            return "(zu lang)";
        }
    }

    /**
     * Gibt eine Bewertung basierend auf dem Score zurück.
     */
    private String getRatingText(int score) {
        if (score >= 90) {
            return "Hervorragend";
        } else if (score >= 70) {
            return "Gut";
        } else if (score >= 50) {
            return "Verbesserungswürdig";
        } else {
            return "Kritisch";
        }
    }
}
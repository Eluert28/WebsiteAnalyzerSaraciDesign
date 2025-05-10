package com.saraci.websiteanalyzer.service.report;

import com.saraci.websiteanalyzer.model.AnalysisResult;

public interface PdfReportGenerator {
    String generateReport(AnalysisResult result) throws Exception;
}
package com.saraci.websiteanalyzer.service.analyzer;

import com.saraci.websiteanalyzer.model.PerformanceResult;

public interface PerformanceAnalyzer {
    PerformanceResult analyze(String url) throws Exception;
}
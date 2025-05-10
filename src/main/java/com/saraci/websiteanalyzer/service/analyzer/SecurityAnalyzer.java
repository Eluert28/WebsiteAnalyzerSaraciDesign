package com.saraci.websiteanalyzer.service.analyzer;

import com.saraci.websiteanalyzer.model.SecurityResult;

public interface SecurityAnalyzer {
    SecurityResult analyze(String url) throws Exception;
}
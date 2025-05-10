package com.saraci.websiteanalyzer.service.analyzer;

import com.saraci.websiteanalyzer.model.ContentResult;

public interface ContentAnalyzer {
    ContentResult analyze(String url) throws Exception;
}
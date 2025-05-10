package com.saraci.websiteanalyzer.service.report;

public interface EmailSender {
    void sendEmail(String to, String subject, String text, String attachmentPath) throws Exception;
}
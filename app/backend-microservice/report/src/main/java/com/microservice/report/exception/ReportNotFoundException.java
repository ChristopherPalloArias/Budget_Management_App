package com.microservice.report.exception;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(String userId, String period) {
        super(String.format("Report not found for user '%s' and period '%s'", userId, period));
    }
}

package com.workruit.us.application.controllers;

import java.util.Date;
import java.util.List;

import lombok.Data;

public class ExceptionResponse {
    private final Date timestamp;
    private final String message;
    private final List<FieldError> details;

    public ExceptionResponse(Date timestamp, String message, List<FieldError> details) {
        super();
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldError> getDetails() {
        return details;
    }

    @Data
    public static class FieldError {
        private String name;
        private String message;
        public FieldError(String name, String message) {
            this.name = name;
            this.message = message;
        }
    }
}
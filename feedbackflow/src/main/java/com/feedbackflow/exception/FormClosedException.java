package com.feedbackflow.exception;

/**
 * Rule 2: Responses cannot be submitted after the form's closing date,
 * before its opening date, or while it is marked inactive.
 * Mapped to HTTP 403 Forbidden by GlobalExceptionHandler.
 */
public class FormClosedException extends RuntimeException {
    public FormClosedException(String message) {
        super(message);
    }
}

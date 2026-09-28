package com.feedbackflow.exception;

/**
 * Rule 1: A student can submit only ONE response per course (feedback form) per semester.
 * Mapped to HTTP 409 Conflict by GlobalExceptionHandler.
 */
public class DuplicateSubmissionException extends RuntimeException {
    public DuplicateSubmissionException(String message) {
        super(message);
    }
}

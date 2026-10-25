package com.crumb.common.exception;

/**
 * BadRequestException - Thrown when client input fails business rules or sanity checks.
 * 
 * WHY: Returns an HTTP 400 with a descriptive error message explaining what failed.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

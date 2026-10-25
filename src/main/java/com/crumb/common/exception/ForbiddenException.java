package com.crumb.common.exception;

/**
 * ForbiddenException - Thrown when an authenticated user lacks the required role (e.g. non-admin).
 * 
 * WHY: Returns an HTTP 403 when a standard customer attempts to invoke admin endpoints.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

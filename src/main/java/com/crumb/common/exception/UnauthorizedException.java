package com.crumb.common.exception;

/**
 * UnauthorizedException - Thrown when a user must be authenticated to perform an action.
 * 
 * WHY: Returns an HTTP 401 when a guest tries to access protected features.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

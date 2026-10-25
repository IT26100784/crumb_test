package com.crumb.common.exception;

/**
 * ResourceNotFoundException - Thrown when a requested entity cannot be found in the database.
 * 
 * WHY: Clearly communicates 404 Not Found errors to callers when IDs do not exist.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

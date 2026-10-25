package com.crumb.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ErrorResponse - Consistent payload structure returned on exceptions.
 * 
 * WHY: Delivers structured error information to frontend and API consumers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    // ENCAPSULATION: private fields with Lombok accessors
    private boolean success;
    private int status;
    private String error;
    private String message;
    private Map<String, String> validationErrors;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

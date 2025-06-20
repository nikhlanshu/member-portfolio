package org.orioz.memberportfolio.exceptions;

import java.time.LocalDateTime;

// Using a record for conciseness in Java 16+
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {
    // You can add constructors or helper methods if needed
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now());
    }
}
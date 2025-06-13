package org.orioz.memberportfolio.exceptions.handler;

import org.orioz.memberportfolio.exceptions.AlreadyHasAdminRoleException;
import org.orioz.memberportfolio.exceptions.EmailAlreadyRegisteredException;
import org.orioz.memberportfolio.exceptions.ErrorResponse;
import org.orioz.memberportfolio.exceptions.InvalidCredentialException;
import org.orioz.memberportfolio.exceptions.MaximumAdminThresholdException;
import org.orioz.memberportfolio.exceptions.MemberNotFoundException;
import org.orioz.memberportfolio.exceptions.UnauthorizedException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public class GlobalWebExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public Mono<ResponseEntity<ErrorResponse>> emailAlreadyRegisteredException(
            EmailAlreadyRegisteredException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.CONFLICT; // 409 Conflict
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                String.format("%s: %s", exchange.getRequest().getMethod().name(), exchange.getRequest().getPath().value())
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }
    @ExceptionHandler(InvalidCredentialException.class)
    public Mono<ResponseEntity<ErrorResponse>> invalidCredentialException(
            InvalidCredentialException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.UNAUTHORIZED; // 409 Conflict
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                String.format("%s: %s", exchange.getRequest().getMethod().name(), exchange.getRequest().getPath().value())
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<ResponseEntity<ErrorResponse>> unauthorizedException(
            UnauthorizedException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.UNAUTHORIZED; // 409 Conflict
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                String.format("%s: %s", exchange.getRequest().getMethod().name(), exchange.getRequest().getPath().value())
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }

    @ExceptionHandler(AlreadyHasAdminRoleException.class)
    public Mono<ResponseEntity<ErrorResponse>> alreadyHasAdminRoleException(
            AlreadyHasAdminRoleException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.CONFLICT; // 409 Conflict
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                String.format("%s: %s", exchange.getRequest().getMethod().name(), exchange.getRequest().getPath().value())
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }

    @ExceptionHandler(MaximumAdminThresholdException.class)
    public Mono<ResponseEntity<ErrorResponse>> maximumAdminThresholdException(
            MaximumAdminThresholdException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                String.format("%s: %s", exchange.getRequest().getMethod().name(), exchange.getRequest().getPath().value())
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }

    @ExceptionHandler(DataAccessException.class)
    public Mono<ResponseEntity<ErrorResponse>> dataAccessException(
            DataAccessException ex, ServerWebExchange exchange) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 409 Conflict
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                exchange.getRequest().getPath().toString()
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleMemberNotFoundException(
            MemberNotFoundException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.NOT_FOUND; // 404 Not Found
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                exchange.getRequest().getPath().toString()
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }

    // --- Validation Exceptions (e.g., @NotBlank, @Email in DTOs) ---
    // This handles errors from @Valid annotation if DTO fields are invalid
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleValidationExceptions(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = Objects.requireNonNull(error.getDefaultMessage());
            errors.put(fieldName, errorMessage);
        });
        // You could also return ErrorResponse for validation, depending on desired format
        return Mono.just(ResponseEntity.badRequest().body(errors)); // 400 Bad Request
    }

    // --- Common Input Errors (e.g., malformed JSON, type mismatch in request body) ---
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleServerWebInputException(
            ServerWebInputException ex, org.springframework.web.server.ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // 400 Bad Request
        ex.getReason();
        String message = ex.getReason();
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            message += " Cause: " + ex.getCause().getMessage();
        }
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().toString()
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }


    // --- Generic Catch-All Exception ---
    // This is a fallback for any unhandled RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericRuntimeException(
            RuntimeException ex, org.springframework.web.server.ServerWebExchange exchange) {
        // IMPORTANT: Log the exception for debugging purposes.
        // In production, use a proper logging framework (e.g., SLF4J with Logback/Log4j2).
        System.err.println("An unhandled error occurred: " + ex.getMessage());
        ex.printStackTrace(); // For development, print stack trace

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500 Internal Server Error
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "An unexpected error occurred. Please try again later.", // Generic message for client
                exchange.getRequest().getPath().toString()
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }
}
package org.orioz.memberportfolio.exceptions.handler;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.exceptions.AlreadyHasAdminRoleException;
import org.orioz.memberportfolio.exceptions.EmailAlreadyRegisteredException;
import org.orioz.memberportfolio.exceptions.ErrorResponse;
import org.orioz.memberportfolio.exceptions.InvalidCredentialException;
import org.orioz.memberportfolio.exceptions.MaximumAdminThresholdException;
import org.orioz.memberportfolio.exceptions.MemberNotFoundException;
import org.orioz.memberportfolio.exceptions.MemberNotInPendingStatusException;
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

@Slf4j
@ControllerAdvice
public class GlobalWebExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public Mono<ResponseEntity<ErrorResponse>> emailAlreadyRegisteredException(
            EmailAlreadyRegisteredException ex, ServerWebExchange exchange) {
        log.error("EmailAlreadyRegisteredException: {}", ex.getMessage(), ex);
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
        log.error("InvalidCredentialException: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.UNAUTHORIZED;
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
        log.error("UnauthorizedException: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.UNAUTHORIZED;
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
        log.error("AlreadyHasAdminRoleException: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.CONFLICT;
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
        log.error("MaximumAdminThresholdException: {}", ex.getMessage(), ex);
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
        log.error("DataAccessException: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
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
        log.error("MemberNotFoundException: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                exchange.getRequest().getPath().toString()
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }

    @ExceptionHandler(MemberNotInPendingStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> memberNotInPendingStatusException(
            MemberNotInPendingStatusException ex, ServerWebExchange exchange) {
        log.error("MemberNotInPendingStatusException: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                exchange.getRequest().getPath().toString()
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleValidationExceptions(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        log.error("Validation exception: {}", ex.getMessage(), ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = Objects.requireNonNull(error.getDefaultMessage());
            errors.put(fieldName, errorMessage);
        });
        return Mono.just(ResponseEntity.badRequest().body(errors));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleServerWebInputException(
            ServerWebInputException ex, ServerWebExchange exchange) {
        log.error("ServerWebInputException: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
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

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericRuntimeException(
            RuntimeException ex, ServerWebExchange exchange) {
        log.error("RuntimeException: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                exchange.getRequest().getPath().toString()
        );
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }
}
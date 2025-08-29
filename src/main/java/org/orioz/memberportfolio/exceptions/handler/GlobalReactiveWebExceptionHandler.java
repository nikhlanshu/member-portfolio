package org.orioz.memberportfolio.exceptions.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.exceptions.ErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Order(-2)
@Component
@Slf4j
public class GlobalReactiveWebExceptionHandler implements WebExceptionHandler {
    private final ObjectMapper objectMapper;

    public GlobalReactiveWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Handling exception before it reaches to controller later: {} - {}", ex.getClass().getSimpleName(), ex);
        // Prevent double error responses
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        HttpStatus status = (ex instanceof ResponseStatusException)
                ? HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value())
                : HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                exchange.getRequest().getPath().toString()
        );

        // Set response headers
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Write error body
        return Mono.fromSupplier(() -> {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                return buffer;
            } catch (Exception e) {
                byte[] fallback = "{\"error\":\"Unable to serialize error response\"}".getBytes(StandardCharsets.UTF_8);
                return exchange.getResponse().bufferFactory().wrap(fallback);
            }
        }).flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer)));
    }
}



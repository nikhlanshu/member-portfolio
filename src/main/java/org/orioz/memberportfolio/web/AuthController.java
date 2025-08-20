package org.orioz.memberportfolio.web;

import jakarta.validation.Valid;
import org.orioz.memberportfolio.dtos.auth.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping(value = "/api/v1/members/auth", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@Valid @RequestBody LoginRequest request) {
        return Mono.just(ResponseEntity.ok("Login successful. You should use basic auth to access secure endpoints."));
    }
}

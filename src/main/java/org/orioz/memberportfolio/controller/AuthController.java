package org.orioz.memberportfolio.controller;

import jakarta.validation.Valid;
import org.orioz.memberportfolio.dtos.auth.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/api/v1/members/auth")
public class AuthController {

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@Valid @RequestBody LoginRequest request) {
        // Normally, you’d return a JWT or session info. For now, test using HTTP Basic Auth
        return Mono.just(ResponseEntity.ok("Login successful. You should use basic auth to access secure endpoints."));
    }
}

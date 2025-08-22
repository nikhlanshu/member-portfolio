package org.orioz.memberportfolio.web;

import jakarta.validation.Valid;
import org.orioz.memberportfolio.dtos.auth.LoginRequest;
import org.orioz.memberportfolio.dtos.auth.LoginResponse;
import org.orioz.memberportfolio.service.auth.LogInService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping(value = "/api/v1/members/auth", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final LogInService logInService;

    public AuthController(LogInService logInService) {
        this.logInService = logInService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return logInService.login(request)
                .map(loginResponse -> ResponseEntity.status(HttpStatus.CREATED).body(loginResponse));
    }
}

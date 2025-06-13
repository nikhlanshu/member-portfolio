package org.orioz.memberportfolio.controller;

import jakarta.validation.Valid;
import org.orioz.memberportfolio.dtos.auth.TokenRequest;
import org.orioz.memberportfolio.service.auth.TokenService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/api/v1/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    public Mono<String> issueToken(@Valid @RequestBody TokenRequest tokenRequest) {
        return tokenService.issueToken(tokenRequest);
    }
}

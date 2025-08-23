package org.orioz.memberportfolio.web;

import jakarta.validation.Valid;
import org.orioz.memberportfolio.dtos.auth.OnBehalfOfTokenRequest;
import org.orioz.memberportfolio.dtos.auth.TokenRequest;
import org.orioz.memberportfolio.dtos.auth.TokenResponse;
import org.orioz.memberportfolio.service.auth.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping(value = "/api/v1/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class TokenController {
    private final TokenService tokenService;
    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    @PostMapping
    public Mono<ResponseEntity<TokenResponse>> issueToken(@Valid @RequestBody TokenRequest tokenRequest) {
        return tokenService.issueAccessToken(tokenRequest)
                .map(memberResponse -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(memberResponse));
    }
    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@Valid @RequestBody OnBehalfOfTokenRequest tokenRequest) {
        return tokenService.refreshToken(tokenRequest)
                .map(memberResponse -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(memberResponse));
    }
}

package org.orioz.memberportfolio.auth;

import org.orioz.memberportfolio.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ReactiveAuthenticationManager authenticationManager;
    private final SecurityProperties securityProperties;

    @Autowired
    public JwtAuthenticationWebFilter(ReactiveAuthenticationManager authenticationManager, SecurityProperties securityProperties) {
        this.authenticationManager = authenticationManager;
        this.securityProperties = securityProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (securityProperties.getPublicPaths().stream().anyMatch(path::equals)) {
            return chain.filter(exchange);
        }
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return Mono.error(new UnauthorizedException("Missing or empty Authorization header"));
        }
        if (authHeader.startsWith(BEARER_PREFIX)) {
            String authToken = authHeader.substring(BEARER_PREFIX.length());
            Authentication auth = new UsernamePasswordAuthenticationToken(null, authToken);

            return authenticationManager.authenticate(auth)
                    .flatMap(authentication -> {
                        SecurityContextImpl securityContext = new SecurityContextImpl(authentication);
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
                    })
                    .onErrorResume(e -> Mono.error(new UnauthorizedException(String.format("Invalid JWT token due to %s", e.getCause().getMessage()))));

        }
        return chain.filter(exchange);
    }
}

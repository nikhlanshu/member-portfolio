package org.orioz.memberportfolio.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtService jwtService;
    @Autowired
    public JwtAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        return jwtService.parseToken(token)
                .map(payload -> {
                    List<GrantedAuthority> authorities = payload.getRoles().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    return (Authentication) new UsernamePasswordAuthenticationToken(
                            payload.getSubject(), token, authorities
                    );
                })
                .onErrorResume(Throwable.class, e -> Mono.empty()); // Catch all errors
    }
}

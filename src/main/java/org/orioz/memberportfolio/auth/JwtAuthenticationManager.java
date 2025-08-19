package org.orioz.memberportfolio.auth;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.exceptions.UnauthorizedException;
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

@Slf4j
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
        return jwtService.inspectToken(token)
                .map(payload -> {
                    log.info("Token has not expired. So Getting next set up validation");
                    List<GrantedAuthority> authorities = payload.getRoles().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    log.debug("Listed Authorities: "+authorities);
                    return (Authentication) new UsernamePasswordAuthenticationToken(payload.getSubject(), token, authorities);
                })
                .onErrorResume(e -> {
                    log.error("Error occurred "+e.getMessage());
                    return Mono.error(new UnauthorizedException(e.getMessage()));
                });
    }
}

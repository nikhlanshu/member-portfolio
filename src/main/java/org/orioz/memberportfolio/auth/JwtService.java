package org.orioz.memberportfolio.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.dtos.auth.TokenPayload;
import org.orioz.memberportfolio.exceptions.UnauthorizedException;
import org.orioz.memberportfolio.models.Member;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class JwtService {
    private final SecretKey secretKey;
    private final SecurityProperties securityProperties;
    public JwtService(SecurityProperties securityProperties) {
        this.secretKey = Keys.hmacShaKeyFor(securityProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.securityProperties = securityProperties;
    }

    public Mono<String> generateToken(Member member) {
        log.info(String.format("generate token called for userId : %s", member.getId()));
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plus(securityProperties.getExpiry().getDuration(), securityProperties.getExpiry().getUnit());

        TokenPayload payload = new TokenPayload(
                member.getId(),
                member.getRoles().stream().map(Enum::name).toList(),
                member.getStatus().name(),
                issuedAt,
                expiration
        );

        return Mono.fromCallable(() -> Jwts.builder()
                .setSubject(payload.getSubject())
                .claim("roles", payload.getRoles())
                .claim("status", payload.getStatus())
                .setIssuedAt(Date.from(payload.getIssuedAt()))
                .setExpiration(Date.from(payload.getExpiration()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<TokenPayload> parseToken(String token) {
        log.info(String.format("parsing token token for: %s", token));
        return Mono.fromCallable(() -> {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return new TokenPayload(
                    claims.getSubject(),
                    claims.get("roles", List.class),
                    claims.get("status", String.class),
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> validateToken(String token, String expectedMemberId) {
        return parseToken(token)
                .map(payload -> payload.getSubject().equals(expectedMemberId));
    }

    public Mono<TokenPayload> inspectToken(String token) {
        return parseToken(token)
                .map(tokenPayload -> {
                    if (tokenPayload.getExpiration().isBefore(Instant.now())) {
                        log.warn("Token has expired");
                        throw new UnauthorizedException("Token has expired");
                    }
                    return tokenPayload;
                });
    }

}

package org.orioz.memberportfolio.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.orioz.memberportfolio.dtos.auth.TokenPayload;
import org.orioz.memberportfolio.models.Member;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private static final String SECRET = "k8F^s@3pL!zA#9vRb7Ty6mZ*qD&XhN$WcMj4EuP!nLx2YgTfCz@VmKr#B1UsPw3De";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public Mono<String> generateToken(Member member) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plus(2, ChronoUnit.HOURS);

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
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<TokenPayload> parseToken(String token) {
        return Mono.fromCallable(() -> {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
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
}

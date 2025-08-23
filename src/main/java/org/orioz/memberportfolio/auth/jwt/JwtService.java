package org.orioz.memberportfolio.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.properties.SecurityProperties;
import org.orioz.memberportfolio.dtos.auth.AccessTokenPayload;
import org.orioz.memberportfolio.dtos.auth.IDTokenPayload;
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

    public Mono<String> generateIdToken(Member member) {
        log.info(String.format("generate ID token called for userId : %s", member.getId()));
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plus(securityProperties.getExpiry().getDuration(), securityProperties.getExpiry().getUnit());

        IDTokenPayload payload = new IDTokenPayload(
                member.getId(),
                member.getStatus().name(),
                member.getFirstName(),
                member.getLastName(),
                member.getEmail(),
                member.getDateOfBirth(),
                issuedAt,
                expiration
        );

        return Mono.fromCallable(() -> Jwts.builder()
                .setSubject(payload.getSubject())
                .claim("status", payload.getStatus())
                .claim("firstName", payload.getFirstName())
                .claim("lastName", payload.getLastName())
                .claim("dateOfBirth", payload.getDob().toString())
                .claim("email", payload.getEmail())
                .setIssuedAt(Date.from(payload.getIssuedAt()))
                .setExpiration(Date.from(payload.getExpiration()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> generateAccessToken(Member member) {
        log.info(String.format("generate Access token called for userId : %s", member.getId()));
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plus(securityProperties.getExpiry().getDuration(), securityProperties.getExpiry().getUnit());

        AccessTokenPayload payload = new AccessTokenPayload(
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

    public Mono<AccessTokenPayload> parseAccessToken(String token) {
        log.info(String.format("parsing token token for: %s", token));
        return Mono.fromCallable(() -> {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return new AccessTokenPayload(
                    claims.getSubject(),
                    claims.get("roles", List.class),
                    claims.get("status", String.class),
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<AccessTokenPayload> inspectAccessToken(String token) {
        return parseAccessToken(token)
                .map(accessTokenPayload -> {
                    if (accessTokenPayload.getExpiration().isBefore(Instant.now())) {
                        log.warn("Token has expired");
                        throw new UnauthorizedException("Token has expired");
                    }
                    if (accessTokenPayload.getRoles().stream().noneMatch(role -> role.equals(Member.Role.MEMBER.name()))) {
                        log.warn("Not a member");
                        throw new UnauthorizedException("Not a member");
                    }

                    if (!accessTokenPayload.getStatus().equals(Member.Status.CONFIRMED.name())) {
                        log.warn("Member Not Confirmed Yet");
                        throw new UnauthorizedException("Member Not Confirmed Yet");
                    }
                    return accessTokenPayload;
                });
    }

    public Mono<AccessTokenPayload> parseAccessTokenAllowExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Mono.just(toAccessTokenPayload(claims));
        } catch (ExpiredJwtException ex) {
            // Token expired but claims are still valid
            log.warn("Access token expired, but claims recovered for refresh");
            return Mono.just(toAccessTokenPayload(ex.getClaims()));
        } catch (JwtException ex) {
            log.error("Invalid token", ex);
            return Mono.error(new UnauthorizedException("Invalid token"));
        }
    }
    private AccessTokenPayload toAccessTokenPayload(Claims claims) {
        return new AccessTokenPayload(
                claims.getSubject(),
                claims.get("roles", List.class),
                claims.get("status", String.class),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant()
        );
    }
}

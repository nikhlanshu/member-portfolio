package org.orioz.memberportfolio.auth.entitlement;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.jwt.JwtService;
import org.orioz.memberportfolio.dtos.auth.EmailEntitlementCheckRequest;
import org.orioz.memberportfolio.dtos.auth.IEntitlementCheckRequest;
import org.orioz.memberportfolio.exceptions.UnauthorizedException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class EntitlementCheckByEmail implements IEntitlementCheck {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    public EntitlementCheckByEmail(JwtService jwtService, MemberRepository memberRepository) {
        this.jwtService = jwtService;
        this.memberRepository = memberRepository;
    }

    @Override
    public Mono<Boolean> isApplicable(IEntitlementCheckRequest request) {
        return Mono.just(request instanceof EmailEntitlementCheckRequest);
    }

    @Override
    public Mono<Void> apply(IEntitlementCheckRequest request) {
        if (!(request instanceof EmailEntitlementCheckRequest emailRequest)) {
            return Mono.error(new IllegalArgumentException("Invalid request type"));
        }

        String email = emailRequest.getEmail();

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    log.debug("Security context retrieved: {}", ctx);
                    return Mono.justOrEmpty(ctx.getAuthentication());
                })
                .flatMap(auth -> {
                    log.debug("Authentication found: {}", auth.getName());
                    String token = (String) auth.getCredentials();
                    log.debug("JWT token extracted from authentication credentials");
                    return jwtService.inspectAccessToken(token)
                            .doOnNext(tokenPayload -> log.debug(
                                    "Token inspected successfully, subject: {}", tokenPayload.getSubject()));
                })
                .flatMap(tokenPayload ->
                        memberRepository.findByEmail(email)
                                .map(member -> {
                                    boolean match = member.getId().equals(tokenPayload.getSubject())
                                            && member.getRoles().stream()
                                            .map(Member.Role::name)
                                            .toList()
                                            .containsAll(tokenPayload.getRoles());

                                    log.debug("Member retrieved: {}, matches token subject: {}", member.getId(), match);
                                    return match;
                                })
                                .defaultIfEmpty(false)
                )
                .flatMap(result -> {
                    log.debug("Entitlement validation result for email {}: {}", email, result);
                    if (!result) {
                        return Mono.error(new UnauthorizedException("Entitlement failed"));
                    }
                    return Mono.empty();
                });
    }
}

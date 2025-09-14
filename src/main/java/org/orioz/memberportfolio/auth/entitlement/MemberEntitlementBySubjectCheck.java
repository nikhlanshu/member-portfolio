package org.orioz.memberportfolio.auth.entitlement;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.jwt.JwtService;
import org.orioz.memberportfolio.dtos.auth.IEntitlementCheckRequest;
import org.orioz.memberportfolio.dtos.auth.MemberEntitlementCheckBySubjectRequest;
import org.orioz.memberportfolio.exceptions.BadRequestException;
import org.orioz.memberportfolio.exceptions.UnauthorizedException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class MemberEntitlementBySubjectCheck implements IEntitlementCheck {
    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    public MemberEntitlementBySubjectCheck(JwtService jwtService, MemberRepository memberRepository) {
        this.jwtService = jwtService;
        this.memberRepository = memberRepository;
    }

    @Override
    public Mono<Boolean> isApplicable(IEntitlementCheckRequest request) {
        return Mono.just(request instanceof MemberEntitlementCheckBySubjectRequest);
    }

    @Override
    public Mono<Void> apply(IEntitlementCheckRequest request) {
        if (!(request instanceof MemberEntitlementCheckBySubjectRequest memberCheckRequest)) {
            return Mono.error(new BadRequestException("Invalid request type"));
        }
        log.info("Applying {}", this.getClass().getSimpleName());
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
                        memberRepository.findById(tokenPayload.getSubject())
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
                    log.debug("Entitlement validation result for subject: {}", result);
                    if (!result) {
                        return Mono.error(new UnauthorizedException("Entitlement failed"));
                    }
                    return Mono.empty();
                });
    }
}

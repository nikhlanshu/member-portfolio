package org.orioz.memberportfolio.auth.entitlement;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.jwt.JwtService;
import org.orioz.memberportfolio.dtos.auth.AdminVoidEntitlementCheckRequest;
import org.orioz.memberportfolio.dtos.auth.IEntitlementCheckRequest;
import org.orioz.memberportfolio.exceptions.UnauthorizedException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AdminEntitlementCheck implements IEntitlementCheck {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    public AdminEntitlementCheck(JwtService jwtService, MemberRepository memberRepository) {
        this.jwtService = jwtService;
        this.memberRepository = memberRepository;
    }

    @Override
    public Mono<Boolean> isApplicable(IEntitlementCheckRequest request) {
        return Mono.just(request instanceof AdminVoidEntitlementCheckRequest);
    }

    @Override
    public Mono<Void> apply(IEntitlementCheckRequest request) {
        if (!(request instanceof AdminVoidEntitlementCheckRequest)) {
            return Mono.error(new IllegalArgumentException("Invalid request type"));
        }

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> Mono.justOrEmpty(ctx.getAuthentication()))
                .flatMap(auth -> {
                    log.debug("Authentication found: {}", auth.getName());
                    String token = (String) auth.getCredentials();
                    return jwtService.inspectAccessToken(token)
                            .doOnNext(tokenPayload -> log.debug("Token inspected, subject: {}", tokenPayload.getSubject()));
                })
                .flatMap(tokenPayload ->
                        memberRepository.findById(tokenPayload.getSubject())
                                .map(member -> {
                                    boolean isAdmin = member.getId().equals(tokenPayload.getSubject())
                                            && member.getRoles().stream()
                                            .map(Member.Role::name)
                                            .toList()
                                            .containsAll(tokenPayload.getRoles());

                                    log.debug("Member retrieved: {}, matches token: {}", member.getId(), isAdmin);
                                    return isAdmin;
                                })
                                .defaultIfEmpty(false)
                )
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.error(new UnauthorizedException("Admin entitlement check failed"));
                    }
                    return Mono.empty();
                });
    }
}

package org.orioz.memberportfolio.auth.entitlement;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.jwt.JwtService;
import org.orioz.memberportfolio.dtos.auth.AssignRoleEntitlementCheckRequest;
import org.orioz.memberportfolio.dtos.auth.IEntitlementCheckRequest;
import org.orioz.memberportfolio.exceptions.BadRequestException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AssignRoleEntitlementCheck implements IEntitlementCheck {
    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    public AssignRoleEntitlementCheck(JwtService jwtService, MemberRepository memberRepository) {
        this.jwtService = jwtService;
        this.memberRepository = memberRepository;
    }
    @Override
    public Mono<Boolean> isApplicable(IEntitlementCheckRequest request) {
        return Mono.just(request instanceof AssignRoleEntitlementCheckRequest);
    }

    @Override
    public Mono<Void> apply(IEntitlementCheckRequest request) {
        AssignRoleEntitlementCheckRequest financialRequest = (AssignRoleEntitlementCheckRequest) request;
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
                                .filter(Member::isMemberConfirmed)
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.info("Member not confirmed: {}", tokenPayload.getSubject());
                                    return Mono.error(new BadRequestException("Member not confirmed"));
                                }))
                                .filter(member -> member.getHighestRole().canAssign(financialRequest.getRole()))
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.info("Role not authorized for member: {}", tokenPayload.getSubject());
                                    return Mono.error(new BadRequestException("Role not authorized for this operation"));
                                }))
                                .then()
                );
    }

}

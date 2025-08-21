package org.orioz.memberportfolio.validator;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.JwtService;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.service.member.MemberService;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component("entitlementValidator")
public class EntitlementValidator {

    private final MemberService memberService;
    private final JwtService jwtService;

    public EntitlementValidator(MemberService memberService, JwtService jwtService) {
        this.memberService = memberService;
        this.jwtService = jwtService;
    }

    /**
     * Validate entitlement for the currently authenticated user against the provided email.
     */
    public Mono<Boolean> validateEntitlement(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        log.info("Starting entitlement validation for email: {}", email);

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    log.debug("Security context retrieved: {}", ctx);
                    return Mono.justOrEmpty(ctx.getAuthentication());
                })
                .flatMap(auth -> {
                    log.debug("Authentication found: {}", auth.getName());
                    String token = (String) auth.getCredentials();
                    log.debug("JWT token extracted from authentication credentials");
                    return jwtService.inspectToken(token)
                            .doOnNext(tokenPayload -> log.debug("Token inspected successfully, subject: {}", tokenPayload.getSubject()));
                })
                .flatMap(tokenPayload ->
                        memberService.getMemberByEmail(email)
                                .map(member -> {
                                    boolean match = member.getId().equals(tokenPayload.getSubject())
                                            && (member.getRoles().stream()
                                            .map(Member.Role::name).toList()).containsAll(tokenPayload.getRoles());
                                    log.debug("Member retrieved from DB: {}, matches token subject: {}", member.getId(), match);
                                    return match;
                                })
                                .defaultIfEmpty(false)
                )
                .doOnNext(result -> log.debug("Entitlement validation result for email {}: {}", email, result));
    }

    /**
     * Validate entitlement for the currently authenticated user against the provided id.
     */
    public Mono<Boolean> validateEntitlementById(String id) {
        Objects.requireNonNull(id, "Email cannot be null");
        log.debug("Starting entitlement validation for email: {}", id);

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    log.debug("Security context retrieved: {}", ctx);
                    return Mono.justOrEmpty(ctx.getAuthentication());
                })
                .flatMap(auth -> {
                    log.debug("Authentication found: {}", auth.getName());
                    String token = (String) auth.getCredentials();
                    log.debug("JWT token extracted from authentication credentials");
                    return jwtService.inspectToken(token)
                            .doOnNext(tokenPayload -> log.debug("Token inspected successfully, subject: {}", tokenPayload.getSubject()));
                })
                .flatMap(tokenPayload ->
                        memberService.getMemberById(id)
                                .map(member -> {
                                    boolean match = member.getId().equals(tokenPayload.getSubject())
                                            && (member.getRoles().stream()
                                            .map(Member.Role::name).toList()).containsAll(tokenPayload.getRoles());
                                    log.debug("Member retrieved from DB: {}, matches token subject: {}", member.getId(), match);
                                    return match;
                                })
                                .defaultIfEmpty(false)
                )
                .doOnNext(result -> log.debug("Entitlement validation result for id {}: {}", id, result));
    }

    public Mono<Boolean> validateAdminEntitlement() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    log.debug("Security context retrieved: {}", ctx);
                    return Mono.justOrEmpty(ctx.getAuthentication());
                })
                .flatMap(auth -> {
                    log.debug("Authentication found: {}", auth.getName());
                    String token = (String) auth.getCredentials();
                    log.debug("JWT token extracted from authentication credentials");
                    return jwtService.inspectToken(token)
                            .doOnNext(tokenPayload -> log.debug("Token inspected successfully, subject: {}", tokenPayload.getSubject()));
                })
                .flatMap(tokenPayload ->
                        memberService.getMemberById(tokenPayload.getSubject())
                                .map(member -> {
                                    boolean match = member.getId().equals(tokenPayload.getSubject())
                                            && (member.getRoles().stream()
                                            .map(Member.Role::name).toList()).containsAll(tokenPayload.getRoles());
                                    log.debug("Member retrieved from DB: {}, matches token subject: {}", member.getId(), match);
                                    return match;
                                })
                                .defaultIfEmpty(false)
                )
                .doOnNext(result -> log.debug("Admin Entitlement validation result {}", result));
    }
}



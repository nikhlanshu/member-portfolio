package org.orioz.memberportfolio.service.member;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.entitlement.EntitlementValidator;
import org.orioz.memberportfolio.dtos.auth.EmailEntitlementCheckRequest;
import org.orioz.memberportfolio.dtos.member.MemberRegistrationRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.exceptions.EmailAlreadyRegisteredException;
import org.orioz.memberportfolio.exceptions.MemberNotFoundException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class MemberPortfolioService implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntitlementValidator entitlementValidator;

    @Autowired
    public MemberPortfolioService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, EntitlementValidator entitlementValidator) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.entitlementValidator = entitlementValidator;
    }

    @Override
    public Mono<MemberResponse> registerMember(MemberRegistrationRequest request) {
        log.info("Attempting to register member with email: {}", request.getEmail());

        return memberRepository.findByEmail(request.getEmail())
                .flatMap(existingMember -> {
                    log.debug("Email {} already exists in DB with memberId: {}", request.getEmail(), existingMember.getId());
                    return Mono.error(new EmailAlreadyRegisteredException(
                            request.getEmail() + " is already registered, try to login or reset password"
                    ));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    Member newMember = Member.toMember(request, passwordEncoder);
                    log.debug("Saving new member: {}", newMember);
                    return memberRepository.save(newMember);
                }))
                .cast(Member.class)
                .map(member -> {
                    log.info("Member registered successfully: {}", member.getEmail());
                    return MemberResponse.fromMember(member);
                });
    }

    @Override
    public Mono<MemberResponse> getMemberByEmail(String email) {
        log.info("Fetching member by email: {}", email);
        return entitlementValidator.validate(new EmailEntitlementCheckRequest(email))
                .then(memberRepository.findByEmail(email)
                        .doOnNext(member -> log.debug("Member found: {}", member))
                        .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found for email: " + email)))
                        .map(MemberResponse::fromMember)
                )
                .doOnSuccess(response -> log.info("MemberResponse prepared for email: {}", email))
                .doOnError(error -> log.warn("Failed to fetch member for email {}: {}", email, error.getMessage()));
    }


    @Override
    public Mono<MemberResponse> getMemberById(String id) {
        log.info("Fetching member by ID: {}", id);
        return memberRepository.findById(id)
                .doOnNext(member -> log.debug("Member found: {}", member))
                .map(MemberResponse::fromMember);
    }
}
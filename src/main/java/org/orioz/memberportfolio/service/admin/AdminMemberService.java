package org.orioz.memberportfolio.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.entitlement.EntitlementValidator;
import org.orioz.memberportfolio.config.AdminConfig;
import org.orioz.memberportfolio.dtos.admin.AdminCreationRequest;
import org.orioz.memberportfolio.dtos.admin.PageResponse;
import org.orioz.memberportfolio.dtos.auth.AdminVoidEntitlementCheckRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.exceptions.AlreadyHasAdminRoleException;
import org.orioz.memberportfolio.exceptions.MaximumAdminThresholdException;
import org.orioz.memberportfolio.exceptions.MemberNotFoundException;
import org.orioz.memberportfolio.exceptions.MemberNotInPendingStatusException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminMemberService implements AdminService {
    private final MemberRepository memberRepository;
    private final AdminConfig adminConfig;
    private final EntitlementValidator entitlementValidator;

    @Autowired
    public AdminMemberService(MemberRepository memberRepository, AdminConfig adminConfig, EntitlementValidator entitlementValidator) {
        this.memberRepository = memberRepository;
        this.adminConfig = adminConfig;
        this.entitlementValidator = entitlementValidator;
    }

    public Mono<MemberResponse> addAdminRole(AdminCreationRequest adminCreationRequest) {
        log.info("Attempting to add ADMIN role to memberId={}", adminCreationRequest.getMemberId());

        return memberRepository.findById(adminCreationRequest.getMemberId())
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with ID: " + adminCreationRequest.getMemberId())))
                .flatMap(member -> {
                    log.debug("Member retrieved: {}", member);
                    if (member.getRoles() != null && member.getRoles().contains(Member.Role.ADMIN)) {
                        log.warn("Member {} already has ADMIN role", member.getId());
                        return Mono.error(new AlreadyHasAdminRoleException("Member already has ADMIN role."));
                    }

                    return memberRepository.findByRolesContaining(Member.Role.ADMIN).count()
                            .flatMap(adminCount -> {
                                log.debug("Current number of admins: {}", adminCount);
                                if (adminCount >= adminConfig.getMaxMember()) {
                                    log.warn("Maximum number of admin members reached: {}", adminConfig.getMaxMember());
                                    return Mono.error(new MaximumAdminThresholdException(
                                            "Maximum " + adminConfig.getMaxMember() + " admin members allowed."));
                                }

                                List<Member.Role> existingRoles = member.getRoles();
                                existingRoles.add(Member.Role.ADMIN);
                                member.setRoles(existingRoles);
                                member.setUpdatedAt(LocalDateTime.now());

                                log.info("Adding ADMIN role to member {} and saving", member.getId());
                                return memberRepository.save(member)
                                        .doOnSuccess(saved -> log.debug("Member saved: {}", saved));
                            })
                            .cast(Member.class)
                            .map(MemberResponse::fromMember);
                });
    }

    @Override
    public Mono<MemberResponse> confirmMember(String memberEmail) {
        log.info("Confirming member with ID={}", memberEmail);
        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .then(memberRepository.findByEmail(memberEmail))
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with ID: " + memberEmail)))
                .flatMap(member -> {
                    log.debug("Member retrieved for confirmation: {}", member);
                    if (member.getStatus() == Member.Status.PENDING) {
                        member.setStatus(Member.Status.CONFIRMED);
                        member.setUpdatedAt(LocalDateTime.now());
                        log.info("Member {} confirmed", memberEmail);
                        return memberRepository.save(member)
                                .doOnSuccess(saved -> log.debug("Member saved after confirmation: {}", saved))
                                .map(MemberResponse::fromMember);
                    } else {
                        log.warn("Cannot confirm member {} as status is not PENDING (current status: {})",
                                member.getId(), member.getStatus());
                        return Mono.error(new MemberNotInPendingStatusException(String.format(
                                "Member Id %s not in pending status (current status: %s)",
                                member.getId(), member.getStatus())));
                    }
                });
    }

    @Override
    public Mono<MemberResponse> rejectMember(String memberEmail) {
        log.info("Rejecting member with ID={}", memberEmail);
        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .then(memberRepository.findByEmail(memberEmail))
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with ID: " + memberEmail)))
                .flatMap(member -> {
                    log.debug("Member retrieved for rejection: {}", member);
                    if (member.getStatus() == Member.Status.PENDING) {
                        member.setStatus(Member.Status.REJECTED);
                        member.setUpdatedAt(LocalDateTime.now());
                        log.info("Member {} rejected", memberEmail);
                        return memberRepository.save(member)
                                .doOnSuccess(saved -> log.debug("Member saved after rejection: {}", saved))
                                .map(MemberResponse::fromMember);
                    } else {
                        log.warn("Cannot reject member {} as status is not PENDING (current status: {})",
                                member.getId(), member.getStatus());
                        return Mono.error(new MemberNotInPendingStatusException(String.format(
                                "Member Id %s not in pending status (current status: %s)",
                                member.getId(), member.getStatus())));
                    }
                });
    }

    @Override
    public Mono<PageResponse<MemberResponse>> getMembersByStatus(Member.Status status, Pageable pageable) {
        log.info("Fetching members with status={} and pageable={}", status, pageable);
        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .then(Mono.zip(memberRepository.findByStatus(status, pageable).collectList(), memberRepository.countByStatus(status)))
                .map(tuple -> {
                    List<Member> members = tuple.getT1();
                    Long totalCount = tuple.getT2();
                    log.debug("Fetched {} members with status={}, totalCount={}", members.size(), status, totalCount);
                    return new PageImpl<>(members, pageable, totalCount);
                })
                .map(memberPage -> {
                    List<MemberResponse> memberResponses = memberPage.getContent().stream()
                            .map(MemberResponse::fromMember)
                            .collect(Collectors.toList());
                    log.debug("Converted members to MemberResponse: {}", memberResponses);
                    return PageResponse.fromPage(new PageImpl<>(
                            memberResponses,
                            memberPage.getPageable(),
                            memberPage.getTotalElements()
                    ));
                });
    }
}
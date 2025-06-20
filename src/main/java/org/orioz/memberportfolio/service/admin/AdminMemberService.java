package org.orioz.memberportfolio.service.admin;

import org.orioz.memberportfolio.config.AdminConfig;
import org.orioz.memberportfolio.dtos.admin.AdminCreationRequest;
import org.orioz.memberportfolio.dtos.admin.PageResponse;
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

@Service
public class AdminMemberService implements AdminService {
    private final MemberRepository memberRepository;
    private final AdminConfig adminConfig;

    @Autowired
    public AdminMemberService(MemberRepository memberRepository, AdminConfig adminConfig) {
        this.memberRepository = memberRepository;
        this.adminConfig = adminConfig;
    }

    public Mono<MemberResponse> addAdminRole(AdminCreationRequest adminCreationRequest) {
        return memberRepository.findById(adminCreationRequest.getMemberId())
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with ID: " + adminCreationRequest.getMemberId())))
                .flatMap(member -> {
                    // Check if member already has ADMIN role
                    if (member.getRoles() != null && member.getRoles().contains(Member.Role.ADMIN)) {
                        return Mono.error(new AlreadyHasAdminRoleException("Member already has ADMIN role."));
                    }
                    // Check if maximum 2 admins limit is reached
                    return memberRepository.findByRolesContaining(Member.Role.ADMIN).count()
                            .flatMap(adminCount -> {
                                if (adminCount >= adminConfig.getMaxMember()) {
                                    return Mono.error(new MaximumAdminThresholdException("Maximum 2 admin members allowed."));
                                }
                                List<Member.Role> existingRoles = member.getRoles();
                                existingRoles.add(Member.Role.ADMIN);
                                member.setRoles(existingRoles);
                                member.setUpdatedAt(LocalDateTime.now());
                                return memberRepository.save(member);
                            })
                            .cast(Member.class)
                            .map(MemberResponse::fromMember);
                });
    }

    @Override
    public Mono<MemberResponse> confirmMember(String memberId) {
        return memberRepository.findById(memberId)
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with ID: " + memberId)))
                .flatMap(member -> {
                    if (member.getStatus() == Member.Status.PENDING) {
                        member.setStatus(Member.Status.CONFIRMED);
                        member.setUpdatedAt(LocalDateTime.now());
                        return memberRepository.save(member)
                                .map(MemberResponse::fromMember);
                    } else {
                        // If not PENDING, throw error or return existing member response
                        return Mono.error(new MemberNotInPendingStatusException(String.format("Member Id %s not in pending status (current status: %s)", member.getId(), member.getStatus())));
                    }
                });
    }

    @Override
    public Mono<MemberResponse> rejectMember(String memberId) {
        return memberRepository.findById(memberId)
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with ID: " + memberId)))
                .flatMap(member -> {
                    if (member.getStatus() == Member.Status.PENDING) {
                        member.setStatus(Member.Status.REJECTED);
                        member.setUpdatedAt(LocalDateTime.now());
                        return memberRepository.save(member)
                                .map(MemberResponse::fromMember);
                    } else {
                        // If not PENDING, throw error or return existing member response
                        return Mono.error(new MemberNotInPendingStatusException(String.format("Member Id %s not in pending status (current status: %s)", member.getId(), member.getStatus())));
                    }
                });
    }

    @Override
    public Mono<PageResponse<MemberResponse>> getMembersByStatus(Member.Status status, Pageable pageable) {
        // 1. Get the Flux of members for the current page and convert to List
        Mono<List<Member>> membersMono = memberRepository.findByStatus(status, pageable).collectList();

        // 2. Get the total count of members matching the status
        Mono<Long> countMono = memberRepository.countByStatus(status);

        // 3. Combine both results to create a Page<Member> object
        return Mono.zip(membersMono, countMono)
                .map(tuple -> {
                    List<Member> members = tuple.getT1();
                    Long totalCount = tuple.getT2();
                    // Create the Spring Data Page<Member>
                    return new PageImpl<>(members, pageable, totalCount);
                })
                .map(memberPage -> {
                    // Convert Page<Member> content to List<MemberResponse>
                    List<MemberResponse> memberResponses = memberPage.getContent().stream()
                            .map(MemberResponse::fromMember)
                            .collect(Collectors.toList());
                    return PageResponse.fromPage(new PageImpl<>(
                            memberResponses,
                            memberPage.getPageable(),
                            memberPage.getTotalElements()
                    ));
                });
    }
}
package org.orioz.memberportfolio.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.entitlement.EntitlementValidator;
import org.orioz.memberportfolio.auth.properties.SendCommunication;
import org.orioz.memberportfolio.comm.dto.CommunicationStage;
import org.orioz.memberportfolio.comm.dto.TemplateProvider;
import org.orioz.memberportfolio.config.AdminConfig;
import org.orioz.memberportfolio.dtos.admin.AdminCreationRequest;
import org.orioz.memberportfolio.dtos.admin.MembershipUpdateRequest;
import org.orioz.memberportfolio.dtos.admin.PageResponse;
import org.orioz.memberportfolio.dtos.auth.AdminVoidEntitlementCheckRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.exceptions.AlreadyHasAdminRoleException;
import org.orioz.memberportfolio.exceptions.BadRequestException;
import org.orioz.memberportfolio.exceptions.MaximumAdminThresholdException;
import org.orioz.memberportfolio.exceptions.MemberNotFoundException;
import org.orioz.memberportfolio.exceptions.MemberNotInPendingStatusException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.orioz.memberportfolio.repositories.SendCommunicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminMemberService implements AdminService {
    private final MemberRepository memberRepository;
    private final AdminConfig adminConfig;
    private final EntitlementValidator entitlementValidator;

    private final SendCommunication sendCommunication;

    private final SendCommunicationRepository sendCommunicationRepository;

    @Autowired
    public AdminMemberService(MemberRepository memberRepository, AdminConfig adminConfig, EntitlementValidator entitlementValidator, SendCommunication sendCommunication, SendCommunicationRepository sendCommunicationRepository) {
        this.memberRepository = memberRepository;
        this.adminConfig = adminConfig;
        this.entitlementValidator = entitlementValidator;
        this.sendCommunication = sendCommunication;
        this.sendCommunicationRepository = sendCommunicationRepository;
    }

    @Override
    public Mono<MemberResponse> addAdminRole(AdminCreationRequest adminCreationRequest) {
        String memberId = adminCreationRequest.getMemberId();
        log.info("Attempting to add ADMIN role to memberId={}", memberId);

        return memberRepository.findById(memberId)
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with ID: " + memberId)))
                .filter(member -> member.getRoles() == null || !member.getRoles().contains(Member.Role.ADMIN))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Member {} already has ADMIN role", memberId);
                    return Mono.error(new AlreadyHasAdminRoleException("Member already has ADMIN role."));
                }))
                .flatMap(member -> memberRepository.findByRolesContaining(Member.Role.ADMIN).count()
                        .filter(adminCount -> adminCount < adminConfig.getMaxMember())
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("Maximum number of admin members reached: {}", adminConfig.getMaxMember());
                            return Mono.error(new MaximumAdminThresholdException("Maximum " + adminConfig.getMaxMember() + " admin members allowed."));
                        }))
                        .map(adminCount -> member)
                )
                .flatMap(member -> {
                    member.getRoles().add(Member.Role.ADMIN);
                    log.info("Assigning ADMIN role to member {}", memberId);
                    return memberRepository.save(member)
                            .doOnSuccess(saved -> log.debug("Member {} saved with new ADMIN role", saved.getId()));
                })

                .map(MemberResponse::fromMember);
    }


    @Override
    public Mono<MemberResponse> confirmMember(String memberEmail) {
        log.info("Confirming member with ID={}", memberEmail);

        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .then(memberRepository.findByEmail(memberEmail))
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with ID: " + memberEmail)))
                .filter(member -> member.getStatus() == Member.Status.PENDING)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Cannot confirm member {} as status is not PENDING", memberEmail);
                    return Mono.error(new MemberNotInPendingStatusException("Member Id " + memberEmail + " not in pending status"));
                }))
                .flatMap(member -> {
                    member.setStatus(Member.Status.CONFIRMED);
                    member.setRegisteredSince(LocalDate.now());
                    log.info("Member {} confirmed", memberEmail);

                    return memberRepository.save(member)
                            .doOnSuccess(saved -> log.debug("Member saved after confirmation: {}", saved))
                            .map(MemberResponse::fromMember)
                            .doOnSuccess(memberResponse -> {
                                log.info("Member approved successfully: {}", member.getEmail());
                                TemplateProvider templateProvider = TemplateProvider.builder()
                                        .toEmail(member.getEmail())
                                        .smtpConfig(sendCommunication.getFrom().get("GMAIL"))
                                        .template(sendCommunication.getTemplates().get(CommunicationStage.APPROVAL.name()))
                                        .variables(Map.of("username", member.getFirstName()))
                                        .build();
                                sendCommunicationRepository.sendEmail(templateProvider)
                                        .subscribe();
                            });
                });
    }

    @Override
    public Mono<MemberResponse> rejectMember(String memberEmail) {
        log.info("Rejecting member with ID={}", memberEmail);

        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .then(memberRepository.findByEmail(memberEmail))
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with ID: " + memberEmail)))
                .filter(member -> member.getStatus() == Member.Status.PENDING)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Cannot reject member {} as status is not PENDING", memberEmail);
                    return Mono.error(new MemberNotInPendingStatusException("Member Id " + memberEmail + " not in pending status"));
                }))
                .flatMap(member -> {
                    member.setStatus(Member.Status.REJECTED);
                    log.info("Member {} rejected", memberEmail);

                    return memberRepository.save(member)
                            .doOnSuccess(saved -> log.debug("Member saved after rejection: {}", saved))
                            .map(MemberResponse::fromMember)
                            .doOnSuccess(memberResponse -> {
                                log.info("Member Rejected: {}", member.getEmail());
                                TemplateProvider templateProvider = TemplateProvider.builder()
                                        .toEmail(member.getEmail())
                                        .smtpConfig(sendCommunication.getFrom().get("GMAIL"))
                                        .template(sendCommunication.getTemplates().get(CommunicationStage.REJECTION.name()))
                                        .variables(Map.of("username", member.getFirstName()))
                                        .build();
                                sendCommunicationRepository.sendEmail(templateProvider)
                                        .subscribe();
                            });
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

    /**
     * Update membership details for a member.
     *
     * @param memberEmail member's email
     * @param request DTO containing duration, start date, and amount
     * @return Updated MemberResponse
     */
    @Override
    public Mono<MemberResponse> updateMembershipForMember(String memberEmail, MembershipUpdateRequest request) {
        log.info("Updating membership for memberEmail={}", memberEmail);

        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .doOnSuccess(v -> log.debug("Entitlement validated for memberEmail={}", memberEmail))
                .then(memberRepository.findByEmail(memberEmail))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Member not found with email={}", memberEmail);
                    return Mono.error(new MemberNotFoundException("Member not found"));
                }))
                .filter(member -> member.getStatus() == Member.Status.CONFIRMED)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Membership update rejected for memberEmail={} as status is not CONFIRMED", memberEmail);
                    return Mono.error(new BadRequestException("Membership can only be updated for CONFIRMED members"));
                }))
                .flatMap(member -> {
                    log.debug("Current membership details for memberEmail={}: {}", memberEmail, member.getMembershipDetails());
                    Member.MembershipDetails membershipDetails = new Member.MembershipDetails();
                    membershipDetails.setDuration(request.getDuration());
                    membershipDetails.setStartDate(request.getStartDate());
                    membershipDetails.setAmount(request.getAmount());
                    member.setMembershipDetails(membershipDetails);
                    log.info("Updating membership details for memberEmail={}: {}", memberEmail, membershipDetails);

                    return memberRepository.save(member)
                            .doOnSuccess(saved -> log.debug("Member saved after membership update: {}", saved));
                })
                .map(MemberResponse::fromMember)
                .doOnSuccess(response -> log.info("Membership updated successfully for memberEmail={}", memberEmail));
    }

    public Flux<MemberResponse> searchMembers(String email, String firstName, String lastName) {
        log.info("Admin searching members with email={}, firstName={}, lastName={}", email, firstName, lastName);

        return entitlementValidator.validate(new AdminVoidEntitlementCheckRequest())
                .thenMany(Flux.defer(() -> {
                    if (email != null && !email.isBlank()) {
                        log.info("Getting members by email");
                        return memberRepository.findByEmail(email)
                                .map(MemberResponse::fromMember)
                                .flux();
                    }
                    boolean hasFirstName = firstName != null && !firstName.isBlank();
                    boolean hasLastName = lastName != null && !lastName.isBlank();
                    if (hasFirstName && hasLastName) {
                        log.info("Getting members by firstName and lastName");
                        return memberRepository.findByFirstNameAndLastName(firstName, lastName)
                                .map(MemberResponse::fromMember);
                    }
                    if (hasLastName) {
                        log.info("Getting members by lastName only");
                        return memberRepository.findByLastName(lastName)
                                .map(MemberResponse::fromMember);
                    }
                    return Flux.error(new BadRequestException(
                            "Provide email or lastName or both firstName and lastName for searching"));
                }));
    }
}
package org.orioz.memberportfolio.controller;

import jakarta.validation.Valid;
import org.orioz.memberportfolio.dtos.AdminCreationRequest;
import org.orioz.memberportfolio.dtos.ConfirmMemberRequest;
import org.orioz.memberportfolio.dtos.MemberResponse;
import org.orioz.memberportfolio.dtos.PageResponse;
import org.orioz.memberportfolio.dtos.RejectMemberRequest;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Endpoint to retrieve all members with PENDING status.
     * Accessible only by administrators.
     *
     * @return Flux of MemberResponse for pending members.
     */
    @GetMapping(value = "/members/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PageResponse<MemberResponse>>> getPendingMembers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "memberSince,desc") String sort
    ) {
        // Convert sort string into Sort object
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sortParams[0]);

        Pageable pageable = PageRequest.of(page, size, sortObj);

        return adminService.getMembersByStatus(Member.Status.PENDING, pageable)
                .map(ResponseEntity::ok);
    }

    /**
     * Endpoint for an admin to approve a pending member's registration.
     * Changes member status from PENDING to ACTIVE.
     *
     * @param memberId The ID of the member to approve.
     * @return Mono<ResponseEntity<MemberResponse>> with the updated member.
     */
    @PatchMapping(value = "/members/{memberId}/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MemberResponse>> approveMember(@PathVariable String memberId, @Valid @RequestBody ConfirmMemberRequest confirmMemberRequest) {
       return adminService.confirmMember(memberId, confirmMemberRequest)
               .map(ResponseEntity::ok);
    }

    /**
     * Endpoint for an admin to reject a pending member's registration.
     * Changes member status from PENDING to REJECTED.
     *
     * @param memberId The ID of the member to reject.
     * @return Mono<ResponseEntity<MemberResponse>> with the updated member.
     */
    @PatchMapping(value = "/members/{memberId}/reject", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MemberResponse>> rejectMember(@PathVariable String memberId, @Valid @RequestBody RejectMemberRequest rejectMemberRequest) {
        return adminService.rejectMember(memberId, rejectMemberRequest)
        .map(ResponseEntity::ok);
    }

    /**
     * Endpoint for an existing admin to add another member as an admin.
     * Enforces a limit of 2 admin members in total.
     *
     * @param request AdminCreationRequest containing the ID of the member to make admin.
     * @return Mono<ResponseEntity<MemberResponse>> with the updated member.
     */
    @PatchMapping(value = "/add-admin",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MemberResponse>> addAdmin(@Valid @RequestBody AdminCreationRequest request) {
        return adminService.addAdminRole(request).map(ResponseEntity::ok);
    }
}
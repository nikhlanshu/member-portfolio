package org.orioz.memberportfolio.controller;

import jakarta.validation.Valid;
import org.orioz.memberportfolio.dtos.admin.AdminCreationRequest;
import org.orioz.memberportfolio.dtos.admin.PageResponse;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
@RequestMapping(value = "/api/v1/admin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
     * @return Mono of ResponseEntity with paginated MemberResponse for pending members.
     */
    @GetMapping(value = "/members/pending")
    public Mono<ResponseEntity<PageResponse<MemberResponse>>> getPendingMembers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "memberSince,desc") String sort
    ) {
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
     *
     * @param memberId               The ID of the member to approve.
     * @return Updated MemberResponse.
     */
    @PatchMapping(value = "/members/{memberId}/confirm")
    public Mono<ResponseEntity<MemberResponse>> approveMember(@PathVariable String memberId) {
        return adminService.confirmMember(memberId)
                .map(ResponseEntity::ok);
    }

    /**
     * Endpoint for an admin to reject a pending member's registration.
     *
     * @param memberId             The ID of the member to reject.
     * @return Updated MemberResponse.
     */
    @PatchMapping(value = "/members/{memberId}/reject")
    public Mono<ResponseEntity<MemberResponse>> rejectMember(
            @PathVariable String memberId) {
        return adminService.rejectMember(memberId)
                .map(ResponseEntity::ok);
    }

    /**
     * Endpoint to assign ADMIN role to a member, limited to a max of 2 admins.
     *
     * @param request AdminCreationRequest containing member ID.
     * @return Updated MemberResponse.
     */
    @PatchMapping(value = "/add-admin")
    public Mono<ResponseEntity<MemberResponse>> addAdmin(
            @Valid @RequestBody AdminCreationRequest request) {
        return adminService.addAdminRole(request)
                .map(memberResponse -> ResponseEntity.status(HttpStatus.OK).body(memberResponse));
    }
}

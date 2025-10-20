package org.orioz.memberportfolio.service.admin;

import org.orioz.memberportfolio.dtos.admin.AddRoleRequest;
import org.orioz.memberportfolio.dtos.admin.AdminCreationRequest;
import org.orioz.memberportfolio.dtos.admin.MembershipUpdateRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.dtos.admin.PageResponse;
import org.orioz.memberportfolio.models.Member;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminService {
    Mono<Void> addRole(AddRoleRequest request);
    Mono<MemberResponse> confirmMember(String memberEmail);
    Mono<MemberResponse> rejectMember(String memberEmail);
    Mono<PageResponse<MemberResponse>> getMembersByStatus(Member.Status status, Pageable pageable);
    Mono<MemberResponse> updateMembershipForMember(String memberEmail, MembershipUpdateRequest request);
    Flux<MemberResponse> searchMembers(String email, String firstName, String lastName);
}

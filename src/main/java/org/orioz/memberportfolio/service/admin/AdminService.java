package org.orioz.memberportfolio.service.admin;

import org.orioz.memberportfolio.dtos.admin.AdminCreationRequest;
import org.orioz.memberportfolio.dtos.admin.ConfirmMemberRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.dtos.admin.PageResponse;
import org.orioz.memberportfolio.dtos.admin.RejectMemberRequest;
import org.orioz.memberportfolio.models.Member;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface AdminService {
    Mono<MemberResponse> addAdminRole(AdminCreationRequest adminCreationRequest);
    Mono<MemberResponse> confirmMember(String memberId);
    Mono<MemberResponse> rejectMember(String memberId);
    Mono<PageResponse<MemberResponse>> getMembersByStatus(Member.Status status, Pageable pageable);
}

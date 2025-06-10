package org.orioz.memberportfolio.service;

import org.orioz.memberportfolio.dtos.AdminCreationRequest;
import org.orioz.memberportfolio.dtos.ConfirmMemberRequest;
import org.orioz.memberportfolio.dtos.MemberResponse;
import org.orioz.memberportfolio.dtos.PageResponse;
import org.orioz.memberportfolio.dtos.RejectMemberRequest;
import org.orioz.memberportfolio.models.Member;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface AdminService {
    Mono<MemberResponse> addAdminRole(AdminCreationRequest adminCreationRequest);
    Mono<MemberResponse> confirmMember(String memberId, ConfirmMemberRequest confirmMemberRequest);
    Mono<MemberResponse> rejectMember(String memberId, RejectMemberRequest rejectMemberRequest);
    Mono<PageResponse<MemberResponse>> getMembersByStatus(Member.Status status, Pageable pageable);
}

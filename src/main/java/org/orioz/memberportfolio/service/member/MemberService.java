package org.orioz.memberportfolio.service.member;

import org.orioz.memberportfolio.dtos.member.MemberRegistrationRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.dtos.member.UpdateMemberRequest;
import reactor.core.publisher.Mono;

public interface MemberService {
    Mono<MemberResponse> registerMember(MemberRegistrationRequest request);
    Mono<MemberResponse> getMemberByEmail(String email);
    Mono<MemberResponse> getMemberById(String id);
    Mono<MemberResponse> updateMemberByEmail(String email, UpdateMemberRequest updateMemberRequest);
}

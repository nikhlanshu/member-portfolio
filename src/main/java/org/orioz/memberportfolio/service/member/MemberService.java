package org.orioz.memberportfolio.service.member;

import org.orioz.memberportfolio.dtos.member.MemberRegistrationRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import reactor.core.publisher.Mono;

public interface MemberService {
    Mono<MemberResponse> registerMember(MemberRegistrationRequest request);

    Mono<MemberResponse> getMember(String email);
}

package org.orioz.memberportfolio.service;

import org.orioz.memberportfolio.dtos.MemberRegistrationRequest;
import org.orioz.memberportfolio.dtos.MemberResponse;
import reactor.core.publisher.Mono;

public interface MemberService {
    Mono<MemberResponse> registerMember(MemberRegistrationRequest request);

    Mono<MemberResponse> getMember(String email);
}

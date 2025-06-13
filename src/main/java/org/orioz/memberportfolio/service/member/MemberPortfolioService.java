package org.orioz.memberportfolio.service.member;

import org.orioz.memberportfolio.dtos.member.MemberRegistrationRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.exceptions.EmailAlreadyRegisteredException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MemberPortfolioService implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberPortfolioService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<MemberResponse> registerMember(MemberRegistrationRequest request) {
        return memberRepository.findByEmail(request.getEmail())
                .flatMap(member -> Mono.error(new EmailAlreadyRegisteredException(request.getEmail() + " is already registered, try to login or reset password")))
                .switchIfEmpty(memberRepository.save(Member.toMember(request, passwordEncoder)))
                .cast(Member.class)
                .map(MemberResponse::fromMember);
    }

    @Override
    public Mono<MemberResponse> getMember(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberResponse::fromMember);
    }

}

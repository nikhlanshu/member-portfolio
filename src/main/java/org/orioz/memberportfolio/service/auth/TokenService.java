package org.orioz.memberportfolio.service.auth;

import org.orioz.memberportfolio.auth.JwtService;
import org.orioz.memberportfolio.dtos.auth.TokenRequest;
import org.orioz.memberportfolio.exceptions.UnauthorizedException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TokenService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public TokenService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Mono<String> issueToken(TokenRequest tokenRequest) {
        return memberRepository.findById(tokenRequest.getUsername())
                .switchIfEmpty(Mono.error(new UnauthorizedException(String.format("Member does not exists %s: ", tokenRequest.getUsername()))))
                .flatMap(memberDetails -> {
                    if (!memberDetails.getStatus().equals(Member.Status.CONFIRMED)) {
                        return Mono.error(new UnauthorizedException("User not confirmed"));
                    }
                    if (!passwordEncoder.matches(tokenRequest.getPassword(), memberDetails.getPassword())) {
                        return Mono.error(new UnauthorizedException("Invalid credentials"));
                    }

                    return jwtService.generateToken(memberDetails);
                });
    }
}
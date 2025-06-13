package org.orioz.memberportfolio.service.auth;

import org.orioz.memberportfolio.auth.JwtService;
import org.orioz.memberportfolio.dtos.auth.TokenRequest;
import org.orioz.memberportfolio.dtos.auth.TokenResponse;
import org.orioz.memberportfolio.exceptions.InvalidCredentialException;
import org.orioz.memberportfolio.exceptions.MemberNotFoundException;
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

    public Mono<TokenResponse> issueToken(TokenRequest tokenRequest) {
        return memberRepository.findById(tokenRequest.getUsername())
                .switchIfEmpty(Mono.error(new MemberNotFoundException(
                        String.format("Member does not exist: %s", tokenRequest.getUsername()))))
                .flatMap(memberDetails -> {
                    if (!memberDetails.getStatus().equals(Member.Status.CONFIRMED)) {
                        return Mono.error(new UnauthorizedException("User not confirmed"));
                    }
                    if (!passwordEncoder.matches(tokenRequest.getPassword().trim(), memberDetails.getPassword().trim())) {
                        return Mono.error(new InvalidCredentialException("Invalid credentials"));
                    }

                    return jwtService.generateToken(memberDetails);
                })
                .map(TokenResponse::new);
    }
}
package org.orioz.memberportfolio.service.auth;

import org.orioz.memberportfolio.auth.JwtService;
import org.orioz.memberportfolio.dtos.auth.LoginRequest;
import org.orioz.memberportfolio.exceptions.InvalidCredentialException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LogInService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public LogInService(MemberRepository memberRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Mono<String> login(LoginRequest loginRequest) {

        return memberRepository.findByEmail(loginRequest.getEmail())
                .switchIfEmpty(Mono.error(new InvalidCredentialException("Invalid credentials")))
                .flatMap(member -> {
                    if (!member.getStatus().equals(Member.Status.CONFIRMED)) {
                        return Mono.error(new InvalidCredentialException("User not confirmed"));
                    }
                    if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
                        return Mono.error(new InvalidCredentialException("Invalid credentials"));
                    }

                    return jwtService.generateToken(member);
                });

    }
}
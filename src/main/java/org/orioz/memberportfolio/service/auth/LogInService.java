package org.orioz.memberportfolio.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.auth.jwt.JwtService;
import org.orioz.memberportfolio.dtos.auth.LoginRequest;
import org.orioz.memberportfolio.dtos.auth.LoginResponse;
import org.orioz.memberportfolio.exceptions.InvalidCredentialException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
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

    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        return memberRepository.findByEmail(loginRequest.getEmail())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("No member found with email: {}", loginRequest.getEmail());
                    return Mono.error(new InvalidCredentialException("Invalid credentials"));
                }))
                .flatMap(member -> {
                    log.debug("Checking status for member: {}", member.getEmail());
                    if (!member.getStatus().equals(Member.Status.CONFIRMED)) {
                        log.warn("Member not confirmed: {}", member.getEmail());
                        return Mono.error(new InvalidCredentialException("User not confirmed"));
                    }

                    log.debug("Validating password for member: {}", member.getEmail());
                    if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
                        log.warn("Invalid password for member: {}", member.getEmail());
                        return Mono.error(new InvalidCredentialException("Invalid credentials"));
                    }

                    log.info("Login successful for member: {}", member.getEmail());

                    // Generate tokens in parallel
                    return Mono.zip(
                            jwtService.generateAccessToken(member),
                            jwtService.generateIdToken(member)
                    ).map(tuple -> new LoginResponse(tuple.getT1(), tuple.getT2()));
                })
                .onErrorResume(error -> {
                    log.error("error occurred: ", error.fillInStackTrace());
                            return Mono.error(error);
                });

    }
}
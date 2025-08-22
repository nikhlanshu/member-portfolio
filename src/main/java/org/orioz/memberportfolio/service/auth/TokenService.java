package org.orioz.memberportfolio.service.auth;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    public Mono<TokenResponse> issueAccessToken(TokenRequest tokenRequest) {
        log.info("Issue Token is invoked");
        return memberRepository.findByEmail(tokenRequest.getUsername())
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("No member found for userId: {}", tokenRequest.getUsername());
                    return Mono.error(new MemberNotFoundException(
                            String.format("Member does not exist: %s", tokenRequest.getUsername())));
                }))
                .flatMap(memberDetails -> {
                    log.debug(String.format("Retrieved member details for userId : %s", tokenRequest.getUsername()));
                    if (!memberDetails.getStatus().equals(Member.Status.CONFIRMED)) {
                        log.warn(String.format("Retrieved member not confirmed for userId : %s", tokenRequest.getUsername()));
                        return Mono.error(new UnauthorizedException("User not confirmed"));
                    }
                    if (!passwordEncoder.matches(tokenRequest.getPassword().trim(), memberDetails.getPassword().trim())) {
                        log.warn(String.format("Retrieved member credential does not match for userId : %s", tokenRequest.getUsername()));
                        return Mono.error(new InvalidCredentialException("Invalid credentials"));
                    }
                    log.debug(String.format("Retrieved member is valid for userId : %s", tokenRequest.getUsername()));

                    return jwtService.generateAccessToken(memberDetails);
                })
                .map(TokenResponse::new);
    }
}
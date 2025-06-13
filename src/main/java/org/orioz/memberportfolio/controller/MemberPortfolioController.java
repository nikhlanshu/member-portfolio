package org.orioz.memberportfolio.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.orioz.memberportfolio.dtos.member.MemberRegistrationRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.service.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping(value = "/api/v1/members", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class MemberPortfolioController {
    private final MemberService memberService;

    @Autowired
    public MemberPortfolioController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * REST endpoint for member registration.
     * Accepts a MemberRegistrationRequest, registers the member, and returns a MemberResponse.
     * @param request The MemberRegistrationRequest containing new member details.
     * @return Mono<ResponseEntity<MemberResponse>> indicating the result of the registration.
     */
    @PostMapping(value = "/register")
    public Mono<ResponseEntity<MemberResponse>> registerMember(@Valid @RequestBody MemberRegistrationRequest request) {
        return memberService.registerMember(request).map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAuthority('MEMBER')")
    @GetMapping(value = "/{email}")
    public Mono<ResponseEntity<MemberResponse>> getMember(@Email @PathVariable String email) {
        return memberService.getMember(email).map(ResponseEntity::ok);
    }
}

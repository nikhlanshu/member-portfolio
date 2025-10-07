package org.orioz.memberportfolio.web;

import jakarta.validation.Valid;
import org.orioz.memberportfolio.dtos.member.MemberRegistrationRequest;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.service.member.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping(value = "/api/v1/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class RegistrationController {
    private final MemberService memberService;

    public RegistrationController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * REST endpoint for member registration.
     * Accepts a MemberRegistrationRequest, registers the member, and returns a MemberResponse.
     * @param request The MemberRegistrationRequest containing new member details.
     * @return Mono<ResponseEntity<MemberResponse>> indicating the result of the registration.
     */
    @PostMapping
    public Mono<ResponseEntity<MemberResponse>> registerMember(@Valid @RequestBody MemberRegistrationRequest request) {
        return memberService.registerMember(request)
                .map(memberResponse -> ResponseEntity.status(HttpStatus.CREATED).body(memberResponse));
    }
}

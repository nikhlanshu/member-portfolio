package org.orioz.memberportfolio.web;

import jakarta.validation.constraints.Email;
import org.orioz.memberportfolio.dtos.member.MemberResponse;
import org.orioz.memberportfolio.dtos.member.UpdateMemberRequest;
import org.orioz.memberportfolio.service.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/{email}")
    public Mono<ResponseEntity<MemberResponse>> getMemberByEmail(@Email @PathVariable String email) {
        return memberService.getMemberByEmail(email)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{email}")
    public Mono<ResponseEntity<MemberResponse>> updateMemberByEmail(@Email @PathVariable String email, @RequestBody UpdateMemberRequest updateMemberRequest) {
        return memberService.updateMemberByEmail(email, updateMemberRequest)
                .map(memberResponse -> ResponseEntity.status(HttpStatus.CREATED).body(memberResponse));
    }
}

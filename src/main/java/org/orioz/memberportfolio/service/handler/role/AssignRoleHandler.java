package org.orioz.memberportfolio.service.handler.role;

import org.orioz.memberportfolio.dtos.admin.AddRoleRequest;
import org.orioz.memberportfolio.exceptions.MemberNotFoundException;
import org.orioz.memberportfolio.models.Member;
import org.orioz.memberportfolio.repositories.MemberRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AssignRoleHandler {
    private final MemberRepository memberRepository;

    public AssignRoleHandler(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Mono<Void> handle(AddRoleRequest addRoleRequest) {
        return memberRepository.findByEmail(addRoleRequest.getEmail())
                .filter(Member::isMemberConfirmed)
                .switchIfEmpty(Mono.error(new MemberNotFoundException("Member not found with Email ID: " + addRoleRequest.getEmail())))
                .flatMap(member -> {
                    Set<Member.Role> updatedRoles = Stream.concat(member.getRoles().stream(), Stream.of(addRoleRequest.getRole()))
                            .collect(Collectors.toSet());
                    member.setRoles(updatedRoles.stream().toList());
                    return memberRepository.save(member);
                })
                .then();
    }
}

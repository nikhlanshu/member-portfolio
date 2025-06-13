package org.orioz.memberportfolio.dtos.member;

import org.orioz.memberportfolio.models.Member;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private List<Member.AddressInfo> addresses;
    private List<Member.ContactInfo> contacts;
    private String occupation;
    private String profilePictureUrl;
    private boolean isLifetimeMember;
    private LocalDateTime memberSince;
    private List<Member.Role> roles;
    private Member.Status status;
    public static MemberResponse fromMember(Member member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setEmail(member.getEmail());
        response.setDateOfBirth(member.getDateOfBirth());
        response.setAddresses(member.getAddresses());
        response.setContacts(member.getContacts());
        response.setOccupation(member.getOccupation());
        response.setProfilePictureUrl(member.getProfilePictureUrl());
        response.setLifetimeMember(member.isLifetimeMember());
        response.setMemberSince(member.getMemberSince());
        response.setRoles(member.getRoles());
        response.setStatus(member.getStatus());
        return response;
    }
}
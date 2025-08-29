package org.orioz.memberportfolio.dtos.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.orioz.memberportfolio.models.Member;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateMemberRequest {
    private List<Member.AddressInfo> addresses;
    private List<Member.ContactInfo> contacts;
}

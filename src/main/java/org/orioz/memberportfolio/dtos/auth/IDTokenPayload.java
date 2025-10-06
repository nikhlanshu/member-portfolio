package org.orioz.memberportfolio.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.orioz.memberportfolio.models.Member;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class IDTokenPayload {
    private String subject;             // member ID
    private String status;              // e.g. "CONFIRMED"
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dob;
    private LocalDate memberSince;
    private Member.MembershipDuration duration;
    private Instant issuedAt;
    private Instant expiration;
}

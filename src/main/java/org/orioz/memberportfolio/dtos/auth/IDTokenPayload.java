package org.orioz.memberportfolio.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class IDTokenPayload {
    private String subject;             // member ID
    private String status;              // e.g. "CONFIRMED"
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dob;
    private Instant issuedAt;
    private Instant expiration;
}

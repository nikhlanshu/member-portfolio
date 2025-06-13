package org.orioz.memberportfolio.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class TokenPayload {
    private String subject;             // member ID
    private List<String> roles;         // e.g. ["MEMBER","ADMIN"]
    private String status;              // e.g. "CONFIRMED"
    private Instant issuedAt;
    private Instant expiration;
}

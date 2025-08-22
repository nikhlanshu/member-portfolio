package org.orioz.memberportfolio.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailEntitlementCheckRequest implements IEntitlementCheckRequest{
    private String email;
}

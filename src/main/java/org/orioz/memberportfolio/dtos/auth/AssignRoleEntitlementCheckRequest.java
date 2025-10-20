package org.orioz.memberportfolio.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.orioz.memberportfolio.models.Member;

@Data
@AllArgsConstructor
public class AssignRoleEntitlementCheckRequest implements IEntitlementCheckRequest {
    private Member.Role role;
}

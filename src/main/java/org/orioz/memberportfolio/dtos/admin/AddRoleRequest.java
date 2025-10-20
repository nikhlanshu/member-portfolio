package org.orioz.memberportfolio.dtos.admin;

import lombok.Data;
import org.orioz.memberportfolio.models.Member;

@Data
public class AddRoleRequest {
    private Member.Role role;
    private String email;
}

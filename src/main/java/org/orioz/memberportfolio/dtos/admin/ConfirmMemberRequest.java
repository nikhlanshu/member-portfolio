package org.orioz.memberportfolio.dtos.admin;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmMemberRequest {
    @Email
    private String email;
}

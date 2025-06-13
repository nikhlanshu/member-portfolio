package org.orioz.memberportfolio.dtos.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreationRequest {
    @Email
    private String email;
    @NotEmpty
    private String memberId;
}

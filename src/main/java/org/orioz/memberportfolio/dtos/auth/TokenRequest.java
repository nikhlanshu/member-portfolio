package org.orioz.memberportfolio.dtos.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRequest {
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
}

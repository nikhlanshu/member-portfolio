package org.orioz.memberportfolio.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnBehalfOfTokenRequest {
    private String token;
}

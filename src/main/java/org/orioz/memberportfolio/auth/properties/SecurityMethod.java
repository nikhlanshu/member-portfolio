package org.orioz.memberportfolio.auth.properties;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class SecurityMethod {
    private String name;
    private List<String> roles;
}

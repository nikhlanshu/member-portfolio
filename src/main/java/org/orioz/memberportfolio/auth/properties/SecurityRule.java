package org.orioz.memberportfolio.auth.properties;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class SecurityRule {
    private String path;
    private List<SecurityMethod> methods;
}

package org.orioz.memberportfolio.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Data
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private List<String> publicPaths;
    private String secret;
    private ExpiryData expiry;
}

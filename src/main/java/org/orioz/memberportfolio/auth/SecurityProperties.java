package org.orioz.memberportfolio.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.temporal.ChronoUnit;
import java.util.List;
@Data
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private List<String> publicPaths;
    private String secret;
    private ExpiryData expiry;
}

@Data
class ExpiryData {
    private Integer duration;
    private ChronoUnit unit;
}

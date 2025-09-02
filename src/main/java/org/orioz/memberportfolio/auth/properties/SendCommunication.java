package org.orioz.memberportfolio.auth.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.send-communication")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendCommunication {

    private Map<String, EmailTemplate> templates;
    private Map<String, SmtpConfig> from;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailTemplate {
        private String subject;
        private String templatePath;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmtpConfig {
        private String host;
        private int mailPort;
        private String username;
        private String password;
        private boolean auth;
        private boolean enable;
        private String content;
    }
}

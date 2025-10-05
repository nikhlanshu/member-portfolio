package org.orioz.memberportfolio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.news")
public class NewsConfig {
    private Integer retrieveLimit;
}

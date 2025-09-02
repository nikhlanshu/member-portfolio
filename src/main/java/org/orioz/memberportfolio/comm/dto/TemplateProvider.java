package org.orioz.memberportfolio.comm.dto;

import lombok.Builder;
import org.orioz.memberportfolio.auth.properties.SendCommunication;

import java.util.Map;

@Builder
public record TemplateProvider(
        String toEmail,
        SendCommunication.SmtpConfig smtpConfig,
        SendCommunication.EmailTemplate template,
        Map<String, Object> variables

) {
}

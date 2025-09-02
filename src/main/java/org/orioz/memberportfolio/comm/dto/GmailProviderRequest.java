package org.orioz.memberportfolio.comm.dto;

import org.orioz.memberportfolio.auth.properties.SendCommunication;

import java.util.Map;

public class GmailProviderRequest implements ITemplateProviderRequest{
    private final SendCommunication communicationConfig;
    private final String toEmail;
    private final CommunicationStage stage;
    private final Map<String, Object> variables;

    public GmailProviderRequest(SendCommunication communicationConfig, String toEmail, CommunicationStage stage, Map<String, Object> variables) {
        this.communicationConfig = communicationConfig;
        this.toEmail = toEmail;
        this.stage = stage;
        this.variables = variables;
    }

    @Override
    public String getToEmail() {
        return toEmail;
    }

    @Override
    public SendCommunication getSendCommunication() {
        return communicationConfig;
    }

    @Override
    public String getCommunicationStage() {
        return stage.name();
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }
}

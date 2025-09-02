package org.orioz.memberportfolio.comm.dto;

import org.orioz.memberportfolio.auth.properties.SendCommunication;

import java.util.Map;

public interface ITemplateProviderRequest {
    String getToEmail();
    SendCommunication getSendCommunication();
    String getCommunicationStage();

    Map<String, Object> getVariables();
}

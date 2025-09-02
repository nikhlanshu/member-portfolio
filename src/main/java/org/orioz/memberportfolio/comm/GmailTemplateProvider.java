package org.orioz.memberportfolio.comm;

import org.orioz.memberportfolio.comm.dto.GmailProviderRequest;
import org.orioz.memberportfolio.comm.dto.ITemplateProviderRequest;
import org.orioz.memberportfolio.comm.dto.TemplateProvider;
import org.springframework.stereotype.Component;

@Component
public class GmailTemplateProvider implements ICommunicationTemplateProvider {
    @Override
    public boolean isApplicable(ITemplateProviderRequest request) {
        return request instanceof GmailProviderRequest;
    }

    @Override
    public TemplateProvider getProvider(ITemplateProviderRequest request) {
        return new TemplateProvider(
                request.getToEmail(),
                request.getSendCommunication().getFrom().get("GMAIL"),
                request.getSendCommunication().getTemplates().get(request.getCommunicationStage()),
                request.getVariables()

        );
    }
}

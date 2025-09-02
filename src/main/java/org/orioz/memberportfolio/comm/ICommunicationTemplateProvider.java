package org.orioz.memberportfolio.comm;

import org.orioz.memberportfolio.comm.dto.ITemplateProviderRequest;
import org.orioz.memberportfolio.comm.dto.TemplateProvider;

public interface ICommunicationTemplateProvider {
    boolean isApplicable(ITemplateProviderRequest request);
    TemplateProvider getProvider(ITemplateProviderRequest request);
}
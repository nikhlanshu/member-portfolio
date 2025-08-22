package org.orioz.memberportfolio.auth.entitlement;

import org.orioz.memberportfolio.dtos.auth.IEntitlementCheckRequest;
import reactor.core.publisher.Mono;

public interface IEntitlementCheck {
    /**
     * Whether this checker should handle the given request.
     */
    Mono<Boolean> isApplicable(IEntitlementCheckRequest request);

    /**
     * Applies the entitlement check. Should throw an exception if it fails.
     */
    Mono<Void> apply(IEntitlementCheckRequest request);
}

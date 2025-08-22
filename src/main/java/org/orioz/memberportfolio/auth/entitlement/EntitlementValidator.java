package org.orioz.memberportfolio.auth.entitlement;

import lombok.extern.slf4j.Slf4j;
import org.orioz.memberportfolio.dtos.auth.IEntitlementCheckRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class EntitlementValidator {

    private final List<IEntitlementCheck> entitlementCheckList;

    public EntitlementValidator(List<IEntitlementCheck> entitlementCheckList) {
        this.entitlementCheckList = entitlementCheckList;
    }

    public Mono<Void> validate(IEntitlementCheckRequest request) {
        log.info("Starting entitlement validation for request: {}", request);

        return Flux.fromIterable(entitlementCheckList)
                .concatMap(check ->
                        check.isApplicable(request)
                                .doOnNext(applicable -> log.debug("Check [{}] applicable = {}", check.getClass().getSimpleName(), applicable))
                                .filter(Boolean::booleanValue)
                                .flatMap(ignored ->
                                        check.apply(request)
                                                .doOnSubscribe(sub -> log.debug("Applying check: {}", check.getClass().getSimpleName()))
                                                .doOnSuccess(v -> log.info("Check [{}] passed", check.getClass().getSimpleName()))
                                                .onErrorResume(error -> {
                                                    log.error("Check [{}] failed: {}", check.getClass().getSimpleName(), error.getMessage(), error);
                                                    return Mono.error(error);
                                                })
                                )
                )
                .then()
                .doOnSuccess(v -> log.info("All entitlement checks passed for request: {}", request))
                .doOnError(e -> log.warn("Entitlement validation failed for request: {}. Reason: {}", request, e.getMessage()));
    }
}

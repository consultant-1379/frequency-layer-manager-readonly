/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.startup;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.flm.kpi.KpiAndCounterValidator;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Used to build {@link CmRestExecutor} and {@link KpiAndCounterValidator} instances with appropriate retry and circuit breaker configurations.
 */
public final class CmKpiRestResilienceCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmKpiRestResilienceCreator.class);
    private static final Set<Integer> RECOVERABLE_STATUS_CODES = buildRecoverableStatusCodesSet();

    private CmKpiRestResilienceCreator() {
    }

    private static Set<Integer> buildRecoverableStatusCodesSet() {
        return IntStream.of(HttpStatus.SC_NOT_FOUND, HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .boxed()
                .collect(Collectors.toSet());
    }

    /**
     * Get a instance of a {@link CmRestExecutor} with the appropriate retry and circuit breaker configuration.
     *
     * @return CmRestExecutor
     */
    public static CmRestExecutor getCmRestExecutor() {
        return new CmRestExecutor.Builder().withRetry(getModelValidationRetry())
                .withCircuitBreaker(getModelValidationCircuitBreaker()).build();
    }

    /**
     * Get a instance of a {@link KpiAndCounterValidator} with the appropriate retry and circuit breaker configuration.
     * 
     * @return KpiAndCounterValidator
     */
    public static KpiAndCounterValidator getKpiAndCounterValidator() {
        return new KpiAndCounterValidator(getModelValidationRetry(),
                getModelValidationCircuitBreaker(), new RestExecutor());
    }

    private static Retry getModelValidationRetry() {
        final RetryConfig retryConfig = RetryConfig.<RestResponse>custom()
                .maxAttempts(Integer.MAX_VALUE)
                .waitDuration(Duration.ofSeconds(30))
                .retryOnResult(CmKpiRestResilienceCreator::retryResponse)
                .retryOnException((Throwable throwable) -> {
                    LOGGER.warn("Failed to validate model (an exception occurred), retrying", throwable);
                    return true;
                })
                .build();
        return Retry.of("modelValidation", retryConfig);
    }

    private static boolean retryResponse(final RestResponse response) {
        final int statusCode = response.getStatus();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Model validation returned status code {}", statusCode);
        }
        return responseIsRecoverable(statusCode);
    }

    private static boolean responseIsRecoverable(final int statusCode) {
        if (RECOVERABLE_STATUS_CODES.contains(statusCode)) {
            LOGGER.warn("Failed to validate model (status code: {}), retrying", statusCode);
            return true;
        }
        return false;
    }

    private static CircuitBreaker getModelValidationCircuitBreaker() {
        final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(80)
                .slidingWindow(10, 10, SlidingWindowType.COUNT_BASED)
                .permittedNumberOfCallsInHalfOpenState(3)
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .build();
        return CircuitBreaker.of("modelValidation", circuitBreakerConfig);
    }
}

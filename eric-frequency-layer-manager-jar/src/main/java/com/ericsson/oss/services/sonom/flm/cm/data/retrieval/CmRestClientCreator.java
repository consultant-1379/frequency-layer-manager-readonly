/*
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.cm.data.retrieval;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Creates a CM Rest Client.
 */
public final class CmRestClientCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmRestClientCreator.class);
    private final Set<Integer> recoverableStatusCodes = new HashSet<>();
    private final int maxRetryAttempts;

    private final int secondsToEstablishConnection;

    /**
     * Creates a CM Rest Client with Retry.
     *
     * @param maxRetryAttempts
     *            The number of attempts to connect to make.
     * @param secondsToEstablishConnection
     *            The number of seconds to wait for connection to be established.
     */
    public CmRestClientCreator(final int maxRetryAttempts, final int secondsToEstablishConnection) {
        // 5xx errors
        recoverableStatusCodes.add(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        this.maxRetryAttempts = maxRetryAttempts;
        this.secondsToEstablishConnection = secondsToEstablishConnection;
    }

    public CmRestClientCreator(final int maxRetryAttempts, final int secondsToEstablishConnection, final int[] httpCodes) {
        for (final int httpCode : httpCodes) {
            recoverableStatusCodes.add(httpCode);
        }
        this.maxRetryAttempts = maxRetryAttempts;
        this.secondsToEstablishConnection = secondsToEstablishConnection;
    }

    public CmRestExecutor getClientWithRetry() {
        return new CmRestExecutor.Builder().withRetry(getRetry()).withCircuitBreaker(getCircuitBreaker()).build();
    }

    private Retry getRetry() {
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(maxRetryAttempts)
                .waitDuration(Duration.ofSeconds(secondsToEstablishConnection))
                .retryOnResult(this::retryResponse)
                .retryOnException(throwable -> {
                    LOGGER.warn("Failed to retrieve topology objects (an exception occurred), retrying", throwable);
                    return true;
                })
                .build();
        return Retry.of("retrieveSectors", retryConfig);
    }

    private boolean retryResponse(final Object r) {
        final RestResponse response = (RestResponse) r;
        final int statusCode = response.getStatus();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Retrieving topology objects returned status code {}", statusCode);
        }
        return responseIsRecoverable(statusCode);
    }

    private boolean responseIsRecoverable(final int statusCode) {
        if (recoverableStatusCodes.contains(statusCode)) {
            LOGGER.warn("Recoverable status code returned while retrieving topology objects (status code: {}), retrying", statusCode);
            return true;
        }
        return false;
    }

    private static CircuitBreaker getCircuitBreaker() {
        final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(80)
                .slidingWindow(10, 10, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .permittedNumberOfCallsInHalfOpenState(3)
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .build();
        return CircuitBreaker.of("CmRestClientCreator", circuitBreakerConfig);
    }
}

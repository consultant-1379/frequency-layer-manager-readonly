/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm;

import static com.ericsson.oss.services.sonom.common.test.rest.ResponseAssertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.api.exception.CmModelValidationException;
import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.flm.cm.schema.CmModelValidator;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Unit tests for {@link CmModelValidator} class.
 */
public class CmModelValidatorTest {

    private static final String ACCEPTED_REQUEST_BODY = "CM model sent for mediation";
    private static final String INVALID_REQUEST_BODY = "Invalid Model";
    private static final int MAX_ATTEMPTS = 3;

    private final CmModelValidator objectUnderTest = new CmModelValidator();
    private final Set<Integer> recoverableStatusCodes = new HashSet<>(2);
    private final RestExecutor mockedRestExecutor = mock(RestExecutor.class);
    private final RestResponse<String> mockedRestResponse = mock(RestResponse.class);
    private final RestResponse<String> recoverableMockedRestResponse = mock(RestResponse.class);

    private CmRestExecutor cmRestExecutor;

    @Before
    public void setUp() {
        System.setProperty("CM_SERVICE_HOSTNAME", "localhost");
        System.setProperty("CM_SERVICE_PORT", "8080");
        recoverableStatusCodes.add(HttpStatus.SC_NOT_FOUND);
        recoverableStatusCodes.add(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        cmRestExecutor = new CmRestExecutor.Builder()
                .withRestExecutor(mockedRestExecutor)
                .withCircuitBreaker(getModelValidationCircuitBreaker())
                .withRetry(getModelValidationRetry()).build();
    }

    @Test
    public void whenValidationReturns202_thenResponseIsReturned_andThereIsNoRetry() throws CmModelValidationException, IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_ACCEPTED);
        when(mockedRestResponse.getEntity()).thenReturn(ACCEPTED_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedRestResponse);
        final RestResponse<String> response = objectUnderTest.sendRequiredCmElementsForMediation(cmRestExecutor);
        verify(mockedRestExecutor, times(1)).sendPostRequest(any(HttpPost.class));
        assertThat(response).containsEntity(ACCEPTED_REQUEST_BODY);
    }

    @Test
    public void whenValidationReturns400_thenResponseIsReturned_andThereIsNoRetry() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredCmElementsForMediation(cmRestExecutor);
            shouldHaveThrown(CmModelValidationException.class);
        } catch (final CmModelValidationException e) {
            verify(mockedRestExecutor, times(1)).sendPostRequest(any(HttpPost.class));
        }
    }

    @Test
    public void whenValidationReturns500_thenThereIsARetry_andAnExceptionIsThrown() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredCmElementsForMediation(cmRestExecutor);
            shouldHaveThrown(CmModelValidationException.class);
        } catch (final CmModelValidationException e) {
            verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPostRequest(any(HttpPost.class));
        }
    }

    @Test
    public void whenValidationReturns404_thenThereIsARetry_andAnExceptionIsThrown() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredCmElementsForMediation(cmRestExecutor);
            shouldHaveThrown(CmModelValidationException.class);
        } catch (final CmModelValidationException e) {
            verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPostRequest(any(HttpPost.class));
        }
    }

    @Test
    public void whenValidationThrowsAnIOException_thenThereIsARetry_andAnExceptionIsThrown() throws CmModelValidationException, IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenThrow(IOException.class);
        try {
            objectUnderTest.sendRequiredCmElementsForMediation(cmRestExecutor);
            shouldHaveThrown(UncheckedIOException.class);
        } catch (final UncheckedIOException e) {
            verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPostRequest(any(HttpPost.class));
        }
    }

    @Test
    public void whenValidationReturns404_thenThrowsAnIOException_thenReturns202_thenThereIs3Attempts_and202IsReturned()
            throws CmModelValidationException, IOException {
        when(recoverableMockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(recoverableMockedRestResponse.getEntity()).thenReturn(INVALID_REQUEST_BODY);
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_ACCEPTED);
        when(mockedRestResponse.getEntity()).thenReturn(ACCEPTED_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(recoverableMockedRestResponse).thenThrow(IOException.class)
                .thenReturn(mockedRestResponse);
        final RestResponse<String> response = objectUnderTest.sendRequiredCmElementsForMediation(cmRestExecutor);
        verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPostRequest(any(HttpPost.class));
        assertThat(response).containsEntity(ACCEPTED_REQUEST_BODY);
    }

    private Retry getModelValidationRetry() {
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(MAX_ATTEMPTS)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(100L, 1.5D))
                .retryOnResult((r) -> retryResponse(r, recoverableStatusCodes))
                .retryExceptions(UncheckedIOException.class)
                .build();
        return Retry.of("cmModelValidation", retryConfig);
    }

    private boolean retryResponse(final Object r, final Set<Integer> recoverableStatusCodes) {
        if (Objects.isNull(r)) {
            return true;
        }
        try {
            final RestResponse<String> response = (RestResponse) r;
            return recoverableStatusCodes.contains(response.getStatus());
        } catch (final Exception e) {
            return true;
        }
    }

    private CircuitBreaker getModelValidationCircuitBreaker() {
        final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(80.0F)
                .slidingWindow(10, 10, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .permittedNumberOfCallsInHalfOpenState(3)
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .build();
        return CircuitBreaker.of("cmModelValidation", circuitBreakerConfig);
    }
}

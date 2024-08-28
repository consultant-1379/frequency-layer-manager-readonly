/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.kpi;

import static com.ericsson.oss.services.sonom.common.test.rest.ResponseAssertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;

import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.common.rest.utils.exception.RestExecutionException;
import com.ericsson.oss.services.sonom.kpi.calculator.api.exception.KpiModelVerificationException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Unit tests for {@link KpiAndCounterValidator} class.
 */
public class KpiAndCounterValidatorTest {

    private static final String ACCEPTED_KPI_REQUEST_BODY = "Provided KPIs have been accepted";
    private static final String ACCEPTED_COUNTER_REQUEST_BODY = "Provided counters have been accepted";
    private static final String INVALID_KPI_REQUEST_BODY = "Invalid KPI";
    private static final String INVALID_COUNTER_REQUEST_BODY = "Invalid Counter";
    private static final String INSIDE_RETRY = "Access failed, trying again";
    private static final String KPI_COUNTER_MODEL_VALIDATION = "kpiCounterModelValidation";
    private static final Class<KpiModelVerificationException> EXCEPTION_EXPECTED = KpiModelVerificationException.class;
    private static final Logger OBJECT_UNDER_TEST_LOGGER = Logger.getLogger(KpiAndCounterValidatorTest.class);
    private static final int MAX_ATTEMPTS = 3;

    private final Set<Integer> recoverableStatusCodes = new HashSet<>(2);
    private final RestExecutor mockedRestExecutor = mock(RestExecutor.class);
    private final RestResponse<String> mockedRestResponse = mock(RestResponse.class);
    private final RestResponse<String> recoverableMockedRestResponse = mock(RestResponse.class);

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private KpiAndCounterValidator objectUnderTest;

    @Before
    public void setUp() {
        System.setProperty("KPI_SERVICE_HOSTNAME", "localhost");
        System.setProperty("KPI_SERVICE_PORT", "8080");
        objectUnderTest = new KpiAndCounterValidator(getModelValidationRetry(), getModelValidationCircuitBreaker(), mockedRestExecutor);
        recoverableStatusCodes.add(HttpStatus.SC_NOT_FOUND);
        recoverableStatusCodes.add(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void whenKpiValidationFails_thenFailureCanBeLoggedEachTime() throws IOException, KpiModelVerificationException {
        final List<LoggingEvent> events = new ArrayList<>();
        OBJECT_UNDER_TEST_LOGGER.addAppender(new TestAppender(events));

        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(MAX_ATTEMPTS)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(100, 1.5))
                .retryOnResult((r) -> {
                    OBJECT_UNDER_TEST_LOGGER.error(INSIDE_RETRY);
                    return retryResponse(r, recoverableStatusCodes);
                })
                .build();
        final Retry retry = Retry.of(KPI_COUNTER_MODEL_VALIDATION, retryConfig);

        objectUnderTest = new KpiAndCounterValidator(retry, getModelValidationCircuitBreaker(), mockedRestExecutor);
        try {
            objectUnderTest.sendRequiredKpisForMediation();
            shouldHaveThrown(NullPointerException.class);
        } catch (final NullPointerException e) { // NOPMD NPE Expected
            verify(mockedRestExecutor, VerificationModeFactory.times(MAX_ATTEMPTS)).sendPutRequest(any(HttpPut.class));
            assertThat(events).hasSize(MAX_ATTEMPTS);
            final AtomicInteger eventCount = new AtomicInteger(1);
            events.forEach(event -> {
                softly.assertThat(event.getMessage()).as("Contents of event #%s of %s", eventCount, MAX_ATTEMPTS)
                        .isEqualTo(INSIDE_RETRY);
                eventCount.incrementAndGet();
            });
        }
    }

    @Test
    public void whenKpiValidationReturns202_thenResponseIsReturned_andThereIsNoRetry() throws KpiModelVerificationException, IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_ACCEPTED);
        when(mockedRestResponse.getEntity()).thenReturn(ACCEPTED_KPI_REQUEST_BODY);
        when(mockedRestExecutor.sendPutRequest(any(HttpPut.class))).thenReturn(mockedRestResponse);
        final RestResponse response = objectUnderTest.sendRequiredKpisForMediation();

        assertThat(response).containsEntity(ACCEPTED_KPI_REQUEST_BODY);
        verify(mockedRestExecutor).sendPutRequest(any(HttpPut.class));
    }

    @Test
    public void whenKpiValidationReturns400_thenExceptionIsThrown_andThereIsNoRetry() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_KPI_REQUEST_BODY);
        when(mockedRestExecutor.sendPutRequest(any(HttpPut.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredKpisForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final KpiModelVerificationException e) {
            verify(mockedRestExecutor).sendPutRequest(any(HttpPut.class));
        }
    }

    @Test
    public void whenKpiValidationReturns409_thenExceptionIsThrown_andThereIsNoRetry() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CONFLICT);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_KPI_REQUEST_BODY);
        when(mockedRestExecutor.sendPutRequest(any(HttpPut.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredKpisForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final KpiModelVerificationException e) {
            verify(mockedRestExecutor).sendPutRequest(any(HttpPut.class));
        }
    }

    @Test
    public void whenKpiValidationReturns500_thenExceptionIsThrown_andThereIs3Attempts() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_KPI_REQUEST_BODY);
        when(mockedRestExecutor.sendPutRequest(any(HttpPut.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredKpisForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final KpiModelVerificationException e) {
            verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPutRequest(any(HttpPut.class));
        }
    }

    @Test
    public void whenKpiValidationReturns404_thenExceptionIsThrown_andThereIs3Attempts() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_KPI_REQUEST_BODY);
        when(mockedRestExecutor.sendPutRequest(any(HttpPut.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredKpisForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final KpiModelVerificationException e) {
            verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPutRequest(any(HttpPut.class));
        }
    }

    @Test
    public void whenKpiValidationThrowsIOException_thenExceptionIsThrown_andThereIs3Attempts() throws KpiModelVerificationException, IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_KPI_REQUEST_BODY);
        when(mockedRestExecutor.sendPutRequest(any(HttpPut.class))).thenThrow(IOException.class);
        try {
            objectUnderTest.sendRequiredKpisForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final RestExecutionException e) {
            verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPutRequest(any(HttpPut.class));
        }
    }

    @Test
    public void whenKpiValidationReturns404_thenThrowsAnIOException_thenReturns202_thenThereIs3Attempts_and202IsReturned()
            throws KpiModelVerificationException, IOException {
        when(recoverableMockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(recoverableMockedRestResponse.getEntity()).thenReturn(INVALID_KPI_REQUEST_BODY);
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_ACCEPTED);
        when(mockedRestResponse.getEntity()).thenReturn(ACCEPTED_KPI_REQUEST_BODY);
        when(mockedRestExecutor.sendPutRequest(any(HttpPut.class))).thenReturn(recoverableMockedRestResponse).thenThrow(IOException.class)
                .thenReturn(mockedRestResponse);
        final RestResponse response = objectUnderTest.sendRequiredKpisForMediation();

        assertThat(response).containsEntity(ACCEPTED_KPI_REQUEST_BODY);
        verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPutRequest(any(HttpPut.class));
    }

    @Test
    public void whenCounterValidationReturns202_thenResponseIsReturned_andThereIsNoRetry() throws KpiModelVerificationException, IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_ACCEPTED);
        when(mockedRestResponse.getEntity()).thenReturn(ACCEPTED_COUNTER_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedRestResponse);
        final RestResponse response = objectUnderTest.sendRequiredCountersForMediation();

        assertThat(response).containsEntity(ACCEPTED_COUNTER_REQUEST_BODY);
        verify(mockedRestExecutor).sendPostRequest(any(HttpPost.class));
    }

    @Test
    public void whenCounterValidationReturns400_thenExceptionIsThrown_andThereIsNoRetry() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_COUNTER_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredCountersForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final KpiModelVerificationException e) {
            verify(mockedRestExecutor).sendPostRequest(any(HttpPost.class));
        }
    }

    @Test
    public void whenCounterValidationReturns409_thenExceptionIsThrown_andThereIsNoRetry() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CONFLICT);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_COUNTER_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredCountersForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final KpiModelVerificationException e) {
            verify(mockedRestExecutor).sendPostRequest(any(HttpPost.class));
        }
    }

    @Test
    public void whenCounterValidationReturns500_thenExceptionIsThrown_andThereIs3Attempts() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_COUNTER_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredCountersForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final KpiModelVerificationException e) {
            verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPostRequest(any(HttpPost.class));
        }
    }

    @Test
    public void whenCounterValidationReturns404_thenExceptionIsThrown_andThereIs3Attempts() throws IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_COUNTER_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(mockedRestResponse);
        try {
            objectUnderTest.sendRequiredCountersForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final KpiModelVerificationException e) {
            verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPostRequest(any(HttpPost.class));
        }
    }

    @Test
    public void whenCounterValidationThrowsIOException_thenExceptionIsThrown_andThereIs3Attempts() throws KpiModelVerificationException, IOException {
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(mockedRestResponse.getEntity()).thenReturn(INVALID_COUNTER_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenThrow(IOException.class);
        try {
            objectUnderTest.sendRequiredCountersForMediation();
            shouldHaveThrown(EXCEPTION_EXPECTED);
        } catch (final RestExecutionException e) {
            verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPostRequest(any(HttpPost.class));
        }
    }

    @Test
    public void whenCounterValidationReturns404_thenThrowsAnIOException_thenReturns202_thenThereIs3Attempts_and202IsReturned()
            throws KpiModelVerificationException, IOException {
        when(recoverableMockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(recoverableMockedRestResponse.getEntity()).thenReturn(INVALID_COUNTER_REQUEST_BODY);
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_ACCEPTED);
        when(mockedRestResponse.getEntity()).thenReturn(ACCEPTED_COUNTER_REQUEST_BODY);
        when(mockedRestExecutor.sendPostRequest(any(HttpPost.class))).thenReturn(recoverableMockedRestResponse).thenThrow(IOException.class)
                .thenReturn(mockedRestResponse);
        final RestResponse response = objectUnderTest.sendRequiredCountersForMediation();

        assertThat(response).containsEntity(ACCEPTED_COUNTER_REQUEST_BODY);
        verify(mockedRestExecutor, times(MAX_ATTEMPTS)).sendPostRequest(any(HttpPost.class));
    }

    private Retry getModelValidationRetry() {
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(MAX_ATTEMPTS)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(100, 1.5))
                .retryOnResult((r) -> retryResponse(r, recoverableStatusCodes))
                .retryExceptions(RestExecutionException.class)
                .build();
        return Retry.of(KPI_COUNTER_MODEL_VALIDATION, retryConfig);
    }

    private boolean retryResponse(final Object r, final Set<Integer> recoverableStatusCodes) {
        if (Objects.isNull(r)) {
            return true;
        }
        try {
            final RestResponse response = (RestResponse) r;
            return recoverableStatusCodes.contains(response.getStatus());
        } catch (final Exception e) {
            return true;
        }
    }

    private CircuitBreaker getModelValidationCircuitBreaker() {
        final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(80)
                .slidingWindow(10, 10, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .permittedNumberOfCallsInHalfOpenState(3)
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .build();
        return CircuitBreaker.of(KPI_COUNTER_MODEL_VALIDATION, circuitBreakerConfig);
    }

    private static class TestAppender extends AppenderSkeleton {
        private final List<LoggingEvent> events;

        public TestAppender(final List<LoggingEvent> events) {
            super();
            this.events = events;
        }

        @Override
        protected void append(final LoggingEvent event) {
            events.add(event);
        }

        @Override
        public void close() {

        }

        @Override
        public boolean requiresLayout() {
            return false;
        }
    }
}

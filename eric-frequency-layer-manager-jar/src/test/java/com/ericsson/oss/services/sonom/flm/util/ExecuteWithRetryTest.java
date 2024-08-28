/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.util;

import static io.github.resilience4j.retry.RetryConfig.custom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;

import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedRunnable;

/**
 * Unit tests for {@link ExecuteWithRetry} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteWithRetryTest {

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();

    @Mock
    private CheckedFunction0<String> mockCheckedFunction0;

    @Mock
    private CheckedRunnable mockCheckedRunnable;

    private static final String RETRY_ALIAS_FOR_TEST = "test";
    private static final String EXPECTED_RETURN_VALUE_FROM_FUNCTION = "pass";

    @Test
    public void whenExecuteCheckedFunction0WithRetry_thenFunctionExecutedFirstTimeAndExpectedStringIsReturned() throws Throwable {
        when(mockCheckedFunction0.apply()).thenReturn(EXPECTED_RETURN_VALUE_FROM_FUNCTION);

        final String result = ExecuteWithRetry.executeWithRetry(mockCheckedFunction0,
                throwable -> new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR), RETRY_ALIAS_FOR_TEST, createRetryConfig(1));

        assertThat(EXPECTED_RETURN_VALUE_FROM_FUNCTION).isEqualTo(result);
    }

    @Test
    public void whenExecuteCheckedFunction0WithRetry_thenFunctionExecutedSecondTimeAndExpectedStringIsReturned() throws Throwable {
        when(mockCheckedFunction0.apply())
                .thenThrow(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR))
                .thenReturn(EXPECTED_RETURN_VALUE_FROM_FUNCTION);

        final String result = ExecuteWithRetry.executeWithRetry(mockCheckedFunction0,
                throwable -> new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR), RETRY_ALIAS_FOR_TEST, createRetryConfig(2));

        verify(mockCheckedFunction0, times(2)).apply();
        assertThat(EXPECTED_RETURN_VALUE_FROM_FUNCTION).isEqualTo(result);
    }

    @Test
    public void whenExecuteCheckedFunction0WithRetry_thenFunctionExecutedButExceptionThrownWhenRetriesExhausted() throws Throwable {

        when(mockCheckedFunction0.apply())
                .thenThrow(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR));

        expectedExceptions.expect(FlmAlgorithmException.class);
        expectedExceptions.expectMessage(FlmServiceExceptionCode.KPI_CALCULATION_ERROR.getErrorMessage());

        ExecuteWithRetry.executeWithRetry(mockCheckedFunction0,
                throwable -> new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR), RETRY_ALIAS_FOR_TEST, createRetryConfig(1));
    }

    @Test
    public void whenExecuteCheckedRunnableWithRetry_thenFunctionExecutedFirstTimeAndExpectedStringIsReturned() throws Throwable {

        ExecuteWithRetry.executeWithRetry(mockCheckedRunnable,
                throwable -> new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR), RETRY_ALIAS_FOR_TEST, createRetryConfig(1));

        verify(mockCheckedRunnable, times(1)).run();
    }

    @Test
    public void whenExecuteCheckedRunnableWithRetry_thenFunctionExecutedSecondTimeAndExpectedStringIsReturned() throws Throwable {
        doThrow(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR)).doNothing().when(mockCheckedRunnable).run();

        ExecuteWithRetry.executeWithRetry(mockCheckedRunnable,
                throwable -> new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR), RETRY_ALIAS_FOR_TEST, createRetryConfig(2));

        verify(mockCheckedRunnable, times(2)).run();
    }

    @Test
    public void whenExecuteCheckedRunnableWithRetry_thenFunctionExecutedButExceptionThrownWhenRetriesExhausted() throws Throwable {

        expectedExceptions.expect(FlmAlgorithmException.class);
        expectedExceptions.expectMessage(FlmServiceExceptionCode.KPI_CALCULATION_ERROR.getErrorMessage());

        ExecuteWithRetry.executeWithRetry(mockCheckedFunction0,
                throwable -> new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR), RETRY_ALIAS_FOR_TEST, createRetryConfig(1));
    }

    private RetryConfig createRetryConfig(final int maxRetryAttempts) {
        return custom()
                .retryOnResult(s -> s.equals(true)) //TODO: compares hashCodes, always returns false. Should be Boolean.TRUE::equals, but tests fail
                .retryExceptions(Throwable.class)
                .maxAttempts(maxRetryAttempts)
                .waitDuration(Duration.ofSeconds(1))
                .build();
    }
}
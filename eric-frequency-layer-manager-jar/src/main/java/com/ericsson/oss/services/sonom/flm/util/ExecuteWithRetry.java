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

package com.ericsson.oss.services.sonom.flm.util;

import java.util.function.Function;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;

/**
 * Utility class for executing with retries.
 */
public final class ExecuteWithRetry {

    private ExecuteWithRetry() {
        // Intentionally private.
    }

    /**
     * Wrapper method to execute a function to return a value with a retry when any of the allowed exceptions are thrown. It will throw the provided
     * exception on retry exhaustion (i.e. all retry attempts fail).
     * 
     * @param functionToDecorateWithRetry
     *            The function to execute.
     * @param exceptionToThrowOnRetryExhaustion
     *            Exception to throw when all retry attempts are exhausted.
     * @param nameOfRetry
     *            An alias for this retry request.
     * @param retryConfig
     *            A retry Resilience4J {@link RetryConfig} definition.
     * @param <T>
     *            Return type of the executed function.
     * @param <R>
     *            Type of Exception to be thrown on retry exhaustion.
     * @return Return type of the executed function.
     *
     * @throws R Thrown when all retries are exhausted.
     */
    public static <T, R extends Throwable> T executeWithRetry(
            final CheckedFunction0<T> functionToDecorateWithRetry, final Function<? super Throwable, R> exceptionToThrowOnRetryExhaustion,
            final String nameOfRetry,
            final RetryConfig retryConfig)
            throws R {

        final CheckedFunction0<T> functionDecoratedWithRetry = Retry.decorateCheckedSupplier(
                buildRetry(nameOfRetry, retryConfig),
                functionToDecorateWithRetry);

        return Try.of(functionDecoratedWithRetry)
                .getOrElseThrow(exceptionToThrowOnRetryExhaustion);
    }

    /**
     * Wrapper method to execute a void function with a retry when any of the allowed exceptions are thrown. It will throw the provided exception on
     * retry * exhaustion (i.e. all retry attempts fail).
     * 
     * @param functionToDecorateWithRetry
     *            The function to execute.
     * @param exceptionToThrowOnRetryExhaustion
     *            Exception to throw when all retry attempts are exhausted.
     * @param nameOfRetry
     *            An alias for this retry request.
     * @param retryConfig
     *            A retry Resilience4J {@link RetryConfig} definition.
     * @param <T>
     *            Type of Exception to be thrown on retry exhaustion.
     * @throws T Thrown when all retries are exhausted.
     */
    public static <T extends Throwable> void executeWithRetry(
            final CheckedRunnable functionToDecorateWithRetry, final Function<? super Throwable, T> exceptionToThrowOnRetryExhaustion,
            final String nameOfRetry,
            final RetryConfig retryConfig)
            throws T {

        final CheckedRunnable functionDecoratedWithRetry = Retry.decorateCheckedRunnable(
                buildRetry(nameOfRetry, retryConfig),
                functionToDecorateWithRetry);

        Try.run(functionDecoratedWithRetry).getOrElseThrow(exceptionToThrowOnRetryExhaustion);
    }

    private static Retry buildRetry(final String nameOfRetry, final RetryConfig config) {
        return Retry.of(nameOfRetry, config);
    }
}

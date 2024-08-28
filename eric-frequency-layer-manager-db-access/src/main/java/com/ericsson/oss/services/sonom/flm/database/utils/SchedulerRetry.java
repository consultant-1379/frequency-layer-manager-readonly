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

package com.ericsson.oss.services.sonom.flm.database.utils;

import static io.github.resilience4j.retry.RetryConfig.custom;

import java.time.Duration;
import java.util.function.Function;

import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

/**
 * Represents scheduler retry.
 */
public final class SchedulerRetry {

    private final int maxRetryAttempts;
    private final long retryWaitDuration;

    public SchedulerRetry(final int maxRetryAttempts, final long retryWaitDuration) {
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryWaitDuration = retryWaitDuration;
    }

    /**
     * Wrapper method to execute a function to return a value with a retry when any of the allowed exceptions are thrown. It will throw the provided
     * exception on retry exhaustion (i.e. all retry attempts fail).
     *
     * @param functionToDecorateWithRetry
     *            The function to execute.
     * @param exceptionToThrowOnRetryExhaustion
     *            Exception to throw when all retry attempts are exhausted.
     * @param <T>
     *            Return type of the executed function.
     * @param <R>
     *            Type of Exception to be thrown on retry exhaustion.
     * @return Return type of the executed function.
     * @throws R
     *             Thrown when all retries are exhausted.
     */
    public <T, R extends Throwable> T executeWithRetry(
            final CheckedFunction0<T> functionToDecorateWithRetry, final Function<? super Throwable, R> exceptionToThrowOnRetryExhaustion)
            throws R {
        final CheckedFunction0<T> functionDecoratedWithRetry = Retry.decorateCheckedSupplier(getRetryForExecuteQuery(),
                functionToDecorateWithRetry);

        return Try.of(functionDecoratedWithRetry).getOrElseThrow(exceptionToThrowOnRetryExhaustion);
    }

    private Retry getRetryForExecuteQuery() {

        final RetryConfig config = custom()
                .retryOnResult(s -> s.equals(true))
                .retryExceptions(ActivitySchedulerException.class)
                .maxAttempts(maxRetryAttempts)
                .waitDuration(Duration.ofSeconds(retryWaitDuration))
                .build();
        return Retry.of("scheduleActivity", config);
    }
}

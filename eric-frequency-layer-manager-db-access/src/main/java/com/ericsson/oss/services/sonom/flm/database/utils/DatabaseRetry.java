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

import java.sql.SQLException;
import java.time.Duration;
import java.util.function.Function;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

/**
 * Represents database retry.
 */
public final class DatabaseRetry {

    private final int maxRetryAttempts;
    private final long retryWaitDuration;

    public DatabaseRetry(final int maxRetryAttempts, final long retryWaitDuration) {
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryWaitDuration = retryWaitDuration;
    }

    /**
     * Retries database queries.
     *
     * @param functionToDecorateWithRetry
     *            the function to execute with the retry.
     * @param <T>
     *            generic function
     * @return Generic
     * @throws SQLException
     *             This exception will be raised if any error occurs during database access.
     */
    public <T> T executeWithRetryAttempts(
            final CheckedFunction0<T> functionToDecorateWithRetry)
            throws SQLException {
        final Retry retry = getRetryForExecuteQuery();
        final CheckedFunction0<T> functionDecoratedWithRetry = Retry.decorateCheckedSupplier(retry,
                functionToDecorateWithRetry);

        return Try.of(functionDecoratedWithRetry).getOrElseThrow((Function<Throwable, SQLException>) SQLException::new);
    }

    private Retry getRetryForExecuteQuery() {
        final RetryConfig config = custom()
                .retryOnResult(s -> s.equals(true))
                .retryExceptions(SQLException.class)
                .maxAttempts(maxRetryAttempts)
                .waitDuration(Duration.ofSeconds(retryWaitDuration))
                .build();
        return Retry.of("executeQuery", config);
    }
}

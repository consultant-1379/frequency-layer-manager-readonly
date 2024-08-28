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

package com.ericsson.oss.services.sonom.flm.executions;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.util.RowsUpdated;

/**
 * Handles operations on executions that need to go to the DB. Basically wraps the retry configuration.
 */
public final class ExecutionDbHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionDbHandler.class);
    private static final int EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 10;
    private static final int EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 30;
    private ExecutionDao executionDao = new ExecutionDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS, EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS); // NOPMD
    private RowsUpdated rowsUpdated = new RowsUpdated(); // NOPMD

    /**
     * Takes an {@link Execution}(s) and sets the state to failed in the DB for each one.
     *
     * @param executions
     *            the {@link Execution}(s) to set the state on.
     * @return The number of executions successfully updated.
     */
    public int applyFailedState(final Execution... executions) {
        final AtomicInteger numberOfExecutionsUpdated = new AtomicInteger();
        Arrays.stream(executions).forEach(execution -> {
            try {

                if (execution.getState().isFinal()) {
                    return;
                }

                execution.setState(ExecutionState.FAILED);

                LOGGER.info("Execution ID {} setting status to {}", execution.getId(), ExecutionState.FAILED);
                final int updatedRows = executionDao.update(execution);

                final Runnable updateFailed = () -> LOGGER.warn("Execution ID {} status not set to {}", execution.getId(), ExecutionState.FAILED);
                final Runnable updateSucceeded = () -> LOGGER.debug("Execution ID {} status set to {}", execution.getId(), ExecutionState.FAILED);

                rowsUpdated.verifyRowsUpdated(updatedRows, updateFailed, updateSucceeded);
                numberOfExecutionsUpdated.incrementAndGet();
            } catch (final SQLException e1) {
                LOGGER.error("Failed setting status to failed for execution ID {}", execution.getId(), e1);
            }
        });
        return numberOfExecutionsUpdated.get();
    }

    /**
     * Takes an {@link Execution}(s) and increments the retryAttempts in the DB for each one.
     *
     * @param executions
     *            The {@link Execution}(s) to increment retryAttempts property on.
     * @return The number of executions successfully updated.
     */
    public int incrementRetryAttempts(final Execution... executions) {
        final AtomicInteger numberOfExecutionsUpdated = new AtomicInteger();
        Arrays.stream(executions).forEach(execution -> {
            try {
                execution.setRetryAttempts(execution.getRetryAttempts() + 1);

                LOGGER.info("Execution ID {} incrementing retryAttempts to {}", execution.getId(), execution.getRetryAttempts());
                final int updatedRows = executionDao.update(execution);

                final Runnable updateFailed = () -> LOGGER.warn("Execution ID {} not incremented retryAttempts to {}", execution.getId(),
                        execution.getRetryAttempts());
                final Runnable updateSucceeded = () -> LOGGER.debug("Execution ID {} incremented retryAttempts to {}", execution.getId(),
                        execution.getRetryAttempts());

                rowsUpdated.verifyRowsUpdated(updatedRows, updateFailed, updateSucceeded);
                numberOfExecutionsUpdated.incrementAndGet();
            } catch (final SQLException e1) {
                LOGGER.error("Failed incrementing retryAttempts for execution ID {}", execution.getId(), e1);
            }
        });
        return numberOfExecutionsUpdated.get();
    }
}

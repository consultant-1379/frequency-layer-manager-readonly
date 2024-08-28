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
package com.ericsson.oss.services.sonom.flm.executor;

import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.EXECUTION_STATE_PERSISTENCE_ERROR;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * PersistenceHandler handles persistence during FLM algorithm executions.
 */
public class PersistenceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceHandler.class);

    private final Execution execution;
    private final ExecutionDao executionDao;

    public PersistenceHandler(final Execution execution, final ExecutionDao executionDao) {
        this.execution = execution;
        this.executionDao = executionDao;
    }

    /**
     * Persist the execution status. <br>
     *
     * @param state
     *            the execution status state.
     * @param isResumed
     *            <code>true</code> if resumed execution
     * @return the duration of the persist operation.
     * @throws FlmAlgorithmException
     *             if persistence to DB fails.
     */
    public long persistExecutionStatus(final ExecutionState state, final boolean isResumed) throws FlmAlgorithmException {
        try {
            final long persistExecutionStartTime = System.nanoTime(); //NOPMD Only used when returning from the method.
            execution.setState(state);
            final long executionTime = System.currentTimeMillis();
            execution.setStateModifiedTime(new Timestamp(executionTime));
            if (isNewExecution(state, isResumed)) {
                execution.setStartTime(new Timestamp(executionTime));
                execution.setId(executionDao.insert(execution));
            } else {
                final int updatedRows = executionDao.update(execution);
                if (updatedRows == 0) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(),
                            String.format("Failed to persist update for execution: %s", execution)));
                    }
                    throw new FlmAlgorithmException(EXECUTION_STATE_PERSISTENCE_ERROR);
                }
            }

            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - persistExecutionStartTime);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Failed to persist execution : {}", e.getMessage());
            throw new FlmAlgorithmException(EXECUTION_STATE_PERSISTENCE_ERROR, e);
        }
    }

    private boolean isNewExecution(final ExecutionState state, final Boolean isResumed) {
        return state == ExecutionState.getInitialState() && !isResumed;
    }
}

/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
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

import java.sql.SQLException;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * Handle whether an execution is SUCCEEDED or PARTIALLY_SUCCEEDED.
 */
public class ExecutionFinalState {

    private static final String ERROR_MESSAGE_TABLE_INSERTION = "Error inserting into the historical cell table.";
    private static final String ERROR_MESSAGE_POLICY_ENGINE_MESSAGES = "Not all events received from the Policy Engine.";
    private static final String ERROR_MESSAGE_CM_CHANGES_NOT_PERSISTED = "Not all changes were persisted to the CM DB.";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionFinalState.class);

    private ExecutionFinalState() {

    }

    /**
     * Sets the final state for execution.
     *
     * @param execution  execution to extract info.
     * @param executionDao to get execution information using this dao.
     * @param persistenceHandler to persist execution information.
     * @param isResumed to check for resuming.
     * @throws SQLException to throw  sql execption.
     * @throws FlmAlgorithmException to throw custom exception.
     */
    public static void setFinalState(final Execution execution,
                                     final ExecutionDao executionDao,
                                     final PersistenceHandler persistenceHandler,
                                     final boolean isResumed) throws SQLException, FlmAlgorithmException {
        final Execution executionFromDb = executionDao.get(execution.getId());

        final boolean isNumOptimizationElementsEqual = (executionFromDb.getNumOptimizationElementsSent()
                .equals(executionFromDb.getNumOptimizationElementsReceived()));
        final boolean isPersistedCorrectlyToCmDb = executionFromDb.getNumChangesNotWrittenToCmDb() == 0;
        final boolean isSuccessfulExecution = execution.getState() == ExecutionState.SUCCEEDED &&
                isNumOptimizationElementsEqual && isPersistedCorrectlyToCmDb;
        final boolean isPartiallySuccessfulExecution = (execution.getState() == ExecutionState.CELL_SETTINGS_HISTORY)
                || (execution.getState() == ExecutionState.SUCCEEDED && (!isNumOptimizationElementsEqual || !isPersistedCorrectlyToCmDb));

        if (isSuccessfulExecution) {
            persistenceHandler.persistExecutionStatus(ExecutionState.SUCCEEDED, isResumed);
        } else if (isPartiallySuccessfulExecution) {
            populateAdditionalExecutionInformation(execution, isNumOptimizationElementsEqual, isPersistedCorrectlyToCmDb);
            persistenceHandler.persistExecutionStatus(ExecutionState.PARTIALLY_SUCCEEDED, isResumed);
        } else {
            persistenceHandler.persistExecutionStatus(ExecutionState.FAILED, isResumed);
        }

        final Execution executionWithFinalState = executionDao.get(execution.getId());

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionWithFinalState.getId(),
                    String.format("Execution is in state %s", executionWithFinalState.getState())));
        }
    }

    private static void populateAdditionalExecutionInformation(final Execution execution,
                                                               final boolean isNumOptimizationElementsEqual,
                                                               final boolean isPersistedCorrectlyToDb) {
        final StringJoiner additionalExecutionInformation = new StringJoiner(" ");

        if (execution.getState() == ExecutionState.CELL_SETTINGS_HISTORY) {
            additionalExecutionInformation.add(ERROR_MESSAGE_TABLE_INSERTION);
        }

        if (!isNumOptimizationElementsEqual) {
            additionalExecutionInformation.add(ERROR_MESSAGE_POLICY_ENGINE_MESSAGES);
        }

        if (!isPersistedCorrectlyToDb) {
            additionalExecutionInformation.add(ERROR_MESSAGE_CM_CHANGES_NOT_PERSISTED);
        }

        execution.setAdditionalExecutionInformation(additionalExecutionInformation.toString());
    }
}
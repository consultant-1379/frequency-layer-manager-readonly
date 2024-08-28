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

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

/**
 * Unit tests for {@link ExecutionFinalState} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutionFinalStateTest {

    @Mock
    private Execution execution;

    @Mock
    private Execution executionFromDb;

    @Mock
    private ExecutionDao executionDao;

    @Mock
    private PersistenceHandler persistenceHandler;

    @Test
    public void whenSuccessfulExecutionElementsSentIsNotEqualToElementsReceived_thenExecutionIsMarkedAsPartiallySucceeded() throws SQLException, FlmAlgorithmException {
        when(executionDao.get(execution.getId())).thenReturn(executionFromDb);
        when(execution.getState()).thenReturn(ExecutionState.SUCCEEDED);
        when(executionFromDb.getNumOptimizationElementsSent()).thenReturn(20);
        when(executionFromDb.getNumOptimizationElementsReceived()).thenReturn(18);

        ExecutionFinalState.setFinalState(execution, executionDao, persistenceHandler, false);

        verify(execution).setAdditionalExecutionInformation("Not all events received from the Policy Engine.");
        verify(persistenceHandler).persistExecutionStatus(ExecutionState.PARTIALLY_SUCCEEDED, false);
    }

    @Test
    public void whenSuccessfulExecutionElementsSentIsEqualToElementsReceived_thenExecutionIsMarkedAsSucceeded() throws SQLException, FlmAlgorithmException {
        when(executionDao.get(execution.getId())).thenReturn(executionFromDb);
        when(execution.getState()).thenReturn(ExecutionState.SUCCEEDED);
        when(executionFromDb.getNumOptimizationElementsSent()).thenReturn(20);
        when(executionFromDb.getNumOptimizationElementsReceived()).thenReturn(20);

        ExecutionFinalState.setFinalState(execution, executionDao, persistenceHandler, false);

        verify(persistenceHandler).persistExecutionStatus(ExecutionState.SUCCEEDED, false);
    }

    @Test
    public void whenExecutionFailsToCopyToHistoricalTable_thenExecutionIsMarkedAsPartialSuccess() throws SQLException, FlmAlgorithmException {
        when(executionDao.get(execution.getId())).thenReturn(executionFromDb);
        when(execution.getState()).thenReturn(ExecutionState.CELL_SETTINGS_HISTORY);
        when(executionFromDb.getNumOptimizationElementsSent()).thenReturn(20);
        when(executionFromDb.getNumOptimizationElementsReceived()).thenReturn(20);

        ExecutionFinalState.setFinalState(execution, executionDao, persistenceHandler, false);

        verify(execution).setAdditionalExecutionInformation("Error inserting into the historical cell table.");
        verify(persistenceHandler).persistExecutionStatus(ExecutionState.PARTIALLY_SUCCEEDED, false);
    }

    @Test
    public void whenExecutionHasThreePartialSuccessReasons_thenExecutionIsMarkedAsPartialSuccess() throws SQLException, FlmAlgorithmException {
        when(executionDao.get(execution.getId())).thenReturn(executionFromDb);
        when(execution.getState()).thenReturn(ExecutionState.CELL_SETTINGS_HISTORY);
        when(executionFromDb.getNumOptimizationElementsSent()).thenReturn(20);
        when(executionFromDb.getNumOptimizationElementsReceived()).thenReturn(18);
        when(executionFromDb.getNumChangesNotWrittenToCmDb()).thenReturn(2);

        ExecutionFinalState.setFinalState(execution, executionDao, persistenceHandler, false);

        verify(execution).setAdditionalExecutionInformation("Error inserting into the historical cell table. " +
                "Not all events received from the Policy Engine. Not all changes were persisted to the CM DB.");
        verify(persistenceHandler).persistExecutionStatus(ExecutionState.PARTIALLY_SUCCEEDED, false);
    }

    @Test
    public void whenExecutionIsNotSuccessOrPartialSuccess_thenExecutionIsMarkedAsFailed() throws SQLException, FlmAlgorithmException {
        when(executionDao.get(execution.getId())).thenReturn(executionFromDb);
        when(execution.getState()).thenReturn(ExecutionState.KPI_PROCESSING_GROUP_2);
        when(executionFromDb.getNumOptimizationElementsSent()).thenReturn(20);
        when(executionFromDb.getNumOptimizationElementsReceived()).thenReturn(18);

        ExecutionFinalState.setFinalState(execution, executionDao, persistenceHandler, false);

        verify(persistenceHandler).persistExecutionStatus(ExecutionState.FAILED, false);
    }

    @Test
    public void whenExecutionCannotPersistChangesToChangeDb_thenExecutionIsMarkedAsPartialSuccess() throws SQLException, FlmAlgorithmException {
        when(executionDao.get(execution.getId())).thenReturn(executionFromDb);
        when(execution.getState()).thenReturn(ExecutionState.SUCCEEDED);
        when(executionFromDb.getNumChangesNotWrittenToCmDb()).thenReturn(2);

        ExecutionFinalState.setFinalState(execution, executionDao, persistenceHandler, false);

        verify(execution).setAdditionalExecutionInformation("Not all changes were persisted to the CM DB.");
        verify(persistenceHandler).persistExecutionStatus(ExecutionState.PARTIALLY_SUCCEEDED, false);
    }
}

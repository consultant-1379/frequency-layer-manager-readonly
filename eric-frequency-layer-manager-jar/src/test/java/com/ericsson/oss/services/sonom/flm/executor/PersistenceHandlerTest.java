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

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_1;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_7;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SUCCEEDED;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.getInitialState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * Unit tests for {@link PersistenceHandler} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PersistenceHandlerTest {

    @Mock
    private Execution execution;

    @Mock
    private ExecutionDao executionDao;

    @InjectMocks
    private PersistenceHandler persistenceHandler;

    @Test
    public void whenAttemptingToPersistSucceededNonResumedExecution_thenPersistsSuccessfully() throws SQLException, FlmAlgorithmException {
        when(executionDao.update(execution)).thenReturn(1);

        final long returnedVal = persistenceHandler.persistExecutionStatus(SUCCEEDED, false);

        verify(executionDao).update(execution);
        assertThat(returnedVal).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void whenAttemptingToPersistSucceededResumedExecution_thenPersistsSuccessfully() throws SQLException, FlmAlgorithmException {
        when(executionDao.update(execution)).thenReturn(1);

        final long returnedVal = persistenceHandler.persistExecutionStatus(SUCCEEDED, true);
        verify(executionDao).update(execution);

        assertThat(returnedVal).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void whenAttemptingToUpdateExecutionThatDoesNotExistInExecutionDao_thenThrowsException() throws FlmAlgorithmException, SQLException {
        try {
            persistenceHandler.persistExecutionStatus(KPI_PROCESSING_GROUP_1, true);
            shouldHaveThrown(FlmAlgorithmException.class);
        } catch (final FlmAlgorithmException e) {
            verify(execution).setState(KPI_PROCESSING_GROUP_1);
            verify(executionDao).update(execution);
        }
    }

    @Test
    public void whenAttemptingToInsertNewExecution_thenExecutionDaoInsertIsCalled() throws SQLException, FlmAlgorithmException {
        when(executionDao.insert(execution)).thenReturn("FLM_1600701252");
        persistenceHandler.persistExecutionStatus(getInitialState(), false);

        verify(execution).setState(getInitialState());
        verify(execution).setStateModifiedTime(any());
        verify(execution).setStartTime(any());
        verify(executionDao).insert(execution);
        verify(execution).setId(executionDao.insert(execution));
    }

    @Test
    public void whenAttemptingToUpdateExistingExecution_thenExecutionDaoUpdateIsCalled() throws SQLException, FlmAlgorithmException {
        when(executionDao.update(execution)).thenReturn(1);

        persistenceHandler.persistExecutionStatus(KPI_PROCESSING_GROUP_7, true);

        verify(executionDao, times(1)).update(execution);
    }

    @Test
    public void whenAttemptingToInsertInvalidExecution_thenThrowException() throws SQLException, FlmAlgorithmException {
        when(executionDao.insert(execution)).thenThrow(SQLException.class);

        try {
            persistenceHandler.persistExecutionStatus(getInitialState(), false);
            shouldHaveThrown(FlmAlgorithmException.class);
        } catch (final FlmAlgorithmException e) {
            verify(executionDao).insert(execution);
        }
    }
}

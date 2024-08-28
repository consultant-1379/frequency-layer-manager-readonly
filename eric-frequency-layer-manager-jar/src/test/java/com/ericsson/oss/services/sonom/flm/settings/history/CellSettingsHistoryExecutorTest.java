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
package com.ericsson.oss.services.sonom.flm.settings.history;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.CELL_SETTINGS_HISTORY;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.FAILED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * Unit tests for {@link CellSettingsHistoryExecutor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellSettingsHistoryExecutorTest {

    private static final String EXECUTION_ID = "FLM_1600701252-162";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final Timestamp executionTimeStamp = Timestamp.valueOf("2020-10-19 16:03:15.930");

    @Mock
    private Execution executionMock;

    @Mock
    private PersistenceHandler persistenceHandlerMock;

    @Mock
    private CellSettingsHistoryProcessor cellSettingsHistoryProcessorMock;

    @Mock
    private MetricHelper flmMetricHelperMock;

    private CellSettingsHistoryExecutor cellSettingsHistoryExecutor;

    @Test
    public void whenResumingInsertToCellSettingsHistory_thenInsertionContinue() throws FlmAlgorithmException, SQLException {
        final ExecutionState executionState = CELL_SETTINGS_HISTORY;
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(executionTimeStamp.toLocalDateTime());

        when(executionMock.getState()).thenReturn(executionState);
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        doNothing().when(cellSettingsHistoryProcessorMock).insertCellSettingsIntoHistoricalTable(CELL_SETTINGS_HISTORY, EXECUTION_ID, true);
        cellSettingsHistoryExecutor = new CellSettingsHistoryExecutor(executionMock, flmMetricHelperMock, persistenceHandlerMock,
                cellSettingsHistoryProcessorMock);
        cellSettingsHistoryExecutor.execute(executionState, true, true, resumeExecutionDate);

        final InOrder inOrder = inOrder(cellSettingsHistoryProcessorMock, persistenceHandlerMock, executionMock, flmMetricHelperMock);
        inOrder.verify(cellSettingsHistoryProcessorMock, times(1)).insertCellSettingsIntoHistoricalTable(any(ExecutionState.class),
                eq(EXECUTION_ID), eq(true));
        inOrder.verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        inOrder.verify(flmMetricHelperMock).incrementFlmMetric(eq(FlmMetric.FLM_CELL_SETTINGS_HISTORY_COPY_TIME_IN_MILLIS), anyLong());
    }

    @Test
    public void whenResumingCellSettingsHistoryFromAnUnrecognisedState_thenDoNothing() throws FlmAlgorithmException {
        final ExecutionState executionState = FAILED;
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(executionTimeStamp.toLocalDateTime());

        when(executionMock.getState()).thenReturn(executionState);

        cellSettingsHistoryExecutor = new CellSettingsHistoryExecutor(executionMock, flmMetricHelperMock, persistenceHandlerMock);

        cellSettingsHistoryExecutor.execute(executionState, false, true, resumeExecutionDate);

        verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        verify(flmMetricHelperMock).incrementFlmMetric(any(FlmMetric.class), anyLong());
        verifyNoMoreInteractions(flmMetricHelperMock);
    }

    @Test
    public void whenPersistingAnExecutionFails_thenAnFlmAlgorithmExceptionIsThrown() throws FlmAlgorithmException {
        final Execution realExecution = new Execution();
        final ExecutionState executionState = CELL_SETTINGS_HISTORY;

        realExecution.setId(EXECUTION_ID);
        realExecution.setState(executionState);
        cellSettingsHistoryExecutor = new CellSettingsHistoryExecutor(realExecution, flmMetricHelperMock,
                new PersistenceHandler(realExecution, new ExecutionDaoImpl(3, 1)));

        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(executionTimeStamp.toLocalDateTime());
        thrown.expect(FlmAlgorithmException.class);
        cellSettingsHistoryExecutor.execute(executionState, false, true, resumeExecutionDate);
    }
}
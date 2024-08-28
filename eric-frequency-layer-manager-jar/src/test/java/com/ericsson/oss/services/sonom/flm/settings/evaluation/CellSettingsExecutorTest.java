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
package com.ericsson.oss.services.sonom.flm.settings.evaluation;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.FAILED;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SETTINGS_PROCESSING;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SETTINGS_PROCESSING_SUCCEEDED;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * Unit tests for {@link CellSettingsExecutor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellSettingsExecutorTest {

    private static final String EXECUTION_ID = "FLM_1600701252-162";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final Timestamp executionTimeStamp = Timestamp.valueOf("2020-05-07 10:53:15.930");

    @Mock
    private CmStore cmStoreMock;

    @Mock
    private Execution executionMock;

    @Mock
    private PersistenceHandler persistenceHandlerMock;

    @Mock
    private CellSettingsProcessor cellSettingsProcessorMock;

    @Mock
    private MetricHelper flmMetricHelperMock;

    @InjectMocks
    private CellSettingsExecutor objectUnderTest;

    static {
        //Must be set so that Mockito can create the mock for CellSettingsHandler
        System.setProperty("CM_SERVICE_HOSTNAME", "localhost");
        System.setProperty("CM_SERVICE_PORT", "8080");
    }

    @Test
    public void whenResumingCellSettingsEvaluation_thenEvaluationsContinue() throws InterruptedException, SQLException, ExecutionException,
            FlmAlgorithmException {
        final ExecutionState executionState = SETTINGS_PROCESSING;
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(executionTimeStamp.toLocalDateTime());

        objectUnderTest = new CellSettingsExecutor(executionMock, flmMetricHelperMock, persistenceHandlerMock, cellSettingsProcessorMock);

        when(executionMock.getState()).thenReturn(executionState);
        doNothing().when(cellSettingsProcessorMock).processApplySettingsToCell(SETTINGS_PROCESSING, resumeExecutionDate, true);
        objectUnderTest.execute(executionState, true, true, resumeExecutionDate);

        final InOrder inOrder = inOrder(cellSettingsProcessorMock, persistenceHandlerMock, executionMock, flmMetricHelperMock);
        inOrder.verify(cellSettingsProcessorMock, times(1)).processApplySettingsToCell(any(ExecutionState.class), eq(resumeExecutionDate), eq(true));
        inOrder.verify(persistenceHandlerMock).persistExecutionStatus(SETTINGS_PROCESSING_SUCCEEDED, true);
        inOrder.verify(executionMock).getState();
        inOrder.verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        inOrder.verify(flmMetricHelperMock).incrementFlmMetric(eq(FlmMetric.SETTINGS_PROCESSING_TIME_IN_MILLIS), anyLong());
    }

    @Test
    public void whenNewCellSettingsEvaluation_thenEvaluationsExecuteInOrder() throws FlmAlgorithmException, InterruptedException,
            ExecutionException, SQLException {
        final ExecutionState executionState = SETTINGS_PROCESSING;
        final String executionDate = DateTimeFormatter.ISO_DATE.format(executionTimeStamp.toLocalDateTime());

        objectUnderTest = new CellSettingsExecutor(executionMock, flmMetricHelperMock, persistenceHandlerMock, cellSettingsProcessorMock);

        when(executionMock.getState()).thenReturn(SETTINGS_PROCESSING);
        objectUnderTest.execute(executionState, false, true, executionDate);

        final InOrder inOrder = inOrder(cellSettingsProcessorMock, persistenceHandlerMock, executionMock, flmMetricHelperMock);
        inOrder.verify(cellSettingsProcessorMock, times(1)).processApplySettingsToCell(any(ExecutionState.class), eq(executionDate), eq(false));
        inOrder.verify(persistenceHandlerMock).persistExecutionStatus(SETTINGS_PROCESSING_SUCCEEDED, false);
        inOrder.verify(executionMock).getState();
        inOrder.verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        inOrder.verify(flmMetricHelperMock).incrementFlmMetric(eq(FlmMetric.SETTINGS_PROCESSING_TIME_IN_MILLIS), anyLong());
    }

    @Test
    public void whenResumingCellSettingsFromAnUnrecognisedState_thenDoNothing() throws FlmAlgorithmException {
        final Execution realExecution = new Execution();
        final ExecutionState executionState = FAILED;

        realExecution.setId(EXECUTION_ID);
        realExecution.setState(executionState);
        final CellSettingsExecutor cellSettingExecutor = new CellSettingsExecutor(cmStoreMock, realExecution, flmMetricHelperMock,
                persistenceHandlerMock);

        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(executionTimeStamp.toLocalDateTime());

        cellSettingExecutor.execute(executionState, true, true, resumeExecutionDate);

        verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        verify(flmMetricHelperMock).incrementFlmMetric(any(FlmMetric.class), anyLong());
        verifyNoMoreInteractions(flmMetricHelperMock);
    }

    @Test
    public void whenPersistingAnExecutionFails_thenAnFlmAlgorithmExceptionIsThrown() throws FlmAlgorithmException {
        final Execution realExecution = new Execution();
        final ExecutionState executionState = SETTINGS_PROCESSING_SUCCEEDED;

        realExecution.setId(EXECUTION_ID);
        realExecution.setState(executionState);
        final CellSettingsExecutor cellSettingExecutor = new CellSettingsExecutor(cmStoreMock, realExecution, flmMetricHelperMock,
                new PersistenceHandler(realExecution, new ExecutionDaoImpl(3, 1)));

        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(executionTimeStamp.toLocalDateTime());

        thrown.expect(FlmAlgorithmException.class);
        cellSettingExecutor.execute(executionState, false, true, resumeExecutionDate);
    }
}
/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2023
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

import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.CANCELLED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.SCHEDULED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.STARTED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.SUCCEEDED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.TERMINATED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.TERMINATING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionsScheduler;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

/**
 * Unit tests for {@link PreAlgorithmExecutor} class.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({ PAExecutionsScheduler.class })
public class PreAlgorithmExecutorTest {
    private static final String EXECUTION_ID_1 = "flmExecution1";
    private static final String EXECUTION_ID_2 = "flmExecution2";
    private static final LocalDateTime PA_WINDOW_START_TIME = LocalDateTime.of(2021, Month.AUGUST, 23, 2, 30, 0);

    @Mock
    private Execution mockedExecution;
    @Mock
    private Execution mockedPrevExecution;
    @Mock
    private ExecutionDaoImpl executionDao;
    @Mock
    private PAExecutionDaoImpl paExecutionDao;

    private PreAlgorithmExecutor objUnderTest;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        Whitebox.setInternalState(PreAlgorithmExecutor.class, "RETRY_MAX_ATTEMPTS", 5);
        Whitebox.setInternalState(PreAlgorithmExecutor.class, "RETRY_WAIT_DURATION", 1);
        Whitebox.setInternalState(PreAlgorithmExecutor.class, "TIMEOUT_OFFSET", TimeUnit.SECONDS.toMillis(30));

        when(mockedPrevExecution.getId()).thenReturn(EXECUTION_ID_1);
        when(mockedPrevExecution.getConfigurationId()).thenReturn(1);
        when(mockedPrevExecution.isEnablePA()).thenReturn(true);

        when(mockedExecution.getId()).thenReturn(EXECUTION_ID_2);
        when(mockedExecution.getConfigurationId()).thenReturn(1);
        when(mockedExecution.isFullExecution()).thenReturn(true);
        when(mockedExecution.getState()).thenReturn(ExecutionState.WAITING);

        when(executionDao.getExecutionsInStates(ExecutionState.SUCCEEDED, ExecutionState.PARTIALLY_SUCCEEDED)).thenReturn(Collections.singletonList(mockedPrevExecution));

        PowerMockito.mockStatic(PAExecutionsScheduler.class);
        PowerMockito.doNothing().when(PAExecutionsScheduler.class, "terminateRunningExecution", any());
        PowerMockito.doNothing().when(PAExecutionsScheduler.class, "cancelScheduledExecutions", any());

        this.objUnderTest = new PreAlgorithmExecutor(mockedExecution, executionDao, paExecutionDao);
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andPaIsRunningAndScheduled_thenMethodRunsSuccessfully()
            throws SQLException, FlmAlgorithmException, ActivitySchedulerException {
        when(paExecutionDao.getPAExecutions(EXECUTION_ID_1))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED))
                .thenReturn(getPaExecutions(TERMINATING, CANCELLED, CANCELLED))
                .thenReturn(getPaExecutions(TERMINATED, CANCELLED, CANCELLED));

        objUnderTest.runPreExecutionSteps();

        verify(executionDao, times(1)).getExecutionsInStates(any());
        verify(paExecutionDao, times(3)).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(1));
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(1));
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andOnlyOneExecutionToCancel_thenMethodRunsSuccessfully()
            throws SQLException, FlmAlgorithmException, ActivitySchedulerException {
        when(paExecutionDao.getPAExecutions(EXECUTION_ID_1))
                .thenReturn(getPaExecutions(SUCCEEDED, SUCCEEDED, SCHEDULED))
                .thenReturn(getPaExecutions(SUCCEEDED, SUCCEEDED, CANCELLED));

        objUnderTest.runPreExecutionSteps();

        verify(executionDao, times(1)).getExecutionsInStates(any());
        verify(paExecutionDao, times(2)).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(1));
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andOnlyOneExecutionToTerminate_thenMethodRunsSuccessfully()
            throws SQLException, FlmAlgorithmException, ActivitySchedulerException {
        when(paExecutionDao.getPAExecutions(EXECUTION_ID_1))
                .thenReturn(getPaExecutions(SUCCEEDED, SUCCEEDED, STARTED))
                .thenReturn(getPaExecutions(SUCCEEDED, SUCCEEDED, TERMINATING))
                .thenReturn(getPaExecutions(SUCCEEDED, SUCCEEDED, TERMINATED));

        objUnderTest.runPreExecutionSteps();

        verify(executionDao, times(1)).getExecutionsInStates(any());
        verify(paExecutionDao, times(3)).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(1));
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andPreviousExecutionDoesNotHavePaEnabled_thenDoNotRunCompleteMethod()
            throws SQLException, FlmAlgorithmException, ActivitySchedulerException {
        when(mockedPrevExecution.isEnablePA()).thenReturn(false);
        objUnderTest.runPreExecutionSteps();

        verify(executionDao, times(1)).getExecutionsInStates(any());
        verify(paExecutionDao, never()).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andExecutionIsNotAFullExecution_thenDoNotRunCompleteMethod()
            throws SQLException, FlmAlgorithmException, ActivitySchedulerException {
        when(mockedExecution.isFullExecution()).thenReturn(false);
        objUnderTest.runPreExecutionSteps();

        verify(executionDao, never()).getExecutionsInStates(any());
        verify(paExecutionDao, never()).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andExecutionIsNotInInitialState_thenDoNotRunCompleteMethod()
            throws SQLException, FlmAlgorithmException, ActivitySchedulerException {
        when(mockedExecution.getState()).thenReturn(ExecutionState.CHECK_IF_NON_SETTINGS_KPI_CALCULATIONS_REQUIRED);
        objUnderTest.runPreExecutionSteps();

        verify(executionDao, never()).getExecutionsInStates(any());
        verify(paExecutionDao, never()).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andExceptionIsThrownWhenTerminatingPaExecution_thenAttemptAndExhaustMaxRetries() throws Exception {
        when(paExecutionDao.getPAExecutions(EXECUTION_ID_1))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED))
                .thenReturn(getPaExecutions(STARTED, CANCELLED, CANCELLED))
                .thenReturn(getPaExecutions(STARTED, CANCELLED, CANCELLED))
                .thenReturn(getPaExecutions(STARTED, CANCELLED, CANCELLED))
                .thenReturn(getPaExecutions(STARTED, CANCELLED, CANCELLED))
                .thenReturn(getPaExecutions(STARTED, CANCELLED, CANCELLED));

        PowerMockito.mockStatic(PAExecutionsScheduler.class);
        PowerMockito.doThrow(new ActivitySchedulerException(""))
                .when(PAExecutionsScheduler.class, "terminateRunningExecution", any());

        try {
            objUnderTest.runPreExecutionSteps();
            shouldHaveThrown(FlmAlgorithmException.class);
        } catch (FlmAlgorithmException e) {
            assertThat(e.getErrorCode()).isEqualTo(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES.getErrorCode());
            assertThat(e.getErrorMessage()).isEqualTo(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES.getErrorMessage());
        }

        verify(executionDao, times(1)).getExecutionsInStates(any());
        verify(paExecutionDao, times(5)).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(1));
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(5));
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andExceptionIsThrownWhenCancellingPaExecution_thenAttemptAndExhaustMaxRetries() throws Exception {
        when(paExecutionDao.getPAExecutions(EXECUTION_ID_1))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED));

        PowerMockito.mockStatic(PAExecutionsScheduler.class);
        PowerMockito.doThrow(new ActivitySchedulerException(""))
                .when(PAExecutionsScheduler.class, "cancelScheduledExecutions", any());

        try {
            objUnderTest.runPreExecutionSteps();
            shouldHaveThrown(FlmAlgorithmException.class);
        } catch (FlmAlgorithmException e) {
            assertThat(e.getErrorCode()).isEqualTo(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES.getErrorCode());
            assertThat(e.getErrorMessage()).isEqualTo(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES.getErrorMessage());
        }

        verify(executionDao, times(1)).getExecutionsInStates(any());
        verify(paExecutionDao, times(5)).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(5));
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andPaExecutionTerminatingIsSlow_thenMaxAttemptsReachedAndExceptionIsThrown() throws Exception {
        when(paExecutionDao.getPAExecutions(EXECUTION_ID_1))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED))
                .thenReturn(getPaExecutions(TERMINATING, CANCELLED, CANCELLED));

        try {
            objUnderTest.runPreExecutionSteps();
            shouldHaveThrown(FlmAlgorithmException.class);
        } catch (FlmAlgorithmException e) {
            assertThat(e.getErrorCode()).isEqualTo(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES.getErrorCode());
            assertThat(e.getErrorMessage()).isEqualTo(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES.getErrorMessage());
        }

        verify(executionDao, times(1)).getExecutionsInStates(any());
        verify(paExecutionDao, times(6)).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(1));
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(1));
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andExceptionIsThrownRetrievingFlmExecutions_thenExceptionIsThrown() throws Exception {
        when(executionDao.getExecutionsInStates(any())).thenThrow(SQLException.class);

        try {
            objUnderTest.runPreExecutionSteps();
            shouldHaveThrown(FlmAlgorithmException.class);
        } catch (FlmAlgorithmException e) {
            assertThat(e.getErrorCode()).isEqualTo(FlmServiceExceptionCode.EXECUTION_STATE_RETRIEVAL_ERROR.getErrorCode());
            assertThat(e.getErrorMessage()).isEqualTo(FlmServiceExceptionCode.EXECUTION_STATE_RETRIEVAL_ERROR.getErrorMessage());
        }

        verify(executionDao, times(1)).getExecutionsInStates(any());
        verify(paExecutionDao, never()).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    @Test
    public void whenRunPreExecutionStepsIsCalled_andTimeoutIsHit_thenExceptionIsThrown() throws Exception {
        when(paExecutionDao.getPAExecutions(EXECUTION_ID_1))
                .thenReturn(getPaExecutions(STARTED, SCHEDULED, SCHEDULED))
                .thenReturn(getPaExecutions(TERMINATING, CANCELLED, CANCELLED));

        Whitebox.setInternalState(PreAlgorithmExecutor.class, "TIMEOUT_OFFSET", TimeUnit.SECONDS.toMillis(1));

        try {
            objUnderTest.runPreExecutionSteps();
            shouldHaveThrown(FlmAlgorithmException.class);
        } catch (FlmAlgorithmException e) {
            assertThat(e.getErrorCode()).isEqualTo(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES.getErrorCode());
            assertThat(e.getErrorMessage()).isEqualTo(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES.getErrorMessage());
        }

        verify(executionDao, times(1)).getExecutionsInStates(any());
        verify(paExecutionDao, times(3)).getPAExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(1));
        PAExecutionsScheduler.cancelScheduledExecutions(any());
        verifyStatic(PAExecutionsScheduler.class, times(1));
        PAExecutionsScheduler.terminateRunningExecution(any());
    }

    private static List<PAExecution> getPaExecutions(final PAExecutionState stateOne, final PAExecutionState stateTwo,
            final PAExecutionState stateThree) {
        return Arrays.asList(getPaExecution(1, stateOne), getPaExecution(2, stateTwo), getPaExecution(3, stateThree));
    }

    private static PAExecution getPaExecution(final int window) {
        return new PAExecution(window, "",
                Timestamp.valueOf(PA_WINDOW_START_TIME.plusHours((window - 1) * 6)),
                Timestamp.valueOf(PA_WINDOW_START_TIME.plusHours((window - 1) * 6).plusHours(6)),
                EXECUTION_ID_1);
    }

    private static PAExecution getPaExecution(final int window, final PAExecutionState executionState) {
        final PAExecution paExecution = getPaExecution(window);
        paExecution.setState(executionState);
        return paExecution;
    }
}

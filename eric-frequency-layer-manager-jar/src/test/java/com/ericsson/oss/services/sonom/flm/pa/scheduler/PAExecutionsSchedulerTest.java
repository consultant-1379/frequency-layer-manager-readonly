/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.pa.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doThrow;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import com.ericsson.oss.services.sonom.common.scheduler.ActivityScheduler;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.common.scheduler.CronSchedule;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

/**
 * Unit tests for {@link PAExecutionsScheduler} class.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({ PAExecutionsScheduler.class, ActivityScheduler.class })
public class PAExecutionsSchedulerTest {

    private static final String DUMMY_PA_WINDOW_START_TIME = "2021-03-12 03:00:00.0";
    private static final String DUMMY_PA_WINDOW_END_TIME = "2021-03-12 08:59:59.0";
    private static final String FLM_EXECUTION_ID = "flm_test_id";

    @Mock
    ActivityScheduler activitySchedulerMock;

    @Mock
    PAExecutionDaoImpl paExecutionDaoMock;

    @Captor
    ArgumentCaptor<PAExecution> paExecutionArgumentCaptor;

    @BeforeClass
    public static void before() {
        //Overriding these for the tests to speed them up
        System.setProperty("PA_SCHEDULER_MAX_RETRY_ATTEMPTS", "3");
        System.setProperty("PA_SCHEDULER_WAIT_PERIOD_IN_SECONDS", "1");

    }

    @AfterClass
    public static void after() {
        System.clearProperty("PA_SCHEDULER_MAX_RETRY_ATTEMPTS");
        System.clearProperty("PA_SCHEDULER_WAIT_PERIOD_IN_SECONDS");
    }

    /**
     * Tests for {@link PAExecutionsScheduler#createSchedule(Execution)}
     */

    @Test
    public void whenPAIsDisabledInTheFlmExecution_thenNoPASchedulesAreCreated() throws Exception {

        PowerMockito.mockStatic(ActivityScheduler.class);

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));// 12 Mar 2021 at 2am
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(false);
        flmExecution.setState(ExecutionState.SUCCEEDED);

        PAExecutionsScheduler.createSchedule(flmExecution);

        verify(activitySchedulerMock, never()).addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));

    }

    @Test
    public void whenPAIsEnabledAndStateIsSucceededInTheFlmExecution_thenThreePASchedulesAreCreatedAndPersistedAsScheduled() throws Exception {

        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));// 12 Mar 2021 at 2am
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.SUCCEEDED);

        PAExecutionsScheduler.createSchedule(flmExecution);

        verify(activitySchedulerMock, times(3))
                .addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));
        verify(paExecutionDaoMock, times(3)).insert(any(PAExecution.class));
    }

    @Test
    public void whenPAIsEnabledAndStateIsPartiallySucceededInTheFlmExecution_thenThreePASchedulesAreCreatedAndPersistedAsScheduled()
            throws Exception {

        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);

        final Execution flmExecution = new Execution();
        //2.00am everyday default flm value
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));// 12 Mar 2021 at 2am
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.PARTIALLY_SUCCEEDED);

        PAExecutionsScheduler.createSchedule(flmExecution);

        verify(activitySchedulerMock, times(3)).addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));
        verify(paExecutionDaoMock, times(3)).insert(any(PAExecution.class));
    }

    @Test
    public void whenPAIsEnabledAndStateIsNotSucceededOrPartiallySucceededInTheFlmExecution_thenNoPASchedulesAreCreated() throws Exception {

        PowerMockito.mockStatic(ActivityScheduler.class);

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));// 12 Mar 2021 at 2am
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.FAILED);

        PAExecutionsScheduler.createSchedule(flmExecution);

        verify(activitySchedulerMock, never()).addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));
    }

    @Test
    public void whenActivitySchedulerExceptionIsThrownForOneOfThePASchedulesAndRetriesAreExhausted_thenOnlyTwoOfTheThreePASchedulesAreCreatedAndPersistAsScheduled()
            throws Exception {

        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);

        doNothing() //pa1 scheduled
                .doThrow(new ActivitySchedulerException("Test Exception")) //pa2 exception
                .doThrow(new ActivitySchedulerException("Test Exception")) //pa2 exception
                .doThrow(new ActivitySchedulerException("Test Exception"))//pa2 exception
                .doNothing()//pa3 scheduled
                .when(activitySchedulerMock).addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));// 12 Mar 2021 at 2am
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.SUCCEEDED);

        PAExecutionsScheduler.createSchedule(flmExecution);

        //pa1-->normal, pa2--> 3 retries, pa3--> normal
        verify(activitySchedulerMock, times(5)).addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));
        verify(paExecutionDaoMock, times(2)).insert(paExecutionArgumentCaptor.capture());
        final List<PAExecution> paExecutions = paExecutionArgumentCaptor.getAllValues();
        assertThat(paExecutions.get(0).getState()).isEqualTo(PAExecutionState.SCHEDULED);
        assertThat(paExecutions.get(1).getState()).isEqualTo(PAExecutionState.SCHEDULED);

    }

    @Test
    public void whenActivitySchedulerExceptionIsThrownForOneOfThePASchedulesAndRetriesAreNotExhausted_thenAllThreePASchedulesAreCreatedAndPersistedAsScheduled()
            throws Exception {

        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);

        doNothing() //pa1 scheduled
                .doThrow(new ActivitySchedulerException("Test")) //pa2 exception
                .doThrow(new ActivitySchedulerException("Test")) //pa2 exception
                .doNothing()//pa2 scheduled
                .doNothing()//pa3 scheduled
                .when(activitySchedulerMock).addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));// 12 Mar 2021 at 2am
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.SUCCEEDED);

        PAExecutionsScheduler.createSchedule(flmExecution);

        //pa1-->normal, pa2-->2 retries then normal, pa3-->normal
        verify(activitySchedulerMock, times(5)).addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));

        verify(paExecutionDaoMock, times(3)).insert(paExecutionArgumentCaptor.capture());
        final List<PAExecution> paExecutions = paExecutionArgumentCaptor.getAllValues();
        assertThat(paExecutions.get(0).getState()).isEqualTo(PAExecutionState.SCHEDULED);
        assertThat(paExecutions.get(1).getState()).isEqualTo(PAExecutionState.SCHEDULED);
        assertThat(paExecutions.get(2).getState()).isEqualTo(PAExecutionState.SCHEDULED);
    }

    /**
     * Resilience tests {@link PAExecutionsScheduler#scheduleExisting(Execution, List)}
     */

    @Test
    public void whenScheduleExistingCalledForFLMExecutionWith3PAExecutionsInScheduledStateAndCronInTheFuture_thenPAExecutionsAreScheduledAgainAsNormalAndUpdatedInTheDB()
            throws Exception {

        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);

        final PAExecution paExecution1 = new PAExecution(1, makeCronInTheFuture(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution1.setState(PAExecutionState.SCHEDULED);
        final PAExecution paExecution2 = new PAExecution(2, makeCronInTheFuture(360), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution2.setState(PAExecutionState.SCHEDULED);
        final PAExecution paExecution3 = new PAExecution(3, makeCronInTheFuture(720), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution3.setState(PAExecutionState.SCHEDULED);
        final List<PAExecution> expectedPAExecutions = new ArrayList<>(1);
        expectedPAExecutions.add(paExecution1);
        expectedPAExecutions.add(paExecution2);
        expectedPAExecutions.add(paExecution3);

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.SUCCEEDED);

        PAExecutionsScheduler.scheduleExisting(flmExecution, expectedPAExecutions);

        verify(activitySchedulerMock, times(3))
                .addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));
        verify(paExecutionDaoMock, times(3)).update(paExecutionArgumentCaptor.capture());

        final List<PAExecution> paExecutions = paExecutionArgumentCaptor.getAllValues();
        assertThat(paExecutions.get(0).getState()).isEqualTo(PAExecutionState.SCHEDULED);
        assertThat(paExecutions.get(1).getState()).isEqualTo(PAExecutionState.SCHEDULED);
        assertThat(paExecutions.get(2).getState()).isEqualTo(PAExecutionState.SCHEDULED);
    }

    @Test
    public void whenScheduleExistingCalledForFLMExecutionWith3PAExecutionOneInStartedStateButWithinTheThresholdAnd2InTheFuture_thenOnePAExecutionIsScheduledToRunImmediatelyOtherTwoAsNormalAndUpdatedAsScheduledInTheDB()
            throws Exception {

        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);

        final PAExecution paExecution1 = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution1.setState(PAExecutionState.STARTED);
        final PAExecution paExecution2 = new PAExecution(2, makeCronInTheFuture(360), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution2.setState(PAExecutionState.SCHEDULED);
        final PAExecution paExecution3 = new PAExecution(3, makeCronInTheFuture(720), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution3.setState(PAExecutionState.SCHEDULED);
        final List<PAExecution> expectedPAExecutions = new ArrayList<>(1);
        expectedPAExecutions.add(paExecution1);
        expectedPAExecutions.add(paExecution2);
        expectedPAExecutions.add(paExecution3);

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:05.00"));
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.SUCCEEDED);

        PAExecutionsScheduler.scheduleExisting(flmExecution, expectedPAExecutions);

        verify(activitySchedulerMock, times(1))
                .runActivity(any(PAExecutionActivity.class));
        verify(activitySchedulerMock, times(2))
                .addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));
        verify(paExecutionDaoMock, times(3)).update(paExecutionArgumentCaptor.capture());

        final List<PAExecution> paExecutions = paExecutionArgumentCaptor.getAllValues();
        assertThat(paExecutions.get(0).getState()).isEqualTo(PAExecutionState.SCHEDULED);
        assertThat(paExecutions.get(1).getState()).isEqualTo(PAExecutionState.SCHEDULED);
        assertThat(paExecutions.get(2).getState()).isEqualTo(PAExecutionState.SCHEDULED);
    }

    @Test
    public void whenScheduleExistingCalledForFLMExecutionWith3PAExecutionOneInStartedStateButNotWithinTheThresholdAnd2InTheFuture_thenOnePAExecutionIsNotScheduledToRunAndUpdatedAsMisfiredInTheDB()
            throws Exception {

        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);

        final PAExecution paExecution1 = new PAExecution(1, makeCronInThePast(180), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution1.setState(PAExecutionState.STARTED);
        final PAExecution paExecution2 = new PAExecution(2, makeCronInTheFuture(360), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution2.setState(PAExecutionState.SCHEDULED);
        final PAExecution paExecution3 = new PAExecution(3, makeCronInTheFuture(720), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution3.setState(PAExecutionState.SCHEDULED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecution1);
        paExecutions.add(paExecution2);
        paExecutions.add(paExecution3);

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.SUCCEEDED);

        PAExecutionsScheduler.scheduleExisting(flmExecution, paExecutions);

        verify(activitySchedulerMock, never())
                .runActivity(any(PAExecutionActivity.class));
        verify(activitySchedulerMock, times(2))
                .addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));
        verify(paExecutionDaoMock, times(3)).update(paExecutionArgumentCaptor.capture());

        for (final PAExecution pa : paExecutionArgumentCaptor.getAllValues()) {
            if (pa.getId().equals("flm_test_id_1")) {
                assertThat(pa.getState()).isEqualTo(PAExecutionState.MISFIRED);
            } else {
                assertThat(pa.getState()).isEqualTo(PAExecutionState.SCHEDULED);
            }
        }
    }

    @Test
    public void whenActivitySchedulerExceptionThrownWhenSchedulingMisfireAndRetriesAreNotExhausted_thenPAExecutionIsScheduledAndUpdatedAsScheduledInTheDB()
            throws Exception {

        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);
        doThrow(new ActivitySchedulerException("Test"))
                .doThrow(new ActivitySchedulerException("Test"))
                .doNothing()
                .when(activitySchedulerMock).runActivity(any(PAExecutionActivity.class));

        final PAExecution paExecution = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution.setState(PAExecutionState.STARTED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecution);

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.SUCCEEDED);

        PAExecutionsScheduler.scheduleExisting(flmExecution, paExecutions);

        verify(activitySchedulerMock, times(3))
                .runActivity(any(PAExecutionActivity.class));
        verify(activitySchedulerMock, never())
                .addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));
        verify(paExecutionDaoMock, times(1)).update(paExecutionArgumentCaptor.capture());
        assertThat(paExecutionArgumentCaptor.getAllValues().get(0).getState()).isEqualTo(PAExecutionState.SCHEDULED);
    }

    @Test
    public void whenActivitySchedulerExceptionThrownWhenSchedulingMisfireAndRetriesExhausted_thenPAExecutionIsNotScheduledAndUpdatedAsFailedInTheDB()
            throws Exception {

        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);
        doThrow(new ActivitySchedulerException("Test"))
                .doThrow(new ActivitySchedulerException("Test"))
                .doThrow(new ActivitySchedulerException("Test"))
                .when(activitySchedulerMock).runActivity(any(PAExecutionActivity.class));

        final PAExecution paExecution = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecution.setState(PAExecutionState.STARTED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecution);

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule("0 0 2 ? * * *");
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setEnablePA(true);
        flmExecution.setState(ExecutionState.SUCCEEDED);

        PAExecutionsScheduler.scheduleExisting(flmExecution, paExecutions);

        verify(activitySchedulerMock, times(3))
                .runActivity(any(PAExecutionActivity.class));
        verify(activitySchedulerMock, never())
                .addCronScheduleForActivity(any(PAExecutionActivity.class), any(CronSchedule.class));
        verify(paExecutionDaoMock, times(1)).update(paExecutionArgumentCaptor.capture());
        assertThat(paExecutionArgumentCaptor.getAllValues().get(0).getState()).isEqualTo(PAExecutionState.FAILED);
    }

    @Test
    public void whenPaExecutionIsScheduledAndActivityCancels_thenRemoveActivityAndPersistCancelledState() throws Exception {
        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);
        when(activitySchedulerMock.activityExists("PA_EXECUTION_ACTIVITY_PA_EXECUTION_SCHEDULE_flm_test_id_2")).thenReturn(true);
        doNothing()
                .doNothing()
                .when(activitySchedulerMock).removeActivity(anyString());

        final PAExecution paExecutionOne = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        final PAExecution paExecutionTwo = new PAExecution(2, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecutionOne.setState(PAExecutionState.SUCCEEDED);
        paExecutionTwo.setState(PAExecutionState.SCHEDULED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecutionOne);
        paExecutions.add(paExecutionTwo);

        PAExecutionsScheduler.cancelScheduledExecutions(paExecutions);

        verify(activitySchedulerMock, times(1)).activityExists(anyString());
        verify(activitySchedulerMock, times(1)).removeActivity(anyString());
        verify(paExecutionDaoMock, times(1)).update(paExecutionArgumentCaptor.capture());
        assertThat(paExecutionArgumentCaptor.getAllValues().get(0).getState()).isEqualTo(PAExecutionState.CANCELLED);
    }

    @Test
    public void whenPaExecutionsAreScheduledAndActivityCancels_thenRemoveActivityAndPersistCancelledState() throws Exception {
        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);
        when(activitySchedulerMock.activityExists(anyString())).thenReturn(true);
        doNothing()
                .doNothing()
                .when(activitySchedulerMock).removeActivity(anyString());

        final PAExecution paExecutionOne = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        final PAExecution paExecutionTwo = new PAExecution(2, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecutionOne.setState(PAExecutionState.SCHEDULED);
        paExecutionTwo.setState(PAExecutionState.SCHEDULED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecutionOne);
        paExecutions.add(paExecutionTwo);

        PAExecutionsScheduler.cancelScheduledExecutions(paExecutions);

        verify(activitySchedulerMock, times(2)).removeActivity(anyString());
        verify(paExecutionDaoMock, times(2)).update(paExecutionArgumentCaptor.capture());
        assertThat(paExecutionArgumentCaptor.getAllValues().get(0).getState()).isEqualTo(PAExecutionState.CANCELLED);
    }

    @Test
    public void whenActivitySchedulerExceptionThrownWhenCancelling_thenActivityIsNotRemovedOrPersisted() throws Exception {
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);
        when(activitySchedulerMock.activityExists(anyString())).thenReturn(true);
        doThrow(new ActivitySchedulerException("Test"))
                .when(activitySchedulerMock).removeActivity(anyString());

        final PAExecution paExecutionOne = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        final PAExecution paExecutionTwo = new PAExecution(2, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecutionOne.setState(PAExecutionState.SCHEDULED);
        paExecutionTwo.setState(PAExecutionState.SCHEDULED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecutionOne);
        paExecutions.add(paExecutionTwo);

        try {
            PAExecutionsScheduler.cancelScheduledExecutions(paExecutions);
            shouldHaveThrown(ActivitySchedulerException.class);
        } catch (final ActivitySchedulerException ignored) {
        }

        verify(activitySchedulerMock, times(1))
                .removeActivity(anyString());
        verify(paExecutionDaoMock, never()).update(paExecutionArgumentCaptor.capture());
    }

    @Test
    public void whenPaExecutionsAreStartedAndActivitySchedulerTerminates_thenPAExecutionActivityIsInterrupted() throws Exception {
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);
        when(activitySchedulerMock.activityExists(anyString())).thenReturn(true);
        doNothing().when(activitySchedulerMock).interruptActivity(anyString());

        final PAExecution paExecutionOne = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecutionOne.setState(PAExecutionState.STARTED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecutionOne);

        PAExecutionsScheduler.terminateRunningExecution(paExecutions);

        verify(activitySchedulerMock, times(1)).interruptActivity(anyString());
        verify(paExecutionDaoMock, never()).update(paExecutionArgumentCaptor.capture());
    }

    @Test
    public void whenPaExecutionsAreNotStartedAndActivitySchedulerTerminates_thenNothingHappens() throws Exception {
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);

        final PAExecution paExecutionOne = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecutionOne.setState(PAExecutionState.SCHEDULED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecutionOne);

        PAExecutionsScheduler.terminateRunningExecution(paExecutions);

        verify(activitySchedulerMock, never()).activityExists(anyString());
        verify(activitySchedulerMock, never()).interruptActivity(anyString());
        verify(paExecutionDaoMock, never()).update(paExecutionArgumentCaptor.capture());
    }

    @Test
    public void whenActivitySchedulerExceptionThrownWhenTerminating_thenPAExecutionActivityIsNotInterrupted() throws Exception {
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);
        when(activitySchedulerMock.activityExists(anyString())).thenReturn(true);
        doThrow(new ActivitySchedulerException("Test")).when(activitySchedulerMock).interruptActivity(anyString());

        final PAExecution paExecutionOne = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecutionOne.setState(PAExecutionState.STARTED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecutionOne);

        try {
            PAExecutionsScheduler.terminateRunningExecution(paExecutions);
            shouldHaveThrown(ActivitySchedulerException.class);
        } catch (final ActivitySchedulerException ignored) {
        }

        verify(activitySchedulerMock, times(1)).interruptActivity(anyString());
        verify(paExecutionDaoMock, never()).update(paExecutionArgumentCaptor.capture());
    }

    @Test
    public void whenActivitySchedulerExceptionThrownWhenTerminatingMisfireActivity_thenPAExecutionActivityIsNotInterrupted()
            throws Exception {
        PowerMockito.mockStatic(ActivityScheduler.class);
        when(ActivityScheduler.getInstance()).thenReturn(activitySchedulerMock);
        when(activitySchedulerMock.activityExists("PA_EXECUTION_ACTIVITY_PA_EXECUTION_SCHEDULE_MISFIRE_flm_test_id_1")).thenReturn(true);
        doThrow(new ActivitySchedulerException("Test")).when(activitySchedulerMock).interruptActivity("PA_EXECUTION_ACTIVITY_PA_EXECUTION_SCHEDULE_MISFIRE_flm_test_id_1");

        final PAExecution paExecutionOne = new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), FLM_EXECUTION_ID);
        paExecutionOne.setState(PAExecutionState.STARTED);
        final List<PAExecution> paExecutions = new ArrayList<>(1);
        paExecutions.add(paExecutionOne);

        try {
            PAExecutionsScheduler.terminateRunningExecution(paExecutions);
            shouldHaveThrown(ActivitySchedulerException.class);
        } catch (final ActivitySchedulerException ignored) {
        }

        verify(activitySchedulerMock, times(2)).activityExists(anyString());
        verify(activitySchedulerMock, times(1)).interruptActivity("PA_EXECUTION_ACTIVITY_PA_EXECUTION_SCHEDULE_MISFIRE_flm_test_id_1");
        verify(paExecutionDaoMock, never()).update(paExecutionArgumentCaptor.capture());
    }

    private String makeCronInTheFuture(final int numberOfMinutesInTheFuture) {
        final Instant instant = Instant.now().plus(numberOfMinutesInTheFuture, ChronoUnit.MINUTES);
        return getCronAsStringFromInstant(instant);
    }

    private String makeCronInThePast(final int numberOfMinutesInThePast) {
        final Instant instant = Instant.now().minus(numberOfMinutesInThePast, ChronoUnit.MINUTES);
        return getCronAsStringFromInstant(instant);
    }

    private String getCronAsStringFromInstant(final Instant instant) {
        final LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s",
                ldt.get(ChronoField.SECOND_OF_MINUTE),
                ldt.get(ChronoField.MINUTE_OF_HOUR),
                ldt.get(ChronoField.HOUR_OF_DAY),
                ldt.get(ChronoField.DAY_OF_MONTH),
                ldt.get(ChronoField.MONTH_OF_YEAR),
                "?",
                ldt.get(ChronoField.YEAR));
    }
}
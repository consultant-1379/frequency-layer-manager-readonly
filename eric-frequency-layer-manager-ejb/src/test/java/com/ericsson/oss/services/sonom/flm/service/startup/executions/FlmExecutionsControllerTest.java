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

package com.ericsson.oss.services.sonom.flm.service.startup.executions;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_1;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_2;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import com.ericsson.oss.services.sonom.common.scheduler.ActivityScheduler;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.InSequence;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.executions.ExecutionDbHandler;
import com.ericsson.oss.services.sonom.flm.scheduler.FlmAlgorithmExecutionScheduler;
import com.ericsson.oss.services.sonom.flm.service.api.FlmConfigurationService;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.startup.util.ExecutionBuilder;

/**
 * Unit tests for {@link FlmExecutionsController} class.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FlmExecutionsController.class })
public class FlmExecutionsControllerTest {

    private static final int MAX_NUMBER_OF_THREADS_TEST = 10;
    private static final String FLM_EXECUTION_ACTIVITY_NAME = "FLM_EXECUTION_ACTIVITY_FLM_EXECUTION_SCHEDULE_1";
    private static final String KPI_SERVICE_HOSTNAME = "localhost";
    private static final String KPI_SERVICE_PORT = "8080";
    private static final String CM_SERVICE_HOSTNAME = "localhost";
    private static final String EXECUTION_ID = "FLM_1600701252-162";
    private static final int CM_SERVICE_PORT = 8080;
    private static FlmExecutionsController objectUnderTest;

    @Mock
    private FlmConfigurationService flmConfigurationServiceBean;
    @Mock
    public ExecutionDao executionDao;
    @Mock
    private ResumedNonSettingsExecutionsRunner resumedNonSettingsExecutionsRunner;
    @Mock
    private ResumedExecutionsRunner resumedExecutionsRunner;

    @BeforeClass
    public static void setUp() {
        System.setProperty("KPI_SERVICE_HOSTNAME", KPI_SERVICE_HOSTNAME);
        System.setProperty("KPI_SERVICE_PORT", KPI_SERVICE_PORT);
        System.setProperty("CM_SERVICE_HOSTNAME", CM_SERVICE_HOSTNAME);
        System.setProperty("CM_SERVICE_PORT", String.valueOf(CM_SERVICE_PORT));

        objectUnderTest = new FlmExecutionsController();
    }

    @Before
    public void clear() throws ActivitySchedulerException {
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        activityScheduler.removeActivity(FLM_EXECUTION_ACTIVITY_NAME);
    }

    @Test
    @InSequence(1)
    public void whenFlmExecutionIsScheduled_thenTheActivityIsScheduled() throws NoSuchFieldException, SQLException, ActivitySchedulerException {
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("flmConfigurationServiceBean"),
                flmConfigurationServiceBean);
        final Configuration configuration = new Configuration();
        configuration.setId(1);
        configuration.setEnabled(true);
        configuration.setName("default");
        configuration.setSchedule("0 0 2 ? * * *");
        when(flmConfigurationServiceBean.getConfigurations()).thenReturn(Arrays.asList(configuration));
        objectUnderTest.scheduleFlmExecutionOnStartup();

        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        assertThat(activityScheduler.activityExists(FLM_EXECUTION_ACTIVITY_NAME)).isTrue();
    }

    @Test
    @InSequence(2)
    public void whenFlmExecutionIsScheduledButConfigurationIsDisabled_thenTheActivityIsNotScheduled()
            throws NoSuchFieldException, SQLException, ActivitySchedulerException {
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("flmConfigurationServiceBean"),
                flmConfigurationServiceBean);
        final Configuration configuration = new Configuration();
        configuration.setId(1);
        configuration.setEnabled(false);
        configuration.setName("default");
        configuration.setSchedule("0 0 2 ? * * *");
        when(flmConfigurationServiceBean.getConfigurations()).thenReturn(Arrays.asList(configuration));
        objectUnderTest.scheduleFlmExecutionOnStartup();

        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        assertThat(activityScheduler.activityExists(FLM_EXECUTION_ACTIVITY_NAME)).isFalse();
    }

    @Test
    @InSequence(3)
    public void whenFlmExecutionIsScheduledWithInvalidCronExpression_thenActivityIsNotScheduled()
            throws NoSuchFieldException, SQLException, ActivitySchedulerException {
        FlmAlgorithmExecutionScheduler.setSchedulerRetryParameters(1, 0);
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("flmConfigurationServiceBean"),
                flmConfigurationServiceBean);
        final Configuration configuration = new Configuration();
        configuration.setId(1);
        configuration.setEnabled(true);
        configuration.setName("default");
        configuration.setSchedule("0 0 2 ? * * /");
        when(flmConfigurationServiceBean.getConfigurations()).thenReturn(Arrays.asList(configuration));
        objectUnderTest.scheduleFlmExecutionOnStartup();

        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        assertThat(activityScheduler.activityExists(FLM_EXECUTION_ACTIVITY_NAME)).isFalse();
    }

    @Test
    @InSequence(4)
    public void whenResumeExecutionIsCalled_thenFlmAlgorithmExecutionIsTriggeredRequiredTimes() throws Exception {
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("nonSettingsExecutorService"),
                Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS_TEST));
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("executorService"),
                Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS_TEST));
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("executionDao"),
                executionDao);
        final List<Execution> executions = new ArrayList<>();

        executions.add(new ExecutionBuilder()
                .id(EXECUTION_ID)
                .configurationId(1)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.930"))
                .state(KPI_PROCESSING_GROUP_2)
                .retryAttempts(3)
                .build()); // excluded because outside max retry attempts
        final Execution firstIncludedExecution = new ExecutionBuilder()
                .id(EXECUTION_ID)
                .configurationId(1)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.922"))
                .state(KPI_PROCESSING_GROUP_2)
                .build();
        executions.add(firstIncludedExecution);
        executions.add(new ExecutionBuilder()
                .id(EXECUTION_ID + "1")
                .configurationId(1)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.866"))
                .state(KPI_PROCESSING_GROUP_1)
                .build()); // excluded because not latest start time for this configuration ID
        final Execution secondIncludedExecution = new ExecutionBuilder()
                .id(EXECUTION_ID + "4")
                .configurationId(2)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.923"))
                .state(KPI_PROCESSING_GROUP_2)
                .build();
        executions.add(secondIncludedExecution); // included because latest start time for this configuration ID
        executions.add(new ExecutionBuilder()
                .id(EXECUTION_ID + "2")
                .configurationId(2)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.87"))
                .state(KPI_PROCESSING_GROUP_1)
                .build()); // excluded because not latest start time for this configuration ID
        when(executionDao.getExecutionsInStates(any())).thenReturn(executions);
        whenNew(ResumedNonSettingsExecutionsRunner.class).withAnyArguments().thenReturn(resumedNonSettingsExecutionsRunner);
        whenNew(ResumedExecutionsRunner.class).withAnyArguments().thenReturn(resumedExecutionsRunner);
        objectUnderTest.resumeExecution();
        verifyNew(ResumedNonSettingsExecutionsRunner.class, times(2)).withArguments(any(Execution.class));
        verifyNew(ResumedExecutionsRunner.class, times(2)).withArguments(any(Execution.class), any(ExecutionDbHandler.class), anyBoolean());
    }

    @Test
    @InSequence(5)
    public void whenResumeExecutionIsCalledWithTwoSettingsAndTwoNonSettingsExecutions_thenNonSettingsExecutionsAreTriggeredTwiceAndSettingsExecutionsAreTriggeredFourTimes()
            throws Exception {
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("nonSettingsExecutorService"),
                Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS_TEST));
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("executorService"),
                Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS_TEST));
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("executionDao"),
                executionDao);

        final List<Execution> executions = new ArrayList<>();

        final Execution firstNonSettingsExecution = new ExecutionBuilder()
                .id(EXECUTION_ID)
                .configurationId(1)
                .state(KPI_PROCESSING_GROUP_2)
                .build();
        executions.add(firstNonSettingsExecution);
        final Execution secondNonSettingsExecution = new ExecutionBuilder()
                .id(EXECUTION_ID + "4")
                .configurationId(2)
                .state(KPI_PROCESSING_GROUP_2)
                .build();
        executions.add(secondNonSettingsExecution);
        executions.add(new ExecutionBuilder()
                .id(EXECUTION_ID + "2")
                .configurationId(3)
                .state(KPI_PROCESSING_GROUP_8)
                .build());
        executions.add(new ExecutionBuilder()
                .id(EXECUTION_ID + "1")
                .configurationId(4)
                .state(KPI_PROCESSING_GROUP_8)
                .build());
        when(executionDao.getExecutionsInStates(any())).thenReturn(executions);
        whenNew(ResumedNonSettingsExecutionsRunner.class).withAnyArguments().thenReturn(resumedNonSettingsExecutionsRunner);
        whenNew(ResumedExecutionsRunner.class).withAnyArguments().thenReturn(resumedExecutionsRunner);
        objectUnderTest.resumeExecution();
        verifyNew(ResumedNonSettingsExecutionsRunner.class, times(2)).withArguments(any(Execution.class));
        verifyNew(ResumedExecutionsRunner.class, times(4)).withArguments(any(Execution.class), any(ExecutionDbHandler.class), anyBoolean());
    }

    @Test
    @InSequence(6)
    public void whenInterruptedExceptionIsThrown_ThenAppropriateErrorMessageIsDisplayed() throws Exception {
        final ExecutorService executorService = spy(Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS_TEST));
        final Logger loggerMock = mock(Logger.class);
        Whitebox.setInternalState(FlmExecutionsController.class, "LOGGER", loggerMock);
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("executorService"),
                executorService);
        FieldSetter.setField(objectUnderTest, objectUnderTest.getClass().getDeclaredField("executionDao"),
                executionDao);
        final List<Execution> executions = new ArrayList<>();
        executions.add(new ExecutionBuilder()
                .id(EXECUTION_ID + "2")
                .configurationId(2)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.87"))
                .state(KPI_PROCESSING_GROUP_8)
                .build());
        when(executionDao.getExecutionsInStates(any())).thenReturn(executions);
        whenNew(ResumedExecutionsRunner.class).withAnyArguments().thenReturn(resumedExecutionsRunner);
        final InterruptedException exception = new InterruptedException("Test Interruption");
        doThrow(exception).when(executorService).invokeAll(any(Collection.class));
        objectUnderTest.resumeExecution();
        verify(executorService).invokeAll(any(Collection.class));
        verify(loggerMock).error("Resumed executions were interrupted", exception);

    }
}
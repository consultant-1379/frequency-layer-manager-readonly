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

package com.ericsson.oss.services.sonom.flm.service.startup.pa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionsScheduler;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

/**
 * Unit tests for {@link PAExecutionsResilienceController} class.
 */

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({ PAExecutionsScheduler.class, PAExecutionsResilienceController.class })
public class PAExecutionsResilienceControllerTest {
    private PAExecutionsResilienceController objectUnderTest;

    @Mock
    private ExecutionDaoImpl executionDaoMock;

    @Mock
    private PAExecutionDaoImpl paExecutionDaoMock;

    @Mock
    private PAExecution paExecutionMock11;

    @Mock
    private PAExecution paExecutionMock12;

    @Mock
    private PAExecution paExecutionMock21;

    @Mock
    private PAExecution paExecutionMock22;

    @Mock
    private Execution executionMock1;

    @Mock
    private Execution executionMock2;

    @Mock
    SQLException sqlExceptionMock;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(PAExecutionDaoImpl.class).withAnyArguments().thenReturn(paExecutionDaoMock);
        PowerMockito.whenNew(ExecutionDaoImpl.class).withAnyArguments().thenReturn(executionDaoMock);
        objectUnderTest = new PAExecutionsResilienceController();
    }

    @Test
    public void whenSchedulePAExecutionsCalled_thenReadPAExecutionsThatNeedToBeScheduledAgainFromTheDBAndScheduleThemAgainWithThePAExecutionsScheduler()
            throws Exception {

        final List<PAExecution> expectedPAExecutionsForFLM1 = new ArrayList<>();
        expectedPAExecutionsForFLM1.add(paExecutionMock11);
        expectedPAExecutionsForFLM1.add(paExecutionMock12);
        final List<PAExecution> expectedPAExecutionsForFLM2 = new ArrayList<>();
        expectedPAExecutionsForFLM2.add(paExecutionMock21);
        expectedPAExecutionsForFLM2.add(paExecutionMock22);
        final Map<String, List<PAExecution>> expectedPAExecutionsMapKeyedByFlmExecutionId = new HashMap<>();
        expectedPAExecutionsMapKeyedByFlmExecutionId.put("FLM1", expectedPAExecutionsForFLM1);
        expectedPAExecutionsMapKeyedByFlmExecutionId.put("FLM2", expectedPAExecutionsForFLM2);

        PowerMockito.mockStatic(PAExecutionsScheduler.class);
        doNothing().when(PAExecutionsScheduler.class);
        PAExecutionsScheduler.scheduleExisting(executionMock1, expectedPAExecutionsForFLM1);
        PAExecutionsScheduler.scheduleExisting(executionMock1, expectedPAExecutionsForFLM1);

        when(executionDaoMock.get("FLM1")).thenReturn(executionMock1);
        when(executionMock1.getId()).thenReturn("FLM1");
        when(executionDaoMock.get("FLM2")).thenReturn(executionMock2);
        when(executionMock2.getId()).thenReturn("FLM2");
        when(paExecutionDaoMock.getPAExecutionsInStates(PAExecutionState.STARTED, PAExecutionState.SCHEDULED))
                .thenReturn(expectedPAExecutionsMapKeyedByFlmExecutionId);

        objectUnderTest.schedulePAExecutions();

        verifyStatic(PAExecutionsScheduler.class, times(2));
        PAExecutionsScheduler.scheduleExisting(executionMock1, expectedPAExecutionsForFLM1);
        PAExecutionsScheduler.scheduleExisting(executionMock2, expectedPAExecutionsForFLM2);
    }

    @Test
    public void whenSQLExceptionThrownWhenReadingFLMExecution_thenTheExceptionIsAbsorbed()
            throws Exception {

        final List<PAExecution> expectedPAExecutionsForFLM1 = new ArrayList<>();
        expectedPAExecutionsForFLM1.add(paExecutionMock11);
        expectedPAExecutionsForFLM1.add(paExecutionMock12);

        final Map<String, List<PAExecution>> expectedPAExecutionsMapKeyedByFlmExecutionId = new HashMap<>();
        expectedPAExecutionsMapKeyedByFlmExecutionId.put("FLM1", expectedPAExecutionsForFLM1);

        PowerMockito.mockStatic(PAExecutionsScheduler.class);

        when(paExecutionDaoMock.getPAExecutionsInStates(PAExecutionState.STARTED, PAExecutionState.SCHEDULED))
                .thenReturn(expectedPAExecutionsMapKeyedByFlmExecutionId);

        when(executionDaoMock.get("FLM1")).thenThrow(sqlExceptionMock);

        objectUnderTest.schedulePAExecutions();

        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.scheduleExisting(any(), any());

    }

    @Test
    public void whenSQLExceptionThrownWhenReadingThePAExecutions_thenTheExceptionIsAbsorbed()
            throws Exception {

        PowerMockito.mockStatic(PAExecutionsScheduler.class);

        when(paExecutionDaoMock.getPAExecutionsInStates(PAExecutionState.STARTED, PAExecutionState.SCHEDULED))
                .thenThrow(sqlExceptionMock);

        objectUnderTest.schedulePAExecutions();

        verifyStatic(PAExecutionsScheduler.class, never());
        PAExecutionsScheduler.scheduleExisting(any(), any());

    }
}
/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
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

import static com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionActivity.FLM_EXECUTION_CONTEXT;
import static com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionActivity.PA_EXECUTION_CONTEXT;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
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

import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * Unit tests for {@link PAExecutionActivity} class.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({ PAExecutionActivity.class })
public class PAExecutionActivityTest {
    private PAExecutionActivity objectUnderTest;

    @Mock
    PAExecution paExecutionMock;

    @Mock
    Execution flmExecutionMock;

    @Mock
    PAExecutionExecutor paExecutionExecutorMock;

    private Map<String, Object> context;

    @Before
    public void setUp() {
        context = new HashMap<>(2);
        context.put(PA_EXECUTION_CONTEXT, paExecutionMock);
        context.put(FLM_EXECUTION_CONTEXT, flmExecutionMock);
        objectUnderTest = new PAExecutionActivity("test_activity_name", context);
    }

    @Test
    public void whenTheActivityIsFired_thenPAExecutionExecutorIsCalledWithPAExecutionAndFlmExecutionInformationFromTheActivityContext()
            throws Exception {

        PowerMockito.whenNew(PAExecutionExecutor.class).withArguments(paExecutionMock, flmExecutionMock).thenReturn(paExecutionExecutorMock);

        objectUnderTest.run(context);

        verify(paExecutionExecutorMock, times(1)).executeActivity();
    }
}
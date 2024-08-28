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

package com.ericsson.oss.services.sonom.flm.pa.executor;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.pa.kpi.PAKpiCalculationExecutor;
import com.ericsson.oss.services.sonom.flm.pa.policy.PAPolicyExecutor;
import com.ericsson.oss.services.sonom.flm.pa.reversion.PAReversionExecutor;
import com.ericsson.oss.services.sonom.flm.policy.PolicyDeployer;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

/**
 * Unit tests for {@link PAExecutionExecutor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PAExecutionExecutorTest {

    private static final String CM_SERVICE_HOSTNAME_ENV_PROPERTY = "CM_SERVICE_HOSTNAME";
    private static final String CM_SERVICE_HOSTNAME = "localhost";
    private static final String CM_SERVICE_PORT_ENV_PROPERTY = "CM_SERVICE_PORT";
    private static final String PA_EXECUTION_TEST_ID = "pa_execution_test_id";
    private static final int CM_SERVICE_PORT = 8080;

    private PAExecutionExecutor objectUnderTest;

    @Mock
    PAExecution paExecutionMock;

    @Mock
    PAExecutionDao paExecutionDaoMock;

    @Mock
    PolicyDeployer policyDeployerMock;

    @Mock
    PAKpiCalculationExecutor paKpiCalculationExecutorMock;

    @Mock
    PAPolicyExecutor paPolicyExecutorMock;

    @Mock
    private PAReversionExecutor paReversionExecutorMock;

    @BeforeClass
    public static void before() {
        System.setProperty("KPI_SERVICE_HOSTNAME", "KPI_SERVICE_HOSTNAME");
        System.setProperty("KPI_SERVICE_PORT", "8080");
        System.setProperty("POLICY_REST_USER", "test_user");
        System.setProperty("POLICY_REST_PASSWORD", "test_password");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_HOST", "test_host");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_PORT", "0000");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_HOST", "test_host");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_PORT", "0000");
    }

    @AfterClass
    public static void after() {
        System.clearProperty("KPI_SERVICE_HOSTNAME");
        System.clearProperty("KPI_SERVICE_PORT");
        System.clearProperty("POLICY_REST_USER");
        System.clearProperty("POLICY_REST_PASSWORD");
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_HOST");
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_PORT");
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_HOST");
        System.clearProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_PORT");
    }

    @Before
    public void setUp() {
        System.setProperty(CM_SERVICE_HOSTNAME_ENV_PROPERTY, CM_SERVICE_HOSTNAME);
        System.setProperty(CM_SERVICE_PORT_ENV_PROPERTY, String.valueOf(CM_SERVICE_PORT));

        objectUnderTest = new PAExecutionExecutor()
                .withPaExecution(paExecutionMock)
                .withPolicyDeployer(policyDeployerMock)
                .withPAKpiCalculationExecutor(paKpiCalculationExecutorMock)
                .withPAExecutionDao(paExecutionDaoMock)
                .withPAPolicyExecutor(paPolicyExecutorMock)
                .withPAReversionExecutor(paReversionExecutorMock)
                .withPAExecutionLatch(new PAExecutionLatch());
    }

    @Test
    public void whenExecutingPA_thenAllPAStagesAreExecutedInTheExpectedOrder() throws Exception {
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_TEST_ID);

        objectUnderTest.executeActivity();

        verify(policyDeployerMock, times(1)).deployPolicy("onap.policies.apex.FlmPa", "policy/DeployFlmPaPolicyPayload.json");

        final InOrder inOrder = inOrder(paKpiCalculationExecutorMock, paPolicyExecutorMock, paReversionExecutorMock);
        inOrder.verify(paKpiCalculationExecutorMock).execute();
        inOrder.verify(paPolicyExecutorMock).execute();
        inOrder.verify(paReversionExecutorMock).execute();

        final InOrder inOrderStateUpdates = inOrder(paExecutionMock);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.STARTED);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.SUCCEEDED);

        //Just called in Logging
        verify(paExecutionMock, times(6)).getId();
        verify(paExecutionDaoMock, times(2)).update(paExecutionMock);
    }

    @Test
    public void whenAnyPAStageThrowsPAExecutionException_thenPAExecutionStateIsUpdatedToFailed() throws Exception {
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_TEST_ID);
        when(paKpiCalculationExecutorMock.execute()).thenThrow(PAExecutionException.class);

        objectUnderTest.executeActivity();

        final InOrder inOrder = inOrder(paKpiCalculationExecutorMock);
        inOrder.verify(paKpiCalculationExecutorMock).execute();

        final InOrder inOrderStateUpdates = inOrder(paExecutionMock);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.STARTED);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.FAILED);

        //Just called in Logging
        verify(paExecutionMock, times(6)).getId();
        verify(paExecutionDaoMock, times(2)).update(paExecutionMock);
    }

    @Test
    public void whenRetrieverExecutorThrowsPAExecutionException_thenPAExecutionStateIsUpdatedToFailed() throws Exception {
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_TEST_ID);
        when(paPolicyExecutorMock.execute()).thenThrow(PAExecutionException.class);

        objectUnderTest.executeActivity();

        final InOrder inOrder = inOrder(paKpiCalculationExecutorMock, paPolicyExecutorMock);
        inOrder.verify(paKpiCalculationExecutorMock).execute();
        inOrder.verify(paPolicyExecutorMock).execute();

        final InOrder inOrderStateUpdates = inOrder(paExecutionMock);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.STARTED);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.FAILED);
    }

    @Test
    public void whenReversionExecutorThrowsPAExecutionException_thenPAExecutionStateIsUpdatedToFailed() throws Exception {
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_TEST_ID);
        when(paReversionExecutorMock.execute()).thenThrow(PAExecutionException.class);

        objectUnderTest.executeActivity();

        final InOrder inOrder = inOrder(paKpiCalculationExecutorMock, paPolicyExecutorMock, paReversionExecutorMock);
        inOrder.verify(paKpiCalculationExecutorMock).execute();
        inOrder.verify(paPolicyExecutorMock).execute();
        inOrder.verify(paReversionExecutorMock).execute();

        final InOrder inOrderStateUpdates = inOrder(paExecutionMock);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.STARTED);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.FAILED);
    }

    @Test
    public void whenInterruptSignalIsCalledDuringKpiCalculatorExecutor_thenPAExecutionStateIsUpdatedToTerminated() throws Exception {
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_TEST_ID);

        doAnswer(invocationOnMock -> {
            objectUnderTest.terminateActivity();
            return null;
        }).when(paKpiCalculationExecutorMock).execute();

        objectUnderTest.executeActivity();

        final InOrder inOrder = inOrder(paKpiCalculationExecutorMock, paPolicyExecutorMock, paReversionExecutorMock);
        inOrder.verify(paKpiCalculationExecutorMock).execute();
        inOrder.verifyNoMoreInteractions();

        final InOrder inOrderStateUpdates = inOrder(paExecutionMock);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.STARTED);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATING);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATED);
    }

    @Test
    public void whenInterruptSignalIsCalledDuringKpiCalculatorExecutor_andThrowsPaExecutionInterruptedException_thenPAExecutionStateIsUpdatedToTerminated()
            throws Exception {
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_TEST_ID);

        doAnswer(invocationOnMock -> {
            objectUnderTest.terminateActivity();
            throw new PAExecutionInterruptedException("");
        }).when(paKpiCalculationExecutorMock).execute();

        objectUnderTest.executeActivity();

        final InOrder inOrder = inOrder(paKpiCalculationExecutorMock, paPolicyExecutorMock, paReversionExecutorMock);
        inOrder.verify(paKpiCalculationExecutorMock).execute();
        inOrder.verifyNoMoreInteractions();

        final InOrder inOrderStateUpdates = inOrder(paExecutionMock);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.STARTED);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATING);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATED);
    }

    @Test
    public void whenInterruptSignalIsCalledDuringPolicyExecutor_thenPAExecutionStateIsUpdatedToTerminated() throws Exception {
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_TEST_ID);

        doAnswer(invocationOnMock -> {
            objectUnderTest.terminateActivity();
            return null;
        }).when(paPolicyExecutorMock).execute();

        objectUnderTest.executeActivity();

        final InOrder inOrder = inOrder(paKpiCalculationExecutorMock, paPolicyExecutorMock, paReversionExecutorMock);
        inOrder.verify(paKpiCalculationExecutorMock).execute();
        inOrder.verify(paPolicyExecutorMock).execute();
        inOrder.verifyNoMoreInteractions();

        final InOrder inOrderStateUpdates = inOrder(paExecutionMock);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.STARTED);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATING);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATED);
    }

    @Test
    public void whenInterruptSignalIsCalledDuringPolicyExecutor_andThrowsPaExecutionInterruptedException_thenPAExecutionStateIsUpdatedToTerminated()
            throws Exception {
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_TEST_ID);

        doAnswer(invocationOnMock -> {
            objectUnderTest.terminateActivity();
            throw new PAExecutionInterruptedException("");
        }).when(paPolicyExecutorMock).execute();

        objectUnderTest.executeActivity();

        final InOrder inOrder = inOrder(paKpiCalculationExecutorMock, paPolicyExecutorMock, paReversionExecutorMock);
        inOrder.verify(paKpiCalculationExecutorMock).execute();
        inOrder.verify(paPolicyExecutorMock).execute();
        inOrder.verifyNoMoreInteractions();

        final InOrder inOrderStateUpdates = inOrder(paExecutionMock);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.STARTED);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATING);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATED);
    }

    @Test
    public void whenInterruptSignalIsCalledDuringReversionExecutor_andThrowsPaExecutionInterruptedException_thenPAExecutionStateIsUpdatedToTerminated()
            throws Exception {
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_TEST_ID);

        doAnswer(invocationOnMock -> {
            objectUnderTest.terminateActivity();
            throw new PAExecutionInterruptedException("");
        }).when(paReversionExecutorMock).execute();

        objectUnderTest.executeActivity();

        final InOrder inOrder = inOrder(paKpiCalculationExecutorMock, paPolicyExecutorMock, paReversionExecutorMock);
        inOrder.verify(paKpiCalculationExecutorMock).execute();
        inOrder.verify(paPolicyExecutorMock).execute();
        inOrder.verify(paReversionExecutorMock).execute();
        inOrder.verifyNoMoreInteractions();

        final InOrder inOrderStateUpdates = inOrder(paExecutionMock);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.STARTED);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATING);
        inOrderStateUpdates.verify(paExecutionMock).setState(PAExecutionState.TERMINATED);
    }
}
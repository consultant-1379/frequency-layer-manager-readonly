/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.kpi;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.FAILED;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_1;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_10;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_11;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_12;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_13;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_14;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_15;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_16;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_2;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_3;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_4;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_5;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_6;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_7;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_8;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_9;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_SUCCEEDED;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SETTINGS_PROCESSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationRequest.KpiCalculationRequestCreator;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;
import com.ericsson.oss.services.sonom.flm.settings.FlmExecutionHandler;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationStateHandler;

/**
 * Unit tests for {@link KpiCalculationExecutor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class KpiCalculationExecutorTest {

    private static final Timestamp EXECUTION_TIME_STAMP = Timestamp.valueOf("2020-05-07 10:53:15.930");
    private static final Map<String, String> stubbedGlobalSettings = new HashMap<>();
    private static final int TWICE = 2;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Mock
    private KpiRequestRetry kpiRequestRetryMock;
    @Mock
    private Execution executionMock;
    @Mock
    private PersistenceHandler persistenceHandlerMock;
    @Mock
    private RequestProcessor requestProcessorMock;
    @Mock
    private KpiCalculationStateHandler kpiCalculationStateHandlerMock;
    @Mock
    private MetricHelper flmMetricHelperMock;
    @Mock
    private FlmExecutionHandler flmExecutionHandlerMock;
    @Mock
    private CmStore cmStore;
    @Mock
    private SectorReferenceCellHandler sectorReferenceCellHandler;
    @InjectMocks
    private KpiCalculationExecutor objectUnderTestMock;

    static {
        //Must be set so that Mockito can create the mock for KpiCalculationStateHandler
        System.setProperty("KPI_SERVICE_HOSTNAME", "KPI_SERVICE_HOSTNAME");
        System.setProperty("KPI_SERVICE_PORT", "8080");
        System.setProperty("CM_SERVICE_HOSTNAME", "localhost");
        System.setProperty("CM_SERVICE_PORT", "8080");

        stubbedGlobalSettings.put("stubbedGlobalSetting", "stub");
    }

    @Test
    public void whenResumingFailedKpiCalculation_thenFlmExceptionThrownAndAlgorithmFails() throws FlmAlgorithmException {
        objectUnderTestMock.setKpiCalculationStateHandler(kpiCalculationStateHandlerMock);
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());
        final String calculationId = "1";

        when(executionMock.getCalculationId()).thenReturn(calculationId);
        when(kpiRequestRetryMock.getKpiCalculationState(kpiCalculationStateHandlerMock, calculationId))
                .thenThrow(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR));
        when(kpiRequestRetryMock.getKpiRecalculationRequiredExceptionPredicate()).thenReturn(throwable -> false); // Algorithm failed;

        thrown.expectCause(instanceOf(FlmAlgorithmException.class));
        thrown.expectMessage(FlmServiceExceptionCode.KPI_CALCULATION_ERROR.getErrorMessage());
        objectUnderTestMock.settingsBasedExecute(KPI_PROCESSING_GROUP_2, true, true, resumeExecutionDate);
        // cannot verify order after exception is thrown
    }

    @Test
    public void whenNewSettingsBasedKpiCalculation_thenCalculationsExecuteInOrder() throws Exception {
        objectUnderTestMock.setKpiCalculationStateHandler(kpiCalculationStateHandlerMock);
        final String executionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());

        when(executionMock.getState()).thenReturn(KPI_PROCESSING_GROUP_8,
                KPI_PROCESSING_GROUP_9, KPI_PROCESSING_GROUP_10, KPI_PROCESSING_GROUP_11, KPI_PROCESSING_GROUP_12,
                KPI_PROCESSING_GROUP_13, KPI_PROCESSING_GROUP_14, KPI_PROCESSING_GROUP_15, KPI_PROCESSING_GROUP_16, KPI_PROCESSING_SUCCEEDED);

        when(executionMock.getCustomizedGlobalSettings()).thenReturn(stubbedGlobalSettings);

        final String executionId = "executionId";
        when(executionMock.getId()).thenReturn(executionId);
        when(executionMock.getState()).thenReturn(KPI_PROCESSING_GROUP_8);
        objectUnderTestMock.settingsBasedExecute(KPI_PROCESSING_GROUP_8, false, true, executionDate);

        final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor = ArgumentCaptor.forClass(KpiCalculationRequest.class);
        final GroupAsserter groupAsserter = new GroupAsserter(kpiCalculationRequestArgumentCaptor, executionId);

        final InOrder inOrder = inOrder(executionMock, requestProcessorMock, persistenceHandlerMock, flmMetricHelperMock);

        inOrder.verify(executionMock).setState(KPI_PROCESSING_GROUP_8);

        inOrder.verify(executionMock, times(TWICE)).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_8(false);

        inOrder.verify(executionMock, times(TWICE)).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_9(false);

        inOrder.verify(executionMock, times(TWICE)).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_10(false);

        inOrder.verify(executionMock, times(TWICE)).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_11(false);

        inOrder.verify(executionMock, times(TWICE)).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_12(false);

        inOrder.verify(executionMock, times(TWICE)).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_13(false);

        inOrder.verify(executionMock, times(TWICE)).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_14(false);

        inOrder.verify(executionMock, times(TWICE)).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_15(false);

        inOrder.verify(executionMock, times(TWICE)).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_16(false);

        inOrder.verify(persistenceHandlerMock).persistExecutionStatus(KPI_PROCESSING_SUCCEEDED, false);
        inOrder.verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        inOrder.verify(flmMetricHelperMock).incrementFlmMetric(any(FlmMetric.class), anyLong());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void whenResumingKpiCalculationFromAnUnrecognisedState_thenDoNothing() throws FlmAlgorithmException, SQLException {
        final Execution realExecution = new Execution();
        final ExecutionState executionState = FAILED;

        realExecution.setId("FLM_1600701252");
        realExecution.setState(executionState);
        realExecution.setCustomizedGlobalSettings(stubbedGlobalSettings);
        final KpiCalculationExecutor kpiCalculationExecutor = new KpiCalculationExecutor(cmStore, realExecution, flmMetricHelperMock,
                new PersistenceHandler(realExecution, new ExecutionDaoImpl(3, 1)), flmExecutionHandlerMock);
        when(flmExecutionHandlerMock.getAllExecutionSummaries()).thenReturn(Collections.EMPTY_LIST);

        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());
        kpiCalculationExecutor.settingsBasedExecute(executionState, false, true, resumeExecutionDate);

        verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        verify(flmMetricHelperMock).incrementFlmMetric(any(FlmMetric.class), anyLong());
        verifyNoMoreInteractions(flmMetricHelperMock);
    }

    @Test
    public void whenPersistingAnExecutionFails_thenAnFlmAlgorithmExceptionIsThrown() throws FlmAlgorithmException {
        final Execution realExecution = new Execution();
        final ExecutionState executionState = KPI_PROCESSING_SUCCEEDED;

        realExecution.setId("FLM_1600701252");
        realExecution.setState(executionState);
        final KpiCalculationExecutor kpiCalculationExecutor = new KpiCalculationExecutor(cmStore, realExecution, flmMetricHelperMock,
                new PersistenceHandler(realExecution, new ExecutionDaoImpl(3, 1)));

        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());
        thrown.expect(FlmAlgorithmException.class);
        kpiCalculationExecutor.settingsBasedExecute(executionState, false, true, resumeExecutionDate);
    }

    @Test
    public void whenResumingSettingsBasedKpiCalculation_thenCalculationsContinueFromNextState() throws Exception {
        //i.e. goes from KPI_PROCESSING_GROUP_8 -> KPI_PROCESSING_GROUP_9 in determine state method and resumes from KPI_PROCESSING_GROUP_9
        final ExecutionState executionState = KPI_PROCESSING_GROUP_8;

        final Execution resumedExecutionSpy = spy(new Execution()); // used to test lost/not lost resumes from correct state
        resumedExecutionSpy.setState(executionState);
        final String calculationId = "1";
        resumedExecutionSpy.setCalculationId(calculationId);

        //re-instantiate with real execution object, other mocks remain the same.
        final KpiCalculationExecutor kpiCalculationExecutorWithRealExecution = new KpiCalculationExecutor(resumedExecutionSpy,
                flmMetricHelperMock, requestProcessorMock, persistenceHandlerMock,
                kpiRequestRetryMock, sectorReferenceCellHandler, flmExecutionHandlerMock);
        kpiCalculationExecutorWithRealExecution.setKpiCalculationStateHandler(kpiCalculationStateHandlerMock);
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());
        resumedExecutionSpy.setCustomizedGlobalSettings(stubbedGlobalSettings);
        final String executionId = "executionId";
        resumedExecutionSpy.setId(executionId);

        kpiCalculationExecutorWithRealExecution.settingsBasedExecute(executionState, true, true, resumeExecutionDate);

        final InOrder inOrder = inOrder(kpiRequestRetryMock, resumedExecutionSpy, requestProcessorMock, persistenceHandlerMock,
                flmMetricHelperMock);

        final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor = ArgumentCaptor.forClass(KpiCalculationRequest.class);
        final GroupAsserter groupAsserter = new GroupAsserter(kpiCalculationRequestArgumentCaptor, executionId);

        inOrder.verify(kpiRequestRetryMock).getKpiCalculationState(kpiCalculationStateHandlerMock, calculationId);
        inOrder.verify(resumedExecutionSpy).setState(KPI_PROCESSING_GROUP_9);
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_9(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_10(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_11(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_12(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_13(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_14(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_15(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_16(true);

        inOrder.verify(persistenceHandlerMock).persistExecutionStatus(KPI_PROCESSING_SUCCEEDED, true);
        inOrder.verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        inOrder.verify(flmMetricHelperMock).incrementFlmMetric(eq(FlmMetric.FLM_KPI_CALCULATION_TIME_IN_MILLIS), anyLong());
    }

    @Test
    public void whenResumingLostKpiCalculation_thenCalculationsContinuesFromTheLostKpiCalculationState() throws Exception {
        //i.e. stays in KPI_PROCESSING_GROUP8 in determine state method, detected as LOST and resumes from KPI_PROCESSING_GROUP_8

        final Execution resumedExecutionSpy = spy(new Execution()); // used to test lost/not lost resumes from correct state
        final ExecutionState executionState = KPI_PROCESSING_GROUP_8;
        resumedExecutionSpy.setState(executionState);
        final String calculationId = "1";
        resumedExecutionSpy.setCalculationId(calculationId);
        resumedExecutionSpy.setCustomizedGlobalSettings(stubbedGlobalSettings);
        final String executionId = "executionId";
        resumedExecutionSpy.setId(executionId);

        final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor = ArgumentCaptor.forClass(KpiCalculationRequest.class);

        final GroupAsserter groupAsserter = new GroupAsserter(kpiCalculationRequestArgumentCaptor, executionId);

        //re-instantiate with real execution object, other mocks remain the same.
        final KpiCalculationExecutor kpiCalculationExecutorWithRealExecution = new KpiCalculationExecutor(resumedExecutionSpy,
                flmMetricHelperMock, requestProcessorMock, persistenceHandlerMock,
                kpiRequestRetryMock, sectorReferenceCellHandler, flmExecutionHandlerMock);
        kpiCalculationExecutorWithRealExecution.setKpiCalculationStateHandler(kpiCalculationStateHandlerMock);
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());

        when(kpiRequestRetryMock.getKpiCalculationState(kpiCalculationStateHandlerMock, calculationId))
                .thenThrow(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_LOST));
        when(kpiRequestRetryMock.getKpiRecalculationRequiredExceptionPredicate()).thenReturn(throwable -> true); // verify recalculation current state
        kpiCalculationExecutorWithRealExecution.settingsBasedExecute(executionState, true, true, resumeExecutionDate);

        final InOrder inOrder = inOrder(kpiRequestRetryMock, requestProcessorMock, persistenceHandlerMock, resumedExecutionSpy,
                flmMetricHelperMock);

        inOrder.verify(kpiRequestRetryMock).getKpiCalculationState(kpiCalculationStateHandlerMock, calculationId);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_8(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_9(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_10(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_11(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_12(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_13(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_14(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_15(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_16(true);

        inOrder.verify(persistenceHandlerMock).persistExecutionStatus(KPI_PROCESSING_SUCCEEDED, true);
        inOrder.verify(resumedExecutionSpy).setState(KPI_PROCESSING_GROUP_9);
        inOrder.verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        inOrder.verify(flmMetricHelperMock).incrementFlmMetric(eq(FlmMetric.FLM_KPI_CALCULATION_TIME_IN_MILLIS), anyLong());
    }

    @Test
    public void whenResumingFailedNonSettingsBasedKpiCalculation_thenFlmExceptionThrownAndAlgorithmFails()
            throws FlmAlgorithmException {
        objectUnderTestMock.setKpiCalculationStateHandler(kpiCalculationStateHandlerMock);
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());
        final String calculationId = "1";

        when(executionMock.getCalculationId()).thenReturn(calculationId);
        when(kpiRequestRetryMock.getKpiCalculationState(kpiCalculationStateHandlerMock, calculationId))
                .thenThrow(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_ERROR));
        when(kpiRequestRetryMock.getKpiRecalculationRequiredExceptionPredicate()).thenReturn(throwable -> false); // Algorithm failed;

        thrown.expectCause(instanceOf(FlmAlgorithmException.class));
        thrown.expectMessage(FlmServiceExceptionCode.KPI_CALCULATION_ERROR.getErrorMessage());
        objectUnderTestMock.nonSettingsBasedExecute(KPI_PROCESSING_GROUP_2, true, resumeExecutionDate);
        // cannot verify order after exception is thrown
    }

    @Test
    public void whenStartingSettingsBasedCalculationsInNonSettingsBasedState_thenFlmExceptionThrownAndAlgorithmFails() throws FlmAlgorithmException {
        objectUnderTestMock.setKpiCalculationStateHandler(kpiCalculationStateHandlerMock);
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());
        when(executionMock.getState()).thenReturn(KPI_PROCESSING_GROUP_2);
        thrown.expectCause(instanceOf(FlmAlgorithmException.class));
        thrown.expectMessage(FlmServiceExceptionCode.ALGORITHM_FAILURE_ERROR.getErrorMessage());
        objectUnderTestMock.settingsBasedExecute(KPI_PROCESSING_GROUP_2, false, true, resumeExecutionDate);
    }

    @Test
    public void whenNewNonSettingsBasedKpiCalculation_thenCalculationsExecuteInOrder() throws Exception {
        objectUnderTestMock.setKpiCalculationStateHandler(kpiCalculationStateHandlerMock);
        final String executionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());

        when(executionMock.getState()).thenReturn(KPI_PROCESSING_GROUP_1,
                KPI_PROCESSING_GROUP_2, KPI_PROCESSING_GROUP_3, KPI_PROCESSING_GROUP_4, KPI_PROCESSING_GROUP_5,
                KPI_PROCESSING_GROUP_6, KPI_PROCESSING_GROUP_7);

        when(sectorReferenceCellHandler.getSectorIdsRequiringReferenceCellRecalculation()).thenReturn(Collections.emptySet());

        final String executionId = "executionId";
        when(executionMock.getId()).thenReturn(executionId);

        when(executionMock.getState()).thenReturn(KPI_PROCESSING_GROUP_1);
        objectUnderTestMock.nonSettingsBasedExecute(KPI_PROCESSING_GROUP_1, false, executionDate);

        final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor = ArgumentCaptor.forClass(KpiCalculationRequest.class);
        final KpiCalculationExecutorTest.GroupAsserter groupAsserter = new KpiCalculationExecutorTest.GroupAsserter(
                kpiCalculationRequestArgumentCaptor, executionId);

        final InOrder inOrder = inOrder(executionMock, requestProcessorMock, persistenceHandlerMock, flmMetricHelperMock,
                sectorReferenceCellHandler);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_1(false);

        inOrder.verify(sectorReferenceCellHandler).getSectorIdsRequiringReferenceCellRecalculation();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_2(false);

        inOrder.verify(executionMock).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_3(false);

        inOrder.verify(executionMock).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_4(false);

        inOrder.verify(executionMock).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_5(false);

        inOrder.verify(executionMock).getId();
        inOrder.verify(sectorReferenceCellHandler).getSectorIdsRequiringReferenceCellRecalculation();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_6(false);

        inOrder.verify(executionMock).getId();
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_7(false);

        inOrder.verify(persistenceHandlerMock).persistExecutionStatus(SETTINGS_PROCESSING, false);
        inOrder.verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        inOrder.verify(flmMetricHelperMock).incrementFlmMetric(any(FlmMetric.class), anyLong());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void whenResumingDKpiCalculationFromAnUnrecognisedState_thenDoNothing() throws FlmAlgorithmException, SQLException, IOException {
        final Execution realExecution = new Execution();
        final ExecutionState executionState = FAILED;
        realExecution.setId("FLM_1600701252");
        realExecution.setState(executionState);
        realExecution.setCustomizedGlobalSettings(stubbedGlobalSettings);
        final KpiCalculationExecutor kpiCalculationExecutor = new KpiCalculationExecutor(cmStore, realExecution, flmMetricHelperMock,
                new PersistenceHandler(realExecution, new ExecutionDaoImpl(3, 1)));

        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());
        kpiCalculationExecutor.nonSettingsBasedExecute(executionState, false, resumeExecutionDate);

        verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        verify(flmMetricHelperMock).incrementFlmMetric(any(FlmMetric.class), anyLong());
        verifyNoMoreInteractions(flmMetricHelperMock);
    }

    @Test
    public void whenResumingNonSettingsBasedKpiCalculation_thenCalculationsContinueFromNextState() throws Exception {
        //i.e. goes from KPI_PROCESSING_GROUP_2 -> KPI_PROCESSING_GROUP_3 in determine state method and resumes from KPI_PROCESSING_GROUP_3
        final ExecutionState executionState = KPI_PROCESSING_GROUP_2;

        final Execution resumedExecutionSpy = spy(new Execution()); // used to test lost/not lost resumes from correct state
        resumedExecutionSpy.setState(executionState);
        final String calculationId = "1";
        resumedExecutionSpy.setCalculationId(calculationId);

        //re-instantiate with real execution object, other mocks remain the same.
        final KpiCalculationExecutor kpiCalculationExecutorWithRealExecution = new KpiCalculationExecutor(resumedExecutionSpy,
                flmMetricHelperMock, requestProcessorMock, persistenceHandlerMock,
                kpiRequestRetryMock, sectorReferenceCellHandler, flmExecutionHandlerMock);
        kpiCalculationExecutorWithRealExecution.setKpiCalculationStateHandler(kpiCalculationStateHandlerMock);
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());
        resumedExecutionSpy.setCustomizedGlobalSettings(stubbedGlobalSettings);
        final String executionId = "executionId";
        resumedExecutionSpy.setId(executionId);

        when(sectorReferenceCellHandler.getSectorIdsRequiringReferenceCellRecalculation()).thenReturn(Collections.emptySet());
        kpiCalculationExecutorWithRealExecution.nonSettingsBasedExecute(executionState, true, resumeExecutionDate);

        final InOrder inOrder = inOrder(kpiRequestRetryMock, resumedExecutionSpy, requestProcessorMock, persistenceHandlerMock,
                flmMetricHelperMock);

        final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor = ArgumentCaptor.forClass(KpiCalculationRequest.class);
        final KpiCalculationExecutorTest.GroupAsserter groupAsserter = new KpiCalculationExecutorTest.GroupAsserter(
                kpiCalculationRequestArgumentCaptor, executionId);

        inOrder.verify(kpiRequestRetryMock).getKpiCalculationState(kpiCalculationStateHandlerMock, calculationId);
        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_3(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_4(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_5(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_6(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_7(true);

        inOrder.verify(persistenceHandlerMock).persistExecutionStatus(SETTINGS_PROCESSING, true);
        inOrder.verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        inOrder.verify(flmMetricHelperMock).incrementFlmMetric(eq(FlmMetric.FLM_KPI_CALCULATION_TIME_IN_MILLIS), anyLong());
    }

    @Test
    public void whenResumingLostNonSettingsBasedKpiCalculation_thenCalculationsContinuesFromTheLostKpiCalculationState() throws Exception {
        //i.e. stays in KPI_PROCESSING_GROUP_2 in determine state method, detected as LOST and resumes from KPI_PROCESSING_GROUP_2

        final Execution resumedExecutionSpy = spy(new Execution()); // used to test lost/not lost resumes from correct state
        final ExecutionState executionState = KPI_PROCESSING_GROUP_2;
        resumedExecutionSpy.setState(executionState);
        final String calculationId = "1";
        resumedExecutionSpy.setCalculationId(calculationId);
        resumedExecutionSpy.setCustomizedGlobalSettings(stubbedGlobalSettings);
        final String executionId = "executionId";
        resumedExecutionSpy.setId(executionId);

        final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor = ArgumentCaptor.forClass(KpiCalculationRequest.class);

        final KpiCalculationExecutorTest.GroupAsserter groupAsserter = new KpiCalculationExecutorTest.GroupAsserter(
                kpiCalculationRequestArgumentCaptor, executionId);

        //re-instantiate with real execution object, other mocks remain the same.
        final KpiCalculationExecutor kpiCalculationExecutorWithRealExecution = new KpiCalculationExecutor(resumedExecutionSpy,
                flmMetricHelperMock, requestProcessorMock, persistenceHandlerMock,
                kpiRequestRetryMock, sectorReferenceCellHandler, flmExecutionHandlerMock);
        kpiCalculationExecutorWithRealExecution.setKpiCalculationStateHandler(kpiCalculationStateHandlerMock);
        final String resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());

        when(kpiRequestRetryMock.getKpiCalculationState(kpiCalculationStateHandlerMock, calculationId))
                .thenThrow(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_LOST));
        when(kpiRequestRetryMock.getKpiRecalculationRequiredExceptionPredicate()).thenReturn(throwable -> true); // verify recalculation current state
        final Long sectorId = 1234L;
        when(sectorReferenceCellHandler.getSectorIdsRequiringReferenceCellRecalculation()).thenReturn(Collections.singleton(sectorId));
        kpiCalculationExecutorWithRealExecution.nonSettingsBasedExecute(executionState, true, resumeExecutionDate);

        final InOrder inOrder = inOrder(kpiRequestRetryMock, requestProcessorMock, persistenceHandlerMock, resumedExecutionSpy,
                flmMetricHelperMock);

        inOrder.verify(kpiRequestRetryMock).getKpiCalculationState(kpiCalculationStateHandlerMock, calculationId);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_2(true, sectorId);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_3(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_4(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_5(true);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_6(true, sectorId);

        inOrder.verify(requestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertGroup_7(true);

        inOrder.verify(persistenceHandlerMock).persistExecutionStatus(SETTINGS_PROCESSING, true);

        inOrder.verify(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());
        inOrder.verify(flmMetricHelperMock).incrementFlmMetric(eq(FlmMetric.FLM_KPI_CALCULATION_TIME_IN_MILLIS), anyLong());
    }

    private static final class GroupAsserter {

        private static final String EXECUTION_DATE = DateTimeFormatter.ISO_DATE.format(EXECUTION_TIME_STAMP.toLocalDateTime());
        private static final LocalDate EXECUTION_LOCAL_DATE = LocalDate.parse(EXECUTION_DATE, DateTimeFormatter.ISO_DATE);
        private static final String START_DATE_TIME = EXECUTION_LOCAL_DATE.minusDays(1).atTime(LocalTime.MIDNIGHT)
                .format(DateTimeFormatter.ISO_DATE_TIME);
        private static final String END_DATE_TIME = EXECUTION_LOCAL_DATE.minusDays(1).atTime(LocalTime.MAX)
                .format(DateTimeFormatter.ISO_DATE_TIME);
        private static final String RECALCULATION_DATE = EXECUTION_LOCAL_DATE.atStartOfDay().minusDays(7).format(DateTimeFormatter.ISO_DATE);

        private final String executionId;
        private final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor;

        public GroupAsserter(final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor,
                final String executionId) {
            this.kpiCalculationRequestArgumentCaptor = kpiCalculationRequestArgumentCaptor;
            this.executionId = executionId;
        }

        private void assertGroup_1(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_1 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_1, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group1_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_1);
        }

        private void assertGroup_2(final boolean isResumed, final Long sectorId) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_2 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_2, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group2_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .withAdditionalParameters(Collections.singletonMap("param.sectors_without_ref_cell",
                            "kpi_db://kpi_cell_sector_1440.sector_id in (" + sectorId + ')'))
                    .build();

            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_2);
        }

        private void assertGroup_2(final boolean isResumed) {
            final KpiCalculationRequest expectedRequestCallOfGroup_2 = KpiCalculationRequestCreator.empty(KPI_PROCESSING_GROUP_2, isResumed);
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_2);
        }

        private void assertGroup_3(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_3 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_3, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group3_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_3);
        }

        private void assertGroup_4(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_4 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_4, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group4_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_4);
        }

        private void assertGroup_5(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_5 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_5, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group5_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_5);
        }

        private void assertGroup_6(final boolean isResumed, final Long sectorId) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_6 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_6, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group6_kpis.json")
                    .withCurrentDate(EXECUTION_DATE)
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .withRecalculationDate(RECALCULATION_DATE)
                    .withAdditionalParameters(Collections.singletonMap("param.sectors_for_signal_range_recalculation",
                            "OR kpi_cell_sector_1440.sector_id in (" + sectorId + ')'))
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_6);
        }

        private void assertGroup_6(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_6 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_6, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group6_kpis.json")
                    .withCurrentDate(EXECUTION_DATE)
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .withRecalculationDate(RECALCULATION_DATE)
                    .withAdditionalParameters(Collections.singletonMap("param.sectors_for_signal_range_recalculation", StringUtils.EMPTY))
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_6);
        }

        private void assertGroup_7(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_7 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_7, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group7_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_7);
        }

        private void assertGroup_8(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_8 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_8, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group8_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .withExecutionId(executionId)
                    .build();

            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_8);
        }

        private void assertGroup_9(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_9 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_9, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group9_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .withExecutionId(executionId)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_9);
        }

        private void assertGroup_10(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_10 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_10, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group10_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .withExecutionId(executionId)
                    .withPreviousExecutionId(StringUtils.EMPTY)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_10);
        }

        private void assertGroup_11(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_11 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_11, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group11_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .withExecutionId(executionId)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_11);
        }

        private void assertGroup_12(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_12 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_12, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group12_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .withExecutionId(executionId)
                    .withAdditionalParameters(Collections.singletonMap("param.stubbed_global_setting", "stub"))
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_12);
        }

        private void assertGroup_13(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_13 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_13, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group13_kpis.json")
                    .withExecutionId(executionId)
                    .withPreviousExecutionId(StringUtils.EMPTY)
                    .withAdditionalParameters(Collections.singletonMap("param.transient_num_days", "11"))
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_13);
        }

        private void assertGroup_14(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_14 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_14, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group14_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .withExecutionId(executionId)
                    .withPreviousExecutionId(StringUtils.EMPTY)
                    .withAdditionalParameters(Collections.singletonMap("param.transient_num_days", "11"))
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedRequestCallOfGroup_14);
        }

        private void assertGroup_15(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_15 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_15, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group15_kpis.json")
                    .withStartDateTime(START_DATE_TIME)
                    .withEndDateTime(END_DATE_TIME)
                    .withExecutionId(executionId)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedRequestCallOfGroup_15);
        }

        private void assertGroup_16(final boolean isResumed) throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_16 = KpiCalculationRequestCreator.create(KPI_PROCESSING_GROUP_16, isResumed)
                    .loadGroupKpis("kpiCalculationRequests/group16_kpis.json")
                    .withExecutionId(executionId)
                    .build();
            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedRequestCallOfGroup_16);
        }
    }
}

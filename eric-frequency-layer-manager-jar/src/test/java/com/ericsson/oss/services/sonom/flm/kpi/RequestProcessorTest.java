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

import static com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState.FINISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.junit.MockitoJUnit.rule;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationRequest.KpiCalculationRequestCreator;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationRequestHandler;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationStateHandler;

import io.github.resilience4j.retry.RetryConfig;

/**
 * Unit test for {@link RequestProcessor} class.
 */
public class RequestProcessorTest {
    private static final String CALCULATION_STATE_RESPONSE = "{ \"successMessage\": \"\",\"calculationId\": 1 ,\"kpiOutputLocations\": {\"\": \"\"}}";
    private static final String CALCULATION_ID = "1";
    private static final String EXECUTION_DATE = "2020-01-28";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestProcessorTest.class);

    @Rule
    public MockitoRule mockitoRule = rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    public MetricHelper mockedMetricHelper;

    @Mock
    private Execution mockedExecution;

    @Mock
    private KpiCalculationRequestHandler mockedKpiCalculationRequestHandler;

    @Mock
    private KpiCalculationStateHandler mockedKpiCalculationStateHandler;

    @Mock
    private ExecutionDao mockedExecutionDao;

    @Mock
    private PersistenceHandler mockedPersistenceHandler;

    @Mock
    private RestResponse<String> mockedRestResponse;

    @Mock
    private KpiRequestRetry mockedKpiRequestRetry;

    @Mock
    private KpiValidation mockedKpiValidation;

    private RequestProcessor objectUnderTest;

    @Before
    public void setUp() {
        objectUnderTest = new RequestProcessor(mockedExecution,
                                               mockedMetricHelper,
                                               mockedExecutionDao,
                                               mockedKpiCalculationRequestHandler,
                                               mockedKpiCalculationStateHandler,
                                               mockedPersistenceHandler,
                                               mockedKpiRequestRetry,
                                               mockedKpiValidation);
    }

    @Test
    public void whenProcessRequestIsCalledWithPayload_thenSuccessFullExecution() throws Exception {
        final String fileName = "kpiCalculationRequests/group1_kpis.json";

        retryStubbing();
        when(mockedKpiCalculationRequestHandler.sendKpiCalculationRequest(any())).thenReturn(mockedRestResponse);
        when(mockedRestResponse.getStatusCode()).then(logAndCallGetStatus());
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(mockedRestResponse.getEntity()).thenReturn(CALCULATION_STATE_RESPONSE);
        when(mockedPersistenceHandler.persistExecutionStatus(any(), anyBoolean())).thenReturn(1L);
        when(mockedExecution.getCalculationId()).thenReturn(CALCULATION_ID);
        when(mockedKpiRequestRetry.getKpiCalculationState(any(KpiCalculationStateHandler.class), eq(CALCULATION_ID))).thenReturn(FINISHED);
        when(mockedMetricHelper.getTimeElapsedInMillis(anyLong())).thenReturn(1L);
        doNothing().when(mockedMetricHelper).incrementFlmMetric(any(), anyLong());
        doNothing().when(mockedMetricHelper).incrementFlmMetric(any());
        objectUnderTest.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, false)
                                                                   .loadGroupKpis(fileName)
                                                                   .withCurrentDate(EXECUTION_DATE)
                                                                   .build());

        verify(mockedExecution).setCalculationId(CALCULATION_ID);
        verify(mockedKpiRequestRetry).getKpiCalculationState(mockedKpiCalculationStateHandler, CALCULATION_ID);
        verify(mockedKpiValidation).validateKpiState(FINISHED, CALCULATION_ID);
        verify(mockedMetricHelper).getTimeElapsedInMillis(anyLong());
        verify(mockedMetricHelper).incrementFlmMetric(any());
    }

    @Test
    public void whenProcessRequestIsCalledWithEmptyPayload_thenExecutionStateIsPersistedOnly() throws Exception {
        retryStubbing();

        when(mockedPersistenceHandler.persistExecutionStatus(any(), anyBoolean())).thenReturn(1L);
        when(mockedMetricHelper.getTimeElapsedInMillis(anyLong())).thenReturn(1L);
        doNothing().when(mockedMetricHelper).incrementFlmMetric(any(), anyLong());
        doNothing().when(mockedMetricHelper).incrementFlmMetric(any());
        objectUnderTest.processRequest(KpiCalculationRequestCreator.empty(ExecutionState.KPI_PROCESSING_GROUP_1, false));

        verify(mockedPersistenceHandler).persistExecutionStatus(any(), anyBoolean());
        verify(mockedMetricHelper).getTimeElapsedInMillis(anyLong());
        verify(mockedMetricHelper).incrementFlmMetric(any());
    }

    @Test
    public void whenProcessRequestIsCalled_AndExhaustsRetries_FlmAlgorithmExceptionIsThrown() throws Exception {
        final String fileName = "kpiCalculationRequests/group1_kpis.json";

        retryStubbing();
        when(mockedKpiCalculationRequestHandler.sendKpiCalculationRequest(any())).thenReturn(mockedRestResponse);
        when(mockedRestResponse.getStatusCode()).then(logAndCallGetStatus());
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(mockedRestResponse.getEntity()).thenReturn(CALCULATION_STATE_RESPONSE);
        when(mockedPersistenceHandler.persistExecutionStatus(any(), anyBoolean())).thenReturn(1L);
        when(mockedExecution.getCalculationId()).thenReturn(CALCULATION_ID);
        when(mockedKpiRequestRetry.getKpiCalculationState(any(KpiCalculationStateHandler.class), eq(CALCULATION_ID)))
                .thenThrow(new FlmAlgorithmException(
                        FlmServiceExceptionCode.KPI_CALCULATION_ERROR));
        when(mockedMetricHelper.getTimeElapsedInMillis(anyLong())).thenReturn(1L);
        doNothing().when(mockedMetricHelper).incrementFlmMetric(any(), anyLong());
        doNothing().when(mockedMetricHelper).incrementFlmMetric(any());

        thrown.expect(FlmAlgorithmException.class);
        thrown.expectMessage(FlmServiceExceptionCode.KPI_CALCULATION_ERROR.getErrorMessage());

        objectUnderTest.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, false)
                                                                   .loadGroupKpis(fileName)
                                                                   .withCurrentDate(EXECUTION_DATE)
                                                                   .build());
    }

    @Test
    public void whenSendOnDemandCalculationRequestIsCalled_thenValidCalculationIdIsReturned() {
        when(mockedKpiCalculationRequestHandler.sendKpiCalculationRequest(any())).thenReturn(mockedRestResponse);
        when(mockedRestResponse.getStatusCode()).then(logAndCallGetStatus());
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(mockedRestResponse.getEntity()).thenReturn(CALCULATION_STATE_RESPONSE);
        final String calculationId = objectUnderTest.sendOnDemandCalculationRequest("");
        assertThat(calculationId).isEqualTo(CALCULATION_ID);

    }

    @Test
    public void whenFailedToPerformKpiCalculation_thenCalculationIdReturnedIsNull() {
        when(mockedKpiCalculationRequestHandler.sendKpiCalculationRequest(any())).thenReturn(mockedRestResponse);
        when(mockedRestResponse.getStatusCode()).then(logAndCallGetStatus());
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        final String calculationId = objectUnderTest.sendOnDemandCalculationRequest("");
        assertThat(calculationId).isNull();
    }

    private void retryStubbing() {
        final RetryConfig retryConfig = new KpiRequestRetry(new KpiActionForState(new KpiValidation()))
                .getRetryConfigDefinitionForKpiRecalculationRetry(1, 1);
        when(mockedKpiRequestRetry.getRetryConfigDefinitionForKpiRecalculationRetry(anyInt(), anyInt())).thenReturn(retryConfig);
    }

    private Answer<Integer> logAndCallGetStatus() {
        LOGGER.info("RestResponse#getStatusCode is deprecated. Use RestResponse#getStatus.");
        return invocation -> mockedRestResponse.getStatus();
    }
}

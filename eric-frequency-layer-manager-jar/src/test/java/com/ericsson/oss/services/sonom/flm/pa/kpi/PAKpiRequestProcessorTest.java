/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.pa.kpi;

import static com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState.FINISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.flm.kpi.KpiActionForState;
import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationRequest;
import com.ericsson.oss.services.sonom.flm.kpi.KpiRequestRetry;
import com.ericsson.oss.services.sonom.flm.kpi.KpiValidation;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationRequestHandler;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationStateHandler;

import io.github.resilience4j.retry.RetryConfig;

/**
 * Unit tests for {@link PAKpiRequestProcessor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PAKpiRequestProcessorTest {
    private static final String CALCULATION_STATE_RESPONSE = "{ \"successMessage\": \"\",\"calculationId\": 1 ,\"kpiOutputLocations\": {\"\": \"\"}}";
    private static final String CALCULATION_ID = "1";
    private static final Timestamp PA_EXECUTION_TIME_STAMP = Timestamp.valueOf("2020-05-07 10:53:15.930");
    private final String PA_EXECUTION_START_TIME_STAMP = DateTimeFormatter.ISO_DATE_TIME.format(PA_EXECUTION_TIME_STAMP.toLocalDateTime());
    private final String PA_EXECUTION_END_TIME_STAMP = DateTimeFormatter.ISO_DATE_TIME.format(PA_EXECUTION_TIME_STAMP.toLocalDateTime());
    private static final String FLM_EXECUTION_ID = "FLM_1600701252-162";

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();

    @Mock
    private KpiRequestRetry mockedKpiRequestRetry;

    @Mock
    private KpiCalculationRequestHandler mockedKpiCalculationRequestHandler;

    @Mock
    private RestResponse<String> mockedRestResponse;

    @Mock
    private KpiCalculationStateHandler mockedKpiCalculationStateHandler;

    @Mock
    private KpiValidation mockedKpiValidation;

    private PAKpiRequestProcessor objectUnderTest;

    @Before
    public void setUp() {
        objectUnderTest = new PAKpiRequestProcessor(mockedKpiRequestRetry,
                mockedKpiCalculationRequestHandler,
                mockedKpiCalculationStateHandler,
                mockedKpiValidation);
    }

    @Test
    public void whenPAKpiProcessRequestIsCalledWithPayload_thenSuccessFullExecution() throws Exception {
        final String fileName = "kpiCalculationRequests/pa_kpis.json";

        retryStubbing();
        when(mockedKpiCalculationRequestHandler.sendKpiCalculationRequest(any())).thenReturn(mockedRestResponse);
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(mockedRestResponse.getEntity()).thenReturn(CALCULATION_STATE_RESPONSE);
        when(mockedKpiRequestRetry.getKpiCalculationState(any(KpiCalculationStateHandler.class), eq(CALCULATION_ID))).thenReturn(FINISHED);

        objectUnderTest.processRequest(KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.SUCCEEDED, false)
                .loadGroupKpis(fileName)
                .withStartTimeStamp(PA_EXECUTION_START_TIME_STAMP)
                .withEndTimeStamp(PA_EXECUTION_END_TIME_STAMP)
                .withExecutionId(FLM_EXECUTION_ID)
                .withAdditionalParameters(Collections.singletonMap("param.stubbed_global_setting", "stub"))
                .build());

        verify(mockedKpiRequestRetry).getKpiCalculationState(mockedKpiCalculationStateHandler, CALCULATION_ID);
        verify(mockedKpiValidation).validateKpiState(FINISHED, CALCULATION_ID);
    }

    @Test
    public void whenPAKpiProcessRequestIsCalled_AndExhaustsRetries_FlmAlgorithmExceptionIsThrown() throws Exception {
        final String fileName = "kpiCalculationRequests/pa_kpis.json";

        retryStubbing();
        when(mockedKpiCalculationRequestHandler.sendKpiCalculationRequest(any())).thenReturn(mockedRestResponse);
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(mockedRestResponse.getEntity()).thenReturn(CALCULATION_STATE_RESPONSE);
        when(mockedKpiRequestRetry.getKpiCalculationState(any(KpiCalculationStateHandler.class), eq(CALCULATION_ID)))
                .thenThrow(new FlmAlgorithmException(
                        FlmServiceExceptionCode.KPI_CALCULATION_ERROR));

        expectedExceptions.expect(FlmAlgorithmException.class);
        expectedExceptions.expectMessage(FlmServiceExceptionCode.KPI_CALCULATION_ERROR.getErrorMessage());

        objectUnderTest.processRequest(KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, false)
                .loadGroupKpis(fileName)
                .withStartTimeStamp(PA_EXECUTION_START_TIME_STAMP)
                .withEndTimeStamp(PA_EXECUTION_END_TIME_STAMP)
                .withExecutionId(FLM_EXECUTION_ID)
                .withAdditionalParameters(Collections.singletonMap("param.stubbed_global_setting", "stub"))
                .build());
    }

    @Test
    public void whenSendOnDemandCalculationRequestIsCalled_thenValidCalculationIdIsReturned() {
        when(mockedKpiCalculationRequestHandler.sendKpiCalculationRequest(any())).thenReturn(mockedRestResponse);
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(mockedRestResponse.getEntity()).thenReturn(CALCULATION_STATE_RESPONSE);

        final String calculationId = objectUnderTest.sendOnDemandCalculationRequest("");

        assertThat(calculationId).isEqualTo(CALCULATION_ID);
    }

    @Test
    public void whenFailedToPerformPAKpiCalculation_thenCalculationIdReturnedIsNull() {
        when(mockedKpiCalculationRequestHandler.sendKpiCalculationRequest(any())).thenReturn(mockedRestResponse);
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        final String calculationId = objectUnderTest.sendOnDemandCalculationRequest("");

        assertThat(calculationId).isNull();
    }

    private void retryStubbing() {
        final RetryConfig retryConfig = new KpiRequestRetry(new KpiActionForState(new KpiValidation()))
                .getRetryConfigDefinitionForKpiRecalculationRetry(1, 1);
        when(mockedKpiRequestRetry.getRetryConfigDefinitionForKpiRecalculationRetry(anyInt(), anyInt())).thenReturn(retryConfig);
    }
}
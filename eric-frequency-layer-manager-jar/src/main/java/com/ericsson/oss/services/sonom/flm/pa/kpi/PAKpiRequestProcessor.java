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

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.KPI_CALCULATION_RETRIES_EXPIRED;

import org.apache.http.HttpStatus;

import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationRequest;
import com.ericsson.oss.services.sonom.flm.kpi.KpiRequestRetry;
import com.ericsson.oss.services.sonom.flm.kpi.KpiValidation;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.util.ExecuteWithRetry;
import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationRequestHandler;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationStateHandler;
import com.ericsson.oss.services.sonom.kpi.service.rest.api.v1.CalculationRequestSuccessResponse;

import com.google.gson.Gson;

import io.vavr.CheckedRunnable;

/**
 * Processes requests made during PA KPI Calculation executions.
 */
public class PAKpiRequestProcessor {

    private static final int MAX_RETRY_ATTEMPTS_KPI_CALCULATION_REQUEST = Integer
            .parseInt(getEnvironmentValue("MAX_RETRY_ATTEMPTS_KPI_CALCULATION_REQUEST", "60"));
    private static final int RETRY_WAIT_DURATION_KPI_CALCULATION_REQUEST_SECONDS = Integer
            .parseInt(getEnvironmentValue("RETRY_WAIT_DURATION_KPI_CALCULATION_REQUEST_SECONDS", "30"));
    private static final int MAX_RETRY_ATTEMPTS_KPI_CALCULATION_STATE = Integer
            .parseInt(getEnvironmentValue("MAX_RETRY_ATTEMPTS_KPI_CALCULATION_STATE", "120"));
    private static final int RETRY_WAIT_DURATION_KPI_CALCULATION_STATE_SECONDS = Integer
            .parseInt(getEnvironmentValue("RETRY_WAIT_DURATION_KPI_CALCULATION_STATE_SECONDS", "5"));
    private static final int KPI_RECALCULATION_RETRY_ATTEMPTS = Integer
            .parseInt(getEnvironmentValue("KPI_RECALCULATION_RETRY_ATTEMPTS", "3"));
    private static final int KPI_RECALCULATION_RETRY_WAIT_DURATION = Integer
            .parseInt(getEnvironmentValue("KPI_RECALCULATION_RETRY_WAIT_DURATION", "1"));
    private static final String REQUEST_PROCESSOR_KPI_RETRY = "KpiRequestProcessor KPI Retry";

    private final KpiRequestRetry kpiRequestRetry;
    private final KpiCalculationRequestHandler kpiCalculationRequestHandler;
    private final KpiCalculationStateHandler kpiCalculationStateHandler;
    private final KpiValidation kpiValidation;

    public PAKpiRequestProcessor(final KpiRequestRetry kpiRequestRetry) {
        this.kpiRequestRetry = kpiRequestRetry;
        kpiCalculationRequestHandler = new KpiCalculationRequestHandler(
                MAX_RETRY_ATTEMPTS_KPI_CALCULATION_REQUEST,
                RETRY_WAIT_DURATION_KPI_CALCULATION_REQUEST_SECONDS); // 30 minutes by default with retries per request, 10 executions in parallel
        kpiCalculationStateHandler = new KpiCalculationStateHandler(
                MAX_RETRY_ATTEMPTS_KPI_CALCULATION_STATE,
                RETRY_WAIT_DURATION_KPI_CALCULATION_STATE_SECONDS, true);
        kpiValidation = new KpiValidation();
    }

    // Added for JUnit
    PAKpiRequestProcessor(final KpiRequestRetry kpiRequestRetry, final KpiCalculationRequestHandler kpiCalculationRequestHandler,
                                 final KpiCalculationStateHandler kpiCalculationStateHandler, final KpiValidation kpiValidation) {
        this.kpiRequestRetry = kpiRequestRetry;
        this.kpiCalculationRequestHandler = kpiCalculationRequestHandler;
        this.kpiCalculationStateHandler = kpiCalculationStateHandler;
        this.kpiValidation = kpiValidation;
    }

    /**
     * Process standard requests.
     *
     * @param kpiCalculationRequest
     *            The request wrapper class
     * @throws FlmAlgorithmException
     *            Thrown if an error occurs during handling the request.
     */
    public void processRequest(final KpiCalculationRequest kpiCalculationRequest) throws FlmAlgorithmException {
        ExecuteWithRetry.executeWithRetry(handleRequest(kpiCalculationRequest),
                throwable -> new FlmAlgorithmException(KPI_CALCULATION_RETRIES_EXPIRED, throwable),
                REQUEST_PROCESSOR_KPI_RETRY,
                kpiRequestRetry.getRetryConfigDefinitionForKpiRecalculationRetry(KPI_RECALCULATION_RETRY_ATTEMPTS,
                        KPI_RECALCULATION_RETRY_WAIT_DURATION));
    }

    private CheckedRunnable handleRequest(final KpiCalculationRequest kpiCalculationRequest) {
        return () -> sendOnDemandCalculation(kpiCalculationRequest);
    }

    private void sendOnDemandCalculation(final KpiCalculationRequest kpiCalculationRequest) throws FlmAlgorithmException {
        final String calculationId = sendOnDemandCalculationRequest(kpiCalculationRequest.getRequestPayload());
        final KpiCalculationState kpiCalculationState = kpiRequestRetry.getKpiCalculationState(kpiCalculationStateHandler, calculationId);
        kpiValidation.validateKpiState(kpiCalculationState, calculationId);
    }

    protected String sendOnDemandCalculationRequest(final String kpiCalculationRequestPayload) {
        final RestResponse<String> response = kpiCalculationRequestHandler.sendKpiCalculationRequest(kpiCalculationRequestPayload);
        if (response.getStatus() == HttpStatus.SC_CREATED) {
            final CalculationRequestSuccessResponse calculationRequestSuccessResponse =
                    new Gson().fromJson(response.getEntity(), CalculationRequestSuccessResponse.class);
            return calculationRequestSuccessResponse.getCalculationId();
        }
        return null;
    }
}

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

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.kpi.KpiActionForState;
import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationRequest;
import com.ericsson.oss.services.sonom.flm.kpi.KpiRequestRetry;
import com.ericsson.oss.services.sonom.flm.kpi.KpiValidation;
import com.ericsson.oss.services.sonom.flm.kpi.util.AdditionalParametersUtils;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionLatch;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAStageExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * PAKpiCalculationExecutor class used during PA KPI Calculations.
 */
public class PAKpiCalculationExecutor implements PAStageExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PAKpiCalculationExecutor.class);

    private final PAExecution paExecution;
    private final Execution flmExecution;
    private final PAKpiRequestProcessor requestProcessor;
    private final PAExecutionLatch latch;

    public PAKpiCalculationExecutor(final PAExecution paExecution, final Execution flmExecution, final PAExecutionLatch latch) {
        this.paExecution = paExecution;
        this.flmExecution = flmExecution;
        final KpiRequestRetry kpiRequestRetry = new KpiRequestRetry(new KpiActionForState(new KpiValidation()));
        requestProcessor = new PAKpiRequestProcessor(kpiRequestRetry);
        this.latch = latch;
    }

    // Added for JUnit Mockito
    PAKpiCalculationExecutor(final PAExecution paExecution, final Execution flmExecution,
            final PAKpiRequestProcessor requestProcessor, final PAExecutionLatch latch) {
        this.paExecution = paExecution;
        this.flmExecution = flmExecution;
        this.requestProcessor = requestProcessor;
        this.latch = latch;
    }

    @Override
    public Object execute() throws PAExecutionException {
        try {
            LOGGER.info("Starting PA KPI Calculation");
            startKpiCalculation();
        } catch (final PAExecutionInterruptedException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Interrupt signal received. Terminating PA KPI Calculation");
            throw e;
        } catch (final Exception e) {
            LOGGER.error("PA KPI Calculation failed for PA execution ID: '{}'", paExecution.getId());
            throw new PAExecutionException(e);
        }
        return null;
    }

    private void startKpiCalculation() throws IOException, FlmAlgorithmException, PAExecutionInterruptedException {
        final String windowStartTimeStamp = DateTimeFormatter.ISO_DATE_TIME.format(paExecution.getPaWindowStartTime().toLocalDateTime());
        final String windowEndTimeStamp = DateTimeFormatter.ISO_DATE_TIME.format(paExecution.getPaWindowEndTime().toLocalDateTime());

        latch.verifyNotInterruptedAndContinue();
        LOGGER.info("Requesting PA KPI calculation for PA execution ID: '{}'", paExecution.getId());
        requestProcessor.processRequest(KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.SUCCEEDED, false)
                //we don't persist or do anything with ExecutionState, so just setting to succeeded
                .loadGroupKpis("kpiCalculationRequests/pa_kpis.json")
                .withStartTimeStamp(windowStartTimeStamp)
                .withEndTimeStamp(windowEndTimeStamp)
                .withExecutionId(flmExecution.getId())
                .withWeekendDays(flmExecution.getWeekendDays())
                .withAdditionalParameters(AdditionalParametersUtils.getAdditionalParametersForGlobalSettings(
                        flmExecution.getCustomizedGlobalSettings()))
                .build());
    }
}

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

package com.ericsson.oss.services.sonom.flm.service.startup.executions;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.executor.FlmAlgorithmExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * Class to resume an {@link Execution} that is responsible for non-settings based KPI calculations.
 */
public class ResumedNonSettingsExecutionsRunner implements Callable<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResumedNonSettingsExecutionsRunner.class);
    private static final FlmExecution FLM_EXECUTION = new FlmExecution();

    private final Execution execution;

    public ResumedNonSettingsExecutionsRunner(final Execution execution) {
        this.execution = execution;
    }

    @Override
    public Void call() {
        final String executionID = execution.getId();
        LOGGER.info("Resuming execution with ID {}. Execution is responsible for non-settings based KPI calculations", executionID);
        final FlmAlgorithmExecutor flmAlgorithmExecutor = FLM_EXECUTION.getNewFlmAlgorithmExecutor(execution);
        final String resumedDate = DateTimeFormatter.ISO_DATE.format(execution.getStartTime().toLocalDateTime());
        if (FlmAlgorithmExecutor.calculateNonSettingsBasedKpis(flmAlgorithmExecutor.getKpiCalculationExecutor(), resumedDate,
                execution, true)) {
            LOGGER.info("For Resumed execution with ID : {} , non-settings based KPI calculations completed ", executionID);
        } else {
            LOGGER.error("Execution ID {} with responsibility for non-settings based KPI calculations has failed.", executionID);
        }
        return null;
    }
}

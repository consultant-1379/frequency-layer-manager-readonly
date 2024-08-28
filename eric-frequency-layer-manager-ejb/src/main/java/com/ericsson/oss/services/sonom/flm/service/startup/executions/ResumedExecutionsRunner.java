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

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.executions.ExecutionDbHandler;
import com.ericsson.oss.services.sonom.flm.executor.FlmAlgorithmExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * Class to resume an {@link Execution}.
 */
public class ResumedExecutionsRunner implements Callable<Void> {

    private static final FlmExecution FLM_EXECUTION = new FlmExecution();
    private static final Logger LOGGER = LoggerFactory.getLogger(ResumedExecutionsRunner.class);

    private final Execution execution;
    private final ExecutionDbHandler executionDbHandler;
    private final boolean isNonSettingsResumedCalculationDone;

    public ResumedExecutionsRunner(final Execution execution,
            final ExecutionDbHandler executionDbHandler, final boolean isNonSettingsResumedCalculationDone) {
        this.execution = execution;
        this.executionDbHandler = executionDbHandler;
        this.isNonSettingsResumedCalculationDone = isNonSettingsResumedCalculationDone;
    }

    @Override
    public Void call() {
        try {
            final FlmAlgorithmExecutor flmAlgorithmExecutor = FLM_EXECUTION.getNewFlmAlgorithmExecutor(execution);
            if (isNonSettingsResumedCalculationDone) {
                flmAlgorithmExecutor.getKpiCalculationExecutor().setIsNonSettingsBasedKpiCalculationCompleted();
            }
            executionDbHandler.incrementRetryAttempts(execution);
            flmAlgorithmExecutor.executeActivity();
            LOGGER.info("Resumed Execution: {} has been completed", execution.getId());
        } catch (final Exception e) {
            LOGGER.error("Error resuming execution with ID {}", execution.getId(), e);
        }
        return null;
    }
}

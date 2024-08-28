/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.settings.evaluation;

import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.CELL_SETTINGS_EVALUATION_ERROR;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDao;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDaoImpl;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.executor.StageExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * CellSettingsExecutor class used during evaluation of Cell settings.
 */
public class CellSettingsExecutor implements StageExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellSettingsExecutor.class);

    private static final int EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 10;
    private static final int EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 30;

    private final CellSettingsProcessor cellSettingsProcessor;
    private final PersistenceHandler persistenceHandler;
    private final Execution execution;
    private final MetricHelper flmMetricHelper;

    public CellSettingsExecutor(final CmStore cmStore, final Execution execution, final MetricHelper flmMetricHelper,
            final PersistenceHandler persistenceHandler) {
        final CellSettingsDao cellSettingsDao = new CellSettingsDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS, EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
        this.execution = execution;
        this.flmMetricHelper = flmMetricHelper;
        this.persistenceHandler = persistenceHandler;
        cellSettingsProcessor = new CellSettingsProcessor(cmStore, execution, cellSettingsDao);
    }

    // required for Mockito JUnit
    public CellSettingsExecutor(final Execution execution, final MetricHelper flmMetricHelper,
            final PersistenceHandler persistenceHandler, final CellSettingsProcessor cellSettingsProcessor) {
        this.execution = execution;
        this.flmMetricHelper = flmMetricHelper;
        this.persistenceHandler = persistenceHandler;
        this.cellSettingsProcessor = cellSettingsProcessor;
    }

    @Override
    public void execute(final ExecutionState executionState, final boolean isResumed, final boolean isFullExecution, final String executionDate)
            throws FlmAlgorithmException {
        try {
            applySettingsToCells(executionDate, isResumed, isFullExecution);
        } catch (final Exception e) {
            throw new FlmAlgorithmException(CELL_SETTINGS_EVALUATION_ERROR, e);
        }
    }

    private void applySettingsToCells(final String executionDate, final boolean isResumed, final boolean isFullExecution)
            throws SQLException, InterruptedException, ExecutionException, FlmAlgorithmException {
        final long cellSettingsStartTime = System.nanoTime();
        try {
            switch (execution.getState()) {
                case SETTINGS_PROCESSING:
                    LOGGER.info("Evaluate settings for cells and persist.");
                    cellSettingsProcessor.processApplySettingsToCell(ExecutionState.SETTINGS_PROCESSING,
                            executionDate, isResumed); //FALLTHROUGH
                case SETTINGS_PROCESSING_SUCCEEDED:
                    persistenceHandler.persistExecutionStatus(ExecutionState.SETTINGS_PROCESSING_SUCCEEDED, isResumed);
                    execution.setState(ExecutionState.getNextState(isFullExecution, execution.getState()));
                    LOGGER.info("All settings applied to cells");
                    break;
                default:
                    LOGGER.info("State is {}, no work needed to apply settings to cells", execution.getState());
                    break;
            }
        } finally {
            incrementFlmCountMetric(cellSettingsStartTime,
                    FlmMetric.SETTINGS_PROCESSING_TIME_IN_MILLIS);
        }
    }

    private void incrementFlmCountMetric(final Long requestStartTime, final FlmMetric flmMetricTime) {
        flmMetricHelper.incrementFlmMetric(flmMetricTime,
                flmMetricHelper.getTimeElapsedInMillis(requestStartTime));
    }
}

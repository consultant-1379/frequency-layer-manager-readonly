/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.settings.history;

import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.CELL_SETTINGS_HISTORY_ERROR;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsHistoryDao;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsHistoryDaoImpl;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.executor.StageExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * CellSettingsHistoryExecutor class used during inserting executions into history settings.
 */
public class CellSettingsHistoryExecutor implements StageExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellSettingsHistoryExecutor.class);

    private static final int EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 10;
    private static final int EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 30;
    private final CellSettingsHistoryProcessor cellSettingsHistoryProcessor;
    private final PersistenceHandler persistenceHandler;
    private final Execution execution;
    private final MetricHelper flmMetricHelper;

    public CellSettingsHistoryExecutor(final Execution execution, final MetricHelper flmMetricHelper,
            final PersistenceHandler persistenceHandler) {
        final ExecutionDao executionDao = new ExecutionDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS, EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
        final CellSettingsHistoryDao cellSettingsHistoryDao = new CellSettingsHistoryDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS,
                EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
        this.execution = execution;
        this.flmMetricHelper = flmMetricHelper;
        this.persistenceHandler = persistenceHandler;
        cellSettingsHistoryProcessor = new CellSettingsHistoryProcessor(execution, executionDao, cellSettingsHistoryDao);
    }

    // required for JUnit Testing
    public CellSettingsHistoryExecutor(final Execution execution, final MetricHelper flmMetricHelper,
            final PersistenceHandler persistenceHandler, final CellSettingsHistoryProcessor cellSettingsHistoryProcessor) {
        this.execution = execution;
        this.flmMetricHelper = flmMetricHelper;
        this.persistenceHandler = persistenceHandler;
        this.cellSettingsHistoryProcessor = cellSettingsHistoryProcessor;
    }

    @Override
    public void execute(final ExecutionState executionState, final boolean isResumed, final boolean isFullExecution, final String executionDate)
            throws FlmAlgorithmException {
        final long cellSettingsHistoryStartTime = System.nanoTime();
        try {
            switch (execution.getState()) {
                case CELL_SETTINGS_HISTORY:
                    copySettingsIntoHistoricalTable(isResumed);
                    //fallthrough
                case CLEAN_CELL_SETTINGS:
                    cleanCellSettings(isResumed);
                    break;
                default:
                    //do nothing
            }
            LOGGER.info("All settings applied to CellSettingsHistory");
        } catch (final Exception e) {
            throw new FlmAlgorithmException(CELL_SETTINGS_HISTORY_ERROR, e);
        } finally {
            incrementFlmCountMetric(cellSettingsHistoryStartTime);
        }
    }

    private void copySettingsIntoHistoricalTable(final Boolean isResumed) throws SQLException, FlmAlgorithmException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                    "Insert cell settings execution into historical cell table."));
        }
        cellSettingsHistoryProcessor.insertCellSettingsIntoHistoricalTable(execution.getState(), execution.getId(), isResumed);
        persistenceHandler.persistExecutionStatus(ExecutionState.CLEAN_CELL_SETTINGS, isResumed);
        execution.setState(ExecutionState.CLEAN_CELL_SETTINGS);
    }

    private void cleanCellSettings(final boolean isResumed) throws SQLException, FlmAlgorithmException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                    "Remove old cell settings from database"));
        }
        cellSettingsHistoryProcessor.processCleanUpRequest(ExecutionState.CLEAN_CELL_SETTINGS, isResumed,
                execution.getConfigurationId(), execution.getId());
        persistenceHandler.persistExecutionStatus(ExecutionState.SUCCEEDED, isResumed);
        execution.setState(ExecutionState.SUCCEEDED);
    }

    private void incrementFlmCountMetric(final Long requestStartTime) {
        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_CELL_SETTINGS_HISTORY_COPY_TIME_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(requestStartTime));
    }

}

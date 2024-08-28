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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsHistoryDao;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * Processes cell history settings insertion
 * during FLM algorithm executions.
 */
public class CellSettingsHistoryProcessor {

    private static final String CELL_CONFIGURATION = "cell_configuration";
    private static final Logger LOGGER = LoggerFactory.getLogger(CellSettingsHistoryProcessor.class);

    private CellSettingsHistoryDao cellSettingsHistoryDao; //NOPMD cannot be final or mockito can't inject mock for testing
    private PersistenceHandler persistenceHandler; //NOPMD cannot be final or mockito can't inject mock for testing

    private DatabaseAccess databaseAccess = new FlmDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing

    public CellSettingsHistoryProcessor(final Execution execution, final ExecutionDao executionDao,
                                        final CellSettingsHistoryDao cellSettingsHistoryDao) {
        this.cellSettingsHistoryDao = cellSettingsHistoryDao;
        this.persistenceHandler = new PersistenceHandler(execution, executionDao);
    }

    /**
     * insert cell setting history.
     *
     * @param executionState the execution status state.
     * @param executionId execution id
     * @param isResumed <code>true</code> if resumed execution
     * @throws SQLException if persistence to DB fails.
     * @throws FlmAlgorithmException  if copy of cell settings in DB fails.
     */
    public void insertCellSettingsIntoHistoricalTable(final ExecutionState executionState,
                                                      final String executionId,
                                                      final boolean isResumed) throws SQLException, FlmAlgorithmException {
        cellSettingsHistoryDao.copyCellSettings(executionId);
        persistenceHandler.persistExecutionStatus(executionState, isResumed);
    }

    /**
     * Remove cell configurations used in this algorithm execution.
     *
     * @param executionState  the execution status state.
     * @param isResumed       <code>true</code> if resumed execution
     * @param configurationId The configuration id of the execution
     * @param executionId     The execution id to be used to clean up the cell_configuration table
     * @throws FlmAlgorithmException if persistence to DB fails.
     * @throws SQLException          if removing of cell configurations in DB fails.
     */
    public void processCleanUpRequest(final ExecutionState executionState, final boolean isResumed,
                                      final int configurationId, final String executionId) throws FlmAlgorithmException, SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId,
                String.format("Cleaning settings from '%s' with Configuration Id: %s.", CELL_CONFIGURATION, configurationId)));
        }
        final String sqlStatement = String.format("DELETE FROM %s WHERE execution_id = '%s'", CELL_CONFIGURATION, executionId);
        try (final Connection connection = ((FlmDatabaseAccess) databaseAccess).getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.execute();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(executionId,
                    String.format("%d cells deleted from '%s'", preparedStatement.getUpdateCount(), CELL_CONFIGURATION)));
            }
            persistenceHandler.persistExecutionStatus(executionState, isResumed);
        }
    }
}

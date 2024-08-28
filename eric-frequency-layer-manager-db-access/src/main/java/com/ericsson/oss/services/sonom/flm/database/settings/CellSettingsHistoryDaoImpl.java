/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.sonom.flm.database.settings;

import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.CELL_CONFIGURATION;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.CELL_CONFIGURATION_HISTORY;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getAllColumnNamesForCellConfigHistory;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getAllColumnNamesForInsertionSelectForCellConfigHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;

import io.vavr.CheckedFunction0;

/**
 * Class to implement methods of {@link CellSettingsHistoryDaoImpl}.
 */
public class CellSettingsHistoryDaoImpl implements CellSettingsHistoryDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(CellSettingsDaoImpl.class);
    private static final String ERROR_MESSAGE_FAILED_TO_EXECUTE_INSERT = "Failed to execute insert : {}";
    private DatabaseAccess databaseAccess = new FlmDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing
    private final DatabaseRetry databaseRetry;

    public CellSettingsHistoryDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public int copyCellSettings(final String executionId) throws SQLException {
        final CheckedFunction0<Integer> executeFunction = () -> performCopyCellSettings(executionId);
        return databaseRetry.executeWithRetryAttempts(executeFunction);
    }

    /**
     * Method to copy cell settings.
     * @param executionId id of execution
     * @return number of updated rows
     * @throws SQLException if a database access error occurs or this method is called on a closed {@code Statement}
     */
    public int performCopyCellSettings(final String executionId) throws SQLException {
        final String queryFirstPart = String.format("INSERT INTO %s(%s)",
                CELL_CONFIGURATION_HISTORY, getAllColumnNamesForCellConfigHistory());
        final String querySecondPart = String.format("SELECT %s FROM %s WHERE %s=?;",
                getAllColumnNamesForInsertionSelectForCellConfigHistory(), CELL_CONFIGURATION, EXECUTION_ID);
        final String query = queryFirstPart + querySecondPart;
        LOGGER.debug("Executing insert query, {}", query);

        try (final Connection connection = ((FlmDatabaseAccess) databaseAccess).getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(query)) { // NOSONAR reviewed and not a SQL vunerability
            preparedStatement.setString(1, executionId);
            preparedStatement.execute();

            return preparedStatement.getUpdateCount();
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_INSERT, query, e);
            throw e;
        }
    }
}

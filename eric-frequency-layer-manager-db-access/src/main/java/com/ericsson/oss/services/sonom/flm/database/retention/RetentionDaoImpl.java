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
package com.ericsson.oss.services.sonom.flm.database.retention;

import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.FLM_EXECUTIONS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.ID;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.START_TIME;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.CELL_CONFIGURATION_HISTORY;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.CREATED;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.EXECUTION_ID;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;

/**
 * Class to implement methods of {@link RetentionDao}.
 */
public class RetentionDaoImpl implements RetentionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetentionDaoImpl.class);

    @Override
    public void cleanUpFlmExecutionsTable(final LocalDateTime retentionDate, final Integer retentionExecutionCount) throws SQLException {
        LOGGER.debug("Cleaning executions from '{}' which are older than '{}' ", FLM_EXECUTIONS, retentionDate);
        final String sqlStatement = String.format(
                "DELETE FROM \"%s\" WHERE \"%s\" < ? OR " +
                "\"%s\" NOT IN (SELECT \"%s\" FROM \"%s\" ORDER BY \"%s\" DESC LIMIT ?)", FLM_EXECUTIONS,
                START_TIME,
                ID,
                ID,
                FLM_EXECUTIONS,
                START_TIME);
        try (final Connection connection = DriverManager.getConnection(DatabaseProperties.getFlmJdbcConnection(),
                DatabaseProperties.getFlmJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(retentionDate));
            preparedStatement.setInt(2, retentionExecutionCount);
            preparedStatement.execute();
            LOGGER.info("{} executions deleted from '{}' which were older than '{}' or which were older than the last '{}' executions.",
                    preparedStatement.getUpdateCount(),
                    FLM_EXECUTIONS,
                    retentionDate, retentionExecutionCount);
        }
    }

    @Override
    public void cleanUpHistoricalCellConfigurationTable(final LocalDateTime retentionDate, final Integer retentionExecutionCount)
            throws SQLException {
        LOGGER.debug("Cleaning historical cell configurations from '{}' which are older than '{}' " +
                "or which are older than the last '{}' executions.", CELL_CONFIGURATION_HISTORY, retentionDate, retentionExecutionCount);
        final String sqlStatement = String.format(
                "DELETE FROM \"%s\" WHERE \"%s\" < ? OR " +
                "\"%s\" NOT IN (SELECT \"%s\" FROM (SELECT \"%s\", max(\"%s\") " +
                "FROM \"%s\" GROUP BY \"%s\") AS max_created ORDER BY max_created DESC LIMIT ?)",
                CELL_CONFIGURATION_HISTORY, CREATED,
                EXECUTION_ID, EXECUTION_ID, EXECUTION_ID, CREATED,
                CELL_CONFIGURATION_HISTORY, EXECUTION_ID);

        try (final Connection connection = DriverManager.getConnection(DatabaseProperties.getFlmJdbcConnection(),
                DatabaseProperties.getFlmJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(retentionDate));
            preparedStatement.setInt(2, retentionExecutionCount);
            preparedStatement.execute();
            LOGGER.info("{} cell configurations deleted from '{}' which were older than '{}' or which were older than the last '{}' executions.",
                    preparedStatement.getUpdateCount(), CELL_CONFIGURATION_HISTORY, retentionDate, retentionExecutionCount);
        }
    }
}

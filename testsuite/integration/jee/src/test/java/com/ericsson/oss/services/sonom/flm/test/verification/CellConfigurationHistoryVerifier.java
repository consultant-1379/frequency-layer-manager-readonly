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
package com.ericsson.oss.services.sonom.flm.test.verification;

import static com.ericsson.oss.services.sonom.common.env.DatabaseProperties.getFlmJdbcConnection;
import static com.ericsson.oss.services.sonom.common.env.DatabaseProperties.getFlmJdbcProperties;
import static com.ericsson.oss.services.sonom.common.test.sql.SqlAssertions.assertCountQuery;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getAllColumnNamesForCellConfigHistory;
import static org.junit.Assert.assertTrue;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.test.util.CsvReader;

public final class CellConfigurationHistoryVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(CellConfigurationHistoryVerifier.class);
    private static final String CELL_CONFIGURATION_HISTORY_TABLE = "cell_configuration_history";
    private static final String QUERY = String.format("SELECT COUNT(*) FROM \"%s\"", CELL_CONFIGURATION_HISTORY_TABLE);
    private static final String CELL_SETTINGS_ASSERTION_FILE = "csv-assertions/flm_service_user/cell_settings_history_assertions.csv";
    private static final List<String> EXPECTED_COLUMNS_OF_CELL_CONFIGURATION_HISTORY = Arrays.asList(getAllColumnNamesForCellConfigHistory().split(",", -1));

    private CellConfigurationHistoryVerifier() {
    }

    public static void verifyCellConfigurationHistoryTable() {
        final Integer expectedResult = CsvReader.getCsvAsList(Boolean.FALSE, CELL_SETTINGS_ASSERTION_FILE).size();

        assertCountQueryForCellConfigurationHistory(expectedResult, QUERY);
        assertTrue(verifyTableColumns(CELL_CONFIGURATION_HISTORY_TABLE, EXPECTED_COLUMNS_OF_CELL_CONFIGURATION_HISTORY));
    }

    private static void assertCountQueryForCellConfigurationHistory(final int expectedCount, final String sqlQuery) {
        assertCountQuery(expectedCount, sqlQuery, getFlmJdbcConnection(), getFlmJdbcProperties());
    }

    private static boolean verifyTableColumns(final String tableName, final List<String> expectedColumns) {
        try (final Connection connection = DriverManager.getConnection(getFlmJdbcConnection(), getFlmJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM %s LIMIT 1", tableName))) {

            for (final String column : expectedColumns) {
                try{
                    resultSet.findColumn(column);
                } catch (final SQLException e) {
                    LOGGER.info(String.format("Table column %s cannot be found.", column), e);
                    return false;
                }
            }

            return true;
        } catch (final SQLException e) {
            LOGGER.error("Unable to verify table columns.", e);
            return false;
        }
    }
}

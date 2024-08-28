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
import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getAllColumnNamesForCellConfigHistory;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class CellConfigurationViewVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(CellConfigurationViewVerifier.class);
    private static final String CELL_CONFIGURATION_VIEW = "cell_configuration_view";
    private static final List<String> EXPECTED_COLUMNS_OF_CELL_CONFIGURATION_VIEW = Arrays.asList(getAllColumnNamesForCellConfigHistory().split(",", -1));

    private CellConfigurationViewVerifier() {
    }

    public static void verifyCellConfigurationViewColumns() {
        assertTrue(verifyCellConfigurationViewColumns(CELL_CONFIGURATION_VIEW, EXPECTED_COLUMNS_OF_CELL_CONFIGURATION_VIEW));
    }

    private static boolean verifyCellConfigurationViewColumns(final String tableName, final List<String> expectedColumns) {
        try (final Connection connection = DriverManager.getConnection(getFlmJdbcConnection(), getFlmExporterJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM %s LIMIT 1", tableName))) {

            for (final String column : expectedColumns) {
                try{
                    resultSet.findColumn(column);
                    LOGGER.info("Column name: {}", column);
                } catch (final SQLException e) {
                    LOGGER.info(String.format("Table column %s cannot be found.", column), e);
                    return false;
                }
            }

            return true;
        } catch (final SQLException e) {
            LOGGER.error("Unable to verify view columns.", e);
            return false;
        }
    }

    private static Properties getFlmExporterJdbcProperties() {
        final Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty("user", getEnvironmentValue("FLM_EXPORT_USER"));
        jdbcProperties.setProperty("password", getEnvironmentValue("FLM_EXPORT_PASSWORD"));
        jdbcProperties.setProperty("driver", getEnvironmentValue("FLM_SERVICE_DB_DRIVER"));
        return jdbcProperties;
    }
}

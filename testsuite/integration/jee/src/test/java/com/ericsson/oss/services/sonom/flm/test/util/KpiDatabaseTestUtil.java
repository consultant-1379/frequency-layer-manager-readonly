/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.test.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;

/**
 * Utility class to interact with the KPI-Serice database during Integration testing.
 */
public class KpiDatabaseTestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(KpiDatabaseTestUtil.class);

    private KpiDatabaseTestUtil() {
    }

    /**
     * Determines if a table within the Kpi-Service has the required number of rows
     * 
     * @param tableName
     *            A {@link String} of the table name to check
     * @return A {@link Boolean} true or false if the the required number of rows
     */
    public static boolean hasKpiTableRequiredAmountOfRows(final String tableName, final int requiredRowCount) {
        final String countNumberOfRowsQuery = String.format("SELECT COUNT(*) FROM \"%s\"", tableName);
        final String jdbcUrl = DatabaseProperties.getKpiServiceJdbcConnection();
        final Properties jdbcProperties = DatabaseProperties.getKpiServiceJdbcProperties();

        try (final Connection connection = DriverManager.getConnection(jdbcUrl, jdbcProperties);
                final Statement statement = connection.createStatement();
                final ResultSet countResultSet = statement.executeQuery(countNumberOfRowsQuery)) {

            countResultSet.next();
            final Integer count = countResultSet.getInt(1);

            LOGGER.info("Checking to see if the table '{}' currently has information stored. Current row count: {}", tableName, count);
            return count == requiredRowCount;
        } catch (final SQLException e) {
            LOGGER.error("Unable to determine if table '{}' is empty", tableName, e);
            return false;
        }
    }
}

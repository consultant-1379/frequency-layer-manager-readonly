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
package com.ericsson.oss.services.sonom.flm.dbrunners;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Class to run Sql Commands for Unit Tests.
 */
public abstract class UnitTestDatabaseRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitTestDatabaseRunner.class);

    public UnitTestDatabaseRunner() {
        setTestDbEnvironmentVariables();
    }

    /**
     * Method to set system environment variables related to Database Configuration.
     * <ul>
     * <li>A {@link Connection} URL</li>
     * <li>Database User</li>
     * <li>Database Password</li>
     * <li>Database Driver</li>
     * </ul>
     */
    protected abstract void setTestDbEnvironmentVariables();

    /**
     * Method to execute SQL commands.
     * 
     * @param sqlCommands
     *            A {@link List} of SQL commands in the form of {@link String}'s.
     */
    public void executeSqlCommands(final List<String> sqlCommands) {
        try (final Connection connection = DriverManager.getConnection(getConnectionUrl(), getUser(), getPassword())) {
            connection.setAutoCommit(false);

            for (final String sqlCommand : sqlCommands) {
                try (final PreparedStatement preparedStatement = connection.prepareStatement(sqlCommand)) {
                    preparedStatement.executeUpdate();
                }
            }
            connection.commit();
        } catch (final Exception e) {
            LOGGER.warn("Failed to execute SQL", e);
            fail("Unable to execute SQL. Failing Test. " + e.getMessage());
        }
    }

    /**
     * Get the database {@link Connection} URL.
     * 
     * @return A {@link String} defining the {@link Connection} URL.
     */
    protected abstract String getConnectionUrl();

    /**
     * Get the database user
     * 
     * @return A {@link String} defining the database user.
     */
    protected abstract String getUser();

    /**
     * Get the database password
     * 
     * @return A {@link String} defining the database password.
     */
    protected abstract String getPassword();
}

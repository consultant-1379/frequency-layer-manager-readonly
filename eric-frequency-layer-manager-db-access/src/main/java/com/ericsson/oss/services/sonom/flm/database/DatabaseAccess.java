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

package com.ericsson.oss.services.sonom.flm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.handlers.ResultHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;

/**
 * Class used to manage database access for FLM.
 */
public abstract class DatabaseAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAccess.class);

    /**
     * Executes a parameterized query.
     *
     * @param sqlQuery
     *            The query to execute.
     * @param resultHandler
     *            The {@link ResultHandler} to use to create the returned object.
     * @param <T>
     *            The type of object to be returned.
     * @param preparedStatementWithParameters
     *            Implementation of {@link PreparedStatementHandler} to create {@link PreparedStatement}
     * @return The object created from executing the query.
     * @throws SQLException
     *             In the case where the query cannot be executed.
     */
    public <T> T executeQuery(final String sqlQuery, final ResultHandler<T> resultHandler,
            final PreparedStatementHandler preparedStatementWithParameters)
            throws SQLException {
        try (final Connection connection = DriverManager.getConnection(getJdbcConnection(),
                getJdbcProperties());
                final PreparedStatement preparedStatement = preparedStatementWithParameters.create(connection, sqlQuery);
                final ResultSet resultSet = preparedStatement.executeQuery()) {

            return resultHandler.populate(resultSet);

        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error while executing the following parameterized query {}", sqlQuery, e);
            throw e;
        }
    }

    /**
     * Executes query. NOTE: Should only be used for simple selects, for more complex queries (e.g. where clause), use parameterized version.
     *
     * @param sqlQuery
     *            The query to execute.
     * @param resultHandler
     *            The {@link ResultHandler} to use to create the returned object.
     * @param <T>
     *            The type of object to be returned.
     * @return The object created from executing the query.
     * @throws SQLException
     *             In the case where the query cannot be executed.
     */
    public <T> T executeQuery(final String sqlQuery, final ResultHandler<T> resultHandler)
            throws SQLException {
        try (final Connection connection = DriverManager.getConnection(getJdbcConnection(),
                getJdbcProperties());
                final PreparedStatement preparedStatement =
                        connection.prepareStatement(sqlQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                final ResultSet resultSet = preparedStatement.executeQuery()) {

            return resultHandler.populate(resultSet);

        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error while executing the following query {}", sqlQuery, e);
            throw e;
        }
    }

    /**
     * Executes a parameterized update query.
     *
     * @param sqlQuery
     *            The query to execute.
     * @param parameters
     *            The parameter values referenced by the placeholders in {@code sqlQuery}. Substitution is based on iteration order.
     * @return The number of records updated after query execution (can be 0, as in the update didn't occur but no SQL error)
     * @throws SQLException
     *             In the case where the query cannot be executed.
     */
    public int executeUpdate(final String sqlQuery, final Object[] parameters) throws SQLException {
        try (final Connection connection = DriverManager.getConnection(getJdbcConnection(),
                getJdbcProperties());
                final PreparedStatement preparedStatement = preparedStatementWithParameters(parameters).create(connection, sqlQuery)) {
            return preparedStatement.executeUpdate();
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error while executing the following parameterized update query {}", sqlQuery, e);
            throw e;
        }
    }

    /**
     * Executes a parameterized insert query.
     *
     * @param sqlQuery
     *            The query to execute.
     * @param resultHandler
     *            The {@link ResultHandler} to use to handle the result of insert.
     * @param <T>
     *            The type of object to be returned.
     * @param parameters
     *            The parameter values referenced by the placeholders in {@code sqlQuery}. Substitution is based on iteration order.
     * @return The object created from executing the query.
     * @throws SQLException
     *             In the case where the query cannot be executed.
     */
    public <T> T executeInsert(final String sqlQuery, final ResultHandler<T> resultHandler,
            final Object[] parameters) throws SQLException {
        try (final Connection connection = DriverManager.getConnection(getJdbcConnection(),
                getJdbcProperties());
                final PreparedStatement preparedStatement = preparedStatementWithParameters(parameters).create(connection, sqlQuery);
                final ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultHandler.populate(resultSet);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error while executing the following parameterized insert query {}", sqlQuery, e);
            throw e;
        }
    }

    /**
     * Executes a parameterized batch insert query.
     *
     * @param sqlQuery
     *            The query to execute.
     * @param parametersList
     *            The list of parameter values referenced by the placeholders in {@code sqlQuery}. Substitution is based on iteration order.
     * @return The number of cells persisted to the database.
     * @throws SQLException
     *             In the case where the query cannot be executed.
     */
    public int executeBatchInsert(final String sqlQuery, final List<Object[]> parametersList) throws SQLException {
        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                for (final Object[] parameters : parametersList) {
                    for (int i = 1; i <= parameters.length; i++) {
                        preparedStatement.setObject(i, parameters[i - 1]);
                    }
                    preparedStatement.addBatch();
                }
                return preparedStatement;
            }
        };
        try (final Connection connection = DriverManager.getConnection(getJdbcConnection(), getJdbcProperties());
                final PreparedStatement preparedStatement = preparedStatementHandler.create(connection, sqlQuery)) {
            return preparedStatement.executeBatch().length;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error while executing the following parameterized insert query {}", sqlQuery, e);
            throw e;
        }
    }

    protected abstract Properties getJdbcProperties();

    protected abstract String getJdbcConnection();

    private PreparedStatementHandler preparedStatementWithParameters(final Object[] parameters) {
        return new PreparedStatementCreator() {
            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                for (int i = 1; i <= parameters.length; i++) {
                    preparedStatement.setObject(i, parameters[i - 1]);
                }
                return preparedStatement;
            }
        };
    }
}

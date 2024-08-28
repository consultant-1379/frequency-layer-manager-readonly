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
package com.ericsson.oss.services.sonom.flm.test.verification;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static com.ericsson.oss.services.sonom.common.env.DatabaseProperties.getFlmJdbcConnection;
import static com.ericsson.oss.services.sonom.common.env.DatabaseProperties.getFlmJdbcProperties;
import static com.ericsson.oss.services.sonom.common.test.sql.SqlAssertions.assertCountQuery;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.FLM_OPTIMIZATIONS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.FLM_EXECUTIONS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_OPTIMIZATION_ELEMENTS_SENT;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_OPTIMIZATION_ELEMENTS_RECEIVED;

public final class FlmOptimizationsVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlmOptimizationsVerifier.class);

    private static final String SELECT_QUERY = String.format("SELECT \"%s\" FROM \"%s\" WHERE \"%s\"=? AND " +
                    "\"%s\"=\"%s\"",
            NUM_OPTIMIZATION_ELEMENTS_RECEIVED, FLM_EXECUTIONS, ExecutionDbConstants.ID,
            NUM_OPTIMIZATION_ELEMENTS_SENT, NUM_OPTIMIZATION_ELEMENTS_RECEIVED);

    private FlmOptimizationsVerifier() {
    }

    /**
     * Function for checking if the num_optimization_elements_received is the same as the flm_optimization table row count
     * for the specific executionId
     * @param executionId
     */
    public static void assertCountOfFlmOptimizations(final String executionId) {
        final String COUNT_QUERY = String.format("SELECT COUNT(*) FROM \"%s\" WHERE \"%s\"='%s'",
        FLM_OPTIMIZATIONS, EXECUTION_ID, executionId);
        final Optional<Integer> expectedCount = querySentAndReceivedElementsForExecution(executionId);
        Assert.assertTrue(String.format("The sent and received messages should be the same for the " +
                "execution: {}", executionId), expectedCount.isPresent());
        LOGGER.info("numberOfOptimizationElementsReceived {}", expectedCount.get());
        assertCountQuery(expectedCount.get(), COUNT_QUERY, getFlmJdbcConnection(), getFlmJdbcProperties());
    }

    /**
     * Function for checking if the num_optimization_elements_sent is the same as the num_optimization_elements_received
     * @param executionId
     */
    public static void assertSentAndReceivedMessagesCountIsEqual(final String executionId) {
        Assert.assertTrue(String.format("The sent and received messages should be the same for the execution: %s", executionId),
                          querySentAndReceivedElementsForExecution(executionId).isPresent());
    }

    /**
     * Query the flm executions DB for num_optimization_elements_received where the following holds:
     * id = executionId
     * num_optimization_elements_sent = num_optimization_elements_received
     * @param executionId
     * @return an optional containing the num_optimization_elements_received or empty if no rows returned
     */
    private static Optional<Integer> querySentAndReceivedElementsForExecution(final String executionId) {
        Optional<Integer> numberOfOptimizationElementsReceived;
        try (final Connection connection = DriverManager.getConnection(getFlmJdbcConnection(), getFlmJdbcProperties());
             final PreparedStatement statement = connection.prepareStatement(SELECT_QUERY)) {
            statement.setString(1, executionId);
            LOGGER.debug("The prepared statement: {}",statement.toString());
            try (final ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                numberOfOptimizationElementsReceived = Optional.of(resultSet.getInt(1));
            }
        } catch (final SQLException e) {
            LOGGER.debug("Couldn't find matching row in database.");
            LOGGER.debug("SQL State: {}",e.getSQLState());
            LOGGER.debug("SQL Message: {}",e.getMessage());
            LOGGER.debug("SQL Error code: {}",e.getErrorCode());
            numberOfOptimizationElementsReceived = Optional.empty();
        }
        return numberOfOptimizationElementsReceived;
    }
}
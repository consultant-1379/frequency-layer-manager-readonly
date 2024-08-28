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
package com.ericsson.oss.services.sonom.flm.database.optimization;

import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.CREATED;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.EMPTY_LBQ_TO_FILTER;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.FLM_OPTIMIZATIONS;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.LBQ;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.NUMBER_OF_SECTORS;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.SECTOR_ID;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.handlers.PolicyOutputEventCountHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.PolicyOutputEventListHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.google.gson.Gson;

import io.vavr.CheckedFunction0;

/**
 * Implementation of {@link OptimizationsDao}.
 */
public class OptimizationsDaoImpl implements OptimizationsDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptimizationsDaoImpl.class);
    private static final String FAILED_TO_EXECUTE_QUERY = "Failed to execute query: {} - {}";
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    private static final String DUPLICATE_KEY_VALUE_ERROR_CODE = "23505";
    private static final Gson GSON = new Gson();
    private final DatabaseRetry databaseRetry;
    private DatabaseAccess databaseAccess = new FlmDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing

    public OptimizationsDaoImpl(final int maxRetryAttempts, final int retryWaitDurationInSec) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDurationInSec);
    }

    @Override
    public Integer getNumberOfPolicyOutputEvents(final String executionId) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(executionId, "Retrieving the number of policy output events"));
        }
        final CheckedFunction0<Integer> policyOutputEventsForExecutionId = () -> retrievePolicyOutputEventNumberForExecutionId(executionId);
        return databaseRetry.executeWithRetryAttempts(policyOutputEventsForExecutionId);
    }

    @Override
    public List<PolicyOutputEvent> getOptimizations(final String executionId) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(executionId, "Retrieving PolicyOutputEvents"));
        }
        final CheckedFunction0<List<PolicyOutputEvent>> policyOutputEventsForExecutionId = () -> retrievePolicyOutputEventsForExecutionId(
                executionId);
        return databaseRetry.executeWithRetryAttempts(policyOutputEventsForExecutionId);
    }

    @Override
    public List<PolicyOutputEvent> getOptimizationsFiltered(final String executionId) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(executionId, "Retrieving filtered PolicyOutputEvents"));
        }
        final CheckedFunction0<List<PolicyOutputEvent>> policyOutputEventsForExecutionId = () -> retrieveFilteredPolicyOutputEventsForExecutionId(
                executionId);
        return databaseRetry.executeWithRetryAttempts(policyOutputEventsForExecutionId);
    }

    @Override
    public Integer insertOptimization(final PolicyOutputEvent policyOutputEvent) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(),
                                                        policyOutputEvent.getSectorId(),
                                                        String.format("Inserting PolicyOutputEvent: %s", policyOutputEvent)));
        }
        final CheckedFunction0<Integer> policyOutputEventId = () -> savePolicyOutputEvent(policyOutputEvent);
        return databaseRetry.executeWithRetryAttempts(policyOutputEventId);
    }

    private Integer retrievePolicyOutputEventNumberForExecutionId(final String executionId) throws SQLException {
        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            private static final int EXECUTION_ID_PARAM = 1;

            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(EXECUTION_ID_PARAM, executionId);
                return preparedStatement;
            }
        };
        final String query = String.format("SELECT COUNT(*) AS %s FROM %s where %s = ? ",
                NUMBER_OF_SECTORS, FLM_OPTIMIZATIONS, EXECUTION_ID);
        return executeRetrievePolicyOutputEventNumberQuery(preparedStatementHandler, query);
    }

    private List<PolicyOutputEvent> retrievePolicyOutputEventsForExecutionId(final String executionId) throws SQLException {
        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            private static final int EXECUTION_ID_PARAM = 1;

            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(EXECUTION_ID_PARAM, executionId);
                return preparedStatement;
            }
        };
        final String query = String.format("SELECT * FROM %s where %s = ? ",
                FLM_OPTIMIZATIONS, EXECUTION_ID);
        return executeRetrieveQuery(executionId, preparedStatementHandler, query);
    }

    private List<PolicyOutputEvent> retrieveFilteredPolicyOutputEventsForExecutionId(final String executionId) throws SQLException {
        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            private static final int EXECUTION_ID_PARAM = 1;

            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(EXECUTION_ID_PARAM, executionId);
                return preparedStatement;
            }
        };
        final String query = String.format("SELECT * FROM %s where %s = ? AND lbq::TEXT != '%s'",
                FLM_OPTIMIZATIONS, EXECUTION_ID, EMPTY_LBQ_TO_FILTER);
        return executeRetrieveQuery(executionId, preparedStatementHandler, query);
    }

    private Integer savePolicyOutputEvent(final PolicyOutputEvent policyOutputEvent) throws SQLException {
        final Object[] parameters = new Object[] {
                policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(), createJsonFromLoadBalancingQuanta(policyOutputEvent),
                new Timestamp(System.currentTimeMillis())
        };
        final String query = String.format(
                "INSERT INTO %s (%s,%s,%s,%s) values (?,?,?::json,?)",
                FLM_OPTIMIZATIONS, EXECUTION_ID, SECTOR_ID, LBQ, CREATED);

        return executeInsertQuery(query, parameters, policyOutputEvent);
    }

    private Integer executeRetrievePolicyOutputEventNumberQuery(final PreparedStatementHandler preparedStatementHandler, final String query)
            throws SQLException {
        try {
            return databaseAccess.executeQuery(query,
                    new PolicyOutputEventCountHandler(), preparedStatementHandler);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.error(FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private List<PolicyOutputEvent> executeRetrieveQuery(final String executionId, final PreparedStatementHandler preparedStatementHandler,
            final String query) throws SQLException {
        try {
            final List<PolicyOutputEvent> policyOutputEvents = databaseAccess.executeQuery(query,
                    new PolicyOutputEventListHandler(), preparedStatementHandler);
            if (policyOutputEvents.isEmpty()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(LoggingFormatter.formatMessage(executionId,
                        String.format("No Policy Output Events are available for given %s", EXECUTION_ID)));
                }
                return Collections.emptyList();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(LoggingFormatter.formatMessage(executionId,
                        String.format("Retrieved Policy Output Events for given %s", EXECUTION_ID)));
                }
                return policyOutputEvents;
            }
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.error(FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private Integer executeInsertQuery(final String query, final Object[] parameters, final PolicyOutputEvent policyOutputEvent) throws SQLException {
        try {
            return databaseAccess.executeUpdate(query, parameters);
        } catch (final SQLIntegrityConstraintViolationException e) { //NOSONAR Exception suitably logged
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(),
                            policyOutputEvent.getSectorId(),
                            String.format("Integrity Constraint Violation. The optimization record has already been created: %s",
                            policyOutputEvent)));
            }
            return 0;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            if (DUPLICATE_OBJECT_ERROR_CODE.equals(e.getSQLState()) || DUPLICATE_KEY_VALUE_ERROR_CODE.equals(e.getSQLState())) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(),
                                policyOutputEvent.getSectorId(),
                                String.format("Duplicate Object Error. The optimization record has already been created: %s",
                                policyOutputEvent)));
                }
                return 0;
            } else {
                LOGGER.error(FAILED_TO_EXECUTE_QUERY, query, e.getMessage());
                throw e;
            }
        }
    }

    private String createJsonFromLoadBalancingQuanta(final PolicyOutputEvent policyOutputEvent) {
        if (policyOutputEvent.getLoadBalancingQuanta() == null) {
            return null;
        }
        return GSON.toJson(policyOutputEvent.getLoadBalancingQuanta());
    }
}

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
package com.ericsson.oss.services.sonom.flm.database.execution;

import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.ADDITIONAL_EXECUTION_INFORMATION;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.CALCULATION_ID;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.CONFIGURATION_ID;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.CUSTOMIZED_DEFAULT_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.CUSTOMIZED_GLOBAL_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.ENABLE_PA;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.EXCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.FLM_EXECUTIONS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.FULL_EXECUTION;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.GROUPS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.ID;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.INCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_CHANGES_NOT_WRITTEN_TO_CM_DB;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_CHANGES_WRITTEN_TO_CM_DB;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_OPTIMIZATION_ELEMENTS_RECEIVED;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_OPTIMIZATION_ELEMENTS_SENT;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_OPTIMIZATION_LBQS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.OPEN_LOOP;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.RETRY_ATTEMPTS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.SCHEDULE;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.START_TIME;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.STATE;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.STATE_MODIFIED_TIME;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.WEEKEND_DAYS;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.handlers.ExecutionHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.ExecutionInsertHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.ExecutionSummaryHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.database.utils.QueryUtils;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.google.gson.Gson;

import io.vavr.CheckedFunction0;

/**
 * Class to implement methods of {@link ExecutionDao}.
 */
public class ExecutionDaoImpl implements ExecutionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionDaoImpl.class);
    private static final String ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY = "Failed to execute query: {}";
    private static final Gson GSON = new Gson();

    private final DatabaseRetry databaseRetry;
    private DatabaseAccess databaseAccess = new FlmDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing

    public ExecutionDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public String insert(final Execution execution) throws SQLException {
        final CheckedFunction0<String> executionsInStateFunctionWithRetry = () -> performInsert(execution);
        return databaseRetry.executeWithRetryAttempts(executionsInStateFunctionWithRetry);
    }

    @Override
    public int update(final Execution execution) throws SQLException {
        final CheckedFunction0<Integer> executionsInStateFunctionWithRetry = () -> performUpdate(execution);
        return databaseRetry.executeWithRetryAttempts(executionsInStateFunctionWithRetry);
    }

    @Override
    public Execution get(final String id) throws SQLException {
        final CheckedFunction0<Execution> executionsInStateFunctionWithRetry = () -> getExecution(id);
        return databaseRetry.executeWithRetryAttempts(executionsInStateFunctionWithRetry);
    }

    @Override
    public Execution getWithoutRetry(final String id) throws SQLException {
        return getExecution(id);
    }

    @Override
    public List<ExecutionSummary> getAllSummaries() throws SQLException {
        final CheckedFunction0<List<ExecutionSummary>> getAllFunctionWithRetry = this::getAllExecutionSummaries;
        return databaseRetry.executeWithRetryAttempts(getAllFunctionWithRetry);
    }

    @Override
    public List<Execution> getExecutionsInStates(final ExecutionState... executionStates) throws SQLException {
        final CheckedFunction0<List<Execution>> executionsInStateFunctionWithRetry = () -> getExecutionInStates(executionStates);
        return databaseRetry.executeWithRetryAttempts(executionsInStateFunctionWithRetry);
    }

    private String performInsert(final Execution execution) throws SQLException {
        final Object[] parameters = new Object[] {
                execution.getId(), execution.getState().toString(), execution.getStateModifiedTime(),
                execution.getAdditionalExecutionInformation(),
                execution.getConfigurationId(), execution.getStartTime(), execution.getSchedule(),
                execution.getRetryAttempts(), execution.getCalculationId(),
                GSON.toJson(execution.getCustomizedGlobalSettings()), GSON.toJson(execution.getCustomizedDefaultSettings()),
                GSON.toJson(execution.getGroups()), execution.getNumSectorsToEvaluateForOptimization(), execution.getNumOptimizationElementsSent(),
                execution.getNumOptimizationElementsReceived(), execution.getNumOptimizationLbqs(), execution.getNumChangesWrittenToCmDb(),
                execution.getNumChangesNotWrittenToCmDb(), execution.isOpenLoop(),
                GSON.toJson(execution.getInclusionList()), GSON.toJson(execution.getExclusionList()), execution.getWeekendDays(),
                execution.isEnablePA(), execution.isFullExecution()
        };
        final String query = String.format("insert into %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, " +
                "%s, %s, %s, %s) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING %s ;", FLM_EXECUTIONS, ID,
                STATE, STATE_MODIFIED_TIME, ADDITIONAL_EXECUTION_INFORMATION,
                CONFIGURATION_ID, START_TIME, SCHEDULE, RETRY_ATTEMPTS, CALCULATION_ID,
                CUSTOMIZED_GLOBAL_SETTINGS, CUSTOMIZED_DEFAULT_SETTINGS, GROUPS, NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION,
                NUM_OPTIMIZATION_ELEMENTS_SENT, NUM_OPTIMIZATION_ELEMENTS_RECEIVED, NUM_OPTIMIZATION_LBQS, NUM_CHANGES_WRITTEN_TO_CM_DB,
                NUM_CHANGES_NOT_WRITTEN_TO_CM_DB, OPEN_LOOP, INCLUSION_LIST, EXCLUSION_LIST, WEEKEND_DAYS,
                ENABLE_PA, FULL_EXECUTION, ID);
        LOGGER.debug("Executing insert query, {}", query);
        try {
            return databaseAccess.executeInsert(query, new ExecutionInsertHandler(), parameters);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private int performUpdate(final Execution execution) throws SQLException {
        final Object[] parameters = new Object[] {
                execution.getState().toString(), execution.getStateModifiedTime(), execution.getAdditionalExecutionInformation(),
                execution.getSchedule(),
                execution.getRetryAttempts(), execution.getCalculationId(), GSON.toJson(execution.getCustomizedGlobalSettings()),
                GSON.toJson(execution.getCustomizedDefaultSettings()), GSON.toJson(execution.getGroups()),
                execution.getNumSectorsToEvaluateForOptimization(), execution.getNumOptimizationElementsSent(),
                execution.getNumOptimizationElementsReceived(), execution.getNumOptimizationLbqs(), execution.getNumChangesWrittenToCmDb(),
                execution.getNumChangesNotWrittenToCmDb(), execution.isOpenLoop(),
                GSON.toJson(execution.getInclusionList()), GSON.toJson(execution.getExclusionList()),
                execution.getWeekendDays(), execution.isEnablePA(), execution.isFullExecution(), execution.getId()
        };
        final String query = String.format(
                "update %s set %s = ? , %s = ?, %s = ?, %s = ?, %s = ?, %s =?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?," +
                        " %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ? where %s = ? ;",
                FLM_EXECUTIONS, STATE, STATE_MODIFIED_TIME, ADDITIONAL_EXECUTION_INFORMATION, SCHEDULE, RETRY_ATTEMPTS,
                CALCULATION_ID, CUSTOMIZED_GLOBAL_SETTINGS, CUSTOMIZED_DEFAULT_SETTINGS, GROUPS, NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION,
                NUM_OPTIMIZATION_ELEMENTS_SENT, NUM_OPTIMIZATION_ELEMENTS_RECEIVED, NUM_OPTIMIZATION_LBQS, NUM_CHANGES_WRITTEN_TO_CM_DB,
                NUM_CHANGES_NOT_WRITTEN_TO_CM_DB, OPEN_LOOP, INCLUSION_LIST, EXCLUSION_LIST, WEEKEND_DAYS,
                ENABLE_PA, FULL_EXECUTION, ID);
        LOGGER.debug("Executing parameterized update query, {}", query);
        try {
            return databaseAccess.executeUpdate(query, parameters);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private Execution getExecution(final String id) throws SQLException {

        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            private static final int EXECUTION_ID_PARAMETER = 1;

            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(EXECUTION_ID_PARAMETER, id);
                return preparedStatement;
            }
        };

        final String query = String.format("SELECT * FROM %s where %s = ? ", FLM_EXECUTIONS, ID);
        try {
            final List<Execution> executions = databaseAccess.executeQuery(query,
                    new ExecutionHandler(), preparedStatementHandler);

            if (executions.isEmpty()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(LoggingFormatter.formatMessage(id, "No execution available"));
                }
                return null;
            }
            return executions.get(0);

        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private List<ExecutionSummary> getAllExecutionSummaries() throws SQLException {
        final String query = String.format("SELECT id, configuration_id, start_time, " +
                "state_modified_time, state, schedule, open_loop, full_execution FROM %s", FLM_EXECUTIONS);
        try {
            final List<ExecutionSummary> executions = databaseAccess.executeQuery(query,
                    new ExecutionSummaryHandler());
            LOGGER.debug("Retrieved executions {}", executions);
            return executions;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private List<Execution> getExecutionInStates(final ExecutionState... executionStates) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Retrieving executions in state: {}", Arrays.toString(executionStates)); // toString here avoids compiler warning
        }

        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                for (int i = 0; i < executionStates.length; i++) {
                    preparedStatement.setString(i + 1, executionStates[i].toString());
                }
                return preparedStatement;
            }
        };

        final String query;

        if (executionStates.length == 1) {
            query = String.format("SELECT %s FROM %s WHERE %s = ? ORDER BY %s, %s DESC;", ExecutionDbConstants.getAllColumnNames(),
                    FLM_EXECUTIONS,
                    STATE,
                    CONFIGURATION_ID,
                    START_TIME);
        } else {
            final String inValues = QueryUtils.inQueryBuilder(executionStates.length);
            query = String.format("SELECT %s FROM %s WHERE %s in %s ORDER BY %s, %s DESC;", ExecutionDbConstants.getAllColumnNames(),
                    FLM_EXECUTIONS,
                    STATE,
                    inValues,
                    CONFIGURATION_ID,
                    START_TIME);
        }
        try {
            final List<Execution> executions = databaseAccess.executeQuery(query, new ExecutionHandler(), preparedStatementHandler);
            LOGGER.debug("executions: {}", executions);
            return executions;

        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }
}

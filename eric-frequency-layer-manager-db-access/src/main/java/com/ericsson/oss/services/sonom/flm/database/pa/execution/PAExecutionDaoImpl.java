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

package com.ericsson.oss.services.sonom.flm.database.pa.execution;

import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.FLM_EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.ID;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.NUM_PA_POLICY_INPUT_EVENTS_SENT;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_EXECUTIONS;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_WINDOW;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_WINDOW_END_TIME;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_WINDOW_START_TIME;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.SCHEDULE;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.STATE;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.STATE_MODIFIED_TIME;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.getAllColumnNames;
import static java.util.stream.Collectors.groupingBy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.pa.handlers.PAExecutionHandler;
import com.ericsson.oss.services.sonom.flm.database.pa.handlers.PAExecutionInsertHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.database.utils.QueryUtils;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

import io.vavr.CheckedFunction0;

/**
 * Class to implement methods of {@link PAExecutionDao}.
 */
public class PAExecutionDaoImpl implements PAExecutionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(PAExecutionDaoImpl.class);
    private static final String ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY = "Failed to execute query: {}";

    private final DatabaseRetry databaseRetry;
    private DatabaseAccess databaseAccess = new FlmDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing

    public PAExecutionDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public String insert(final PAExecution paExecution) throws SQLException {
        final CheckedFunction0<String> insertFunction = () -> performInsert(paExecution);
        return databaseRetry.executeWithRetryAttempts(insertFunction);
    }

    @Override
    public int update(final PAExecution paExecution) throws SQLException {
        final CheckedFunction0<Integer> updateFunction = () -> performUpdate(paExecution);
        return databaseRetry.executeWithRetryAttempts(updateFunction);
    }

    @Override
    public List<PAExecution> getPAExecutions(final String flmExecutionId) throws SQLException {
        final CheckedFunction0<List<PAExecution>> getFunction = () -> readPaExecutions(flmExecutionId);
        return databaseRetry.executeWithRetryAttempts(getFunction);
    }

    @Override
    public Map<String, List<PAExecution>> getPAExecutionsInStates(final PAExecutionState... paExecutionStates) throws SQLException {
        final CheckedFunction0<Map<String, List<PAExecution>>> getPAExecutionInStates = () -> readPAExecutionWithStates(paExecutionStates);
        return databaseRetry.executeWithRetryAttempts(getPAExecutionInStates);
    }

    private String performInsert(final PAExecution paExecution) throws SQLException {
        final Object[] parameters = new Object[]{
                paExecution.getId(),
                paExecution.getSchedule(),
                paExecution.getPaWindow(),
                paExecution.getPaWindowStartTime(),
                paExecution.getPaWindowEndTime(),
                paExecution.getState().toString(),
                new Timestamp(System.currentTimeMillis()),
                paExecution.getFlmExecutionId(),
                paExecution.getNumPaPolicyInputEventsSent()
        };

        final String query = String.format("insert into %s (%s) values (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING %s;",
                PA_EXECUTIONS, getAllColumnNames(), ID);
        LOGGER.debug("Executing insert query, {}", query);

        try {
            return databaseAccess.executeInsert(query, new PAExecutionInsertHandler(), parameters);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private int performUpdate(final PAExecution paExecution) throws SQLException {
        final Object[] parameters = new Object[]{
                paExecution.getPaWindow(),
                paExecution.getSchedule(),
                paExecution.getPaWindowStartTime(),
                paExecution.getPaWindowEndTime(),
                paExecution.getState().toString(),
                new Timestamp(System.currentTimeMillis()),
                paExecution.getFlmExecutionId(),
                paExecution.getNumPaPolicyInputEventsSent(),
                paExecution.getId()
        };
        final String query = String.format("update %s set %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ? where %s = ?;",
                PA_EXECUTIONS,
                PA_WINDOW,
                SCHEDULE,
                PA_WINDOW_START_TIME,
                PA_WINDOW_END_TIME,
                STATE,
                STATE_MODIFIED_TIME,
                FLM_EXECUTION_ID,
                NUM_PA_POLICY_INPUT_EVENTS_SENT,
                ID);
        LOGGER.debug("Executing parameterized update query, {}", query);
        try {
            return databaseAccess.executeUpdate(query, parameters);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private List<PAExecution> readPaExecutions(final String flmExecutionId) throws SQLException {

        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            private static final int FLM_EXECUTION_ID_PARAMETER = 1;

            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(FLM_EXECUTION_ID_PARAMETER, flmExecutionId);
                return preparedStatement;
            }
        };

        final String query = String.format("SELECT * FROM %s where %s = ? ", PA_EXECUTIONS, FLM_EXECUTION_ID);
        try {
            final List<PAExecution> executions = databaseAccess.executeQuery(query, new PAExecutionHandler(), preparedStatementHandler);

            if (executions.isEmpty()) {
                LOGGER.info("No executions available for flm_execution_id {}", flmExecutionId);
                return Collections.emptyList();
            }

            LOGGER.info("Retrieved {} PA executions, for flm_execution_id {}", executions.size(), flmExecutionId);
            LOGGER.debug("PA executions Retrieved for flm_execution_id {}: \n{}", executions, flmExecutionId);
            return executions;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private Map<String, List<PAExecution>> readPAExecutionWithStates(final PAExecutionState[] paExecutionStates) throws SQLException {

        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                for (int i = 0; i < paExecutionStates.length; i++) {
                    preparedStatement.setString(i + 1, paExecutionStates[i].toString());
                }
                return preparedStatement;
            }
        };

        final String inValues = QueryUtils.inQueryBuilder(paExecutionStates.length);
        final String query = String.format("SELECT %s FROM %s WHERE %s IN %s ORDER BY %s, %s;", getAllColumnNames(),
                PA_EXECUTIONS,
                STATE,
                inValues,
                FLM_EXECUTION_ID,
                PA_WINDOW);

        try {
            final List<PAExecution> paExecutions = databaseAccess.executeQuery(query, new PAExecutionHandler(), preparedStatementHandler);
            LOGGER.debug("PA executions: {}", paExecutions);
            
            return paExecutions.stream().collect(groupingBy(PAExecution::getFlmExecutionId));
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }
}

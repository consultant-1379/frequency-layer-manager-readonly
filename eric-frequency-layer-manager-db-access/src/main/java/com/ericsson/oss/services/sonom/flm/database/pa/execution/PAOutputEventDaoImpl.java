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

import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_OUTPUT_EVENTS;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.getAllColumnNamesForOutputEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.pa.handlers.PAOutputEventHandler;
import com.ericsson.oss.services.sonom.flm.database.pa.handlers.PAOutputEventInsertHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;
import com.google.gson.Gson;

import io.vavr.CheckedFunction0;

/**
 * Class to implement methods of {@link PAOutputEventDao}.
 */
public class PAOutputEventDaoImpl implements PAOutputEventDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(PAOutputEventDaoImpl.class);
    private static final String ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY = "Failed to execute query: {}";
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    private static final String DUPLICATE_OBJECT_KEY_VALUE_ERROR_CODE = "23505";
    private static final Gson GSON = new Gson();

    private final DatabaseRetry databaseRetry;
    private DatabaseAccess databaseAccess = new FlmDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing

    public PAOutputEventDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public String insertPaPolicyOutputEvent(final PaPolicyOutputEvent paPolicyOutputEvent) throws SQLException {
        final CheckedFunction0<String> insertEventFunction = () -> performEventInsert(paPolicyOutputEvent);
        return databaseRetry.executeWithRetryAttempts(insertEventFunction);
    }

    @Override
    public List<PaPolicyOutputEvent> getPaPolicyOutputEventById(final String paExecutionId) throws SQLException {
        final CheckedFunction0<List<PaPolicyOutputEvent>> getFunction = () -> readPaPolicyEventsById(paExecutionId);
        return databaseRetry.executeWithRetryAttempts(getFunction);

    }

    private String performEventInsert(final PaPolicyOutputEvent paPolicyOutputEvent) throws SQLException {
        final Object[] parameters = new Object[] {
                paPolicyOutputEvent.getFlmExecutionId(),
                paPolicyOutputEvent.getPaExecutionId(),
                paPolicyOutputEvent.getPaWindow(),
                Long.parseLong(paPolicyOutputEvent.getSector().getSectorId()),
                GSON.toJson(paPolicyOutputEvent.getDegradationStatus())
        };

        final String query = String.format("INSERT INTO %s (%s) VALUES (?,?,?,?,?::json) RETURNING %s",
                PA_OUTPUT_EVENTS, getAllColumnNamesForOutputEvent(), PA_EXECUTION_ID);
        LOGGER.debug("Executing insert query, {}", query);

        try {
            return databaseAccess.executeInsert(query, new PAOutputEventInsertHandler(), parameters);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            if (DUPLICATE_OBJECT_ERROR_CODE.equals(e.getSQLState()) || DUPLICATE_OBJECT_KEY_VALUE_ERROR_CODE.equals(e.getSQLState())) {
                LOGGER.warn("The PA Policy output event has already been recorded {}", paPolicyOutputEvent);
                throw e;
            }
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query);
            throw e;
        }

    }

    private List<PaPolicyOutputEvent> readPaPolicyEventsById(final String paExecutionId) throws SQLException {

        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {

            private static final int PA_EXECUTION_ID_PARAMETER = 1;

            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(PA_EXECUTION_ID_PARAMETER, paExecutionId);
                return preparedStatement;
            }
        };
        final String query = String.format("SELECT * FROM %s WHERE %s = ? ", PA_OUTPUT_EVENTS, PA_EXECUTION_ID);
        try {
            final List<PaPolicyOutputEvent> paPolicyOutputEvents = databaseAccess.executeQuery(query,
                    new PAOutputEventHandler(), preparedStatementHandler);

            if (paPolicyOutputEvents.isEmpty()) {
                LOGGER.info("No output event available for PA Execution ID {}", paExecutionId);
                return new ArrayList<>();
            } else {
                LOGGER.info("Retrieved {} PA Policy Output Events for PA Execution ID {}", paPolicyOutputEvents.size(), paExecutionId);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("PA Policy Output Event Retrieved for PA Execution ID {}: {}", paExecutionId, paPolicyOutputEvents);
            }
            return paPolicyOutputEvents;

        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

}

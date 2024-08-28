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
package com.ericsson.oss.services.sonom.flm.database.lbdar;

import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.FLM_LBDAR;
import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.LEAKAGE_CELLS;
import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.SECTOR_ID;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.handlers.LeakageCellSetHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.google.gson.Gson;

import io.vavr.CheckedFunction0;

/**
 * Implementation of {@link LbdarDao}.
 */
public class LbdarDaoImpl implements LbdarDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(LbdarDaoImpl.class);
    private static final String FAILED_TO_EXECUTE_QUERY = "Failed to execute query: {} - {}";
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    private static final String DUPLICATE_KEY_VALUE_ERROR_CODE = "23505";
    private static final Gson GSON = new Gson();

    private final DatabaseRetry databaseRetry;
    private DatabaseAccess databaseAccess = new FlmDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing

    public LbdarDaoImpl(final int maxRetryAttempts, final int retryWaitDurationInSec) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDurationInSec);
    }

    @Override
    public Integer insertLeakageCells(final String executionId, final Long sectorId, final Set<LeakageCell> leakageCells) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(executionId,
                    sectorId,
                    String.format("Inserting leakage cells %s", leakageCells)));
        }
        final CheckedFunction0<Integer> policyOutputEventId = () -> savePolicyOutputEvent(executionId, sectorId, leakageCells);
        return databaseRetry.executeWithRetryAttempts(policyOutputEventId);
    }

    @Override
    public Set<LeakageCell> getLeakageCells(final String executionId, final Long sectorId) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(executionId, sectorId, "Retrieving leakage cells"));
        }
        final CheckedFunction0<Set<LeakageCell>> leakageCellForExecutionAndSector = () -> retrieveLeakageCells(
                executionId, sectorId);
        return databaseRetry.executeWithRetryAttempts(leakageCellForExecutionAndSector);
    }

    private Integer savePolicyOutputEvent(final String executionId, final Long sectorId, final Set<LeakageCell> leakageCells) throws SQLException {
        final Object[] parameters = new Object[] {
                executionId, sectorId, createJsonFromLeakageCells(leakageCells)
        };
        final String query = String.format(
                "INSERT INTO %s (%s,%s,%s) values (?,?,?::json)",
                FLM_LBDAR, EXECUTION_ID, SECTOR_ID, LEAKAGE_CELLS);

        return executeInsertQuery(query, parameters, executionId, sectorId, leakageCells);
    }

    private Integer executeInsertQuery(final String query, final Object[] parameters, final String executionId, final Long sectorId,
            final Set<LeakageCell> leakageCells) throws SQLException {
        try {
            return databaseAccess.executeUpdate(query, parameters);
        } catch (final SQLIntegrityConstraintViolationException e) { //NOSONAR Exception suitably logged
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(executionId,
                        sectorId,
                        String.format("Integrity Constraint Violation. The cell leakage record has already been created: %s",
                                leakageCells)));
            }
            return 0;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            if (DUPLICATE_OBJECT_ERROR_CODE.equals(e.getSQLState()) || DUPLICATE_KEY_VALUE_ERROR_CODE.equals(e.getSQLState())) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(LoggingFormatter.formatMessage(executionId,
                            sectorId,
                            String.format("Duplicate Object Error. The cell leakage record has already been created: %s",
                                    leakageCells)));
                }
                return 0;
            } else {
                LOGGER.error(FAILED_TO_EXECUTE_QUERY, query, e.getMessage());
                throw e;
            }
        }
    }

    private String createJsonFromLeakageCells(final Set<LeakageCell> leakageCells) {
        if (leakageCells == null) {
            return GSON.toJson(Collections.emptyList());
        }
        return GSON.toJson(leakageCells.toArray());
    }

    private Set<LeakageCell> retrieveLeakageCells(final String executionId, final Long sectorId) throws SQLException {
        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            private static final int EXECUTION_ID_PARAM = 1;
            private static final int SECTOR_ID_PARAM = 2;

            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(EXECUTION_ID_PARAM, executionId);
                preparedStatement.setLong(SECTOR_ID_PARAM, sectorId);
                return preparedStatement;
            }
        };
        final String query = String.format("SELECT %s FROM %s where %s = ? and %s = ?",
                LEAKAGE_CELLS, FLM_LBDAR, EXECUTION_ID, SECTOR_ID);
        return executeRetrieveQuery(executionId, sectorId, preparedStatementHandler, query);
    }

    private Set<LeakageCell> executeRetrieveQuery(final String executionId, final Long sectorId,
            final PreparedStatementHandler preparedStatementHandler,
            final String query) throws SQLException {
        try {
            final List<LeakageCell> leakageCells = databaseAccess.executeQuery(query,
                    new LeakageCellSetHandler(), preparedStatementHandler);
            if (leakageCells.isEmpty()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(LoggingFormatter.formatMessage(executionId,
                            String.format("No leakage cells are available for given execution '%s' and sector '%d'", executionId, sectorId)));
                }
                return Collections.emptySet();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(LoggingFormatter.formatMessage(executionId,
                            String.format("Retrieved leakage cells for given execution '%s' and sector '%d'", executionId, sectorId)));
                }
                return new HashSet<>(leakageCells);
            }
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.error(FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }
}

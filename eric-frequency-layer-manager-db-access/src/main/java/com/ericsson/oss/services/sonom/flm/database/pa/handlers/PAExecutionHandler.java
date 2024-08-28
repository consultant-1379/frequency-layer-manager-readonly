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

package com.ericsson.oss.services.sonom.flm.database.pa.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.handlers.ResultHandler;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

/**
 * Creates a {@link List} of execution's represented as {@link PAExecution}.
 */
public class PAExecutionHandler implements ResultHandler<List<PAExecution>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PAExecutionHandler.class);

    private final PAExecutionDao paExecutionDao;

    /**
     * Creates a PAExecutionHandler instance.
     */
    public PAExecutionHandler() {
        paExecutionDao = new PAExecutionDaoImpl(1, 10);
    }

    /**
     * Creates a PAExecutionHandler instance with a PAExecutionDao instance given as input is used.
     * @param paExecutionDao
     *        {@link PAExecutionDao} instance
     */

    public PAExecutionHandler(final PAExecutionDao paExecutionDao) {
        this.paExecutionDao = paExecutionDao;
    }

    @Override
    public List<PAExecution> populate(final ResultSet resultSet) throws SQLException {
        final List<PAExecution> executions = new ArrayList<>();

        while (resultSet.next()) {
            final PAExecution execution = new PAExecution(
                    resultSet.getString(PAExecutionDbConstants.ID),
                    resultSet.getInt(PAExecutionDbConstants.PA_WINDOW),
                    resultSet.getString(PAExecutionDbConstants.SCHEDULE),
                    resultSet.getTimestamp(PAExecutionDbConstants.PA_WINDOW_START_TIME),
                    resultSet.getTimestamp(PAExecutionDbConstants.PA_WINDOW_END_TIME),
                    resultSet.getTimestamp(PAExecutionDbConstants.STATE_MODIFIED_TIME),
                    resultSet.getString(PAExecutionDbConstants.FLM_EXECUTION_ID));
            execution.setNumPaPolicyInputEventsSent(resultSet.getInt(PAExecutionDbConstants.NUM_PA_POLICY_INPUT_EVENTS_SENT));
            execution.setState(PAExecutionState.valueOf(resultSet.getString(PAExecutionDbConstants.STATE)));
            executions.add(execution);
        }
        LOGGER.debug("Populated list of executions: {}", executions);
        return executions;
    }

    /**
     * Get PA execution by flm execution id.
     *
     * @param flmExecutionId
     *      the id of the {@link Execution}
     * @return {@link List} of {@link PAExecution} containing all of the PA executions.
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    public List<PAExecution> getPAExecutions(final String flmExecutionId) throws SQLException {
        return paExecutionDao.getPAExecutions(flmExecutionId);
    }

}

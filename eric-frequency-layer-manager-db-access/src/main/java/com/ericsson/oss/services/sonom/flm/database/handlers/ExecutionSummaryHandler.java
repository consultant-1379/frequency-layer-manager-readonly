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
package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;

/**
 * Creates a {@link List} of execution summaries represented as {@link ExecutionSummary}.
 */
public class ExecutionSummaryHandler implements ResultHandler<List<ExecutionSummary>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionSummaryHandler.class);

    @Override
    public List<ExecutionSummary> populate(final ResultSet resultSet) throws SQLException {
        final List<ExecutionSummary> executions = new ArrayList<>();

        while (resultSet.next()) {
            final ExecutionSummary execution = new ExecutionSummary();
            execution.setId(resultSet.getString(ExecutionDbConstants.ID));
            execution.setConfigurationId(resultSet.getInt(ExecutionDbConstants.CONFIGURATION_ID));
            execution.setStartTime(resultSet.getTimestamp(ExecutionDbConstants.START_TIME));
            execution.setStateModifiedTime(resultSet.getTimestamp(ExecutionDbConstants.STATE_MODIFIED_TIME));
            execution.setState(ExecutionState.valueOf(resultSet.getString(ExecutionDbConstants.STATE)));
            execution.setSchedule(resultSet.getString(ExecutionDbConstants.SCHEDULE));
            execution.setOpenLoop(resultSet.getBoolean(ExecutionDbConstants.OPEN_LOOP));
            execution.setFullExecution(resultSet.getBoolean(ExecutionDbConstants.FULL_EXECUTION));
            executions.add(execution);
        }
        LOGGER.debug("Populated list of executions: {}", executions);
        return executions;
    }
}

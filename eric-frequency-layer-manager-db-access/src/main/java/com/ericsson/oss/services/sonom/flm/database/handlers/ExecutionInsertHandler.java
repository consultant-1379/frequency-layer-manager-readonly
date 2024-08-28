/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
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

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants;

/**
 * Handles the {@link ResultSet} for an inserted {@link com.ericsson.oss.services.sonom.flm.service.api.executions.Execution}.
 */
public class ExecutionInsertHandler implements ResultHandler<String> {

    /**
     * Gets the ID of the newly inserted {@link com.ericsson.oss.services.sonom.flm.service.api.executions.Execution} record from the
     * {@link ResultSet}.
     *
     * @param resultSet
     *            The {@link ResultSet} to retrieve the ID from.
     * @return The ID of the new {@link com.ericsson.oss.services.sonom.flm.service.api.executions.Execution}.
     * @throws SQLException
     *             In the case where an error occurs getting the value from the {@link ResultSet}
     */
    @Override
    public String populate(final ResultSet resultSet) throws SQLException {
        String executionId = "";
        while (resultSet.next()) {
            executionId = resultSet.getString(ExecutionDbConstants.ID);
        }
        return executionId;
    }
}

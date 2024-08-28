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

import com.ericsson.oss.services.sonom.flm.database.handlers.ResultHandler;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * Handles the {@link ResultSet} for an inserted {@link PAExecution}.
 */
public class PAExecutionInsertHandler implements ResultHandler<String> {

    /**
     * Gets the pa_execution_id of the newly inserted {@link PAExecution} record from the
     * {@link ResultSet}.
     *
     * @param resultSet
     *            The {@link ResultSet} to retrieve the pa_execution_id from.
     * @return The pa_execution_id of the new {@link PAExecution}.
     * @throws SQLException
     *             In the case where an error occurs getting the value from the {@link ResultSet}
     */
    @Override
    public String populate(final ResultSet resultSet) throws SQLException {
        String paExecutionId = "";
        while (resultSet.next()) {
            paExecutionId = resultSet.getString(PAExecutionDbConstants.ID);
        }
        return paExecutionId;
    }
}

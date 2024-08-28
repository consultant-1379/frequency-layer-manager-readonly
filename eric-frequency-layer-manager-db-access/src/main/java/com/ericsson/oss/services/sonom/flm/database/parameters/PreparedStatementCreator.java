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

package com.ericsson.oss.services.sonom.flm.database.parameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Use for creating {@link PreparedStatement} for queries with or without parameters.
 */
public class PreparedStatementCreator implements PreparedStatementHandler {

    /**
     * Override this method to add parameters or leave default if no parameters required.
     *
     * @param preparedStatement
     *            The prepared statement to add the parameters to.
     * @return A prepared statement.
     * @throws SQLException
     *             This exception will be raised if the parameter cannot be added to the statement.
     */
    public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement;
    }

    @Override
    public PreparedStatement create(final Connection connection, final String sqlQuery) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        return addParameters(preparedStatement);
    }
}

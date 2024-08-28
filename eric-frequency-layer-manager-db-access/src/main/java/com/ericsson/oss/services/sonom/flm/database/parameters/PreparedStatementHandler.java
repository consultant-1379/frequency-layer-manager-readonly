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
 * Interface for creating {@link PreparedStatement}.
 */
public interface PreparedStatementHandler {

    /**
     * Creates a {@link PreparedStatement}.
     * 
     * @param connection
     *            The Connection to use to create the {@link PreparedStatement}
     * @param sqlQuery
     *            The SQL Query to run with this {@link PreparedStatement}
     * @return Newly created {@link PreparedStatement}
     * @throws SQLException
     *             If there is a problem creating the {@link PreparedStatement}
     */
    PreparedStatement create(Connection connection, String sqlQuery) throws SQLException;
}

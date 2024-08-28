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
package com.ericsson.oss.services.sonom.flm.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;

/**
 * Class used to manage FLM database access for FLM.
 */
public class FlmDatabaseAccess extends DatabaseAccess {

    @Override
    protected Properties getJdbcProperties() {
        return DatabaseProperties.getFlmJdbcProperties();
    }

    @Override
    protected String getJdbcConnection() {
        return DatabaseProperties.getFlmJdbcConnection();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getJdbcConnection(), getJdbcProperties());
    }

}

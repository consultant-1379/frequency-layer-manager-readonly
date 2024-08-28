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
package com.ericsson.oss.services.sonom.flm.database.kpi;

import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;

/**
 * Implementation class of {@link UnitTestDatabaseRunner} with <code>eric-pm-kpi-calculator</code> specific details.
 */
public final class KpiServiceUnitTestDatabaseRunner extends UnitTestDatabaseRunner {

    private static final String DB_NAME = "kpi_service_db";
    private static final String H2_CONNECTION_URL_FORMAT = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";

    private static final String H2_CONNECTION_URL = String.format(H2_CONNECTION_URL_FORMAT, DB_NAME);
    private static final String DB_USER = "kpi_service_test_user";
    private static final String DB_PWD = "db_test_password";
    private static final String DB_DRIVER = "org.h2.Driver";

    @Override
    public void setTestDbEnvironmentVariables() {
        System.setProperty("KPI_SERVICE_DB_JDBC_CONNECTION", H2_CONNECTION_URL);
        System.setProperty("KPI_SERVICE_DB_EXTERNAL_USER", DB_USER);
        System.setProperty("KPI_SERVICE_DB_USER", DB_USER);
        System.setProperty("KPI_SERVICE_DB_EXTERNAL_PASSWORD", DB_PWD);
        System.setProperty("KPI_SERVICE_DB_PASSWORD", DB_PWD);
        System.setProperty("KPI_SERVICE_DB_DRIVER", DB_DRIVER);
    }

    @Override
    protected String getConnectionUrl() {
        return H2_CONNECTION_URL;
    }

    @Override
    protected String getUser() {
        return DB_USER;
    }

    @Override
    protected String getPassword() {
        return DB_PWD;
    }

}

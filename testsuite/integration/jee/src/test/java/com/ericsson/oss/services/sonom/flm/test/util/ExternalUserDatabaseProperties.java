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

package com.ericsson.oss.services.sonom.flm.test.util;

import java.util.Properties;

import com.ericsson.oss.services.sonom.common.env.Environment;

/**
 * Utility class to retrieve username/password for exposed KPI calculator user, kpi_exporter.
 */
public class ExternalUserDatabaseProperties {

    private ExternalUserDatabaseProperties() {

    }

    /**
     * Retrieves the JDBC connection properties ('user', 'password', 'driver') for <code>kpi_service_db</code> using the environment variables:
     * <ul>
     * <li>KPI_SERVICE_DB_EXTERNAL_USER</li>
     * <li>KPI_SERVICE_DB_EXTERNAL_PASSWORD</li>
     * <li>KPI_SERVICE_DB_DRIVER</li>
     * </ul>
     *
     * @return the JDBC connection properties for <code>kpi_service_db</code>
     */
    public static Properties getExternalUserKpiServiceJdbcProperties() {
        final Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty("user", Environment.getEnvironmentValue("KPI_SERVICE_DB_EXTERNAL_USER"));
        jdbcProperties.setProperty("password", Environment.getEnvironmentValue("KPI_SERVICE_DB_EXTERNAL_PASSWORD"));
        jdbcProperties.setProperty("driver", Environment.getEnvironmentValue("KPI_SERVICE_DB_DRIVER"));
        return jdbcProperties;
    }
}

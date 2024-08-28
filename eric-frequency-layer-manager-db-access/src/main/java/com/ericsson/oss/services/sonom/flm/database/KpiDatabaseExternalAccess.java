/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.database;

import java.util.Properties;

import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;
import com.ericsson.oss.services.sonom.common.env.Environment;

/**
 * Class used to manage KPI database external access for FLM.
 */
public class KpiDatabaseExternalAccess extends DatabaseAccess {

    @Override
    protected Properties getJdbcProperties() {
        return getKpiServiceJdbcProperties();
    }

    @Override
    protected String getJdbcConnection() {
        return DatabaseProperties.getKpiServiceJdbcConnection();
    }

    private static Properties getKpiServiceJdbcProperties() {
        final Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty("user", Environment.getEnvironmentValue("KPI_SERVICE_DB_EXTERNAL_USER"));
        jdbcProperties.setProperty("password", Environment.getEnvironmentValue("KPI_SERVICE_DB_EXTERNAL_PASSWORD"));
        jdbcProperties.setProperty("driver", Environment.getEnvironmentValue("KPI_SERVICE_DB_DRIVER"));
        return jdbcProperties;
    }
}
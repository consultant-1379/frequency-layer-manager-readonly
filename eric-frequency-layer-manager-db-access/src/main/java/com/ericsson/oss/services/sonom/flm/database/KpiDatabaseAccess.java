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

import java.util.Properties;

import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;

/**
 * Class used to manage KPI database access for FLM.
 */
public class KpiDatabaseAccess extends DatabaseAccess {

    @Override
    protected Properties getJdbcProperties() {
        return DatabaseProperties.getKpiServiceJdbcProperties();
    }

    @Override
    protected String getJdbcConnection() {
        return DatabaseProperties.getKpiServiceJdbcConnection();
    }
}
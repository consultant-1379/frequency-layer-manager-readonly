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

import static com.ericsson.oss.services.sonom.common.test.sql.SqlQueryExecutor.cleanTablesByTableName;

import java.util.List;
import java.util.Properties;

/**
 * Test class which sets/cleans the test environment.
 */
public final class TestEnvironmentBean {

    private TestEnvironmentBean() {

    }

    /**
     * Drops all kpi tables created by flm-service.
     * @param jdbcConnection
     *        jdbc connection name.
     * @param jdbcProperties
     *        jdbc connection properties.
     * @param tablesToClean
     *        list of tables to clean.
     */
    public static void cleanUp(final String jdbcConnection, final Properties jdbcProperties, final List<String> tablesToClean) {
        cleanTablesByTableName(jdbcConnection, jdbcProperties, tablesToClean);
    }
}

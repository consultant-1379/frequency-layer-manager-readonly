/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class providing formatting data used in the KPI DB.
 */
public class KpiDataFormatter {

    private KpiDataFormatter() {
        // do nothing
    }

    /**
     * This method return kpi value as string for not null value and "null" for null values. "null" string is returned to comply with avro schema
     * restrictions.
     *
     * @param resultSet
     *            result set
     * @param kpi
     *            kpi name
     * @return String kpi value
     * @throws SQLException
     *             thrown if an error occurred executing the query
     */
    public static String getStringKpiValue(final ResultSet resultSet, final String kpi) throws SQLException {
        return resultSet.getObject(kpi) == null
                ? "null"
                : String.valueOf(resultSet.getDouble(kpi));
    }

    /**
     * This method return kpi value as a Nullable Object.
     *
     * @param resultSet
     *            result set
     * @param kpi
     *            kpi name
     * @return Object kpi value
     * @throws SQLException
     *             thrown if an error occurred executing the query
     */
    public static Object getObjectKpiValue(final ResultSet resultSet, final String kpi) throws SQLException {
        return resultSet.getObject(kpi) == null
                ? null
                : resultSet.getObject(kpi);
    }

}
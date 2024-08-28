/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
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

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * KPI sector data access through KPI external interface.
 */
public interface KpiSectorExternalDao {

    /**
     * Retrieve all sector busy hours for the Sector IDs provided.
     *
     * @param queryDate
     *            The day for which to retrieve the sector busy hours
     * @param sectorIds
     *            A {@link List} of Sector IDs to retrieve busy hours for
     * @return A {@link Map} of Sector ID to sector busy hour
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<Long, String> getSectorBusyHourForSectorIds(String queryDate, List<String> sectorIds) throws SQLException;

    /**
     * Retrieve selected sector level KPIs per the sector IDs provided between the given UTC timestamps.
     *
     * @param startDateTime
     *            fetch the KPIs with a timestamp greater or equal to this timestamp
     * @param endDateTime
     *            fetch the KPIs with a timestamp less than this timestamp
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @param sectorIds
     *            A {@link List} of sector IDs to filter
     * @return A {@link Map} of Sector ID which holds a {@link Map} of Hourly Timestamps further linked to a {@link Map} of KPI values.
     * @throws SQLException
     *            thrown if an error occurred executing the query.
     */
    Map<Long, Map<String, Map<String, Object>>> getSectorHourlyKpis(String startDateTime, String endDateTime,
            List<String> kpiNames, Collection<Long> sectorIds) throws SQLException;

    /**
     * Retrieve selected sector level KPIs per FLM execution for the sector IDs and execution ID provided between the given UTC timestamps.
     *
     * @param executionId
     *            The FLM execution ID for which to retrieve KPIs
     * @param startDateTime
     *            fetch the KPIs with a timestamp greater or equal to this timestamp
     * @param endDateTime
     *            fetch the KPIs with a timestamp less than this timestamp
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @param sectorIds
     *            A {@link List} of sector IDs to filter
     * @return A {@link Map} of Sector ID which holds a {@link Map} of Hourly Timestamps further linked to a {@link Map} of KPI values.
     * @throws SQLException
     *            thrown if an error occurred executing the query.
     */
    Map<Long, Map<String, Map<String, Object>>> getSectorHourlyKpisForFlmExecution(String executionId, String startDateTime,
            String endDateTime, List<String> kpiNames, Collection<Long> sectorIds) throws SQLException;
}

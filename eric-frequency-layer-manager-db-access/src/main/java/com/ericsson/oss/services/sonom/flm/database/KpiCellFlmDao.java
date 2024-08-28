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

package com.ericsson.oss.services.sonom.flm.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;

/**
 * KPI Cell FLM data access through KPI interface.
 */
public interface KpiCellFlmDao {

    /**
     * Retrieve not visible cell hourly KPIs for the given day.
     *
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @param localTimeStamp
     *            A date time of the KPI to fetch
     * @return A {@link Map} of {@link CellKpi} which hold the cells identifiers to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellKpi, CellKpis> getNotVisibleCellHourlyKpis(List<String> kpiNames, String localTimeStamp) throws SQLException;

    /**
     * Retrieve not visible cell daily KPIs for the given day.
     *
     * @param executionDay
     *            the execution day timestamp on which to fetch the KPIs for
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @return A {@link Map} of {@link CellKpi} which hold the cells identifiers to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellKpi, Map<String, Object>> getNotVisibleCellDailyKpis(String executionDay, List<String> kpiNames) throws SQLException;

    /**
     * Retrieve Cell Sector FLM KPIs for cells with given FLM execution id.
     *
     * @param executionId
     *            The FLM execution ID for which to retrieve KPIs
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @return A {@link Map} of {@link CellKpi} which hold the cells identifiers to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellKpi, Map<String, Object>> getNotVisibleCellSectorDailyFlmKpis(String executionId, List<String> kpiNames) throws SQLException;
}
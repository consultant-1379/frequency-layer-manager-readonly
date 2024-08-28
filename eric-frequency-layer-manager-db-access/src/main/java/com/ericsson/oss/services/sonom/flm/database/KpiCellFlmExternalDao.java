/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
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
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

/**
 * KPI Cell FLM data access through KPI external interface.
 */
public interface KpiCellFlmExternalDao {

    /**
     * Retrieve hourly cell KPIs for the execution ID provided.
     *
     * @param executionId
     *            The FLM execution ID for which to retrieve KPIs
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @return A {@link Map} of {@link CellIdentifier} which holds to a {@link Map} of Hourly Timestamps further linked to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellIdentifier, Map<String, Map<String, Object>>> getCellHourlyFlmKpis(String executionId,
            List<String> kpiNames) throws SQLException;

    /**
     * Retrieve daily cell sector KPIs for the execution ID provided.
     *
     * @param executionId
     *            The FLM execution ID for which to retrieve KPIs
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @return A {@link Map} of {@link CellKpi} which hold the cells identifiers to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellKpi, Map<String, Object>> getCellSectorDailyFlmKpis(String executionId, List<String> kpiNames) throws SQLException;

    /**
     * Retrieve Coverage Balance KPIs for the given sector IDs on the given date.
     *
     * @param executionDay
     *            the date on which to fetch the KPIs for
     * @param sectorIds
     *            The list of all sectors for which KPIs need to be retrieved.
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @return A {@link Map} of {@link CellKpi} which hold the cells identifiers to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellKpi, Map<String, Object>> getCoverageBalanceKpis(String executionDay,
            List<String> sectorIds, List<String> kpiNames) throws SQLException;

    /**
     * Retrieve daily cell KPIs for the given date.
     *
     * @param executionDay
     *            the date on which to fetch the KPIs for
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @return A {@link Map} of {@link CellKpi} which hold the cells identifiers to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellKpi, Map<String, Object>> getCellDailyKpis(String executionDay, List<String> kpiNames) throws SQLException;

    /**
     * Retrieve hourly cell KPIs between the given time stamps.
     *
     * @param startDateTime
     *            fetch the KPIs with a time stamp greater or equal to this timestamp
     * @param endDateTime
     *            fetch the KPIs with a time stamp less than this timestamp
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @return A {@link Map} of {@link CellIdentifier} which holds to a {@link Map} of Hourly Timestamps further linked to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellIdentifier, Map<String, Map<String, Object>>> getCellHourlyKpis(String startDateTime,
            String endDateTime, List<String> kpiNames) throws SQLException;

    /**
     * Retrieve selected Cell level KPIs per FLM execution for the OSS ID and FDN combinations and execution ID provided between the given UTC
     * timestamps.
     *
     * @param executionId
     *            The FLM execution ID for which to retrieve KPIs
     * @param startDateTime
     *            fetch the KPIs with a timestamp greater or equal to this timestamp
     * @param endDateTime
     *            fetch the KPIs with a timestamp less than this timestamp
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @param cells
     *            A {@link List} of Cells to filter
     * @return A {@link Map} of {@link CellIdentifier} which holds a {@link Map} of Hourly Timestamps further linked to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellIdentifier, Map<String, Map<String, Object>>> getKpisForGivenCellsPerFlmExecution(String executionId, String startDateTime,
            String endDateTime, List<String> kpiNames, List<CellIdentifier> cells) throws SQLException;

    /**
     * Retrieve cell hourly KPIs between the given UTC timestamps for the given list of cells.
     *
     * @param startDateTime
     *            fetch the KPIs with a timestamp greater or equal to this timestamp
     * @param endDateTime
     *            fetch the KPIs with a timestamp less than this timestamp
     * @param kpiNames
     *            A {@link List} of KPI names to retrieve
     * @param cells
     *            A {@link List} of Cells to filter
     * @return A {@link Map} of {@link CellIdentifier} which holds to a {@link Map} of Hourly Timestamps further linked to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Map<CellIdentifier, Map<String, Map<String, Object>>> getHourlyKpisForGivenCells(String startDateTime, String endDateTime,
            List<String> kpiNames, List<CellIdentifier> cells) throws SQLException;

}

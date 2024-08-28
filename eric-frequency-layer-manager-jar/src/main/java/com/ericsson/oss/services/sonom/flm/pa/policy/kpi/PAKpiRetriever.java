/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.pa.policy.kpi;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmExternalDao;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmExternalDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorExternalDao;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorExternalDaoImpl;
import com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor.PAConstants;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

/**
 * Gets the KPIs required for PA Optimization.
 */
public class PAKpiRetriever {
    private final KpiCellFlmExternalDao cellFlmDao;
    private final KpiSectorExternalDao sectorDao;

    public PAKpiRetriever() {
        cellFlmDao = new KpiCellFlmExternalDaoImpl(PAConstants.MAX_RETRY_ATTEMPTS, PAConstants.SECONDS_TO_WAIT);
        sectorDao = new KpiSectorExternalDaoImpl(PAConstants.MAX_RETRY_ATTEMPTS, PAConstants.SECONDS_TO_WAIT);
    }

    public PAKpiRetriever(final KpiCellFlmExternalDao cellFlmDao, final KpiSectorExternalDao sectorDao) {
        this.cellFlmDao = cellFlmDao;
        this.sectorDao = sectorDao;
    }

    /**
     * Retrieve the on-demand degradation threshold PA KPIs for the {@link TopologyObjectId}s and UTC time period provided.
     * 
     * @param executionId
     *            The Execution ID for which to retrieve the KPIs
     * @param startDateTime
     *            The start time of the PA window
     * @param endDateTime
     *            The end time of the PA window
     * @param cells
     *            {@link List} of TopologyObjectIds.
     * @return A {@link Map} of {@link CellIdentifier} to a Map of timestamp to Kpis
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellIdentifier, Map<String, Map<String, Object>>> retrieveCellDegradationThresholdKpis(final String executionId,
            final String startDateTime, final String endDateTime, final List<TopologyObjectId> cells) throws SQLException {
        final List<String> kpiNames = Arrays.stream(PACellGuid60Kpis.values())
                .map(PACellGuid60Kpis::getThresholdName)
                .collect(Collectors.toList());
        final List<CellIdentifier> cellIdentifiers = cells.stream().map(PAKpiRetriever::topologyObjectIdToCellIdentifier)
                .collect(Collectors.toList());

        return cellFlmDao.getKpisForGivenCellsPerFlmExecution(executionId, startDateTime, endDateTime, kpiNames, cellIdentifiers);
    }

    /**
     * Retrieve the hourly PA KPIs for the {@link TopologyObjectId}s and UTC time period provided.
     *
     * @param startDateTime
     *            The start time of the PA window
     * @param endDateTime
     *            The end time of the PA window
     * @param cells
     *            {@link List} of TopologyObjectIds.
     * @return A {@link Map} of {@link CellIdentifier} to a Map of timestamp to Kpis
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellIdentifier, Map<String, Map<String, Object>>> retrieveCellHourlyKpis(final String startDateTime, final String endDateTime,
            final List<TopologyObjectId> cells) throws SQLException {
        final List<String> kpiNames = Arrays.stream(PACellGuid60Kpis.values())
                .map(PACellGuid60Kpis::getKpiName)
                .collect(Collectors.toList());
        final List<CellIdentifier> cellIdentifiers = cells.stream().map(PAKpiRetriever::topologyObjectIdToCellIdentifier)
                .collect(Collectors.toList());

        return cellFlmDao.getHourlyKpisForGivenCells(startDateTime, endDateTime, kpiNames, cellIdentifiers);
    }

    /**
     * Retrieve the on-demand degradation threshold PA KPIs for the Sector IDs and UTC time period provided.
     *
     * @param executionId
     *            The Execution ID for which to retrieve the KPIs
     * @param startDateTime
     *            The start time of the PA window
     * @param endDateTime
     *            The end time of the PA window
     * @param sectorIds
     *            {@link List} of Sector IDs.
     * @return A {@link Map} of {@link String} to a Map of timestamp to Kpis
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<Long, Map<String, Map<String, Object>>> retrieveSectorDegradationThresholdKpis(final String executionId,
            final String startDateTime, final String endDateTime, final Collection<Long> sectorIds) throws SQLException {
        final List<String> kpiNames = Arrays.stream(PASector60Kpis.values())
                .map(PASector60Kpis::getThresholdName)
                .collect(Collectors.toList());

        return sectorDao.getSectorHourlyKpisForFlmExecution(executionId, startDateTime, endDateTime, kpiNames, sectorIds);
    }

    /**
     * Retrieve the hourly PA KPIs for the Sector IDs and UTC time period provided.
     *
     * @param startDateTime
     *            The start time of the PA window
     * @param endDateTime
     *            The end time of the PA window
     * @param sectorIds
     *            {@link List} of Sector IDs.
     * @return A {@link Map} of {@link String} to a Map of timestamp to Kpis
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<Long, Map<String, Map<String, Object>>> retrieveSectorHourlyKpis(final String startDateTime, final String endDateTime,
            final Collection<Long> sectorIds) throws SQLException {
        final List<String> kpiNames = Arrays.stream(PASector60Kpis.values())
                .map(PASector60Kpis::getKpiName)
                .collect(Collectors.toList());

        return sectorDao.getSectorHourlyKpis(startDateTime, endDateTime, kpiNames, sectorIds);
    }

    private static CellIdentifier topologyObjectIdToCellIdentifier(final TopologyObjectId topologyObjectId) {
        return new CellIdentifier(topologyObjectId.getOssId(), topologyObjectId.getFdn());
    }
}

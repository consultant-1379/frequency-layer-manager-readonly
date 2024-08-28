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

package com.ericsson.oss.services.sonom.flm.kpi.store;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.BusyHourCellKpis;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.CellFlmKpiRetriever;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.SectorBusyHourRetriever;

/**
 * Gets Cell KPIs from {@link BusyHourCellKpis} enum for a sector busy hour.
 */
public class CellKpiStoreImpl implements CellKpiStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(CellKpiStoreImpl.class);
    private static final int INVALID_VALUE = -1;
    private SectorBusyHourRetriever sectorBusyHourRetriever = new SectorBusyHourRetriever();
    private CellFlmKpiRetriever cellFlmKpiRetriever = new CellFlmKpiRetriever();
    private final Map<TopologyObjectId, CellKpis> cellBusyHourKpis = new HashMap<>();

    /**
     * Store busy hour KPIs from {@link BusyHourCellKpis} enum for the {@link TopologySector}s provided.
     *
     * @param queryDate
     *            The Date of the Busy Hour for which to retrieve the Busy Hour KPIs.
     * @param sectorList
     *            The sectors for which to retrieve the Busy Hour KPIs.
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public CellKpiStoreImpl(final String queryDate, final Collection<TopologySector> sectorList) throws SQLException {
        this.retrieveBusyHourCellKpisForAllCell(queryDate, sectorList);
    }

    /**
     * Constructor used for tests.
     *
     * @param cellFlmKpiRetriever
     *            Mocked {@link CellFlmKpiRetriever}
     * @param sectorBusyHourRetriever
     *            Mocked {@link SectorBusyHourRetriever}
     * @param queryDate
     *            The Date of the Busy Hour for which to retrieve the Busy Hour KPIs.
     * @param sectorList
     *            The sectors for which to retrieve the Busy Hour KPIs.
     * @throws SQLException
     *            thrown if an error occurred retrieving the KPIs.
     */
    public CellKpiStoreImpl(final SectorBusyHourRetriever sectorBusyHourRetriever, final CellFlmKpiRetriever cellFlmKpiRetriever,
                            final String queryDate, final Collection<TopologySector> sectorList) throws SQLException {
        this.sectorBusyHourRetriever = sectorBusyHourRetriever;
        this.cellFlmKpiRetriever = cellFlmKpiRetriever;
        this.retrieveBusyHourCellKpisForAllCell(queryDate, sectorList);
    }

    /**
     * Gets the Busy Hour Kpis for a given cell.
     *
     * @param cellFdn The Cell's fdn
     * @param cellOssId The Cell's oss Id
     * @return
     *            The Busy Hour Kpis for a given cell or null if one of the KPI value is empty.
     */
    @Override
    public CellKpis getKpisForCell(final String cellFdn, final long cellOssId) {
        return cellBusyHourKpis.get(TopologyObjectId.of(cellFdn, (int) cellOssId));
    }

    private void retrieveBusyHourCellKpisForAllCell(final String queryDate, final Collection<TopologySector> sectorList) throws SQLException {
        LOGGER.info("Retrieving busy hour cell KPIs for date: {}", queryDate);
        final Map<Long, String> sectorBusyHoursBySectorId = sectorBusyHourRetriever.populateSectorIdToBusyHour(queryDate, sectorList);
        LOGGER.debug("Retrieved {} number of sector busy hours", sectorBusyHoursBySectorId.size());
        final Map<Long, Collection<Cell>> cellsBySectorId = mapCellsInSectorBySectorId(sectorList);
        LOGGER.debug("Created map from sector_id to cells in sector with {} entries", cellsBySectorId.size());
        final Map<String, List<Long>> sectorIdsByBusyHour = groupSectorIdsByBusyHour(sectorBusyHoursBySectorId);
        LOGGER.debug("Grouped sector ids by busy hour with {} number of entries", sectorIdsByBusyHour.size());
        for (final Map.Entry<String, List<Long>> entry : sectorIdsByBusyHour.entrySet()) {
            final List<Long> sectorIds = entry.getValue();
            // busy hour example: 2021-01-18 09:00:00.0
            final String busyHour = entry.getKey();
            final String formattedBusyHour = DateTimeFormatter.ISO_DATE_TIME.format(
                    LocalDateTime.parse(busyHour, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")));
            // Retrieve cell kpis for the cells in the sector for a busy hour:
            final Map<CellKpi, CellKpis> cellKpis = cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis(formattedBusyHour);

            sectorIds.forEach(sectorId -> {
                final Collection<Cell> cellsInSector = cellsBySectorId.get(sectorId);
                if (cellsInSector != null) {
                    mapBusyHourKpisToCells(cellKpis, cellsInSector);
                }
            });
        }
    }

    private Map<Long, Collection<Cell>> mapCellsInSectorBySectorId(final Collection<TopologySector> sectorList) {
        final Map<Long, Collection<Cell>> cellsBySectorId = new HashMap<>();
        sectorList.forEach(topologySector -> cellsBySectorId.put(topologySector.getSectorId(), topologySector.getAssociatedCells()));
        return cellsBySectorId;
    }

    private Map<String, List<Long>> groupSectorIdsByBusyHour(final Map<Long, String> sectorBusyHoursBySectorId) {
        final Map<String, List<Long>> sectorIdsByBusyHour = new HashMap<>(24);
        sectorBusyHoursBySectorId.forEach((sectorId, busyHour) -> {
            final List<Long> sectorIds = sectorIdsByBusyHour.getOrDefault(busyHour, new ArrayList<>(1));
            sectorIds.add(sectorId);
            sectorIdsByBusyHour.put(busyHour, sectorIds);
        });

        return sectorIdsByBusyHour;
    }

    private void mapBusyHourKpisToCells(final Map<CellKpi, CellKpis> cellKpis, final Collection<Cell> cells) {
        cells.forEach(cell -> {
            final TopologyObjectId cellId = TopologyObjectId.of(cell.getFdn(), cell.getOssId());
            for (final Map.Entry<CellKpi, CellKpis> kpiMapEntry : cellKpis.entrySet()) {
                final CellKpi cellKpi = kpiMapEntry.getKey();
                if (cellKpi.getOssId() == cellId.getOssId()
                        && cellKpi.getFdn() != null && cellKpi.getFdn().equals(cellId.getFdn())) {
                    cellBusyHourKpis.put(cellId, validateKpis(kpiMapEntry.getValue()));
                }
            }
        });
    }

    private CellKpis validateKpis(final CellKpis cellKpis) {
        if (cellKpis.getConnectedUsers() == INVALID_VALUE
            || cellKpis.getSubscriptionRatio() == INVALID_VALUE
            || cellKpis.getPmIdleModeRelDistrHighLoad() == INVALID_VALUE
            || cellKpis.getPmIdleModeRelDistrMediumHighLoad() == INVALID_VALUE
            || cellKpis.getPmIdleModeRelDistrMediumLoad() == INVALID_VALUE
            || cellKpis.getPmIdleModeRelDistrLowMediumLoad() == INVALID_VALUE
            || cellKpis.getPmIdleModeRelDistrLowLoad() == INVALID_VALUE) {
            LOGGER.warn("Invalid cellKpis with data {}, {}, {}, {}, {}, {}, {}",
                    cellKpis.getConnectedUsers(),
                    cellKpis.getSubscriptionRatio(),
                    cellKpis.getPmIdleModeRelDistrLowLoad(),
                    cellKpis.getPmIdleModeRelDistrLowMediumLoad(),
                    cellKpis.getPmIdleModeRelDistrMediumLoad(),
                    cellKpis.getPmIdleModeRelDistrMediumHighLoad(),
                    cellKpis.getPmIdleModeRelDistrHighLoad());
            return null;
        }

        return cellKpis;
    }
}

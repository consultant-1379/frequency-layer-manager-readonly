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

package com.ericsson.oss.services.sonom.flm.optimization.kpi;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.metric.FlmMetricHelper;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

import io.vavr.Tuple2;

/**
 * This class collects all the KPIs for each cellIdentifier in the sectors.
 */
public class CellKpiCollection {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellKpiCollection.class);

    private final CellFlmKpiRetriever cellFlmKpiRetriever;
    private final MetricHelper flmMetricHelper;
    private final Map<CellIdentifier, Map<String, Object>> cellKpiMap = new HashMap<>();
    private Map<Long, String> sectorBusyHoursBySectorId = new HashMap<>();

    public CellKpiCollection() {
        cellFlmKpiRetriever = new CellFlmKpiRetriever();
        flmMetricHelper = new FlmMetricHelper();
    }

    // required for Mockito JUnit
    public CellKpiCollection(final CellFlmKpiRetriever cellFlmKpiRetriever, final FlmMetricHelper flmMetricHelper) {
        this.cellFlmKpiRetriever = cellFlmKpiRetriever;
        this.flmMetricHelper = flmMetricHelper;
    }

    /**
     * Method to collect KPIs.
     *
     * @param sectorList
     *            list of sectors
     * @param executionId
     *            execution id
     * @param endDateTime
     *            the ISO_DATE representation of the day of the execution
     * @param startDateTime
     *            the ISO_DATE representation of the day before the execution
     * @return collected {@link Entry} of {@link CellIdentifier}
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellIdentifier, Map<String, Object>> collect(final Collection<TopologySector> sectorList, final String executionId,
            final String endDateTime, final String startDateTime) throws SQLException {
        populateSectorBusyHourKpis(sectorList, executionId, endDateTime, startDateTime);
        final Map<CellKpi, Map<String, Object>> coverageBalanceRatioKpis = getCoverageBalanceRatioKpisForCells(sectorList, startDateTime);
        mergeCellKpis(coverageBalanceRatioKpis);
        final Map<CellKpi, Map<String, Object>> cellDailyKpis = getCellDailyKpis(startDateTime);
        mergeCellKpis(cellDailyKpis);
        final Map<CellKpi, Map<String, Object>> notVisibleCellDailyKpis = getNotVisibleCellDailyKpis(startDateTime);
        mergeCellKpis(notVisibleCellDailyKpis);
        final Map<CellKpi, Map<String, Object>> targetCellCapacityKpis = getNotVisibleCellSectorFlmKpis(executionId);
        mergeCellKpis(targetCellCapacityKpis);
        final Map<CellKpi, Map<String, Object>> visibleCellSectorKpis = getVisibleCellSectorFlmKpis(executionId);
        mergeCellKpis(visibleCellSectorKpis);
        return cellKpiMap;
    }

    private Map<CellKpi, Map<String, Object>> getNotVisibleCellSectorFlmKpis(final String executionId) throws SQLException {
        final long targetCellCapacityKpisLoadStartTime = System.nanoTime();
        final Map<CellKpi, Map<String, Object>> targetCellCapacityKpis = cellFlmKpiRetriever.retrieveNotVisibleCellSectorFlmKpis(executionId);
        LOGGER.info("'{}' non-visible daily cell sector FLM KPIs have been loaded successfully", targetCellCapacityKpis.size());
        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_CELL_SECTOR_KPI_LOAD_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(targetCellCapacityKpisLoadStartTime));
        return targetCellCapacityKpis;
    }

    private Map<CellKpi, Map<String, Object>> getVisibleCellSectorFlmKpis(final String executionId) throws SQLException {
        final long visibleCellSectorKpisLoadStartTime = System.nanoTime();
        final Map<CellKpi, Map<String, Object>> visibleCellSectorKpis = cellFlmKpiRetriever.retrieveVisibleCellSectorFlmKpis(executionId);
        LOGGER.info("'{}' visible daily cell sector FLM KPIs have been loaded successfully", visibleCellSectorKpis.size());
        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_CELL_SECTOR_KPI_LOAD_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(visibleCellSectorKpisLoadStartTime));
        return visibleCellSectorKpis;
    }

    private Map<CellKpi, Map<String, Object>> getNotVisibleCellDailyKpis(final String startDateTime) throws SQLException {
        final long notVisibleCellDailyKpisLoadStartTime = System.nanoTime();
        final Map<CellKpi, Map<String, Object>> notVisibleCellDailyKpis = cellFlmKpiRetriever.retrieveNotVisibleCellDailyKpis(startDateTime);
        LOGGER.info("'{}' non-visible daily cell KPIs have been loaded successfully", notVisibleCellDailyKpis.size());
        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_CELL_DAILY_KPI_LOAD_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(notVisibleCellDailyKpisLoadStartTime));
        return notVisibleCellDailyKpis;
    }

    private Map<CellKpi, Map<String, Object>> getCellDailyKpis(final String startDateTime)
            throws SQLException {
        final long cellDailyKpisLoadStartTime = System.nanoTime();
        final Map<CellKpi, Map<String, Object>> cellDailyKpis = cellFlmKpiRetriever.retrieveCellDailyKpis(startDateTime);
        LOGGER.info("'{}' visible daily cell KPIs have been loaded successfully", cellDailyKpis.size());
        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_CELL_DAILY_KPI_LOAD_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(cellDailyKpisLoadStartTime));
        return cellDailyKpis;
    }

    private void populateSectorBusyHourKpis(final Collection<TopologySector> sectorList, final String executionId, final String endDateTime,
            final String startDateTime) throws SQLException {
        final Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>> allKpis =
                getAllKpis(startDateTime, endDateTime, sectorList, executionId);
        sectorBusyHoursBySectorId = allKpis._1();
        sectorList.forEach(sector -> populateKpisForCellsInSector(sector, allKpis._2()));
        sectorBusyHoursBySectorId = null;
    }

    private Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>> getAllKpis(final String startDateTime,
            final String endDateTime, final Collection<TopologySector> sectorList, final String executionId)
            throws SQLException {
        final long gettingAllKpisStartTime = System.nanoTime();
        final Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>> cellKpis =
                cellFlmKpiRetriever.retrieveAllKpis(startDateTime, endDateTime, sectorList, executionId);
        LOGGER.info("All KPIs have been loaded successfully");
        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_BUSY_HOUR_KPI_LOAD_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(gettingAllKpisStartTime));
        return cellKpis;
    }

    private Map<CellKpi, Map<String, Object>> getCoverageBalanceRatioKpisForCells(final Collection<TopologySector> sectorList,
            final String startDateTime)
            throws SQLException {
        final long coverageBalanceRatioKpiLoadStartTime = System.nanoTime();
        final Map<CellKpi, Map<String, Object>> coverageBalanceRatioKpis = cellFlmKpiRetriever.retrieveCoverageBalanceFlmKpis(sectorList,
                startDateTime);
        LOGGER.info("Coverage Balance KPIs have been loaded successfully");
        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_COVERAGE_BALANCE_RATIO_KPI_LOAD_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(coverageBalanceRatioKpiLoadStartTime));
        return coverageBalanceRatioKpis;
    }

    private void populateKpisForCellsInSector(final TopologySector sector, final Map<CellIdentifier, Map<String, Map<String, Object>>> kpiMap) {
        final BinaryOperator<Map<String, Object>> mapBinaryOperator = (key1, key2) -> {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ignoring duplicate key '{}' found in sector '{}'", key2, sector.getSectorId());
            }
            return key1;
        };

        if (sectorBusyHoursBySectorId.get(sector.getSectorId()) == null) {
            LOGGER.warn("Sector busy hour null for sector '{}', no sector busy hour KPIs will be added to input event", sector.getSectorId());
        }

        final Map<CellIdentifier, Map<String, Object>> sectorCellKpiMap = sector.getAssociatedCells()
                .stream()
                .map(cell -> new CellPolicyInputEventContainer(
                        new CellIdentifier(cell.getOssId(), cell.getFdn()), getCellKpisForSectorBusyHour(cell, sector, kpiMap)))
                .collect(
                        Collectors.toMap(CellPolicyInputEventContainer::getCellIdentifier, CellPolicyInputEventContainer::getKpiNameValuePair,
                                mapBinaryOperator));
        for (final Map.Entry<CellIdentifier, Map<String, Object>> entry : sectorCellKpiMap.entrySet()) {
            final CellIdentifier cellIdentifier = entry.getKey();
            if (cellKpiMap.containsKey(cellIdentifier)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Cell exists in cellKpiMap, Adding KPIs to existing cell fdn '{}', oss_id '{}' found in sector '{}'",
                            cellIdentifier.getFdn(), cellIdentifier.getOssId(), sector.getSectorId());
                }
                cellKpiMap.get(cellIdentifier).putAll(entry.getValue());
            } else {
                cellKpiMap.put(cellIdentifier, entry.getValue());
            }
        }
    }

    private Map<String, Object> getCellKpisForSectorBusyHour(final Cell cell, final TopologySector sector,
            final Map<CellIdentifier, Map<String, Map<String, Object>>> kpiMap) {
        final String busyHour = sectorBusyHoursBySectorId.get(sector.getSectorId());
        if (busyHour == null) {
            return new LinkedHashMap<>();
        }
        final CellIdentifier cellKpi = new CellIdentifier(cell.getOssId(), cell.getFdn());
        final Map<String, Map<String, Object>> timestampMap = kpiMap.get(cellKpi);
        if (timestampMap != null) {
            final Map<String, Object> cellKpis = timestampMap.get(busyHour);
            if (cellKpis != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("KPIs found for fdn '{}', oss_id '{}', in sector '{}'", cell.getFdn(), cell.getOssId(), sector.getSectorId());
                }
                return cellKpis;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("KPIs not found for fdn '{}', oss_id '{}', in sector '{}'", cell.getFdn(), cell.getOssId(), sector.getSectorId());
            }
            return new LinkedHashMap<>();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Timestamp Map not found for fdn '{}', oss_id '{}', in sector '{}'", cell.getFdn(), cell.getOssId(), sector.getSectorId());
        }
        return new LinkedHashMap<>();

    }

    private void mergeCellKpis(final Map<CellKpi, Map<String, Object>> kpisToMerge) {
        for (final Map.Entry<CellKpi, Map<String, Object>> entry : kpisToMerge.entrySet()) {
            final CellKpi cellKpi = entry.getKey();
            final CellIdentifier cellIdentifier = new CellIdentifier(cellKpi.getOssId(), cellKpi.getFdn());
            if (cellKpiMap.containsKey(cellIdentifier)) {
                cellKpiMap.get(cellIdentifier).putAll(entry.getValue());
            } else {
                cellKpiMap.put(cellIdentifier, entry.getValue());
            }
        }
    }

    private static class CellPolicyInputEventContainer {
        private final CellIdentifier cellIdentifier;
        private final Map<String, Object> kpiNameValuePair;

        private CellPolicyInputEventContainer(final CellIdentifier cellIdentifier, final Map<String, Object> kpiNameValuePair) {
            this.cellIdentifier = cellIdentifier;
            this.kpiNameValuePair = kpiNameValuePair;
        }

        public CellIdentifier getCellIdentifier() {
            return cellIdentifier;
        }

        public Map<String, Object> getKpiNameValuePair() {
            return kpiNameValuePair;
        }
    }
}

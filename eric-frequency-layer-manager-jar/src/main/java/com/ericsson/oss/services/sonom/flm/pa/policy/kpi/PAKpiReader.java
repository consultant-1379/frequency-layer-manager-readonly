/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.pa.policy.kpi;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionLatch;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.KpiValue;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.Cell;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.CellLevelKpi;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.Sector;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.SectorLevelKpi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reads retrieved KPI values.
 */
public class PAKpiReader {

    private static final String VALUE_ATTR_NAME = "value";
    private static final String THRESHOLD_ATTR_NAME = "threshold";
    private static final String PA_KPI_SETTINGS_NAME = "paKpiSettings";
    private static final String ENABLE_KPI_SETTING_NAME = "enableKPI";
    private static final String RELEVANCE_THRESHOLD_SETTING_NAME = "relevanceThreshold";
    private static final String NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_SETTING_NAME = "numberOfKpiDegradedHoursThreshold";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PAExecution paExecution;
    private final Execution flmExecution;
    private final PAKpiRetriever kpiRetriever;
    private final PAExecutionLatch latch;

    public PAKpiReader(final PAExecution paExecution, final Execution flmExecution, final PAKpiRetriever kpiRetriever, final PAExecutionLatch latch) {
        this.paExecution = paExecution;
        this.flmExecution = flmExecution;
        this.kpiRetriever = kpiRetriever;
        this.latch = latch;
    }

    /**
     * Generates a list of sectors based on the sector and cells map.
     *
     * @param sectorsAndCells
     *            Map of sectors and cells.
     * @return list of sectors.
     * @throws PAExecutionException
     *             when failed to retrieve sector-level KPIs.
     */
    public List<Sector> generateSectorList(final Map<Long, List<TopologyObjectId>> sectorsAndCells) throws PAExecutionException {
        final JsonNode paSettings = retrievePaKpiSettings();
        final Map<String, String> policyInputSettings = retrievePolicyInputSettings();

        final List<Sector> sectors = new ArrayList<>();

        final String paWindowStartTime = timestampToUtcString(paExecution.getPaWindowStartTime());
        final String paWindowEndTime = timestampToUtcString(paExecution.getPaWindowEndTime());

        try {
            final Map<Long, Map<String, Map<String, Object>>> hourlySectorKpis = kpiRetriever.retrieveSectorHourlyKpis(paWindowStartTime,
                    paWindowEndTime, sectorsAndCells.keySet());
            final Map<Long, Map<String, Map<String, Object>>> degradationSectorKpis = kpiRetriever.retrieveSectorDegradationThresholdKpis(
                    flmExecution.getId(), paWindowStartTime, paWindowEndTime, sectorsAndCells.keySet());

            for (final Map.Entry<Long, List<TopologyObjectId>> sectorAndCellsEntry : sectorsAndCells.entrySet()) {
                latch.verifyNotInterruptedAndContinue();
                final Long sectorId = sectorAndCellsEntry.getKey();
                final Map<String, Map<String, Object>> timestampToHourlyKpis = hourlySectorKpis.getOrDefault(sectorId, Collections.emptyMap());
                final Map<String, Map<String, Object>> timestampToDegradationKpis = degradationSectorKpis.getOrDefault(sectorId,
                        Collections.emptyMap());

                final Map<String, SectorLevelKpi> sectorLevelKpis = generateSectorLevelKpis(timestampToHourlyKpis, timestampToDegradationKpis,
                        paSettings);
                final List<Cell> cells = generateCellList(sectorAndCellsEntry.getValue(), paSettings);

                sectors.add(new Sector(String.valueOf(sectorId), policyInputSettings, sectorLevelKpis, cells));
            }
        } catch (final SQLException e) {
            throw new PAExecutionException("Failed to retrieve sector-level KPIs for PA", e);
        }
        return sectors;
    }

    private List<Cell> generateCellList(final List<TopologyObjectId> topologyObjectIds, final JsonNode paSettings) throws PAExecutionException {
        final String paWindowStartTime = timestampToUtcString(paExecution.getPaWindowStartTime());
        final String paWindowEndTime = timestampToUtcString(paExecution.getPaWindowEndTime());

        final List<Cell> cells = new ArrayList<>();

        try {
            final Map<CellIdentifier, Map<String, Map<String, Object>>> cellHourlyKpis = kpiRetriever.retrieveCellHourlyKpis(paWindowStartTime,
                    paWindowEndTime, topologyObjectIds);
            final Map<CellIdentifier, Map<String, Map<String, Object>>> cellDegradationKpis = kpiRetriever
                    .retrieveCellDegradationThresholdKpis(flmExecution.getId(), paWindowStartTime, paWindowEndTime, topologyObjectIds);

            for (final TopologyObjectId topologyObjectId : topologyObjectIds) {
                final CellIdentifier cellIdentifier = new CellIdentifier(topologyObjectId.getOssId(), topologyObjectId.getFdn());
                final Map<String, Map<String, Object>> timestampToKpis = cellHourlyKpis.getOrDefault(cellIdentifier, Collections.emptyMap());
                final Map<String, Map<String, Object>> timestampToDegradation = cellDegradationKpis.getOrDefault(cellIdentifier,
                        Collections.emptyMap());

                final Map<String, CellLevelKpi> cellLevelKpis = generateCellLevelKpis(timestampToKpis, timestampToDegradation, paSettings);
                final Cell cell = new Cell(cellIdentifier.getFdn(), cellIdentifier.getOssId(), cellLevelKpis);
                cells.add(cell);
            }

        } catch (final SQLException e) { // NOSONAR Exception suitably logged
            throw new PAExecutionException("Failed to retrieve cell-levels KPIs for PA", e);
        }

        return cells;
    }

    private JsonNode retrievePaKpiSettings() throws PAExecutionException {
        final String paSettingsString = flmExecution.getCustomizedGlobalSettings().get(PA_KPI_SETTINGS_NAME);

        try {
            return OBJECT_MAPPER.readTree(paSettingsString);
        } catch (final JsonProcessingException e) {
            throw new PAExecutionException("Failed to parse PA Settings.", e);
        }
    }

    private Map<String, String> retrievePolicyInputSettings() {
        final Map<String, String> policyInputSettings = new HashMap<>();
        policyInputSettings.put(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_SETTING_NAME,
                flmExecution.getCustomizedGlobalSettings().get(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_SETTING_NAME));
        return policyInputSettings;
    }

    private static Map<String, CellLevelKpi> generateCellLevelKpis(final Map<String, Map<String, Object>> hourlyKpisByTimestamp,
            final Map<String, Map<String, Object>> degradationKpisByTimestamp,
            final JsonNode paSettings) {
        final Map<String, Map<String, Map<String, Object>>> kpisToTimestamp = sortKpiByKpiName(hourlyKpisByTimestamp, degradationKpisByTimestamp,
                PACellGuid60Kpis::getThresholdNameForKpi);
        final Map<String, CellLevelKpi> cellLevelKpis = new HashMap<>();

        for (final PACellGuid60Kpis kpi : PACellGuid60Kpis.values()) {
            final String kpiName = kpi.getKpiName();
            final Map<String, Map<String, Object>> timestampMap = kpisToTimestamp.getOrDefault(kpiName, Collections.emptyMap());
            final List<KpiValue> kpiValues = new ArrayList<>();

            timestampMap.forEach((timestamp, valueMap) -> {
                final String value = String.valueOf(valueMap.get(VALUE_ATTR_NAME));
                final String threshold = String.valueOf(valueMap.get(THRESHOLD_ATTR_NAME));
                kpiValues.add(new KpiValue(value, timestamp, threshold));
            });

            final JsonNode kpiSettings = paSettings.get(kpi.getSettingName());
            final boolean kpiEnabled = kpiSettings.get(ENABLE_KPI_SETTING_NAME).asBoolean();
            final String relevanceThreshold = kpiSettings.get(RELEVANCE_THRESHOLD_SETTING_NAME).asText();

            final CellLevelKpi cellLevelKpi = new CellLevelKpi(kpiValues, kpiEnabled, relevanceThreshold, kpi.getThresholdType());
            cellLevelKpis.put(kpiName, cellLevelKpi);
        }

        return cellLevelKpis;
    }

    private static Map<String, SectorLevelKpi> generateSectorLevelKpis(final Map<String, Map<String, Object>> hourlyKpisByTimestamp,
            final Map<String, Map<String, Object>> degradationKpisByTimestamp, final JsonNode paSettings) {
        final Map<String, Map<String, Map<String, Object>>> kpisToTimestamp = sortKpiByKpiName(hourlyKpisByTimestamp,
                degradationKpisByTimestamp,
                PASector60Kpis::getThresholdNameForKpi);
        final Map<String, SectorLevelKpi> sectorLevelKpis = new HashMap<>();

        for (final PASector60Kpis kpi : PASector60Kpis.values()) {
            final String kpiName = kpi.getKpiName();
            final List<KpiValue> kpiValues = new ArrayList<>();
            final Map<String, Map<String, Object>> timestampMap = kpisToTimestamp.getOrDefault(kpiName, Collections.emptyMap());

            timestampMap.forEach((timestamp, valueMap) -> {
                final String value = String.valueOf(valueMap.get(VALUE_ATTR_NAME));
                final String threshold = String.valueOf(valueMap.get(THRESHOLD_ATTR_NAME));
                kpiValues.add(new KpiValue(value, timestamp, threshold));
            });

            final boolean kpiEnabled = paSettings.get(kpi.getSettingName())
                    .get(ENABLE_KPI_SETTING_NAME)
                    .asBoolean();

            final SectorLevelKpi sectorLevelKpi = new SectorLevelKpi(kpiValues, kpiEnabled, kpi.getLowerRangeLimit(), kpi.getUpperRangeLimit());
            sectorLevelKpis.put(kpiName, sectorLevelKpi);
        }

        return sectorLevelKpis;
    }

    static Map<String, Map<String, Map<String, Object>>> sortKpiByKpiName(final Map<String, Map<String, Object>> hourlyKpisByTimestamp,
            final Map<String, Map<String, Object>> degradationKpisByTimestamp, final UnaryOperator<String> kpiNameToThresholdNameFunction) {
        final Map<String, Map<String, Map<String, Object>>> kpisToTimestamp = new HashMap<>();

        hourlyKpisByTimestamp.forEach((timestamp, kpiMap) -> kpiMap.forEach((kpiName, kpiValue) -> {
            final Map<String, Object> reorderedKpiMap = new HashMap<>();
            reorderedKpiMap.put(VALUE_ATTR_NAME, kpiValue);
            if (degradationKpisByTimestamp.containsKey(timestamp)) {
                final Map<String, Object> degradationKpiMap = degradationKpisByTimestamp.get(timestamp);
                final String degradationThresholdKpiName = kpiNameToThresholdNameFunction.apply(kpiName);
                if (degradationKpiMap.containsKey(degradationThresholdKpiName)) {
                    reorderedKpiMap.put(THRESHOLD_ATTR_NAME, degradationKpiMap.get(degradationThresholdKpiName));
                }
            }
            if (!kpisToTimestamp.containsKey(kpiName)) {
                kpisToTimestamp.put(kpiName, new HashMap<>());
            }
            kpisToTimestamp.get(kpiName).put(timestamp, reorderedKpiMap);
        }));

        return kpisToTimestamp;
    }

    static String timestampToUtcString(final Timestamp timestamp) {
        return timestamp.toLocalDateTime().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }
}

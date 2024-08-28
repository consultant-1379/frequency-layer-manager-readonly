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

package com.ericsson.oss.services.sonom.flm.test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.Cell;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.CellLevelKpi;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.RelevanceThresholdType;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation.DegradationStatus;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation.DegradedCellKpi;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation.DegradedSectorKpi;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.KpiValue;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.Sector;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.SectorLevelKpi;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyInputEvent;

import io.cucumber.java.DataTableType;

public class PaPolicyStepsUtil {

    private static final String VALUE = "value";
    private static final String TIMESTAMP = "timestamp";
    private static final String THRESHOLD = "threshold";
    private static final String ENABLED = "enabled";
    private static final String RELEVANCE_THRESHOLD = "relevanceThreshold";
    private static final String RELEVANCE_THRESHOLD_TYPE = "relevanceThresholdType";
    private static final String UPPER_RANGE_LIMIT = "1.2";
    private static final String LOWER_RANGE_LIMIT = "0";
    private static final String KPI_NAME = "kpiName";
    private static final String SETTING_VALUE = "settingValue";
    private static final String SETTING_NAME = "numberOfKpiDegradedHoursThreshold";
    private static final String KPI_LEVEL = "kpiLevel";
    private static final String SECTOR = "sector";
    private static final int OSS_ID = 1;
    private static final String FDN = "fdn";
    private static final String SECTOR_ID = "sectorId";
    private static final String TOPOLOGY_ID = "topologyId";
    private static final String EXECUTION_ID = "executionId";
    private static final String PA_EXECUTION_ID = "paExecutionId";
    private static final Integer PA_WINDOW = 1;

    private static List<Cell> cells = Collections.emptyList();
    private static Sector sector;

    /**
     * Util method to convert Cucumber Datatable to {@link Cell}
     *
     * @param entry
     *            a map of table entries from feature file.
     * @return the parsed {@link Cell}
     */
    @DataTableType
    public Cell defineCell(final Map<String, String> entry) {
        final List<KpiValue> valueList = new ArrayList<>(1);
        valueList.add(new KpiValue(entry.get(VALUE), entry.get(TIMESTAMP), entry.get(THRESHOLD)));
        final CellLevelKpi cellKpi = new CellLevelKpi(valueList, Boolean.valueOf(entry.get(ENABLED)),
                entry.get(RELEVANCE_THRESHOLD), RelevanceThresholdType.valueOf(entry.get(RELEVANCE_THRESHOLD_TYPE)));
        final Map<String, CellLevelKpi> kpiMap = new HashMap<>(1);
        kpiMap.put(entry.get(KPI_NAME), cellKpi);
        return new Cell(entry.get(FDN), OSS_ID, kpiMap);
    }

    /**
     * Util method to convert Cucumber Datatable to {@link Sector}
     *
     * @param entry
     *            a map of table entries from feature file.
     * @return the parsed {@link Sector}
     */
    @DataTableType
    public Sector defineSector(final Map<String, String> entry) {
        final List<KpiValue> valueList = new ArrayList<>(1);
        valueList.add(new KpiValue(entry.get(VALUE),
                entry.get(TIMESTAMP), entry.get(THRESHOLD)));
        final SectorLevelKpi sectorKpi = new SectorLevelKpi(valueList, Boolean.valueOf(entry.get(ENABLED)), LOWER_RANGE_LIMIT, UPPER_RANGE_LIMIT);
        final Map<String, SectorLevelKpi> kpis = new HashMap<>(1);
        kpis.put(entry.get(KPI_NAME), sectorKpi);
        final Map<String, String> settings = new HashMap<>(1);
        settings.put(SETTING_NAME, entry.get(SETTING_VALUE));
        return new Sector(entry.get(SECTOR_ID), settings, kpis, cells);
    }

    /**
     * Util method to convert Cucumber Datatable to {@link PaPolicyInputEvent}
     *
     * @param entry
     *            a map of table entries from feature file.
     * @return the parsed {@link PaPolicyInputEvent}
     */
    @DataTableType
    public PaPolicyInputEvent definePaPolicyInputEvent(final Map<String, String> entry) {
        return new PaPolicyInputEvent(entry.get(EXECUTION_ID), entry.get(PA_EXECUTION_ID), PA_WINDOW, sector);
    }

    /**
     * Util method to convert Cucumber Datatable to {@link DegradationStatus}
     *
     * @param entry
     *            a map of table entries from feature file.
     * @return the parsed {@link DegradationStatus}
     */
    @DataTableType
    public static DegradationStatus defineDegradationStatus(final Map<String, String> entry) {
        final String verdict = "DEGRADED";
        final Map<String, DegradedSectorKpi> degradedSectorKpis = new HashMap<>(1);
        final Map<String, DegradedCellKpi> degradedCellKpis = new HashMap<>(1);
        final Map<String, List<String>> degradedKpiMap = new HashMap<>(1);
        degradedKpiMap.put(entry.get(TOPOLOGY_ID), Collections.singletonList(entry.get(TIMESTAMP)));
        if (entry.get(KPI_LEVEL).equals(SECTOR)) {
            final DegradedSectorKpi degradedSectorKpi = new DegradedSectorKpi(degradedKpiMap);
            degradedSectorKpis.put(entry.get(KPI_NAME), degradedSectorKpi);
        } else {
            final Map<String, Map<String, List<String>>> ossIdToFdnToDegradedTimestamps = new HashMap<>(1);
            ossIdToFdnToDegradedTimestamps.put("1", degradedKpiMap);
            final DegradedCellKpi degradedCellKpi = new DegradedCellKpi(ossIdToFdnToDegradedTimestamps);
            degradedCellKpis.put(entry.get(KPI_NAME), degradedCellKpi);
        }
        return new DegradationStatus(verdict, degradedSectorKpis, degradedCellKpis);
    }

    /**
     * Set the {@link List} of {@link Cell}'s to pass into {@link Sector} constructor.
     *
     * @param cells
     *            the cells to be set.
     */
    public static void setCells(final List<Cell> cells) {
        PaPolicyStepsUtil.cells = new ArrayList<>(cells);
    }

    /**
     * Set the {@link Sector} to pass into the {@link PaPolicyInputEvent} constructor.
     *
     * @param sector
     *            the optimization cells to be set.
     */
    public static void setSector(final Sector sector) {
        PaPolicyStepsUtil.sector = sector;
    }
}

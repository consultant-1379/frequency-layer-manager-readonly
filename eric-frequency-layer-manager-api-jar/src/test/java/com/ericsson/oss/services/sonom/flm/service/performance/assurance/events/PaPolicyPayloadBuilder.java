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

package com.ericsson.oss.services.sonom.flm.service.performance.assurance.events;

import java.util.ArrayList;
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

/**
 * Test Utility class for building input and output events for the FLM performance assurance policy.
 */
public final class PaPolicyPayloadBuilder {

    private static final String TIMESTAMP_ONE = "1970-01-01 00:00:00.0";
    private static final String TIMESTAMP_TWO = "1970-01-01 00:01:00.0";
    private static final String CELL_KPI_NAME = "sample_cell_kpi_name";
    private static final String SECTOR_KPI_NAME = "sample_sector_kpi_name";
    private static final String RELEVANCE_THRESHOLD = "99";
    private static final String NUM_HOURS_DEGRADED = "num_hours_degraded";
    private static final String FLM_EXECUTION_ID = "FLM_execution_one";
    private static final String FLM_PA_EXECUTION_ID = "FLM_PA_execution_one";
    private static final Integer PA_WINDOW = 1;
    private static final String FDN_ONE = "cell_one";
    private static final String FDN_TWO = "cell_two";
    private static final String VERDICT = "DEGRADED";
    private static final String LOWER_RANGE_LIMIT = "0";
    private static final String UPPER_RANGE_LIMIT = "1.2";

    private PaPolicyPayloadBuilder() {
    }

    public static PaPolicyInputEvent buildPaInputEvent() {
        return new PaPolicyInputEvent(FLM_EXECUTION_ID, FLM_PA_EXECUTION_ID, PA_WINDOW, getSector());
    }

    public static PaPolicyOutputEvent buildPaOutputEvent() {
        return new PaPolicyOutputEvent(FLM_EXECUTION_ID, FLM_PA_EXECUTION_ID, PA_WINDOW, getSector(), getDegradationStatus());
    }

    private static DegradationStatus getDegradationStatus() {
        final List<String> degradedTimes = new ArrayList<>(2);
        degradedTimes.add(TIMESTAMP_ONE);
        degradedTimes.add(TIMESTAMP_TWO);

        final Map<String, List<String>> degradedCells = new HashMap<>(2);
        degradedCells.put(FDN_ONE, degradedTimes);
        degradedCells.put(FDN_TWO, degradedTimes);
        final Map<String, Map<String, List<String>>> ossIdToFdnToDegradedCellKpis = new HashMap<>(1);
        ossIdToFdnToDegradedCellKpis.put("1", degradedCells);

        final Map<String, List<String>> degradedSector = new HashMap<>(2);
        degradedSector.put("101", degradedTimes);

        final DegradedCellKpi degradedCellKpi = new DegradedCellKpi(ossIdToFdnToDegradedCellKpis);

        final DegradedSectorKpi degradedSectorKpi = new DegradedSectorKpi(degradedSector);

        final Map<String, DegradedCellKpi> allDegradedCellKpis = new HashMap<>(1);
        final Map<String, DegradedSectorKpi> allDegradedSectorKpis = new HashMap<>(1);
        allDegradedCellKpis.put(CELL_KPI_NAME, degradedCellKpi);
        allDegradedSectorKpis.put(SECTOR_KPI_NAME, degradedSectorKpi);

        return new DegradationStatus(VERDICT, allDegradedSectorKpis, allDegradedCellKpis);
    }

    private static Sector getSector() {
        final List<KpiValue> cellValues = getCellKpiValues();
        final Map<String, CellLevelKpi> cellKpis = getCellLevelKpisToFdn(cellValues);
        final List<Cell> cells = getCells(cellKpis);
        final List<KpiValue> sectorValues = getSectorKpiValues();
        final Map<String, SectorLevelKpi> sectorKpis = getSectorLevelKpisTosectorId(sectorValues);
        final Map<String, String> settings = getSettings();
        return new Sector("101", settings, sectorKpis, cells);
    }

    private static List<KpiValue> getCellKpiValues() {
        final KpiValue kpiValueOne = new KpiValue("100", TIMESTAMP_ONE, "99");
        final KpiValue kpiValueTwo = new KpiValue("200", TIMESTAMP_TWO, "199");
        final List<KpiValue> cellValues = new ArrayList<>(2);
        cellValues.add(kpiValueOne);
        cellValues.add(kpiValueTwo);
        return cellValues;
    }

    private static List<KpiValue> getSectorKpiValues() {
        final KpiValue kpiValueOne = new KpiValue("100", TIMESTAMP_ONE, "99");
        final KpiValue kpiValueTwo = new KpiValue("200", TIMESTAMP_TWO, "199");
        final List<KpiValue> sectorValues = new ArrayList<>(2);
        sectorValues.add(kpiValueOne);
        sectorValues.add(kpiValueTwo);
        return sectorValues;
    }

    private static Map<String, CellLevelKpi> getCellLevelKpisToFdn(final List<KpiValue> kpiValues) {
        final CellLevelKpi cellKpi = new CellLevelKpi(kpiValues, true, RELEVANCE_THRESHOLD, RelevanceThresholdType.MAX);
        final Map<String, CellLevelKpi> cellKpis = new HashMap<>(1);
        cellKpis.put(CELL_KPI_NAME, cellKpi);
        return cellKpis;
    }

    private static Map<String, SectorLevelKpi> getSectorLevelKpisTosectorId(final List<KpiValue> kpiValues) {
        final SectorLevelKpi sectorKpi = new SectorLevelKpi(kpiValues, true, LOWER_RANGE_LIMIT, UPPER_RANGE_LIMIT);
        final Map<String, SectorLevelKpi> sectorKpis = new HashMap<>(1);
        sectorKpis.put(SECTOR_KPI_NAME, sectorKpi);
        return sectorKpis;
    }

    private static List<Cell> getCells(final Map<String, CellLevelKpi> cellKpis) {
        final Cell cellOne = new Cell(FDN_ONE, 1, cellKpis);
        final Cell cellTwo = new Cell(FDN_TWO, 1, cellKpis);
        final List<Cell> cells = new ArrayList<>(2);
        cells.add(cellOne);
        cells.add(cellTwo);
        return cells;
    }

    private static Map<String, String> getSettings() {
        final Map<String, String> settings = new HashMap<>(1);
        settings.put(NUM_HOURS_DEGRADED, "2");
        return settings;
    }

}

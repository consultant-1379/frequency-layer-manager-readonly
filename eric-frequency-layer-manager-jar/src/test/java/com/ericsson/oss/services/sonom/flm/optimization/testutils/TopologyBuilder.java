/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.optimization.testutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyInputEvent;

public class TopologyBuilder {

    private TopologyBuilder() {

    }

    public static Map<CellIdentifier, Map<String, Object>> buildCellKpis(final List<Cell> cells, final Map<String, Object> kpis) {
        final Map<CellIdentifier, Map<String, Object>> cellKpis = new HashMap<>(cells.size());
        for (final Cell cell : cells) {
            cellKpis.put(new CellIdentifier(cell.getOssId(), cell.getFdn()), kpis);
        }
        return cellKpis;
    }

    public static TopologySector buildTopologySector(final Long sectorId, final List<Cell> associatedCells) {
        return new TopologySector(sectorId, associatedCells);
    }

    public static List<Cell> buildAssociatedCellsForSector() {
        final Cell cellOne = new Cell(1l, 1, "fdnOne", 1400, "outdoor", "undefined");
        final Cell cellTwo = new Cell(2l, 1, "fdnTwo", 1400, "outdoor", "undefined");
        final List<Cell> associatedCells = new ArrayList<>(2);
        associatedCells.add(cellOne);
        associatedCells.add(cellTwo);
        return associatedCells;
    }

    public static PolicyInputEvent buildInputEvent(final String executionId, final Map<String, Object> kpis, final String sectorId) {
        final Map<String, String> cmAttrMap = new HashMap<>();
        cmAttrMap.put("bandwidth", "1400");
        cmAttrMap.put("caimc", "ACTIVATED");
        cmAttrMap.put("lteNrSpectrumShared", "undefined");

        final Map<String, String> settingsMap = new HashMap<>();

        final OptimizationCell cellOne = new OptimizationCell("fdnOne", 1, kpis, cmAttrMap, settingsMap);
        final OptimizationCell cellTwo = new OptimizationCell("fdnTwo", 1, kpis, cmAttrMap, settingsMap);
        final List<OptimizationCell> optimizationCells = new ArrayList<>();
        optimizationCells.add(cellOne);
        optimizationCells.add(cellTwo);
        return new PolicyInputEvent(optimizationCells, sectorId, executionId);
    }

    public static PolicyInputEvent buildInputEventWithoutCells(final String executionId) {
        final List<OptimizationCell> optimizationCells = new ArrayList<>();
        return new PolicyInputEvent(optimizationCells, "1", executionId);
    }

    public static PolicyInputEvent buildInputEventWithOneCell(final String executionId, final Map<String, Object> kpis, final String sectorId) {
        final Map<String, String> cmAttrMap = new HashMap<>();
        cmAttrMap.put("bandwidth", "1400");
        cmAttrMap.put("caimc", "ACTIVATED");
        cmAttrMap.put("lteNrSpectrumShared", "undefined");
        final Map<String, String> settingsMap = new HashMap<>();

        final OptimizationCell cellOne = new OptimizationCell("fdnOne", 1, kpis, cmAttrMap, settingsMap);
        final List<OptimizationCell> optimizationCells = new ArrayList<>();
        optimizationCells.add(cellOne);
        return new PolicyInputEvent(optimizationCells, sectorId, executionId);
    }

    public static List<TopologySector> buildListOfTopologySectors(final List<Cell> associatedCells) {
        final List<TopologySector> sectors = new ArrayList<>();
        sectors.add(buildTopologySector(1L, associatedCells));
        sectors.add(buildTopologySector(2L, associatedCells));
        return sectors;
    }
}

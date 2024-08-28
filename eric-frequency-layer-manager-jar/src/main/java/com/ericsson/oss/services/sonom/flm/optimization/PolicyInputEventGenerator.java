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

package com.ericsson.oss.services.sonom.flm.optimization;

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants.BANDWIDTH;
import static com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants.CAIMC;
import static com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants.LTENRSPECTRUMSHARED;
import static com.ericsson.oss.services.sonom.flm.cm.util.Caimc.NULL;
import static com.ericsson.oss.services.sonom.flm.cm.util.Caimc.UNDEFINED;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.EXCLUSION_LIST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Sector;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyInputEvent;

/**
 * This class generates a {@link PolicyInputEvent} for a {@link TopologySector}
 */
class PolicyInputEventGenerator {

    private final Map<CellIdentifier, Map<String, Object>> cellAllKpis;
    private final Map<CellIdentifier, Map<String, String>> cellSettings;
    private final Map<CellIdentifier, Set<Long>> multiSectorCells;
    private final CmStore cmStore;

    PolicyInputEventGenerator(final Map<CellIdentifier, Map<String, Object>> cellAllKpis,
                              final Map<CellIdentifier, Map<String, String>> cellSettings, final Map<CellIdentifier, Set<Long>> multiSectorCells,
                              final CmStore cmStore) {
        this.cellAllKpis = cellAllKpis;
        this.cellSettings = cellSettings;
        this.multiSectorCells = multiSectorCells;
        this.cmStore = cmStore;
    }

    PolicyInputEventGenerator(final CmStore cmStore) {
        cellAllKpis = new HashMap<>();
        cellSettings = new HashMap<>();
        multiSectorCells = new HashMap<>();
        this.cmStore = cmStore;
    }

    /**
     * Generates a {@link PolicyInputEvent}
     *
     * @param sector
     *            The sectors for which to generate the Input Event
     * @param executionId
     *            The FLM Execution ID to use for the input event
     * @return A {@link PolicyInputEvent} oto represent a sector
     */
    PolicyInputEvent generateInputEvent(final TopologySector sector, final String executionId) {
        return new PolicyInputEvent(generateOptimizationCellList(sector, executionId), sector.getSectorId().toString(), executionId);
    }

    private List<OptimizationCell> generateOptimizationCellList(final TopologySector sector, final String executionId) {
        final String sectorId = sector.getSectorId().toString();
        return sector.getAssociatedCells()
                .stream()
                .filter(cell -> isOutdoorCell(sectorId, cell, executionId))
                .map(c -> new OptimizationCell(c.getFdn(), c.getOssId(), getKpis(c),
                        getCMAttributes(c, sector), getCellSettings(c)))
                .filter(optimizationCell -> isNotExcludedCell(sectorId, optimizationCell, executionId))
                .collect(Collectors.toList());
    }

    private boolean isOutdoorCell(final String sectorId, final Cell cell, final String executionId) {
        if (!cell.getInstallationType().equalsIgnoreCase(ModelConstants.INDOOR)) {
            return true;
        }

        LoggingFormatter.logFilteredCell(executionId,
                String.valueOf(cell.getOssId()),
                sectorId,
                cell.getFdn(),
                "Cell excluded from optimization as installationType is: " + cell.getInstallationType());
        return false;
    }

    private boolean isNotExcludedCell(final String sectorId, final OptimizationCell cell, final String executionId) {
        final String exclusionListSetting = (String) cell.getSettings().get(EXCLUSION_LIST);
        if (exclusionListSetting != null) {
            LoggingFormatter.logFilteredCell(executionId, String.valueOf(cell.getOssId()), sectorId, cell.getFdn(),
                    "Cell excluded from optimization because it is in exclusion list: " + exclusionListSetting);
            return false;
        }
        final CellIdentifier cellIdentifier = new CellIdentifier(cell.getOssId(), cell.getFdn());
        if (multiSectorCells.containsKey(cellIdentifier)) {
            final Set<Long> sectors = multiSectorCells.get(cellIdentifier);
            LoggingFormatter.logFilteredCell(executionId, String.valueOf(cell.getOssId()), sectorId, cell.getFdn(),
                    String.format("Cell excluded from optimization due to presence in multiple sectors: %s", StringUtils.join(sectors, ",")));
            return false;
        }
        return true;
    }

    private Map<String, Object> getKpis(final Cell cell) {
        return cellAllKpis.getOrDefault(getCellIdentifier(cell), Collections.emptyMap());
    }

    private Map<String, String> getCellSettings(final Cell cell) {
        return cellSettings.getOrDefault(getCellIdentifier(cell), Collections.emptyMap());
    }

    private static CellIdentifier getCellIdentifier(final Cell cell) {
        return new CellIdentifier(cell.getOssId(), cell.getFdn());
    }

    private Map<String, String> getCMAttributes(final Cell cell, final Sector sector) {
        final Map<String, String> cmAttrMap = new HashMap<>();
        cmAttrMap.put(BANDWIDTH, String.valueOf(cell.getBandwidth()));
        cmAttrMap.put(CAIMC, getCapabilityAwareIdleModeControlActivated(cell, sector));
        cmAttrMap.put(LTENRSPECTRUMSHARED, cell.getlteNrSpectrumShared());
        return cmAttrMap;
    }

    private String getCapabilityAwareIdleModeControlActivated(final Cell cell, final Sector sector) {
        final Collection<Long> collectionSector = new ArrayList<>();
        final Collection<TopologyObjectId> collectionCell = new ArrayList<>();
        final Node node;
        collectionSector.add(sector.getSectorId());
        collectionCell.add(cell.getTopologyObjectId());
        try {
            node = cmStore.getCmNodeObjectsStore(collectionCell, collectionSector).getNodeForCellFdn(cell.getFdn(), cell.getOssId());
        } catch (final FlmAlgorithmException e) {
            return NULL.getCaimcValue();
        }
        if (node != null) {
            if (node.getFeatureState() != null) {
                return node.getFeatureState().getCapabilityAwareIdleModeControlActivated();
            }
            return UNDEFINED.getCaimcValue();
        }
        return NULL.getCaimcValue();
    }
}
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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator;

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants.OUTDOOR;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_SOURCE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_TARGET_ONE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_TARGET_TWO;

import java.util.List;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;


public class EnrichedPolicyOutputEventPrototypeCellLevelInfo extends EnrichedPolicyOutputEventPrototype {

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        super(enrichedPolicyOutputEvent);
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo connectedUsersAtSource(final int connectedUsers) {
        enrichedPolicyOutputEvent.getCellKpis().put(CELL_SOURCE,
                createModifiedCellKpisWithConnectedUsers(connectedUsers, CELL_SOURCE));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo subscriptionRatioAtSource(final float subscriptionRatio) {
        enrichedPolicyOutputEvent.getCellKpis().put(CELL_SOURCE,
                createModifiedCellKpisWithSubscriptionRatio(subscriptionRatio, CELL_SOURCE));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo bandWidthAtSource(final int newBandWidth) {
        final Cell cell = enrichedPolicyOutputEvent.getCell(CELL_SOURCE);
        if (cell.getBandwidth() != newBandWidth) {
            enrichedPolicyOutputEvent.getCellCmData().put(CELL_SOURCE, buildNewCell(cell, newBandWidth));
        }
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo distrLoadsAtSource(final List<Integer> distributionLoads) {
        enrichedPolicyOutputEvent.getCellKpis().put(CELL_SOURCE,
                createModifiedCellKpisWithDistributionLoads(distributionLoads, CELL_SOURCE));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo idleModePrioAtReleaseRefAtSource(final String idleModePrioAtReleaseRef) {
        final Cell cell = enrichedPolicyOutputEvent.getCell(CELL_SOURCE);
        if (!cell.getIdleModePrioAtReleaseRef().equals(idleModePrioAtReleaseRef)) {
            enrichedPolicyOutputEvent.getCellCmData().put(CELL_SOURCE, buildNewCellWithIdleModePrioAtReleaseRef(cell, idleModePrioAtReleaseRef));
        }
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo connectedUsersAtTargetOne(final int connectedUsers) {
        enrichedPolicyOutputEvent.getCellKpis().put(CELL_TARGET_ONE,
                createModifiedCellKpisWithConnectedUsers(connectedUsers, CELL_TARGET_ONE));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo subscriptionRatioAtTargetOne(final float subscriptionRatio) {
        enrichedPolicyOutputEvent.getCellKpis().put(CELL_TARGET_ONE,
                createModifiedCellKpisWithSubscriptionRatio(subscriptionRatio, CELL_TARGET_ONE));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo bandWidthAtTargetOne(final int newBandWidth) {
        final Cell cell = enrichedPolicyOutputEvent.getCell(CELL_TARGET_ONE);
        if (cell.getBandwidth() != newBandWidth) {
            enrichedPolicyOutputEvent.getCellCmData().put(CELL_TARGET_ONE, buildNewCell(cell, newBandWidth));
        }
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo distrLoadsAtTargetOne(final List<Integer> distributionLoads) {
        enrichedPolicyOutputEvent.getCellKpis().put(CELL_TARGET_ONE,
                createModifiedCellKpisWithDistributionLoads(distributionLoads, CELL_TARGET_ONE));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo idleModePrioAtReleaseRefAtTargetOne(final String idleModePrioAtReleaseRef) {
        final Cell cell = enrichedPolicyOutputEvent.getCell(CELL_TARGET_ONE);
        if (!cell.getIdleModePrioAtReleaseRef().equals(idleModePrioAtReleaseRef)) {
            enrichedPolicyOutputEvent.getCellCmData().put(CELL_TARGET_ONE, buildNewCellWithIdleModePrioAtReleaseRef(cell, idleModePrioAtReleaseRef));
        }
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo connectedUsersAtTargetTwo(final int connectedUsers) {
        enrichedPolicyOutputEvent.getCellKpis().put(CELL_TARGET_TWO,
                createModifiedCellKpisWithConnectedUsers(connectedUsers, CELL_TARGET_TWO));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo subscriptionRatioAtTargetTwo(final float subscriptionRatio) {
        enrichedPolicyOutputEvent.getCellKpis().put(CELL_TARGET_TWO,
                createModifiedCellKpisWithSubscriptionRatio(subscriptionRatio, CELL_TARGET_TWO));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo bandWidthAtTargetTwo(final int newBandWidth) {
        final Cell cell = enrichedPolicyOutputEvent.getCell(CELL_TARGET_TWO);
        if (cell.getBandwidth() != newBandWidth) {
            enrichedPolicyOutputEvent.getCellCmData().put(CELL_TARGET_TWO, buildNewCell(cell, newBandWidth));
        }
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo distrLoadsAtTargetTwo(final List<Integer> distributionLoads) {
        enrichedPolicyOutputEvent.getCellKpis().put(CELL_TARGET_TWO,
                createModifiedCellKpisWithDistributionLoads(distributionLoads, CELL_TARGET_TWO));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo idleModePrioAtReleaseRefAtTargetTwo(final String idleModePrioAtReleaseRef) {
        final Cell cell = enrichedPolicyOutputEvent.getCell(CELL_TARGET_TWO);
        if (!cell.getIdleModePrioAtReleaseRef().equals(idleModePrioAtReleaseRef)) {
            enrichedPolicyOutputEvent.getCellCmData().put(CELL_TARGET_TWO, buildNewCellWithIdleModePrioAtReleaseRef(cell, idleModePrioAtReleaseRef));
        }
        return this;
    }

    private CellKpis createModifiedCellKpisWithConnectedUsers(final int connectedUsers,
                                                              final TopologyObjectId cellId) {
        final CellKpis oldCellKpis = enrichedPolicyOutputEvent.getCellKpis().get(cellId);
        return new CellKpis(connectedUsers,
                oldCellKpis.getSubscriptionRatio(),
                oldCellKpis.getPmIdleModeRelDistrHighLoad(),
                oldCellKpis.getPmIdleModeRelDistrMediumHighLoad(),
                oldCellKpis.getPmIdleModeRelDistrMediumLoad(),
                oldCellKpis.getPmIdleModeRelDistrLowMediumLoad(),
                oldCellKpis.getPmIdleModeRelDistrLowLoad());
    }

    private CellKpis createModifiedCellKpisWithSubscriptionRatio(final float subscriptionRatio,
                                                                 final TopologyObjectId cellId) {
        final CellKpis oldCellKpis = enrichedPolicyOutputEvent.getCellKpis().get(cellId);
        return new CellKpis(oldCellKpis.getConnectedUsers(),
                subscriptionRatio,
                oldCellKpis.getPmIdleModeRelDistrHighLoad(),
                oldCellKpis.getPmIdleModeRelDistrMediumHighLoad(),
                oldCellKpis.getPmIdleModeRelDistrMediumLoad(),
                oldCellKpis.getPmIdleModeRelDistrLowMediumLoad(),
                oldCellKpis.getPmIdleModeRelDistrLowLoad());
    }

    private CellKpis createModifiedCellKpisWithDistributionLoads(final List<Integer> distributionLoads,
                                                                 final TopologyObjectId cellId) {
        final CellKpis oldCellKpis = enrichedPolicyOutputEvent.getCellKpis().get(cellId);
        return new CellKpis(oldCellKpis.getConnectedUsers(),
                oldCellKpis.getSubscriptionRatio(),
                distributionLoads.get(4),
                distributionLoads.get(3),
                distributionLoads.get(2),
                distributionLoads.get(1),
                distributionLoads.get(0));
    }

    private Cell buildNewCell(final Cell cell, final int newBandWidth) {
        return new Cell(cell.getCellId(), cell.getOssId(), cell.getFdn(), cell.getCarrier(),
                cell.getIdleModePrioAtReleaseRef(), cell.getCgi(), newBandWidth, OUTDOOR, cell.getlteNrSpectrumShared());
    }

    private Cell buildNewCellWithIdleModePrioAtReleaseRef(final Cell cell, final String idleModePrioAtReleaseRef) {
        return new Cell(cell.getCellId(), cell.getOssId(), cell.getFdn(), cell.getCarrier(),
                idleModePrioAtReleaseRef, cell.getCgi(), cell.getBandwidth(), OUTDOOR, cell.getlteNrSpectrumShared());
    }

}

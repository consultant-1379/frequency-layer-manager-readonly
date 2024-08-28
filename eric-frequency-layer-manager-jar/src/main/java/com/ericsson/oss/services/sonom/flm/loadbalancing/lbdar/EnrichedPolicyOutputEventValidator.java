/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileValidator.areDistributionInfosValid;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileValidator.areSumOfDistributionValuesValid;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileValidator.isThresholdValid;
import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.logFilteredSector;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;

/**
 * This class needs to validate the given EnrichedPolicyOutputEvent. It needs to check if all the idleModePrioAtRelease are available for all the
 * cells of PolicyOutputEvent.
 */
public class EnrichedPolicyOutputEventValidator {

    private static final String EXCLUSION_REASON_MESSAGE_FOR_SECTOR = "No optimization is possible for sector because";

    /**
     * This method should return true if all the data is available to calculate ProfileChanges by ProfileChangeCalculator.
     * 
     * @param enrichedPolicyOutputEvent
     *            an object of {@link EnrichedPolicyOutputEvent}
     * @return true if the input is valid, false otherwise
     */
    public boolean isValid(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {

        if (enrichedPolicyOutputEvent.getSourceCellNode() == null) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " node for source cell %s is null",
                            enrichedPolicyOutputEvent.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellFdn()));
            return false;
        }
        if (enrichedPolicyOutputEvent.getCellKpis() == null) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " Subscription Ratio or LBDAR counters missing");
            return false;
        }
        if (areKpisMissing(enrichedPolicyOutputEvent)) {
            return false;
        }

        if (areCellCmDataMissing(enrichedPolicyOutputEvent)) {
            return false;
        }

        if (duplicateCarrierCellsInSectorFound(enrichedPolicyOutputEvent)) {
            return false;
        }

        if (sourceOrTargetFrequencyIsMissing(enrichedPolicyOutputEvent)) {
            return false;
        }

        if (!isProfileValid(enrichedPolicyOutputEvent)) {
            return false;
        }

        if (!areProfilesExistForCells(enrichedPolicyOutputEvent)) {
            return false;
        }

        return enrichedPolicyOutputEvent.getSourceCellNode().getFeatureState().isLoadBasedDistributionAtReleaseActivated();
    }

    private boolean duplicateCarrierCellsInSectorFound(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        final long numberOfDistinctCarriers = enrichedPolicyOutputEvent.getCellCmData().values().stream()
                .map(Cell::getCarrier)
                .distinct()
                .count();
        if (numberOfDistinctCarriers != enrichedPolicyOutputEvent.getCellCmData().size()) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " Sector contains multiple cells with same carrier");
            return true;
        }
        return false;
    }

    private boolean sourceOrTargetFrequencyIsMissing(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        final boolean sourceFrequencyFound = enrichedPolicyOutputEvent.getFrequencyToCarrier()
                .containsValue(getSourceCellCarrier(enrichedPolicyOutputEvent));
        if (!sourceFrequencyFound) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " no frequency found with carrier of source cell");
            return true;
        }
        final boolean targetFrequenciesFound = enrichedPolicyOutputEvent.getFrequencyToCarrier()
                .values().containsAll(getTargetCarriers(enrichedPolicyOutputEvent));
        if (!targetFrequenciesFound) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " no frequency found with carrier of target cells");
            return true;
        }
        return false;
    }

    private boolean areCellCmDataMissing(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        for (final TopologyObjectId cellId : getAllCellIds(enrichedPolicyOutputEvent)) {
            if (!enrichedPolicyOutputEvent.getCellCmData().containsKey(cellId)) {
                logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                        String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " CellCmData is missing at cell %s",
                                cellId.getFdn()));
                return true;
            }
            if (isEmpty(enrichedPolicyOutputEvent.getCellCmData().get(cellId).getIdleModePrioAtReleaseRef())) {
                logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                        String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " profile reference is missing at cell %s",
                                cellId.getFdn()));
                return true;
            }
        }
        return false;
    }

    private boolean areKpisMissing(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        final Map<TopologyObjectId, CellKpis> cellKpis = enrichedPolicyOutputEvent.getCellKpis();
        for (final TopologyObjectId cellId : getAllCellIds(enrichedPolicyOutputEvent)) {
            if (!cellKpis.containsKey(cellId)) {
                logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                        String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " CellKpi is missing at cell %s",
                                cellId.getFdn()));
                return true;
            }
            if (cellKpis.get(cellId).getSubscriptionRatio() == 0.0d) {
                logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                        String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " subscription ratio is zero at cell %s",
                                cellId.getFdn()));
                return true;
            }
            if (cellKpis.get(cellId).getConnectedUsers() == 0.0d) {
                logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                        String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " connected user number is zero at cell %s",
                                cellId.getFdn()));
                return true;
            }
        }
        return false;
    }

    private List<TopologyObjectId> getAllCellIds(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        final List<TopologyObjectId> cellIds = getTargetCellIds(enrichedPolicyOutputEvent);
        final String sourceCellFdn = enrichedPolicyOutputEvent.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellFdn();
        final int sourceCellOssId = enrichedPolicyOutputEvent.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellOssId();
        cellIds.add(TopologyObjectId.of(sourceCellFdn, sourceCellOssId));
        return cellIds;
    }

    private boolean isProfileValid(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        if (enrichedPolicyOutputEvent.getProfiles() == null || enrichedPolicyOutputEvent.getProfiles().isEmpty()) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " profiles don't exists");
            return false;
        }

        if (!isDistributionInfoAtSourceProfileValid(enrichedPolicyOutputEvent, getSourceCellCarrier(enrichedPolicyOutputEvent),
                enrichedPolicyOutputEvent.getProfiles().get(getSourceProfileId(enrichedPolicyOutputEvent)))) {
            return false;
        }

        for (final EnrichedIdleModePrioAtRelease profile : enrichedPolicyOutputEvent.getProfiles().values()) {
            if (!areDistributionInfosValid(profile)) {
                logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                        String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " not all of the frequency " +
                                "list contains frequency references in profile '%s'", profile.getFdn()));
                return false;
            }

            if (!isThresholdValid(profile)) {
                logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                        String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " threshold limits are not consistent in " +
                                "profile '%s'", profile.getFdn()));
                return false;
            }

            if (!areSumOfDistributionValuesValid(profile)) {
                logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                        String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " distributionInfo is greater than 100 percent at profile '%s'",
                                profile.getFdn()));
                return false;
            }

        }
        return true;
    }

    private TopologyObjectId getSourceProfileId(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        final String sourceCellFdn = enrichedPolicyOutputEvent.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellFdn();
        final int sourceCellOssId = enrichedPolicyOutputEvent.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellOssId();
        final TopologyObjectId sourceId = TopologyObjectId.of(sourceCellFdn, sourceCellOssId);
        return TopologyObjectId.of(enrichedPolicyOutputEvent.getCellCmData().get(sourceId).getIdleModePrioAtReleaseRef(),
                sourceCellOssId);
    }

    private boolean isDistributionInfoAtSourceProfileValid(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent,
            final Integer sourceCellCarrier, final EnrichedIdleModePrioAtRelease profile) {
        final List<Integer> targetCarriers = getTargetCarriers(enrichedPolicyOutputEvent);
        final String executionId = enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId();
        final String sectorId = String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId());
        final String sourceCellFdn = enrichedPolicyOutputEvent.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellFdn();
        final String profileFdn = profile.getFdn();
        for (final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo distributionInfo : profile.getDistributionInfos()) {
            final boolean selfRetain = distributionInfo.containsCarrier(sourceCellCarrier);
            if (!selfRetain) {
                logFilteredSector(executionId, sectorId,
                        String.format(
                                EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " Source cell is not configured at Source frequency " +
                                        "for given Source FDN: %s , Profile FDN: %s , Source Cell Carrier: %d",
                                sourceCellFdn, profileFdn, sourceCellCarrier));
                return false;
            }
            if (!isFreqAndDistributionListValid(targetCarriers, distributionInfo)) {
                logFilteredSector(executionId, sectorId,
                        String.format(
                                EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " Source cell has no proper distribution value at Target frequency "
                                        + "for given Source FDN: %s , Profile FDN: %s , Target Carriers: %s",
                                sourceCellFdn, profileFdn, targetCarriers));
                return false;
            }
        }
        return true;
    }

    private boolean isFreqAndDistributionListValid(final List<Integer> targetCarriers,
            final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo distributionInfo) {
        return targetCarriers.stream()
                .allMatch(distributionInfo::distributionValueIsPositive);
    }

    private Integer getSourceCellCarrier(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        final TopologyObjectId sourceTopId = TopologyObjectId.of(enrichedPolicyOutputEvent
                .getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellFdn(),
                enrichedPolicyOutputEvent.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellOssId());

        final Cell sourceCell = enrichedPolicyOutputEvent.getCellCmData().get(sourceTopId);
        return sourceCell == null ? null : sourceCell.getCarrier();
    }

    private List<Integer> getTargetCarriers(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        final List<TopologyObjectId> targetCells = getTargetCellIds(enrichedPolicyOutputEvent);

        return targetCells.stream()
                .map(targetCellId -> enrichedPolicyOutputEvent.getCellCmData().get(targetCellId).getCarrier())
                .collect(Collectors.toList());
    }

    private List<TopologyObjectId> getTargetCellIds(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        return enrichedPolicyOutputEvent
                .getTargetCells()
                .stream()
                .map(t -> TopologyObjectId.of(t.getTargetCellFdn(), t.getTargetCellOssId()))
                .collect(Collectors.toList());
    }

    private boolean areProfilesExistForCells(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        return getAllCellIds(enrichedPolicyOutputEvent).stream()
                .map(enrichedPolicyOutputEvent::getCell)
                .allMatch(cell -> {
                    if (!enrichedPolicyOutputEvent.getProfiles().containsKey(TopologyObjectId.of(cell.getIdleModePrioAtReleaseRef(),
                            cell.getOssId()))) {
                        logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                                String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                                EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " profile not found " + cell.getIdleModePrioAtReleaseRef());
                        return false;
                    }
                    return true;
                });
    }
}

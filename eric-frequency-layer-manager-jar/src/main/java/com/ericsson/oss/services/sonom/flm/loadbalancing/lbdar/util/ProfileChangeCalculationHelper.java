/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.logFilteredSector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * A helper class which helps the ProfileChangeCalculatorImpl in the calculation. It contains some repetitive calculation methods related to the
 * calculation and profile manipulation.
 */
public class ProfileChangeCalculationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileChangeCalculationHelper.class);
    private static final int THRESHOLD_MULTIPLIER = 1000;
    private static final String TARGET_CELL_SELF_RETAIN_EQUALS_NULL_OR_ZERO_MESSAGE = "Change excluded as target cell self retain " +
            "value is null or zero";


    private final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private final TopologyObjectId sourceCellId;
    private final int sourceCellCarrier;

    //CellId to Threshold level
    private final Map<TopologyObjectId, Integer> selectedThresholds = new HashMap<>();

    public ProfileChangeCalculationHelper(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) throws LbdarException {
        this.enrichedPolicyOutputEvent = enrichedPolicyOutputEvent;
        sourceCellId = getSourceCellId();
        sourceCellCarrier = enrichedPolicyOutputEvent.getCell(sourceCellId).getCarrier();
        setActiveThresholdsForProfilesInLbq();
    }

    /**
     * Finds the active threshold of a profile and set it to the selectedThreshold map.
     *
     * @param cellKpis
     *            is a {@link CellKpis} which is needed to set the active threshold
     * @param profile
     *            is a {@link EnrichedIdleModePrioAtRelease} where the active threshold should be found
     * @param cellId
     *            is a {@link TopologyObjectId} id of the Cell
     * @throws LbdarException
     *             when threshold selection is unsuccessful
     */
    public final void setActiveThreshold(final CellKpis cellKpis, final EnrichedIdleModePrioAtRelease profile,
            final TopologyObjectId cellId) throws LbdarException {
        long distrLoadNumber = 0;
        Optional<Integer> level = Optional.empty();
        final List<Long> distrLoads = cellKpis.getPmIdleModeReleaseCounters();
        for (int i = 1; i < ThresholdLevel.values().length; i++) {
            if (profile.getThresholds().get(i) >= cellKpis.getSubscriptionRatio() * THRESHOLD_MULTIPLIER &&
                    cellKpis.getSubscriptionRatio() * THRESHOLD_MULTIPLIER >= profile.getThresholds().get(i - 1)
                    && (distrLoads.get(i - 1) > distrLoadNumber || distrLoadNumber == 0)) {
                level = Optional.of(i - 1);
                distrLoadNumber = distrLoads.get(i - 1);
            }
        }
        if (cellKpis.getSubscriptionRatio() * THRESHOLD_MULTIPLIER >= profile.getThresholds().get(ThresholdLevel.values().length - 1)
                && (distrLoads.get(ThresholdLevel.HIGH_LOAD_THRESHOLD.ordinal()) > distrLoadNumber || !level.isPresent())) {
            level = Optional.of(ThresholdLevel.values().length - 1);
        }
        selectedThresholds.put(cellId, level.orElseThrow(() -> {
            final String errorMessage = String.format("Threshold selection was unsuccessful at cell '%s'", cellId);
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()), errorMessage);
            return new LbdarException(errorMessage);
        }));
        if (LOGGER.isInfoEnabled()) {
            level.ifPresent(thresholdIndex -> LOGGER.info(LoggingFormatter.formatMessage(
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                    String.format("Selected threshold level for cell %s is %s", cellId, ThresholdLevel.values()[thresholdIndex].name()))));
        }
    }

    public final TopologyObjectId getSourceCellId() {
        return sourceCellId == null
                ? TopologyObjectId.of(enrichedPolicyOutputEvent.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellFdn(),
                        enrichedPolicyOutputEvent.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellOssId())
                : sourceCellId;
    }

    public int getSourceCellCarrier() {
        return sourceCellCarrier;
    }

    public Integer getSelectedThreshold(final TopologyObjectId cellId) {
        return selectedThresholds.getOrDefault(cellId, -1);
    }

    public EnrichedPolicyOutputEvent getEnrichedPolicyOutputEvent() {
        return enrichedPolicyOutputEvent;
    }

    /**
     * Set a new distribution value in a profile. It modifies the self-retain value as well.
     *
     * @param profile
     *            is a {@link EnrichedIdleModePrioAtRelease} where the change has to be done
     * @param sourceCell
     *            is a {@link TopologyObjectId} and this is the start/source cell
     * @param targetCell
     *            is a {@link TopologyObjectId} and this is the end/target cell
     * @param distrValue
     *            the value which is needed to set
     * @return the modified profile
     * @throws LbdarException
     *             if a cell's carrier cannot be uniquely identified
     */
    public EnrichedIdleModePrioAtRelease updateDistributionOfProfile(final EnrichedIdleModePrioAtRelease profile, final TopologyObjectId sourceCell,
            final TopologyObjectId targetCell, final float distrValue) throws LbdarException {
        final List<EnrichedDistributionInfo> distributionInfos = profile.getDistributionInfos();
        final EnrichedDistributionInfo distributionInfo = distributionInfos
                .get(getSelectedThreshold(sourceCell));
        final int sourceCarrier = enrichedPolicyOutputEvent.getCellCmData().get(sourceCell).getCarrier();
        final int targetCarrier = enrichedPolicyOutputEvent.getCellCmData().get(targetCell).getCarrier();
        final int indexOfOne = distributionInfo.getIndexOfCarrier(sourceCarrier);
        final int indexOfTwo = distributionInfo.getIndexOfCarrier(targetCarrier);

        final Float oldOwnDistribution = distributionInfo.getDistributionOfFrequency(sourceCarrier);
        validateOldOwnDistribution(oldOwnDistribution, enrichedPolicyOutputEvent);

        final List<Float> distributionList = distributionInfo.getFreqDistributionList();
        final List<String> eUtranFreqRefList = distributionInfo.getEUtranFreqRefList();
        final List<Integer> carriers = distributionInfo.getCarriers();
        float oldLeaveDistribution = 0;
        if (indexOfTwo == -1) {
            distributionList.add(distrValue);
            eUtranFreqRefList.add(enrichedPolicyOutputEvent.getFrequencyForCarrier(targetCarrier, targetCell));
            carriers.add(targetCarrier);
        } else {
            oldLeaveDistribution = distributionInfo.getFreqDistributionList().get(indexOfTwo);
            distributionList.set(indexOfTwo, distrValue);
        }
        distributionList.set(indexOfOne, oldOwnDistribution - (distrValue - oldLeaveDistribution));

        return profile.getModifiedCopy(new EnrichedDistributionInfo(distributionInfo.getThresholdLevel(), distributionList, eUtranFreqRefList,
                carriers));
    }

    /**
     * Gives back the actual distributionInfo of a Profile.
     *
     * @param profile
     *            is a {@link EnrichedIdleModePrioAtRelease} from where the actual distribution info is required
     * @param cellId
     *            is a {@link TopologyObjectId} the cell which is needed to select the actual distribution level
     * @return the actual DistributionInfo
     */
    public EnrichedDistributionInfo getSelectedDistributionInfo(final EnrichedIdleModePrioAtRelease profile,
            final TopologyObjectId cellId) {
        return profile.getDistributionInfos()
                .get(getSelectedThreshold(cellId));
    }

    /**
     * Converts a TargetCell to a Topology Object.
     *
     * @param targetCell
     *            the target cell which is needed to convert to Topology Object.
     * @return a Topology Object
     */
    public static TopologyObjectId getTopologyFromTarget(final TargetCell targetCell) {
        return TopologyObjectId.of(targetCell.getTargetCellFdn(),
                targetCell.getTargetCellOssId());
    }

    /**
     * Returns the list of Target cell's id where user leakage is possible and leakage handling should be investigated.
     *
     * @param profileChanges
     *            a Map of profile changes.
     * @return list of TopologyObjectId of the targetCells where leakage is possible
     */
    public List<TopologyObjectId> getTargetCellsWithIncreasedUsers(final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileChanges) {
        if (!profileChanges.containsKey(sourceCellId)) {
            return Collections.emptyList();
        }

        final List<TopologyObjectId> targetCellIds = new ArrayList<>();

        final EnrichedDistributionInfo originalSourceDist = getSelectedDistributionInfo(enrichedPolicyOutputEvent
                .getProfileFromCellId(sourceCellId), sourceCellId);
        final EnrichedDistributionInfo newSourceDist = getSelectedDistributionInfo(profileChanges.get(sourceCellId), sourceCellId);

        for (int i = 0; i < newSourceDist.getFreqDistributionList().size(); i++) {
            if (newSourceDist.getFreqDistributionList().get(i) > originalSourceDist.getFreqDistributionList().get(i)) {
                targetCellIds.add(enrichedPolicyOutputEvent.getCellByCarrier(newSourceDist.getCarriers().get(i)));
            }
        }
        return targetCellIds;
    }

    private void setActiveThresholdsForProfilesInLbq() throws LbdarException {
        for (final TopologyObjectId cellId : getAllCellIdFromLbq()) {
            final CellKpis cellKpis = enrichedPolicyOutputEvent.getCellKpis().get(cellId);
            final EnrichedIdleModePrioAtRelease profile = enrichedPolicyOutputEvent.getProfileFromCellId(cellId);
            setActiveThreshold(cellKpis, profile, cellId);
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                    "The active threshold levels are chosen"));
        }
    }

    private List<TopologyObjectId> getAllCellIdFromLbq() {
        final List<TopologyObjectId> cellIds = new ArrayList<>();
        cellIds.add(sourceCellId);
        for (final TargetCell targetCell : enrichedPolicyOutputEvent.getTargetCells()) {
            cellIds.add(TopologyObjectId.of(targetCell.getTargetCellFdn(), targetCell.getTargetCellOssId()));
        }
        return cellIds;
    }

    private void validateOldOwnDistribution(final Float oldOwnDistribution, final EnrichedPolicyOutputEvent changesCalculationInput)
            throws LbdarException {
        if (oldOwnDistribution == null || oldOwnDistribution == 0) {
            logFilteredSector(changesCalculationInput.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(changesCalculationInput.getPolicyOutputEvent().getSectorId()),
                    TARGET_CELL_SELF_RETAIN_EQUALS_NULL_OR_ZERO_MESSAGE);
            throw new LbdarException(TARGET_CELL_SELF_RETAIN_EQUALS_NULL_OR_ZERO_MESSAGE);

        }
    }

}

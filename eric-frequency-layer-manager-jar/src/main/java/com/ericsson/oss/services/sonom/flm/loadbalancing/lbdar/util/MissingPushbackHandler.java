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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.FormulaSolver.calculateBalancedDistribution;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.ProfileChangeCalculationHelper.getTopologyFromTarget;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.DistributionInfo;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileChangesCalculatorImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * This class is used by {@link ProfileChangesCalculatorImpl} to handle the missing pushback values. When at target cell the pushback value is missing
 * to source cell, the default value will be used as replacement, and the push value from source cell to target cell will be adjusted.
 */
public class MissingPushbackHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissingPushbackHandler.class);

    private final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private final ProfileChangeCalculatorSettings configs;
    private final ProfileChangeCalculationHelper helper;

    public MissingPushbackHandler(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent,
            final ProfileChangeCalculatorSettings configs,
            final ProfileChangeCalculationHelper helper) {
        this.enrichedPolicyOutputEvent = enrichedPolicyOutputEvent;
        this.configs = configs;
        this.helper = helper;
    }

    /**
     * Replaces the missing pushback values with the default value.
     * 
     * @return a map with profile changes where the missing pushback is replaced
     * @throws LbdarException
     *             if a cell's carrier cannot be uniquely identified
     */
    public Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> replaceMissingPushbackWithDefault() throws LbdarException {
        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileChanges = new HashMap<>();
        for (final TargetCell targetCell : enrichedPolicyOutputEvent.getTargetCells()) {
            final TopologyObjectId targetCellId = getTopologyFromTarget(targetCell);
            final EnrichedIdleModePrioAtRelease profileTarget = enrichedPolicyOutputEvent.getProfileFromCellId(targetCellId);
            if (!hasPushBackToSource(profileTarget, targetCellId)) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                            enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                            String.format(Locale.ROOT, "Default pushback value added to target cell %s",
                                    targetCellId.getFdn())));
                }
                profileChanges.put(targetCellId, helper.updateDistributionOfProfile(profileTarget,
                        targetCellId, helper.getSourceCellId(), configs.getTargetPushBack()));
            }
        }
        return profileChanges;
    }

    /**
     * Adjusts the distribution values at source cell when the missing pushback values were replaced at target cells.
     * 
     * @param replacedTargets
     *            is a map with profile changes at target cells where the missing pushback values were replaced.
     * @return a map with profile changes
     * @throws LbdarUnexpectedException
     *             when no valid usersToMove data can be found.
     * @throws LbdarException
     *             if a cell's carrier cannot be uniquely identified
     */
    public Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> adjustDistributionAtSourceCell(
            final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> replacedTargets) throws LbdarUnexpectedException, LbdarException {

        final TopologyObjectId sourceCellId = helper.getSourceCellId();
        final EnrichedIdleModePrioAtRelease sourceProfile = enrichedPolicyOutputEvent.getProfileFromCellId(sourceCellId);
        Optional<EnrichedIdleModePrioAtRelease> changedSourceProfile = Optional.empty();
        final float targetToSource = configs.getTargetPushBack();

        for (final TopologyObjectId targetId : replacedTargets.keySet()) {

            final long numberOfUsersInSource = Math.round(enrichedPolicyOutputEvent.getCellKpis().get(sourceCellId).getConnectedUsers());
            final long numberOfUsersInTarget = Math.round(enrichedPolicyOutputEvent.getCellKpis().get(targetId).getConnectedUsers());

            final long usersToMoveInSource = enrichedPolicyOutputEvent.getUsersToMoveFromCellId(sourceCellId);
            final long usersToMoveInTarget = enrichedPolicyOutputEvent.getUsersToMoveFromCellId(targetId);

            final long totalUsersInSource = numberOfUsersInSource - usersToMoveInSource;
            final long totalUsersInTarget = numberOfUsersInTarget + usersToMoveInTarget;

            final float oldSourceToTarget = helper.getSelectedDistributionInfo(changedSourceProfile.orElse(sourceProfile), sourceCellId)
                    .getDistributionOfFrequency(enrichedPolicyOutputEvent.getCellCmData().get(targetId).getCarrier());
            final float incrementalOne = calculateBalancedDistribution(numberOfUsersInTarget, targetToSource,
                    1, numberOfUsersInSource);
            final float incrementalTwo = calculateBalancedDistribution(totalUsersInTarget, targetToSource,
                    1, totalUsersInSource);

            final float changedSourceToTarget = oldSourceToTarget + incrementalOne + incrementalTwo;

            if (oldSourceToTarget < changedSourceToTarget) {
                changedSourceProfile = Optional.of(helper.updateDistributionOfProfile(changedSourceProfile
                        .orElse(sourceProfile), sourceCellId, targetId, changedSourceToTarget));
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                            enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                            String.format(Locale.ROOT, "Changed profile of source cell %s to target cell %s to %.3f", sourceCellId, targetId,
                                    changedSourceToTarget)));
                }
            }
        }
        changedSourceProfile.ifPresent(p -> replacedTargets.put(sourceCellId, p));
        return replacedTargets;
    }

    private boolean hasPushBackToSource(final EnrichedIdleModePrioAtRelease profile, final TopologyObjectId targetCellId) {
        final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo distributionInfo = helper.getSelectedDistributionInfo(profile, targetCellId);

        final int indexOfSource = distributionInfo.getIndexOfCarrier(helper.getSourceCellCarrier());

        return !(indexOfSource == -1 ||
                distributionValueIsZero(distributionInfo, indexOfSource));
    }

    private static boolean distributionValueIsZero(final DistributionInfo distributionInfo, final int indexOfSource) {
        return Float.compare(distributionInfo.getFreqDistributionList().get(indexOfSource), 0f) == 0;
    }

}

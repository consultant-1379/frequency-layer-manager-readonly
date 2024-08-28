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
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.FormulaSolver.calculateCValue;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.ProfileChangeCalculationHelper.getTopologyFromTarget;
import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.formatMessage;
import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.logFilteredSector;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileChangesCalculatorImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;

/**
 * This class is used by {@link ProfileChangesCalculatorImpl} to solve the equilibrium between source and target cells.
 */
public class Balancer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Balancer.class);
    private static final int DEFAULT_C_VALUE = 1;

    private final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private final ProfileChangeCalculatorSettings configs;
    private final ProfileChangeCalculationHelper helper;

    public Balancer(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent,
            final ProfileChangeCalculatorSettings configs,
            final ProfileChangeCalculationHelper helper) {
        this.enrichedPolicyOutputEvent = enrichedPolicyOutputEvent;
        this.configs = configs;
        this.helper = helper;
    }

    /**
     * Solve the equilibrium calculation between the source and target cells.
     * 
     * @param profileChanges
     *            a map of profile changes which should be used in the calculation.
     * @param minimumSourceRetained
     *            the minimum source retained setting required for normalization.
     * @return a map of profile changes.
     * @throws LbdarException
     *             when distribution values would decrease or self-retention threshold would decreased
     * @throws LbdarUnexpectedException
     *             an unexpected scenario occurred which prevented the equilibrium calculation
     */
    public Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> solveEquilibrium(
            final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileChanges, final Integer minimumSourceRetained)
            throws LbdarException, LbdarUnexpectedException {

        final TopologyObjectId sourceCellId = helper.getSourceCellId();
        EnrichedIdleModePrioAtRelease sourceProfile = Optional.ofNullable(profileChanges.get(sourceCellId))
                .orElse(enrichedPolicyOutputEvent.getProfileFromCellId(sourceCellId));

        logDistributionInfo(sourceProfile, sourceCellId, "Original source distributionInfo : %s");

        final List<TargetCell> targetCellsWithUnchangedPushbackValue = filterTargetCells(profileChanges);
        final Map<TopologyObjectId, Float> stepSizes = new HashMap<>();
        final Map<TopologyObjectId, Float> distributions = new HashMap<>();

        for (final TargetCell targetCell : targetCellsWithUnchangedPushbackValue) {
            final TopologyObjectId targetId = getTopologyFromTarget(targetCell);
            final EnrichedIdleModePrioAtRelease targetProfile = Optional.ofNullable(profileChanges.get(targetId))
                    .orElse(enrichedPolicyOutputEvent.getProfileFromCellId(targetId));

            final Float targetToSource = helper.getSelectedDistributionInfo(targetProfile, targetId)
                    .getDistributionOfFrequency(helper.getSourceCellCarrier());
            final Float oldSourceToTarget = helper.getSelectedDistributionInfo(sourceProfile, sourceCellId)
                    .getDistributionOfFrequency(enrichedPolicyOutputEvent.getCellCmData().get(targetId).getCarrier());
            if (targetToSource == null || oldSourceToTarget == null) {
                final String message = String.format(Locale.ROOT, "Failed to find distribution value for source or target carrier source=%d," +
                        " target=%d",
                        helper.getSourceCellCarrier(), enrichedPolicyOutputEvent.getCellCmData().get(targetId).getCarrier());
                logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                        message);
                throw new LbdarException(message);
            }
            final float couplingFactor = getCouplingFactor(sourceCellId, sourceProfile, targetId, targetProfile, oldSourceToTarget);
            final long numberOfUsersInSource = Math.round(enrichedPolicyOutputEvent.getCellKpis().get(sourceCellId).getConnectedUsers()) -
                    enrichedPolicyOutputEvent.getUsersToMoveFromCellId(sourceCellId);
            final long numberOfUsersInTarget = Math.round(enrichedPolicyOutputEvent.getCellKpis().get(targetId).getConnectedUsers()) +
                    Long.parseLong(targetCell.getTargetUsersMove());
            final float newSourceToTarget = calculateBalancedDistribution(numberOfUsersInTarget, targetToSource,
                    couplingFactor, numberOfUsersInSource) + diversionDueToCValueOverride(oldSourceToTarget);

            validateDistributionValueChange(oldSourceToTarget, newSourceToTarget);

            if (oldSourceToTarget < newSourceToTarget) {
                sourceProfile = helper.updateDistributionOfProfile(sourceProfile, sourceCellId, targetId, newSourceToTarget);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                            enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                            String.format(Locale.ROOT, "Changed profile of source cell %s to target cell %s to %.3f", sourceCellId, targetId,
                                    newSourceToTarget)));
                }
                profileChanges.put(sourceCellId, sourceProfile);
            }
            distributions.put(targetId, oldSourceToTarget);
            stepSizes.put(targetId, newSourceToTarget - oldSourceToTarget);
        }

        normalizeSelfRetainIfNeeded(profileChanges, sourceCellId, stepSizes, distributions, sourceProfile, minimumSourceRetained);

        return profileChanges;
    }

    private float getCouplingFactor(final TopologyObjectId sourceCellId, final EnrichedIdleModePrioAtRelease sourceProfile,
            final TopologyObjectId targetId, final EnrichedIdleModePrioAtRelease targetProfile,
            final Float oldSourceToTarget) {
        float couplingFactor = 1;
        if (oldSourceToTarget == 0) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                        String.format("Coupling Factor calculate between source cell %s and target cell %s defaulted " +
                                "to: %s as source to target is 0 percent", sourceCellId.getFdn(), targetId.getFdn(), couplingFactor)));
            }
        } else {
            couplingFactor = calculateC(targetId, sourceProfile, targetProfile);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                        String.format("Coupling Factor calculate between source cell %s and target cell %s the result is: %s",
                                sourceCellId.getFdn(), targetId.getFdn(), couplingFactor)));
            }
        }
        return couplingFactor;
    }

    private void normalizeSelfRetainIfNeeded(final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileChanges,
            final TopologyObjectId sourceCellId, final Map<TopologyObjectId, Float> stepSizes, final Map<TopologyObjectId, Float> distributions,
            final EnrichedIdleModePrioAtRelease sourceProfile, final int minimumSourceRetained) throws LbdarException {
        final float currentSelfRetain = helper.getSelectedDistributionInfo(sourceProfile, sourceCellId)
                .getDistributionOfFrequency(helper.getSourceCellCarrier());
        if (selfRetainBreached(currentSelfRetain, minimumSourceRetained)) {

            final String executionId = enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId();
            final Long sectorId = enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId();

            logDistributionInfo(sourceProfile, sourceCellId, "Proposed source distributionInfo before normalization : %s");

            final Map<TopologyObjectId, Integer> stepSizesConverted = stepSizes.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, value -> value.getValue().intValue()));

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(formatMessage(executionId, sectorId, String.format("Step sizes : %s", stepSizesConverted.toString())));
            }

            final Map<TopologyObjectId, Float> normalizedTargetDistributions = DistributionSelfRetainNormalizer.normalize(distributions,
                    currentSelfRetain, stepSizes, minimumSourceRetained);

            if (normalizedTargetDistributions == null && LOGGER.isInfoEnabled()) {

                LOGGER.info(formatMessage(executionId, sectorId,
                        "Retained level of users at source is lower than threshold after normalization."));
            }
            if (normalizedTargetDistributions == null) {
                final String errorMessage = String.format("Retained level of users at Source cell is too low, %s threshold is breached.",
                        minimumSourceRetained);
                logFilteredSector(executionId, String.valueOf(sectorId), errorMessage);

                throw new LbdarException(errorMessage);
            }

            EnrichedIdleModePrioAtRelease resultProfile = sourceProfile;
            for (final Map.Entry<TopologyObjectId, Float> entry : normalizedTargetDistributions.entrySet()) {
                resultProfile = helper.updateDistributionOfProfile(resultProfile, sourceCellId, entry.getKey(), entry.getValue());
            }

            final EnrichedIdleModePrioAtRelease finalResultProfile = resultProfile;
            logDistributionInfo(finalResultProfile, sourceCellId, "Proposed source distributionInfo after normalization : %s");

            final Optional<Map.Entry<TopologyObjectId, EnrichedIdleModePrioAtRelease>> originalSourceProfile = enrichedPolicyOutputEvent.getProfiles()
                    .entrySet().stream()
                    .filter(profile -> profile.getValue().getTopologyObjectId().getFdn().equals(finalResultProfile.getTopologyObjectId().getFdn()))
                    .findFirst();

            if (originalSourceProfile.isPresent()) {
                final EnrichedIdleModePrioAtRelease originalEnrichedIdleModePrioAtRelease = originalSourceProfile.get().getValue();

                final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo enrichedDistributionInfoOriginal = helper
                        .getSelectedDistributionInfo(originalEnrichedIdleModePrioAtRelease, helper.getSourceCellId());
                final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo enrichedDistributionInfoAfterNormalization = helper
                        .getSelectedDistributionInfo(finalResultProfile, helper.getSourceCellId());

                if (enrichedDistributionInfoOriginal.getFreqDistributionList()
                        .equals(enrichedDistributionInfoAfterNormalization.getFreqDistributionList())) {
                    final String errorMessage = "Change excluded as proposed LBDAR profile change is the " +
                            "same as the current profile.";

                    logFilteredSector(executionId, String.valueOf(sectorId), errorMessage);

                    throw new LbdarException(errorMessage);
                } else {
                    profileChanges.put(sourceCellId, finalResultProfile);
                }
            }
        }
    }

    private float diversionDueToCValueOverride(final float oldSourceToTarget) {
        return configs.cValueIsOverrode() ? oldSourceToTarget : 0;
    }

    private List<TargetCell> filterTargetCells(final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileChanges) {
        return enrichedPolicyOutputEvent.getTargetCells().stream()
                .filter(t -> !profileChanges.containsKey(TopologyObjectId.of(t.getTargetCellFdn(), t.getTargetCellOssId())))
                .collect(Collectors.toList());
    }

    private float calculateC(final TopologyObjectId targetId,
            final EnrichedIdleModePrioAtRelease sourceProfile,
            final EnrichedIdleModePrioAtRelease targetProfile) {
        if (configs.cValueIsOverrode()) {
            return DEFAULT_C_VALUE;
        }
        final TopologyObjectId sourceId = helper.getSourceCellId();
        final float sourceUsers = Math.round(enrichedPolicyOutputEvent.getCellKpis().get(sourceId).getConnectedUsers());
        final float targetUsers = Math.round(enrichedPolicyOutputEvent.getCellKpis().get(targetId).getConnectedUsers());
        final float sourceToTarget = getDistribution(sourceId, targetId, sourceProfile);
        final float targetToSource = getDistribution(targetId, sourceId, targetProfile);
        return calculateCValue(sourceUsers, sourceToTarget, targetUsers, targetToSource);
    }

    private Float getDistribution(final TopologyObjectId cellId1, final TopologyObjectId cellId2,
            final EnrichedIdleModePrioAtRelease profile) {
        final EnrichedDistributionInfo sourceDistribution = helper.getSelectedDistributionInfo(profile, cellId1);
        return sourceDistribution.getDistributionOfFrequency(enrichedPolicyOutputEvent.getCellCmData().get(cellId2).getCarrier());
    }

    private void validateDistributionValueChange(final float oldSourceToTarget, final float newSourceToTarget) throws LbdarException {
        if (newSourceToTarget < oldSourceToTarget) {
            final String message = String.format(Locale.ROOT, "Distribution level shouldn't be decreased! " +
                    "Current value '%.4f' and proposed value '%.4f'", oldSourceToTarget, newSourceToTarget);
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    message);
            throw new LbdarException(message);
        }
    }

    private boolean selfRetainBreached(final float currentSelfRetain, final float minSourceRetain) {
        return currentSelfRetain < minSourceRetain;
    }

    private void logDistributionInfo(final EnrichedIdleModePrioAtRelease profile,
            final TopologyObjectId cellId, final String logMessage) {
        final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo enrichedDistributionInfo = helper.getSelectedDistributionInfo(profile, cellId);
        final List<Integer> frequencyDistributionList = enrichedDistributionInfo.getFreqDistributionList().stream()
                .map(Float::intValue).collect(Collectors.toList());

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                    String.format(logMessage, frequencyDistributionList.toString())));
        }
    }
}

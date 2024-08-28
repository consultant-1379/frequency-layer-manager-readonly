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

import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MAXIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MINIMUM_LBDAR_STEPSIZE;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileChangesCalculatorImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * This class is used by {@link ProfileChangesCalculatorImpl} to saturate the LbdarStepsizeThresholds breaches.
 */
public class DistributionChangeSaturator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionChangeSaturator.class);

    private final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private final ProfileChangeCalculatorSettings configs;
    private final ProfileChangeCalculationHelper helper;

    public DistributionChangeSaturator(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent,
            final ProfileChangeCalculatorSettings configs,
            final ProfileChangeCalculationHelper helper) {
        this.enrichedPolicyOutputEvent = enrichedPolicyOutputEvent;
        this.configs = configs;
        this.helper = helper;
    }

    /**
     * It checks the given profile and saturates the distribution value when the min/max LbdarStepSize limits are breached.
     * 
     * @param cellToProfile
     *            is an TopologyObjectId-Profile pair where stepSize change in the distribution have to be validated.
     * @return an Optional of profile
     * @throws LbdarUnexpectedException
     *             when there is no valid MaxLbdarStepSize in configuration
     * @throws LbdarException
     *             if a cell's carrier cannot be uniquely identified
     */
    public Optional<EnrichedIdleModePrioAtRelease> saturateStepSize(final Map.Entry<TopologyObjectId, EnrichedIdleModePrioAtRelease> cellToProfile)
            throws LbdarUnexpectedException, LbdarException {
        final TopologyObjectId sourceCellId = cellToProfile.getKey();

        final EnrichedDistributionInfo newDistrInfo = helper.getSelectedDistributionInfo(cellToProfile.getValue(), sourceCellId);
        final EnrichedDistributionInfo oldDistrInfo = helper
                .getSelectedDistributionInfo(enrichedPolicyOutputEvent.getProfileFromCellId(sourceCellId), sourceCellId);

        final int indexOfOwnFrequency = newDistrInfo.getCarriers()
                .indexOf(enrichedPolicyOutputEvent.getCell(sourceCellId).getCarrier());
        Optional<EnrichedIdleModePrioAtRelease> profileToSaturate = Optional.empty();
        for (int i = 0; i < newDistrInfo.getCarriers().size(); i++) {
            if (indexOfOwnFrequency != i) {
                final float oldDistr = i < oldDistrInfo.getFreqDistributionList().size() ? oldDistrInfo.getFreqDistributionList().get(i) : 0;
                final float newDistr = newDistrInfo.getFreqDistributionList().get(i);
                final float diffDistr = newDistr - oldDistr;
                final TopologyObjectId targetCellId = enrichedPolicyOutputEvent.getCellByCarrier(newDistrInfo.getCarriers().get(i));
                final Float stepSize = validateAndGetStepSize(sourceCellId, targetCellId, diffDistr);
                if (stepSize != null) {
                    final float newDistribution = getNewDistribution(stepSize, oldDistr, diffDistr);
                    profileToSaturate = Optional.of(helper.updateDistributionOfProfile(
                            profileToSaturate.orElse(cellToProfile.getValue()), sourceCellId, targetCellId, newDistribution));
                }
            }
        }
        return profileToSaturate;
    }

    private Float validateAndGetStepSize(final TopologyObjectId sourceCellId, final TopologyObjectId targetCellId, final float diffDistr)
            throws LbdarUnexpectedException, LbdarException {
        final Cell targetCell = enrichedPolicyOutputEvent.getCell(targetCellId);
        validateTargetCell(targetCell, enrichedPolicyOutputEvent);
        final float maxLbdarStepSize = configs.getMaxLbdarStepSize(targetCell.getBandwidth())
                .orElseThrow(() -> {
                    final String errorMessage = String.format("No valid %s configuration for '%s' cell",
                            THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, sourceCellId.getFdn());
                    return new LbdarUnexpectedException(errorMessage);
                });

        final float minLbdarStepSize = Math.min(configs.getMinLbdarStepSize(), maxLbdarStepSize);

        if (maxLbdarStepSize < configs.getMinLbdarStepSize() && LOGGER.isInfoEnabled()) {
            LOGGER.warn(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                    String.format("minLbdarStepsize is greater than maxLbdarStepsize, the maxLbdarStepsize is used " +
                            "for step size saturation at cell %s", sourceCellId)));
        }
        Float stepSize = null;
        if (minLbdarStepSizeThresholdIsBreached(minLbdarStepSize, diffDistr)) {
            stepSize = minLbdarStepSize;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                        String.format("Lbdar StepSize is saturated as %s is breached at cell %s",
                                THRESHOLD_MINIMUM_LBDAR_STEPSIZE, sourceCellId)));
            }
        } else if (maxLbdarStepSizeThresholdIsBreached(maxLbdarStepSize, diffDistr)) {
            stepSize = maxLbdarStepSize;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                        String.format("Lbdar StepSize is saturated as %s is breached at cell %s",
                                THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, sourceCellId)));
            }
        }
        return stepSize;
    }

    private void validateTargetCell(final Cell cell, final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) throws LbdarException {
        if (cell == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(), "Frequency distribution existing in the profile but no "
                                + "cell with the corresponding carrier frequency exists in the sector"));
            }
            throw new LbdarException("Frequency distribution existing in the profile but no "
                    + "cell with the corresponding carrier frequency exists in the sector");
      }
    }

    private int getNewDistribution(final float distrThreshold, final float oldDistr, final float diffDistr) {
        return diffDistr > 0 ? Math.round(oldDistr + distrThreshold) : Math.round(oldDistr - distrThreshold);
    }

    private boolean maxLbdarStepSizeThresholdIsBreached(final float maxLbdarStepSize, final float diffDistr) {
        final float absDiff = Math.abs(diffDistr);
        return absDiff > 0f && absDiff > maxLbdarStepSize;
    }

    private boolean minLbdarStepSizeThresholdIsBreached(final float minLbdarStepSize, final float diffDistr) {
        return diffDistr > 0f && diffDistr < minLbdarStepSize;
    }
}

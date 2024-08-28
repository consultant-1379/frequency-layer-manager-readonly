/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
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

import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;

/**
 * This class contains method for normalization of distribution values in order that self retain is above min self retain config value.
 */
public class DistributionSelfRetainNormalizer {
    private DistributionSelfRetainNormalizer() {

    }

    /**
     * This method changes the distribution values so that the source retain value is above min source retain.
     * 
     * @param distributions
     *            The original distribution values for target cells
     * @param currentSelfRetain
     *            The new self retain value that is below min self retain
     * @param stepSizes
     *            The calculated step sizes for each target cell
     * @param minSelfRetain
     *            The min self retain value coming from configuration
     * @return A map from target cell id to distribution values where these are changed so that self retain is not less that min source retain
     *         or null if maxDistributionToShare is less than zero
     */
    public static Map<TopologyObjectId, Float> normalize(final Map<TopologyObjectId, Float> distributions, final float currentSelfRetain,
            final Map<TopologyObjectId, Float> stepSizes, final int minSelfRetain) {
        final float sumOfStepSizes = stepSizes.values().stream()
                .reduce(0f, Float::sum);
        final float oldSelfRetain = currentSelfRetain + sumOfStepSizes;
        final float maxDistributionToShare = oldSelfRetain - minSelfRetain;
        if (maxDistributionToShare < 0) {
            return null;
        }

        final float wantedDistributionToShare = Math.round(oldSelfRetain / 2f);
        final float distributionToShare = Math.min(wantedDistributionToShare, maxDistributionToShare);
        return stepSizes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> distributions.get(entry.getKey()) + Math.round(distributionToShare * entry.getValue() / sumOfStepSizes)));
    }
}

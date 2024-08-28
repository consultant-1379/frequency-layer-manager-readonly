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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import java.util.List;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.DistributionInfo;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;

/**
 * This class needs to validate the given IdleModePrioAtRelease.
 */
public class ProfileValidator {

    private ProfileValidator() {
    }

    /**
     * This method should return true if the given IdleModePrioAtRelease has valid threshold and distribution values.
     * @param profile an object of {@link IdleModePrioAtRelease}
     * @return true if the input is valid, false otherwise
     */
    static boolean isValid(final IdleModePrioAtRelease profile) {
        return areDistributionInfosValid(profile) && isThresholdValid(profile) && areSumOfDistributionValuesValid(profile);
    }

    /**
     * Returns true if distribution info is valid.
     * @param profile an object of {@link IdleModePrioAtRelease}
     * @return true if distribution info is valid
     */
    static <T extends DistributionInfo> boolean areDistributionInfosValid(final GenericIdleModePrioAtRelease<T> profile) {
        return profile.getDistributionInfos()
                .stream()
                .noneMatch(di -> di.getEUtranFreqRefList() == null || di.getEUtranFreqRefList().isEmpty());
    }

    /**
     * Returns true if threshold is valid.
     * @param profile an object of {@link IdleModePrioAtRelease}
     * @return true if threshold is valid.
     */
    static <T extends DistributionInfo> boolean isThresholdValid(final GenericIdleModePrioAtRelease<T> profile) {
        final List<Integer> thresholds = profile.getThresholds();
        if (thresholds.size() != IdleModePrioAtRelease.ThresholdLevel.values().length) {
            return false;
        }

        if (thresholds.stream().anyMatch(th -> th == null || th < 0)) {
            return false;
        }

        for (int i = 1; i < thresholds.size(); i++) {
            if (thresholds.get(i) < thresholds.get(i - 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if sum of distribution values are equal or less than 100.
     * @param profile an object of {@link IdleModePrioAtRelease}
     * @return true if sum of distribution values are equal or less than 100.
     */
    static <T extends DistributionInfo> boolean areSumOfDistributionValuesValid(final GenericIdleModePrioAtRelease<T> profile) {
        for (final IdleModePrioAtRelease.DistributionInfo distributionInfo : profile.getDistributionInfos()) {
            final float sumOfDistributionValue = distributionInfo.getFreqDistributionList().stream()
                    .map(ProfileValidator::replaceNullWithZero)
                    .reduce(0f, Float::sum);
            if (sumOfDistributionValue > 100f) {
                return false;
            }

        }
        return true;
    }

    private static Float replaceNullWithZero(final Float distribution) {
        return distribution == null ? Float.valueOf(0f) : distribution;
    }

}

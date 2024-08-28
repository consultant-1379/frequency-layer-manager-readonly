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

import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.PROFILE_ONE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.PROFILE_THREE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.PROFILE_TWO;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;

public class EnrichedPolicyOutputEventPrototypeProfile extends EnrichedPolicyOutputEventPrototype {

    public EnrichedPolicyOutputEventPrototypeProfile(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        super(enrichedPolicyOutputEvent);
    }

    public EnrichedPolicyOutputEventPrototypeProfile frequenciesAtSource(final ThresholdLevel thresholdLevel,
                                                                         final List<String> frequencies,
                                                                         final Map<String, Integer> frequencyToCarrier) {
        final EnrichedIdleModePrioAtRelease profileSource = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE);
        final List<EnrichedDistributionInfo> distributionInfos = profileSource.getDistributionInfos();
        final EnrichedDistributionInfo newDistributionInfo =
                new EnrichedDistributionInfo(thresholdLevel,
                        distributionInfos.get(thresholdLevel.ordinal()).getFreqDistributionList(),
                        frequencies,
                        frequencyToCarrier);
        distributionInfos.set(thresholdLevel.ordinal(), newDistributionInfo);
        enrichedPolicyOutputEvent.getProfiles().put(PROFILE_ONE,
                new EnrichedIdleModePrioAtRelease(profileSource.getId(), profileSource.getFdn(), profileSource.getOssId(),
                        profileSource.getName(), profileSource.getThresholds(), distributionInfos,
                        profileSource.getReservedBy()));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeProfile frequenciesAtTargetOne(final ThresholdLevel thresholdLevel,
                                                                            final List<String> frequencies,
                                                                            final Map<String, Integer> frequencyToCarrier) {
        final EnrichedIdleModePrioAtRelease profileSource = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_TWO);
        final List<EnrichedDistributionInfo> distributionInfos = profileSource.getDistributionInfos();
        final EnrichedDistributionInfo newDistributionInfo =
                new EnrichedDistributionInfo(thresholdLevel,
                        distributionInfos.get(thresholdLevel.ordinal()).getFreqDistributionList(),
                        frequencies, frequencyToCarrier);
        distributionInfos.set(thresholdLevel.ordinal(), newDistributionInfo);
        enrichedPolicyOutputEvent.getProfiles().put(PROFILE_TWO,
                new EnrichedIdleModePrioAtRelease(profileSource.getId(), profileSource.getFdn(), profileSource.getOssId(),
                        profileSource.getName(), profileSource.getThresholds(), distributionInfos,
                        profileSource.getReservedBy()));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeProfile distributionsAtSource(final ThresholdLevel thresholdLevel,
                                                                           final List<Float> distributions,
                                                                           final Map<String, Integer> frequencyToCarrier) {
        final EnrichedIdleModePrioAtRelease profileSource = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE);
        final List<EnrichedDistributionInfo> distributionInfos = profileSource.getDistributionInfos();
        final EnrichedDistributionInfo newDistributionInfo =
                new EnrichedDistributionInfo(thresholdLevel,
                        distributions,
                        distributionInfos.get(thresholdLevel.ordinal()).getEUtranFreqRefList(), frequencyToCarrier);
        distributionInfos.set(thresholdLevel.ordinal(), newDistributionInfo);
        enrichedPolicyOutputEvent.getProfiles().put(PROFILE_ONE,
                new EnrichedIdleModePrioAtRelease(profileSource.getId(), profileSource.getFdn(), profileSource.getOssId(),
                        profileSource.getName(), profileSource.getThresholds(), distributionInfos,
                        profileSource.getReservedBy()));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeProfile distributionsAtTargetOne(final ThresholdLevel thresholdLevel,
                                                                              final List<Float> distributions,
                                                                              final Map<String, Integer> frequencyToCarrier) {
        final EnrichedIdleModePrioAtRelease profileSource = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_TWO);
        final List<EnrichedDistributionInfo> distributionInfos = profileSource.getDistributionInfos();
        final EnrichedDistributionInfo newDistributionInfo =
                new EnrichedDistributionInfo(thresholdLevel,
                        distributions,
                        distributionInfos.get(thresholdLevel.ordinal()).getEUtranFreqRefList(),
                        frequencyToCarrier);
        distributionInfos.set(thresholdLevel.ordinal(), newDistributionInfo);
        enrichedPolicyOutputEvent.getProfiles().put(PROFILE_TWO,
                new EnrichedIdleModePrioAtRelease(profileSource.getId(), profileSource.getFdn(), profileSource.getOssId(),
                        profileSource.getName(), profileSource.getThresholds(), distributionInfos,
                        profileSource.getReservedBy()));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeProfile distributionsAtTargetTwo(final ThresholdLevel thresholdLevel,
                                                                              final List<Float> distributions,
                                                                              final Map<String, Integer> frequencyToCarrier) {
        final EnrichedIdleModePrioAtRelease profileSource = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_THREE);
        final List<EnrichedDistributionInfo> distributionInfos = profileSource.getDistributionInfos();
        final EnrichedDistributionInfo newDistributionInfo =
                new EnrichedDistributionInfo(thresholdLevel,
                        distributions,
                        distributionInfos.get(thresholdLevel.ordinal()).getEUtranFreqRefList(), frequencyToCarrier);
        distributionInfos.set(thresholdLevel.ordinal(), newDistributionInfo);
        enrichedPolicyOutputEvent.getProfiles().put(PROFILE_THREE,
                new EnrichedIdleModePrioAtRelease(profileSource.getId(), profileSource.getFdn(), profileSource.getOssId(),
                        profileSource.getName(), profileSource.getThresholds(), distributionInfos,
                        profileSource.getReservedBy()));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeProfile thresholdLimitsAtSource(final List<Integer> thresholds) {
        final EnrichedIdleModePrioAtRelease profileSource = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE);
        enrichedPolicyOutputEvent.getProfiles().put(PROFILE_ONE,
                new EnrichedIdleModePrioAtRelease(profileSource.getId(), profileSource.getFdn(), profileSource.getOssId(),
                        profileSource.getName(), thresholds, profileSource.getDistributionInfos(),
                        profileSource.getReservedBy()));
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeProfile thresholdLimitsAtTargetOne(final List<Integer> thresholds) {
        final EnrichedIdleModePrioAtRelease profileSource = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_TWO);
        enrichedPolicyOutputEvent.getProfiles().put(PROFILE_TWO,
                new EnrichedIdleModePrioAtRelease(profileSource.getId(), profileSource.getFdn(), profileSource.getOssId(),
                        profileSource.getName(), thresholds, profileSource.getDistributionInfos(),
                        profileSource.getReservedBy()));
        return this;
    }

}
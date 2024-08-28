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

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.DistributionInfo;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;

public class TestDataBuilder {

    private TestDataBuilder() {
    }

    public static IdleModePrioAtRelease getTestIdleModePrioAtRelease(final int id, final String fdn, final Integer ossId,
                                                                     final List<String> frequencyFdns,
                                                                     final Set<String> reservedBy) {
        return new IdleModePrioAtRelease(id, fdn, ossId, "P" + id, getTestThresholds(),
                getTestDistributionInfo(frequencyFdns), reservedBy);
    }

    private static List<Integer> getTestThresholds() {
        return Arrays.asList(0, 200, 400, 600, 800);
    }

    private static List<DistributionInfo> getTestDistributionInfo(final List<String> frequencyFdns) {
        final List<DistributionInfo> result = new ArrayList<>(5);
        result.add(new DistributionInfo(ThresholdLevel.LOW_LOAD_THRESHOLD, Arrays.asList(80.0f, 20.0f), frequencyFdns));
        result.add(new DistributionInfo(ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD, Arrays.asList(80.0f, 20.0f), frequencyFdns));
        result.add(new DistributionInfo(ThresholdLevel.MEDIUM_LOAD_THRESHOLD, Arrays.asList(80.0f, 20.0f), frequencyFdns));
        result.add(new DistributionInfo(ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD, Arrays.asList(80.0f, 20.0f), frequencyFdns));
        result.add(new DistributionInfo(ThresholdLevel.HIGH_LOAD_THRESHOLD, Arrays.asList(80.0f, 20.0f), frequencyFdns));
        return result;
    }
}

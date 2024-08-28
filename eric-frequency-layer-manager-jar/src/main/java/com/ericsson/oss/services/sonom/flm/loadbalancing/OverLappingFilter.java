/*
 * -----------------------------------------------------------------------------
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

package com.ericsson.oss.services.sonom.flm.loadbalancing;

import static com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo.OverlappingFlag.NOT_OVERLAPPING;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo.OverlappingFlag.OVERLAP_DROP_NEEDED;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.OverLappingLogger.logOverlappingSectors;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Class for filter the overlapping sectors based on the OverlapInfo.
 */
public class OverLappingFilter {

    private boolean allPolicyOutputEventsWereDropped;

    /**
     * Filter the overlapping sectors based on the OverlapInfo object.
     * @param policyOutputEventsWithOverlapInfo is a List of Pairs of {@link PolicyOutputEvent} and {@link OverlapInfo}.
     * @return a List of {@link PolicyOutputEvent}.
     */
    public List<PolicyOutputEvent> filterAndLogOverlapped(final List<Pair<PolicyOutputEvent, OverlapInfo>> policyOutputEventsWithOverlapInfo) {
        final List<PolicyOutputEvent> filteredPolicyOutputEvents = policyOutputEventsWithOverlapInfo.stream()
                .filter(this::logAndFilter)
                .map(Pair::getLeft)
                .collect(Collectors.toList());
        if (!policyOutputEventsWithOverlapInfo.isEmpty() && filteredPolicyOutputEvents.isEmpty()) {
            allPolicyOutputEventsWereDropped = true;
        }
        return filteredPolicyOutputEvents;
    }

    private boolean logAndFilter(final Pair<PolicyOutputEvent, OverlapInfo> pair) {
        if (!NOT_OVERLAPPING.equals(pair.getRight().getOverlappingFlag())) {
            logOverlappingSectors(pair);
        }
        return !OVERLAP_DROP_NEEDED.equals(pair.getRight().getOverlappingFlag());
    }

    public boolean allPolicyOutputEventsWereDropped() {
        return allPolicyOutputEventsWereDropped;
    }

}

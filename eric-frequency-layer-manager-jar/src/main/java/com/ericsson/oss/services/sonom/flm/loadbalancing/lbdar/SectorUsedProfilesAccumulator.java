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

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;

class SectorUsedProfilesAccumulator {
    private static final String ERBS = "ERBS";
    private static final String RADIO_NODE = "RadioNode";
    private static final int DEFAULT_TOTAL_AVAILABLE_PROFILES = 8;
    private final Map<TopologyObjectId, Integer> profilesUsedByNodes = new HashMap<>();

    SectorUsedProfilesAccumulator(final Map<TopologyObjectId, Integer> profilesUsedAccordingToCmService,
                                  final Map<TopologyObjectId, Integer> profilesUsedByExecution) {
        profilesUsedByNodes.putAll(profilesUsedAccordingToCmService);
        profilesUsedByExecution.forEach((nodeId, count) -> profilesUsedByNodes.compute(nodeId, (oldNodeId, oldUsed) -> oldUsed == null ?
                count : oldUsed + count));
    }

    public boolean isAvailable(final Node node, final int count) {
        return profilesUsedByNodes.get(node.getTopologyObjectId()) + count + 1 <=
                getTotalNumberOfProfiles(node.getNodeType());
    }

    public void increase(final TopologyObjectId nodeId, final int count) {
        profilesUsedByNodes.compute(nodeId, (oldNodeId, oldUsed) -> oldUsed == null ? count : oldUsed + count);

    }

    private int getTotalNumberOfProfiles(final String nodeType) {
        if (nodeType.equalsIgnoreCase(ERBS)) {
            return 8;
        }
        if (nodeType.equalsIgnoreCase(RADIO_NODE)) {
            return 24;
        }
        return DEFAULT_TOTAL_AVAILABLE_PROFILES;
    }
}

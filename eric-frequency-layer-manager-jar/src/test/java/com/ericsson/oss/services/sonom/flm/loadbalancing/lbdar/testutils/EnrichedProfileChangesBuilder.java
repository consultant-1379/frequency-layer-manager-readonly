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

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedProfileChanges;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileChanges;

public class EnrichedProfileChangesBuilder {
    private final Map<TopologyObjectId, Node> nodes = new HashMap<>();
    private final Map<TopologyObjectId, Integer> profilesUsed = new HashMap<>();
    private final ProfileChanges profileChanges;

    public EnrichedProfileChangesBuilder(final ProfileChanges profileChanges) {
        this.profileChanges = profileChanges;
    }

    /**
     * Add node tho the structure to build
     * @param cellId the {@link TopologyObjectId} of a cell
     * @param node a {@link Node} object
     * @param usedProfiles number of used profiles on the node
     * @return an {@link EnrichedProfileChangesBuilder} object
     */
    public EnrichedProfileChangesBuilder addNode(final TopologyObjectId cellId, final Node node, final int usedProfiles) {
        nodes.put(cellId, node);
        profilesUsed.put(new TopologyObjectId(node.getFdn(), node.getOssId()), usedProfiles);
        return this;
    }

    public EnrichedProfileChanges build() {
        return new EnrichedProfileChanges(profileChanges, nodes, profilesUsed);
    }
}

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

import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;

/**
 * This class is used to store cm data needed for ChangeElementCalculator to create ChangeElements for given {@link ProfileChanges}.
 * The original ProfileChanges are enriched with cm data.
 */
public class EnrichedProfileChanges {
    Map<TopologyObjectId, Node> nodesByCell;
    Map<TopologyObjectId, Integer> profilesUsedByNodes;

    private final ProfileChanges profileChanges;

    public EnrichedProfileChanges(final ProfileChanges profileChanges, final Map<TopologyObjectId, Node> nodesByCell,
                                  final Map<TopologyObjectId, Integer> profilesUsedByNodes) {
        this.profileChanges = profileChanges;
        this.nodesByCell = nodesByCell;
        this.profilesUsedByNodes = profilesUsedByNodes;
    }

    public ProfileChanges getProfileChanges() {
        return profileChanges;
    }

    public Node getNodeForCell(final TopologyObjectId cellId) {
        return nodesByCell.getOrDefault(cellId, null);
    }

    public Integer getProfilesUsed(final TopologyObjectId nodeId) {
        return profilesUsedByNodes.getOrDefault(nodeId, null);
    }

    public Map<TopologyObjectId, Integer> getProfilesUsedByNodes() {
        return profilesUsedByNodes;
    }
}

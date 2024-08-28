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
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;

/**
 * This class ss used to enrich {@link ProfileChanges} object with cell to node mappings and number of used profiles per nodes.
 */
public class ProfileChangesEnricher {
    private final CmNodeObjectsStore cmNodeObjectStore;

    public ProfileChangesEnricher(final CmNodeObjectsStore cmNodeObjectStore) {
        this.cmNodeObjectStore = cmNodeObjectStore;
    }

    /**
     * This method creates an {@link EnrichedProfileChanges} object from the given {@link ProfileChanges} object by enriching it with cell to node.
     * mapping and used profile number per node
     * @param profileChanges an {@link ProfileChanges} object
     * @return returns an {@link EnrichedProfileChanges} object
     */
    public EnrichedProfileChanges enrich(final ProfileChanges profileChanges) {
        final Map<TopologyObjectId, Node> nodes = profileChanges.getCells().stream()
                .map(cellId -> Pair.of(cellId, cmNodeObjectStore.getNodeForCellFdn(cellId.getFdn(), cellId.getOssId())))
                .filter(pair -> pair.getRight() != null)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        final Map<TopologyObjectId, Integer> profilesUsed = nodes.values().stream()
                .distinct()
                .map(node -> Pair.of(new TopologyObjectId(node.getFdn(), node.getOssId()),
                        cmNodeObjectStore.getNumberOfProfilesUsedByNode(node.getFdn(), node.getOssId())))
                .filter(pair -> pair.getRight() != null)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return new EnrichedProfileChanges(profileChanges, nodes, profilesUsed);
    }
}

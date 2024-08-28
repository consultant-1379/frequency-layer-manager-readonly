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

package com.ericsson.oss.services.sonom.flm.cm.data.stores;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.EUtranFrequency;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.NodeWithCells;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.NodeWithEutranFrequencies;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.NodeWithIdleModePrioAtReleases;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmNodeCellRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmNodeFrequencyRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmNodeProfileRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.util.IdleModePrioAtReleaseUtils;

/**
 * This class retrieves and caches all data that is needed for calculation of LBDAR Profile. It stores profiles, cell to node, number of used profiles
 * per node, frequencies, node to frequencies data
 */
public class CmNodeObjectsStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmNodeObjectsStore.class);
    // map from profile fdn, ossId pair to profile
    private final Map<TopologyObjectId, IdleModePrioAtRelease> profiles = new HashMap<>();
    // map from cellId to Node
    private final Map<TopologyObjectId, Node> cellToNode = new HashMap<>();
    // map from nodeId to number of used profiles on node
    private final Map<TopologyObjectId, Integer> usedProfiles = new HashMap<>();
    //map from nodeId to collection of frequencies
    private final Map<TopologyObjectId, Collection<EUtranFrequency>> nodeToFrequencies = new HashMap<>();
    private final CmNodeProfileRetriever cmNodeProfileRetriever;
    private final CmNodeFrequencyRetriever cmNodeFrequencyRetriever;

    /**
     * A constructor that is used in production code to load data of the store.
     * 
     * @param cmSectorCellStore
     *            A {@link CmSectorCellStore} object used to get {@link Cell} data
     * @param cellIdsOfLBQs
     *            A collection of cell ids that is used to filter data retrieved from retrievers in case of profiles.
     *            It should contain the source and target cell ids from LBQs.
     * @param sectorIds the list of sectors for which cells cm data should be stored. All the cm data is collected for these cells except profiles
     */
    public CmNodeObjectsStore(final CmSectorCellStore cmSectorCellStore, final Collection<TopologyObjectId> cellIdsOfLBQs,
                              final Collection<Long> sectorIds) {
        this(cmSectorCellStore, cellIdsOfLBQs, sectorIds, new CmNodeCellRetriever(), new CmNodeProfileRetriever(), new CmNodeFrequencyRetriever());
    }

    /**
     * A constructor that is used in production code to load data of the store without filling profiles or frequencies.
     * This constructor is used earlier in the flow where we don't need to fill profiles or frequencies.
     *
     * @param cmSectorCellStore
     *            A {@link CmSectorCellStore} object used to get {@link Cell} data
     * @param sectorIds the list of sectors for which cells cm data should be stored. All the cm data is collected for these cells except profiles
     */
    public CmNodeObjectsStore(final CmSectorCellStore cmSectorCellStore,
                              final Collection<Long> sectorIds) {
        this(cmSectorCellStore, sectorIds, new CmNodeCellRetriever(), new CmNodeProfileRetriever(), new CmNodeFrequencyRetriever());
    }

    /**
     * This constructor is used in test only.
     * 
     * @param cmSectorCellStore
     *            A {@link CmSectorCellStore} object used to get {@link Cell} data
     * @param cellIds
     *            A collection of cell ids that is used to filter data retrieved from retrievers
     * @param sectorIds
     *            the list of sectors for which cells cm data should be stored. All the cm data is collected for these cells except profiles
     * @param cmNodeCellRetriever
     *            A {@link CmNodeCellRetriever} that is used to get node to cells objects
     * @param cmNodeProfileRetriever
     *            A {@link CmNodeProfileRetriever} that is used to get node to profiles objects
     * @param cmNodeFrequencyRetriever
     *            A {@link CmNodeFrequencyRetriever} that is used to get node to frequencies objects
     */
    public CmNodeObjectsStore(final CmSectorCellStore cmSectorCellStore, final Collection<TopologyObjectId> cellIds, final Collection<Long> sectorIds,
            final CmNodeCellRetriever cmNodeCellRetriever, final CmNodeProfileRetriever cmNodeProfileRetriever,
            final CmNodeFrequencyRetriever cmNodeFrequencyRetriever) {
        final List<Cell> cellsOfSectors = getCellsOfSectors(cmSectorCellStore, sectorIds);
        final List<Cell> cellsOfLBQs = getCellsOfLBQs(cellsOfSectors, cellIds);
        fillCellToNodeMap(cmNodeCellRetriever, cellsOfSectors);
        fillFrequencies(cmNodeFrequencyRetriever);
        fillProfiles(cmNodeProfileRetriever, cellsOfLBQs);
        this.cmNodeProfileRetriever = cmNodeProfileRetriever;
        this.cmNodeFrequencyRetriever = cmNodeFrequencyRetriever;
    }

    /**
     * A constructor that is used to load data of the store without filling profiles or frequencies.
     * This constructor is used earlier in the flow where we don't need to fill profiles or frequencies.
     * This constructor is used for retrieving FeatureState in PolicyInputEventGenerator.
     * Populating frequencies and profiles are not needed in PolicyInputEventGenerator.
     *
     * @param cmSectorCellStore
     *            A {@link CmSectorCellStore} object used to get {@link Cell} data
     * @param sectorIds
     *            the list of sectors for which cells cm data should be stored. All the cm data is collected for these cells except profiles
     * @param cmNodeCellRetriever
     *            A {@link CmNodeCellRetriever} that is used to get node to cells objects
     * @param cmNodeProfileRetriever
     *            A {@link CmNodeProfileRetriever} that is used to get node to profiles objects
     * @param cmNodeFrequencyRetriever
     *            A {@link CmNodeFrequencyRetriever} that is used to get node to frequencies objects
     */
    public CmNodeObjectsStore(final CmSectorCellStore cmSectorCellStore, final Collection<Long> sectorIds,
                              final CmNodeCellRetriever cmNodeCellRetriever, final CmNodeProfileRetriever cmNodeProfileRetriever,
                              final CmNodeFrequencyRetriever cmNodeFrequencyRetriever) {
        final List<Cell> cellsOfSectors = getCellsOfSectors(cmSectorCellStore, sectorIds);
        fillCellToNodeMap(cmNodeCellRetriever, cellsOfSectors);
        this.cmNodeProfileRetriever = cmNodeProfileRetriever;
        this.cmNodeFrequencyRetriever = cmNodeFrequencyRetriever;
    }

    /**
     * Returns {@link EUtranFrequency} for the given nodeFdn, nodeOssId pair and earfcndl value.
     * @param nodeFdn fdn of a node
     * @param nodeOssId ossId of a node
     * @param earfcndl earfcndl number coming from Cell
     * @return returns {@link EUtranFrequency} object if found, otherwise null
     */
    public EUtranFrequency getEutranFrequencyOnNode(final String nodeFdn, final int nodeOssId, final int earfcndl) {
        final Collection<EUtranFrequency> frequenciesOfNode = nodeToFrequencies.getOrDefault(TopologyObjectId.of(nodeFdn, nodeOssId),
                null);
        if (frequenciesOfNode == null) {
            return null;
        }
        final List<EUtranFrequency> frequencies = frequenciesOfNode.stream()
                .filter(frequency -> frequency.getArfcnValueEUtranDl() == earfcndl)
                .collect(Collectors.toList());
        if (frequencies.isEmpty()) {
            return null;
        }
        if (frequencies.size() > 1) {
            LOGGER.warn("More than one frequency have been found for node {} and earfcndl {}", nodeFdn, earfcndl);
        }
        return frequencies.get(0);
    }

    public Collection<EUtranFrequency> getEutranFrequencies(final String nodeFdn, final int nodeOssId) {
        return nodeToFrequencies.getOrDefault(TopologyObjectId.of(nodeFdn, nodeOssId),
                Collections.emptyList());
    }

    /**
     * This method returns an IdleModePrioAtRelease object for the given fdn and ossId.
     * @param profileFdn an IdleModePrioAtRelease fdn
     * @param profileOssId an IdleModePrioAtRelease ossId
     * @return an {@link IdleModePrioAtRelease} object or null if not found
     */
    public IdleModePrioAtRelease getIdleModePrioAtRelease(final String profileFdn, final int profileOssId) {
        IdleModePrioAtRelease profile = profiles.get(TopologyObjectId.of(profileFdn, profileOssId));
        if (profile == null) {
            profile = cmNodeProfileRetriever.retrieve(profileFdn, profileOssId);
            if (profile != null) {
                normalizeDistributedInfo(profile);
                profiles.put(profile.getTopologyObjectId(), profile);
            }
        }

        return profile;
    }

    /**
     * This method returns a {@link Node} object for the given cellFdn and cellOssId.
     * @param cellFdn the fdn of a cell
     * @param cellOssId an ossId of a cell
     * @return returns a {@link Node} object or null if not found
     */
    public Node getNodeForCellFdn(final String cellFdn, final int cellOssId) {
        return cellToNode.get(TopologyObjectId.of(cellFdn, cellOssId));
    }

    /**
     * Returns the number of idleModePrioAtRelease created for the given Node.
     * @param nodeFdn an fdn of a {@link Node}
     * @param nodeOssId an ossId of a {@link Node}
     * @return returns a number bigger than zero if node has been found, -1 otherwise
     */
    public Integer getNumberOfProfilesUsedByNode(final String nodeFdn, final int nodeOssId) {
        return usedProfiles.get(TopologyObjectId.of(nodeFdn, nodeOssId));
    }

    /**
     * This method updates frequencies and profiles with given Sectors and Cell Ids.
     * @param cmSectorCellStore
     *            A {@link CmSectorCellStore} object used to get {@link Cell} data
     * @param cellIds
     *            A collection of cell ids that is used to filter data retrieved from retrievers
     * @param sectorIds
     *            the list of sectors for which cells cm data should be stored. All the cm data is collected for these cells except profiles
     */
    public void updateFrequenciesAndProfiles(final CmSectorCellStore cmSectorCellStore, final Collection<TopologyObjectId> cellIds,
                                             final Collection<Long> sectorIds) {
        final List<Cell> cellsOfSectors = getCellsOfSectors(cmSectorCellStore, sectorIds);
        final List<Cell> cellsOfLBQs = getCellsOfLBQs(cellsOfSectors, cellIds);
        fillFrequencies(cmNodeFrequencyRetriever);
        fillProfiles(cmNodeProfileRetriever, cellsOfLBQs);
    }

    private List<Cell> getCellsOfSectors(final CmSectorCellStore cmSectorCellStore, final Collection<Long> sectorIds) {
        return cmSectorCellStore.getFullSectors().stream()
                                .filter(topologySector -> sectorIds.contains(topologySector.getSectorId()))
                                .flatMap(topologySector -> topologySector.getAssociatedCells().stream())
                                .collect(Collectors.toList());
    }

    private List<Cell> getCellsOfLBQs(final Collection<Cell> cellsOfSectors, final Collection<TopologyObjectId> cellIds) {
        return cellsOfSectors.stream()
                .filter(cell -> cellIds.contains(cell.getTopologyObjectId()))
                .collect(Collectors.toList());
    }

    private void fillCellToNodeMap(final CmNodeCellRetriever cmNodeCellRetriever, final List<Cell> cellsOfSectors) {
        final Collection<NodeWithCells> nodesWithCells = cmNodeCellRetriever.retrieve();
        if (LOGGER.isInfoEnabled()) {
            final long numberOfCells = nodesWithCells.stream()
                    .mapToLong(nodeWithCells -> nodeWithCells.getAssociatedCells().size())
                    .sum();
            LOGGER.info("Retrieved {} NodeWithCell objects", numberOfCells);
        }
        nodesWithCells.forEach(nodeWithCells -> nodeWithCells.getAssociatedCells().forEach(cell -> {
            final int position = cellsOfSectors.indexOf(cell);
            if (position >= 0) {
                cellToNode.put(cellsOfSectors.get(position).getTopologyObjectId(), nodeWithCells.getNode());
            }
        }));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("CellToNode structure contains {} items after filtering", cellToNode.size());
        }
    }

    private void fillProfiles(final CmNodeProfileRetriever cmNodeProfileRetriever, final Collection<Cell> cellsOfLBQs) {
        final Iterator<Collection<NodeWithIdleModePrioAtReleases>> iterator = cmNodeProfileRetriever.retrieve();
        cellToNode.values().forEach(node -> usedProfiles.put(node.getTopologyObjectId(), 0));
        while (iterator.hasNext()) {
            final Collection<NodeWithIdleModePrioAtReleases> nodesWithProfiles = iterator.next();
            if (LOGGER.isInfoEnabled()) {
                final long numberOfProfiles = nodesWithProfiles.stream()
                        .mapToLong(nodeWithProfiles -> nodeWithProfiles.getAssociatedProfiles().size())
                        .sum();
                LOGGER.info("Retrieved {} NodeWithIdleModePrioAtReleases objects", numberOfProfiles);
            }
            nodesWithProfiles.forEach(nodeWithProfiles -> nodeWithProfiles.getAssociatedProfiles().forEach(profile -> {
                if (isProfileReferenced(profile.getFdn(), cellsOfLBQs)) {
                    normalizeDistributedInfo(profile);
                    profiles.put(profile.getTopologyObjectId(), profile);
                }
                usedProfiles.computeIfPresent(nodeWithProfiles.getNode().getTopologyObjectId(), (oldNodeId, oldValue) -> oldValue + 1);
            }));
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Profiles structure contains {} items after filtering", profiles.size());
        }
    }

    private static void normalizeDistributedInfo(final IdleModePrioAtRelease profile) {
        if (profile != null && profile.getDistributionInfos() != null) {
                profile.setDistributionInfos(profile.getDistributionInfos()
                        .stream()
                        .map(IdleModePrioAtReleaseUtils::removeGrouping)
                        .map(IdleModePrioAtReleaseUtils::removeDuplicateFreqs)
                        .collect(Collectors.toList()));
        }
    }

    private boolean isProfileReferenced(final String idleModePrioAtReleaseFdn, final Collection<Cell> cells) {
        return cells.stream().anyMatch(cell -> cell.getIdleModePrioAtReleaseRef() != null &&
                cell.getIdleModePrioAtReleaseRef().equals(idleModePrioAtReleaseFdn));
    }

    private void fillFrequencies(final CmNodeFrequencyRetriever cmNodeFrequencyRetriever) {
        final Collection<NodeWithEutranFrequencies> nodesWithEutranFrequencies = cmNodeFrequencyRetriever.retrieve();
        if (LOGGER.isInfoEnabled()) {
            final long numberOfFrequencies = nodesWithEutranFrequencies.stream()
                    .mapToLong(nodesWithEutranFrequency -> nodesWithEutranFrequency.getAssociatedFrequencies().size())
                    .sum();
            LOGGER.info("Retrieved {} NodeWithEutranFrequencies objects", numberOfFrequencies);
        }
        nodesWithEutranFrequencies.stream().filter(nodeWithEutranFrequencies -> cellToNode.containsValue(nodeWithEutranFrequencies.getNode()))
                .forEach(nodeWithEutranFrequencies -> nodeToFrequencies.put(nodeWithEutranFrequencies.getNode().getTopologyObjectId(),
                        nodeWithEutranFrequencies.getAssociatedFrequencies()));
        if (LOGGER.isInfoEnabled()) {
            final long numberOfFrequencies = nodeToFrequencies.values().stream()
                    .mapToLong(Collection::size)
                    .sum();
            LOGGER.info("NodeToFrequencies structure contains {} items after filtering", numberOfFrequencies);
        }
    }

    /**
     * This method is used in tests only.
     * @return the number of nodes
     */
    int getNumberOfNodesToFrequencies() {
        return nodeToFrequencies.size();
    }

    /**
     * This method is used in tests only.
     * @return number of cells.
     */
    int getNumberOfCells() {
        return cellToNode.size();
    }

    /**
     * This method is used in tests only.
     * @return number of profiles
     */
    int getNumberOfProfiles() {
        return profiles.size();
    }

    /**
     * This method is used in tests only.
     * @return number of profiles
     */
    int getNumberOfUsedProfilesEntries() {
        return usedProfiles.size();
    }
}

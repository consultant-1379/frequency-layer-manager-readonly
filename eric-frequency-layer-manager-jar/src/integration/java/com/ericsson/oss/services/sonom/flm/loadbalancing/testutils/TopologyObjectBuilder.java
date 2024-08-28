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

package com.ericsson.oss.services.sonom.flm.loadbalancing.testutils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObject;
import com.ericsson.oss.services.sonom.cm.service.api.TopologyType;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.DistributionInfo;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node.FeatureState;
import com.google.gson.Gson;

public class TopologyObjectBuilder {
    private static final Gson GSON = new Gson().newBuilder().disableHtmlEscaping().create();

    private TopologyObjectBuilder() {
    }

    /**
     * Creates a cell type TopologyObject.
     * @param cellId id of cell
     * @param ossId ossId of cell
     * @param fdn fdn of cell
     * @param carrier carrier of cell
     * @param idleModePrioAtReleaseRef reference to IdleModePrioAtRelease fdn
     * @param cgi cgi of cell
     * @param bandwidth bandwidth of cell
     * @param installationType installationType of cell
     * @return a cell type {@link TopologyType}
     */
    public static TopologyObject buildCell(final Long cellId, final int ossId, final String fdn, final int carrier,
                                           final String idleModePrioAtReleaseRef, final String cgi, final Integer bandwidth,
                                           final String installationType, final String lteNrSpectrumShared) {
        final TopologyObject cell = new TopologyObject();
        cell.setType(TopologyType.CELL.toString());
        cell.setId(cellId);
        cell.setAttribute("fdn", fdn);
        cell.setAttribute("oss_id", ossId);
        cell.setAttribute("cgi", cgi);
        cell.setAttribute("bandwidth", bandwidth);
        cell.setAttribute("carrier", carrier);
        cell.setAttribute("idleModePrioAtReleaseRef", idleModePrioAtReleaseRef);
        cell.setAttribute("installationType", installationType);
        cell.setAttribute("lteNrSpectrumShared", lteNrSpectrumShared);
        return cell;
    }

    /**
     * It builds a node type {@link TopologyObject} from given parameters.
     * @param id id of the node
     * @param fdn fdn of the node
     * @param ossId ossId of the node
     * @param featureState featureState string of the node
     * @param revision revision of the node
     * @param nodeType nodeType of the noe
     * @return an instance of {@link TopologyObject} of type node
     */
    public static TopologyObject buildNode(final long id, final String fdn, final int ossId, final FeatureState featureState,
                                           final String revision, final String nodeType) {
        final TopologyObject node = new TopologyObject();
        node.setType(TopologyType.NODE.toString());
        node.setId(id);
        node.setAttribute("fdn", fdn);
        node.setAttribute("oss_id", ossId);
        node.setAttribute("featureState", toJson(featureState));
        node.setAttribute("revision", revision);
        node.setAttribute("nodeType", nodeType);
        return node;
    }

    /**
     * It builds an IdleModePrioAtRelease type {@link TopologyObject} from given parameters.
     * @param id id of the IdleModePrioAtRelease
     * @param fdn fdn of the IdleModePrioAtRelease
     * @param ossId ossId of the IdleModePrioAtRelease
     * @param name name string of the IdleModePrioAtRelease
     * @param thresholds thresholds of the IdleModePrioAtRelease
     * @param distributionInfos distributionInfos of the IdleModePrioAtRelease
     * @param reservedBy set of cell fdns
     * @return an instance of {@link TopologyObject} of type IdleModePrioAtRelease
     */
    public static TopologyObject buildProfile(final long id, final String fdn, final int ossId, final String name, final List<Integer> thresholds,
                                              final List<DistributionInfo> distributionInfos, final Set<String> reservedBy) {
        final TopologyObject profile = new TopologyObject();
        profile.setType(TopologyType.IDLEMODEPRIOATRELEASE.toString());
        profile.setId(id);
        profile.setAttribute("fdn", fdn);
        profile.setAttribute("oss_id", ossId);
        profile.setAttribute("name", name);

        profile.setAttribute("lowLoadThreshold", thresholds.get(ThresholdLevel.LOW_LOAD_THRESHOLD.ordinal()));
        profile.setAttribute("lowMediumLoadThreshold", thresholds.get(ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD.ordinal()));
        profile.setAttribute("mediumLoadThreshold", thresholds.get(ThresholdLevel.MEDIUM_LOAD_THRESHOLD.ordinal()));
        profile.setAttribute("mediumHighLoadThreshold", thresholds.get(ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD.ordinal()));
        profile.setAttribute("highLoadThreshold", thresholds.get(ThresholdLevel.HIGH_LOAD_THRESHOLD.ordinal()));

        profile.setAttribute("lowLoadDistributionInfo", toJson(distributionInfos.get(ThresholdLevel.LOW_LOAD_THRESHOLD.ordinal())));
        profile.setAttribute("lowMediumLoadDistributionInfo", toJson(distributionInfos.get(ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD.ordinal())));
        profile.setAttribute("mediumLoadDistributionInfo", toJson(distributionInfos.get(ThresholdLevel.MEDIUM_LOAD_THRESHOLD.ordinal())));
        profile.setAttribute("mediumHighLoadDistributionInfo", toJson(distributionInfos.get(ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD.ordinal())));
        profile.setAttribute("highLoadDistributionInfo", toJson(distributionInfos.get(ThresholdLevel.HIGH_LOAD_THRESHOLD.ordinal())));

        profile.setAttribute("reservedBy", GSON.toJson(reservedBy));

        return profile;
    }

    /**
     * It builds EUtranFrequency type of {@link TopologyObject} from the given parameters.
     * @param id id of the frequency
     * @param fdn fdn of the frequency
     * @param ossId ossId of the frequency
     * @param name name of the frequency
     * @param arfcnValueEUtranDl carrier value of the frequency
     * @return it returns an EUtranFrequency type of {@link TopologyObject}
     */
    public static TopologyObject buildFrequency(final long id, final String fdn, final int ossId, final String name, final int arfcnValueEUtranDl) {
        final TopologyObject frequency = new TopologyObject();
        frequency.setType(TopologyType.EUTRANFREQUENCY.toString());
        frequency.setId(id);
        frequency.setAttribute("fdn", fdn);
        frequency.setAttribute("oss_id", ossId);
        frequency.setAttribute("name", name);
        frequency.setAttribute("arfcnValueEUtranDl", arfcnValueEUtranDl);
        return frequency;
    }

    /**
     * Creates a Sector with Cells.
     * @param sectorId a sector id
     * @param cells a list of cell Topology Objects
     * @return returns a sector TopologyObject with cells
     */
    public static TopologyObject buildSectorWithCells(final Long sectorId, final List<TopologyObject> cells) {
        final TopologyObject sectorWithCells = new TopologyObject();
        sectorWithCells.setId(sectorId);
        sectorWithCells.setType(TopologyType.SECTOR.toString());
        sectorWithCells.setAssociations(cells);
        return sectorWithCells;
    }

    /**
     * Create a Node with cells.
     * @param node a node TopologyObject
     * @param cells a list of cell TopologyObjects
     * @return a node TopologyObject with associated cells
     */
    public static TopologyObject buildNodeWithCells(final TopologyObject node, final List<TopologyObject> cells) {
        final TopologyObject nodeWithCells = new TopologyObject();
        nodeWithCells.setType(node.getType());
        nodeWithCells.setId(node.getId());
        nodeWithCells.setAttributes(node.getAttributes());
        nodeWithCells.setChildren(cells);
        return nodeWithCells;
    }

    /**
     * It returns a {@link TopologyObject} that contains a node with provided profiles.
     * @param node the node {@link TopologyObject}
     * @param profiles the list of profiles {@link TopologyObject}
     * @return a node type of {@link TopologyObject}
     */
    public static TopologyObject buildNodeWithProfiles(final TopologyObject node, final List<TopologyObject> profiles) {
        final TopologyObject nodeWithProfiles = new TopologyObject();
        nodeWithProfiles.setType(node.getType());
        nodeWithProfiles.setId(node.getId());
        nodeWithProfiles.setAttributes(node.getAttributes());
        nodeWithProfiles.setChildren(profiles);
        return nodeWithProfiles;
    }

    /**
     * It returns a {@link TopologyObject} that contains a node with provided frequencies.
     * @param node the node {@link TopologyObject}
     * @param frequencies the list of EUtranFrequency {@link TopologyObject}
     * @return a node type of {@link TopologyObject}
     */
    public static TopologyObject buildNodeWithFrequencies(final TopologyObject node, final List<TopologyObject> frequencies) {
        final TopologyObject nodeWithFrequencies = new TopologyObject();
        nodeWithFrequencies.setType(node.getType());
        nodeWithFrequencies.setId(node.getId());
        nodeWithFrequencies.setAttributes(node.getAttributes());
        nodeWithFrequencies.setChildren(frequencies);
        return nodeWithFrequencies;
    }

    private static String toJson(final FeatureState featureState) {
        return "{" +
               "SubscriberTriggeredMobility" + ":" + (featureState.isSubscriberTriggeredMobilityActivated() ? "\"ACTIVATED\"" : "\"DEACTIVATED\"") + "," +
               "LoadBasedDistributionAtRelease" + ":" + (featureState.isLoadBasedDistributionAtReleaseActivated() ? "\"ACTIVATED\"" : "\"DEACTIVATED\"") + "," +
               "EvolvedLoadBasedDistributionAtRelease" + ":" + (featureState.isEvolvedLoadBasedDistributionAtReleaseActivated() ? "\"ACTIVATED\"" : "\"DEACTIVATED\"") + "," +
               "InterFrequencyLoadBalancing" + ":" + (featureState.isInterFrequencyLoadBalancingActivated() ? "\"ACTIVATED\"" : "\"DEACTIVATED\"") + "," +
               "CapabilityAwareIdleModeControl" + ":" + (featureState.getCapabilityAwareIdleModeControlActivated()) + "," +
               "ENDCTriggeredHandoverduringSetup" + ":" + (featureState.isEndcTriggeredHandoverDuringSetupActivated() ? "\"ACTIVATED\"" : "\"DEACTIVATED\"") +
               "}";
    }

    private static String toJson(final DistributionInfo distributionInfo) {
        final List<Integer> modifiedDistributions = distributionInfo.getFreqDistributionList()
                .stream()
                .map(Float::intValue)
                .collect(Collectors.toList());
        return "{" +
                "eUtranFreqRefList: " +
                GSON.toJson(distributionInfo.getEUtranFreqRefList()) +
                "," +
                "freqDistributionList: " +
                GSON.toJson(modifiedDistributions) +
                "}";
    }
}

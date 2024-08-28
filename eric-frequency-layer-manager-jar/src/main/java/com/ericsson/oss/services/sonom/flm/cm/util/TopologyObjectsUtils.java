/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.cm.util;

import static com.ericsson.oss.services.sonom.flm.cm.util.Caimc.UNDEFINED;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObject;
import com.ericsson.oss.services.sonom.cm.service.api.TopologyType;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.EUtranFrequency;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.google.gson.Gson;

/**
 * Utility function for retrieving cells/profiles from topology objects.
 */
public final class TopologyObjectsUtils {
    private static final String ACTIVATED = "ACTIVATED";
    private static final String DEACTIVATED = "DEACTIVATED";
    private static final String BANDWIDTH = "bandwidth";
    private static final String OSS_ID = "oss_id";
    private static final String FDN = "fdn";
    private static final String CARRIER = "carrier";
    private static final String IDLEMODEPRIOATRELEASEREF = "idleModePrioAtReleaseRef";
    private static final String CGI = "cgi";
    private static final String LTENRSPECTRUMSHARED = "lteNrSpectrumShared";

    private TopologyObjectsUtils() {
        // Intentionally private.
    }

    /**
     * Retrieve all of the cells from a list of topology objects.
     * @param topologyObjects list of {@link TopologyObject}
     * @return List of Cells
     */
    public static List<Cell> getAllCellObjectsFromTopologyObjects(final List<TopologyObject> topologyObjects) {
        return topologyObjects.parallelStream()
                .filter(TopologyObjectsUtils::isACell)
                .map(TopologyObjectsUtils::getCellObjectFromTopologyObject)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all of the EUtranFrequencies from a list of topology objects.
     * @param topologyObjects list of {@link TopologyObject}
     * @return List of EUtranFrequencies
     */
    public static List<EUtranFrequency> getAllEUtranFrequencyObjectsFromTopologyObjects(final List<TopologyObject> topologyObjects) {
        return topologyObjects.parallelStream()
                .filter(TopologyObjectsUtils::isAEUtranFrequency)
                .map(TopologyObjectsUtils::getEUtranFrequencyObjectFromTopologyObject)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all of the profiles from a list of topology objects.
     * @param profileTopologyObjects list of {@link TopologyObject}
     * @return List of {@link IdleModePrioAtRelease}
     */
    public static List<IdleModePrioAtRelease> getAllProfileObjectsFromTopologyObjects(final List<TopologyObject> profileTopologyObjects) {
        return profileTopologyObjects
                .parallelStream()
                .filter(TopologyObjectsUtils::isAProfile)
                .map(TopologyObjectsUtils::getProfileObjectFromTopologyObject)
                .collect(Collectors.toList());
    }

    /**
     * Check topology object is a cell.
     * @param topologyObject a single {@link TopologyObject}
     * @return boolean
     */
    public static boolean isACell(final TopologyObject topologyObject) {
        return topologyObject.getType().equals(TopologyType.CELL.toString());
    }

    /**
     * Check topology object is a {@link EUtranFrequency}.
     * @param topologyObject a single {@link TopologyObject}
     * @return boolean
     */
    public static boolean isAEUtranFrequency(final TopologyObject topologyObject) {
        return topologyObject.getType().equals(TopologyType.EUTRANFREQUENCY.toString());
    }

    /**
     * Check topology object is a profile.
     * @param profileTopologyObjects a single {@link TopologyObject}
     * @return boolean
     */
    public static boolean isAProfile(final TopologyObject profileTopologyObjects) {
        return profileTopologyObjects.getType().equals(TopologyType.IDLEMODEPRIOATRELEASE.toString());
    }

    /**
     * Check topology object is a node.
     * @param nodeTopologyObjects a single {@link TopologyObject}
     * @return boolean
     */
    public static boolean isANode(final TopologyObject nodeTopologyObjects) {
        return nodeTopologyObjects.getType().equals(TopologyType.NODE.toString());
    }

    /**
     * Get the cell object from the topology object.
     * @param topologyObject a single {@link TopologyObject}
     * @return Cell
     */
    public static Cell getCellObjectFromTopologyObject(final TopologyObject topologyObject) {
        final Object carrier = topologyObject.getAttributes().get(CARRIER);
        final Object idleModePrioAtReleaseRef = topologyObject.getAttributes().get(IDLEMODEPRIOATRELEASEREF);
        final Object cgi = topologyObject.getAttributes().get(CGI);
        final Object bandwidth = topologyObject.getAttributes().get(BANDWIDTH);
        final Object lteNrSpectrumShared = topologyObject.getAttributes().get(LTENRSPECTRUMSHARED);
        return new Cell(topologyObject.getId(),
                        Integer.parseInt(topologyObject.getAttributes().get(OSS_ID).toString()),
                        topologyObject.getAttributes().get(FDN).toString(),
                        carrier == null ? -1 : Integer.parseInt(carrier.toString()),
                        idleModePrioAtReleaseRef == null ? null : idleModePrioAtReleaseRef.toString(),
                        cgi == null ? null : cgi.toString(),
                        bandwidth == null ? null : Integer.parseInt(bandwidth.toString()),
                        topologyObject.getAttributes().get("installationType").toString(),
                        lteNrSpectrumShared == null ? "undefined" : lteNrSpectrumShared.toString());
    }

    /**
     * Get the EUtranFrequency object from the topology object.
     * @param eUtranFrequencyTopologyObject a single {@link TopologyObject}
     * @return EUtranFrequency
     */
    public static EUtranFrequency getEUtranFrequencyObjectFromTopologyObject(final TopologyObject eUtranFrequencyTopologyObject) {
        if (!isAEUtranFrequency(eUtranFrequencyTopologyObject)) {
            throw new IllegalArgumentException("TopologyObject type is not EUtranFrequency!");
        }
        return new EUtranFrequency(eUtranFrequencyTopologyObject.getId(),
                                   eUtranFrequencyTopologyObject.getAttributes().get(FDN).toString(),
                                   Integer.parseInt(eUtranFrequencyTopologyObject.getAttributes().get(OSS_ID).toString()),
                                   eUtranFrequencyTopologyObject.getAttributes().get("name").toString(),
                                   Integer.parseInt(eUtranFrequencyTopologyObject.getAttributes().get("arfcnValueEUtranDl").toString()));
    }

    /**
     * Get the profile object from the topology object.
     * @param profileTopologyObject a single {@link TopologyObject}
     * @return {@link IdleModePrioAtRelease}
     */
    public static IdleModePrioAtRelease getProfileObjectFromTopologyObject(final TopologyObject profileTopologyObject) {
        if (!isAProfile(profileTopologyObject)) {
            throw new IllegalArgumentException("TopologyObject type is not Profile!");
        }
        return new IdleModePrioAtRelease(profileTopologyObject.getId(),
                    profileTopologyObject.getAttributes().get(FDN).toString(),
                    Integer.parseInt(profileTopologyObject.getAttributes().get(OSS_ID).toString()),
                    profileTopologyObject.getAttributes().get("name").toString(),
                    //thresholds
                    Arrays.asList(Integer.parseInt(profileTopologyObject.getAttributes().get("lowLoadThreshold").toString()),
                                  Integer.parseInt(profileTopologyObject.getAttributes().get("lowMediumLoadThreshold").toString()),
                                  Integer.parseInt(profileTopologyObject.getAttributes().get("mediumLoadThreshold").toString()),
                                  Integer.parseInt(profileTopologyObject.getAttributes().get("mediumHighLoadThreshold").toString()),
                                  Integer.parseInt(profileTopologyObject.getAttributes().get("highLoadThreshold").toString())),
                    //distributionInfoList
                    Arrays.asList(getDistributionInfo(profileTopologyObject, IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD),
                                  getDistributionInfo(profileTopologyObject, IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD),
                                  getDistributionInfo(profileTopologyObject, IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD),
                                  getDistributionInfo(profileTopologyObject, IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD),
                                  getDistributionInfo(profileTopologyObject, IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD)),
                    new HashSet<>(getReservedBy(profileTopologyObject)));
    }

    /**
     * Get the node object from the topology object.
     * @param nodeTopologyObject a single {@link TopologyObject}
     * @return node
     */
    public static Node getNodeObjectFromTopologyObject(final TopologyObject nodeTopologyObject) {
        if (!isANode(nodeTopologyObject)) {
            throw new IllegalArgumentException("TopologyObject type is not Node!");
        }
        return new Node(nodeTopologyObject.getId(),
                        nodeTopologyObject.getAttributes().get(FDN).toString(),
                        Integer.parseInt(nodeTopologyObject.getAttributes().get(OSS_ID).toString()),
                        getFeatureState(nodeTopologyObject),
                        nodeTopologyObject.getAttributes().get("nodeType") == null ? null :
                        nodeTopologyObject.getAttributes().get("nodeType").toString());
    }

    /**
     * Get the feature state from the topology object.
     * @param nodeTopologyObject a single {@link TopologyObject}
     * @return node.FeatureState
     */
    public static Node.FeatureState getFeatureState(final TopologyObject nodeTopologyObject) {
        final Object featureStatesObj = nodeTopologyObject.getAttributes().get("featureState");
        if (featureStatesObj != null) {
            final String featureStates = featureStatesObj.toString();
            final Map<String, Object> featureStateMap = new Gson().fromJson(featureStates, Map.class);
            return new Node.FeatureState(featureStateMap.getOrDefault("SubscriberTriggeredMobility", DEACTIVATED).toString().equals(ACTIVATED),
                    featureStateMap.getOrDefault("LoadBasedDistributionAtRelease", DEACTIVATED).toString().equals(ACTIVATED),
                    featureStateMap.getOrDefault("EvolvedLoadBasedDistributionAtRelease", DEACTIVATED).toString()
                            .equals(ACTIVATED),
                    featureStateMap.getOrDefault("InterFrequencyLoadBalancing", DEACTIVATED).toString().equals(ACTIVATED),
                    featureStateMap.getOrDefault("CapabilityAwareIdleModeControl", UNDEFINED.getCaimcValue()).toString(),
                    featureStateMap.getOrDefault("ENDCTriggeredHandoverduringSetup", DEACTIVATED).toString().equals(ACTIVATED));
        }
        return null;
    }

    private static List<String> getReservedBy(final TopologyObject profileTopologyObject) {
        final Object reservedByObject = profileTopologyObject.getAttributes().get("reservedBy");
        return reservedByObject == null ? Collections.emptyList() : new Gson().fromJson(reservedByObject.toString(), List.class);
    }

    private static IdleModePrioAtRelease.DistributionInfo getDistributionInfo(final TopologyObject profileTopologyObject,
            final IdleModePrioAtRelease.ThresholdLevel level) {
        final String levelName = getLevelName(level);
        final String mapAsString = profileTopologyObject.getAttributes().get(levelName).toString();
        final Map<String, Object> distrMap = new Gson().fromJson(mapAsString, Map.class);
        final List<Double> list = (List<Double>) distrMap.get("freqDistributionList");
        final List<Float> freqDistributionList = list.stream().map(Double::floatValue).collect(Collectors.toList());
        final List<String> eUtranFreRefList = (List<String>) distrMap.get("eUtranFreqRefList");
        return new IdleModePrioAtRelease.DistributionInfo(level, freqDistributionList, eUtranFreRefList);
    }

    private static String getLevelName(final IdleModePrioAtRelease.ThresholdLevel level) {
        switch (level) {
            case LOW_LOAD_THRESHOLD: return "lowLoadDistributionInfo";
            case LOW_MEDIUM_LOAD_THRESHOLD: return "lowMediumLoadDistributionInfo";
            case MEDIUM_LOAD_THRESHOLD: return "mediumLoadDistributionInfo";
            case MEDIUM_HIGH_LOAD_THRESHOLD: return "mediumHighLoadDistributionInfo";
            case HIGH_LOAD_THRESHOLD : return "highLoadDistributionInfo";
            default: return null;
        }
    }
}

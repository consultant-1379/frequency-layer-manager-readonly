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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.core.util.Sets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObject;
import com.ericsson.oss.services.sonom.cm.service.api.TopologyType;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.EUtranFrequency;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.DistributionInfo;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node.FeatureState;
import com.google.gson.JsonObject;

/**
 * Unit tests for {@link TopologyObjectsUtils} class.
 */
public class TopologyObjectsUtilsTest {

    private static final String FDN = "fdn";
    private static final String CARRIER_NAME = "carrier";
    private static final int CARRIER_VALUE = 5230;
    private static final String IDLE_MODE_PRIO_AT_RELEASE_REF_NAME = "idleModePrioAtReleaseRef";
    private static final String IDLE_MODE_PRIO_AT_RELEASE_REF_VALUE = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1," +
            "MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    private static final String CGI_NAME = "cgi";
    private static final String CGI_VALUE = "311-480-54004-1";
    private static final String BANDWIDTH_NAME = "bandwidth";
    private static final Integer BANDWIDTH_VALUE = 1400;
    private static final String INSTALLATION_TYPE = "installationType";
    private static final String OUTDOOR = "outdoor";
    private static final String DISTRIBUTION_INFO = "{\"freqDistributionList\": [0, 1, 86, 13]," +
            "\"eUtranFreqRefList\": " +
            "[\"SubNetwork=SON,MeContext=1,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1\"," +
            "\"SubNetwork=SON,MeContext=1,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2585\"," +
            "\"SubNetwork=SON,MeContext=1,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2100\", " +
            "\"SubNetwork=SON,MeContext=1,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1000\"]}";
    private static final String OSS_ID_NAME = "oss_id";
    private static final int OSS_ID_VALUE = 1;
    private static final String NAME = "name";
    private static final Set<String> RESERVED_BY_CONTENT = Sets.newLinkedHashSet("SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_053," +
            "MeContext=653919_ROSE_BOWL_PERM_DAS13,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=653919_3");
    private static final long ID = 111L;
    private static final String LTE_NR_SPECTRUM_SHARED = "lteNrSpectrumShared";
    private static final String LTE_NR_SPECTRUM_SHARED_VALUE = "undefined";

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void whenAllCellObjectsAreRetrievedFromTopologyObjectsList_thenOnlyCellObjectsAreRetrieved() {
        final TopologyObject cellTO = new TopologyObject();
        cellTO.setType(TopologyType.CELL.toString());
        cellTO.setId(ID);
        cellTO.setAttribute(OSS_ID_NAME, OSS_ID_VALUE);
        cellTO.setAttribute(FDN, FDN);
        cellTO.setAttribute(CARRIER_NAME, CARRIER_VALUE);
        cellTO.setAttribute(IDLE_MODE_PRIO_AT_RELEASE_REF_NAME, IDLE_MODE_PRIO_AT_RELEASE_REF_VALUE);
        cellTO.setAttribute(CGI_NAME, CGI_VALUE);
        cellTO.setAttribute(BANDWIDTH_NAME, BANDWIDTH_VALUE);
        cellTO.setAttribute(INSTALLATION_TYPE, OUTDOOR);
        cellTO.setAttribute(LTE_NR_SPECTRUM_SHARED, LTE_NR_SPECTRUM_SHARED_VALUE);

        final TopologyObject nodeTO = new TopologyObject();
        nodeTO.setType(TopologyType.NODE.toString());
        nodeTO.setId(ID);
        nodeTO.setAttribute(OSS_ID_NAME, OSS_ID_VALUE);
        nodeTO.setAttribute(FDN, FDN);

        final List<TopologyObject> topologyObjects = Arrays.asList(cellTO, nodeTO);

        final List<Cell> result = TopologyObjectsUtils.getAllCellObjectsFromTopologyObjects(topologyObjects);
        assertThat(result)
                .containsExactly(new Cell(ID, OSS_ID_VALUE, FDN, CARRIER_VALUE, IDLE_MODE_PRIO_AT_RELEASE_REF_VALUE, CGI_VALUE, BANDWIDTH_VALUE,
                        OUTDOOR, LTE_NR_SPECTRUM_SHARED_VALUE));
    }

    @Test
    public void whenCheckingATopologyObjectIsACellTopologyObject_thenReturnTrueIfObjectIsACellObject() {
        final TopologyObject topologyObject = new TopologyObject();
        topologyObject.setType(TopologyType.CELL.toString());

        final boolean result = TopologyObjectsUtils.isACell(topologyObject);

        assertThat(result).isTrue();
    }

    @Test
    public void whenCheckingATopologyObjectIsACellTopologyObject_thenReturnFalseIfObjectIsNotACellObject() {
        final TopologyObject topologyObject = new TopologyObject();
        topologyObject.setType(TopologyType.NODE.toString());

        final boolean result = TopologyObjectsUtils.isACell(topologyObject);

        assertThat(result).isFalse();
    }

    @Test
    public void whenACellObjectIsRetrievedFromATopologyObject_thenCellObjectIsReturned() {
        final TopologyObject topologyObject1 = new TopologyObject();
        topologyObject1.setId(ID);
        topologyObject1.setAttribute(OSS_ID_NAME, OSS_ID_VALUE);
        topologyObject1.setAttribute(FDN, FDN);
        topologyObject1.setAttribute(CARRIER_NAME, CARRIER_VALUE);
        topologyObject1.setAttribute(IDLE_MODE_PRIO_AT_RELEASE_REF_NAME, IDLE_MODE_PRIO_AT_RELEASE_REF_VALUE);
        topologyObject1.setAttribute(CGI_NAME, CGI_VALUE);
        topologyObject1.setAttribute(BANDWIDTH_NAME, BANDWIDTH_VALUE);
        topologyObject1.setAttribute(INSTALLATION_TYPE, OUTDOOR);
        topologyObject1.setAttribute(LTE_NR_SPECTRUM_SHARED, LTE_NR_SPECTRUM_SHARED_VALUE);
        final Cell result1 = TopologyObjectsUtils.getCellObjectFromTopologyObject(topologyObject1);

        softly.assertThat(result1)
                .isEqualTo(new Cell(ID, OSS_ID_VALUE, FDN, CARRIER_VALUE, IDLE_MODE_PRIO_AT_RELEASE_REF_VALUE, CGI_VALUE, BANDWIDTH_VALUE, OUTDOOR, LTE_NR_SPECTRUM_SHARED_VALUE));
        final TopologyObject topologyObject2 = new TopologyObject();
        topologyObject2.setId(ID);
        topologyObject2.setAttribute(OSS_ID_NAME, String.valueOf(OSS_ID_VALUE));
        topologyObject2.setAttribute(FDN, FDN);
        topologyObject2.setAttribute(INSTALLATION_TYPE, OUTDOOR);
        topologyObject2.setAttribute(LTE_NR_SPECTRUM_SHARED, LTE_NR_SPECTRUM_SHARED_VALUE);
        final Cell result2 = TopologyObjectsUtils.getCellObjectFromTopologyObject(topologyObject2);
        softly.assertThat(result2).isEqualTo(new Cell(ID, OSS_ID_VALUE, FDN, null, OUTDOOR, LTE_NR_SPECTRUM_SHARED_VALUE));
    }

    @Test
    public void whenACellObjectIsRetrievedWithNullValuesFromATopologyObject_thenCellObjectIsReturnedWithDefaultValues() {
        final TopologyObject topologyObject = new TopologyObject();
        topologyObject.setId(ID);
        topologyObject.setAttribute(OSS_ID_NAME, OSS_ID_VALUE);
        topologyObject.setAttribute(FDN, FDN);
        topologyObject.setAttribute(CARRIER_NAME, null);
        topologyObject.setAttribute(IDLE_MODE_PRIO_AT_RELEASE_REF_NAME, null);
        topologyObject.setAttribute(CGI_NAME, null);
        topologyObject.setAttribute(BANDWIDTH_NAME, null);
        topologyObject.setAttribute(INSTALLATION_TYPE, OUTDOOR);
        topologyObject.setAttribute(LTE_NR_SPECTRUM_SHARED, LTE_NR_SPECTRUM_SHARED_VALUE);
        final Cell result = TopologyObjectsUtils.getCellObjectFromTopologyObject(topologyObject);
        assertThat(result).isEqualTo(new Cell(ID, OSS_ID_VALUE, FDN, -1, null, null, null, OUTDOOR, LTE_NR_SPECTRUM_SHARED_VALUE));
    }

    @Test
    public void whenNodeTopologyObjectPassedToGetNodeObjectFromTopologyObject_thenNodeObjectCreated() {
        final TopologyObject topologyObject = createNodeTopologyObject(ID, OSS_ID_VALUE);
        addFeatureState(topologyObject);

        final Node node = TopologyObjectsUtils.getNodeObjectFromTopologyObject(topologyObject);
        assertThat(node).isEqualTo(new Node(ID, FDN, OSS_ID_VALUE, new FeatureState(true, false, true, false, "ACTIVATED", false), "nodetype"));

    }

    @Test
    public void whenNodeTopologyObjectFeatureIsEmptyPassedToGetNodeObjectFromTopologyObject_thenNodeObjectCreated() {
        final TopologyObject topologyObject = createNodeTopologyObject(ID, OSS_ID_VALUE);

        final Node node = TopologyObjectsUtils.getNodeObjectFromTopologyObject(topologyObject);
        assertThat(node).isEqualTo(new Node(ID, FDN, OSS_ID_VALUE, null, "nodetype"));
    }

    @Test
    public void whenProfileTopologyObjectPassedToGetProfileObjectFromTopologyObject_thenProfileObjectCreated() {
        final TopologyObject topologyObject = createProfileTopologyObjectWithReservedBy(ID, OSS_ID_VALUE);
        final List<DistributionInfo> distributionInfoList = getDistributionInfo();
        final Set<String> reservedByList = new HashSet<>(RESERVED_BY_CONTENT);

        final IdleModePrioAtRelease profile = TopologyObjectsUtils.getProfileObjectFromTopologyObject(topologyObject);

        assertThat(profile).isEqualTo(new IdleModePrioAtRelease(ID, FDN, OSS_ID_VALUE, NAME,
                Arrays.asList(1, 2, 3, 4, 5), distributionInfoList, reservedByList));
    }

    @Test
    public void whenProfileTopologyObjectReservedByIsEmptyPassedToGetProfileObjectFromTopologyObject_thenProfileObjectCreated() {
        final TopologyObject topologyObject = createProfileTopologyObject(ID, OSS_ID_VALUE);
        final List<DistributionInfo> distributionInfoList = getDistributionInfo();

        final IdleModePrioAtRelease profile = TopologyObjectsUtils.getProfileObjectFromTopologyObject(topologyObject);

        assertThat(profile).isEqualTo(new IdleModePrioAtRelease(ID, FDN, OSS_ID_VALUE, NAME,
                Arrays.asList(1, 2, 3, 4, 5), distributionInfoList, Collections.EMPTY_SET));
    }

    @Test
    public void whenProfileTopologyObjectPassedToGetNodeObjectFromTopologyObject_thenExceptionThrown() {
        final TopologyObject topologyObject = createProfileTopologyObject(ID, OSS_ID_VALUE);

        thrown.expect(IllegalArgumentException.class);
        TopologyObjectsUtils.getNodeObjectFromTopologyObject(topologyObject);
    }

    @Test
    public void whenNodeTopologyObjectPassedToGetProfileObjectFromTopologyObject_thenExceptionThrown() {
        final TopologyObject topologyObject = createNodeTopologyObject(ID, OSS_ID_VALUE);
        thrown.expect(IllegalArgumentException.class);
        TopologyObjectsUtils.getProfileObjectFromTopologyObject(topologyObject);
    }

    @Test
    public void whenGetAllProfileObjectFromTopologyObjectCalled_thenProfileObjectListCreated() {
        final List<TopologyObject> topologyObjectList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            topologyObjectList.add(createProfileTopologyObjectWithReservedBy(ID + i, OSS_ID_VALUE + i));
        }
        final List<IdleModePrioAtRelease.DistributionInfo> distributionInfoList = getDistributionInfo();

        final Set<String> reservedByList = new HashSet<>(RESERVED_BY_CONTENT);

        final List<IdleModePrioAtRelease> profileList = TopologyObjectsUtils.getAllProfileObjectsFromTopologyObjects(topologyObjectList);
        assertThat(profileList).hasSize(5);

        for (int i = 0; i < 5; i++) {
            final AtomicBoolean elementFound = new AtomicBoolean(false);
            final int incrementValue = i;
            profileList.forEach(profile -> {
                if (profile.equals(new IdleModePrioAtRelease(ID + incrementValue, FDN, OSS_ID_VALUE + incrementValue, NAME,
                        Arrays.asList(1, 2, 3, 4, 5), distributionInfoList, reservedByList))) {
                    elementFound.set(true);
                }
            });
            softly.assertThat(elementFound).as("Unable to find topology object index: %d", i).isTrue();
        }
    }

    @Test
    public void whenGetAllProfileObjectFromTopologyObjectCalled_thenFilteredListReturned() {
        final List<TopologyObject> topologyObjectList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            topologyObjectList.add(createProfileTopologyObjectWithReservedBy(ID + i, OSS_ID_VALUE + i));
        }
        final List<IdleModePrioAtRelease.DistributionInfo> distributionInfoList = getDistributionInfo();
        topologyObjectList.add(createNodeTopologyObject(ID, OSS_ID_VALUE));

        final Set<String> reservedByList = new HashSet<>(RESERVED_BY_CONTENT);

        final List<IdleModePrioAtRelease> profileList = TopologyObjectsUtils.getAllProfileObjectsFromTopologyObjects(topologyObjectList);
        assertThat(profileList).hasSize(5);

        for (int i = 0; i < 5; i++) {
            final AtomicBoolean elementFound = new AtomicBoolean(false);
            final int incrementValue = i;
            profileList.forEach(profile -> {
                if (profile.equals(new IdleModePrioAtRelease(ID + incrementValue, FDN, OSS_ID_VALUE + incrementValue, NAME,
                        Arrays.asList(1, 2, 3, 4, 5), distributionInfoList, reservedByList))) {
                    elementFound.set(true);
                }
            });
            assertThat(elementFound).isTrue();
        }
    }

    @Test
    public void whenIsAProfileCalledWithProfile_returnsTrue() {
        final TopologyObject topologyObject = new TopologyObject();
        topologyObject.setType(TopologyType.IDLEMODEPRIOATRELEASE.toString());
        assertThat(TopologyObjectsUtils.isAProfile(topologyObject)).isTrue();
    }

    @Test
    public void whenIsAProfileCalledWithNotAProfile_returnsFalse() {
        final TopologyObject topologyObject = new TopologyObject();
        topologyObject.setType(TopologyType.NODE.toString());
        assertThat(TopologyObjectsUtils.isAProfile(topologyObject)).isFalse();
    }

    @Test
    public void whenIsANodeCalledWithNode_returnsTrue() {
        final TopologyObject topologyObject = new TopologyObject();
        topologyObject.setType(TopologyType.NODE.toString());
        assertThat(TopologyObjectsUtils.isANode(topologyObject)).isTrue();
    }

    @Test
    public void whenIsANodeCalledWithNotANode_returnsFalse() {
        final TopologyObject topologyObject = new TopologyObject();
        topologyObject.setType(TopologyType.IDLEMODEPRIOATRELEASE.toString());
        assertThat(TopologyObjectsUtils.isANode(topologyObject)).isFalse();
    }

    private TopologyObject createNodeTopologyObject(final long id, final int ossId) {
        final TopologyObject topologyObject = new TopologyObject();
        topologyObject.setId(id);
        topologyObject.setType(TopologyType.NODE.toString());
        topologyObject.setAttribute(OSS_ID_NAME, ossId);
        topologyObject.setAttribute(FDN, FDN);
        topologyObject.setAttribute("nodeType", "nodetype");
        return topologyObject;
    }

    private TopologyObject createProfileTopologyObject(final long id, final int ossId) {
        final TopologyObject topologyObject = new TopologyObject();
        topologyObject.setId(id);
        topologyObject.setType(TopologyType.IDLEMODEPRIOATRELEASE.toString());
        topologyObject.setAttribute(OSS_ID_NAME, ossId);
        topologyObject.setAttribute(FDN, FDN);
        topologyObject.setAttribute(NAME, NAME);
        setThresholds(topologyObject);
        setDistributionInfos(topologyObject);
        return topologyObject;
    }

    private TopologyObject createProfileTopologyObjectWithReservedBy(final long id, final int ossId) {
        final TopologyObject topologyObject = createProfileTopologyObject(id, ossId);
        topologyObject.setAttribute("reservedBy", String.format("[\"%s\"]", RESERVED_BY_CONTENT.iterator().next()));
        return topologyObject;
    }

    private void addFeatureState(final TopologyObject topologyObject) {
        final JsonObject featureStateObject = new JsonObject();
        addFeaturedStateAttributes(featureStateObject);
        final String featureState = featureStateObject.toString();
        topologyObject.setAttribute("featureState", featureState);
    }

    private void addFeaturedStateAttributes(final JsonObject featureStateObject) {
        final String activated = "ACTIVATED";
        final String deactivated = "DEACTIVATED";
        featureStateObject.addProperty("SubscriberTriggeredMobility", activated);
        featureStateObject.addProperty("LoadBasedDistributionAtRelease", deactivated);
        featureStateObject.addProperty("EvolvedLoadBasedDistributionAtRelease", activated);
        featureStateObject.addProperty("InterFrequencyLoadBalancing", deactivated);
        featureStateObject.addProperty("CapabilityAwareIdleModeControl", activated);
        featureStateObject.addProperty("ENDCTriggeredHandoverduringSetup", deactivated);
    }

    private void setDistributionInfos(final TopologyObject topologyObject) {
        topologyObject.setAttribute("lowLoadDistributionInfo", DISTRIBUTION_INFO);
        topologyObject.setAttribute("lowMediumLoadDistributionInfo", DISTRIBUTION_INFO);
        topologyObject.setAttribute("mediumLoadDistributionInfo", DISTRIBUTION_INFO);
        topologyObject.setAttribute("mediumHighLoadDistributionInfo", DISTRIBUTION_INFO);
        topologyObject.setAttribute("highLoadDistributionInfo", DISTRIBUTION_INFO);
    }

    private void setThresholds(final TopologyObject topologyObject) {
        topologyObject.setAttribute("lowLoadThreshold", "1");
        topologyObject.setAttribute("lowMediumLoadThreshold", "2");
        topologyObject.setAttribute("mediumLoadThreshold", "3");
        topologyObject.setAttribute("mediumHighLoadThreshold", "4");
        topologyObject.setAttribute("highLoadThreshold", "5");
    }

    private List<DistributionInfo> getDistributionInfo() {
        final List<Float> freqDistributionList = Arrays.asList(0.0f, 1.0f, 86.0f, 13.0f);
        final List<String> eUtranFreRefList = Arrays.asList(
                "SubNetwork=SON,MeContext=1,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1",
                "SubNetwork=SON,MeContext=1,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2585",
                "SubNetwork=SON,MeContext=1,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2100",
                "SubNetwork=SON,MeContext=1,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1000");
        return Arrays.asList(
                new DistributionInfo(ThresholdLevel.LOW_LOAD_THRESHOLD, freqDistributionList, eUtranFreRefList),
                new DistributionInfo(ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD, freqDistributionList, eUtranFreRefList),
                new DistributionInfo(ThresholdLevel.MEDIUM_LOAD_THRESHOLD, freqDistributionList, eUtranFreRefList),
                new DistributionInfo(ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD, freqDistributionList, eUtranFreRefList),
                new DistributionInfo(ThresholdLevel.HIGH_LOAD_THRESHOLD, freqDistributionList, eUtranFreRefList));
    }

    @Test
    public void whenGetAllEUtranFrequencyObjectsFromEmptyList_thenReturnEmptyList() {
        assertThat(TopologyObjectsUtils.getAllEUtranFrequencyObjectsFromTopologyObjects(Collections.emptyList())).isEmpty();
    }

    @Test
    public void whenGetAllEUtranFrequencyObjectsFromTOsWithoutEuTranFreqs_thenReturnEmptyList() {
        final TopologyObject notEuTran = new TopologyObject();
        notEuTran.setType(TopologyType.CELL.toString());
        assertThat(TopologyObjectsUtils.getAllEUtranFrequencyObjectsFromTopologyObjects(Collections.singletonList(notEuTran))).isEmpty();
    }

    @Test
    public void whenGetAllEUtranFrequencyObjectsFromTos_thenReturnAllEuTrans() {
        final long id = 123L;
        final String name = "eutran1";
        final int arfcnValueEUtranDl = 4;
        final TopologyObject euTran = new TopologyObject();
        euTran.setType(TopologyType.EUTRANFREQUENCY.toString());
        euTran.setId(id);
        euTran.setAttribute(FDN, FDN);
        euTran.setAttribute(OSS_ID_NAME, OSS_ID_VALUE);
        euTran.setAttribute("name", name);
        euTran.setAttribute("arfcnValueEUtranDl", arfcnValueEUtranDl);

        final TopologyObject notEuTran = new TopologyObject();
        notEuTran.setType(TopologyType.CELL.toString());
        final List<EUtranFrequency> allEUtranFrequencyObjects =
                TopologyObjectsUtils.getAllEUtranFrequencyObjectsFromTopologyObjects(Arrays.asList(notEuTran, euTran));

        assertThat(allEUtranFrequencyObjects).hasSize(1);
        final EUtranFrequency euTranFreq = allEUtranFrequencyObjects.get(0);
        assertThat(euTranFreq).isEqualToComparingFieldByField(new EUtranFrequency(id, FDN, OSS_ID_VALUE, name, arfcnValueEUtranDl));
    }

    @Test
    public void whenGetEUtranFrequencyObjectFromNonEuTranFreq_thenThrowException() {
        final TopologyObject notEuTran = new TopologyObject();
        notEuTran.setType(TopologyType.CELL.toString());
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("TopologyObject type is not EUtranFrequency!");
        TopologyObjectsUtils.getEUtranFrequencyObjectFromTopologyObject(notEuTran);
    }
}
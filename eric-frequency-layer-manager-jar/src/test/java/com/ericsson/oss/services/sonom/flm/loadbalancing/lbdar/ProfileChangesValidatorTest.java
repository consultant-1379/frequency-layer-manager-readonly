/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder;

/**
 * Unit tests for {@link ProfileChangesValidatorTest} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileChangesValidatorTest {

    private static final IdleModePrioAtRelease TPROFILE1 = buildValidProfile();
    private static final String EXECUTION_ID = "FLM_2_EXEC_ID";
    private static final String P1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00004,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    private static final String F1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2300";
    private static final String F2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00004,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2300";
    private static final String C1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00004,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00004-1";
    private static final Node.FeatureState ALL_TRUE_FEATURE_STATE = new Node.FeatureState(true, true, true, true, "ACTIVATED", true);
    private static final Node.FeatureState IN_ACTIVE_FEATURE_STATE = new Node.FeatureState(true, false, true, true, "ACTIVATED", true);
    private static final Node N1 = new Node(1L, InputDataBuilder.N1, 1, ALL_TRUE_FEATURE_STATE, "RadioNode");
    private static final Node N2 = new Node(1L, InputDataBuilder.N1, 1, IN_ACTIVE_FEATURE_STATE, "RadioNode");
    private static final TopologyObjectId CELL_ID_1 = TopologyObjectId.of(C1, 1);
    private static final Cell CELL1 = new Cell(1L, 1, C1, 1000, ModelConstants.OUTDOOR, "undefined");
    private static final long SECTOR_ID = 1L;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private ProfileChangesValidator objectUnderTest;
    private EnrichedProfileChanges enrichedProfileChanges;

    @Test
    public void whenEnrichedProfileChangesIsOk_thenValidationWillPass() {
        enrichedProfileChanges = new EnrichedProfileChanges(new ProfileChanges(EXECUTION_ID, SECTOR_ID, 100, getCellToProfiles(), getCellCmData()), getNodesByCell(N1), getProfilesUsedByNodes(N1));
        objectUnderTest = new ProfileChangesValidator();
        softly.assertThat(objectUnderTest.validate(enrichedProfileChanges)).isTrue();
    }

    @Test
    public void whenEnrichedProfileChangesIsNull_thenValidationWillBroke() {
        objectUnderTest = new ProfileChangesValidator();
        softly.assertThat(objectUnderTest.validate(null)).isFalse();
    }

    @Test
    public void whenEnrichedProfileChangesDoesNotContainProfileChanges_thenValidationWillBroke() {
        enrichedProfileChanges = new EnrichedProfileChanges(null, getNodesByCell(N1), getProfilesUsedByNodes(N1));
        objectUnderTest = new ProfileChangesValidator();
        softly.assertThat(objectUnderTest.validate(enrichedProfileChanges)).isFalse();
    }

    @Test
    public void whenCellToProfileMapInProfileChangesIsNull_thenValidationWillBroke() {
        enrichedProfileChanges = new EnrichedProfileChanges(new ProfileChanges(EXECUTION_ID, SECTOR_ID, 100, null, getCellCmData()), getNodesByCell(N1), getProfilesUsedByNodes(N1));
        objectUnderTest = new ProfileChangesValidator();
        softly.assertThat(objectUnderTest.validate(enrichedProfileChanges)).isFalse();
    }

    @Test
    public void whenCellToProfileMapInProfileChangesIsEmpty_thenValidationWillBroke() {
        enrichedProfileChanges = new EnrichedProfileChanges(new ProfileChanges(EXECUTION_ID, SECTOR_ID, 100, new HashMap<>(), getCellCmData()), getNodesByCell(N1), getProfilesUsedByNodes(N1));
        objectUnderTest = new ProfileChangesValidator();
        softly.assertThat(objectUnderTest.validate(enrichedProfileChanges)).isFalse();
    }

    @Test
    public void whenSourceUsersMoveIsZero_thenValidationWillBroke() {
        enrichedProfileChanges = new EnrichedProfileChanges(new ProfileChanges(EXECUTION_ID, SECTOR_ID, 0, getCellToProfiles(), getCellCmData()), getNodesByCell(N1), getProfilesUsedByNodes(N1));
        objectUnderTest = new ProfileChangesValidator();
        softly.assertThat(objectUnderTest.validate(enrichedProfileChanges)).isFalse();
    }

    @Test
    public void whenSourceUsersMoveIsNegative_thenValidationWillBroke() {
        enrichedProfileChanges = new EnrichedProfileChanges(new ProfileChanges(EXECUTION_ID, SECTOR_ID, -10, getCellToProfiles(), getCellCmData()), getNodesByCell(N1), getProfilesUsedByNodes(N1));
        objectUnderTest = new ProfileChangesValidator();
        softly.assertThat(objectUnderTest.validate(enrichedProfileChanges)).isFalse();
    }

    @Test
    public void whenNodeNotFoundForSourceCell_thenValidationWillBroke() {
        final Map<TopologyObjectId, Node> nodesByCell = getNodesByCell(N1);
        nodesByCell.remove(CELL_ID_1);
        enrichedProfileChanges = new EnrichedProfileChanges(new ProfileChanges(EXECUTION_ID, SECTOR_ID, 100, getCellToProfiles(), getCellCmData()), nodesByCell, getProfilesUsedByNodes(N1));
        objectUnderTest = new ProfileChangesValidator();
        softly.assertThat(objectUnderTest.validate(enrichedProfileChanges)).isFalse();
    }

    @Test
    public void whenLbdarIsNotActivatedOnNode_thenValidationWillBroke() {
        enrichedProfileChanges = new EnrichedProfileChanges(new ProfileChanges(EXECUTION_ID, SECTOR_ID, 100, getCellToProfiles(), getCellCmData()), getNodesByCell(N2), getProfilesUsedByNodes(N2));
        objectUnderTest = new ProfileChangesValidator();
        softly.assertThat(objectUnderTest.validate(enrichedProfileChanges)).isFalse();
    }

    private Map<TopologyObjectId, Integer> getProfilesUsedByNodes(final Node node) {
        final Map<TopologyObjectId, Integer> profilesUsedByNodes = new HashMap<>();
        profilesUsedByNodes.put(TopologyObjectId.of(node.getFdn(), node.getOssId()), 8);
        return profilesUsedByNodes;
    }

    private Map<TopologyObjectId, Node> getNodesByCell(final Node node) {
        final Map<TopologyObjectId, Node> nodesByCell = new HashMap<>();
        nodesByCell.put(CELL_ID_1, node);
        return nodesByCell;
    }

    private Map<TopologyObjectId, Cell> getCellCmData() {
        final Map<TopologyObjectId, Cell> cellCmData = new HashMap<>();
        cellCmData.put(CELL_ID_1, CELL1);
        return cellCmData;
    }

    private Map<TopologyObjectId, IdleModePrioAtRelease> getCellToProfiles() {
        final Map<TopologyObjectId, IdleModePrioAtRelease> cellToProfiles = new HashMap<>();
        cellToProfiles.put(CELL_ID_1, TPROFILE1);
        return cellToProfiles;
    }

    private static IdleModePrioAtRelease buildValidProfile() {
        return new IdleModePrioAtRelease(1, P1, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
                Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(ThresholdLevel.LOW_LOAD_THRESHOLD,
                                Arrays.asList(10f, 90f), Arrays.asList(F1, F2)),
                        new IdleModePrioAtRelease.DistributionInfo(ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                                Arrays.asList(10f, 90f), Arrays.asList(F1, F2)),
                        new IdleModePrioAtRelease.DistributionInfo(ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                                Arrays.asList(10f, 90f), Arrays.asList(F1, F2)),
                        new IdleModePrioAtRelease.DistributionInfo(ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                                Arrays.asList(10f, 90f), Arrays.asList(F1, F2)),
                        new IdleModePrioAtRelease.DistributionInfo(ThresholdLevel.HIGH_LOAD_THRESHOLD,
                                Arrays.asList(10f, 90f), Arrays.asList(F1, F2))), Collections.singleton(C1));
    }

}
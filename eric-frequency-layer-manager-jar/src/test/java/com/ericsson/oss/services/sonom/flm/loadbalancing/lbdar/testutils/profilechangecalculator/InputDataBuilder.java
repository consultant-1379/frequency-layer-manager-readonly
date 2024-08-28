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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator;

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants.OUTDOOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.DistributionInfo;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

public class InputDataBuilder {
    public static final int OSS_ID = 1;
    public static final String C1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-1";
    public static final String C2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00002-1";
    public static final String C3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00003-1";

    public static final String N1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001";

    public static final String P1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    public static final String P2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    public static final String P3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";

    public static final String F1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1500";
    public static final String F2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2300";
    public static final String F3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=4500";

    public static final String F4 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=900";
    public static final String F5 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2050";
    public static final String F6 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=66986";

    public static final TopologyObjectId CELL_SOURCE = new TopologyObjectId(C1, OSS_ID);
    public static final TopologyObjectId CELL_TARGET_ONE = new TopologyObjectId(C2, OSS_ID);
    public static final TopologyObjectId CELL_TARGET_TWO = new TopologyObjectId(C3, OSS_ID);

    public static final TopologyObjectId PROFILE_ONE = new TopologyObjectId(P1, OSS_ID);
    public static final TopologyObjectId PROFILE_TWO = new TopologyObjectId(P2, OSS_ID);
    public static final TopologyObjectId PROFILE_THREE = new TopologyObjectId(P3, OSS_ID);

    public static final String SOURCE_PROFILE = "Source Profile";
    public static final String TARGET_ONE_PROFILE = "TargetOne Profile";
    public static final String TARGET_TWO_PROFILE = "TargetTwo Profile";

    private InputDataBuilder() {
    }

    public static PolicyOutputEvent buildPolicyOutputEvent(final List<Integer> usersToMove) {

        final Integer totalNumbers = usersToMove.stream().reduce(0, Integer::sum);
        final ProposedLoadBalancingQuanta lbq = new ProposedLoadBalancingQuanta(C1, OSS_ID, String.valueOf(totalNumbers),
                buildTargetCells(usersToMove));

        return new PolicyOutputEvent(lbq);
    }

    public static PolicyOutputEvent buildPolicyOutputEventWithCustomTotalNumbers(final List<Integer> usersToMove, final Integer totalNumbers) {
        final ProposedLoadBalancingQuanta lbq = new ProposedLoadBalancingQuanta(C1, OSS_ID, String.valueOf(totalNumbers),
                buildTargetCells(usersToMove));

        return new PolicyOutputEvent(lbq);
    }

    public static List<TargetCell> buildTargetCells(final List<Integer> usersToMove) {
        final List<TargetCell> targetCells = new ArrayList<>();
        final TargetCell targetCellOne = new TargetCell(C2, OSS_ID, String.valueOf(usersToMove.get(0)));
        targetCells.add(targetCellOne);
        if (usersToMove.size() > 1) {
            final TargetCell targetCellTwo = new TargetCell(C3, OSS_ID, String.valueOf(usersToMove.get(1)));
            targetCells.add(targetCellTwo);
        }
        return targetCells;
    }

    public static Map<TopologyObjectId, Cell> buildCmData(final int numberOfTargetCells) {
        final Map<TopologyObjectId, Cell> cmData = new HashMap<>();
        cmData.put(CELL_SOURCE, new Cell(1L, OSS_ID, C1, 1500, P1, "cgi1", 5000, OUTDOOR, "undefined"));
        cmData.put(CELL_TARGET_ONE, new Cell(2L, OSS_ID, C2, 2300, P2, "cgi2", 5000, OUTDOOR, "undefined"));
        if (numberOfTargetCells > 1) {
            cmData.put(CELL_TARGET_TWO, new Cell(3L, OSS_ID, C3, 4500, P3, "cgi3", 5000, OUTDOOR, "undefined"));
        }
        return cmData;
    }

    public static Map<TopologyObjectId, Cell> buildCmDataForSelfRetainTooLow() {
        final Map<TopologyObjectId, Cell> cmData = new HashMap<>();
        cmData.put(CELL_SOURCE, new Cell(1L, OSS_ID, C1, 900, P1, "cgi1", 5000, OUTDOOR, "undefined"));
        cmData.put(CELL_TARGET_ONE, new Cell(2L, OSS_ID, C2, 2050, P2, "cgi2", 5000, OUTDOOR, "undefined"));
        cmData.put(CELL_TARGET_TWO, new Cell(3L, OSS_ID, C3, 66986, P3, "cgi3", 5000, OUTDOOR, "undefined"));
        return cmData;
    }

    public static Map<TopologyObjectId, Cell> buildCmDataForSelfRetainTooLowAndProfileChangeCreatedSameAsOriginal() {
        final Map<TopologyObjectId, Cell> cmData = new HashMap<>();
        cmData.put(CELL_SOURCE, new Cell(1L, OSS_ID, C1, 66961, P1, "cgi1", 5000, OUTDOOR, "undefined"));
        cmData.put(CELL_TARGET_ONE, new Cell(2L, OSS_ID, C2, 2050, P2, "cgi2", 5000, OUTDOOR, "undefined"));
        return cmData;
    }

    public static Map<TopologyObjectId, Cell> buildCmDataForDuplicateCellCarrier(final int numberOfTargetCells) {
        final Map<TopologyObjectId, Cell> cmData = new HashMap<>();
        cmData.put(CELL_SOURCE, new Cell(1L, OSS_ID, C1, 1500, P1, "cgi1", 5000, OUTDOOR, "undefined"));
        cmData.put(CELL_TARGET_ONE, new Cell(2L, OSS_ID, C2, 2300, P2, "cgi2", 5000, OUTDOOR, "undefined"));
        if (numberOfTargetCells > 1) {
            cmData.put(CELL_TARGET_TWO, new Cell(3L, OSS_ID, C3, 2300, P3, "cgi3", 5000, OUTDOOR, "undefined"));
        }
        return cmData;
    }

    public static Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> buildEnrichedProfiles(final boolean simpleCase) {
        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileMap = new HashMap<>();
        profileMap.put(PROFILE_ONE, buildEnrichedProfileForSource(simpleCase));
        profileMap.put(PROFILE_TWO, buildEnrichedProfileForTargetOne(simpleCase));
        if (!simpleCase) {
            profileMap.put(PROFILE_THREE, buildEnrichedProfileForTargetTwo());
        }
        return profileMap;
    }

    public static Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> buildEnrichedProfilesMissingSource() {
        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileMap = new HashMap<>();
        profileMap.put(PROFILE_ONE, buildEnrichedProfileWithMissingSource());
        profileMap.put(PROFILE_TWO, buildEnrichedProfileForTargetOne(true));
        return profileMap;
    }

    public static Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> buildEnrichedProfilesMissingCarrier() {
        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileMap = new HashMap<>();
        profileMap.put(PROFILE_ONE, buildEnrichedProfileForSourceMissingCarrier());
        profileMap.put(PROFILE_TWO, buildEnrichedProfileForTargetOne(true));
        return profileMap;
    }

    public static Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> buildEnrichedProfilesForSelfRetainTooLow() {
        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileMap = new HashMap<>();
        profileMap.put(PROFILE_ONE, buildEnrichedProfileForSourceSelfRetainTooLow());
        profileMap.put(PROFILE_TWO, buildEnrichedProfileForTargetOneSelfRetainTooLow());
        profileMap.put(PROFILE_THREE, buildEnrichedProfileForTargetTwoSelfRetainTooLow());
        return profileMap;
    }

    private static EnrichedIdleModePrioAtRelease buildEnrichedProfileForSource(final boolean simpleCase) {
        final List<Integer> thresholds = Arrays.asList(0, 250, 450, 700, 900);
        final List<EnrichedDistributionInfo> distributionInfos = buildEnrichedDistributionInfosForSource(simpleCase);
        return new EnrichedIdleModePrioAtRelease(1L, P1, OSS_ID, SOURCE_PROFILE, thresholds, distributionInfos, Collections.emptySet());
    }

    private static EnrichedIdleModePrioAtRelease buildEnrichedProfileWithMissingSource() {
        final List<Integer> thresholds = Arrays.asList(0, 250, 450, 700, 900);
        final List<EnrichedDistributionInfo> distributionInfos = buildEnrichedDistributionInfosMissingSource();
        return new EnrichedIdleModePrioAtRelease(1L, P1, OSS_ID, SOURCE_PROFILE, thresholds, distributionInfos, Collections.emptySet());
    }

    private static EnrichedIdleModePrioAtRelease buildEnrichedProfileForSourceMissingCarrier() {
        final List<Integer> thresholds = Arrays.asList(0, 250, 450, 700, 900);
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(F1, 1500);
        final List<EnrichedDistributionInfo> distributionInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final EnrichedDistributionInfo distributionInfo;
            distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i], Collections.singletonList(100f),
                    Collections.singletonList(F1)), frequencyToCarrier);
            distributionInfos.add(distributionInfo);
        }
        return new EnrichedIdleModePrioAtRelease(1L, P1, OSS_ID, SOURCE_PROFILE, thresholds, distributionInfos, Collections.emptySet());
    }

    private static EnrichedIdleModePrioAtRelease buildEnrichedProfileForSourceSelfRetainTooLow() {
        final List<Integer> thresholds = Arrays.asList(0, 250, 450, 700, 900);
        final List<EnrichedDistributionInfo> distributionInfos = buildEnrichedDistributionInfosForSourceSelfRetainTooLow();
        return new EnrichedIdleModePrioAtRelease(1L, P1, OSS_ID, SOURCE_PROFILE, thresholds, distributionInfos, Collections.emptySet());
    }

    public static List<EnrichedDistributionInfo> buildEnrichedDistributionInfosForSource(final boolean simpleCase) {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(F1, 1500);
        frequencyToCarrier.put(F2, 2300);
        final List<EnrichedDistributionInfo> distributionInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final EnrichedDistributionInfo distributionInfo;
            if (simpleCase) {
                distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i], Arrays.asList(90f, 10f),
                        Arrays.asList(F1, F2)), frequencyToCarrier);
            } else {
                frequencyToCarrier.put(F3, 4500);
                distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i], Arrays.asList(80f, 10f, 10f),
                        Arrays.asList(F1, F2, F3)), frequencyToCarrier);
            }

            distributionInfos.add(distributionInfo);
        }
        return distributionInfos;
    }

    public static List<EnrichedDistributionInfo> buildEnrichedDistributionInfosMissingSource() {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(F1, 1500);
        frequencyToCarrier.put(F2, 2300);
        final Map<String, Integer> frequencyToCarrierMissingSource = new HashMap<>();
        frequencyToCarrierMissingSource.put(F1, 1500);
        frequencyToCarrierMissingSource.put(F2, 2400);

        final List<EnrichedDistributionInfo> distributionInfos = new ArrayList<>();
        EnrichedDistributionInfo distributionInfo = new EnrichedDistributionInfo(
                new DistributionInfo(ThresholdLevel.values()[0], Arrays.asList(90f, 10f),
                        Arrays.asList(F1, F2)),
                frequencyToCarrierMissingSource);
        distributionInfos.add(distributionInfo);

        for (int i = 1; i < 5; i++) {
            distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i], Arrays.asList(90f, 10f),
                    Arrays.asList(F1, F2)), frequencyToCarrier);

            distributionInfos.add(distributionInfo);
        }
        return distributionInfos;
    }

    public static List<EnrichedDistributionInfo> buildEnrichedDistributionInfosForSourceSelfRetainTooLow() {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(F4, 900);
        frequencyToCarrier.put(F5, 2050);
        frequencyToCarrier.put(F6, 66986);
        final List<EnrichedDistributionInfo> distributionInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final EnrichedDistributionInfo distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i],
                    Arrays.asList(25f, 50f, 25f), Arrays.asList(F4, F5, F6)), frequencyToCarrier);
            distributionInfos.add(distributionInfo);
        }
        return distributionInfos;
    }

    private static EnrichedIdleModePrioAtRelease buildEnrichedProfileForTargetOne(final boolean simpleCase) {
        final List<Integer> thresholds = Arrays.asList(0, 250, 450, 700, 900);
        final List<EnrichedDistributionInfo> distributionInfos = buildEnrichedDistributionInfosForTargetOne(simpleCase);
        return new EnrichedIdleModePrioAtRelease(2L, P2, 1, TARGET_ONE_PROFILE, thresholds, distributionInfos, Collections.emptySet());
    }

    private static EnrichedIdleModePrioAtRelease buildEnrichedProfileForTargetOneSelfRetainTooLow() {
        final List<Integer> thresholds = Arrays.asList(0, 250, 450, 700, 900);
        final List<EnrichedDistributionInfo> distributionInfos = buildEnrichedDistributionInfosForTargetOneSelfRetainTooLow();
        return new EnrichedIdleModePrioAtRelease(2L, P2, 1, TARGET_ONE_PROFILE, thresholds, distributionInfos, Collections.emptySet());
    }

    public static List<EnrichedDistributionInfo> buildEnrichedDistributionInfosForTargetOne(final boolean simpleCase) {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(F1, 1500);
        frequencyToCarrier.put(F2, 2300);
        final List<EnrichedDistributionInfo> distributionInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final EnrichedDistributionInfo distributionInfo;
            if (simpleCase) {
                distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i], Arrays.asList(80f, 20f),
                        Arrays.asList(F2, F1)), frequencyToCarrier);
            } else {
                frequencyToCarrier.put(F3, 4500);
                distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i], Arrays.asList(60f, 20f, 20f),
                        Arrays.asList(F2, F1, F3)), frequencyToCarrier);
            }
            distributionInfos.add(distributionInfo);
        }
        return distributionInfos;
    }

    public static List<EnrichedDistributionInfo> buildEnrichedDistributionInfosForTargetOneSelfRetainTooLow() {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(F4, 900);
        frequencyToCarrier.put(F5, 2050);
        frequencyToCarrier.put(F6, 66986);
        final List<EnrichedDistributionInfo> distributionInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final EnrichedDistributionInfo distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i],
                    Arrays.asList(25f, 50f, 25f), Arrays.asList(F4, F5, F6)), frequencyToCarrier);
            distributionInfos.add(distributionInfo);
        }
        return distributionInfos;
    }

    public static IdleModePrioAtRelease buildProfileForTargetTwo() {
        final List<Integer> thresholds = Arrays.asList(0, 250, 450, 700, 900);
        final List<DistributionInfo> distributionInfos = buildDistributionInfosForTargetTwo(false);
        return new IdleModePrioAtRelease(3L, P3, 1, TARGET_TWO_PROFILE, thresholds, distributionInfos, Collections.emptySet());
    }

    public static EnrichedIdleModePrioAtRelease buildEnrichedProfileForTargetTwo() {
        final List<Integer> thresholds = Arrays.asList(0, 250, 450, 700, 900);
        final List<EnrichedDistributionInfo> distributionInfos = buildEnrichedDistributionInfosForTargetTwo(false);
        return new EnrichedIdleModePrioAtRelease(3L, P3, 1, TARGET_TWO_PROFILE, thresholds, distributionInfos, Collections.emptySet());
    }

    public static EnrichedIdleModePrioAtRelease buildEnrichedProfileForTargetTwoSelfRetainTooLow() {
        final List<Integer> thresholds = Arrays.asList(0, 250, 450, 700, 900);
        final List<EnrichedDistributionInfo> distributionInfos = buildEnrichedDistributionInfosForTargetTwoSelfRetainTooLow();
        return new EnrichedIdleModePrioAtRelease(3L, P3, 1, TARGET_TWO_PROFILE, thresholds, distributionInfos, Collections.emptySet());
    }

    public static List<DistributionInfo> buildDistributionInfosForTargetTwo(final boolean simpleCase) {
        final List<DistributionInfo> distributionInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final DistributionInfo distributionInfo;
            if (simpleCase) {
                distributionInfo = new DistributionInfo(ThresholdLevel.values()[i], Arrays.asList(90f, 10f),
                        Arrays.asList(F3, F1));
            } else {
                distributionInfo = new DistributionInfo(ThresholdLevel.values()[i], Arrays.asList(80f, 10f, 10f),
                        Arrays.asList(F3, F1, F2));
            }

            distributionInfos.add(distributionInfo);
        }
        return distributionInfos;
    }

    public static List<EnrichedDistributionInfo> buildEnrichedDistributionInfosForTargetTwo(final boolean simpleCase) {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(F1, 1500);
        frequencyToCarrier.put(F3, 4500);
        final List<EnrichedDistributionInfo> distributionInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final EnrichedDistributionInfo distributionInfo;
            if (simpleCase) {
                distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i], Arrays.asList(90f, 10f),
                        Arrays.asList(F3, F1)), frequencyToCarrier);
            } else {

                frequencyToCarrier.put(F2, 2300);
                distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i], Arrays.asList(80f, 10f, 10f),
                        Arrays.asList(F3, F1, F2)), frequencyToCarrier);
            }

            distributionInfos.add(distributionInfo);
        }
        return distributionInfos;
    }

    public static List<EnrichedDistributionInfo> buildEnrichedDistributionInfosForTargetTwoSelfRetainTooLow() {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(F4, 900);
        frequencyToCarrier.put(F5, 2050);
        frequencyToCarrier.put(F6, 66986);
        final List<EnrichedDistributionInfo> distributionInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final EnrichedDistributionInfo distributionInfo = new EnrichedDistributionInfo(new DistributionInfo(ThresholdLevel.values()[i],
                    Arrays.asList(25f, 50f, 25f), Arrays.asList(F4, F5, F6)), frequencyToCarrier);
            distributionInfos.add(distributionInfo);
        }
        return distributionInfos;
    }

    public static List<DistributionInfo> buildDistributionInfosForIndependentCell() {
        final List<DistributionInfo> distributionInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            distributionInfos.add(new DistributionInfo(ThresholdLevel.values()[i],
                    Arrays.asList(0.9f, 0.1f),
                    Arrays.asList(F3, F2)));
        }
        return distributionInfos;
    }

    public static Map<TopologyObjectId, CellKpis> buildCellKpis(final int numberOfTargetCells) {
        final Map<TopologyObjectId, CellKpis> cellKpisMap = new HashMap<>();
        cellKpisMap.put(CELL_SOURCE, buildKpiForSourceCellAndTargetTwo());
        cellKpisMap.put(CELL_TARGET_ONE, buildKpiForTargetOne());
        if (numberOfTargetCells > 1) {
            cellKpisMap.put(CELL_TARGET_TWO, buildKpiForSourceCellAndTargetTwo());
        }
        return cellKpisMap;
    }

    public static Map<TopologyObjectId, CellKpis> buildCellKpisSelfRetainTooLow() {
        final Map<TopologyObjectId, CellKpis> cellKpisMap = new HashMap<>();
        cellKpisMap.put(CELL_SOURCE, buildKpiForSourceCellAndTargetTwoSelfRetainTooLow());
        cellKpisMap.put(CELL_TARGET_ONE, buildKpiForTargetOneSelfRetainTooLow());
        cellKpisMap.put(CELL_TARGET_TWO, buildKpiForTargetTwoSelfRetainTooLow());
        return cellKpisMap;
    }

    public static Map<TopologyObjectId, CellKpis> buildCellKpisSelfRetainTooLowAndProfileChangeIsSameAsOriginal(final int numberOfTargetCells) {
        final Map<TopologyObjectId, CellKpis> cellKpisMap = new HashMap<>();
        cellKpisMap.put(CELL_SOURCE, buildKpiForSourceCellSelfRetainTooLowAndProfileChangeIsSameAsOriginal());
        cellKpisMap.put(CELL_TARGET_ONE, buildKpiForTargetOneSelfRetainTooLowAndProfileChangeIsSameAsOriginal());
        if (numberOfTargetCells > 1) {
            cellKpisMap.put(CELL_TARGET_TWO, buildKpiForSourceCellAndTargetTwo());
        }
        return cellKpisMap;
    }

    public static Map<TopologyObjectId, Integer> buildFrequencyToCarrier(final int numberOfTargetCells) {
        final Map<TopologyObjectId, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(TopologyObjectId.of(F1, OSS_ID), 1500);
        frequencyToCarrier.put(TopologyObjectId.of(F2, OSS_ID), 2300);
        if (numberOfTargetCells > 1) {
            frequencyToCarrier.put(TopologyObjectId.of(F3, OSS_ID), 4500);
        }
        return frequencyToCarrier;
    }

    public static Map<TopologyObjectId, Integer> buildFrequencyToCarrierToDuplicateCellCarrier(final int numberOfTargetCells) {
        final Map<TopologyObjectId, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(TopologyObjectId.of(F1, OSS_ID), 1500);
        frequencyToCarrier.put(TopologyObjectId.of(F2, OSS_ID), 2300);
        if (numberOfTargetCells > 1) {
            frequencyToCarrier.put(TopologyObjectId.of(F3, OSS_ID), 2300);
        }
        return frequencyToCarrier;
    }

    public static Map<TopologyObjectId, TopologyObjectId> buildDuplicateCarrierCellToFrequency(final int numberOfTargetCells) {
        final Map<TopologyObjectId, TopologyObjectId> duplicateCarrierCellToFrequency = new HashMap<>();
        if (numberOfTargetCells > 1) {
            duplicateCarrierCellToFrequency.put(CELL_TARGET_ONE, TopologyObjectId.of(F2, OSS_ID));
            duplicateCarrierCellToFrequency.put(CELL_TARGET_TWO, TopologyObjectId.of(F3, OSS_ID));
        }
        return duplicateCarrierCellToFrequency;
    }

    public static Map<TopologyObjectId, Integer> buildFrequencyToCarrierSelfRetainTooLow() {
        final Map<TopologyObjectId, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(TopologyObjectId.of(F4, OSS_ID), 900);
        frequencyToCarrier.put(TopologyObjectId.of(F5, OSS_ID), 2050);
        frequencyToCarrier.put(TopologyObjectId.of(F6, OSS_ID), 66986);
        return frequencyToCarrier;
    }

    private static CellKpis buildKpiForTargetOne() {
        return new CellKpis(500f,
                0.35f,
                0,
                0,
                0,
                4,
                0);
    }

    private static CellKpis buildKpiForTargetOneSelfRetainTooLowAndProfileChangeIsSameAsOriginal() {
        return new CellKpis(37.0125f,
                0.0200208333333333f,
                0,
                0,
                0,
                0,
                5229);
    }

    private static CellKpis buildKpiForTargetOneSelfRetainTooLow() {
        return new CellKpis(94.5541666666667f,
                0.35f,
                0,
                0,
                0,
                4,
                0);
    }

    public static CellKpis buildKpiForSourceCellAndTargetTwo() {
        return new CellKpis(1000f,
                0.5f,
                0,
                0,
                5,
                0,
                0);
    }

    public static CellKpis buildKpiForSourceCellSelfRetainTooLowAndProfileChangeIsSameAsOriginal() {
        return new CellKpis(2.75694444444444f,
                0.00578333333333333f,
                0,
                0,
                0,
                0,
                334);
    }

    public static CellKpis buildKpiForSourceCellAndTargetTwoSelfRetainTooLow() {
        return new CellKpis(103.445833333333f,
                0.5f,
                0,
                0,
                5,
                0,
                0);
    }

    private static CellKpis buildKpiForTargetTwoSelfRetainTooLow() {
        return new CellKpis(5.28333333333333F,
                0.35,
                0,
                0,
                0,
                4,
                0);
    }
}
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.EUtranFrequency;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;

public class TestConstants {
    public static final Node.FeatureState ALL_TRUE_FEATURES = new Node.FeatureState(true, true, true, true, "ACTIVATED", true);
    public static final String N1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1";
    public static final String N2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1";
    public static final String N3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1";

    public static final String C1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-1";
    public static final String C2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-2";
    public static final String C3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-3";
    public static final String C4 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00002-1";
    public static final String C5 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00002-2";
    public static final String C6 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00002-3";
    public static final String C7 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00002-4";
    public static final String C8 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00002-5";
    public static final String C9 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00003-1";

    public static final String P1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    public static final String P2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=2";
    public static final String P3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=3";
    public static final String P4 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    public static final String P5 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=2";
    public static final String P6 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=3";
    public static final String P7 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=4";
    public static final String P8 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=5";
    public static final String P9 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";

    public static final String F1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1500";
    public static final String F2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2300";
    public static final String F3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=3200";
    public static final String F4 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1500";
    public static final String F5 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2300";
    public static final String F6 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=3200";
    public static final String F7 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=5400";
    public static final String F8 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1500";

    public static final Node NODE1 = new Node(1L, N1, 1, ALL_TRUE_FEATURES, "ERBS");
    public static final Node NODE2 = new Node(2L, N2, 1, ALL_TRUE_FEATURES, "RadioNode");
    public static final Node NODE3 = new Node(3L, N3, 1, ALL_TRUE_FEATURES, "RadioNode");

    public static final Cell CELL1 = new Cell(1L, 1, C1, 1500, P1, "", 1000, "outdoor", "undefined");
    public static final Cell CELL2 = new Cell(2L, 1, C2, 2300, P2, "", 1000, "outdoor", "undefined");
    public static final Cell CELL3 = new Cell(3L, 1, C3, 3200, P3, "", 1000, "outdoor", "undefined");
    public static final Cell CELL4 = new Cell(4L, 1, C4, 1500, P4, "", 1000, "outdoor", "undefined");
    public static final Cell CELL5 = new Cell(5L, 1, C5, 2300, P5, "", 1000, "outdoor", "undefined");
    public static final Cell CELL6 = new Cell(6L, 1, C6, 3200, P6, "", 1000, "outdoor", "undefined");
    public static final Cell CELL7 = new Cell(7L, 1, C7, 5400, P7, "", 1000, "outdoor", "undefined");
    public static final Cell CELL8 = new Cell(8L, 1, C8, 3200, P8, "", 1000, "outdoor", "undefined");
    public static final Cell CELL9 = new Cell(9L, 1, C9, 5400, P9, "", 1000, "outdoor", "undefined");


    public static final IdleModePrioAtRelease.DistributionInfo D1 = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
        Arrays.asList(5.0F, 0.0F, 95.0F),
        Arrays.asList(F1, F2, F3));
    public static final IdleModePrioAtRelease.DistributionInfo D2 = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
        Arrays.asList(40.0F, 0.0F, 20.0F, 0.0F, 20.0F, 10.0F),
        Arrays.asList(F1, F2, F3, F4, F5, F6));
    public static final IdleModePrioAtRelease.DistributionInfo D3 = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
        Arrays.asList(0.0F, 0.0F, 100.0F),
        Arrays.asList(F1, F2, F3));
    public static final IdleModePrioAtRelease.DistributionInfo D4 = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
        Arrays.asList(44.0F, 55.0F, 1.0F),
        Arrays.asList(F1, F2, F3));
    public static final IdleModePrioAtRelease.DistributionInfo D5 = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(40.0F, 0.0F, 0.0F, 0.0F, 30.0F, 0.0F),
        Arrays.asList(F1, F2, F3, F4, F5, F2));
    public static final IdleModePrioAtRelease.DistributionInfo D6 = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(40.0F, 0.0F, 0.0F, 0.0F, 25.0F, 5.0F, 2.0F, 2.0F, 2.0F),
        Arrays.asList(F1, F2, F3, F4, F5, F2, F6, F2, F1));
    public static final IdleModePrioAtRelease.DistributionInfo D7 = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(83.0F, 0.0F, 0.0F, 0.0F, 17.0F),
        Arrays.asList(F1, F2, F3, F4, F5));
    public static final IdleModePrioAtRelease.DistributionInfo D8 = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(0.0F, 83.0F, 0.0F, 0.0F, 0.0F, 17.0F),
        Arrays.asList(F6, F1, F2, F3, F4, F5));

    public static final IdleModePrioAtRelease.DistributionInfo D1_NORMALIZED = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
        Arrays.asList(3.0F, 2.0F, 95.0F),
        Arrays.asList(F1, F2, F3));
    public static final IdleModePrioAtRelease.DistributionInfo D2_NORMALIZED = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
        Arrays.asList(20.0F, 20.0F, 10.0F, 10.0F, 20.0F, 10.0F),
        Arrays.asList(F1, F2, F3, F4, F5, F6));
    public static final IdleModePrioAtRelease.DistributionInfo D5_NORMALIZED = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(10.0F, 25.0F, 10.0F, 10.0F, 15.0F),
        Arrays.asList(F1, F2, F3, F4, F5));
    public static final IdleModePrioAtRelease.DistributionInfo D6_NORMALIZED = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(12.0F, 17.0F, 10.0F, 10.0F, 25.0F, 2.0F),
        Arrays.asList(F1, F2, F3, F4, F5, F6));
    public static final IdleModePrioAtRelease.DistributionInfo D7_NORMALIZED = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(21.0F, 21.0F, 21.0F, 20.0F, 17.0F),
        Arrays.asList(F1, F2, F3, F4, F5));
    public static final IdleModePrioAtRelease.DistributionInfo D8_NORMALIZED = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(0.0F, 21.0F, 21.0F, 21.0F, 20.0F, 17.0F),
        Arrays.asList(F6, F1, F2, F3, F4, F5));
    public static final IdleModePrioAtRelease.DistributionInfo D5_NORMALIZED_ONLY_UNGROUP = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(10.0F, 10.0F, 10.0F, 10.0F, 15.0F, 15.0F),
        Arrays.asList(F1, F2, F3, F4, F5, F2));
    public static final IdleModePrioAtRelease.DistributionInfo D6_NORMALIZED_ONLY_UNGROUP = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(10.0F, 10.0F, 10.0F, 10.0F, 25.0F, 5.0F, 2.0F, 2.0F, 2.0F),
        Arrays.asList(F1, F2, F3, F4, F5, F2, F6, F2, F1));
    public static final IdleModePrioAtRelease.DistributionInfo D5_NORMALIZED_ONLY_REMOVE_DUP_FREQ = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(40.0F, 0.0F, 0.0F, 0.0F, 30.0F),
        Arrays.asList(F1, F2, F3, F4, F5));
    public static final IdleModePrioAtRelease.DistributionInfo D6_NORMALIZED_ONLY_REMOVE_DUP_FREQ = new IdleModePrioAtRelease.DistributionInfo(
        IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
        Arrays.asList(42.0F, 7.0F, 0.0F, 0.0F, 25.0F, 2.0F),
        Arrays.asList(F1, F2, F3, F4, F5, F6));
    public static final IdleModePrioAtRelease.DistributionInfo D_EMPTYLIST =
            new IdleModePrioAtRelease.DistributionInfo(null, new ArrayList<>(), new ArrayList<>());

    public static final IdleModePrioAtRelease PROFILE1 = new IdleModePrioAtRelease(1L, P1, 1, "1", Collections.emptyList(),
            Collections.emptyList(), Collections.emptySet());
    public static final IdleModePrioAtRelease PROFILE2 = new IdleModePrioAtRelease(2L, P2, 1, "2", Collections.emptyList(),
            Collections.emptyList(), Collections.emptySet());
    public static final IdleModePrioAtRelease PROFILE3 = new IdleModePrioAtRelease(3L, P3, 1, "3", Collections.emptyList(),
            Collections.emptyList(), Collections.emptySet());
    public static final IdleModePrioAtRelease PROFILE4 = new IdleModePrioAtRelease(4L, P4, 1, "1", Collections.emptyList(),
            Collections.emptyList(), Collections.emptySet());
    public static final IdleModePrioAtRelease PROFILE5 = new IdleModePrioAtRelease(5L, P5, 1, "2", Collections.emptyList(),
            Collections.emptyList(), Collections.emptySet());
    public static final IdleModePrioAtRelease PROFILE6 = new IdleModePrioAtRelease(6L, P6, 1, "3", Collections.emptyList(),
            Collections.emptyList(), Collections.emptySet());
    public static final IdleModePrioAtRelease PROFILE7 = new IdleModePrioAtRelease(7L, P7, 1, "4", Collections.emptyList(),
            Collections.emptyList(), Collections.emptySet());
    public static final IdleModePrioAtRelease PROFILE8 = new IdleModePrioAtRelease(8L, P8, 1, "5",
            Collections.emptyList(),
            Arrays.asList(D1, D2, D3, D4, D5, D6, D7, D8),
            Collections.emptySet());
    public static final IdleModePrioAtRelease PROFILE9 = new IdleModePrioAtRelease(9L, P9, 1, "5", Collections.emptyList(),
            Collections.emptyList(), Collections.emptySet());
    public static final IdleModePrioAtRelease PROFILE8_NORMALIZED = new IdleModePrioAtRelease(8L, P8, 1, "5",
            Collections.emptyList(),
            Arrays.asList(D1_NORMALIZED, D2_NORMALIZED, D3, D4,
                          D5_NORMALIZED, D6_NORMALIZED, D7_NORMALIZED, D8_NORMALIZED),
            Collections.emptySet());

    public static final EUtranFrequency FREQ1 = new EUtranFrequency(1L, F1, 1, "1500", 1500);
    public static final EUtranFrequency FREQ2 = new EUtranFrequency(2L, F2, 1, "2300", 2300);
    public static final EUtranFrequency FREQ3 = new EUtranFrequency(3L, F3, 1, "3200", 3200);
    public static final EUtranFrequency FREQ4 = new EUtranFrequency(4L, F4, 1, "1500", 1500);
    public static final EUtranFrequency FREQ5 = new EUtranFrequency(5L, F5, 1, "2300", 2300);
    public static final EUtranFrequency FREQ6 = new EUtranFrequency(6L, F6, 1, "3200", 3200);
    public static final EUtranFrequency FREQ7 = new EUtranFrequency(7L, F7, 1, "5400", 5400);
    public static final EUtranFrequency FREQ8 = new EUtranFrequency(8L, F8, 1, "1500", 1500);

    public static final TopologySector TSECTOR1 = new TopologySector(1L, Arrays.asList(TestConstants.CELL1, TestConstants.CELL2, TestConstants.CELL3));
    public static final TopologySector TSECTOR2 = new TopologySector(2L, Arrays.asList(TestConstants.CELL4, TestConstants.CELL5, TestConstants.CELL6, TestConstants.CELL8));
    public static final TopologySector TSECTOR3 = new TopologySector(3L, Arrays.asList(TestConstants.CELL7, TestConstants.CELL9));
}

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

import java.util.Arrays;
import java.util.Collections;

import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObject;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;

public class TestConstants {
    public static final Node.FeatureState ALL_TRUE_FEATURE_STATE = new Node.FeatureState(true, true, true, true, "ACTIVATED", true);
    // Sector 1 data
    public static final long S1 = 1L;

    public static final String C1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-1";
    public static final String C2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00002-1";

    public static final String N1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001";
    public static final String N2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002";
    public static final String N1_REVERSION = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1";

    public static final String P1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    public static final String P2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    public static final String P1_REVERSION = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,310-410-43234-23";

    public static final String F1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1500";
    public static final String F2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2300";

    public static final TopologyObject TCELL1 = TopologyObjectBuilder.buildCell(1L, 1, TestConstants.C1, 1500, TestConstants.P1, "310-410-43234-23", 5000, "outdoor", "undefined");
    public static final TopologyObject TCELL2 = TopologyObjectBuilder.buildCell(2L, 1, TestConstants.C2, 2300, TestConstants.P2, "310-410-43234-23_1", 5000, "outdoor", "undefined");
    public static final TopologyObject TCELL1_REVERSION = TopologyObjectBuilder.buildCell(1L, 1, TestConstants.C1, 1500, TestConstants.P1, "1", 5000, "outdoor", "undefined");

    public static final TopologyObject TNODE1 = TopologyObjectBuilder.buildNode(1L, TestConstants.N1, 1, ALL_TRUE_FEATURE_STATE, "R13F52", "RadioNode");
    public static final TopologyObject TNODE2 = TopologyObjectBuilder.buildNode(2L, TestConstants.N2, 1, ALL_TRUE_FEATURE_STATE, "R13F52", "ERBS");
    public static final TopologyObject TNODE1_REVERSION = TopologyObjectBuilder.buildNode(1L, TestConstants.N1_REVERSION, 1, ALL_TRUE_FEATURE_STATE, "R13F52", "RadioNode");

    public static final TopologyObject TFREQ1 = TopologyObjectBuilder.buildFrequency(1, TestConstants.F1, 1, "1500", 1500);
    public static final TopologyObject TFREQ2 = TopologyObjectBuilder.buildFrequency(2, TestConstants.F2, 1, "2300", 2300);

    public static final TopologyObject TPROFILE1 = TopologyObjectBuilder.buildProfile(1, P1, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                    Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2))),
            Collections.singleton(C1));
    public static final TopologyObject TPROFILE2 = TopologyObjectBuilder.buildProfile(2, P2, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(80f, 20f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(80f, 20f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(80f, 20f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(80f, 20f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(80f, 20f), Arrays.asList(F1, F2))),
            Collections.singleton(C2));

    public static final TopologyObject RESULTPROFILE1 = TopologyObjectBuilder.buildProfile(1, P1, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(88f, 12f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2))),
            Collections.singleton(C1));

    public static final TopologyObject RESULTPROFILE1_REVERSION = TopologyObjectBuilder.buildProfile(-1, P1_REVERSION, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2))),
            Collections.singleton(C1));

    public static final TopologyObject TSECTOR1 = TopologyObjectBuilder.buildSectorWithCells(S1, Arrays.asList(TCELL1, TCELL2));

    // Sector 2 data
    public static final long S2 = 2L;

    public static final String C3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00003-1";
    public static final String C4 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00003-2";
    public static final String C5 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00004,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00004-1";

    public static final String N3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003";
    public static final String N4 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00004";
    public static final String N3_REVERSION = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1";

    public static final String P3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    public static final String P4 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=2";
    public static final String P5 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00004,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";

    public static final String F3 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1500";
    public static final String F4 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2300";
    public static final String F5 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00004,ManagedElement=1,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=4500";

    public static final TopologyObject TCELL3 = TopologyObjectBuilder.buildCell(3L, 1, TestConstants.C3, 1500, TestConstants.P3, "310-410-43234-23", 5000, "outdoor", "undefined");
    public static final TopologyObject TCELL4 = TopologyObjectBuilder.buildCell(4L, 1, TestConstants.C4, 2300, TestConstants.P4, "310-410-43234-23_1", 5000, "outdoor", "undefined");
    public static final TopologyObject TCELL5 = TopologyObjectBuilder.buildCell(5L, 1, TestConstants.C5, 4500, TestConstants.P5, "310-410-43234-23_1", 5000, "outdoor", "undefined");

    public static final TopologyObject TNODE3 = TopologyObjectBuilder.buildNode(3L, TestConstants.N3, 1, ALL_TRUE_FEATURE_STATE, "R13F52", "RadioNode");
    public static final TopologyObject TNODE4 = TopologyObjectBuilder.buildNode(4L, TestConstants.N4, 1, ALL_TRUE_FEATURE_STATE, "R13F52", "ERBS");
    public static final TopologyObject TNODE3_REVERSION = TopologyObjectBuilder.buildNode(1L, TestConstants.N3_REVERSION, 1, ALL_TRUE_FEATURE_STATE, "R13F52", "RadioNode");

    public static final TopologyObject TFREQ3 = TopologyObjectBuilder.buildFrequency(3, TestConstants.F3, 1, "1500", 1500);
    public static final TopologyObject TFREQ4 = TopologyObjectBuilder.buildFrequency(4, TestConstants.F4, 1, "2300", 2300);
    public static final TopologyObject TFREQ5 = TopologyObjectBuilder.buildFrequency(5, TestConstants.F5, 1, "4500", 4500);

    public static final TopologyObject TPROFILE3 = TopologyObjectBuilder.buildProfile(3, P3, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4))),
            Collections.singleton(C3));

    public static final TopologyObject TPROFILE4 = TopologyObjectBuilder.buildProfile(4, P4, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5))),
            Collections.singleton(C4));

    public static final TopologyObject TPROFILE5 = TopologyObjectBuilder.buildProfile(5, P5, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(10f, 90f), Arrays.asList(F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(10f, 90f), Arrays.asList(F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(10f, 90f), Arrays.asList(F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(10f, 90f), Arrays.asList(F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(10f, 90f), Arrays.asList(F4, F5))),
            Collections.singleton(C5));

    public static final TopologyObject RESULTPROFILE2 = TopologyObjectBuilder.buildProfile(3, P3, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(88f, 12f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4))),
            Collections.singleton(C3));
    public static final TopologyObject RESULTPROFILE3 = TopologyObjectBuilder.buildProfile(4, P4, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(20f, 62f, 18f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5))),
            Collections.singleton(C4));

    public static final TopologyObject TCELL3_REVERSION = TopologyObjectBuilder.buildCell(3L, 1, TestConstants.C3, 1500, TestConstants.P1_REVERSION, "1", 5000, "outdoor", "undefined");
    public static final TopologyObject TCELL4_Reversion = TopologyObjectBuilder.buildCell(4L, 1, TestConstants.C4, 1500, TestConstants.P3_Reversion, "2", 5000, "outdoor", "undefined");
    public static final String P2_Reversion = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,310-410-43234-23";
    public static final String P3_Reversion = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00003,310-410-43234-23_1";

    public static final TopologyObject RESULTPROFILE2_REVERSION = TopologyObjectBuilder.buildProfile(-1, P2_Reversion, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F3, F4))),
            Collections.singleton(C3));
    public static final TopologyObject RESULTPROFILE3_REVERSION = TopologyObjectBuilder.buildProfile(-1, P3_Reversion, 1, "1", Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(20f, 60f, 20f), Arrays.asList(F3, F4, F5))),
            Collections.singleton(C4));

    public static final TopologyObject TSECTOR2 = TopologyObjectBuilder.buildSectorWithCells(S2, Arrays.asList(TCELL3, TCELL4, TCELL5));

    // Sector 3 data
    public static final long S3 = 3L;

    public static final String C6 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtranCellFDD=054500_3";
    public static final String C7 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtranCellFDD=054500_3_9";
    public static final String C8 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtranCellFDD=354500_3_2";
    public static final String C9 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtranCellFDD=354500_3_4";
    public static final String C10 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtranCellFDD=354500_3_6";
    public static final String C11 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtranCellFDD=354500_3_7";

    public static final String N5 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY";
    public static final String N5_REVERSION = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=1,ENodeBFunction=1";
    public static final String N6 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY";

    public static final String P6 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=5230";
    public static final String P7 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=2585";
    public static final String P7_REVERSION = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,320-410-43234-23_7";
    public static final String P8 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=2325";
    public static final String P9 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1000";
    public static final String P10 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=67086";
    public static final String P11 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=66911";


    public static final String F6 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1";
    public static final String F7 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2585";
    public static final String F8 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2325";
    public static final String F9 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1000";
    public static final String F10 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=67086";
    public static final String F11 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054500_OCEANSIDE_ABBEY,ManagedElement=054500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=66911";

    public static final String F12 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=5230";
    public static final String F13 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2585";
    public static final String F14 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2325";
    public static final String F15 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=1000";
    public static final String F16 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=67086";
    public static final String F17 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=354500_OCEANSIDE_ABBEY,ManagedElement=354500_OCEANSIDE_ABBEY,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=66911";


    public static final TopologyObject TCELL6 = TopologyObjectBuilder.buildCell(6L, 1, TestConstants.C6, 5230, TestConstants.P6, "320-410-43234-23_6", 10000, "outdoor", "undefined");
    public static final TopologyObject TCELL7 = TopologyObjectBuilder.buildCell(7L, 1, TestConstants.C7, 2585, TestConstants.P7, "320-410-43234-23_7", 5000, "outdoor", "undefined");
    public static final TopologyObject TCELL7_REVERSION = TopologyObjectBuilder.buildCell(7L, 1, TestConstants.C7, 2585, TestConstants.P7, "320-410-43234-23_7", 5000, "outdoor", "undefined");
    public static final TopologyObject TCELL8 = TopologyObjectBuilder.buildCell(8L, 1, TestConstants.C8, 2325, TestConstants.P8, "320-410-43234-23_8", 15000, "outdoor", "undefined");
    public static final TopologyObject TCELL9 = TopologyObjectBuilder.buildCell(9L, 1, TestConstants.C9, 1000, TestConstants.P9, "320-410-43234-23_9", 10000, "outdoor", "undefined");
    public static final TopologyObject TCELL10 = TopologyObjectBuilder.buildCell(10L, 1, TestConstants.C10, 67086, TestConstants.P10, "320-410-43234-23_10", 10000, "outdoor", "undefined");
    public static final TopologyObject TCELL11 = TopologyObjectBuilder.buildCell(11L, 1, TestConstants.C11, 66911, TestConstants.P11, "320-410-43234-23_11", 5000, "outdoor", "undefined");


    public static final TopologyObject TNODE5 = TopologyObjectBuilder.buildNode(5L, TestConstants.N5, 1, ALL_TRUE_FEATURE_STATE, "R13F52", "RadioNode");
    public static final TopologyObject TNODE5_REVERSION = TopologyObjectBuilder.buildNode(5L, TestConstants.N5_REVERSION, 1, ALL_TRUE_FEATURE_STATE, "R13F52", "RadioNode");
    public static final TopologyObject TNODE6 = TopologyObjectBuilder.buildNode(6L, TestConstants.N6, 1, ALL_TRUE_FEATURE_STATE, "R13F52", "ERBS");

    public static final TopologyObject TFREQ6 = TopologyObjectBuilder.buildFrequency(6, TestConstants.F6, 1, "1", 5230);
    public static final TopologyObject TFREQ7 = TopologyObjectBuilder.buildFrequency(7, TestConstants.F7, 1, "2585", 2585);
    public static final TopologyObject TFREQ8 = TopologyObjectBuilder.buildFrequency(8, TestConstants.F8, 1, "2325", 2325);
    public static final TopologyObject TFREQ9 = TopologyObjectBuilder.buildFrequency(9, TestConstants.F9, 1, "1000", 1000);
    public static final TopologyObject TFREQ10 = TopologyObjectBuilder.buildFrequency(10, TestConstants.F10, 1, "67086", 67086);
    public static final TopologyObject TFREQ11 = TopologyObjectBuilder.buildFrequency(11, TestConstants.F11, 1, "66911", 66911);

    public static final TopologyObject TFREQ12 = TopologyObjectBuilder.buildFrequency(12, TestConstants.F12, 1, "5230", 5230);
    public static final TopologyObject TFREQ13 = TopologyObjectBuilder.buildFrequency(13, TestConstants.F13, 1, "2585", 2585);
    public static final TopologyObject TFREQ14 = TopologyObjectBuilder.buildFrequency(14, TestConstants.F14, 1, "2325", 2325);
    public static final TopologyObject TFREQ15 = TopologyObjectBuilder.buildFrequency(15, TestConstants.F15, 1, "1000", 1000);
    public static final TopologyObject TFREQ16 = TopologyObjectBuilder.buildFrequency(16, TestConstants.F16, 1, "67086", 67086);
    public static final TopologyObject TFREQ17 = TopologyObjectBuilder.buildFrequency(17, TestConstants.F17, 1, "66911", 66911);

    public static final TopologyObject TPROFILE6 = TopologyObjectBuilder.buildProfile(6, P6, 1, "5230", Arrays.asList(0, 500, 1000, 1500, 5000),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(1f, 1f, 1f, 94f, 1f, 1f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(1f, 1f, 0f, 84f, 1f, 0f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(1f, 4f, 1f, 74f, 4f, 1f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(1f, 5f, 2f, 69f, 5f, 2f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(18f, 18f, 9f, 27f, 18f, 9f), Arrays.asList(F6, F9, F7, F8, F10,F11))),
            Collections.singleton(C6));
    public static final TopologyObject TPROFILE7 = TopologyObjectBuilder.buildProfile(7, P7, 1, "2585", Arrays.asList(0, 250, 500, 750, 1000),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(1f, 1f, 95f, 1f, 1f, 1f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(4f, 4f, 80f, 6f, 4f, 2f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(6f, 6f, 70f, 9f, 6f, 3f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(10f, 10f, 50f, 15f, 10f, 5f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(18f, 18f, 9f, 27f, 18f, 9f), Arrays.asList(F6, F9, F7, F8, F10,F11))),
            Collections.singleton(C7));
    public static final TopologyObject TPROFILE8 = TopologyObjectBuilder.buildProfile(8, P8, 1, "2325", Arrays.asList(0, 900, 1000, 1500, 3000),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(1f, 1f, 95f, 1f, 1f, 1f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(1f, 0f, 95f, 1f, 1f, 0f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(2f, 1f, 90f, 2f, 2f, 1f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(5f, 2f, 80f, 5f, 5f, 2f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(18f, 9f, 27f, 18f, 18f, 9f), Arrays.asList(F15, F13, F14, F12, F16, F17))),
            Collections.singleton(C8));
    public static final TopologyObject TPROFILE9 = TopologyObjectBuilder.buildProfile(9, P9, 1, "1000", Arrays.asList(0, 900, 1000, 1500, 3000),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(95f, 1f, 1f, 1f, 1f, 1f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(95f, 0f, 1f, 1f, 1f, 0f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 1f, 3f, 2f, 2f, 0f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(80f, 2f, 6f, 4f, 4f, 2f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(18f, 9f, 27f, 18f, 18f, 9f), Arrays.asList(F15, F13, F14, F12, F16, F17))),
            Collections.singleton(C9));
    public static final TopologyObject TPROFILE10 = TopologyObjectBuilder.buildProfile(10, P10, 1, "67086", Arrays.asList(0, 900, 1000, 1500, 3000),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(1f, 1f, 1f, 1f, 95f, 1f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(1f, 0f, 1f, 1f, 95f, 0f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(2f, 1f, 3f, 2f, 90f, 0f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(4f, 2f, 6f, 4f, 80f, 2f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(18f, 9f, 27f, 18f, 18f, 9f), Arrays.asList(F15, F13, F14, F12, F16, F17))),
            Collections.singleton(C10));
    public static final TopologyObject TPROFILE11 = TopologyObjectBuilder.buildProfile(11, P11, 1, "66911", Arrays.asList(0, 250, 500, 750, 1000),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(1f, 1f, 1f, 1f, 1f, 95f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(4f, 2f, 6f, 4f, 4f, 80f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(6f, 3f, 9f, 6f, 6f, 70f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(10f, 5f, 15f, 10f, 10f, 50f), Arrays.asList(F15, F13, F14, F12, F16, F17)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(18f, 9f, 27f, 18f, 18f, 9f), Arrays.asList(F15, F13, F14, F12, F16, F17))),
            Collections.singleton(C11));

    public static final TopologyObject RESULTPROFILE4 = TopologyObjectBuilder.buildProfile(7, P7, 1, "2585", Arrays.asList(0, 250, 500, 750, 1000),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(1f, 1f, 94f, 2f, 1f, 1f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(4f, 4f, 80f, 6f, 4f, 2f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(6f, 6f, 70f, 9f, 6f, 3f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(10f, 10f, 50f, 15f, 10f, 5f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(18f, 18f, 9f, 27f, 18f, 9f), Arrays.asList(F6, F9, F7, F8, F10,F11))),
            Collections.singleton(C7));

    public static final TopologyObject RESULTPROFILE4_REVERSION = TopologyObjectBuilder.buildProfile(-1, P7_REVERSION, 1, "2585", Arrays.asList(0, 250, 500, 750, 1000),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                            Arrays.asList(1f, 1f, 95f, 1f, 1f, 1f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(4f, 4f, 80f, 6f, 4f, 2f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(6f, 6f, 70f, 9f, 6f, 3f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(10f, 10f, 50f, 15f, 10f, 5f), Arrays.asList(F6, F9, F7, F8, F10,F11)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(18f, 18f, 9f, 27f, 18f, 9f), Arrays.asList(F6, F9, F7, F8, F10,F11))),
            Collections.singleton(C7));

    public static final TopologyObject TSECTOR3 = TopologyObjectBuilder.buildSectorWithCells(S3, Arrays.asList(TCELL6, TCELL7, TCELL8, TCELL9, TCELL10, TCELL11));
}

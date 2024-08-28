/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021 - 2023
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.EUtranFrequency;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node.FeatureState;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.TestDataBuilder;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Unit tests for {@link PolicyOutputEventEnricher} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PolicyOutputEventEnricherTest {

    private static final String OUTDOOR = "outdoor";

    private static final Cell S =
            new Cell(1L, 1, "sourceCellFdn", 1200, "idleModePrioAtReleaseRef1", "cgi1", 10000, OUTDOOR, "undefined");
    private static final Cell T1 =
            new Cell(2L, 1, "targetCellFdn1", 1300, "idleModePrioAtReleaseRef2", "cgi2", 20000, OUTDOOR, "undefined");
    private static final Cell T2 =
            new Cell(3L, 1, "targetCellFdn2", 1200, "idleModePrioAtReleaseRef3", "cgi3", 10000, OUTDOOR, "undefined");
    private static final Cell T3 =
            new Cell(4L, 1, "targetCellFdn3", 1300, "idleModePrioAtReleaseRef4", "cgi4", 10000, OUTDOOR, "undefined");
    private static final Cell SECTOR2_S =
            new Cell(11L, 1, "sourceCellFdn", 1200, "sector2_idleModePrioAtReleaseRef1", "cgi1", 10000, OUTDOOR, "undefined");
    private static final Cell SECTOR2_T1 =
    	    new Cell(12L, 1, "targetCellFdn1", 1300, "sector2_idleModePrioAtReleaseRef2", "cgi2", 20000, OUTDOOR, "undefined");
    private static final Cell SECTOR2_T4 =
            new Cell(15L, 1, "targetCellFdn5", 1400, "sector2_idleModePrioAtReleaseRef3", "cgi3", 10000, OUTDOOR, "undefined");
    private static final Cell SECTOR2_T5 =
            new Cell(16L, 1, "targetCellFdn6", 1500, "sector2_idleModePrioAtReleaseRef4", "cgi4", 10000, OUTDOOR, "undefined");

    private static final FeatureState ALL_TRUE = new FeatureState(true, true, true, true, "ACTIVATED", true);
    private static final Node N1 = new Node(1L, "nodeFdn1", 1, ALL_TRUE, "radionode");
    private static final Node N2 = new Node(2L, "nodeFdn2", 2, ALL_TRUE, "radionode");
    private static final Node N3 = new Node(2L, "nodeFdn3", 3, ALL_TRUE, "radionode");

    private static final EUtranFrequency F1_1200 = new EUtranFrequency(1L, "freqFDN1", 1, "F1", 1200);
    private static final EUtranFrequency F2_1300 = new EUtranFrequency(2L, "freqFDN2", 1, "F2", 1300);
    private static final EUtranFrequency F3_1200 = new EUtranFrequency(3L, "freqFDN3", 1, "F3", 1200);
    private static final EUtranFrequency F4_1300 = new EUtranFrequency(4L, "freqFDN4", 1, "F4", 1300);
    private static final EUtranFrequency F5_1400 = new EUtranFrequency(5L, "freqFDN5", 1, "F5", 1400);
    private static final EUtranFrequency F6_1500 = new EUtranFrequency(6L, "freqFDN6", 1, "F6", 1500);
    private static final EUtranFrequency F7_1300 = new EUtranFrequency(7L, "freqFDN7", 1, "F7", 1300);
    private static final EUtranFrequency F8_1300 = new EUtranFrequency(8L, "freqFDN8", 1, "F8", 1300);
    private static final EUtranFrequency F9_1600 = new EUtranFrequency(9L, "freqFDN9", 1, "F9", 1600);
    private static final EUtranFrequency F10_1600 = new EUtranFrequency(10L, "freqFDN10", 1, "F10", 1600);
    private static final EUtranFrequency F11_1500 = new EUtranFrequency(11L, "freqFDN11", 1, "F11", 1500);

    private static final IdleModePrioAtRelease P1 = TestDataBuilder.getTestIdleModePrioAtRelease(1, "idleModePrioAtReleaseRef1", 1,
            Arrays.asList(F1_1200.getFdn(), F2_1300.getFdn(), F3_1200.getFdn(), F4_1300.getFdn()), Collections.emptySet());
    private static final IdleModePrioAtRelease P2 = TestDataBuilder.getTestIdleModePrioAtRelease(2, "idleModePrioAtReleaseRef2", 1,
            Arrays.asList(F1_1200.getFdn(), F2_1300.getFdn(), F3_1200.getFdn(), F4_1300.getFdn()), Collections.emptySet());
    private static final IdleModePrioAtRelease P3 = TestDataBuilder.getTestIdleModePrioAtRelease(3, "idleModePrioAtReleaseRef3", 1,
            Arrays.asList(F1_1200.getFdn(), F2_1300.getFdn(), F3_1200.getFdn(), F4_1300.getFdn()), Collections.emptySet());
    private static final IdleModePrioAtRelease P4 = TestDataBuilder.getTestIdleModePrioAtRelease(4, "idleModePrioAtReleaseRef4", 1,
            Arrays.asList(F1_1200.getFdn(), F2_1300.getFdn(), F3_1200.getFdn(), F4_1300.getFdn()), Collections.emptySet());

    private static final IdleModePrioAtRelease SECTOR2_P1 = TestDataBuilder.getTestIdleModePrioAtRelease(1, "sector2_idleModePrioAtReleaseRef1", 1,
            Arrays.asList(F1_1200.getFdn(), F2_1300.getFdn(), F5_1400.getFdn(), F6_1500.getFdn()), Collections.emptySet());
    private static final IdleModePrioAtRelease SECTOR2_P2 = TestDataBuilder.getTestIdleModePrioAtRelease(2, "sector2_idleModePrioAtReleaseRef2", 1,
            Arrays.asList(F1_1200.getFdn(), F2_1300.getFdn(), F5_1400.getFdn(), F6_1500.getFdn()), Collections.emptySet());
    private static final IdleModePrioAtRelease SECTOR2_P5 = TestDataBuilder.getTestIdleModePrioAtRelease(4, "sector2_idleModePrioAtReleaseRef3", 1,
            Arrays.asList(F1_1200.getFdn(), F2_1300.getFdn(), F5_1400.getFdn(), F6_1500.getFdn()), Collections.emptySet());
    private static final IdleModePrioAtRelease SECTOR2_P6 = TestDataBuilder.getTestIdleModePrioAtRelease(4, "sector2_idleModePrioAtReleaseRef4", 1,
            Arrays.asList(F1_1200.getFdn(), F2_1300.getFdn(), F5_1400.getFdn(), F6_1500.getFdn()), Collections.emptySet());

    private static final CellKpis K1 = new CellKpis(1000.0, 0.5, 4, 6, 0, 0, 0);
    private static final CellKpis K2 = new CellKpis(1100.0, 0.6, 4, 6, 8, 0, 0);
    private static final CellKpis K3 = new CellKpis(1200.0, 0.7, 2, 4, 6, 8, 0);
    private static final CellKpis K4 = new CellKpis(1300.0, 0.8, 0, 0, 6, 8, 0);
    private static final PolicyOutputEvent TEST_POE = new PolicyOutputEvent("name", "version", "namespace", "source", "target", 1L, "flm_1",
            getTestLoadBalancingQuanta(), Collections.emptyList());
    private static final TopologySector SECTOR = new TopologySector(1L, Arrays.asList(S, T1, T2, T3));
    private static final TopologySector SECTOR2 = new TopologySector(2L, Arrays.asList(SECTOR2_S, SECTOR2_T1, SECTOR2_T4, SECTOR2_T5));

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private CmSectorCellStore mockCellStore;

    @Mock
    private CmNodeObjectsStore mockNodeObjectsStore;

    @Mock
    private CellKpiStore mockKpiStore;

    private PolicyOutputEventEnricher objectUnderTest;

    @Before
    public void setUp() {
        objectUnderTest = new PolicyOutputEventEnricher(mockNodeObjectsStore, mockCellStore, mockKpiStore);
    }

    @Test
    public void whenEverythingFoundInStores_thenEverythingIsCollectedWell() {
        final Map<String, Integer> frequencyToCarrier = getFrequencyToCarrier(Arrays.asList(F1_1200, F2_1300, F3_1200, F4_1300));
        mockGetSector();
        mockNodeForCellFdn(N1, N2);
        mockGetProfiles(P1, P2, P3, P4);
        mockGetFreqOnNode();
        mockGetKPIs(K1, K2, K3, K4);

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        softly.assertThat(result.getPolicyOutputEvent()).isEqualTo(TEST_POE);

        softly.assertThat(result.getCellCmData()).hasSize(4);
        softly.assertThat(result.getCell(createTopologyObjectIdFromCell(S))).isEqualTo(S);
        softly.assertThat(result.getCell(createTopologyObjectIdFromCell(T1))).isEqualTo(T1);
        softly.assertThat(result.getCell(createTopologyObjectIdFromCell(T2))).isEqualTo(T2);
        softly.assertThat(result.getCell(createTopologyObjectIdFromCell(T3))).isEqualTo(T3);
        softly.assertThat(result.getProfiles()).containsOnly(
                entry(TopologyObjectId.of(P1.getFdn(), P1.getOssId()), new EnrichedIdleModePrioAtRelease(P1, frequencyToCarrier)),
                entry(TopologyObjectId.of(P2.getFdn(), P2.getOssId()), new EnrichedIdleModePrioAtRelease(P2, frequencyToCarrier)),
                entry(TopologyObjectId.of(P3.getFdn(), P3.getOssId()), new EnrichedIdleModePrioAtRelease(P3, frequencyToCarrier)),
                entry(TopologyObjectId.of(P4.getFdn(), P4.getOssId()), new EnrichedIdleModePrioAtRelease(P4, frequencyToCarrier))
        );

        softly.assertThat(result.getFrequencyToCarrier()).containsOnly(
                entry(F1_1200.getTopologyObjectId(), F1_1200.getArfcnValueEUtranDl()),
                entry(F2_1300.getTopologyObjectId(), F2_1300.getArfcnValueEUtranDl()),
                entry(F3_1200.getTopologyObjectId(), F3_1200.getArfcnValueEUtranDl()),
                entry(F4_1300.getTopologyObjectId(), F4_1300.getArfcnValueEUtranDl())
        );

        softly.assertThat(result.getCellKpis()).containsOnly(
                entry(createTopologyObjectIdFromCell(S), K1),
                entry(createTopologyObjectIdFromCell(T1), K2),
                entry(createTopologyObjectIdFromCell(T2), K3),
                entry(createTopologyObjectIdFromCell(T3), K4)
        );

        softly.assertThat(result.getDuplicateCarrierCellToFrequency()).containsOnly(
                entry(createTopologyObjectIdFromCell(S), F1_1200.getTopologyObjectId()),
                entry(createTopologyObjectIdFromCell(T1), F2_1300.getTopologyObjectId()),
                entry(createTopologyObjectIdFromCell(T2), F3_1200.getTopologyObjectId()),
                entry(createTopologyObjectIdFromCell(T3), F4_1300.getTopologyObjectId())
        );
    }

    @Test
    public void whenDuplicateCarrierExistsInTwoNodeFrequencies_andTheCarrierIsInTheSectorCells_thenDuplicatesAreFound() {
        when(mockCellStore.getFullSector(1L)).thenReturn(SECTOR2);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_S.getFdn(), SECTOR2_S.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T1.getFdn(), SECTOR2_T1.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T4.getFdn(), SECTOR2_T4.getOssId())).thenReturn(N2);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T5.getFdn(), SECTOR2_T5.getOssId())).thenReturn(N2);
        mockGetProfiles(SECTOR2_P1, SECTOR2_P2, SECTOR2_P5, SECTOR2_P6);
        when(mockNodeObjectsStore.getEutranFrequencies(N1.getFdn(), N1.getOssId())).thenReturn(Arrays.asList(F1_1200, F2_1300));
        when(mockNodeObjectsStore.getEutranFrequencies(N2.getFdn(), N2.getOssId())).thenReturn(Arrays.asList(F5_1400, F6_1500, F7_1300));

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        softly.assertThat(result.getDuplicateCarrierCellToFrequency()).containsOnly(
                entry(createTopologyObjectIdFromCell(SECTOR2_T1), F2_1300.getTopologyObjectId())
        );
    }

    @Test
    public void whenMultipleDuplicateCarriersExistsInTwoNodeFrequencies_andTheCarrierIsInTheSectorCells_thenDuplicatesAreFound() {
        when(mockCellStore.getFullSector(1L)).thenReturn(SECTOR2);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_S.getFdn(), SECTOR2_S.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T1.getFdn(), SECTOR2_T1.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T4.getFdn(), SECTOR2_T4.getOssId())).thenReturn(N2);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T5.getFdn(), SECTOR2_T5.getOssId())).thenReturn(N2);
        mockGetProfiles(SECTOR2_P1, SECTOR2_P2, SECTOR2_P5, SECTOR2_P6);
        when(mockNodeObjectsStore.getEutranFrequencies(N1.getFdn(), N1.getOssId())).thenReturn(Arrays.asList(F1_1200, F2_1300, F5_1400, F11_1500));
        when(mockNodeObjectsStore.getEutranFrequencies(N2.getFdn(), N2.getOssId())).thenReturn(Arrays.asList(F6_1500, F7_1300));

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        softly.assertThat(result.getDuplicateCarrierCellToFrequency()).containsOnly(
                entry(createTopologyObjectIdFromCell(SECTOR2_T1), F2_1300.getTopologyObjectId()),
                entry(createTopologyObjectIdFromCell(SECTOR2_T5), F6_1500.getTopologyObjectId())
        );
    }

    @Test
    public void whenDuplicateCarrierExistsInMultipleNodesFrequencies_andTheCarrierIsInTheSectorCells_thenDuplicatesAreFound() {
        when(mockCellStore.getFullSector(1L)).thenReturn(SECTOR2);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_S.getFdn(), SECTOR2_S.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T1.getFdn(), SECTOR2_T1.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T4.getFdn(), SECTOR2_T4.getOssId())).thenReturn(N2);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T5.getFdn(), SECTOR2_T5.getOssId())).thenReturn(N3);
        mockGetProfiles(SECTOR2_P1, SECTOR2_P2, SECTOR2_P5, SECTOR2_P6);
        when(mockNodeObjectsStore.getEutranFrequencies(N1.getFdn(), N1.getOssId())).thenReturn(Arrays.asList(F1_1200, F2_1300));
        when(mockNodeObjectsStore.getEutranFrequencies(N2.getFdn(), N2.getOssId())).thenReturn(Arrays.asList(F5_1400, F7_1300));
        when(mockNodeObjectsStore.getEutranFrequencies(N3.getFdn(), N3.getOssId())).thenReturn(Arrays.asList(F6_1500, F8_1300));

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        softly.assertThat(result.getDuplicateCarrierCellToFrequency()).containsOnly(
                entry(createTopologyObjectIdFromCell(SECTOR2_T1), F2_1300.getTopologyObjectId())
        );
    }

    @Test
    public void whenDuplicateCarrierExistsInNodesFrequencies_andTheCarrierIsNotInTheSectorCells_thenDuplicatesAreNotFound() {
        when(mockCellStore.getFullSector(1L)).thenReturn(SECTOR2);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_S.getFdn(), SECTOR2_S.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T1.getFdn(), SECTOR2_T1.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T4.getFdn(), SECTOR2_T4.getOssId())).thenReturn(N2);
        when(mockNodeObjectsStore.getNodeForCellFdn(SECTOR2_T5.getFdn(), SECTOR2_T5.getOssId())).thenReturn(N2);
        mockGetProfiles(SECTOR2_P1, SECTOR2_P2, SECTOR2_P5, SECTOR2_P6);
        when(mockNodeObjectsStore.getEutranFrequencies(N1.getFdn(), N1.getOssId())).thenReturn(Arrays.asList(F1_1200, F2_1300));
        when(mockNodeObjectsStore.getEutranFrequencies(N2.getFdn(), N2.getOssId())).thenReturn(Arrays.asList(F5_1400, F6_1500, F9_1600, F10_1600));

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        softly.assertThat(result.getDuplicateCarrierCellToFrequency()).isEmpty();
    }

    @Test
    public void whenNothingFoundInStores_thenNothingIsCollected() {
        mockEmptySector();
        mockGetKPIs(null, null, null, null);

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        softly.assertThat(result.getCellCmData()).isEmpty();
        softly.assertThat(result.getProfiles()).isEmpty();
        softly.assertThat(result.getFrequencyToCarrier()).isEmpty();
        softly.assertThat(result.getCellKpis()).isEmpty();
        softly.assertThat(result.getDuplicateCarrierCellToFrequency()).isEmpty();
    }

    @Test
    public void whenProfilesNotFoundInStores_thenProfilesAreNotCollected() {
        mockGetSector();

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        assertThat(result.getProfiles()).isEmpty();
    }

    @Test
    public void whenDuplicateProfilesNotFoundInStores_thenDistrinctProfilesAreCollected() {
        final Map<String, Integer> frequencyToCarrier = getFrequencyToCarrier(Arrays.asList(F1_1200, F2_1300, F3_1200, F4_1300));
        mockGetSector();
        mockNodeForCellFdn(N1, N2);
        mockGetFreqOnNode();
        mockGetKPIs(K1, K2, K3, K4);
        mockGetProfiles(P1, P2, P3, P1);

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        softly.assertThat(result.getProfiles()).containsOnly(
                entry(TopologyObjectId.of(P1.getFdn(), P1.getOssId()), new EnrichedIdleModePrioAtRelease(P1, frequencyToCarrier)),
                entry(TopologyObjectId.of(P2.getFdn(), P2.getOssId()), new EnrichedIdleModePrioAtRelease(P2, frequencyToCarrier)),
                entry(TopologyObjectId.of(P3.getFdn(), P3.getOssId()), new EnrichedIdleModePrioAtRelease(P3, frequencyToCarrier))
        );
    }

    @Test
    public void whenCellsNotFoundOnNodeInStores_thenFrequenciesAreNotCollected() {
        mockGetSector();
        mockNodeForCellFdn(null, null);

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        assertThat(result.getFrequencyToCarrier()).isEmpty();
    }

    @Test
    public void whenCarrierNotFoundOnNodeInStores_thenFrequenciesAreNotCollected() {
        mockGetSector();
        mockNodeForCellFdn(N1, N2);
        mockGetFreqOnNodeReturnsEmptyList();

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        softly.assertThat(result.getPolicyOutputEvent()).isEqualTo(TEST_POE);
        softly.assertThat(result.getFrequencyToCarrier()).isEmpty();
    }

    @Test
    public void whenNodeNotFoundForSomeCells_thenDuplicateCarrierCellToFrequencyIsStillPopulatedWithTheRemainingCellFreqInfo() {
        mockGetSector();
        when(mockNodeObjectsStore.getNodeForCellFdn(S.getFdn(), S.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(T1.getFdn(), T1.getOssId())).thenReturn(N1);
        when(mockNodeObjectsStore.getNodeForCellFdn(T2.getFdn(), T2.getOssId())).thenReturn(N2);
        mockGetProfiles(P1, P2, P3, P4);
        mockGetFreqOnNode();
        mockGetKPIs(K1, K2, K3, K4);

        final EnrichedPolicyOutputEvent result = objectUnderTest.enrich(TEST_POE);

        softly.assertThat(result.getDuplicateCarrierCellToFrequency()).containsOnly(
                entry(createTopologyObjectIdFromCell(S), F1_1200.getTopologyObjectId()),
                entry(createTopologyObjectIdFromCell(T1), F2_1300.getTopologyObjectId()),
                entry(createTopologyObjectIdFromCell(T2), F3_1200.getTopologyObjectId())
        );
    }

    private Map<String, Integer> getFrequencyToCarrier(final List<EUtranFrequency> frequencies) {
        return frequencies.stream()
                .collect(Collectors.toMap(EUtranFrequency::getFdn, EUtranFrequency::getArfcnValueEUtranDl));
    }

    private static ProposedLoadBalancingQuanta getTestLoadBalancingQuanta() {
        final List<TargetCell> targetCells = new ArrayList<>();
        targetCells.add(new TargetCell(T1.getFdn(), T1.getOssId(), "200"));
        targetCells.add(new TargetCell(T2.getFdn(), T2.getOssId(), "300"));
        targetCells.add(new TargetCell(T3.getFdn(), T3.getOssId(), "400"));

        return new ProposedLoadBalancingQuanta(S.getFdn(), S.getOssId(), "900", targetCells);
    }

    private void mockGetKPIs(final CellKpis k1, final CellKpis k2, final CellKpis k3, final CellKpis k4) {
        when(mockKpiStore.getKpisForCell(S.getFdn(), S.getCellId())).thenReturn(k1);
        when(mockKpiStore.getKpisForCell(T1.getFdn(), T1.getOssId())).thenReturn(k2);
        when(mockKpiStore.getKpisForCell(T2.getFdn(), T2.getOssId())).thenReturn(k3);
        when(mockKpiStore.getKpisForCell(T3.getFdn(), T3.getOssId())).thenReturn(k4);
    }

    private void mockGetFreqOnNode() {
        when(mockNodeObjectsStore.getEutranFrequencies(N1.getFdn(), N1.getOssId())).thenReturn(Arrays.asList(F1_1200, F2_1300));
        when(mockNodeObjectsStore.getEutranFrequencies(N2.getFdn(), N2.getOssId())).thenReturn(Arrays.asList(F3_1200, F4_1300));
    }

    private void mockGetFreqOnNodeReturnsEmptyList() {
        when(mockNodeObjectsStore.getEutranFrequencies(N1.getFdn(), N1.getOssId())).thenReturn(Collections.emptyList());
        when(mockNodeObjectsStore.getEutranFrequencies(N2.getFdn(), N2.getOssId())).thenReturn(Collections.emptyList());
    }

    private void mockGetProfiles(final IdleModePrioAtRelease p1, final IdleModePrioAtRelease p2, final IdleModePrioAtRelease p3,
            final IdleModePrioAtRelease p4) {
        when(mockNodeObjectsStore.getIdleModePrioAtRelease(p1.getFdn(), p1.getOssId())).thenReturn(p1);
        when(mockNodeObjectsStore.getIdleModePrioAtRelease(p2.getFdn(), p2.getOssId())).thenReturn(p2);
        when(mockNodeObjectsStore.getIdleModePrioAtRelease(p3.getFdn(), p3.getOssId())).thenReturn(p3);
        when(mockNodeObjectsStore.getIdleModePrioAtRelease(p4.getFdn(), p4.getOssId())).thenReturn(p4);
    }

    private void mockNodeForCellFdn(final Node n1, final Node n2) {
        when(mockNodeObjectsStore.getNodeForCellFdn(S.getFdn(), S.getOssId())).thenReturn(n1);
        when(mockNodeObjectsStore.getNodeForCellFdn(T1.getFdn(), T1.getOssId())).thenReturn(n1);
        when(mockNodeObjectsStore.getNodeForCellFdn(T2.getFdn(), T2.getOssId())).thenReturn(n2);
        when(mockNodeObjectsStore.getNodeForCellFdn(T3.getFdn(), T3.getOssId())).thenReturn(n2);
    }

    private void mockGetSector() {
        when(mockCellStore.getFullSector(1L)).thenReturn(SECTOR);
    }

    private void mockEmptySector() {
        when(mockCellStore.getFullSector(1L)).thenReturn(null);
    }

    private static TopologyObjectId createTopologyObjectIdFromCell(final Cell cell) {
        return new TopologyObjectId(cell.getFdn(), cell.getOssId());
    }
}
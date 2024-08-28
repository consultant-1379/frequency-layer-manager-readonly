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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.NodeWithCells;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.NodeWithEutranFrequencies;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.NodeWithIdleModePrioAtReleases;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmNodeCellRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmNodeFrequencyRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmNodeProfileRetriever;

/**
 * Unit tests for {@link CmNodeObjectsStore} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CmNodeObjectsStoreTest {
    public static final int OSSID = 1;
    public static final String RANDOM_FDN = "randomFdn";
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    CmNodeCellRetriever cmNodeCellRetrieverMock;

    @Mock
    CmNodeProfileRetriever cmNodeProfileRetrieverMock;

    @Mock
    CmNodeFrequencyRetriever cmNodeFrequencyRetriever;

    @Mock
    CmSectorCellStore cmSectorCellStoreMock;

    private CmNodeObjectsStore objectUnderTest;

    @Test
    public void whenEverythingIsEmpty_thenStoreShouldBeEmpty() {
        when(cmNodeCellRetrieverMock.retrieve()).thenReturn(Collections.emptyList());
        when(cmNodeProfileRetrieverMock.retrieve()).thenReturn(Collections.emptyIterator());
        when(cmNodeFrequencyRetriever.retrieve()).thenReturn(Collections.emptyList());

        objectUnderTest = new CmNodeObjectsStore(cmSectorCellStoreMock, Collections.emptyList(),
                Collections.emptyList(), cmNodeCellRetrieverMock,
                cmNodeProfileRetrieverMock, cmNodeFrequencyRetriever);

        assertThat(objectUnderTest.getNumberOfCells()).isZero();
        assertThat(objectUnderTest.getNumberOfUsedProfilesEntries()).isZero();
        assertThat(objectUnderTest.getNumberOfProfiles()).isZero();
        assertThat(objectUnderTest.getNumberOfNodesToFrequencies()).isZero();
        assertThat(objectUnderTest.getIdleModePrioAtRelease(RANDOM_FDN, 1)).isNull();
        assertThat(objectUnderTest.getNodeForCellFdn(RANDOM_FDN, OSSID)).isNull();
        assertThat(objectUnderTest.getNumberOfProfilesUsedByNode(RANDOM_FDN, OSSID)).isNull();
        assertThat(objectUnderTest.getEutranFrequencyOnNode(RANDOM_FDN, 1, 1500)).isNull();
    }

    @Test
    public void whenRetrieversAndCellStoreFilled_thenCmNodeProfileStoreWorksFine() {
        when(cmNodeCellRetrieverMock.retrieve()).thenReturn(getTestNodeToCells());
        when(cmNodeProfileRetrieverMock.retrieve()).thenReturn(getTestNodeWithProfiles().iterator());
        when(cmNodeFrequencyRetriever.retrieve()).thenReturn(getTestNodeFrequencies());
        when(cmSectorCellStoreMock.getFullSectors()).thenReturn(Arrays.asList(TestConstants.TSECTOR1, TestConstants.TSECTOR2, TestConstants.TSECTOR3));

        objectUnderTest = new CmNodeObjectsStore(cmSectorCellStoreMock, Arrays.asList(
                TopologyObjectId.of(TestConstants.C1, 1),
                TopologyObjectId.of(TestConstants.C3, 1),
                TopologyObjectId.of(TestConstants.C4, 1),
                TopologyObjectId.of(TestConstants.C5, 1)),
                Arrays.asList(1L, 2L),
                cmNodeCellRetrieverMock, cmNodeProfileRetrieverMock, cmNodeFrequencyRetriever);

        assertThat(objectUnderTest.getNumberOfCells()).isEqualTo(7);
        assertThat(objectUnderTest.getNumberOfUsedProfilesEntries()).isEqualTo(2);
        assertThat(objectUnderTest.getNumberOfProfiles()).isEqualTo(4);
        assertThat(objectUnderTest.getNumberOfNodesToFrequencies()).isEqualTo(2);

        softly.assertThat(objectUnderTest.getNumberOfProfilesUsedByNode(TestConstants.N1, OSSID)).isEqualTo(3);
        softly.assertThat(objectUnderTest.getNumberOfProfilesUsedByNode(TestConstants.N2, OSSID).intValue()).isEqualTo(5);
        softly.assertThat(objectUnderTest.getNumberOfProfilesUsedByNode(RANDOM_FDN, OSSID)).isNull();
        softly.assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P1, 1)).isEqualTo(TestConstants.PROFILE1);
        softly.assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P2, 1)).isNull();
        softly.assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P3, 1)).isEqualTo(TestConstants.PROFILE3);
        softly.assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P4, 1)).isEqualTo(TestConstants.PROFILE4);
        softly.assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P5, 1)).isEqualTo(TestConstants.PROFILE5);
        softly.assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P6, 1)).isNull();
        softly.assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P7, 1)).isNull();
        softly.assertThat(objectUnderTest.getNodeForCellFdn(TestConstants.C1, OSSID)).isEqualTo(TestConstants.NODE1);
        softly.assertThat(objectUnderTest.getNodeForCellFdn(TestConstants.C2, OSSID)).isEqualTo(TestConstants.NODE1);
        softly.assertThat(objectUnderTest.getNodeForCellFdn(TestConstants.C3, OSSID)).isEqualTo(TestConstants.NODE1);
        softly.assertThat(objectUnderTest.getNodeForCellFdn(TestConstants.C4, OSSID)).isEqualTo(TestConstants.NODE2);
        softly.assertThat(objectUnderTest.getNodeForCellFdn(TestConstants.C5, OSSID)).isEqualTo(TestConstants.NODE2);
        softly.assertThat(objectUnderTest.getNodeForCellFdn(TestConstants.C6, OSSID)).isEqualTo(TestConstants.NODE2);
        softly.assertThat(objectUnderTest.getNodeForCellFdn(TestConstants.C7, OSSID)).isNull();
        softly.assertThat(objectUnderTest.getNodeForCellFdn(TestConstants.C8, OSSID)).isEqualTo(TestConstants.NODE2);
        softly.assertThat(objectUnderTest.getNodeForCellFdn(TestConstants.C9, OSSID)).isNull();
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N1, 1, 1500)).isEqualTo(TestConstants.FREQ1);
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N1, 1, 2300)).isEqualTo(TestConstants.FREQ2);
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N1, 1, 3200)).isEqualTo(TestConstants.FREQ3);
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N2, 1, 1500)).isEqualTo(TestConstants.FREQ4);
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N2, 1, 2300)).isEqualTo(TestConstants.FREQ5);
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N2, 1, 3200)).isEqualTo(TestConstants.FREQ6);
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N2, 1, 5400)).isEqualTo(TestConstants.FREQ7);
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N3, 1, 5400)).isNull();
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N1, 2, 1500)).isNull();
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(RANDOM_FDN, 1, 1500)).isNull();
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N1, 1, 2323)).isNull();
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(TestConstants.N2, 1, 2323)).isNull();
        softly.assertThat(objectUnderTest.getEutranFrequencyOnNode(RANDOM_FDN, 1, 2323)).isNull();
    }

    @Test
    public void whenProfileNotFoundInStore_thenProfileIsRetrievedSeparatelyAndCached() {
        when(cmNodeCellRetrieverMock.retrieve()).thenReturn(getTestNodeToCells());
        when(cmNodeProfileRetrieverMock.retrieve()).thenReturn(getTestNodeWithProfiles().iterator());
        when(cmNodeProfileRetrieverMock.retrieve(TestConstants.P2, 1)).thenReturn(TestConstants.PROFILE2);
        when(cmNodeFrequencyRetriever.retrieve()).thenReturn(getTestNodeFrequencies());
        when(cmSectorCellStoreMock.getFullSectors()).thenReturn(Arrays.asList(TestConstants.TSECTOR1, TestConstants.TSECTOR2));

        objectUnderTest = new CmNodeObjectsStore(cmSectorCellStoreMock, Collections.singletonList(TopologyObjectId.of(TestConstants.C1, 1)),
                Collections.singletonList(1L),
                cmNodeCellRetrieverMock, cmNodeProfileRetrieverMock, cmNodeFrequencyRetriever);

        assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P1, 1)).isEqualTo(TestConstants.PROFILE1);
        assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P2, 1)).isEqualTo(TestConstants.PROFILE2);
        assertThat(objectUnderTest.getNumberOfProfiles()).isEqualTo(2);
    }

    @Test
    public void whenProfileNotFoundInStoreAndCannotFindSeparately_thenNoProfileIsReturned() {
        when(cmNodeCellRetrieverMock.retrieve()).thenReturn(getTestNodeToCells());
        when(cmNodeProfileRetrieverMock.retrieve()).thenReturn(getTestNodeWithProfiles().iterator());
        when(cmNodeProfileRetrieverMock.retrieve(TestConstants.P2, 1)).thenReturn(null);
        when(cmNodeFrequencyRetriever.retrieve()).thenReturn(getTestNodeFrequencies());
        when(cmSectorCellStoreMock.getFullSectors()).thenReturn(Arrays.asList(TestConstants.TSECTOR1, TestConstants.TSECTOR2));

        objectUnderTest = new CmNodeObjectsStore(cmSectorCellStoreMock, Collections.singletonList(TopologyObjectId.of(TestConstants.C1, 1)),
                Collections.singletonList(1L),
                cmNodeCellRetrieverMock, cmNodeProfileRetrieverMock, cmNodeFrequencyRetriever);

        assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P1, 1)).isEqualTo(TestConstants.PROFILE1);
        assertThat(objectUnderTest.getIdleModePrioAtRelease(TestConstants.P2, 1)).isNull();
    }

    @Test
    public void whenGetIdleModePrioAtRelease_thenDegroupingAndDeduplicationIsHandledCorrectly() {
        when(cmNodeProfileRetrieverMock.retrieve()).thenReturn(getTestNodeWithProfiles().iterator());
        when(cmSectorCellStoreMock.getFullSectors()).thenReturn(Arrays.asList(TestConstants.TSECTOR1, TestConstants.TSECTOR2));

        objectUnderTest = new CmNodeObjectsStore(cmSectorCellStoreMock, Arrays.asList(
                TopologyObjectId.of(TestConstants.C8, 1)),
                Collections.singletonList(2L),
                cmNodeCellRetrieverMock, cmNodeProfileRetrieverMock, cmNodeFrequencyRetriever);

        final IdleModePrioAtRelease idleModePrioAtReleaseToCheck = objectUnderTest.getIdleModePrioAtRelease(TestConstants.P8, 1);
        softly.assertThat(idleModePrioAtReleaseToCheck).isEqualTo(TestConstants.PROFILE8);
        softly.assertThat(idleModePrioAtReleaseToCheck.equals(TestConstants.PROFILE8_NORMALIZED)).isTrue();
    }

    private List<NodeWithEutranFrequencies> getTestNodeFrequencies() {
        return Arrays.asList(
                new NodeWithEutranFrequencies(TestConstants.NODE1, Arrays.asList(TestConstants.FREQ1, TestConstants.FREQ2, TestConstants.FREQ3)),
                new NodeWithEutranFrequencies(TestConstants.NODE2, Arrays.asList(TestConstants.FREQ4, TestConstants.FREQ5, TestConstants.FREQ6, TestConstants.FREQ7, TestConstants.FREQ8)));
    }

    private List<NodeWithCells> getTestNodeToCells() {
        return Arrays.asList(
                new NodeWithCells(TestConstants.NODE1, Arrays.asList(TestConstants.CELL1, TestConstants.CELL2, TestConstants.CELL3)),
                new NodeWithCells(TestConstants.NODE2, Arrays.asList(TestConstants.CELL4, TestConstants.CELL5,
                                  TestConstants.CELL6, TestConstants.CELL7, TestConstants.CELL8)),
                new NodeWithCells(TestConstants.NODE3, Collections.singleton(TestConstants.CELL9)));
    }

    private Collection<Collection<NodeWithIdleModePrioAtReleases>> getTestNodeWithProfiles() {
        final Collection<Collection<NodeWithIdleModePrioAtReleases>> nodesWithProfiles = new ArrayList<>();
        nodesWithProfiles.add(Collections.singletonList(new NodeWithIdleModePrioAtReleases(
                TestConstants.NODE1, Arrays.asList(TestConstants.PROFILE1, TestConstants.PROFILE2, TestConstants.PROFILE3))));
        nodesWithProfiles.add(Collections.singletonList(new NodeWithIdleModePrioAtReleases(
                TestConstants.NODE2, Arrays.asList(TestConstants.PROFILE4, TestConstants.PROFILE5, TestConstants.PROFILE6,
                                                   TestConstants.PROFILE7, TestConstants.PROFILE8))));
        nodesWithProfiles.add(Collections.singletonList(new NodeWithIdleModePrioAtReleases(
                TestConstants.NODE3, Collections.singletonList(TestConstants.PROFILE9))));
        return nodesWithProfiles;
    }
}

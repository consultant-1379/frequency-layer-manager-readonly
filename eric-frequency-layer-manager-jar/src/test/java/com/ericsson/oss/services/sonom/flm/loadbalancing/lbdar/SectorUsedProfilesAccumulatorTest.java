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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node.FeatureState;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;

/**
 * Unit tests for {@link SectorUsedProfilesAccumulator} class.
 */
public class SectorUsedProfilesAccumulatorTest {
    private static final FeatureState ALL_TRUE = new FeatureState(true, true, true, true, "ACTIVATED", true);
    private static final TopologyObjectId N1 = TopologyObjectId.of("nodeFdn1", 1);
    private static final TopologyObjectId N2 = TopologyObjectId.of("nodeFdn2", 1);
    private static final TopologyObjectId N3 = TopologyObjectId.of("nodeFdn3", 1);
    private static final TopologyObjectId N4 = TopologyObjectId.of("nodeFdn4", 1);
    private static final TopologyObjectId N5 = TopologyObjectId.of("nodeFdn5", 1);
    private static final TopologyObjectId N6 = TopologyObjectId.of("nodeFdn6", 1);
    private static final TopologyObjectId N7 = TopologyObjectId.of("nodeFdn7", 1);
    private static final Node NODE1 = new Node(1L, N1.getFdn(), 1, ALL_TRUE, "erbs");
    private static final Node NODE2 = new Node(2L, N2.getFdn(), 1, ALL_TRUE, "erbs");
    private static final Node NODE3 = new Node(3L, N3.getFdn(), 1, ALL_TRUE, "radionode");
    private static final Node NODE4 = new Node(4L, N4.getFdn(), 1, ALL_TRUE, "erbs");
    private static final Node NODE5 = new Node(5L, N5.getFdn(), 1, ALL_TRUE, "default");
    private static final Node NODE6 = new Node(6L, N6.getFdn(), 1, ALL_TRUE, "ERBS");
    private static final Node NODE7 = new Node(7L, N7.getFdn(), 1, ALL_TRUE, "RADIONODE");

    @Test
    public void whenNoProfilesUsedForExecution_thenAccumulatorCalculatesWell() {
        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(
                Collections.singletonMap(N1, 6), Collections.emptyMap());
        assertThat(objectUnderTest.isAvailable(NODE1, 0)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE1, 1)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE1, 2)).isFalse();
    }

    @Test
    public void whenProfilesUsedForExecution_thenAccumulatorCalculatesWell() {
        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(
                Collections.singletonMap(N1, 6), Collections.singletonMap(N1, 1));

        assertThat(objectUnderTest.isAvailable(NODE1, 1)).isFalse();
    }

    @Test
    public void whenNoProfilesUsedForExecutionAndIncreasing_thenAccumulatorCalculatesWell() {
        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(
                Collections.singletonMap(N1, 6), Collections.emptyMap());

        assertThat(objectUnderTest.isAvailable(NODE1, 1)).isTrue();
        objectUnderTest.increase(N1, 1);
        assertThat(objectUnderTest.isAvailable(NODE1, 1)).isFalse();
    }

    @Test
    public void whenProfilesUsedForExecutionAndIncreasing_thenAccumulatorCalculatesWell() {
        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(
                Collections.singletonMap(N1, 5), Collections.singletonMap(N1, 1));

        assertThat(objectUnderTest.isAvailable(NODE1, 1)).isTrue();
        objectUnderTest.increase(N1, 1);
        assertThat(objectUnderTest.isAvailable(NODE1, 1)).isFalse();
    }

    @Test
    public void whenRadioNode_thenNumberOfProfilesOnNodeIsOk() {
        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(
                Collections.singletonMap(N3, 21), Collections.singletonMap(N3, 1));

        assertThat(objectUnderTest.isAvailable(NODE3, 1)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE3, 2)).isFalse();
    }

    @Test
    public void whenERBS_thenNumberOfProfilesOnNodeIsOk() {
        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(
                Collections.singletonMap(N4, 5), Collections.singletonMap(N4, 1));

        assertThat(objectUnderTest.isAvailable(NODE4, 1)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE4, 2)).isFalse();
    }

    @Test
    public void whenNodeTypeNotRadioNodeNorERBS_thenNumberOfProfilesOnNodeIsOk() {
        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(
                Collections.singletonMap(N5, 5), Collections.singletonMap(N5, 1));

        assertThat(objectUnderTest.isAvailable(NODE5, 1)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE5, 2)).isFalse();
    }

    @Test
    public void whenERBSWithUppercase_thenNumberOfProfilesOnNodeIsOk() {
        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(
                Collections.singletonMap(N6, 5), Collections.singletonMap(N6, 1));

        assertThat(objectUnderTest.isAvailable(NODE6, 1)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE6, 2)).isFalse();
    }

    @Test
    public void whenRadioNodeWithUppercase_thenNumberOfProfilesOnNodeIsOk() {
        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(
                Collections.singletonMap(N7, 21), Collections.singletonMap(N7, 1));

        assertThat(objectUnderTest.isAvailable(NODE7, 1)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE7, 2)).isFalse();
    }

    @Test
    public void whenProfilesUsedForExecutionAndIncreasingMultipleNodes_thenAccumulatorCalculatesWell() {
        final Map<TopologyObjectId, Integer> profilesUsedAccordingToCmService = new HashMap<>();
        final Map<TopologyObjectId, Integer> profilesUsedByExecution = new HashMap<>();

        profilesUsedAccordingToCmService.put(N1, 5);
        profilesUsedAccordingToCmService.put(N2, 4);
        profilesUsedByExecution.put(N1, 1);
        profilesUsedByExecution.put(N2, 1);

        final SectorUsedProfilesAccumulator objectUnderTest = new SectorUsedProfilesAccumulator(profilesUsedAccordingToCmService, profilesUsedByExecution);

        assertThat(objectUnderTest.isAvailable(NODE1, 1)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE1, 2)).isFalse();
        assertThat(objectUnderTest.isAvailable(NODE2, 1)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE2, 2)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE2, 3)).isFalse();

        objectUnderTest.increase(N1, 1);
        assertThat(objectUnderTest.isAvailable(NODE1, 1)).isFalse();

        objectUnderTest.increase(N2, 1);
        assertThat(objectUnderTest.isAvailable(NODE2, 1)).isTrue();
        assertThat(objectUnderTest.isAvailable(NODE2, 2)).isFalse();
    }
}
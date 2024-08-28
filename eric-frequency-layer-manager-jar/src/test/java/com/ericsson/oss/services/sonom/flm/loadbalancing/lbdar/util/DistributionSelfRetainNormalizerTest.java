/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;

/**
 * Unit tests for {@link DistributionSelfRetainNormalizer} class.
 */
public class DistributionSelfRetainNormalizerTest {
    private static final TopologyObjectId CELL1 = TopologyObjectId.of("cell_145603_2_2", 1);
    private static final TopologyObjectId CELL2 = TopologyObjectId.of("cell_145603_2_8", 1);
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void whenNegativeSelfRetain_thenSelfRetainBecomeHigherThanMinSelfRetain() {
        final int minSourceRetain = 10;
        final Map<TopologyObjectId, Float> distributions = new HashMap<>();
        distributions.put(CELL1, 50f);
        distributions.put(CELL2, 25f);

        final Map<TopologyObjectId, Float> stepSizes = new HashMap<>();
        stepSizes.put(CELL1, 14f);
        stepSizes.put(CELL2, 27f);

        final Map<TopologyObjectId, Float> result = DistributionSelfRetainNormalizer.normalize(distributions, -16f, stepSizes, minSourceRetain);
        assertThat(result).hasSize(2);
        softly.assertThat(100 - result.get(CELL1) + result.get(CELL2)).isGreaterThan(minSourceRetain);
        softly.assertThat(result.get(CELL1)).isCloseTo(54f, offset(0f));
        softly.assertThat(result.get(CELL2)).isCloseTo(34f, offset(0f));

    }

    @Test
    public void whenZeroSelfRetain_thenSelfRetainBecomeHigherThanMinSelfRetain() {
        final int minSourceRetain = 10;
        final Map<TopologyObjectId, Float> distributions = new HashMap<>();
        distributions.put(CELL1, 54f);
        distributions.put(CELL2, 33f);

        final Map<TopologyObjectId, Float> stepSizes = new HashMap<>();
        stepSizes.put(CELL1, 4f);
        stepSizes.put(CELL2, 9f);

        final Map<TopologyObjectId, Float> result = DistributionSelfRetainNormalizer.normalize(distributions, 0f, stepSizes, minSourceRetain);
        assertThat(result).hasSize(2);
        softly.assertThat(100 - result.get(CELL1) + result.get(CELL2)).isGreaterThan(minSourceRetain);
        softly.assertThat(result.get(CELL1)).isCloseTo(55f, offset(0f));
        softly.assertThat(result.get(CELL2)).isCloseTo(35f, offset(0f));
    }

    @Test
    public void whenNegativeSelfRetainAndMinSourceRetainZero_thenSelfRetainBecomeHigherThanMinSelfRetain() {
        final int minSourceRetain = 0;
        final Map<TopologyObjectId, Float> distributions = new HashMap<>();
        distributions.put(CELL1, 46f);
        distributions.put(CELL2, 42f);

        final Map<TopologyObjectId, Float> stepSizes = new HashMap<>();
        stepSizes.put(CELL1, 4f);
        stepSizes.put(CELL2, 9f);

        final Map<TopologyObjectId, Float> result = DistributionSelfRetainNormalizer.normalize(distributions, -1f, stepSizes, minSourceRetain);
        assertThat(result).hasSize(2);
        softly.assertThat(100 - result.get(CELL1) + result.get(CELL2)).isGreaterThan(minSourceRetain);
        softly.assertThat(result.get(CELL1)).isCloseTo(48f, offset(0f));
        softly.assertThat(result.get(CELL2)).isCloseTo(46f, offset(0f));
    }
}
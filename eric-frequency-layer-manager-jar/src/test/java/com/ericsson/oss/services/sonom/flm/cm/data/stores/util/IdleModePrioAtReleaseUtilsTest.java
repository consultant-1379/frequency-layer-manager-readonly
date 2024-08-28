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

package com.ericsson.oss.services.sonom.flm.cm.data.stores.util;

import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D1;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D1_NORMALIZED;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D2;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D2_NORMALIZED;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D3;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D4;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D5;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D5_NORMALIZED_ONLY_REMOVE_DUP_FREQ;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D5_NORMALIZED_ONLY_UNGROUP;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D6;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D6_NORMALIZED_ONLY_REMOVE_DUP_FREQ;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D6_NORMALIZED_ONLY_UNGROUP;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D7;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D7_NORMALIZED;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D8;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D8_NORMALIZED;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.D_EMPTYLIST;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease;

/**
 * Unit tests for class {@link IdleModePrioAtReleaseUtils}.
 */
@RunWith(MockitoJUnitRunner.class)
public class IdleModePrioAtReleaseUtilsTest {
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void whenRemoveGrouping_returnsCorrectValues() {
        softly.assertThat(IdleModePrioAtReleaseUtils.removeGrouping(D1)).isEqualTo(D1_NORMALIZED);
        softly.assertThat(IdleModePrioAtReleaseUtils.removeGrouping(D2)).isEqualTo(D2_NORMALIZED);
        assertRemoveGroupingReturnsSameDistributionInfo(D3);
        assertRemoveGroupingReturnsSameDistributionInfo(D4);
        softly.assertThat(IdleModePrioAtReleaseUtils.removeGrouping(D5)).isEqualTo(D5_NORMALIZED_ONLY_UNGROUP);
        softly.assertThat(IdleModePrioAtReleaseUtils.removeGrouping(D6)).isEqualTo(D6_NORMALIZED_ONLY_UNGROUP);
        softly.assertThat(IdleModePrioAtReleaseUtils.removeGrouping(D7)).isEqualTo(D7_NORMALIZED);
        softly.assertThat(IdleModePrioAtReleaseUtils.removeGrouping(D8)).isEqualTo(D8_NORMALIZED);
    }

    @Test
    public void whenRemoveDuplicateFreqs_returnsCorrectValues() {
        assertRemoveDuplicateFreqsReturnsSameDistributionInfo(D1);
        assertRemoveDuplicateFreqsReturnsSameDistributionInfo(D2);
        assertRemoveDuplicateFreqsReturnsSameDistributionInfo(D3);
        assertRemoveDuplicateFreqsReturnsSameDistributionInfo(D4);
        softly.assertThat(IdleModePrioAtReleaseUtils.removeDuplicateFreqs(D5)).isEqualTo(D5_NORMALIZED_ONLY_REMOVE_DUP_FREQ);
        softly.assertThat(IdleModePrioAtReleaseUtils.removeDuplicateFreqs(D6)).isEqualTo(D6_NORMALIZED_ONLY_REMOVE_DUP_FREQ);
        assertRemoveDuplicateFreqsReturnsSameDistributionInfo(D7);
        assertRemoveDuplicateFreqsReturnsSameDistributionInfo(D8);
    }

    @Test
    public void whenRemoveDuplicateFreqsNull_returnsNull() {
        assertRemoveDuplicateFreqsReturnsSameDistributionInfo(null);
    }

    @Test
    public void whenRemoveDuplicateFreqsEmptyList_returnsEmptyList() {
        assertRemoveDuplicateFreqsReturnsSameDistributionInfo(D_EMPTYLIST);
    }

    @Test
    public void whenRemoveGroupingNull_returnsNull() {
        assertRemoveGroupingReturnsSameDistributionInfo(null);
    }

    @Test
    public void whenRemoveGroupingEmptyList_returnsEmptyList() {
        assertRemoveGroupingReturnsSameDistributionInfo(D_EMPTYLIST);
    }

    private void assertRemoveGroupingReturnsSameDistributionInfo(final GenericIdleModePrioAtRelease.DistributionInfo distributionInfo) {
        softly.assertThat(IdleModePrioAtReleaseUtils.removeGrouping(distributionInfo)).isEqualTo(distributionInfo);
    }

    private void assertRemoveDuplicateFreqsReturnsSameDistributionInfo(final GenericIdleModePrioAtRelease.DistributionInfo distributionInfo) {
        softly.assertThat(IdleModePrioAtReleaseUtils.removeDuplicateFreqs(distributionInfo)).isEqualTo(distributionInfo);
    }
}

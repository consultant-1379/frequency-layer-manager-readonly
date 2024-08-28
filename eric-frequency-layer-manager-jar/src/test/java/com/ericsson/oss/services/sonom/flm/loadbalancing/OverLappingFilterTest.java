/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing;

import static com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo.OverlappingFlag.NOT_OVERLAPPING;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo.OverlappingFlag.OVERLAP_LOG_NEEDED;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo.OverlappingFlag.OVERLAP_DROP_NEEDED;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo;
import com.ericsson.oss.services.sonom.flm.loadbalancing.testutils.TestConstants;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OverLappingLogger.class)
public class OverLappingFilterTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Captor
    private ArgumentCaptor<Pair<PolicyOutputEvent, OverlapInfo>> policyOutputEventWithOverlapInfoCaptor;

    private OverLappingFilter objectUnderTest;

    private List<Pair<PolicyOutputEvent, OverlapInfo>> policyOutputEventWithOverLapInfoPairs;

    @Before
    public void setUp() {
        objectUnderTest = new OverLappingFilter();
    }

    @Test
    public void whenEmptyListGiven_thenEmptyListResulted() {
        PowerMockito.mockStatic(OverLappingLogger.class);
        final List<PolicyOutputEvent> policyOutputEvents = objectUnderTest.filterAndLogOverlapped(Collections.emptyList());
        softly.assertThat(policyOutputEvents).isEmpty();
        softly.assertThat(objectUnderTest.allPolicyOutputEventsWereDropped()).isFalse();
        PowerMockito.verifyZeroInteractions(OverLappingLogger.class);
    }

    @Test
    public void whenNoOverlappingCasesAreInList_thenNoneIsFiltered() {
        PowerMockito.mockStatic(OverLappingLogger.class);
        final PolicyOutputEventOverLappingBuilder builder = new PolicyOutputEventOverLappingBuilder();
        policyOutputEventWithOverLapInfoPairs = builder.withNoOverlapping(5)
                .build();
        final List<PolicyOutputEvent> policyOutputEvents = objectUnderTest.filterAndLogOverlapped(policyOutputEventWithOverLapInfoPairs);
        PowerMockito.verifyZeroInteractions(OverLappingLogger.class);
        softly.assertThat(policyOutputEvents).hasSize(5);
        softly.assertThat(objectUnderTest.allPolicyOutputEventsWereDropped()).isFalse();
    }

    @Test
    public void whenOnlyOverlappingCasesAreInListWithNoDroppingNeeded_thenNoneIsFiltered() {
        PowerMockito.mockStatic(OverLappingLogger.class);
        final PolicyOutputEventOverLappingBuilder builder = new PolicyOutputEventOverLappingBuilder();
        policyOutputEventWithOverLapInfoPairs = builder.withOverLappingNoDrop(7).build();
        final List<PolicyOutputEvent> policyOutputEvents = objectUnderTest.filterAndLogOverlapped(policyOutputEventWithOverLapInfoPairs);
        PowerMockito.verifyStatic(OverLappingLogger.class, times(7));
        OverLappingLogger.logOverlappingSectors(policyOutputEventWithOverlapInfoCaptor.capture());
        final List<Pair<PolicyOutputEvent, OverlapInfo>> capturedList = policyOutputEventWithOverlapInfoCaptor.getAllValues();
        softly.assertThat(capturedList).isEqualTo(policyOutputEventWithOverLapInfoPairs);
        softly.assertThat(policyOutputEvents).hasSize(7);
        softly.assertThat(objectUnderTest.allPolicyOutputEventsWereDropped()).isFalse();
    }

    @Test
    public void whenThereAreSomeOverlappingCasesAreInListWithNoDroppingNeeded_thenNoneIsFiltered() {
        PowerMockito.mockStatic(OverLappingLogger.class);
        final PolicyOutputEventOverLappingBuilder builder = new PolicyOutputEventOverLappingBuilder();
        policyOutputEventWithOverLapInfoPairs = builder
                .withNoOverlapping(5)
                .withOverLappingNoDrop(7)
                .build();
        final List<PolicyOutputEvent> policyOutputEvents = objectUnderTest.filterAndLogOverlapped(policyOutputEventWithOverLapInfoPairs);
        PowerMockito.verifyStatic(OverLappingLogger.class, times(7));
        OverLappingLogger.logOverlappingSectors(policyOutputEventWithOverlapInfoCaptor.capture());
        final List<Pair<PolicyOutputEvent, OverlapInfo>> capturedList = policyOutputEventWithOverlapInfoCaptor.getAllValues();
        softly.assertThat(capturedList).isNotEqualTo(policyOutputEventWithOverLapInfoPairs);
        softly.assertThat(capturedList).hasSize(7);
        softly.assertThat(capturedList.stream().noneMatch(p -> NOT_OVERLAPPING.equals(p.getRight().getOverlappingFlag()))).isTrue();
        softly.assertThat(policyOutputEvents).hasSize(12);
        softly.assertThat(objectUnderTest.allPolicyOutputEventsWereDropped()).isFalse();
    }

    @Test
    public void whenThereAreSomeOverlappingCasesInListWithDroppingNeeded_thenThoseAreFiltered() {
        PowerMockito.mockStatic(OverLappingLogger.class);
        final PolicyOutputEventOverLappingBuilder builder = new PolicyOutputEventOverLappingBuilder();
        policyOutputEventWithOverLapInfoPairs = builder
                .withNoOverlapping(5)
                .withOverLappingNoDrop(7)
                .withOverlappingDropNeeded(3)
                .build();
        final List<PolicyOutputEvent> policyOutputEvents = objectUnderTest.filterAndLogOverlapped(policyOutputEventWithOverLapInfoPairs);
        PowerMockito.verifyStatic(OverLappingLogger.class, times(10));
        OverLappingLogger.logOverlappingSectors(policyOutputEventWithOverlapInfoCaptor.capture());
        final List<Pair<PolicyOutputEvent, OverlapInfo>> capturedList = policyOutputEventWithOverlapInfoCaptor.getAllValues();
        softly.assertThat(capturedList).isNotEqualTo(policyOutputEventWithOverLapInfoPairs);
        softly.assertThat(capturedList).hasSize(10);
        softly.assertThat(capturedList.stream().noneMatch(p -> NOT_OVERLAPPING.equals(p.getRight().getOverlappingFlag()))).isTrue();
        softly.assertThat(capturedList.stream().filter(p -> OVERLAP_DROP_NEEDED.equals(p.getRight().getOverlappingFlag())).count()).isEqualTo(3);
        softly.assertThat(capturedList.stream().filter(p -> OVERLAP_LOG_NEEDED.equals(p.getRight().getOverlappingFlag())).count()).isEqualTo(7);
        softly.assertThat(policyOutputEvents).hasSize(12);
        softly.assertThat(policyOutputEvents.stream().noneMatch(p -> Arrays.asList("Name13", "Name14", "Name15").contains(p.getName()))).isTrue();
        softly.assertThat(objectUnderTest.allPolicyOutputEventsWereDropped()).isFalse();
    }

    @Test
    public void whenOnlyOverlappingCasesAreInListWithDroppingNeeded_thenAllAreFiltered() {
        PowerMockito.mockStatic(OverLappingLogger.class);
        final PolicyOutputEventOverLappingBuilder builder = new PolicyOutputEventOverLappingBuilder();
        policyOutputEventWithOverLapInfoPairs = builder
                .withOverlappingDropNeeded(6)
                .build();
        final List<PolicyOutputEvent> policyOutputEvents = objectUnderTest.filterAndLogOverlapped(policyOutputEventWithOverLapInfoPairs);
        PowerMockito.verifyStatic(OverLappingLogger.class, times(6));
        OverLappingLogger.logOverlappingSectors(policyOutputEventWithOverlapInfoCaptor.capture());
        final List<Pair<PolicyOutputEvent, OverlapInfo>> capturedList = policyOutputEventWithOverlapInfoCaptor.getAllValues();
        softly.assertThat(capturedList).isEqualTo(policyOutputEventWithOverLapInfoPairs);
        softly.assertThat(capturedList).hasSize(6);
        softly.assertThat(capturedList.stream().allMatch(p -> OVERLAP_DROP_NEEDED.equals(p.getRight().getOverlappingFlag()))).isTrue();
        softly.assertThat(policyOutputEvents).hasSize(0);
        softly.assertThat(objectUnderTest.allPolicyOutputEventsWereDropped()).isTrue();
    }

    static class PolicyOutputEventOverLappingBuilder {

        private int numberOfNoOverlapping;
        private int numberOfOverlappingNoDrop;
        private int numberOfOverlappingDropNeeded;

        private static final String C1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-1";
        private static final String C2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00002-1";

        public PolicyOutputEventOverLappingBuilder withNoOverlapping(final int numberOfNoOverlapping) {
            this.numberOfNoOverlapping = numberOfNoOverlapping;
            return this;
        }

        public PolicyOutputEventOverLappingBuilder withOverLappingNoDrop(final int numberOfOverlappingNoDrop) {
            this.numberOfOverlappingNoDrop = numberOfOverlappingNoDrop;
            return this;
        }

        public PolicyOutputEventOverLappingBuilder withOverlappingDropNeeded(final int numberOfOverlappingDropNeeded) {
            this.numberOfOverlappingDropNeeded = numberOfOverlappingDropNeeded;
            return this;
        }

        public List<Pair<PolicyOutputEvent, OverlapInfo>> build() {
            final List<Pair<PolicyOutputEvent, OverlapInfo>> list = new ArrayList<>();
            int number = 0;
            for (int i = 0; i < numberOfNoOverlapping; i++) {
                list.add(Pair.of(buildPolicyOutputEvent("Name" + ++number), OverlapInfo.of(NOT_OVERLAPPING, "")));
            }
            for (int i = 0; i < numberOfOverlappingNoDrop; i++) {
                list.add(Pair.of(buildPolicyOutputEvent("Name" + ++number), OverlapInfo.of(OVERLAP_LOG_NEEDED, "")));
            }
            for (int i = 0; i < numberOfOverlappingDropNeeded; i++) {
                list.add(Pair.of(buildPolicyOutputEvent("Name" + ++number), OverlapInfo.of(OVERLAP_DROP_NEEDED, "")));
            }

            return list;
        }

        private static PolicyOutputEvent buildPolicyOutputEvent(final String name) {
            final ProposedLoadBalancingQuanta quanta = new ProposedLoadBalancingQuanta(
                    C1, 1, "50",
                    Collections.singletonList(new TargetCell(C2, 1, "50")));

            return new PolicyOutputEvent(name, "1.0", "namespace", "alg_FLM_1", "flm",
                    TestConstants.S1, "FLM_1", quanta, Collections.emptyList());
        }

    }

}
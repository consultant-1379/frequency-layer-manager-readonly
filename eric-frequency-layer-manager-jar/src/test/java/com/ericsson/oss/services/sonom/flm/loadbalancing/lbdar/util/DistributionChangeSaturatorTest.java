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

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.JUnitSoftAssertions;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

@RunWith(MockitoJUnitRunner.class)
public class DistributionChangeSaturatorTest {
    private static final int OSS_ID = 1;
    private static final TopologyObjectId S = TopologyObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1," +
            "MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-1", OSS_ID);
    private static final TopologyObjectId T1 = TopologyObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1," +
            "MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-2", OSS_ID);
    private static final TopologyObjectId T2 = TopologyObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1," +
            "MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-3", OSS_ID);

    private static final String SP = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001," +
            "ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1";
    private static final String TP1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001," +
            "ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=2";
    private static final String TP2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001," +
            "ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=3";

    private static final EnrichedIdleModePrioAtRelease SOURCE_PROFILE = getProfile(Arrays.asList(50f, 20f, 30f));
    private static final EnrichedIdleModePrioAtRelease TARGET_PROFILE_1 = getProfile(Arrays.asList(55f, 15f, 30f));
    private static final EnrichedIdleModePrioAtRelease TARGET_PROFILE_2 = getProfile(Arrays.asList(45f, 25f, 30f));
    private static final EnrichedIdleModePrioAtRelease NEW_SOURCE_PROFILE = getProfile(Arrays.asList(24f, 21f, 55f));
    private static final List<Float> EXPECTED_DISTRIBUTION = Arrays.asList(28f, 22f, 50f);
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DistributionChangeSaturator objectUnderTest;

    @Mock
    private ProfileChangeCalculatorSettings configMock;

    @Mock
    private ProfileChangeCalculationHelper helperMock;

    @Before
    public void setUp() {

        objectUnderTest = new DistributionChangeSaturator(getEnrichedPolicyOutputEvent(), configMock, helperMock);
    }

    @Test
    public void whenSaturateStepSizeCalled_thenCalculatesSaturatedValues() throws LbdarUnexpectedException, LbdarException {
        when(configMock.getMaxLbdarStepSize(10000)).thenReturn(Optional.of(10.0f));
        when(configMock.getMaxLbdarStepSize(20000)).thenReturn(Optional.of(20.0f));
        when(configMock.getMinLbdarStepSize()).thenReturn(2.0f);

        when(helperMock.getSelectedDistributionInfo(SOURCE_PROFILE, S))
                .thenReturn(SOURCE_PROFILE.getLowLoadDistributionInfo());
        when(helperMock.getSelectedDistributionInfo(NEW_SOURCE_PROFILE, S))
                .thenReturn(NEW_SOURCE_PROFILE.getLowLoadDistributionInfo());
        when(helperMock.updateDistributionOfProfile(any(), eq(S), eq(T1), eq(22f)))
                .thenReturn(getProfile(Arrays.asList(48f, 22f, 30f)));
        when(helperMock.updateDistributionOfProfile(any(), eq(S), eq(T2), eq(50f)))
                .thenReturn(getProfile(Arrays.asList(28f, 22f, 50f)));

        final Map.Entry<TopologyObjectId, EnrichedIdleModePrioAtRelease> modifiedProfile = new AbstractMap.SimpleEntry<>(S, NEW_SOURCE_PROFILE);

        final Optional<EnrichedIdleModePrioAtRelease> result = objectUnderTest.saturateStepSize(modifiedProfile);
        softly.assertThat(result.isPresent()).isTrue();
        softly.assertThat(result.get().getLowLoadDistributionInfo().getFreqDistributionList()).hasSameElementsAs(EXPECTED_DISTRIBUTION);
    }

    @Test
    public void whenSaturateStepSizeCalledAndNoStepSizeExistsForBandwidth_thenCalculatesSaturatedThrowsException()
            throws LbdarUnexpectedException, LbdarException {
        when(configMock.getMaxLbdarStepSize(10000)).thenReturn(Optional.empty());
        when(helperMock.getSelectedDistributionInfo(SOURCE_PROFILE, S))
                .thenReturn(SOURCE_PROFILE.getLowLoadDistributionInfo());
        when(helperMock.getSelectedDistributionInfo(NEW_SOURCE_PROFILE, S))
                .thenReturn(NEW_SOURCE_PROFILE.getLowLoadDistributionInfo());

        final Map.Entry<TopologyObjectId, EnrichedIdleModePrioAtRelease> modifiedProfile = new AbstractMap.SimpleEntry<>(S, NEW_SOURCE_PROFILE);

        thrown.expect(LbdarUnexpectedException.class);
        thrown.expectMessage(StringContains.containsString("No valid maxLbdarStepsize configuration for"));

        objectUnderTest.saturateStepSize(modifiedProfile);
    }

    private EnrichedPolicyOutputEvent getEnrichedPolicyOutputEvent() {
        return new EnrichedPolicyOutputEvent(new PolicyOutputEvent("", "", "", "", "", 12032023023L,
                "FLM_23232", null, null),
                null, getCellCmData(), getProfiles(), null, getFrequencyToCarrier(), Collections.EMPTY_MAP);
    }

    private Map<TopologyObjectId, Cell> getCellCmData() {
        final Map<TopologyObjectId, Cell> cells = new HashMap<>();
        cells.put(S, new Cell(1L, OSS_ID, S.getFdn(), 5230, SP, "cgi1",
                5000, "indoor", ""));
        cells.put(T1, new Cell(2L, OSS_ID, T1.getFdn(), 900, TP1, "cgi2",
                10000, "indoor", ""));
        cells.put(T2, new Cell(3L, OSS_ID, T2.getFdn(), 66986, TP2, "cgi3",
                20000, "indoor", ""));
        return cells;
    }

    private Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> getProfiles() {
        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profiles = new HashMap<>();
        profiles.put(TopologyObjectId.of(SP, OSS_ID), SOURCE_PROFILE);
        profiles.put(TopologyObjectId.of(TP1, OSS_ID), TARGET_PROFILE_1);
        profiles.put(TopologyObjectId.of(TP2, OSS_ID), TARGET_PROFILE_2);
        return profiles;
    }

    private static EnrichedIdleModePrioAtRelease getProfile(final List<Float> distributions) {
        return new EnrichedIdleModePrioAtRelease(1L, SP, OSS_ID, "1", Arrays.asList(0, 50, 100, 150, 200),
                getDistributionInfos(distributions), Collections.emptySet());
    }

    private static List<EnrichedDistributionInfo> getDistributionInfos(final List<Float> distributions) {
        final List<EnrichedDistributionInfo> result = new ArrayList<>();
        Arrays.stream(GenericIdleModePrioAtRelease.ThresholdLevel.values()).forEach(
                threshold -> result.add(getDistributionInfo(threshold, distributions)));
        return result;
    }

    private static EnrichedDistributionInfo getDistributionInfo(final GenericIdleModePrioAtRelease.ThresholdLevel level,
            final List<Float> distributions) {

        return new EnrichedDistributionInfo(level, distributions, Arrays.asList("f1", "f2", "f3"),
                getFrequencyToCarrier().entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getFdn(), Map.Entry::getValue)));
    }

    private static Map<TopologyObjectId, Integer> getFrequencyToCarrier() {
        final Map<TopologyObjectId, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(TopologyObjectId.of("f1", OSS_ID), 5230);
        frequencyToCarrier.put(TopologyObjectId.of("f2", OSS_ID), 900);
        frequencyToCarrier.put(TopologyObjectId.of("f3", OSS_ID), 66986);
        return frequencyToCarrier;
    }
}

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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD;
import static com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.EnrichedPolicyOutputEventBuilder;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder;

/**
 * Unit tests for {@link EnrichedPolicyOutputEventValidator} class.
 */
public class EnrichedPolicyOutputEventValidatorTest {

    private final EnrichedPolicyOutputEventValidator objectUnderTest = new EnrichedPolicyOutputEventValidator();
    private EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private final Map<String, Integer> frequencyToCarrier = new HashMap<>();

    @Before
    public void setUp() {
        frequencyToCarrier.put(InputDataBuilder.F1, 1500);
        frequencyToCarrier.put(InputDataBuilder.F2, 2300);
    }

    @Test
    public void whenEnrichedPolicyOutputEventContainsValidData_thenValidationWillPass() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isTrue();
    }

    @Test
    public void whenProfilesOfSourceAndTargetCellsIsNotConsistentWithLessThan5ThresholdValue_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(100, 300, 400, 500))
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenThereIsNoCellKpiForAllCells_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();
        enrichedPolicyOutputEvent.getCellKpis().remove(InputDataBuilder.CELL_TARGET_ONE);
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenSubscriptionRatioIsNullAtOneOfTheCells_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .withCellLevelInfo()
                .subscriptionRatioAtSource(0.0f)
                .build();
        enrichedPolicyOutputEvent.getCellKpis().remove(InputDataBuilder.CELL_TARGET_ONE);
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenConnectedUsersIsNullAtOneOfTheCells_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .withCellLevelInfo()
                .connectedUsersAtSource(0)
                .build();
        enrichedPolicyOutputEvent.getCellKpis().remove(InputDataBuilder.CELL_TARGET_ONE);
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenThereIsNoCellCmDataForAllCells_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();
        enrichedPolicyOutputEvent.getCellCmData().remove(InputDataBuilder.CELL_TARGET_ONE);
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenProfilesOfSourceAndTargetCellsIsNotConsistentWithNonPositiveThresholdValue_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(-1, 250, 450, 700, 900))
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenProfilesOfSourceAndTargetCellsIsNotConsistentWithNotIncreasingThresholdLimits_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(0, 250, 450, 400, 900))
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenProfilesOfSourceAndTargetCellsIsNotConsistentWithEmptyFreqDistributionList_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .frequenciesAtSource(LOW_MEDIUM_LOAD_THRESHOLD, Collections.emptyList(), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenThresholdIsChosenAndSourceToTargetConfiguredWithNull_thenValidationWillPass() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtSource(MEDIUM_LOAD_THRESHOLD, Arrays.asList(1f, null), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isTrue();
    }

    @Test
    public void whenThresholdIsChosenAndSourceToTargetConfiguredWithZeroPercent_thenValidationWillPass() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtSource(MEDIUM_LOAD_THRESHOLD, Arrays.asList(100f, 0f), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isTrue();
    }

    @Test
    public void whenThresholdIsChosenAndTargetToSourceConfiguredWithNull_thenValidationWillPass() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtTargetOne(MEDIUM_LOAD_THRESHOLD, Arrays.asList(1f, null), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isTrue();
    }

    @Test
    public void whenThresholdIsChosenAndThereIsNoReferenceFromSourceToTargetInDistributionList_thenValidationWillPass() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .frequenciesAtSource(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(InputDataBuilder.F1), frequencyToCarrier)
                .distributionsAtSource(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(1f), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isTrue();
    }

    @Test
    public void whenThresholdIsChosenAndThereIsNoReferenceFromTargetToSourceInDistributionList_thenValidationWillPass() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .frequenciesAtTargetOne(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(InputDataBuilder.F2), frequencyToCarrier)
                .distributionsAtTargetOne(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(1f), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isTrue();
    }

    @Test
    public void whenThresholdIsChosenAndThereIsNoReferenceFromSourceToSourceInDistributionList_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtSource(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(1f), frequencyToCarrier)
                .frequenciesAtSource(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(InputDataBuilder.F2), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenThresholdIsChosenAndThereIsNoReferenceFromTargetToTargetInDistributionList_thenValidationWillPass() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtTargetOne(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(1f), frequencyToCarrier)
                .frequenciesAtTargetOne(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(InputDataBuilder.F1), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isTrue();
    }

    @Test
    public void whenSumOfDistributionValueIsLessThanOneHundredPercent_thenValidationWillPass() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtSource(MEDIUM_LOAD_THRESHOLD, Arrays.asList(0.8f, 0.1f), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isTrue();
    }

    @Test
    public void whenSumOfDistributionValueIsGreaterThanOneHundredPercent_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtSource(MEDIUM_LOAD_THRESHOLD, Arrays.asList(80f, 22f), frequencyToCarrier)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenSourceCellNodeIsNull_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = spy(EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build());
        when(enrichedPolicyOutputEvent.getSourceCellNode()).thenReturn(null);
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenCellKpisIsNull_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = spy(EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build());
        when(enrichedPolicyOutputEvent.getCellKpis()).thenReturn(null);
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenIdleModePrioAtReleaseRefIsEmpty_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                .idleModePrioAtReleaseRefAtSource("")
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenCellKpiSubscriptionRatioIsZero_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                .subscriptionRatioAtSource(0f)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenCellKpiConnectedUsersIsZero_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                .connectedUsersAtSource(0)
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenOneFrequencyIsMissing_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();
        enrichedPolicyOutputEvent.getFrequencyToCarrier().remove(TopologyObjectId.of(InputDataBuilder.F1, InputDataBuilder.OSS_ID));
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenOneProfileReferencedInCellIsMissing_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();
        enrichedPolicyOutputEvent.getProfiles().remove(InputDataBuilder.PROFILE_TWO);
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenNoProfilesExists_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();
        enrichedPolicyOutputEvent.getProfiles().clear();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenSectorWithDuplicateCellCarriers_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCellsWithSameCarrier()
                .build();
        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }

    @Test
    public void whenAtLeastOneOfTheDistributionInfosHasNoFrequencyReferences_thenValidationWillBreak() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();

        final EnrichedIdleModePrioAtRelease enrichedIdleModePrioAtRelease = enrichedPolicyOutputEvent.getProfiles().get(new TopologyObjectId(
                "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=1",
                1));
        final List<EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo> distributionInfos = enrichedIdleModePrioAtRelease.getDistributionInfos();
        distributionInfos.clear();
        distributionInfos.add(
                new EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo(
                        new GenericIdleModePrioAtRelease.DistributionInfo(
                                GenericIdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD, Arrays.asList(80f, 10f, 10f),
                                Collections.EMPTY_LIST),
                        frequencyToCarrier));
        enrichedIdleModePrioAtRelease.setDistributionInfos(distributionInfos);

        assertThat(objectUnderTest.isValid(enrichedPolicyOutputEvent)).isFalse();
    }
}

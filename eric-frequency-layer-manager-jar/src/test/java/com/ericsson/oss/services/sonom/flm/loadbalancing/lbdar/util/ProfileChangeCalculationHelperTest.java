/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021-2023
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

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants.OUTDOOR;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.C1;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_SOURCE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_TARGET_ONE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_TARGET_TWO;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.F1;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.F2;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.F3;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.PROFILE_ONE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.OSS_ID;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.P1;
import static org.assertj.core.data.Offset.offset;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.DistributionInfo;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.EnrichedPolicyOutputEventBuilder;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link ProfileChangeCalculationHelper} class.
 */
public class ProfileChangeCalculationHelperTest {

    private EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private ProfileChangeCalculationHelper objectUnderTest;
    private EnrichedIdleModePrioAtRelease profile;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void whenProfilesAreConsistent_thenRightThresholdIsChosen() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .withCellLevelInfo()
                    .subscriptionRatioAtTargetTwo(1f)
                    .distrLoadsAtTargetTwo(Arrays.asList(0,0,0,0,5))
                .build();

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);

        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_SOURCE)).isEqualTo(2);
        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_TARGET_ONE)).isEqualTo(1);
        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_TARGET_TWO)).isEqualTo(4);
    }

    @Test
    public void whenProfilesAreConsistentAndExtraThresholdCouldBeChosen_thenThresholdsWithHigherKPIIsChosen() throws LbdarException {

        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                    .distrLoadsAtSource(Arrays.asList(0, 0, 5, 7, 0))
                    .distrLoadsAtTargetOne(Arrays.asList(0, 4, 2, 0, 0))
                .withProfile()
                    .thresholdLimitsAtSource(Arrays.asList(0, 250, 450, 500, 900))
                    .thresholdLimitsAtTargetOne(Arrays.asList(0, 250, 350, 700, 900))
                .build();

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);

        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_SOURCE)).isEqualTo(3);
        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_TARGET_ONE)).isEqualTo(1);
    }

    @Test
    public void whenKpisAreInconsistentOrPmIdleCountersAreZero_thenRightThresholdIsChosen() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .withCellLevelInfo()
                .subscriptionRatioAtTargetTwo(0.01f)
                .distrLoadsAtTargetTwo(Arrays.asList(0,2,0,0,0))
                .distrLoadsAtTargetOne(Arrays.asList(0,0,0,0,0))
                .build();

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);

        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_SOURCE)).isEqualTo(2);
        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_TARGET_ONE)).isEqualTo(1);
        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_TARGET_TWO)).isZero();
    }

    @Test
    public void whenAllLoadLevelsHaveTheSameThreshold_ThenHighestLevelIsChosen() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                .distrLoadsAtSource(Arrays.asList(0, 202, 6850, 678, 100))
                .subscriptionRatioAtSource(0.061145833f)
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(50, 50, 50, 50, 50))
                .build();

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_SOURCE)).isEqualTo(4);
    }

    @Test
    public void whenFourLoadLevelsHaveTheSameThreshold_andHighLoadKpiIsZero_ThenCorrectLevelIsChosen() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                .distrLoadsAtSource(Arrays.asList(0, 202, 6850, 0, 0))
                .subscriptionRatioAtSource(0.061145833f)
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(0, 50, 50, 50, 50))
                .build();

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        softly.assertThat(objectUnderTest.getSelectedThreshold(CELL_SOURCE)).isEqualTo(4);
    }

    @Test
    public void whenDistributionSetIsAsked_thenDistributionIsCorrectlySet() throws LbdarException, LbdarUnexpectedException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        profile = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE);

        profile = objectUnderTest.updateDistributionOfProfile(profile, CELL_SOURCE, CELL_TARGET_ONE, 25f);
        final Float ownDistribution = profile.getDistributionInfos().get(2).getFreqDistributionList().get(0);
        final Float sourceToTargetDistribution = profile.getDistributionInfos().get(2).getFreqDistributionList().get(1);

        softly.assertThat(75D).isCloseTo(ownDistribution, offset(0.0));
        softly.assertThat(25D).isCloseTo(sourceToTargetDistribution, offset(0.0));
    }

    @Test
    public void whenDistributionSetIsAskedForUnknownFrequency_thenDistributionIsCorrectlySet() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsOneHasNoConnectionWithSource()
                .build();

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        profile = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE);

        profile = objectUnderTest.updateDistributionOfProfile(profile, CELL_SOURCE, CELL_TARGET_TWO, 10f);
        final Float ownDistribution = profile.getDistributionInfos().get(2).getFreqDistributionList().get(0);
        final Float sourceToTargetOneDistribution = profile.getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final Float sourceToTargetTwoDistribution = profile.getDistributionInfos().get(2).getFreqDistributionList().get(2);

        softly.assertThat(80D).isCloseTo(ownDistribution, offset(0.0));
        softly.assertThat(10D).isCloseTo(sourceToTargetOneDistribution, offset(0.0));
        softly.assertThat(10D).isCloseTo(sourceToTargetTwoDistribution, offset(0.0));
    }

    @Test
    public void whenSelectedDistributionInfoIsAsked_thenCorrectDistributionInfoIsGiven() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        profile = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE);

        final DistributionInfo selectedDistributionInfo = objectUnderTest.getSelectedDistributionInfo(profile, CELL_SOURCE);

        softly.assertThat(selectedDistributionInfo).isEqualTo(profile.getDistributionInfos().get(2));
        softly.assertThat(selectedDistributionInfo).isNotEqualTo(profile.getDistributionInfos().get(1));
    }

    @Test
    public void whenTargetCellsFilteredForLeakageHandling_thenCorrectTargetCellListIsGiven() throws LbdarException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(F1, 1500);
        frequencyToCarrier.put(F2, 2300);
        frequencyToCarrier.put(F3, 4500);
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        final EnrichedIdleModePrioAtRelease sourceProfile = enrichedPolicyOutputEvent.getProfileFromCellId(CELL_SOURCE);
        final DistributionInfo distributionInfo = sourceProfile.getDistributionInfos().get(2);
        final EnrichedIdleModePrioAtRelease modifiedSourceProfile
                = sourceProfile.getModifiedCopy(new EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo(distributionInfo.getThresholdLevel(),
                                                                     Arrays.asList(75f, 15f, 10f),
                                                                     distributionInfo.getEUtranFreqRefList(),
                                                                     frequencyToCarrier));
        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> changedProfiles = new HashMap<>();
        changedProfiles.put(CELL_SOURCE, modifiedSourceProfile);
        final List<TopologyObjectId> targetCellsWithIncreasedUsers = objectUnderTest.getTargetCellsWithIncreasedUsers(changedProfiles);
        softly.assertThat(targetCellsWithIncreasedUsers.size()).isEqualTo(1);
        softly.assertThat(targetCellsWithIncreasedUsers).contains(CELL_TARGET_ONE);
    }

    @Test
    public void whenDistributionSetIsZero_thenExceptionThrown() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsOneHasNoConnectionWithSource()
                .build();
        enrichedPolicyOutputEvent.getCellCmData().put(CELL_SOURCE,new Cell(1L, OSS_ID, C1, 0, P1, "cgi1", 5000, OUTDOOR, "undefined"));

        objectUnderTest = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        profile = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE);

        thrown.expect(LbdarException.class);
        thrown.expectMessage(containsString("Change excluded as target cell self retain value is null or zero"));

        profile = objectUnderTest.updateDistributionOfProfile(profile, CELL_SOURCE, CELL_TARGET_TWO, 10f);

    }

}

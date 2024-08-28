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

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD;
import static com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD;
import static com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD;
import static com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants.OUTDOOR;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_SOURCE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_TARGET_ONE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_TARGET_TWO;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_EXISTING_HIGH_PUSH;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_LBQ_IMPACT;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_THIRD_CELL;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MAXIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MINIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_OVERRIDE_C_CALCULATOR;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_TARGET_PUSH_BACK;
import static org.assertj.core.data.Offset.offset;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.DistributionInfo;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDaoImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.EnrichedPolicyOutputEventBuilder;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder;
import com.ericsson.oss.services.sonom.flm.settings.CellFlmSettingsRetriever;

/**
 * Unit tests for {@link ProfileChangesCalculatorImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileChangesCalculatorTest {

    private static final String LEAKAGE_ERROR_MSG = "Cell SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00003-1"
            + " can't be used as leakage cell because";

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private CmNodeObjectsStore profileStore;

    @Mock
    private CellFlmSettingsRetriever cellFlmSettingsRetriever;

    @Mock
    private LbdarDaoImpl lbdarDao;

    private EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;

    private ProfileChangesCalculatorImpl objectUnderTest;

    private ProfileChanges profileChanges;

    private final Map<String, String> customizedGlobalSettings = new HashMap<>();

    @Before
    public void setUp() throws SQLException {
        fillUpCustomizedGlobalSettings();
        ProfileChangesCalculatorImpl.setCellFlmSettingsRetriever(cellFlmSettingsRetriever);
        when(cellFlmSettingsRetriever.retrieveGivenCellSettingValue(anyString(), anyInt(), anyString(), isNull())).thenReturn("20");
    }

    @Test
    public void whenThresholdPushbackToTargetIsSet_thenSetThresholdPushbackToTargetAndNoCValueCalculated()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F1, 1500);
        frequencyToCarrier.put(InputDataBuilder.F2, 2300);
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtTargetOne(LOW_MEDIUM_LOAD_THRESHOLD, Arrays.asList(100f, 0f), frequencyToCarrier)
                .frequenciesAtTargetOne(LOW_MEDIUM_LOAD_THRESHOLD, Arrays.asList(InputDataBuilder.F2, InputDataBuilder.F1), frequencyToCarrier)
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float defaultPushBack = Float.parseFloat(customizedGlobalSettings.get(THRESHOLD_TARGET_PUSH_BACK));
        final float distribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);

        softly.assertThat(pushBackIsSetToTarget(defaultPushBack, CELL_TARGET_ONE, profileChanges, 1)).isTrue();
        softly.assertThat(sumOfDistributionIsOneHundredPercent(profileChanges, CELL_TARGET_ONE, 1)).isTrue();
        softly.assertThat(distribution).isCloseTo(12f, offset(0.0f));
    }

    @Test
    public void whenNoSourceFrequencySetInTargetAndThresholdPushbackToTargetIsSet_thenSetThresholdPushbackToTargetAndNoCValueCalculated()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F2, 2300);
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .frequenciesAtTargetOne(LOW_MEDIUM_LOAD_THRESHOLD, Collections.singletonList(InputDataBuilder.F2), frequencyToCarrier)
                .distributionsAtTargetOne(LOW_MEDIUM_LOAD_THRESHOLD, Collections.singletonList(100f), frequencyToCarrier)
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float defaultPushBack = Float.parseFloat(customizedGlobalSettings.get(THRESHOLD_TARGET_PUSH_BACK));
        final float distribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);

        softly.assertThat(pushBackIsSetToTarget(defaultPushBack, CELL_TARGET_ONE, profileChanges, 1)).isTrue();
        softly.assertThat(sumOfDistributionIsOneHundredPercent(profileChanges, CELL_TARGET_ONE, 1)).isTrue();
        softly.assertThat(distribution).isCloseTo(12f, offset(0.0f));

    }

    @Test
    public void whenSourceToTargetCalculationRun_thenCorrectResultIsGiven() throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float sumOfDistribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().stream().reduce(0.0f, Float::sum);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distribution).isCloseTo(12f, offset(0.0f));
        softly.assertThat(sumOfDistribution).isCloseTo(100f, offset(0.0f));
    }

    @Test
    public void whenSourceToTargetCalculationRunButRoundedResultIsTheSameAsOriginal_thenNoProfileChangeCalculated()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withLBQ()
                .usersToMoveAtTargetOne(15)
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        softly.assertThat(profileChanges).isNull();
    }

    @Test
    public void whenSourceToTargetCalculationRunButSumOfDistributionIsLessThanOneHundred_thenCorrectResultIsGiven()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F2, 2300);
        frequencyToCarrier.put(InputDataBuilder.F1, 1500);
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtSource(MEDIUM_LOAD_THRESHOLD, Arrays.asList(88f, 10f), frequencyToCarrier)
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float sumOfDistribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().stream().reduce(0.0f, Float::sum);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distribution).isCloseTo(12f, offset(0.0f));
        softly.assertThat(sumOfDistribution).isCloseTo(98f, offset(0.0f));
    }

    @Test
    public void whenSourceToTargetCalculationRunButThereIsNoEquilibrium_thenCorrectResultIsGiven()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                .connectedUsersAtSource(1200)
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float sumOfDistribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().stream().reduce(0f, Float::sum);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distribution).isCloseTo(11f, offset(0.0f));
        softly.assertThat(sumOfDistribution).isCloseTo(100f, offset(0.0f));

    }

    @Test
    public void whenSourceToTargetCalculationRunAndCValueCalculationIsOverrode_thenCorrectResultIsGiven()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                .connectedUsersAtSource(1300)
                .bandWidthAtSource(10000)
                .build();

        customizedGlobalSettings.put(THRESHOLD_OVERRIDE_C_CALCULATOR, "Yes");

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float sumOfDistribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().stream().reduce(0f, Float::sum);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distribution).isCloseTo(19f, offset(0.0f));
        softly.assertThat(sumOfDistribution).isCloseTo(100f, offset(0.0f));
    }

    @Test
    public void whenThereAreMoreTargetCellsAndSourceToTargetCalculationRun_thenCorrectResultIsGiven()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsAndTargetCellsHaveNoConnection()
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float distributionToTargetTwo = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(2);
        final float sumOfDistribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().stream().reduce(0f, Float::sum);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distributionToTargetOne).isCloseTo(12f, offset(0.0f));
        softly.assertThat(distributionToTargetTwo).isCloseTo(11f, offset(0.0f));
        softly.assertThat(sumOfDistribution).isCloseTo(100f, offset(0.0f));
    }

    @Test
    public void whenThereAreMoreTargetCellsAndSourceToTargetCalculationRunWithAnUnbalancedSector_thenCorrectResultIsGiven()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsAndTargetCellsHaveNoConnection()
                .withLBQ()
                .usersToMoveAtTargetOne(200)
                .withCellLevelInfo()
                .connectedUsersAtSource(1400)
                .connectedUsersAtTargetTwo(1100)
                .bandWidthAtSource(10000)
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float distributionToTargetTwo = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(2);
        final float sumOfDistribution = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().stream().reduce(0f, Float::sum);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distributionToTargetOne).isCloseTo(17f, offset(0.0f));
        softly.assertThat(distributionToTargetTwo).isCloseTo(12f, offset(0.0f));
        softly.assertThat(sumOfDistribution).isCloseTo(100f, offset(0.0f));
    }

    @Test
    public void whenThresholdPushbackToTargetIsSetAndSourceToTargetCalculationRuns_thenCorrectResultIsGiven()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F2, 2300);
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .frequenciesAtTargetOne(LOW_MEDIUM_LOAD_THRESHOLD, Collections.singletonList(InputDataBuilder.F2), frequencyToCarrier)
                .distributionsAtTargetOne(LOW_MEDIUM_LOAD_THRESHOLD, Collections.singletonList(100f), frequencyToCarrier)
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float distributionToSource = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_TARGET_ONE)
                .getDistributionInfos().get(1).getFreqDistributionList().get(1);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distributionToTargetOne).isCloseTo(12f, offset(0.0f));
        softly.assertThat(distributionToSource).isCloseTo(2f, offset(0.0f));
    }

    @Test
    public void whenThresholdPushbackToTargetIsSetAndSourcePushIsMissingToTargetCalculationRuns_thenCorrectResultIsGiven()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCellsMissingSourceCarrier().build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distributionToTargetOne).isCloseTo(12f, offset(0.0f));
    }

    @Test
    public void whenThresholdPushbackToTargetIsMissingAndSourcePushIsMissingToTargetCalculationRuns_thenCorrectResultIsGiven()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F1, 1500);
        final Map<String, Integer> frequencyToCarrier_2 = new HashMap<>();
        frequencyToCarrier_2.put(InputDataBuilder.F2, 2300);
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .frequenciesAtSource(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(InputDataBuilder.F1), frequencyToCarrier)
                .distributionsAtSource(MEDIUM_LOAD_THRESHOLD, Collections.singletonList(100f), frequencyToCarrier)
                .frequenciesAtTargetOne(LOW_MEDIUM_LOAD_THRESHOLD, Collections.singletonList(InputDataBuilder.F2), frequencyToCarrier_2)
                .distributionsAtTargetOne(LOW_MEDIUM_LOAD_THRESHOLD, Collections.singletonList(100f), frequencyToCarrier_2)
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float distributionToSource = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_TARGET_ONE)
                .getDistributionInfos().get(1).getFreqDistributionList().get(1);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distributionToTargetOne).isCloseTo(2f, offset(0.0f));
        softly.assertThat(distributionToSource).isCloseTo(2f, offset(0.0f));
    }

    @Test
    public void whenSourceToTargetCalculationRunsAndMinSourceRetainedThresholdIsBreached_thenSourceRetainIsNormalizedAndSaturationIsDone()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withLBQ()
                .usersToMoveAtTargetOne(500)
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToSource = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(0);
        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distributionToSource).isCloseTo(75f, offset(0.0f));
        softly.assertThat(distributionToTargetOne).isCloseTo(25f, offset(0.0f));
        softly.assertThat(distributionToSource + distributionToTargetOne).isCloseTo(100f, offset(0f));
    }

    @Test
    public void whenLbdarStepSizeValidatedAndMinStepSizeIsBreached_thenMinThresholdIsSaturated()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();

        customizedGlobalSettings.put(THRESHOLD_MINIMUM_LBDAR_STEPSIZE, "3");

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float distributionToSource = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(0);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distributionToTargetOne).isCloseTo(13f, offset(0.0f));
        softly.assertThat(distributionToSource).isCloseTo(87f, offset(0.0f));
    }

    @Test
    public void whenDistributionInfosUpdated_andTwoThresholdsEqual_thenFrequencyDistributionIsAlignedCorrectly()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(0, 250, 250, 750, 1000))
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final List<Float> distrOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(1).getFreqDistributionList();
        final List<Float> distrTwo = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList();

        softly.assertThat(distrOne).isEqualTo(distrTwo);
    }

    @Test
    public void whenDistributionInfosUpdated_andThreeThresholdsEqual_thenFrequencyDistributionIsAlignedCorrectly()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(0, 250, 250, 250, 1000))
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final List<Float> distrOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(1).getFreqDistributionList();
        final List<Float> distrTwo = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList();
        final List<Float> distrThree = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(3).getFreqDistributionList();

        softly.assertThat(distrOne).isEqualTo(distrTwo);
        softly.assertThat(distrTwo).isEqualTo(distrThree);
    }

    @Test
    public void whenDistributionInfosUpdated_andFourThresholdsEqual_thenFrequencyDistributionIsAlignedCorrectly()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(250, 250, 250, 250, 1000))
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final List<Float> distrZero = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(0).getFreqDistributionList();
        final List<Float> distrOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(1).getFreqDistributionList();
        final List<Float> distrTwo = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList();
        final List<Float> distrThree = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(3).getFreqDistributionList();

        softly.assertThat(distrZero).isEqualTo(distrOne);
        softly.assertThat(distrOne).isEqualTo(distrTwo);
        softly.assertThat(distrTwo).isEqualTo(distrThree);
    }

    @Test
    public void whenDistributionInfosUpdated_andAllThresholdsEqual_thenFrequencyDistributionIsAlignedCorrectly()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                .subscriptionRatioAtSource(2f)
                .distrLoadsAtSource(Arrays.asList(1, 2, 3, 4, 5))
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(1000, 1000, 1000, 1000, 1000))
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final List<Float> distrZero = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(0).getFreqDistributionList();
        final List<Float> distrOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(1).getFreqDistributionList();
        final List<Float> distrTwo = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList();
        final List<Float> distrThree = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(3).getFreqDistributionList();
        final List<Float> distrFour = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(4).getFreqDistributionList();

        softly.assertThat(distrZero).isEqualTo(distrOne);
        softly.assertThat(distrOne).isEqualTo(distrTwo);
        softly.assertThat(distrTwo).isEqualTo(distrThree);
        softly.assertThat(distrThree).isEqualTo(distrFour);
    }

    @Test
    public void whenDistributionInfosUpdated_andTwoPairsOfThresholdsEqual_thenFrequencyDistributionIsAlignedCorrectly()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(0, 250, 250, 1000, 1000))
                .build();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final List<Float> distrOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(1).getFreqDistributionList();
        final List<Float> distrTwo = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList();
        final List<Float> distrThree = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(3).getFreqDistributionList();
        final List<Float> distrFour = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(4).getFreqDistributionList();

        softly.assertThat(distrOne).isEqualTo(distrTwo);
        softly.assertThat(distrThree).isEqualTo(distrFour);
    }

    @Test
    public void whenLbdarStepSizeValidatedAndMaxStepSizeIsBreached_thenMaxThresholdIsSaturated()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withCellLevelInfo()
                .bandWidthAtTargetOne(1400)
                .build();

        customizedGlobalSettings.put(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, "[{\"BW\":\"1400\", \"value\":\"1\"}, {\"BW\":\"3000\", \"value\":\"2\"}, " +
                "{\"BW\":\"5000\", \"value\":\"5\"}, {\"BW\":\"10000\", \"value\":\"20\"}, " +
                "{\"BW\":\"15000\", \"value\":\"25\"}, {\"BW\":\"20000\", \"value\":\"30\"}]");

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE).getDistributionInfos()
                .get(2).getFreqDistributionList().get(1);
        final float distributionToSource = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(0);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(distributionToTargetOne).isCloseTo(11f, offset(0.0f));
        softly.assertThat(distributionToSource).isCloseTo(89f, offset(0.0f));
    }

    @Test
    public void whenThereIsSignificantLeakageAndLeakageThresholdThirdCellIsBreached_thenLeakageIsResolved()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();

        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_THIRD_CELL, "1");

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore, lbdarDao);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float distributionToTargetTwo = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(2);
        final float distributionTargetOneToThird = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_TARGET_ONE)
                .getDistributionInfos().get(1).getFreqDistributionList().get(2);

        softly.assertThat(sumOfDistributionIsOneHundredPercent(profileChanges, CELL_SOURCE, 2)).isTrue();
        softly.assertThat(sumOfDistributionIsOneHundredPercent(profileChanges, CELL_TARGET_ONE, 1)).isTrue();
        softly.assertThat(distributionToTargetOne).isCloseTo(12f, offset(0.0f));
        softly.assertThat(distributionToTargetTwo).isCloseTo(11f, offset(0.0f));
        softly.assertThat(distributionTargetOneToThird).isCloseTo(19f, offset(0.0f));
        verify(lbdarDao, times(1)).insertLeakageCells(any(), any(), any(Set.class));
    }

    @Test
    public void whenThereIsSignificantLeakageAndLeakageThresholdLBQIsBreached_thenLeakageIsResolved()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();

        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "20");

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore, lbdarDao);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionTargetOneToThird = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_TARGET_ONE)
                .getDistributionInfos().get(1).getFreqDistributionList().get(2);

        softly.assertThat(sumOfDistributionIsOneHundredPercent(profileChanges, CELL_SOURCE, 2)).isTrue();
        softly.assertThat(sumOfDistributionIsOneHundredPercent(profileChanges, CELL_TARGET_ONE, 1)).isTrue();
        softly.assertThat(distributionTargetOneToThird).isCloseTo(19f, offset(0.0f));
        verify(lbdarDao, times(1)).insertLeakageCells(any(), any(), any(Set.class));

    }

    @Test
    public void whenSectorHasOneIndependentThirdCellAndLeakageIsResolvedBetweenTargetCellAndThirdCell_thenGoodResultIsGiven()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsOneHasNoConnectionWithSource()
                .build();
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "50");
        mockMyStores();

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore, lbdarDao);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float distributionTargetOneToThird = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_TARGET_ONE)
                .getDistributionInfos().get(1).getFreqDistributionList().get(2);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(sumOfDistributionIsOneHundredPercent(profileChanges, CELL_SOURCE, 2)).isTrue();
        softly.assertThat(sumOfDistributionIsOneHundredPercent(profileChanges, CELL_TARGET_ONE, 1)).isTrue();
        softly.assertThat(distributionToTargetOne).isCloseTo(12f, offset(0.0f));
        softly.assertThat(distributionTargetOneToThird).isCloseTo(18f, offset(0.0f));
        verify(lbdarDao, times(1)).insertLeakageCells(any(), any(), any(Set.class));
    }

    @Test
    public void whenSourceRetainTooLow_thenTargetDistributionsAreDecreasedToHaveHighSourceRetain()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCellsForSelfRetainTooLow()
                .build();

        when(cellFlmSettingsRetriever.retrieveGivenCellSettingValue(anyString(), anyInt(), anyString(), isNull())).thenReturn("0");

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

        final float distributionFromSourceToSource = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(0);
        final float distributionFromSourceToTargetOne = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(1);
        final float distributionFromSourceToTargetTwo = profileChanges.getCellToIdleModePrioAtReleases().get(CELL_SOURCE)
                .getDistributionInfos().get(2).getFreqDistributionList().get(2);

        softly.assertThat(profileChanges.getCellToIdleModePrioAtReleases()).containsKey(CELL_SOURCE);
        softly.assertThat(sumOfDistributionIsOneHundredPercent(profileChanges, CELL_SOURCE, 2)).isTrue();
        softly.assertThat(distributionFromSourceToSource).isCloseTo(12f, offset(0.0f));
        softly.assertThat(distributionFromSourceToTargetOne).isCloseTo(54f, offset(0.0f));
        softly.assertThat(distributionFromSourceToTargetTwo).isCloseTo(34f, offset(0.0f));
    }

    @Test
    public void whenSourceRetainTooLow_thenExceptionIndicatingChangeTheSameAsOriginalThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F2, 66961);
        frequencyToCarrier.put(InputDataBuilder.F1, 2050);

        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCellsSelfRetainTooLowAndProfileChangeCreatedIsSameAsOriginal()
                .withProfile()
                .distributionsAtSource(LOW_LOAD_THRESHOLD, Arrays.asList(95f, 5f), frequencyToCarrier)
                .distributionsAtTargetOne(LOW_LOAD_THRESHOLD, Arrays.asList(95f, 5f), frequencyToCarrier)
                .withLBQ()
                .usersToMoveAtTargetOne(1)
                .build();

        when(cellFlmSettingsRetriever.retrieveGivenCellSettingValue(anyString(), anyInt(), anyString(), isNull())).thenReturn("5");

        thrown.expect(LbdarException.class);
        thrown.expectMessage(containsString("Change excluded as proposed LBDAR profile change is the same as " +
                "the current profile."));

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

    }

    @Test
    public void whenSourceRetainTooLow_numberOfUsersAtSourceIsLessThanMinimumSourceRetained_thenExceptionIndicatingThresholdBreachedThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final String minimumSourceRetained = "10";

        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F2, 66961);
        frequencyToCarrier.put(InputDataBuilder.F1, 2050);

        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCellsSelfRetainTooLowAndProfileChangeCreatedIsSameAsOriginal()
                .withProfile()
                .distributionsAtSource(LOW_LOAD_THRESHOLD, Arrays.asList(95f, 5f), frequencyToCarrier)
                .distributionsAtTargetOne(LOW_LOAD_THRESHOLD, Arrays.asList(95f, 5f), frequencyToCarrier)
                .withLBQ()
                .usersToMoveAtTargetOne(1)
                .build();

        when(cellFlmSettingsRetriever.retrieveGivenCellSettingValue(anyString(), anyInt(), anyString(), isNull())).thenReturn(minimumSourceRetained);

        thrown.expect(LbdarException.class);
        thrown.expectMessage(containsString(String.format("Retained level of users at Source cell is too low, %s threshold is breached.",
                minimumSourceRetained)));

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);

    }

    @Test
    public void whenSectorHasOneIndependentThirdCellAndNoCellObjectGiven_thenExceptionIsThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsOneHasNoConnectionWithSource()
                .build();
        enrichedPolicyOutputEvent.getCellCmData().remove(CELL_TARGET_TWO);
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "50");
        mockMyStores();

        thrown.expect(LbdarException.class);

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);
    }

    @Test
    public void whenSectorHasOneIndependentThirdCellAndNoCellKpiObjectGiven_thenExceptionIsThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsOneHasNoConnectionWithSource()
                .build();
        enrichedPolicyOutputEvent.getCellKpis().remove(CELL_TARGET_TWO);
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "50");
        mockMyStores();

        thrown.expect(LbdarException.class);

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);
    }

    @Test
    public void whenSectorHasOneIndependentThirdCellAndConnectedUsersAtThirdCellIsZero_thenExceptionIsThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsOneHasNoConnectionWithSource()
                .withCellLevelInfo()
                .connectedUsersAtTargetTwo(0)
                .build();
        mockMyStores();

        thrown.expect(LbdarException.class);
        thrown.expectMessage(containsString(
                LEAKAGE_ERROR_MSG + " connected user number is zero"));

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);
    }

    @Test
    public void whenSectorHasOneIndependentThirdCellAndSubscriptionRatioAtThirdCellIsZero_thenExceptionIsThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsOneHasNoConnectionWithSource()
                .withCellLevelInfo()
                .subscriptionRatioAtTargetTwo(0)
                .build();
        mockMyStores();

        thrown.expect(LbdarException.class);
        thrown.expectMessage(containsString(
                LEAKAGE_ERROR_MSG + " subscription ratio is zero"));

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);
    }

    @Test
    public void whenLeakageHandlingIsStartedAndCollectedNewProfileIsInvalid_thenExceptionIsThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsOneHasNoConnectionWithSource()
                .build();
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "50");
        mockMyStoresWithInvalidProfile();

        thrown.expect(LbdarException.class);

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);
        objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);
    }

    @Test
    public void whenThereIsSignificantLeakageAndNoPushBackFromThirdPartyToTarget_thenExceptionThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F1, 1500);
        frequencyToCarrier.put(InputDataBuilder.F2, 2300);
        frequencyToCarrier.put(InputDataBuilder.F3, 4500);
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .withProfile()
                .distributionsAtTargetTwo(MEDIUM_LOAD_THRESHOLD, Arrays.asList(0.8f, 0.1f, 0f), frequencyToCarrier)
                .build();

        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "50");

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        thrown.expect(LbdarException.class);
        thrown.expectMessage(containsString("Change excluded as Leakage resolution not possible"));

        objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);
    }

    @Test
    public void whenThereIsSignificantLeakageAndHighPushThresholdIsBreached_thenExceptionThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();

        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "40");
        customizedGlobalSettings.put(THRESHOLD_EXISTING_HIGH_PUSH, "9");

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        thrown.expect(LbdarException.class);
        thrown.expectMessage(containsString("Change excluded due to high push to leakage cell"));

        objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);
    }

    @Test
    public void whenThereIsSignificantLeakageButLeakageCellsFailToPersist_thenExceptionThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();

        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_THIRD_CELL, "1");

        when(lbdarDao.insertLeakageCells(any(), any(), any())).thenThrow(new SQLException());
        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore, lbdarDao);

        thrown.expect(LbdarException.class);
        thrown.expectMessage(containsString(
                "Change excluded as leakage cells could not be persisted to the DB with value: [LeakageCell:: { ossId: 1, fdn: 'SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00003-1' }]"));

        profileChanges = objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);
    }

    @Test
    public void whenThereIsNullPushBackFromThirdPartyToTarget_thenExceptionThrown()
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F1, 1500);
        frequencyToCarrier.put(InputDataBuilder.F2, 9700);
        frequencyToCarrier.put(InputDataBuilder.F3, 4500);
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .withProfile()
                .distributionsAtTargetTwo(MEDIUM_LOAD_THRESHOLD, Arrays.asList(0.8f, 0.1f, 0f), frequencyToCarrier)
                .build();

        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "50");

        objectUnderTest = new ProfileChangesCalculatorImpl(customizedGlobalSettings, profileStore);

        thrown.expect(LbdarException.class);
        thrown.expectMessage(containsString("Change excluded as Leakage resolution not possible, due to the leakage cell distribution "));

        objectUnderTest.calculateProfileChanges(enrichedPolicyOutputEvent);
    }

    private void mockMyStores() {
        final IdleModePrioAtRelease profile = InputDataBuilder.buildProfileForTargetTwo();
        profile.setDistributionInfos(InputDataBuilder.buildDistributionInfosForIndependentCell());
        when(profileStore.getIdleModePrioAtRelease(InputDataBuilder.P3, InputDataBuilder.OSS_ID))
                .thenReturn(profile);

    }

    private void mockMyStoresWithInvalidProfile() {
        final Cell cell = new Cell(3L, InputDataBuilder.OSS_ID, InputDataBuilder.C3, 1500,
                InputDataBuilder.P3, "cgi3", 5000, OUTDOOR, "undefined");

        final IdleModePrioAtRelease profile = InputDataBuilder.buildProfileForTargetTwo();
        final List<DistributionInfo> distributionInfos = InputDataBuilder.buildDistributionInfosForIndependentCell();
        final List<DistributionInfo> newDistributionInfos = new ArrayList<>();
        final DistributionInfo newDistributionInfo = new DistributionInfo(MEDIUM_LOAD_THRESHOLD, Collections.emptyList(), Collections.emptyList());
        newDistributionInfos.add(distributionInfos.get(0));
        newDistributionInfos.add(distributionInfos.get(1));
        newDistributionInfos.add(newDistributionInfo);
        newDistributionInfos.add(distributionInfos.get(3));
        newDistributionInfos.add(distributionInfos.get(4));

        profile.setDistributionInfos(newDistributionInfos);
        when(profileStore.getIdleModePrioAtRelease(cell.getIdleModePrioAtReleaseRef(), cell.getOssId()))
                .thenReturn(profile);
    }

    private boolean sumOfDistributionIsOneHundredPercent(final ProfileChanges profileChanges,
            final TopologyObjectId topologyObjectId,
            final int distrLevel) {
        final Optional<IdleModePrioAtRelease> profile = Optional.ofNullable(profileChanges
                .getCellToIdleModePrioAtReleases()
                .get(topologyObjectId));

        return profile
                .map(idleModePrioAtRelease -> idleModePrioAtRelease
                        .getDistributionInfos().get(distrLevel)
                        .getFreqDistributionList()
                        .stream()
                        .reduce(0f, Float::sum).equals(100f))
                .orElse(false);
    }

    private boolean pushBackIsSetToTarget(final Float targetPushBack,
            final TopologyObjectId topologyObjectId,
            final ProfileChanges profileChanges,
            final int distrLevel) {
        return profileChanges.getCellToIdleModePrioAtReleases().containsKey(topologyObjectId) &&
                profileChanges.getCellToIdleModePrioAtReleases().get(topologyObjectId)
                        .getDistributionInfos().get(distrLevel).getFreqDistributionList().get(1).equals(targetPushBack);

    }

    private void fillUpCustomizedGlobalSettings() {
        customizedGlobalSettings.put(THRESHOLD_TARGET_PUSH_BACK, "2");
        customizedGlobalSettings.put(THRESHOLD_OVERRIDE_C_CALCULATOR, "No");
        customizedGlobalSettings.put(THRESHOLD_MINIMUM_LBDAR_STEPSIZE, "1");
        customizedGlobalSettings.put(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, "[{\"BW\":\"1400\", \"value\":\"1\"}, {\"BW\":\"3000\", \"value\":\"2\"}," +
                " {\"BW\":\"5000\", \"value\":\"15\"}, {\"BW\":\"10000\", \"value\":\"20\"}," +
                " {\"BW\":\"15000\", \"value\":\"25\"}, {\"BW\":\"20000\", \"value\":\"30\"}]");
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_THIRD_CELL, "10");
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "20");
        customizedGlobalSettings.put(THRESHOLD_EXISTING_HIGH_PUSH, "30");
    }
}
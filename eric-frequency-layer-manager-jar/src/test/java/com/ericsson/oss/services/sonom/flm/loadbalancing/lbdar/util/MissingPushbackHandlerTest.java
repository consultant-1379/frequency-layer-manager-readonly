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

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_SOURCE;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_TARGET_ONE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_EXISTING_HIGH_PUSH;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_LBQ_IMPACT;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_THIRD_CELL;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MAXIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MINIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_OVERRIDE_C_CALCULATOR;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_TARGET_PUSH_BACK;
import static org.assertj.core.data.Offset.offset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.EnrichedPolicyOutputEventBuilder;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder;

/**
 * Unit tests for {@link MissingPushbackHandler} class.
 */
public class MissingPushbackHandlerTest {

    private EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private MissingPushbackHandler objectUnderTest;
    private ProfileChangeCalculationHelper helper;
    private ProfileChangeCalculatorSettings configs;

    Map<String, String> customizedGlobalSettings = new HashMap<>();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        fillUpCustomizedGlobalSettings();
    }

    @Test
    public void whenThereIsPushBackValue_thenNoPushbackSetAtTargetCell() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();
        helper = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        configs = new ProfileChangeCalculatorSettings(customizedGlobalSettings);
        objectUnderTest = new MissingPushbackHandler(enrichedPolicyOutputEvent, configs, helper);

        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> result = objectUnderTest.replaceMissingPushbackWithDefault();

        softly.assertThat(result).isEmpty();
    }

    @Test
    public void whenThereIsNoPushBackValue_thenPushbackSetAtTargetCellAndDistributionAtSourceIsAdjustedCorrectly()
            throws LbdarException, LbdarUnexpectedException {
        final Map<String, Integer> frequencyToCarrier = new HashMap<>();
        frequencyToCarrier.put(InputDataBuilder.F1, 1500);
        frequencyToCarrier.put(InputDataBuilder.F2, 2300);
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtTargetOne(LOW_MEDIUM_LOAD_THRESHOLD, Arrays.asList(100f, 0f), frequencyToCarrier)
                .build();
        helper = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        configs = new ProfileChangeCalculatorSettings(customizedGlobalSettings);
        objectUnderTest = new MissingPushbackHandler(enrichedPolicyOutputEvent, configs, helper);

        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> result = objectUnderTest.replaceMissingPushbackWithDefault();

        softly.assertThat(result).isNotEmpty();
        softly.assertThat(result).containsKey(CELL_TARGET_ONE);
        softly.assertThat(result.get(CELL_TARGET_ONE).getDistributionInfos().get(1).getFreqDistributionList().get(1)).isCloseTo(2F, offset(0.0F));
        softly.assertThat(result).doesNotContainKey(CELL_SOURCE);

        final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> result2 = objectUnderTest.adjustDistributionAtSourceCell(result);
        softly.assertThat(result2).containsKey(CELL_SOURCE);
        softly.assertThat(result2.get(CELL_SOURCE).getDistributionInfos().get(2).getFreqDistributionList().get(0)).isCloseTo(88F, offset(0.0F));
        softly.assertThat(result2.get(CELL_SOURCE).getDistributionInfos().get(2).getFreqDistributionList().get(1)).isCloseTo(12F, offset(0.0F));
    }

    private void fillUpCustomizedGlobalSettings() {
        customizedGlobalSettings.put(THRESHOLD_TARGET_PUSH_BACK, "2");
        customizedGlobalSettings.put(THRESHOLD_OVERRIDE_C_CALCULATOR, "No");
        customizedGlobalSettings.put(THRESHOLD_MINIMUM_LBDAR_STEPSIZE, "1");
        customizedGlobalSettings.put(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE,
                "[{\"BW\":\"1400\", \"value\":\"1\"}, {\"BW\":\"3000\", \"value\":\"2\"}, {\"BW\":\"5000\", \"value\":\"5\"}, {\"BW\":\"10000\", \"value\":\"20\"}, {\"BW\":\"15000\", \"value\":\"25\"}, {\"BW\":\"20000\", \"value\":\"30\"}]");
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_THIRD_CELL, "10");
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "20");
        customizedGlobalSettings.put(THRESHOLD_EXISTING_HIGH_PUSH, "30");
    }
}

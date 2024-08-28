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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.PROFILE_ONE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.EnrichedPolicyOutputEventBuilder;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder;

/**
 * Unit tests for {@link ProfileValidator} class.
 */
public class ProfileValidatorTest {

    private EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private IdleModePrioAtRelease profile;
    private final Map<String, Integer> frequencyToCarrier = new HashMap<>();

    @Before
    public void setUp() {
        frequencyToCarrier.put(InputDataBuilder.F1, 1500);
        frequencyToCarrier.put(InputDataBuilder.F2, 2300);
    }

    @Test
    public void whenProfileContainsValidData_thenValidationWillPass() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();

        profile = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE).getIdleModePrioAtReleaseCopy();
        assertThat(ProfileValidator.isValid(profile)).isTrue();
    }

    @Test
    public void whenProfileIsNotConsistentWithLessThan5ThresholdValue_thenValidationWillBroke() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(100, 300, 400, 500))
                .build();

        profile = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE).getIdleModePrioAtReleaseCopy();
        assertThat(ProfileValidator.isValid(profile)).isFalse();
    }

    @Test
    public void whenProfileIsNotConsistentWithNonPositiveThresholdValue_thenValidationWillBroke() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(-1, 250, 450, 700, 900))
                .build();

        profile = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE).getIdleModePrioAtReleaseCopy();
        assertThat(ProfileValidator.isValid(profile)).isFalse();

    }

    @Test
    public void whenProfileIsNotConsistentWithNotIncreasingThresholdLimits_thenValidationWillBroke() {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .thresholdLimitsAtSource(Arrays.asList(0, 250, 450, 400, 900))
                .build();

        profile = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE).getIdleModePrioAtReleaseCopy();
        assertThat(ProfileValidator.isValid(profile)).isFalse();
    }

    @Test
    public void whenDistributionValuesAreGreaterThanOneHundredPercent_thenValidationWillBroke() {

        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withProfile()
                .distributionsAtSource(LOW_MEDIUM_LOAD_THRESHOLD, Arrays.asList(21F, 80F), frequencyToCarrier)
                .build();

        profile = enrichedPolicyOutputEvent.getProfiles().get(PROFILE_ONE).getIdleModePrioAtReleaseCopy();
        assertThat(ProfileValidator.isValid(profile)).isFalse();
    }
}

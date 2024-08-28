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

import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_EXISTING_HIGH_PUSH;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_LBQ_IMPACT;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_THIRD_CELL;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MAXIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MINIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_OVERRIDE_C_CALCULATOR;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_TARGET_PUSH_BACK;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit tests for {@link ProfileChangeCalculatorSettings} class.
 */
public class ProfileChangeCalculatorSettingsTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private final static String TEST_EXECUTION_ID = "FLM_TEST_123123123";
    private final static Long TEST_SECTOR_ID = 123123123L;

    private final ProfileChangeCalculatorSettings objectUnderTest = new ProfileChangeCalculatorSettings(getCustomizedGlobalSettings());

    private Map<String, String> getCustomizedGlobalSettings() {
        final Map<String, String> customizedGlobalSettings = new HashMap<>();
        customizedGlobalSettings.put(THRESHOLD_TARGET_PUSH_BACK, "2");
        customizedGlobalSettings.put(THRESHOLD_OVERRIDE_C_CALCULATOR, "No");
        customizedGlobalSettings.put(THRESHOLD_MINIMUM_LBDAR_STEPSIZE, "1");
        customizedGlobalSettings.put(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE,
                "[{\"BW\":\"1400\", \"value\":\"1\"}, {\"BW\":\"3000\", \"value\":\"2\"}, {\"BW\":\"5000\", \"value\":\"5\"}, {\"BW\":\"10000\", \"value\":\"20\"}, {\"BW\":\"15000\", \"value\":\"25\"}, {\"BW\":\"20000\", \"value\":\"30\"}]");
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_THIRD_CELL, "10");
        customizedGlobalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT, "20");
        customizedGlobalSettings.put(THRESHOLD_EXISTING_HIGH_PUSH, "30");
        return customizedGlobalSettings;
    }

    @Test
    public void whenLeakageResolutionInvokedByLBQImpact_thenLeakageThresholdsAreBreached() {
        final long targetUsersFromLbq = 50;
        final long targetUsers = 550;
        final long thirdPartyUsers = 1000;
        final long newTargetUsers = 520;

        softly.assertThat(objectUnderTest.leakageThresholdsAreBreached(targetUsersFromLbq, targetUsers, thirdPartyUsers, newTargetUsers,
                TEST_EXECUTION_ID, TEST_SECTOR_ID))
                .isTrue();
    }

    @Test
    public void whenLeakageResolutionInvokedBy3rdPartyCells_thenLeakageThresholdsAreBreached() {
        final long targetUsersFromLbq = 1600;
        final long targetUsers = 550;
        final long thirdPartyUsers = 1000;
        final long newTargetUsers = 250;

        softly.assertThat(objectUnderTest.leakageThresholdsAreBreached(targetUsersFromLbq, targetUsers, thirdPartyUsers, newTargetUsers,
                TEST_EXECUTION_ID, TEST_SECTOR_ID))
                .isTrue();
    }

    @Test
    public void whenLeakageResolutionNotInvoked_thenLeakageThresholdsAreNotBreached() {
        final long targetUsersFromLbq = 50;
        final long targetUsers = 550;
        final long thirdPartyUsers = 1000;
        final long newTargetUsers = 540;

        softly.assertThat(objectUnderTest.leakageThresholdsAreBreached(targetUsersFromLbq, targetUsers, thirdPartyUsers, newTargetUsers,
                TEST_EXECUTION_ID, TEST_SECTOR_ID))
                .isFalse();
    }
}

/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.pa.api;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.service.api.pa.PASettings;

/**
 * Unit tests for {@link PASettings} class.
 */
public class PASettingsTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private static final String failMessage = "Unexpected value for '%s'. Please consult CA group before changing this setting";

    @Test
    public void enforcePASettings() {
        softly.assertThat(PASettings.NUMBER_OF_PA_EXECUTIONS)
                .as(failMessage, "PASettings.NUMBER_OF_PA_EXECUTIONS")
                .isEqualTo(3);

        softly.assertThat(PASettings.INITIAL_PA_WINDOW_OFFSET_TIME_IN_MINUTES)
                .as(failMessage, "PASettings.INITIAL_PA_WINDOW_OFFSET_TIME_IN_MINUTES")
                .isEqualTo(60);

        softly.assertThat(PASettings.PA_WINDOW_DURATION_IN_MINUTES)
                .as(failMessage, "PASettings.PA_WINDOW_DURATION_IN_MINUTES")
                .isEqualTo(360);

        softly.assertThat(PASettings.PA_EXECUTION_OFFSET_TIME_IN_MINUTES)
                .as(failMessage, "PASettings.PA_EXECUTION_OFFSET_TIME_IN_MINUTES")
                .isEqualTo(120);

        softly.assertThat(PASettings.PA_EXECUTION_MISFIRE_THRESHOLD_IN_MINUTES)
                .as(failMessage, "PASettings.PA_EXECUTION_MISFIRE_THRESHOLD_IN_MINUTES")
                .isEqualTo(120);
    }
}
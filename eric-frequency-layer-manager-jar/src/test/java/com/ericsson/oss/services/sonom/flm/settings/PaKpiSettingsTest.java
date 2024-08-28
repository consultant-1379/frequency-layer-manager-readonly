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

package com.ericsson.oss.services.sonom.flm.settings;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit tests for {@link PaKpiSettings} class.
 */
public class PaKpiSettingsTest {

    private final PaKpiSettings objectUnderTest = new PaKpiSettings();

    private String invalidSettings;

    @Test
    public void whenParsingValidPaKpiSettings_thenParsingIsOk() {
        assertThat(objectUnderTest.getDataSize()).isZero();
        final String validSettings = "{\"cellHandoverSuccessRate\":{\"enableKPI\":true,\"confidenceInterval\":\"99\"," +
                "\"relevanceThreshold\":\"99.90\"}}";

        putSettingsAndAssertDataSize(objectUnderTest, validSettings);
        final PaKpiSettings.Data data = objectUnderTest.get(PaKpiSettings.PaKpi.CELLHOSR);
        assertThat(data.enableKpi).isTrue();
        assertThat(data.confidenceInterval).isEqualTo(99d);
        assertThat(data.relevanceThreshold).isEqualTo(99.90d);
    }

    @Test
    public void whenParsingInvalidKpiName_thenKpiIsNotParsed() {
        invalidSettings = "{\"invalidKpiName\":{\"enableKPI\":true,\"confidenceInterval\":\"99\",\"relevanceThreshold\":\"99.90\"}}";

        objectUnderTest.put(invalidSettings);
        assertThat(objectUnderTest.getDataSize()).isZero();
    }

    @Test
    public void whenParsingInvalidEnableKpiSettingName_thenSettingNameIsNull() {
        invalidSettings = "{\"cellHandoverSuccessRate\":{\"invalidName\":true,\"confidenceInterval\":\"99\",\"relevanceThreshold\":\"99.90\"}}";

        putSettingsAndAssertDataSize(objectUnderTest, invalidSettings);
        assertThat(objectUnderTest.get(PaKpiSettings.PaKpi.CELLHOSR).enableKpi).isNull();
    }

    @Test
    public void whenParsingInvalidConfidenceIntervalSettingName_thenSettingNameIsNull() {
        invalidSettings = "{\"cellHandoverSuccessRate\":{\"enableKpi\":true,\"invalidName\":\"99\",\"relevanceThreshold\":\"99.90\"}}";

        putSettingsAndAssertDataSize(objectUnderTest, invalidSettings);
        assertThat(objectUnderTest.get(PaKpiSettings.PaKpi.CELLHOSR).confidenceInterval).isNull();
    }

    @Test
    public void whenParsingInvalidRelevanceThresholdSettingName_thenSettingNameIsNull() {
        invalidSettings = "{\"cellHandoverSuccessRate\":{\"enableKpi\":true,\"confidenceInterval\":\"99\",\"invalidName\":\"99.90\"}}";

        putSettingsAndAssertDataSize(objectUnderTest, invalidSettings);
        assertThat(objectUnderTest.get(PaKpiSettings.PaKpi.CELLHOSR).relevanceThreshold).isNull();
    }

    private void putSettingsAndAssertDataSize(final PaKpiSettings paKpiSettings, final String settings) {
        paKpiSettings.put(settings);
        assertThat(paKpiSettings.getDataSize()).isEqualTo(1);
    }
}

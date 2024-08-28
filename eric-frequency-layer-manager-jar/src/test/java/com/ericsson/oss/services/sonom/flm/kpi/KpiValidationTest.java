/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.kpi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState;

/**
 * Unit tests for {@link KpiValidation} class.
 */
public class KpiValidationTest {

    public static final String EXECUTION_ID = "1";
    private static final String CALCULATION_ID = "1";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final KpiValidation objectUnderTest = new KpiValidation();

    @Test
    public void whenKpiCalculationStateIsFailed_thenFlmAlgorithmExceptionThrown() throws FlmAlgorithmException {
        thrown.expectMessage("KPI calculation failed: FAILED for calculation ID: 1");
        thrown.expect(FlmAlgorithmException.class);

        objectUnderTest.validateKpiState(KpiCalculationState.FAILED, CALCULATION_ID);
    }

    @Test
    public void whenKpiCalculationStateIsLost_thenFlmAlgorithmExceptionThrown() throws FlmAlgorithmException {
        thrown.expectMessage("KPI calculation failed: LOST for calculation ID: 1");
        thrown.expect(FlmAlgorithmException.class);

        objectUnderTest.validateKpiState(KpiCalculationState.LOST, CALCULATION_ID);
    }

    @Test
    public void whenValidCalculationId_thenReturnFalse() {
        final boolean result = objectUnderTest.inValidKpiCalculationId(EXECUTION_ID, CALCULATION_ID);

        assertThat(result).isFalse();
    }

    @Test
    public void whenInValidCalculationId_thenReturnTrue() {
        final boolean result = objectUnderTest.inValidKpiCalculationId(EXECUTION_ID, null);

        assertThat(result).isTrue();
    }

    @Test
    public void whenGlobalSettingsParametersContainsEntryWithJson_thenReturnMapWithExtractedKeyValues() {
        final Map<String, String> gloalSettings = Collections.singletonMap("paKpiSettings",
                "{\"cellHandoverSuccessRate\": { \"enableKPI\": true, \"confidenceInterval\": \"99\", \"relevanceThreshold\": \"99.90\" }, \"initialAndAddedERabEstabSrHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\" }, \"ulPuschSinrHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"15\" }}");
        final Map<String, String> results = KpiValidation.createGlobalSettingsParameters(gloalSettings);
        assertThat(results).hasSize(8);
        assertThat(results).contains(
                entry("param.cell_handover_success_rate_relevance_threshold", "99.90"),
                entry("param.cell_handover_success_rate_enable_kp_i", "true"),
                entry("param.cell_handover_success_rate_confidence_interval", "99"),
                entry("param.initial_and_added_e_rab_estab_sr_hourly_enable_kp_i", "true"),
                entry("param.initial_and_added_e_rab_estab_sr_hourly_confidence_interval", "97.50"),
                entry("param.ul_pusch_sinr_hourly_enable_kp_i", "true"),
                entry("param.ul_pusch_sinr_hourly_confidence_interval", "97.50"),
                entry("param.ul_pusch_sinr_hourly_relevance_threshold", "15"));
    }
}
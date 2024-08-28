/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2018 - 2022
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
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;
import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationStateHandler;
import com.ericsson.oss.services.sonom.kpi.service.rest.api.v1.CalculationStateResponse;

import io.github.resilience4j.retry.RetryConfig;

/**
 * Unit tests for {@link KpiRequestRetry} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class KpiRequestRetryTest {

    private static final String CALCULATION_ID = "1234";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private KpiCalculationStateHandler kpiCalculationStateHandler;

    private KpiRequestRetry objectUnderTest;

    static {
        // speed up tests (doesn't work for PIT, need to upgrade PIT version to support environment tag in POM files)
        System.setProperty("WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_STARTED_STATE", "1");
        System.setProperty("WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_IN_PROGRESS_STATE", "1");
    }

    @Before
    public void setUp() {
        objectUnderTest = new KpiRequestRetry(new KpiActionForState(new KpiValidation()));
    }

    @Test
    public void whenGetRetryConfigDefinitionForKpiRecalculationRetry_thenFunctionParametersUsedToCreateRetry() {
        final RetryConfig retryConfig = objectUnderTest.getRetryConfigDefinitionForKpiRecalculationRetry(2, 1);

        softly.assertThat(retryConfig.getResultPredicate()).accepts(true);
        softly.assertThat(retryConfig.getMaxAttempts()).isEqualTo(2);
        softly.assertThat(retryConfig.getIntervalFunction().apply(1)).isEqualTo(1000L);
        softly.assertThat(retryConfig.getExceptionPredicate()).accepts(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_LOST));
    }

    @Test
    public void whenGetKpiRecalculationRequiredExceptionPredicate_thenFunctionParametersUsedToCreateRetry() {
        final List<FlmServiceExceptionCode> flmServiceExceptionCodes = Arrays.stream(FlmServiceExceptionCode.values())
                .collect(Collectors.toList());
        final Predicate<Throwable> kpiRecalculationRequiredExceptionPredicate = objectUnderTest.getKpiRecalculationRequiredExceptionPredicate();

        flmServiceExceptionCodes.remove(FlmServiceExceptionCode.KPI_CALCULATION_LOST);

        softly.assertThat(kpiRecalculationRequiredExceptionPredicate).accepts(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_LOST));

        for (final FlmServiceExceptionCode notAcceptedCode : flmServiceExceptionCodes) {
            softly.assertThat(kpiRecalculationRequiredExceptionPredicate)
                    .rejects(new FlmAlgorithmException(notAcceptedCode));
        }

        // Required for PIT to test non FlmAlgorithmException types for mutation testing
        softly.assertThat(kpiRecalculationRequiredExceptionPredicate).rejects(new IllegalArgumentException());
    }

    @Test
    public void whenGetKpiCalculationStateWithNullCalculationId_thenThrowFlmAlgorithmException() throws FlmAlgorithmException {
        thrown.expectMessage(FlmServiceExceptionCode.KPI_CALCULATION_ERROR.getErrorMessage());
        objectUnderTest.getKpiCalculationState(kpiCalculationStateHandler, null);
    }

    @Test
    public void whenGetKpiCalculationStateWithStartedState_thenLoopUntilFinished() throws FlmAlgorithmException {
        mockingForKpiCalculationState(KpiCalculationState.STARTED, KpiCalculationState.IN_PROGRESS, KpiCalculationState.FINISHED);
        final KpiCalculationState resultKpiCalculationState = objectUnderTest.getKpiCalculationState(kpiCalculationStateHandler, CALCULATION_ID);

        assertThat(resultKpiCalculationState).isEqualTo(KpiCalculationState.FINISHED);
    }

    @Test
    public void whenGetKpiCalculationStateWithUnsupportedState_thenIllegalArgumentException() throws FlmAlgorithmException {

        final CalculationStateResponse calculationStateResponse = new CalculationStateResponse();
        calculationStateResponse.setCalculationId(CALCULATION_ID);
        calculationStateResponse.setStatus(""); // unknown status returned

        when(kpiCalculationStateHandler.getKpiCalculationStateResponse(anyString())).thenReturn(calculationStateResponse);

        thrown.expectMessage(
                "java.lang.IllegalArgumentException: No enum constant com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState.");
        thrown.expect(FlmAlgorithmException.class);
        objectUnderTest.getKpiCalculationState(kpiCalculationStateHandler, CALCULATION_ID);
    }

    @Test
    public void whenGetKpiCalculationStateWithFailedState_thenFlmAlgorithmExceptionWithCalculationFailedMessageThrown() {
        mockingForKpiCalculationState(KpiCalculationState.STARTED, KpiCalculationState.FAILED);
        try {
            objectUnderTest.getKpiCalculationState(kpiCalculationStateHandler, CALCULATION_ID);
            shouldHaveThrown(FlmAlgorithmException.class);
        } catch (final FlmAlgorithmException e1) {
            softly.assertThat(e1.getErrorCode()).isEqualTo(FlmServiceExceptionCode.KPI_CALCULATION_ERROR.getErrorCode());
            softly.assertThat(e1.getErrorMessage()).isEqualTo(FlmServiceExceptionCode.KPI_CALCULATION_ERROR.getErrorMessage());
            softly.assertThat(e1).hasMessage("KPI calculation failed: FAILED for calculation ID: 1234");
        }
    }

    @Test
    public void whenGetKpiCalculationStateWithLostState_thenFlmAlgorithmExceptionWithCalculationLostMessageThrown() {
        mockingForKpiCalculationState(KpiCalculationState.STARTED, KpiCalculationState.LOST);
        try {
            objectUnderTest.getKpiCalculationState(kpiCalculationStateHandler, CALCULATION_ID);
            shouldHaveThrown(FlmAlgorithmException.class);
        } catch (final FlmAlgorithmException e1) {
            softly.assertThat(e1.getErrorCode()).isEqualTo(FlmServiceExceptionCode.KPI_CALCULATION_LOST.getErrorCode());
            softly.assertThat(e1.getErrorMessage()).isEqualTo(FlmServiceExceptionCode.KPI_CALCULATION_LOST.getErrorMessage());
            softly.assertThat(e1).hasMessage("KPI calculation failed: LOST for calculation ID: 1234");
        }
    }

    private void mockingForKpiCalculationState(final KpiCalculationState... kpiCalculationStates) {

        final List<CalculationStateResponse> calculationStateResponses = new ArrayList<>(kpiCalculationStates.length);

        for (final KpiCalculationState kpiCalculationState : kpiCalculationStates) {
            final CalculationStateResponse calculationStateResponse = new CalculationStateResponse();
            calculationStateResponse.setCalculationId(CALCULATION_ID);
            calculationStateResponse.setStatus(kpiCalculationState.name());

            calculationStateResponses.add(calculationStateResponse);
        }

        when(kpiCalculationStateHandler.getKpiCalculationStateResponse(anyString()))
                .thenAnswer(AdditionalAnswers.returnsElementsOf(calculationStateResponses));
    }
}
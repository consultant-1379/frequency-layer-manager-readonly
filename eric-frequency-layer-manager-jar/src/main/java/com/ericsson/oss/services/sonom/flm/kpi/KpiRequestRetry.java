/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.kpi;

import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.KPI_CALCULATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.KPI_CALCULATION_LOST;
import static com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState.FINISHED;
import static com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState.STARTED;
import static io.github.resilience4j.retry.RetryConfig.custom;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationStateHandler;
import com.ericsson.oss.services.sonom.kpi.service.rest.api.v1.CalculationStateResponse;

import io.github.resilience4j.retry.RetryConfig;

/**
 * Contains functionality to manage Kpi Request Retries.
 */
public class KpiRequestRetry {

    private static final Logger LOGGER = LoggerFactory.getLogger(KpiRequestRetry.class);

    private static final int[] RETRY_KPI_CALCULATOR_ERROR_CODES = { KPI_CALCULATION_LOST.getErrorCode() };

    private final KpiActionForState actionForState;
    private KpiCalculationState currentState = STARTED;

    public KpiRequestRetry(final KpiActionForState actionForState) {
        this.actionForState = actionForState;
    }

    public Predicate<Throwable> getKpiRecalculationRequiredExceptionPredicate() {

        return (exception) -> {
            if ((exception instanceof FlmAlgorithmException)) {
                final FlmAlgorithmException flmAlgorithmException = (FlmAlgorithmException) exception;
                final boolean retry = IntStream.of(RETRY_KPI_CALCULATOR_ERROR_CODES)
                        .anyMatch(x -> x == flmAlgorithmException.getErrorCode()); // array contains code

                if (retry) {
                    LOGGER.warn("{} ({}). Resending calculation.",
                            flmAlgorithmException.getLocalizedMessage(), flmAlgorithmException.getErrorMessage());
                    return true;
                }
            }
            return false; // It isn't a FlmAlgorithmException
        };
    }

    /**
     * Creates the config for KPI Calculator Retries based on the timing values passed in. The created config will retry on exceptions that are
     * allowed to be retried for KPI Calculator.
     *
     * @param maxRetryAttempts
     *            The max number of retry attempts for this retry.
     * @param retryWaitDuration
     *            The max retry wait duration for this retry.
     * @return RetryConfig that will retry on exceptions that are allowed to be retried for KPI Calculator.
     */
    public RetryConfig getRetryConfigDefinitionForKpiRecalculationRetry(final int maxRetryAttempts, final int retryWaitDuration) {
        return custom()
                .retryOnResult(s -> s.equals(true))
                .retryOnException(getKpiRecalculationRequiredExceptionPredicate())
                .maxAttempts(maxRetryAttempts)
                .waitDuration(Duration.ofSeconds(retryWaitDuration))
                .build();
    }

    /**
     * Gets KPI Calculation State with retry configuration provided.
     *
     * @param kpiCalculationStateHandler
     *            An instance {@link KpiCalculationStateHandler} to use to make the request.
     * @param calculationId
     *            The calculation ID to check the state for.
     * @return The current state of the calculation. @throws FlmAlgorithmException If calculation lost or failed.
     * @throws FlmAlgorithmException
     *             If calculation lost or failed or {@link InterruptedException} occurs.
     */
    public KpiCalculationState getKpiCalculationState(final KpiCalculationStateHandler kpiCalculationStateHandler, final String calculationId)
            throws FlmAlgorithmException {

        do {
            try {
                LOGGER.debug("Getting calculation state for id: {}", calculationId);
                final String status = getStatus(kpiCalculationStateHandler, calculationId);
                LOGGER.debug("Retrieved calculation state for id: {}, status: {}", calculationId, status);
                currentState = KpiCalculationState.valueOf(status); // IllegalArgumentException if not parsed.

                actionForState.determineActionForState(currentState, calculationId);
            } catch (final InterruptedException | IllegalArgumentException e1) { // NOSONAR Exception suitably logged
                LOGGER.warn("Unrecoverable Exception Occurred", e1);
                throw new FlmAlgorithmException(KPI_CALCULATION_ERROR, e1);
            }
        } while (isTerminalState());

        return currentState;
    }

    private boolean isTerminalState() {
        return currentState != FINISHED; // other terminal states result in exception
    }

    private String getStatus(final KpiCalculationStateHandler kpiCalculationStateHandler, final String calculationId) throws FlmAlgorithmException {
        try {
            if (calculationId == null) {
                LOGGER.error("Cannot send request for KPI Calculation State with null calculationId");
                throw new FlmAlgorithmException(KPI_CALCULATION_ERROR);
            }
            final CalculationStateResponse kpiCalculationStateResponse = kpiCalculationStateHandler.getKpiCalculationStateResponse(calculationId);
            return kpiCalculationStateResponse.getStatus();
        } catch (Exception e1) {
            throw new FlmAlgorithmException(KPI_CALCULATION_ERROR, e1);
        }
    }

}

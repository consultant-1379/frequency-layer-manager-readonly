/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020
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

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState.IN_PROGRESS;
import static com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState.STARTED;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState;

/**
 * Class to decide what to do considering the KPI's current state. I.e. wait or throw {@link FlmAlgorithmException}
 */
public class KpiActionForState {

    private static final Logger LOGGER = LoggerFactory.getLogger(KpiActionForState.class);
    private static final int WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_STARTED_STATE = Integer.parseInt(getEnvironmentValue(
            "WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_STARTED_STATE", "5"));
    private static final int WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_IN_PROGRESS_STATE = Integer.parseInt(getEnvironmentValue(
            "WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_IN_PROGRESS_STATE", "5"));

    private final KpiValidation kpiValidation;

    public KpiActionForState(final KpiValidation kpiValidation) {
        this.kpiValidation = kpiValidation;
    }

    /**
     * Determines what action to take for a given state of a KPI that isn't FINISHED state.
     *
     * NOTE: No JUnits since it only logs and sleeps.
     *
     * @param currentState
     *            the current state of the KPI.
     * @param calculationId
     *            the ID of the current KPI for logging purposes.
     * @throws FlmAlgorithmException
     *             When KPI is in LOST or FAILED state.
     * @throws InterruptedException
     *             If the thread got interrupted somehow.
     */
    public void determineActionForState(final KpiCalculationState currentState, final String calculationId) throws FlmAlgorithmException,
            InterruptedException {

        kpiValidation.validateKpiState(currentState, calculationId);

        if (STARTED == currentState) {
            LOGGER.info("{} state, waiting {} seconds for KPI calculation to start for id: {}", currentState,
                    WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_STARTED_STATE, calculationId);
            TimeUnit.SECONDS.sleep(WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_STARTED_STATE);
        } else if (IN_PROGRESS == currentState) {
            LOGGER.info("{} state, waiting {} seconds for KPI calculation to finish for id: {}", currentState,
                    WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_IN_PROGRESS_STATE, calculationId);
            TimeUnit.SECONDS.sleep(WAIT_KPI_CALCULATION_STATE_SECONDS_FOR_IN_PROGRESS_STATE);
        }
    }
}

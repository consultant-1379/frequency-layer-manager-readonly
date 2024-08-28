/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.api.executions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ENUM defining the possible states of an execution. Within a {@link Execution}, there are 8 phases currently: <br>
 * 1. Waiting if previous execution of same ID has PA executions running 2. CHECK if non-settings based KPI processing required
 * 3. Non-settings based KPI Processing Groups (1-7) 4. SETTINGS-Processing, 5. KPI-Processing
 * Groups (8-16) , 6. Optimization-Processing 7. Cell-Settings-History, 8. Finished. <br>
 * 'Waiting' phase has the following States: WAITING <br>
 * 'CHECKING' phase has the following States: CHECK_IF_NON_SETTINGS_BASED_KPI_CALCULATIONS_REQUIRED <br>
 * 'Non-settings based KPI-Processing' phase has the following States: KPI_PROCESSING_GROUP_1-7 <br>
 * 'SETTINGS-Processing' phase has the following States: SETTINGS_PROCESSING <br>
 * 'KPI-Processing' phase has the following States: KPI_PROCESSING_GROUP_8-14 <br>
 * 'Optimization-Processing' phase has the following State: OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, and
 * OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE <br>
 * 'Cell-Settings-History' phase has the following State: CELL_SETTINGS_HISTORY <br>
 * 'Finished' phase has one of the following States: SUCCEEDED, PARTIALLY_SUCCEEDED, FAILED.
 */
public enum ExecutionState {
    WAITING(Boolean.FALSE),
    CHECK_IF_NON_SETTINGS_KPI_CALCULATIONS_REQUIRED(Boolean.FALSE),
    KPI_PROCESSING_GROUP_1(Boolean.FALSE),
    KPI_PROCESSING_GROUP_2(Boolean.FALSE),
    KPI_PROCESSING_GROUP_3(Boolean.FALSE),
    KPI_PROCESSING_GROUP_4(Boolean.FALSE),
    KPI_PROCESSING_GROUP_5(Boolean.FALSE),
    KPI_PROCESSING_GROUP_6(Boolean.FALSE),
    KPI_PROCESSING_GROUP_7(Boolean.FALSE),
    SETTINGS_PROCESSING(Boolean.FALSE),
    SETTINGS_PROCESSING_SUCCEEDED(Boolean.FALSE),
    KPI_PROCESSING_GROUP_8(Boolean.FALSE),
    KPI_PROCESSING_GROUP_9(Boolean.FALSE),
    KPI_PROCESSING_GROUP_10(Boolean.FALSE),
    KPI_PROCESSING_GROUP_11(Boolean.FALSE),
    KPI_PROCESSING_GROUP_12(Boolean.FALSE),
    KPI_PROCESSING_GROUP_13(Boolean.FALSE),
    KPI_PROCESSING_GROUP_14(Boolean.FALSE),
    KPI_PROCESSING_GROUP_15(Boolean.FALSE),
    KPI_PROCESSING_GROUP_16(Boolean.FALSE),
    KPI_PROCESSING_SUCCEEDED(Boolean.FALSE),
    OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE(Boolean.FALSE),
    OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE(Boolean.FALSE),
    OPTIMIZATION_PROCESSING_SUCCEEDED(Boolean.FALSE),
    LOAD_BALANCING(Boolean.FALSE),
    CELL_SETTINGS_HISTORY(Boolean.FALSE),
    CLEAN_CELL_SETTINGS(Boolean.FALSE),
    SUCCEEDED(Boolean.TRUE),
    PARTIALLY_SUCCEEDED(Boolean.TRUE),
    FAILED(Boolean.TRUE);

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionState.class);
    private final boolean isFinal;

    ExecutionState(final boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * This will return next state in the Execution.
     *
     * @param isFullExecution
     *            if true execute all algorithm stages 1. CHECK if non-settings based KPI calculation has been started 2. Non-settings based KPI
     *            Processing(if first execution for the day) 3. SETTINGS-Processing, 4. KPI-Processing, 5. Optimization-Processing 6.
     *            Cell-Settings-History, 7. Finished. if false then execute stages related to KPI calculation only 1. CHECK non-settings based KPI
     *            Processing 2. Non-settings based KPI Processing 3. SETTINGS-Processing, 4. KPI-Processing, 5. Cell-Settings-History, 6. Finished.
     * @param state
     *            state of the Execution
     * @return next state of the Execution
     */
    public static ExecutionState getNextState(final boolean isFullExecution, final ExecutionState state) {
        switch (state) {
            case WAITING:
                return CHECK_IF_NON_SETTINGS_KPI_CALCULATIONS_REQUIRED;
            case CHECK_IF_NON_SETTINGS_KPI_CALCULATIONS_REQUIRED:
                return KPI_PROCESSING_GROUP_1;
            case KPI_PROCESSING_GROUP_1:
                return KPI_PROCESSING_GROUP_2;
            case KPI_PROCESSING_GROUP_2:
                return KPI_PROCESSING_GROUP_3;
            case KPI_PROCESSING_GROUP_3:
                return KPI_PROCESSING_GROUP_4;
            case KPI_PROCESSING_GROUP_4:
                return KPI_PROCESSING_GROUP_5;
            case KPI_PROCESSING_GROUP_5:
                return KPI_PROCESSING_GROUP_6;
            case KPI_PROCESSING_GROUP_6:
                return KPI_PROCESSING_GROUP_7;
            case KPI_PROCESSING_GROUP_7:
                return SETTINGS_PROCESSING;
            case SETTINGS_PROCESSING:
                return SETTINGS_PROCESSING_SUCCEEDED;
            case SETTINGS_PROCESSING_SUCCEEDED:
                return KPI_PROCESSING_GROUP_8;
            case KPI_PROCESSING_GROUP_8:
                return KPI_PROCESSING_GROUP_9;
            case KPI_PROCESSING_GROUP_9:
                return KPI_PROCESSING_GROUP_10;
            case KPI_PROCESSING_GROUP_10:
                return KPI_PROCESSING_GROUP_11;
            case KPI_PROCESSING_GROUP_11:
                return KPI_PROCESSING_GROUP_12;
            case KPI_PROCESSING_GROUP_12:
                return KPI_PROCESSING_GROUP_13;
            case KPI_PROCESSING_GROUP_13:
                return KPI_PROCESSING_GROUP_14;
            case KPI_PROCESSING_GROUP_14:
                return KPI_PROCESSING_GROUP_15;
            case KPI_PROCESSING_GROUP_15:
                return KPI_PROCESSING_GROUP_16;
            case KPI_PROCESSING_GROUP_16:
                return KPI_PROCESSING_SUCCEEDED;
            case KPI_PROCESSING_SUCCEEDED:
                if (isFullExecution) {
                    return OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE;
                } else {
                    return CELL_SETTINGS_HISTORY;
                }
            case OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE:
                return OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE;
            case OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE:
                return OPTIMIZATION_PROCESSING_SUCCEEDED;
            case OPTIMIZATION_PROCESSING_SUCCEEDED:
                return LOAD_BALANCING;
            case LOAD_BALANCING:
                return CELL_SETTINGS_HISTORY;
            case CELL_SETTINGS_HISTORY:
                return CLEAN_CELL_SETTINGS;
            case CLEAN_CELL_SETTINGS:
                return SUCCEEDED;
            default:
                LOGGER.error("Execution state not known: {}", state);
                return null;
        }
    }

    /**
     * This will return initial state in the Execution.
     *
     * @return Initial state of the Execution
     */
    public static ExecutionState getInitialState() {
        return WAITING;
    }

    /**
     * This will return first state in the Execution.
     *
     * @return First state of the Execution
     */
    public static ExecutionState getFirstState() {
        return CHECK_IF_NON_SETTINGS_KPI_CALCULATIONS_REQUIRED;
    }

    /**
     * This boolean flag indicates an end state in the Execution.
     *
     * @return {@link boolean} state of the Execution
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * This will return the restart-able execution states.
     *
     * @return Restart-able execution states
     */
    public static ExecutionState[] getRestartableExecutionStates() {
        return new ExecutionState[] {
                ExecutionState.WAITING, ExecutionState.CHECK_IF_NON_SETTINGS_KPI_CALCULATIONS_REQUIRED,
                ExecutionState.SETTINGS_PROCESSING, ExecutionState.SETTINGS_PROCESSING_SUCCEEDED,
                ExecutionState.KPI_PROCESSING_GROUP_1, ExecutionState.KPI_PROCESSING_GROUP_2,
                ExecutionState.KPI_PROCESSING_GROUP_3, ExecutionState.KPI_PROCESSING_GROUP_4,
                ExecutionState.KPI_PROCESSING_GROUP_5, ExecutionState.KPI_PROCESSING_GROUP_6,
                ExecutionState.KPI_PROCESSING_GROUP_7, ExecutionState.KPI_PROCESSING_GROUP_8,
                ExecutionState.KPI_PROCESSING_GROUP_9, ExecutionState.KPI_PROCESSING_GROUP_10,
                ExecutionState.KPI_PROCESSING_GROUP_11, ExecutionState.KPI_PROCESSING_GROUP_12,
                ExecutionState.KPI_PROCESSING_GROUP_13, ExecutionState.KPI_PROCESSING_GROUP_14,
                ExecutionState.KPI_PROCESSING_GROUP_15, ExecutionState.KPI_PROCESSING_GROUP_16,
                ExecutionState.KPI_PROCESSING_SUCCEEDED, ExecutionState.OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE,
                ExecutionState.OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE,
                ExecutionState.OPTIMIZATION_PROCESSING_SUCCEEDED, ExecutionState.LOAD_BALANCING,
                ExecutionState.CELL_SETTINGS_HISTORY, ExecutionState.CLEAN_CELL_SETTINGS };
    }

    /**
     * This will return whether a given {@link ExecutionState} is in the non-settings based KPI calculation states(KPI_PROCESSING_GROUP_1 to 7).
     *
     * @param executionState
     *            State to check for non-settings based KPI calculations.
     * @return whether state is in between the KPI processing groups 1 to 7.
     */
    public static boolean isStateAssociatedWithNonSettingsBasedKpiCalculations(final ExecutionState executionState) {
        return executionState.ordinal() >= ExecutionState.KPI_PROCESSING_GROUP_1.ordinal()
                && executionState.ordinal() <= ExecutionState.KPI_PROCESSING_GROUP_7.ordinal();
    }

}

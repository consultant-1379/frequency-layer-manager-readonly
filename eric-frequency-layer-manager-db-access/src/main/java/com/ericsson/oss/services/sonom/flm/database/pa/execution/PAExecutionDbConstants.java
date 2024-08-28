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

package com.ericsson.oss.services.sonom.flm.database.pa.execution;

/**
 * Class to store column names for the pa_executions table.
 */
public final class PAExecutionDbConstants {

    public static final String PA_EXECUTIONS = "pa_executions";
    public static final String PA_OUTPUT_EVENTS = "pa_output_events";
    public static final String ID = "id";
    public static final String SCHEDULE = "schedule";
    public static final String PA_WINDOW = "pa_window";
    public static final String PA_WINDOW_START_TIME = "pa_window_start_time";
    public static final String PA_WINDOW_END_TIME = "pa_window_end_time";
    public static final String STATE = "state";
    public static final String STATE_MODIFIED_TIME = "state_modified_time";
    public static final String FLM_EXECUTION_ID = "flm_execution_id";
    public static final String NUM_PA_POLICY_INPUT_EVENTS_SENT = "num_pa_policy_input_events_sent";
    public static final String PA_EXECUTION_ID = "pa_execution_id";
    public static final String SECTOR_ID = "sector_id";
    public static final String DEGRADATION_STATUS = "degradation_status";

    private static final char COMMA = ',';

    private PAExecutionDbConstants() {
    }

    /**
     * Returns all column names in format for select query.
     *
     * @return a comma separated list of column names.
     */
    public static String getAllColumnNames() {
        return new StringBuilder()
                .append(ID).append(COMMA)
                .append(SCHEDULE).append(COMMA)
                .append(PA_WINDOW).append(COMMA)
                .append(PA_WINDOW_START_TIME).append(COMMA)
                .append(PA_WINDOW_END_TIME).append(COMMA)
                .append(STATE).append(COMMA)
                .append(STATE_MODIFIED_TIME).append(COMMA)
                .append(FLM_EXECUTION_ID).append(COMMA)
                .append(NUM_PA_POLICY_INPUT_EVENTS_SENT).toString();
    }

    /**
     * Returns all column names of the table pa_output_events in format for select query.
     *
     * @return a comma separated list of column names.
     */
    public static String getAllColumnNamesForOutputEvent() {
        return new StringBuilder()
                .append(FLM_EXECUTION_ID).append(COMMA)
                .append(PA_EXECUTION_ID).append(COMMA)
                .append(PA_WINDOW).append(COMMA)
                .append(SECTOR_ID).append(COMMA)
                .append(DEGRADATION_STATUS).toString();
    }
}

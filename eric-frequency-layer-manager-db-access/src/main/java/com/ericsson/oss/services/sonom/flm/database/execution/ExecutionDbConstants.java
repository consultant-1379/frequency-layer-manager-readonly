/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.database.execution;

import java.util.StringJoiner;

/**
 * Class to store column names for the flm_executions table.
 */
public final class ExecutionDbConstants {

    public static final String FLM_EXECUTIONS = "flm_executions";
    public static final String ID = "id";
    public static final String CONFIGURATION_ID = "configuration_id";
    public static final String START_TIME = "start_time";
    public static final String STATE = "state";
    public static final String STATE_MODIFIED_TIME = "state_modified_time";
    public static final String ADDITIONAL_EXECUTION_INFORMATION = "additional_execution_information";
    public static final String SCHEDULE = "schedule";
    public static final String RETRY_ATTEMPTS = "retry_attempts";
    public static final String CALCULATION_ID = "calculation_id";
    public static final String CUSTOMIZED_GLOBAL_SETTINGS = "customized_global_settings";
    public static final String CUSTOMIZED_DEFAULT_SETTINGS = "customized_default_settings";
    public static final String GROUPS = "groups";
    public static final String NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION = "num_sectors_to_evaluate_for_optimization";
    public static final String NUM_OPTIMIZATION_ELEMENTS_SENT = "num_optimization_elements_sent";
    public static final String NUM_OPTIMIZATION_ELEMENTS_RECEIVED = "num_optimization_elements_received";
    public static final String NUM_OPTIMIZATION_LBQS = "num_optimization_lbqs";
    public static final String NUM_CHANGES_WRITTEN_TO_CM_DB = "num_changes_written_to_cm_db";
    public static final String NUM_CHANGES_NOT_WRITTEN_TO_CM_DB = "num_changes_not_written_to_cm_db";
    public static final String OPEN_LOOP = "open_loop";
    public static final String INCLUSION_LIST = "inclusion_list";
    public static final String EXCLUSION_LIST = "exclusion_list";
    public static final String WEEKEND_DAYS = "weekend_days";
    public static final String ENABLE_PA = "enable_pa";
    public static final String FULL_EXECUTION = "full_execution";

    private ExecutionDbConstants() {
    }

    /**
     * Returns all column names in format for select query.
     *
     * @return a comma separated list of column names.
     */
    public static String getAllColumnNames() {
        final StringJoiner columnNames = new StringJoiner(",");
        return columnNames.add(ID)
                .add(CONFIGURATION_ID)
                .add(START_TIME)
                .add(STATE)
                .add(STATE_MODIFIED_TIME)
                .add(ADDITIONAL_EXECUTION_INFORMATION)
                .add(SCHEDULE)
                .add(RETRY_ATTEMPTS)
                .add(CALCULATION_ID)
                .add(CUSTOMIZED_GLOBAL_SETTINGS)
                .add(CUSTOMIZED_DEFAULT_SETTINGS)
                .add(GROUPS)
                .add(NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION)
                .add(NUM_OPTIMIZATION_ELEMENTS_SENT)
                .add(NUM_OPTIMIZATION_ELEMENTS_RECEIVED)
                .add(NUM_OPTIMIZATION_LBQS)
                .add(NUM_CHANGES_WRITTEN_TO_CM_DB)
                .add(NUM_CHANGES_NOT_WRITTEN_TO_CM_DB)
                .add(OPEN_LOOP)
                .add(INCLUSION_LIST)
                .add(EXCLUSION_LIST)
                .add(WEEKEND_DAYS)
                .add(ENABLE_PA)
                .add(FULL_EXECUTION).toString();
    }
}

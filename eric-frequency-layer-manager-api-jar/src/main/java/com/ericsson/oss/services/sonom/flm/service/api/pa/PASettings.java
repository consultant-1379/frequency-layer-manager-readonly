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

package com.ericsson.oss.services.sonom.flm.service.api.pa;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static java.lang.Integer.parseInt;

/**
 * Class to store and represent {@link PAExecution} settings information.
 */
public final class PASettings {

    public static final int PA_WINDOW_DURATION_IN_MINUTES = parseInt(getEnvironmentValue("PA_WINDOW_DURATION_IN_MINUTES", "360"));
    public static final int NUMBER_OF_PA_EXECUTIONS = parseInt(getEnvironmentValue("NUMBER_OF_PA_EXECUTIONS", "3"));
    public static final int INITIAL_PA_WINDOW_OFFSET_TIME_IN_MINUTES = parseInt(getEnvironmentValue("INITIAL_PA_WINDOW_OFFSET_TIME_IN_MINUTES",
            "60"));
    public static final int PA_EXECUTION_OFFSET_TIME_IN_MINUTES = parseInt(getEnvironmentValue("PA_EXECUTION_OFFSET_TIME_IN_MINUTES", "120"));
    public static final int PA_EXECUTION_MISFIRE_THRESHOLD_IN_MINUTES = parseInt(
            getEnvironmentValue("PA_EXECUTION_MISFIRE_THRESHOLD_IN_MINUTES", "120"));

    private PASettings() {
    }
}
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

/**
 * ENUM defining the possible states of a {@link PAExecution}. There are 5 states:
 * <ul>
 * <li>SCHEDULED</li>
 * <li>STARTED</li>
 * <li>TERMINATING</li>
 * <li>SUCCEEDED</li>
 * <li>MISFIRED</li>
 * <li>FAILED</li>
 * <li>CANCELLED</li>
 * <li>TERMINATED</li>
 * </ul>
 */
public enum PAExecutionState {
    SCHEDULED,
    STARTED,
    TERMINATING,
    SUCCEEDED,
    MISFIRED,
    FAILED,
    CANCELLED,
    TERMINATED
}
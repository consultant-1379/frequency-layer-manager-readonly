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

package com.ericsson.oss.services.sonom.flm.service.startup.executions;

import javax.ejb.Local;

/**
 * Implementations of this interface will determine the state of FLM executions on start up.
 */
@Local
public interface ExecutionsController {

    /**
     * Resume or schedule an execution.
     */
    void resumeOrScheduleExecution();
}

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
package com.ericsson.oss.services.sonom.flm.executor;

import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;

/**
 * Interface to provide execute method for each stage of the algorithm execution.
 */
public interface StageExecutor {

    /**
     * Execute the execution stage from it's initial state or a resumed state.
     * 
     * @param executionState
     *            the execution state to execute from.
     * @param isResumed
     *            whether it is resuming from a previous execution.
     * @param isFullExecution
     *            whether it is a full FLM execution or KPI calculation only.
     * @param executionDate
     *            the date of the execution.
     * @throws FlmAlgorithmException
     *             thrown in the event of an error when executing.
     */
    void execute(ExecutionState executionState, boolean isResumed, boolean isFullExecution, String executionDate) throws FlmAlgorithmException;
}

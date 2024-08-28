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

package com.ericsson.oss.services.sonom.flm.pa.executor;

import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;

/**
 * Interface to provide execute method for each stage of the {@link PAExecutionExecutor}.
 * 
 * @param <T>
 *            T generic type
 */
public interface PAStageExecutor<T> {

    /**
     * Executes a {@link PAExecutionExecutor} stage.
     *
     * @return T generic type.
     * @throws PAExecutionException
     *             thrown in the event of an error when executing.
     */
    T execute() throws PAExecutionException;
}
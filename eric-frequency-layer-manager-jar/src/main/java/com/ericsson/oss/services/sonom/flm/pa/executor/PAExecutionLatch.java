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

import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;

/**
 * A latch to check if the {@link PAExecutionExecutor} has received an interruption signal.
 */
public class PAExecutionLatch {
    private boolean interrupted;

    public PAExecutionLatch() {
        this.interrupted = false;
    }

    /**
     * Sets the internal latch to true.
     */
    public void interrupt() {
        this.interrupted = true;
    }

    /**
     * A method to check if the interrupt method has been called.
     *
     * @throws PAExecutionInterruptedException
     *             if the interrupt method has been called.
     */
    public void verifyNotInterruptedAndContinue() throws PAExecutionInterruptedException {
        if (interrupted) {
            throw new PAExecutionInterruptedException("Interrupt signal received from PAExecutorExecutor.");
        }
    }
}

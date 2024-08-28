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

import static org.assertj.core.api.Assertions.shouldHaveThrown;

import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import org.junit.Test;

/**
 * Unit tests for {@link PAExecutionLatch} class.
 */
public class PAExecutionLatchTest {
    @Test
    public void whenLatchIsInterrupted_andVerifyNotInterruptedIsCalled_thenExceptionIsThrown() {
        final PAExecutionLatch objUnderTest = new PAExecutionLatch();

        objUnderTest.interrupt();

        try {
            objUnderTest.verifyNotInterruptedAndContinue();
            shouldHaveThrown(PAExecutionInterruptedException.class);
        } catch (final PAExecutionInterruptedException ignored) {
        }
    }

    @Test
    public void whenLatchIsNotInterrupted_andVerifyNotInterruptedIsCalled_thenExceptionIsNotThrown() throws PAExecutionInterruptedException {
        final PAExecutionLatch objUnderTest = new PAExecutionLatch();
        objUnderTest.verifyNotInterruptedAndContinue();
    }
}

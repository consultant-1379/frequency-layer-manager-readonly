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
package com.ericsson.oss.services.sonom.flm.pa.exceptions;

import java.io.Serializable;

import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionExecutor;

/**
 * Exception produced by {@link PAExecutionExecutor} when execution is interrupted.
 */
public class PAExecutionInterruptedException extends PAExecutionException implements Serializable {
    private static final long serialVersionUID = 7018444503897825631L;

    public PAExecutionInterruptedException(final Exception e) {
        super(e);
    }

    public PAExecutionInterruptedException(final String message, final Exception e) {
        super(message, e);
    }

    public PAExecutionInterruptedException(final String message) {
        super(message);
    }
}

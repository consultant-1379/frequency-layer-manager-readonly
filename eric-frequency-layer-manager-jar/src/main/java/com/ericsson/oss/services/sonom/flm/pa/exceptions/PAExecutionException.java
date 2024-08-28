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
 * Exception produced by {@link PAExecutionExecutor}.
 */
public class PAExecutionException extends Exception implements Serializable {

    private static final long serialVersionUID = 538094454134861527L;

    public PAExecutionException(final Exception e) {
        super(e);
    }

    public PAExecutionException(final String message, final Exception e) {
        super(message, e);
    }

    public PAExecutionException(final String message) {
        super(message);
    }
}
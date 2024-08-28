/**
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.retention;

/**
 * Class for {@code RetentionManagerException} exceptions.
 */
public class RetentionManagerException extends RuntimeException {
    private static final long serialVersionUID = -7193630427006541728L;

    /**
     * Constructs an instance of this class.
     *
     * @param   cause
     *          the reason for the exception
     *
     * @param  throwable
     *          the {@code Throwable} exception
     */
    public RetentionManagerException(final String cause, final Throwable throwable) {
        super(cause, throwable);
    }
}
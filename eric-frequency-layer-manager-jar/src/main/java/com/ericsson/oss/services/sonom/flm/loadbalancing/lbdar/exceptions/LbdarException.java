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

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions;

/**
 * Exception for LBDAR.
 */
public class LbdarException extends Exception {

    private static final long serialVersionUID = 4840246200532179380L;

    public LbdarException(final String message) {
        super(message);
    }

    public LbdarException(final String message, final Throwable e) {
        super(message, e);
    }
}

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
 * Exception for Lbdar Reversion.
 */
public class LbdarReversionException extends Exception {

    private static final long serialVersionUID = 8070915932847071444L;

    public LbdarReversionException(final String message) {
        super(message);
    }
}

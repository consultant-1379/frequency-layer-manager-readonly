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
 * Unexpected exception scenarios in LBDAR (such as a coding fault in FLM or invalid data in the LBQ from PolicyEngine).
 */
public class LbdarUnexpectedException extends Exception {

    private static final long serialVersionUID = 8644203863891971591L;

    public LbdarUnexpectedException(final String message) {
        super(message);
    }

}

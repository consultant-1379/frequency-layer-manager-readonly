/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.messagehandler.exceptions;

/**
 * An exception thrown where the objects in messagehandler package are not setup properly.
 */
public class BadSetupException extends Exception {

    private static final long serialVersionUID = -5834174470012748431L;

    public BadSetupException() {
        super();
    }

    public BadSetupException(final String message) {
        super(message);
    }
}

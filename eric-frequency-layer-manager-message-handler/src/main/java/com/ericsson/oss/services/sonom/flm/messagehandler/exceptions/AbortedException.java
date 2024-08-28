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
 * It is used at ExecutionCounter to notify that the ExecutionCounter has been aborted.
 */
public class AbortedException extends InterruptedException {
    private static final long serialVersionUID = -6458660909923990254L;
}

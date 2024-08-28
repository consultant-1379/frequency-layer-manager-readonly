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

package com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor;

/**
 * Class to store constants used for retrieval of change elements.
 */
public final class PAConstants {

    public static final int MAX_RETRY_ATTEMPTS = 10;
    public static final int SECONDS_TO_WAIT = 30;
    public static final String DEGRADED = "DEGRADED";
    public static final String NOT_DEGRADED = "NOT DEGRADED";

    private PAConstants() {

    }

}
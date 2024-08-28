/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.test.util;

/**
 * Utility class that returns the hostnames for services
 */
public class ServiceHostnameAndPortProvider {

    private ServiceHostnameAndPortProvider() {
    }

    /**
     * Gets FLM hostname port from environment variables.
     *
     * @return the Frequency Layer Manager hostname port ( eg: eric-frequency-layer-manager:8080 )
     */
    public static String getFlmAlgorithmHostnameAndPort() {
        return System.getenv("FLM_HOSTNAME") + ":" + System.getenv("FLM_PORT");
    }

    /**
     * Gets CM Service hostname port from environment variables.
     *
     * @return the CM Service hostname port
     */
    public static String getCmServiceHostnameAndPort() {
        return System.getenv( "CM_SERVICE_HOSTNAME" ) + ":" + System.getenv( "CM_SERVICE_PORT" );
    }

}

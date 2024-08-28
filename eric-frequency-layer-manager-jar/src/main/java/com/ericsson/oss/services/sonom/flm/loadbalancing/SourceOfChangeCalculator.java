/*
 * -----------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing;

/**
 * This utility class is used to calculate the FLM's source of change value for Change Elements and Activation Policies.
 */
public final class SourceOfChangeCalculator {

    private static final String SOURCE_METHOD_PREFIX = "alg_";
    private static final String SOURCE_ALG_TYPE_PREFIX = "FLM_";

    private SourceOfChangeCalculator() {
    }

    /**
     * Creates the FLM sourceOfChange String based on the configuration ID.
     * @param configurationId {@link com.ericsson.oss.services.sonom.flm.service.api.executions.Execution}'s configurationId
     * @return sourceOfChange
     */
    public static String calculateSourceOfChange(final int configurationId) {
        return SOURCE_METHOD_PREFIX + SOURCE_ALG_TYPE_PREFIX + configurationId;
    }
}

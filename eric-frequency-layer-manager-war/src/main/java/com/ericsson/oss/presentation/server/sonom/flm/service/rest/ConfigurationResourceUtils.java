/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.presentation.server.sonom.flm.service.rest;

import com.ericsson.oss.services.sonom.common.jndi.JndiServiceFinder;
import com.ericsson.oss.services.sonom.flm.service.api.FlmConfigurationService;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * Utils class for {@link ConfigurationResource}.
 */
class ConfigurationResourceUtils {

    private static ConfigurationResourceUtils instance;

    public static ConfigurationResourceUtils getInstance() {
        if (instance == null) {
            instance = new ConfigurationResourceUtils();
        }
        return instance;
    }

    public MetricHelper getFirstMetricHelper(final JndiServiceFinder jndiServiceFinder) {
        return jndiServiceFinder.findFirst(MetricHelper.class);
    }

    public FlmConfigurationService getFirstConfigurationService(final JndiServiceFinder jndiServiceFinder) {
        return jndiServiceFinder.findFirst(FlmConfigurationService.class);
    }
}

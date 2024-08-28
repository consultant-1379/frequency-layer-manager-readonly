/*
 * ------------------------------------------------------------------------------
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

import java.util.Map;

import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ChangeElementCalculator;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileChangesCalculator;
import com.ericsson.oss.services.sonom.flm.loadbalancing.retriever.PolicyOutputEventRetriever;

/**
 * This class is here only to be able to play with different component in Load Balancing implementation.
 * In tests we can create different implementations by creating different calculators and NodeStore and CellStore implementations
 * Will be removed when the proper implementation of these will be found
 */
public interface LoadBalancingComponents {
    ChangeElementCalculator getChangeElementCalculator(String executionId, int configurationId);

    ProfileChangesCalculator getProfileChangeCalculator(Map<String, String> customizedGlobalSettings);

    PolicyOutputEventRetriever getPolicyOutputEventRetriever();

    CellKpiStore getCellKpiStore();
}

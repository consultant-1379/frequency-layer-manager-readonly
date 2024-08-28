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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.LbdarCalculator;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * LoadBalancingCalculatorFactory is used to create the appropriate LoadBalancingCalculator, based on the type given. For now only LBDAR is supported
 */
public class LoadBalancingCalculatorFactory {
    /**
     * A enum that represent the possible different Load Balancing algorithms.
     */
    public enum LoadBalancingType {
        LBDAR
    }

    /**
     * This method creates a LoadBalancingCalculator based on {@link LoadBalancingType} given.
     * @param lbType a type of LoadBalancing Algorithm
     * @param execution an Execution instance
     * @param customizedGlobalSettings customized global settings from Configuration of Execution
     * @param cmStore a store that can be used to query CM data
     * @param cellKpiStore an instance of cellKpiStore
     * @param policyOutputEvents the list of policyOutputEvents used to initialize stores
     * @return it returns an optional of LoadBalancingCalculator. It is empty type is not supported
     * @throws FlmAlgorithmException
     *          if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public Optional<LoadBalancingCalculator> create(final LoadBalancingType lbType,
                                                    final Execution execution,
                                                    final Map<String, String> customizedGlobalSettings,
                                                    final CmStore cmStore,
                                                    final CellKpiStore cellKpiStore,
                                                    final List<PolicyOutputEvent> policyOutputEvents) throws FlmAlgorithmException {
        if (lbType == LoadBalancingType.LBDAR) {
                return Optional.of(new LbdarCalculator(execution,
                                                       cmStore,
                                                       cellKpiStore,
                                                       customizedGlobalSettings,
                                                       policyOutputEvents));
        }
        return Optional.empty();
    }
}

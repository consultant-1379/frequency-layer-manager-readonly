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

import org.apache.commons.lang3.tuple.Pair;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Interface for load balancing calculating.
 * <p>
 * A Load Balancing Calculator should be able to create ChangeElements for the given list of PolicyOutputEvents
 */
public interface LoadBalancingCalculator {
    /**
     * The implementation of this method should calculate the ChangeElements for the given PolicyOutputEvents.
     *
     * @param policyOutputEvents a list of {@link PolicyOutputEvent}s
     * @return returns a List of Pairs containing Optimization and Reversion ChangeElements. The list cannot be null, but can be empty
     */
    List<Pair<ChangeElement, ChangeElement>> calculateChanges(List<PolicyOutputEvent> policyOutputEvents);
}

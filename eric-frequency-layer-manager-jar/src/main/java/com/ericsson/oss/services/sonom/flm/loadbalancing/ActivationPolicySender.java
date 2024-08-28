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

import com.ericsson.oss.services.sonom.activation.kafka.ActivationPolicy;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * Interface for activation policy sending.
 */
public interface ActivationPolicySender {

    /**
     * Sends {@link ActivationPolicy} to the configured Change Mediation Kafka Topic.
     * @param executionId ID of the {@link Execution}
     * @param sourceOfChange source of change
     * @throws FlmAlgorithmException a wrapper {@link FlmAlgorithmException} is thrown in case of any Exception during execution
     */
    void sendActivationPolicyToKafka(String executionId, String sourceOfChange) throws FlmAlgorithmException;
}

/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.startup.policy.deploy;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link PolicyCreator}. Creates the policy.
 */
@Stateless(name = "policyCreator")
public class PolicyCreatorImpl implements PolicyCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyCreatorImpl.class);
    private final com.ericsson.oss.services.sonom.flm.policy.PolicyCreator policyCreator;

    public PolicyCreatorImpl() {
        policyCreator = new com.ericsson.oss.services.sonom.flm.policy.PolicyCreator();
    }

    public PolicyCreatorImpl(final com.ericsson.oss.services.sonom.flm.policy.PolicyCreator flmAndPaPolicyCreator) {
        policyCreator = flmAndPaPolicyCreator;
    }

    @Override
    public void createPolicy(final String policyName, final String policyFilePath) {
        try {
            policyCreator.checkIfPolicyIsAlreadyDeployedAndDelete(policyName);
            policyCreator.createPolicy(policyFilePath);
            LOGGER.info("{} Policy is created", policyName);
        } catch (final Exception e) {
            LOGGER.error("Failed to create {} policy", policyName, e);
        }
    }
}

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

import javax.ejb.Local;

/**
 * Implementations of this interface will create a policy by sending it to the Policy API.
 */
@Local
public interface PolicyCreator {

    /**
     * Create the policy.
     *
     * @param policyName
     *            The name of the policy to be created
     * @param policyFilePath
     *            The file path of the policy to be created
     */
    void createPolicy(String policyName, String policyFilePath);
}

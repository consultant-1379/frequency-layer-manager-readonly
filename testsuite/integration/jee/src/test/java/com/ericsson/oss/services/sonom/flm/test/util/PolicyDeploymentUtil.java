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
package com.ericsson.oss.services.sonom.flm.test.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyDeploymentException;
import com.ericsson.oss.services.sonom.policy.client.PolicyRestExecutor;

/**
 * Utility class that is used to deploy the FLM Policy using the Policy Engine's PAP service.
 */
public class PolicyDeploymentUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyDeploymentUtil.class);

    private static final PolicyRestExecutor policyRestExecutor = new PolicyRestExecutor.Builder().withUnlimitedRetry().build();
    private static final String FLM_POLICY_ID = "onap.policies.apex.Flm";

    private PolicyDeploymentUtil() {
    }

    /**
     * Deploys the FLM policy via the Get PAP service
     */
    public static void deployPolicy(final String deployPolicyPayload) throws IOException, PolicyDeploymentException {
        final String policyDeploymentPayload = loadResource(deployPolicyPayload);
        policyRestExecutor.deployPolicy(policyDeploymentPayload);
        LOGGER.info("Policy has been deployed successfully");
    }

    public static boolean undeployPolicy() throws PolicyDeploymentException {
        policyRestExecutor.undeployPolicy(FLM_POLICY_ID);
        LOGGER.info("FLM Policy has been undeployed");
        return true;
    }

    private static String loadResource(final String filePath) throws IOException {
        try {
            return ResourceLoaderUtils.getClasspathResourceAsString(filePath);
        } catch (final Exception e) { //NOSONAR Exception suitably logged
            throw new IOException(("Error loading resource through filepath:" + filePath), e);
        }
    }

}

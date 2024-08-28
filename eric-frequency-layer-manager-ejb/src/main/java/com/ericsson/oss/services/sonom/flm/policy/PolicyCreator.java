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

package com.ericsson.oss.services.sonom.flm.policy;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyCreationException;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyDeploymentException;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyGetException;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyRestException;
import com.ericsson.oss.services.sonom.policy.client.PolicyRestExecutor;

/**
 * Loads the policy and sends it to the Policy Engine to be created.
 */
public class PolicyCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyCreator.class);
    private static final String POLICY_VERSION = "1.0.0";
    private static final Set<Integer> RECOVERABLE_STATUS_CODES_FOR_POLICY_DELETE = new HashSet<>(2);

    private final PolicyRestExecutor policyRestExecutor;
    private final PolicyRestExecutor policyDeleteRestExecutor;
    private final PolicyRestExecutor policyLimitedRetryRestExecutor;

    public PolicyCreator() {
        RECOVERABLE_STATUS_CODES_FOR_POLICY_DELETE.add(HttpStatus.SC_BAD_REQUEST);
        RECOVERABLE_STATUS_CODES_FOR_POLICY_DELETE.add(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        policyRestExecutor = new PolicyRestExecutor.Builder().withUnlimitedRetry().build();
        policyDeleteRestExecutor = new PolicyRestExecutor.Builder().withUnlimitedRetry(RECOVERABLE_STATUS_CODES_FOR_POLICY_DELETE).build();

        policyLimitedRetryRestExecutor = new PolicyRestExecutor.Builder().withLimitedRetry(1).build();

    }

    //This is only used for testing purposes
    public PolicyCreator(final RestExecutor restExecutor) {
        policyRestExecutor = new PolicyRestExecutor.Builder().withRestExecutor(restExecutor).build();
        policyDeleteRestExecutor = new PolicyRestExecutor.Builder().withRestExecutor(restExecutor).build();

        policyLimitedRetryRestExecutor = new PolicyRestExecutor.Builder().withRestExecutor(restExecutor).build();
    }

    private static String loadResource(final String filePath) throws IOException {
        try {
            return ResourceLoaderUtils.getClasspathResourceAsString(filePath);
        } catch (final Exception e) { //NOSONAR Exception suitably logged
            throw new IOException(("Error loading resource through filepath: " + filePath), e);
        }
    }

    /**
     * Deletes the existing policy from the policy framework if it exists.
     *
     * @param policyName
     *            The policy name
     * @throws PolicyRestException
     *             thrown if the policy fails to be deleted
     */
    public void checkIfPolicyIsAlreadyDeployedAndDelete(final String policyName) throws PolicyRestException {
        if (isPolicyIsAlreadyDeployed(policyName)) {
            undeployPolicy(policyName);
        }
        LOGGER.info("Deleting any previously created {} policy", policyName);
        policyDeleteRestExecutor.deletePolicy(policyName, POLICY_VERSION);
        LOGGER.info("{} policy has been deleted successfully", policyName);
    }

    /**
     * Loads the policy and sends it to the Policy Engine to be created.
     *
     * @param policyFilePath
     *            The file path of the policy
     * @throws PolicyCreationException
     *             thrown if the policy fails to be created
     * @throws IOException
     *             thrown if the policy files fail to be read from resources
     */
    public void createPolicy(final String policyFilePath) throws PolicyCreationException, IOException {
        final String basePolicyString = loadResource(policyFilePath);
        final String createPolicyString = basePolicyString.replace("BOOTSTRAP_SERVER", getEnvironmentValue("BOOTSTRAP_SERVER"));
        policyRestExecutor.createPolicy(createPolicyString);
    }

    /**
     * Undeploys the policy
     *
     * @param policyName
     *            The policy name
     */
    private void undeployPolicy(final String policyName) throws PolicyDeploymentException, PolicyGetException {
        if (isPolicyIsAlreadyDeployed(policyName)) {
            policyRestExecutor.undeployPolicy(policyName);
            LOGGER.info("{} Policy has been undeployed", policyName);
        }
        LOGGER.info("{} Policy is not currently deployed", policyName);
    }

    /**
     * Checks to see if the policy is already deployed
     *
     * @param policyName
     *            The policy name
     */
    private boolean isPolicyIsAlreadyDeployed(final String policyName) throws PolicyGetException {
        LOGGER.info("Checking if {} policy is already deployed", policyName);
        final String entity = policyLimitedRetryRestExecutor.getDeployedPolicies();
        if (entity.contains("\"" + policyName + "\"")) {
            LOGGER.info("{} policy is already deployed, this policy will be undeployed.", policyName);
            return true;
        }
        LOGGER.info("{} is not already deployed", policyName);
        return false;
    }
}

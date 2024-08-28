/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2023
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyDeploymentException;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyGetException;
import com.ericsson.oss.services.sonom.policy.client.PolicyRestExecutor;


/**
 * Loads the policy and sends it to the Policy Engine to be deployed.
 */
public class PolicyDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyDeployer.class);
    private static final int EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 10;
    private static final int EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 30;
    private static final int PA_EXECUTION_AMOUNT_OF_RETRIES = 120; //120 retries equates to 1 Hour - 30 seconds per retry
    private final PolicyRestExecutor policyRestExecutor;
    private final PolicyRestExecutor policyLimitedRetryRestExecutor;
    private final ExecutionDao flmExecutionDao = new ExecutionDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS, EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
    private final PAExecutionDao paExecutionDao = new PAExecutionDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS, EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);

    public PolicyDeployer() {
        policyRestExecutor = new PolicyRestExecutor.Builder().withLimitedRetry(PA_EXECUTION_AMOUNT_OF_RETRIES).build();
        policyLimitedRetryRestExecutor = new PolicyRestExecutor.Builder().withLimitedRetry(3).build();
    }

    private static String loadResource(final String filePath) throws IOException {
        try {
            return ResourceLoaderUtils.getClasspathResourceAsString(filePath);
        } catch (final Exception e) { //NOSONAR Exception suitably logged
            throw new IOException(("Error loading resource through filepath:" + filePath), e);
        }
    }

    /**
     * Loads the policy payload and sends it to the Policy Engine to be deployed.
     *
     * @param policyId
     *            The policy name needed for the log
     * @param policyPayload
     *            The filepath of the payload of the policy to be deployed
     * @throws PolicyDeploymentException
     *             thrown if the policy fails to be deployed
     * @throws IOException
     *             thrown if the policy files fail to be read from resources
     */
    public void deployPolicy(final String policyId, final String policyPayload) throws PolicyDeploymentException, IOException {
        final String deployPolicyString = loadResource(policyPayload);
        policyRestExecutor.deployPolicy(deployPolicyString);
        LOGGER.info("{} policy has been deployed", policyId);
    }

    /**
     * Undeploys the policy from the Policy Engine.
     *
     * @param policyId
     *            The policy name to be undeployed
     * @throws PolicyDeploymentException
     *             thrown if the policy fails to be undeployed
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized void undeployPolicy(final String policyId) throws PolicyDeploymentException {
        policyRestExecutor.undeployPolicy(policyId);
        LOGGER.info("{} policy has been undeployed", policyId);
    }

    /**
     * Check if a flm execution is currently running and if not, undeploy the policy.
     *
     * @param policyId
     *            The policy name to be undeployed
     * @throws PolicyDeploymentException
     *             thrown if the policy fails to be undeployed
     * @throws PolicyGetException
     *            returns a policy exception
     * @throws SQLException
     *             thrown if the checking the states in the DB fails
     */
    public void checkIfFlmExecutionIsRunningAndUndeployPolicy(final String policyId)
            throws PolicyDeploymentException, SQLException, PolicyGetException {
        if (!isFlmParallelExecutionsRunning() && isPolicyIsAlreadyDeployed(policyId)) {
            undeployPolicy(policyId);
        }
    }

    /**
     * Check if a flm pa execution is currently running and if not, undeploy the policy.
     *
     * @param policyId
     *            The policy name to be undeployed
     * @throws PolicyDeploymentException
     *             thrown if the policy fails to be undeployed
     * @throws PolicyGetException
     *              returns a policy exception
     * @throws SQLException
     *             thrown if the checking the states in the DB fails
     */
    public void checkIfFlmPaExecutionIsRunningAndUndeployPolicy(final String policyId)
            throws PolicyDeploymentException, SQLException, PolicyGetException {
        if (!isFlmPaParallelExecutionsRunning() && isPolicyIsAlreadyDeployed(policyId)) {
            undeployPolicy(policyId);
        }
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

    private boolean isFlmParallelExecutionsRunning() throws SQLException {
        final ExecutionState[] unfinishedStates = ExecutionState.getRestartableExecutionStates();
        final List<Execution> runningExecutions = flmExecutionDao.getExecutionsInStates(unfinishedStates);
        if (runningExecutions.isEmpty()) {
            LOGGER.info("All running FLM executions have completed, FLM Policy will be undeployed");
            return false;
        }
        for (final Execution runningExecution : runningExecutions) {
            LOGGER.info("Parallel FLM execution with id '{}' still in progress, FLM Policy will not be undeployed", runningExecution.getId());
        }
        return true;
    }

    private boolean isFlmPaParallelExecutionsRunning() throws SQLException {
        final Map<String, List<PAExecution>> runningExecutions = paExecutionDao.getPAExecutionsInStates(PAExecutionState.STARTED);
        if (runningExecutions.isEmpty()) {
            LOGGER.info("All running FLM PA executions have completed, FLM PA Policy will be undeployed");
            return false;
        }
        for (final Map.Entry<String, List<PAExecution>> entry : runningExecutions.entrySet()) {
            final List<PAExecution> paExecutions = entry.getValue();
            for (final PAExecution paExecution : paExecutions) {
                LOGGER.info("Parallel FLM PA execution with id '{}' still in progress, FLM PA Policy will not be undeployed", paExecution.getId());
            }
        }
        return true;
    }
}


/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.pa.executor;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDaoImpl;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import com.ericsson.oss.services.sonom.flm.pa.kpi.PAKpiCalculationExecutor;
import com.ericsson.oss.services.sonom.flm.pa.policy.PAPolicyExecutor;
import com.ericsson.oss.services.sonom.flm.pa.reversion.PAReversionExecutor;
import com.ericsson.oss.services.sonom.flm.policy.PolicyDeployer;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

/**
 * Responsible for executing a {@link PAExecution}. <br>
 * It will execute the different {@link PAStageExecutor} that make up a pa execution <br>
 * and will also update the {@link PAExecutionState} in the DB.
 */
public class PAExecutionExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PAExecutionExecutor.class);
    private static final String DEPLOY_FLM_PA_POLICY_PAYLOAD_JSON = "policy/DeployFlmPaPolicyPayload.json";
    private static final String FLM_PA_POLICY_ID = "onap.policies.apex.FlmPa";
    private static final int PA_EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 10;
    private static final int PA_EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 30;
    private PAExecution paExecution;
    private PAPolicyExecutor paPolicyExecutor;
    private PolicyDeployer policyDeployer;
    private PAExecutionDao paExecutionDao;
    private PAKpiCalculationExecutor paKpiCalculationExecutor;
    private PAReversionExecutor paReversionExecutor;
    private PAExecutionLatch latch;

    public PAExecutionExecutor(final PAExecution paExecution, final Execution flmExecution) {
        this.paExecution = paExecution;
        this.latch = new PAExecutionLatch();
        paExecutionDao = new PAExecutionDaoImpl(PA_EXECUTION_DAO_MAX_RETRY_ATTEMPTS, PA_EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
        final PAOutputEventDao paOutputEventDao = new PAOutputEventDaoImpl(PA_EXECUTION_DAO_MAX_RETRY_ATTEMPTS,
                PA_EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
        policyDeployer = new PolicyDeployer();
        paKpiCalculationExecutor = new PAKpiCalculationExecutor(this.paExecution, flmExecution, this.latch);
        paPolicyExecutor = new PAPolicyExecutor(paExecutionDao, flmExecution, paExecution, paOutputEventDao, this.latch);
        paReversionExecutor = new PAReversionExecutor(paExecution, paOutputEventDao, flmExecution.getConfigurationId(), this.latch);
    }

    // for unit testing only.
    PAExecutionExecutor() {
    }

    /**
     * Executes the different {@link PAStageExecutor}(s) for the pa execution .
     */
    public void executeActivity() {
        LOGGER.info("Starting PA Execution for PA Execution ID: '{}'", paExecution.getId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting PA Execution for '{}'", paExecution);
        }
        updatePaExecutionState(paExecution, PAExecutionState.STARTED);

        try {
            latch.verifyNotInterruptedAndContinue();
            policyDeployer.deployPolicy(FLM_PA_POLICY_ID, DEPLOY_FLM_PA_POLICY_PAYLOAD_JSON);
            paKpiCalculationExecutor.execute();
            LOGGER.info("Finished PA KPI Calculations");

            latch.verifyNotInterruptedAndContinue();
            paPolicyExecutor.execute();
            LOGGER.info("Finished Executing PA policy");

            latch.verifyNotInterruptedAndContinue();
            paReversionExecutor.execute();
            LOGGER.info("Finished PA Reversion Executor");
            updatePaExecutionState(paExecution, PAExecutionState.SUCCEEDED);
            LOGGER.info("Finished PA Execution for PA Execution ID: '{}'", paExecution.getId());
        } catch (final PAExecutionInterruptedException e) {
            updatePaExecutionState(paExecution, PAExecutionState.TERMINATED);
            LOGGER.warn("PA Execution terminated for PA Execution ID:  '{}'", paExecution.getId(), e);
        } catch (final Exception e) {
            updatePaExecutionState(paExecution, PAExecutionState.FAILED);
            LOGGER.error("PA Execution failed for PA Execution ID:  '{}'", paExecution.getId(), e);
        } finally {
            try {
                policyDeployer.checkIfFlmPaExecutionIsRunningAndUndeployPolicy(FLM_PA_POLICY_ID);
            } catch (final Exception e) {
                LOGGER.error("Failed to undeploy PA policy after execution", e);
            }
        }
    }

    /**
     * Terminates the on-going {@link PAExecution}.
     */
    public void terminateActivity() {
        LOGGER.info("Interrupt signal received for PA Execution '{}'.", paExecution.getId());
        latch.interrupt();
        updatePaExecutionState(paExecution, PAExecutionState.TERMINATING);
    }

    private void updatePaExecutionState(final PAExecution paExecution, final PAExecutionState paExecutionState) {
        paExecution.setState(paExecutionState);
        try {
            LOGGER.info("Updating PA Execution '{}' state to '{}'", paExecution.getId(), paExecutionState);
            paExecutionDao.update(paExecution);
            LOGGER.info("Successfully updated PA Execution '{}' state to '{}'", paExecution.getId(), paExecutionState);
        } catch (final SQLException e) {
            LOGGER.error("Failed to update PA Execution state '{}'", paExecution, e);
        }
    }

    PAExecutionExecutor withPAKpiCalculationExecutor(final PAKpiCalculationExecutor paKpiCalculationExecutor) {
        this.paKpiCalculationExecutor = paKpiCalculationExecutor;
        return this;
    }

    PAExecutionExecutor withPAPolicyExecutor(final PAPolicyExecutor paPolicyExecutor) {
        this.paPolicyExecutor = paPolicyExecutor;
        return this;
    }

    PAExecutionExecutor withPAExecutionDao(final PAExecutionDao paExecutionDao) {
        this.paExecutionDao = paExecutionDao;
        return this;
    }

    PAExecutionExecutor withPolicyDeployer(final PolicyDeployer policyDeployer) {
        this.policyDeployer = policyDeployer;
        return this;
    }

    PAExecutionExecutor withPaExecution(final PAExecution paExecution) {
        this.paExecution = paExecution;
        return this;
    }

    PAExecutionExecutor withPAReversionExecutor(final PAReversionExecutor paReversionExecutor) {
        this.paReversionExecutor = paReversionExecutor;
        return this;
    }

    PAExecutionExecutor withPAExecutionLatch(final PAExecutionLatch latch) {
        this.latch = latch;
        return this;
    }
}
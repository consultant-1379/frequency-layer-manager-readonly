/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.startup.executions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.executions.ExecutionDbHandler;
import com.ericsson.oss.services.sonom.flm.executions.ResumeExecutions;
import com.ericsson.oss.services.sonom.flm.scheduler.FlmAlgorithmExecutionScheduler;
import com.ericsson.oss.services.sonom.flm.service.api.FlmConfigurationService;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.startup.policy.deploy.PolicyCreator;

import io.vavr.CheckedFunction0;

/**
 * Implements {@link ExecutionsController}. Schedules an FLM execution.
 */
@Stateless(name = "flmExecutionController")
public class FlmExecutionsController implements ExecutionsController {

    public static final String FLM_POLICY_NAME = "onap.policies.apex.Flm";
    public static final String FLM_POLICY_FILE_PATH = "policy/FrequencyLayerManagerPolicy.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmExecutionsController.class);
    //MAX_NUMBER_OF_THREADS and org.quartz.threadPool.threadCount value in the scheduler.properties (from son-common) must have the same value of 10
    private static final int MAX_NUMBER_OF_THREADS = 10;
    private static final int EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 1;
    private static final int EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 30;
    private static final int CONFIGURATIONS_MAX_RETRY_ATTEMPTS = Integer.MAX_VALUE;
    private static final int CONFIGURATIONS_WAIT_PERIOD_IN_SECONDS = 30;
    private static final String FLM_PA_POLICY_NAME = "onap.policies.apex.FlmPa";
    private static final String FLM_PA_POLICY_FILE_PATH = "policy/PerformanceAssurancePolicy.json";

    private final ExecutionDao executionDao;
    private final ExecutionDbHandler executionDbHandler = new ExecutionDbHandler();
    private final DatabaseRetry configurationsRetry;
    private final ExecutorService nonSettingsExecutorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);

    @EJB(name = "flmConfigurationServiceBean")
    private FlmConfigurationService flmConfigurationServiceBean;

    @EJB(name = "policyCreator")
    private PolicyCreator policyCreator;

    public FlmExecutionsController() {
        executionDao = new ExecutionDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS, EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
        configurationsRetry = new DatabaseRetry(CONFIGURATIONS_MAX_RETRY_ATTEMPTS, CONFIGURATIONS_WAIT_PERIOD_IN_SECONDS);
    }

    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void resumeOrScheduleExecution() {
        LOGGER.info("Policy creation process is starting");
        policyCreator.createPolicy(FLM_POLICY_NAME, FLM_POLICY_FILE_PATH);
        policyCreator.createPolicy(FLM_PA_POLICY_NAME, FLM_PA_POLICY_FILE_PATH);
        resumeExecution();
        scheduleFlmExecutionOnStartup();
    }

    void resumeExecution() {

        List<Execution> allUnfinishedExecutions = Collections.emptyList();
        try {
            allUnfinishedExecutions = executionDao.getExecutionsInStates(ExecutionState.getRestartableExecutionStates());
        } catch (final SQLException e1) {
            LOGGER.error("Failed to retrieve executions to resume on startup", e1);
            return;
        }

        LOGGER.debug("Execution(s) returned in KPI_PROCESSING_GROUP_# state: {}", allUnfinishedExecutions);
        LOGGER.debug("Count of Executions returned in KPI_PROCESSING_GROUP_# state: {}", allUnfinishedExecutions.size());

        final List<Execution> executionsToBeResumed = new ResumeExecutions(allUnfinishedExecutions).findExecutionsToResume();

        LOGGER.debug("Execution(s) to be resumed: {}", executionsToBeResumed);
        LOGGER.info("Count of executions to be resumed: {}", executionsToBeResumed.size());

        final List<Callable<Void>> nonSettingsTasks = new ArrayList<>();

        // Usually only one execution will be responsible for non-settings based KPI calculations.
        // For scenarios with multiple (i.e. resumed executions from different dates) they will be resumed in parallel
        final List<Execution> resumedNonSettingsBasedExecutions = getNonSettingsBasedExecutions(executionsToBeResumed);
        if (resumedNonSettingsBasedExecutions.isEmpty()) {
            LOGGER.info("No execution to be resumed responsible for non-settings based KPI calculations");
        } else {
            LOGGER.info("Found executions that could be responsible for non-settings based KPI calculations, resuming these executions : {}",
                    resumedNonSettingsBasedExecutions);
            for (final Execution nonSettingsExecution : resumedNonSettingsBasedExecutions) {
                nonSettingsTasks.add(new ResumedNonSettingsExecutionsRunner(nonSettingsExecution));
                allUnfinishedExecutions.remove(nonSettingsExecution);
            }
            try {
                nonSettingsExecutorService.invokeAll(nonSettingsTasks);
            } catch (final InterruptedException e) { //NOSONAR Exception suitably logged
                LOGGER.error("Resumed non-settings based KPI calculations were interrupted ", e);
                nonSettingsExecutorService.shutdownNow();
            } finally {
                nonSettingsExecutorService.shutdown();
            }
        }

        LOGGER.info("Resuming {} execution(s)", executionsToBeResumed.size());
        final List<Callable<Void>> settingsTasks = new ArrayList<>();

        for (final Execution execution : executionsToBeResumed) {
            LOGGER.info("Resuming execution with ID: {}", execution.getId());
            if (resumedNonSettingsBasedExecutions.contains(execution)) {
                settingsTasks.add(new ResumedExecutionsRunner(execution, executionDbHandler, true));
            } else {
                settingsTasks.add(new ResumedExecutionsRunner(execution, executionDbHandler, false));
            }
            allUnfinishedExecutions.remove(execution);
        }
        try {
            executorService.invokeAll(settingsTasks);
        } catch (final InterruptedException e) { //NOSONAR Exception suitably logged
            LOGGER.error("Resumed executions were interrupted", e);
            executorService.shutdownNow();
        } finally {
            executorService.shutdown();
        }

        allUnfinishedExecutions.removeAll(executionsToBeResumed);

        LOGGER.debug("Execution(s) to mark as failed: {}", allUnfinishedExecutions);
        LOGGER.info("Count of executions to mark as failed: {}", allUnfinishedExecutions.size());

        final Execution[] failedExecutions = allUnfinishedExecutions.toArray(new Execution[0]);
        executionDbHandler.applyFailedState(failedExecutions);
    }

    /**
     * Get a list of {@link Execution}s with state between 'KPI_PROCESSING_GROUP_1' and 'KPI_PROCESSING_GROUP_7' from a list of {@link Execution}s.
     *
     * @param executions
     *            List of executions to check for non-settings based KPI calculations.
     * @return Filtered list of {@link Execution}s that require non-settings based KPI calculations.
     */
    public List<Execution> getNonSettingsBasedExecutions(final List<Execution> executions) {
        return executions.stream().filter(execution -> ExecutionState.isStateAssociatedWithNonSettingsBasedKpiCalculations(execution.getState()))
                .collect(Collectors.toList());
    }

    void scheduleFlmExecutionOnStartup() {
        try {
            final CheckedFunction0<List<Configuration>> getFlmConfigurationsFunctionWithRetry = () -> flmConfigurationServiceBean.getConfigurations();
            final List<Configuration> configurations = configurationsRetry.executeWithRetryAttempts(getFlmConfigurationsFunctionWithRetry);
            configurations
                    .stream().filter(Configuration::isEnabled).collect(Collectors.toList())
                    .forEach(configuration -> {
                        try {
                            FlmAlgorithmExecutionScheduler.createSchedule(configuration);
                        } catch (final ActivitySchedulerException e) {
                            LOGGER.error("Failed to schedule the FLM Configuration '{}' from the db on start up", configuration, e);
                        }
                    });
        } catch (final SQLException e) {
            LOGGER.error("Failed to retrieve the FLM Configurations from the db on start up", e);
        }
    }

}

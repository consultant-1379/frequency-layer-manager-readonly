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
package com.ericsson.oss.services.sonom.flm.executor;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SETTINGS_PROCESSING;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationExecutor;
import com.ericsson.oss.services.sonom.flm.loadbalancing.LoadBalancingExecutor;
import com.ericsson.oss.services.sonom.flm.metric.FlmMetricHelper;
import com.ericsson.oss.services.sonom.flm.optimization.OptimizationExecutor;
import com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionsScheduler;
import com.ericsson.oss.services.sonom.flm.policy.PolicyDeployer;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;
import com.ericsson.oss.services.sonom.flm.settings.evaluation.CellSettingsExecutor;
import com.ericsson.oss.services.sonom.flm.settings.history.CellSettingsHistoryExecutor;
import com.ericsson.oss.services.sonom.policy.api.exception.PolicyRestException;

/**
 * Algorithm executor.
 */
public class FlmAlgorithmExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmAlgorithmExecutor.class);
    private static final int EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 10;
    private static final int EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 30;
    private static final String FLM_ALGORITHM_ID_PREFIX = "FLM_";
    private static final String DEPLOY_FLM_POLICY_PAYLOAD_JSON = "policy/DeployFlmPolicyPayload.json";
    private static final String FLM_POLICY_ID = "onap.policies.apex.Flm";
    private ExecutionDao executionDao = new ExecutionDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS, EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
    private boolean isResumed;
    private String resumeExecutionDate;
    private String cronExpression;
    private String executionDate;
    private int configurationId;
    private Map<String, String> customizedGlobalSettings;
    private Map<String, String> customizedDefaultSettings;
    private List<CustomizedGroup> groups;
    private Boolean openLoop;
    private List<Group> inclusionList;
    private List<Group> exclusionList;
    private String weekendDays;
    private Boolean enablePA;

    private CmStore cmStore;
    private Execution execution = new Execution();
    private PolicyDeployer policyDeployer = new PolicyDeployer();
    private PersistenceHandler persistenceHandler;
    private MetricHelper flmMetricHelper;
    private PreAlgorithmExecutor preAlgorithmExecutor;
    private CellSettingsExecutor cellSettingsExecutor;
    private KpiCalculationExecutor kpiCalculationExecutor;
    private OptimizationExecutor optimizationExecutor;
    private LoadBalancingExecutor loadBalancingExecutor;
    private CellSettingsHistoryExecutor cellSettingsHistoryExecutor;

    public FlmAlgorithmExecutor(final Configuration configuration) {
        init(configuration);
    }

    /**
     * Constructor to be used when resuming an execution.
     *
     * @param resumedExecution
     *            The execution to be resumed.
     */
    public FlmAlgorithmExecutor(final Execution resumedExecution) {
        isResumed = true;
        execution = resumedExecution;
        resumeExecutionDate = DateTimeFormatter.ISO_DATE.format(resumedExecution.getStartTime().toLocalDateTime());
        flmMetricHelper = new FlmMetricHelper();
        cmStore = new CmStore(execution);
        persistenceHandler = new PersistenceHandler(execution, executionDao);
        preAlgorithmExecutor = new PreAlgorithmExecutor(execution, executionDao);
        cellSettingsExecutor = new CellSettingsExecutor(cmStore, execution, flmMetricHelper, persistenceHandler);
        kpiCalculationExecutor = new KpiCalculationExecutor(cmStore, execution, flmMetricHelper, persistenceHandler);
        optimizationExecutor = new OptimizationExecutor(cmStore, execution, persistenceHandler, executionDao);
        loadBalancingExecutor = new LoadBalancingExecutor(execution, cmStore, flmMetricHelper, persistenceHandler);
        cellSettingsHistoryExecutor = new CellSettingsHistoryExecutor(execution, flmMetricHelper, persistenceHandler);
    }

    FlmAlgorithmExecutor withCmStore(final CmStore cmStore) {
        this.cmStore = cmStore;
        return this;
    }

    FlmAlgorithmExecutor withExecutionDao(final ExecutionDao executionDao) {
        this.executionDao = executionDao;
        return this;
    }

    FlmAlgorithmExecutor withIsResumed(final boolean isResumed) {
        this.isResumed = isResumed;
        return this;
    }

    FlmAlgorithmExecutor withExecution(final Execution resumedExecution) {
        execution = resumedExecution;
        return this;
    }

    FlmAlgorithmExecutor withExecutionDate(final String executionDate) {
        this.executionDate = executionDate;
        return this;
    }

    FlmAlgorithmExecutor withPersistenceHandler(final PersistenceHandler persistenceHandler) {
        this.persistenceHandler = persistenceHandler;
        return this;
    }

    PersistenceHandler getPersistenceHandler() {
        return persistenceHandler;
    }

    public KpiCalculationExecutor getKpiCalculationExecutor() {
        return kpiCalculationExecutor;
    }

    FlmAlgorithmExecutor withCellSettingsExecutor(final CellSettingsExecutor cellSettingsExecutor) {
        this.cellSettingsExecutor = cellSettingsExecutor;
        return this;
    }

    FlmAlgorithmExecutor withKpiCalculatorExecutor(final KpiCalculationExecutor kpiCalculationExecutor) {
        this.kpiCalculationExecutor = kpiCalculationExecutor;
        return this;
    }

    FlmAlgorithmExecutor withOptimizationExecutor(final OptimizationExecutor optimizationExecutor) {
        this.optimizationExecutor = optimizationExecutor;
        return this;
    }

    FlmAlgorithmExecutor withLoadBalancingExecutor(final LoadBalancingExecutor loadBalancingExecutor) {
        this.loadBalancingExecutor = loadBalancingExecutor;
        return this;
    }

    FlmAlgorithmExecutor withCellSettingsHistoryExecutor(final CellSettingsHistoryExecutor cellSettingsHistoryExecutor) {
        this.cellSettingsHistoryExecutor = cellSettingsHistoryExecutor;
        return this;
    }

    FlmAlgorithmExecutor withFlmMetricHelper(final MetricHelper flmMetricHelper) {
        this.flmMetricHelper = flmMetricHelper;
        return this;
    }

    FlmAlgorithmExecutor withPolicyDeployer(final PolicyDeployer policyDeployer) {
        this.policyDeployer = policyDeployer;
        return this;
    }

    FlmAlgorithmExecutor withPreAlgorithmExecutor(final PreAlgorithmExecutor preAlgorithmExecutor) {
        this.preAlgorithmExecutor = preAlgorithmExecutor;
        return this;
    }

    /**
     * Method to start FLM algorithm execution.
     */
    public void executeActivity() {
        final long algorithmStartTime = System.nanoTime();
        try {
            prepareExecution();
            if (execution.getState() == ExecutionState.getInitialState()) {
                preAlgorithmExecutor.runPreExecutionSteps();
                persistenceHandler.persistExecutionStatus(ExecutionState.getFirstState(), isResumed);
            }
            startNonSettingsBasedKpiFlow(kpiCalculationExecutor, executionDate, execution, isResumed, executionDao);
            if (execution.isFullExecution()) {
                executeFullFlmAlgorithm();
            } else {
                executeFlmAlgorithmKpiCalculationSteps();
            }
        } catch (final FlmAlgorithmException | PolicyRestException | IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(LoggingFormatter.formatMessage(execution.getId(), "Failed to execute all algorithm stages"), e);
            }
        } finally {
            finalizeExecution(algorithmStartTime);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Retrieved execution {}", execution);
            }
        }
        triggerPaScheduler(execution);
    }

    /**
     * Method to check if execution needs to calculate non-settings based KPIs.
     *
     * @param kpiCalculationExecutor
     *            The executor to be used to begin non-settings based KPI calculations if required.
     * @param executionDate
     *            Date of execution.
     * @param execution
     *            The current {@link Execution} object.
     * @param isResumed
     *            if true, execution is resumed.
     * @param executionDao
     *            DAO object to use when checking other executions in database.
     */
    private static void startNonSettingsBasedKpiFlow(final KpiCalculationExecutor kpiCalculationExecutor, final String executionDate,
            final Execution execution, final boolean isResumed, final ExecutionDao executionDao) {
        synchronized (FlmAlgorithmExecutor.class) {
            try {
                final List<Execution> databaseExecutions = executionDao
                        .getExecutionsInStates(Arrays.stream(ExecutionState.values()).toArray(ExecutionState[]::new));
                final LocalDate latestExecutionDate = getLatestDate(databaseExecutions);

                LOGGER.info("Checking if execution with ID: {} is responsible for non-settings based KPI calculation", execution.getId());
                if (LocalDate.parse(executionDate).equals(latestExecutionDate)
                        && !isNonSettingsBasedKpiCalculationStarted(execution, latestExecutionDate, databaseExecutions)) {
                    LOGGER.info("Non-settings based KPI calculations started for execution ID: {}", execution.getId());
                    calculateNonSettingsBasedKpis(kpiCalculationExecutor, executionDate, execution, isResumed);
                } else if (execution.getState() == ExecutionState.getFirstState()) {
                    LOGGER.info("Execution ID {} not responsible for non-settings based KPI calculations", execution.getId());
                    execution.setState(SETTINGS_PROCESSING);
                }
            } catch (final Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(LoggingFormatter.formatMessage(execution.getId(), "Failed to execute non-settings based KPI stages"), e);
                }
            }
        }
    }

    private static LocalDate getLatestDate(final List<Execution> databaseExecutions) {
        LocalDateTime tempLocalDate = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        for (final Execution execution : databaseExecutions) {
            if (execution.getStartTime().toLocalDateTime().isAfter(tempLocalDate)) {
                tempLocalDate = execution.getStartTime().toLocalDateTime();
            }
        }
        return tempLocalDate.toLocalDate();
    }

    private static boolean isNonSettingsBasedKpiCalculationStarted(final Execution currentExecution, final LocalDate latestDate,
            final List<Execution> databaseExecutions) {
        final LocalDateTime tempLocalDate = latestDate.atStartOfDay();
        for (final Execution execution : databaseExecutions) {
            if (databaseExecutionResponsibleForNonSettingsKpiCalculations(currentExecution, tempLocalDate, execution)) {
                return true;
            }
        }
        return false;
    }

    private static boolean databaseExecutionResponsibleForNonSettingsKpiCalculations(final Execution currentExecution,
            final LocalDateTime tempLocalDate, final Execution execution) {
        return execution.getStartTime().toLocalDateTime().isAfter(tempLocalDate)
                && execution.getState().ordinal() > ExecutionState.getFirstState().ordinal()
                && execution.getState().ordinal() != ExecutionState.FAILED.ordinal()
                && execution.getWeekendDays().equalsIgnoreCase(currentExecution.getWeekendDays());
    }

    /**
     * Calculates non-settings based KPIs for given executor.
     *
     * @param kpiCalculationExecutor
     *            The executor to be used to begin non-settings based KPI calculations.
     * @param executionDate
     *            Date of execution.
     * @param execution
     *            The current {@link Execution} object.
     * @param isResumed
     *            if true, execution is resumed.
     * @return whether non-settings based KPI calculations passed or failed.
     */
    public static boolean calculateNonSettingsBasedKpis(final KpiCalculationExecutor kpiCalculationExecutor, final String executionDate,
            final Execution execution, final boolean isResumed) {
        try {
            kpiCalculationExecutor.nonSettingsBasedExecute(execution.getState(), isResumed, executionDate);
        } catch (final FlmAlgorithmException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(LoggingFormatter.formatMessage(execution.getId(), "Failed to execute non-settings based KPI stages"), e);
            }
            return false;
        }
        return execution.getState() != ExecutionState.FAILED;
    }

    private void executeFullFlmAlgorithm() throws PolicyRestException, IOException, FlmAlgorithmException {
        LOGGER.info("Starting full FLM algorithm for schedule : {}", cronExpression);
        LOGGER.info("FLM Execution ID is {}", execution.getId());
        LOGGER.info("Execution date is {}", executionDate);
        policyDeployer.deployPolicy(FLM_POLICY_ID, DEPLOY_FLM_POLICY_PAYLOAD_JSON);
        LOGGER.info("OSF_TABLE is set to: \"{}\"", getEnvironmentValue("OSF_TABLE"));
        LOGGER.info("BW_STEP_SIZE_TABLE is set to: \"{}\"", getEnvironmentValue("BW_STEP_SIZE_TABLE"));
        // Stage 1 - Apply Cell Settings Evaluations
        cellSettingsExecutor.execute(execution.getState(), isResumed, execution.isFullExecution(), executionDate);
        // Stage 2 - Execute KPI Calculations
        kpiCalculationExecutor.settingsBasedExecute(execution.getState(), isResumed, execution.isFullExecution(), executionDate);
        // Stage 3 - Optimization
        optimizationExecutor.execute(execution.getState(), isResumed, execution.isFullExecution(), executionDate);
        // Stage 4 - Execute Load Balancing
        loadBalancingExecutor.execute(execution.getState(), isResumed, execution.isFullExecution(), executionDate);
        // Stage 5 - Insert Settings Into Historical Table
        cellSettingsHistoryExecutor.execute(execution.getState(), isResumed, execution.isFullExecution(), executionDate);
    }

    private void executeFlmAlgorithmKpiCalculationSteps() throws FlmAlgorithmException {
        LOGGER.info("Starting FLM algorithm KPI calculation steps for configuration : {}", configurationId);
        LOGGER.info("FLM Execution ID is {}", execution.getId());
        LOGGER.info("Execution date is {}", executionDate);
        // Stage 1 - Apply Cell Settings Evaluations
        cellSettingsExecutor.execute(execution.getState(), isResumed, execution.isFullExecution(), executionDate);
        // Stage 2 - Execute KPI Calculations
        kpiCalculationExecutor.settingsBasedExecute(execution.getState(), isResumed, execution.isFullExecution(), executionDate);
        // Stage 3 - Insert Settings Into Historical Table
        cellSettingsHistoryExecutor.execute(execution.getState(), isResumed, execution.isFullExecution(), executionDate);
    }

    /**
     * Method to execute finishing steps for an execution.
     *
     * @param algorithmStartTime
     *            Time the algorithm starts
     */
    protected void finalizeExecution(final long algorithmStartTime) {
        try {
            ExecutionFinalState.setFinalState(execution, executionDao, persistenceHandler, isResumed);
        } catch (final SQLException | FlmAlgorithmException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(LoggingFormatter.formatMessage(execution.getId(), "Failed to set final state after execution"), e);
            }
        }
        if (execution.isFullExecution()) {
            try {
                policyDeployer.checkIfFlmExecutionIsRunningAndUndeployPolicy(FLM_POLICY_ID);
            } catch (final Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(LoggingFormatter.formatMessage(execution.getId(), "Failed to undeploy policy after execution"), e);
                }
            }
        }

        incrementFlmCountAndTimeMetrics(algorithmStartTime,
                FlmMetric.FLM_ALG_EXECUTION_TIME_IN_MILLIS, FlmMetric.FLM_ALG_EXECUTION);

        if (execution.isFullExecution()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("Finished full FLM Algorithm for schedule : %s", execution.getSchedule())));
            }
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("Finished daily KPI calculation steps of FLM algorithm for schedule : %s",
                                execution.getSchedule())));
            }
        }
    }

    protected void triggerPaScheduler(final Execution flmExecution) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(flmExecution.getId(), "Triggering Performance Assurance for FLM"));
        }
        CompletableFuture.runAsync(() -> PAExecutionsScheduler.createSchedule(flmExecution));
    }

    protected void incrementFlmCountAndTimeMetrics(final Long requestStartTime, final FlmMetric flmMetricTime, final FlmMetric flmMetricCount) {
        flmMetricHelper.incrementFlmMetric(flmMetricTime, flmMetricHelper.getTimeElapsedInMillis(requestStartTime));
        flmMetricHelper.incrementFlmMetric(flmMetricCount);
    }

    protected void prepareExecution() throws FlmAlgorithmException {
        if (isResumed) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("Execution is restarted from stage %s", execution.getState())));
            }
            executionDate = resumeExecutionDate;
        } else {
            executionDate = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
            buildDefaultExecutionObject();
        }
    }

    protected void buildDefaultExecutionObject() throws FlmAlgorithmException {
        execution.setState(ExecutionState.getInitialState());
        execution.setRetryAttempts(0);
        execution.setSchedule(cronExpression);
        execution.setConfigurationId(configurationId);
        execution.setCustomizedGlobalSettings(customizedGlobalSettings);
        execution.setCustomizedDefaultSettings(customizedDefaultSettings);
        execution.setGroups(groups);
        execution.setOpenLoop(openLoop);
        execution.setInclusionList(inclusionList);
        execution.setExclusionList(exclusionList);
        execution.setWeekendDays(weekendDays);
        execution.setId(generateExecutionId());
        execution.setNumSectorsToEvaluateForOptimization(0);
        execution.setNumOptimizationElementsSent(0);
        execution.setNumOptimizationElementsReceived(0);
        execution.setNumOptimizationLbqs(0);
        execution.setNumChangesWrittenToCmDb(0);
        execution.setNumChangesNotWrittenToCmDb(0);
        execution.setEnablePA(enablePA);
        execution.setFullExecution(isFullExecutionScheduled(executionDate, cronExpression));
        execution.setAdditionalExecutionInformation(StringUtils.EMPTY);
        disablePAIfNotFullExecution(execution);
        persistenceHandler.persistExecutionStatus(execution.getState(), isResumed);
    }

    private void disablePAIfNotFullExecution(final Execution execution) {
        if (enablePA != null && enablePA && !execution.isFullExecution()) {
            execution.setEnablePA(false);
        }
    }

    private void init(final Configuration configuration) {
        cronExpression = configuration.getSchedule();
        configurationId = configuration.getId();
        customizedGlobalSettings = configuration.getCustomizedGlobalSettings();
        customizedDefaultSettings = configuration.getCustomizedDefaultSettings();
        groups = configuration.getGroups();
        openLoop = configuration.isOpenLoop();
        inclusionList = configuration.getInclusionList();
        exclusionList = configuration.getExclusionList();
        weekendDays = configuration.getWeekendDays();
        enablePA = configuration.isEnablePA();
        cmStore = new CmStore(execution);
        flmMetricHelper = new FlmMetricHelper();
        persistenceHandler = new PersistenceHandler(execution, executionDao);
        preAlgorithmExecutor = new PreAlgorithmExecutor(execution, executionDao);
        cellSettingsExecutor = new CellSettingsExecutor(cmStore, execution, flmMetricHelper, persistenceHandler);
        kpiCalculationExecutor = new KpiCalculationExecutor(cmStore, execution, flmMetricHelper, persistenceHandler);
        optimizationExecutor = new OptimizationExecutor(cmStore, execution, persistenceHandler, executionDao);
        loadBalancingExecutor = new LoadBalancingExecutor(execution, cmStore, flmMetricHelper, persistenceHandler);
        cellSettingsHistoryExecutor = new CellSettingsHistoryExecutor(execution, flmMetricHelper, persistenceHandler);
    }

    private String generateExecutionId() {
        final long executionStartTime = System.currentTimeMillis();
        return FLM_ALGORITHM_ID_PREFIX + executionStartTime + "-" + Thread.currentThread().getId();
    }

    private static Boolean isFullExecutionScheduled(final String date, final String cronExpression) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
            final Date executionDate = parseDate(sdf, date);
            final CronExpression configurationSchedule = new CronExpression(cronExpression);
            final Date nextFullExecutionDate = configurationSchedule.getNextValidTimeAfter(executionDate);

            return datesAreSameDay(sdf, executionDate, nextFullExecutionDate);
        } catch (final ParseException e) {
            LOGGER.error("Error parsing CRON expression from FLM schedule '{}' provided in configuration", cronExpression);
        }
        return false;
    }

    private static Date parseDate(final SimpleDateFormat sdf, final String date) throws ParseException {
        return sdf.parse(date);
    }

    private static boolean datesAreSameDay(final SimpleDateFormat sdf, final Date date, final Date nextRun) {
        return sdf.format(date).equals(sdf.format(nextRun));
    }
}

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

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_1;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.policy.api.exception.PolicyRestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.dbrunners.ExecutionDbCommands;
import com.ericsson.oss.services.sonom.flm.dbrunners.FlmServiceUnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.dbrunners.UnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.executions.util.ExecutionBuilder;
import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationExecutor;
import com.ericsson.oss.services.sonom.flm.loadbalancing.LoadBalancingExecutor;
import com.ericsson.oss.services.sonom.flm.messagehandler.KafkaConsumerWrapper;
import com.ericsson.oss.services.sonom.flm.optimization.ExecutionConsumerController;
import com.ericsson.oss.services.sonom.flm.optimization.OptimizationExecutor;
import com.ericsson.oss.services.sonom.flm.policy.PolicyDeployer;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;
import com.ericsson.oss.services.sonom.flm.settings.evaluation.CellSettingsExecutor;
import com.ericsson.oss.services.sonom.flm.settings.history.CellSettingsHistoryExecutor;

/**
 * Unit tests for {@link FlmAlgorithmExecutor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlmAlgorithmExecutorTest {

    private static final String EXECUTION_ID = "FLM_1600701252-162";
    private static final String DEPLOY_FLM_POLICY_PAYLOAD_JSON = "policy/DeployFlmPolicyPayload.json";
    private static final String FLM_POLICY_ID = "onap.policies.apex.Flm";
    public static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new FlmServiceUnitTestDatabaseRunner();
    private static final ExecutionDao testDbExecutionDao = new ExecutionDaoImpl(2, 5);
    private static final Configuration configuration = new Configuration();

    static {
        System.setProperty("CM_SERVICE_HOSTNAME", "CM_SERVICE_HOSTNAME");
        System.setProperty("CM_SERVICE_PORT", "8080");
        System.setProperty("KPI_SERVICE_HOSTNAME", "KPI_SERVICE_HOSTNAME");
        System.setProperty("KPI_SERVICE_PORT", "8080");
        System.setProperty("POLICY_REST_USER", "healthcheck");
        System.setProperty("POLICY_REST_PASSWORD", "zb!XztG34");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_HOST", "localhost");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_API_SERVICE_PORT", "6969");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_HOST", "localhost");
        System.setProperty("ERIC_AUT_POLICY_ENGINE_AX_PAP_SERVICE_PORT", "6969");
    }

    private FlmAlgorithmExecutor resumedObjectTest;
    private FlmAlgorithmExecutor notResumedObjectTest;
    private FlmAlgorithmExecutor usingMockedExecutionObjectTest;

    @Mock
    private CmStore cmStore;
    @Mock
    private KpiCalculationExecutor kpiCalculationExecutor;
    @Mock
    private PersistenceHandler persistenceHandler;
    @Mock
    private CellSettingsExecutor cellSettingsExecutor;
    @Mock
    private OptimizationExecutor optimizationExecutor;
    @Mock
    private LoadBalancingExecutor loadBalancingExecutor;
    @Mock
    private CellSettingsHistoryExecutor cellSettingsHistoryExecutor;
    @Mock
    private MetricHelper flmMetricHelperMock;
    @Mock
    private Execution mockedExecution;
    @Mock
    private KafkaConsumerWrapper<PolicyOutputEvent> wrapperMock;
    @Mock
    private PolicyDeployer mockedPolicyDeployer;
    @Mock
    private ExecutionDaoImpl executionDao;
    @Mock
    private PreAlgorithmExecutor preAlgorithmExecutor;

    @Before
    public void setUp() throws IOException, PolicyRestException, SQLException, FlmAlgorithmException {
        initMocks(this);

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.dropTable(ExecutionDbConstants.FLM_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.createExecutionTable());
        ExecutionConsumerController.getOrCreate(wrapperMock);

        final HashMap<String, String> testMap = new HashMap<>();
        testMap.put("testk", "testv");
        configuration.setId(1);
        configuration.setSchedule("0 0 2 ? * * *");
        configuration.setEnablePA(true);
        configuration.setGroups(Collections.singletonList(new CustomizedGroup("test", testMap)));
        configuration.setInclusionList(Collections.singletonList(new Group("test_group2")));
        configuration.setExclusionList(Collections.singletonList(new Group("test_group2")));
        configuration.setWeekendDays("Saturday,Sunday");

        doNothing().when(mockedPolicyDeployer).deployPolicy(FLM_POLICY_ID, DEPLOY_FLM_POLICY_PAYLOAD_JSON);
        doNothing().when(preAlgorithmExecutor).runPreExecutionSteps();
        usingMockedExecutionObjectTest = createFlmAlgorithmExecutor(mockedExecution, configuration, Boolean.FALSE);

        final Execution execution = new DefaultExecutionBuilder().build();
        notResumedObjectTest = spy(createFlmAlgorithmExecutor(execution, configuration, Boolean.FALSE));
        resumedObjectTest = spy(createFlmAlgorithmExecutor(execution, configuration, Boolean.TRUE));

        doNothing().when(notResumedObjectTest).finalizeExecution(isA(Long.class));

        when(executionDao.insert(any(Execution.class))).thenAnswer(oneInsert -> insertCommand(oneInsert.getArgument(0)));
        when(executionDao.update(any(Execution.class))).thenAnswer(oneUpdate -> FlmAlgorithmExecutorTest.updateCommand(oneUpdate.getArgument(0)));
        when(executionDao.getExecutionsInStates(any(ExecutionState.class))).thenAnswer(oneGet -> getCommand());
    }

    @Test
    public void whenExecutingFlmAlgorithm_andFullExecutionIsNotScheduled_thenCorrectStagesAreExecutedInCorrectOrder()
            throws FlmAlgorithmException, SQLException {
        resetTableCommand();

        final Execution execution = new DefaultExecutionBuilder()
                .executionId(EXECUTION_ID)
                .configurationId(1)
                .build();
        configuration.setSchedule("0 0 2 * * ? 2099");
        final FlmAlgorithmExecutor kpiOnlyVerifier = spy(createFlmAlgorithmExecutor(execution, configuration, Boolean.FALSE));
        doNothing().when(kpiOnlyVerifier).finalizeExecution(isA(Long.class));

        kpiOnlyVerifier.executeActivity();

        verify(executionDao).insert(execution);
        final InOrder inOrder = inOrder(kpiCalculationExecutor, cellSettingsExecutor, kpiCalculationExecutor, cellSettingsHistoryExecutor);
        inOrder.verify(kpiCalculationExecutor).nonSettingsBasedExecute(any(ExecutionState.class), eq(false), anyString());
        inOrder.verify(cellSettingsExecutor).execute(any(ExecutionState.class), eq(false), eq(false), anyString());
        inOrder.verify(kpiCalculationExecutor).settingsBasedExecute(any(ExecutionState.class), eq(false), eq(false), anyString());
        inOrder.verify(cellSettingsHistoryExecutor).execute(any(ExecutionState.class), eq(false), eq(false), anyString());

        assertThat(execution.isFullExecution()).isFalse();
        assertThat(execution.isEnablePA()).isFalse();
    }

    @Test
    public void whenExecutingFlmAlgorithm_thenAllStagesExecutedInExpectedOrder() throws FlmAlgorithmException {
        resetTableCommand();
        notResumedObjectTest.executeActivity();

        final InOrder inOrder = inOrder(preAlgorithmExecutor, kpiCalculationExecutor, cellSettingsExecutor, kpiCalculationExecutor, optimizationExecutor,
                loadBalancingExecutor,
                cellSettingsHistoryExecutor);

        inOrder.verify(preAlgorithmExecutor).runPreExecutionSteps();
        inOrder.verify(kpiCalculationExecutor).nonSettingsBasedExecute(any(ExecutionState.class), eq(false), anyString());
        inOrder.verify(cellSettingsExecutor).execute(any(ExecutionState.class), anyBoolean(), eq(true), anyString());
        inOrder.verify(kpiCalculationExecutor).settingsBasedExecute(any(ExecutionState.class), anyBoolean(), eq(true), anyString());
        inOrder.verify(optimizationExecutor).execute(any(ExecutionState.class), anyBoolean(), eq(true), anyString());
        inOrder.verify(loadBalancingExecutor).execute(any(ExecutionState.class), anyBoolean(), eq(true), anyString());
        inOrder.verify(cellSettingsHistoryExecutor).execute(any(ExecutionState.class), anyBoolean(), eq(true), anyString());

        verify(notResumedObjectTest, times(1)).triggerPaScheduler(any(Execution.class));
    }

    @Test
    public void whenExecutingScheduledFlmAlgorithm_thenExecutionDateIsToday() throws FlmAlgorithmException {
        resetTableCommand();
        final String todaysDate = DateTimeFormatter.ISO_DATE.format(LocalDate.now());

        notResumedObjectTest.executeActivity();

        verify(notResumedObjectTest, times(1)).buildDefaultExecutionObject();
        verify(kpiCalculationExecutor, times(1)).nonSettingsBasedExecute(any(ExecutionState.class), eq(false), anyString());
        verify(cellSettingsExecutor, times(1)).execute(any(ExecutionState.class), eq(false), eq(true), eq(todaysDate));
        verify(kpiCalculationExecutor, times(1)).settingsBasedExecute(any(ExecutionState.class), eq(false), eq(true), eq(todaysDate));
        verify(cellSettingsHistoryExecutor, times(1)).execute(any(ExecutionState.class), eq(false), eq(true), eq(todaysDate));
    }

    @Test
    public void whenExecutingResumedFlmAlgorithm_thenExecutionDateIsResumed() throws FlmAlgorithmException {
        resetTableCommand();
        final String todaysDate = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
        doNothing().when(resumedObjectTest).finalizeExecution(isA(Long.class));
        doNothing().when(resumedObjectTest).prepareExecution();

        resumedObjectTest.withIsResumed(true);
        resumedObjectTest.executeActivity();

        verify(kpiCalculationExecutor, times(1)).settingsBasedExecute(any(ExecutionState.class), eq(true), eq(true), eq(todaysDate));
    }

    @Test
    public void whenExecutingFlmAlgorithm_thenIncrementsFlmCountAndTimeMetrics() {
        resetTableCommand();

        doNothing().when(flmMetricHelperMock).incrementFlmMetric(any());
        doReturn(1L).when(flmMetricHelperMock).getTimeElapsedInMillis(anyLong());

        notResumedObjectTest.executeActivity();
        notResumedObjectTest.incrementFlmCountAndTimeMetrics(1L, FlmMetric.FLM_ALG_EXECUTION_TIME_IN_MILLIS, FlmMetric.FLM_ALG_EXECUTION);

        verify(flmMetricHelperMock, times(1)).incrementFlmMetric(any());
        verify(flmMetricHelperMock, times(1)).incrementFlmMetric(any(), anyLong());
    }

    @Test
    public void whenBuildingDefaultExecution_thenVerifyThatFieldsAreSet() throws FlmAlgorithmException {
        usingMockedExecutionObjectTest.withPersistenceHandler(persistenceHandler);
        usingMockedExecutionObjectTest.buildDefaultExecutionObject();

        verify(mockedExecution, times(1)).setSchedule(any());
        verify(mockedExecution, times(1)).setState(any());
        verify(mockedExecution, times(1)).setConfigurationId(anyInt());
        verify(mockedExecution, times(1)).setRetryAttempts(anyInt());
        verify(mockedExecution, times(1)).setCustomizedDefaultSettings(any());
        verify(mockedExecution, times(1)).setGroups(any());
        verify(mockedExecution, times(1)).setOpenLoop(any());
        verify(mockedExecution, times(1)).setInclusionList(any());
        verify(mockedExecution, times(1)).setExclusionList(any());
        verify(mockedExecution, times(1)).setWeekendDays(any());
        verify(mockedExecution, times(2)).setEnablePA(any());
        verify(mockedExecution, times(1)).setFullExecution(any());
    }

    @Test
    public void whenExecutorCreated_thenObjectIsConstructedSuccessful() {
        final Configuration configuration = new Configuration();
        configuration.setSchedule("0 0 2 ? * * *");
        configuration.setId(1);
        notResumedObjectTest = new FlmAlgorithmExecutor(configuration);
        notResumedObjectTest.withCmStore(cmStore);

        final Execution resumeExecution = new ExecutionBuilder()
                .id(EXECUTION_ID + "_3")
                .configurationId(1)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.930"))
                .state(KPI_PROCESSING_GROUP_2)
                .retryAttempts(3)
                .build();
        new FlmAlgorithmExecutor(resumeExecution);

        assertThat(resumeExecution.getId()).isEqualTo(EXECUTION_ID.concat("_3"));
    }

    public static String insertCommand(final Execution execution) {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.insertExecution(Collections.singletonList(execution)));
        return execution.getId();
    }

    public static List<Execution> getCommand() throws SQLException {
        return testDbExecutionDao.getExecutionsInStates(Arrays.stream(ExecutionState.values()).toArray(ExecutionState[]::new));

    }

    public static Integer updateCommand(final Execution execution) {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(Collections.singletonList(ExecutionDbCommands.performUpdate(execution)));
        return 1;
    }

    public static void resetTableCommand() {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));
    }

    private FlmAlgorithmExecutor createFlmAlgorithmExecutor(final Execution execution, final Configuration configuration, final boolean isResumed) {
        return new FlmAlgorithmExecutor(configuration).withCmStore(cmStore)
                .withExecution(execution)
                .withExecutionDate(LocalDate.now().toString())
                .withCellSettingsExecutor(cellSettingsExecutor)
                .withKpiCalculatorExecutor(kpiCalculationExecutor)
                .withOptimizationExecutor(optimizationExecutor)
                .withLoadBalancingExecutor(loadBalancingExecutor)
                .withCellSettingsHistoryExecutor(cellSettingsHistoryExecutor)
                .withPersistenceHandler(new PersistenceHandler(execution, executionDao))
                .withIsResumed(isResumed)
                .withFlmMetricHelper(flmMetricHelperMock)
                .withPolicyDeployer(mockedPolicyDeployer)
                .withExecutionDao(executionDao)
                .withPreAlgorithmExecutor(preAlgorithmExecutor);
    }

    static class DefaultExecutionBuilder {
        // default values for a test execution
        private static final long BASE_EXECUTION_TIME = 1_611_751_613_017L;
        private static final String calculationId = "CALCULATION_ID_0";
        private static final String additionalExecutionInformation = "";
        private static final boolean isFullExecution = true;
        private final Map<String, String> customizedGlobalSettings = new HashMap<>();
        private final Map<String, String> customizedDefaultSettings = new HashMap<>();
        private final List<CustomizedGroup> groups = Collections.emptyList();
        private final Timestamp stateModifiedTime = new Timestamp(BASE_EXECUTION_TIME);
        private final List<Group> inclusionList = Collections.emptyList();
        private String cronExpression = "0 0 2 ? * * *";
        private int configurationId = 1;
        private String executionId = "a";
        private ExecutionState executionState = KPI_PROCESSING_GROUP_1;
        private int numOptimizationLbqs;
        private int numChangesWrittenToCmDb;
        private boolean openLoop = true;
        private List<Group> exclusionList = Collections.emptyList();
        private boolean enablePA = true;
        private Timestamp startTime = new Timestamp(BASE_EXECUTION_TIME);

        protected DefaultExecutionBuilder executionId(final String executionId) {
            this.executionId = executionId;
            return this;
        }

        protected DefaultExecutionBuilder startTime(final Timestamp timeStamp) {
            startTime = timeStamp;
            return this;
        }

        protected DefaultExecutionBuilder configurationId(final int configurationId) {
            this.configurationId = configurationId;
            return this;
        }

        protected DefaultExecutionBuilder state(final ExecutionState state) {
            executionState = state;
            return this;
        }

        protected DefaultExecutionBuilder schedule(final String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        protected DefaultExecutionBuilder numOptimizationLbqs(final int numOptimizationLbqs) {
            this.numOptimizationLbqs = numOptimizationLbqs;
            return this;
        }

        protected DefaultExecutionBuilder numChangesWrittenToCmDb(final int numChangesWrittenToCmDb) {
            this.numChangesWrittenToCmDb = numChangesWrittenToCmDb;
            return this;
        }

        protected DefaultExecutionBuilder openLoop(final boolean openLoop) {
            this.openLoop = openLoop;
            return this;
        }

        protected DefaultExecutionBuilder exclusionList(final List<Group> exclusionList) {
            this.exclusionList = exclusionList;
            return this;
        }

        protected DefaultExecutionBuilder enablePA(final boolean enablePA) {
            this.enablePA = enablePA;
            return this;
        }

        protected Execution build() {
            final Execution execution = new Execution();
            execution.setId(executionId);
            execution.setStateModifiedTime(stateModifiedTime);
            execution.setStartTime(startTime);
            execution.setConfigurationId(configurationId);
            execution.setState(executionState);
            execution.setSchedule(cronExpression);
            execution.setCalculationId(calculationId);
            execution.setCustomizedGlobalSettings(customizedGlobalSettings);
            execution.setCustomizedDefaultSettings(customizedDefaultSettings);
            execution.setGroups(groups);
            execution.setAdditionalExecutionInformation(additionalExecutionInformation);
            execution.setNumOptimizationLbqs(numOptimizationLbqs);
            execution.setNumChangesWrittenToCmDb(numChangesWrittenToCmDb);
            execution.setOpenLoop(openLoop);
            execution.setInclusionList(inclusionList);
            execution.setExclusionList(exclusionList);
            execution.setEnablePA(enablePA);
            execution.setFullExecution(isFullExecution);
            return execution;
        }
    }
}

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
package com.ericsson.oss.services.sonom.flm.executor;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SETTINGS_PROCESSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.dbrunners.ExecutionDbCommands;
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
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;
import com.ericsson.oss.services.sonom.flm.settings.evaluation.CellSettingsExecutor;
import com.ericsson.oss.services.sonom.flm.settings.history.CellSettingsHistoryExecutor;

/**
 * Silent Unit tests for parallel tests of {@link FlmAlgorithmExecutor} class.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class FlmAlgorithmParallelExecutorTest {
    @Mock
    private CmStore cmStore;
    @Mock
    private KpiCalculationExecutor kpiCalculationExecutor;
    @Mock
    private KpiCalculationExecutor kpiCalculationExecutor2;
    @Mock
    private KpiCalculationExecutor kpiCalculationExecutor3;
    @Mock
    private CellSettingsExecutor cellSettingsExecutor;
    @Mock
    private CellSettingsHistoryExecutor cellSettingsHistoryExecutor;
    @Mock
    private ExecutionDaoImpl executionDao;
    @Mock
    private OptimizationExecutor optimizationExecutor;
    @Mock
    private LoadBalancingExecutor loadBalancingExecutor;
    @Mock
    private MetricHelper flmMetricHelperMock;
    @Mock
    private PolicyDeployer mockedPolicyDeployer;
    @Mock
    private KafkaConsumerWrapper wrapperMock;

    @Before
    public void setUp() throws SQLException {
        initMocks(this);
        ExecutionConsumerController.getOrCreate(wrapperMock);
        FlmAlgorithmExecutorTest.UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.dropTable(ExecutionDbConstants.FLM_EXECUTIONS));
        FlmAlgorithmExecutorTest.UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.createExecutionTable());
        when(executionDao.insert(any(Execution.class))).thenAnswer(oneInsert -> FlmAlgorithmExecutorTest.insertCommand(oneInsert.getArgument(0)));
        when(executionDao.getExecutionsInStates(any(ExecutionState.class))).thenAnswer(oneGet -> FlmAlgorithmExecutorTest.getCommand());
        when(executionDao.update(any(Execution.class))).thenAnswer(oneUpdate -> FlmAlgorithmExecutorTest.updateCommand(oneUpdate.getArgument(0)));
    }

    @Test
    public void whenMultipleExecutionsArePassed_thenActivitiesRunInParallel() throws FlmAlgorithmException, NullPointerException {
        final String todaysDate = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
        final Configuration configuration = new Configuration();
        configuration.setId(1);
        configuration.setSchedule("0 0 2 * * ? 2099");
        final HashMap<String, String> testMap = new HashMap<>();
        testMap.put("testk", "testv");
        configuration.setGroups(Collections.singletonList(new CustomizedGroup("test", testMap)));
        configuration.setInclusionList(Collections.singletonList(new Group("test_group2")));
        configuration.setExclusionList(Collections.singletonList(new Group("test_group2")));
        configuration.setWeekendDays("Saturday,Sunday");
        final Execution execution1 = new Execution();
        final Execution execution2 = new Execution();
        final Execution execution3 = new Execution();

        final List<FlmAlgorithmExecutor> parallelExecutors = new ArrayList<>();
        final FlmAlgorithmExecutor flmAlgorithmExecutor1 = spy(createFlmAlgorithmExecutor(execution1, configuration, Boolean.FALSE)
                .withPersistenceHandler(new PersistenceHandler(execution1, executionDao)));
        doNothing().when(flmAlgorithmExecutor1).finalizeExecution(any(Long.class));
        final FlmAlgorithmExecutor flmAlgorithmExecutor2 = spy(createFlmAlgorithmExecutor(execution2, configuration, Boolean.FALSE)
                .withPersistenceHandler(new PersistenceHandler(execution2, executionDao)))
                        .withKpiCalculatorExecutor(kpiCalculationExecutor2);
        doNothing().when(flmAlgorithmExecutor2).finalizeExecution(any(Long.class));
        final FlmAlgorithmExecutor flmAlgorithmExecutor3 = spy(createFlmAlgorithmExecutor(execution3, configuration, Boolean.FALSE)
                .withPersistenceHandler(new PersistenceHandler(execution3, executionDao)))
                        .withKpiCalculatorExecutor(kpiCalculationExecutor3);
        doNothing().when(flmAlgorithmExecutor3).finalizeExecution(any(Long.class));

        doAnswer(once -> flmAlgorithmExecutor1.getPersistenceHandler().persistExecutionStatus(SETTINGS_PROCESSING, Boolean.FALSE))
                .when(kpiCalculationExecutor).nonSettingsBasedExecute(any(ExecutionState.class), eq(false), eq(todaysDate));
        doAnswer(once -> flmAlgorithmExecutor2.getPersistenceHandler().persistExecutionStatus(SETTINGS_PROCESSING, Boolean.FALSE))
                .when(kpiCalculationExecutor2).nonSettingsBasedExecute(any(ExecutionState.class), eq(false), eq(todaysDate));
        doAnswer(once -> flmAlgorithmExecutor3.getPersistenceHandler().persistExecutionStatus(SETTINGS_PROCESSING, Boolean.FALSE))
                .when(kpiCalculationExecutor3).nonSettingsBasedExecute(any(ExecutionState.class), eq(false), eq(todaysDate));

        parallelExecutors.add(flmAlgorithmExecutor1);
        parallelExecutors.add(flmAlgorithmExecutor2);
        parallelExecutors.add(flmAlgorithmExecutor3);

        parallelExecutors.parallelStream().forEach(FlmAlgorithmExecutor::executeActivity);

        //assert that non-settings based kpis are only executed once for all the three executors for the same day. and 3 calls for usual settings based executions
        final int ifFirstOneTriggersNonSettingsKpis = mockingDetails(kpiCalculationExecutor).getInvocations().stream().findFirst().get().getMethod()
                .getName()
                .equals("nonSettingsBasedExecute") ? 1 : 0;
        final int ifSecondOneTriggersNonSettingsKpis = mockingDetails(kpiCalculationExecutor2).getInvocations().stream().findFirst().get().getMethod()
                .getName()
                .equals("nonSettingsBasedExecute") ? 1 : 0;
        final int ifThirdOneTriggersNonSettingsKpis = mockingDetails(kpiCalculationExecutor3).getInvocations().stream().findFirst().get().getMethod()
                .getName()
                .equals("nonSettingsBasedExecute") ? 1 : 0;
        assertThat(ifFirstOneTriggersNonSettingsKpis + ifSecondOneTriggersNonSettingsKpis + ifThirdOneTriggersNonSettingsKpis).isEqualTo(1); //assert only one of them triggers

        verify(cellSettingsExecutor, times(3)).execute(any(ExecutionState.class), eq(false), eq(false), eq(todaysDate));
        verify(kpiCalculationExecutor, times(1)).settingsBasedExecute(any(ExecutionState.class), eq(false), eq(false), eq(todaysDate));
        verify(kpiCalculationExecutor2, times(1)).settingsBasedExecute(any(ExecutionState.class), eq(false), eq(false), eq(todaysDate));
        verify(kpiCalculationExecutor3, times(1)).settingsBasedExecute(any(ExecutionState.class), eq(false), eq(false), eq(todaysDate));
        verify(cellSettingsHistoryExecutor, times(3)).execute(any(ExecutionState.class), eq(false), eq(false), eq(todaysDate));
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
                .withExecutionDao(executionDao);
    }
}

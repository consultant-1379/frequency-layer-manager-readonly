/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing;

import static com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo.OverlappingFlag.NOT_OVERLAPPING;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo.OverlappingFlag.OVERLAP_DROP_NEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.retriever.PolicyOutputEventRetriever;
import com.ericsson.oss.services.sonom.flm.metric.FlmMetricHelper;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.CellFlmKpiRetriever;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.SectorBusyHourRetriever;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementSender;

/**
 * Unit tests for {@link LoadBalancingExecutorTest} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadBalancingExecutorTest {

    private static final String EXECUTION_ID = "FLM_1";
    private static final int CONFIGURATION_ID = 1;
    private static final int ONCE = 1;
    private static final String EXECUTION_DATE = LocalDate.of(2_021, 3, 31).toString();

    private Condition<Throwable> expectedErrorMessage;

    @Mock
    private LoadBalancingCalculatorFactory factory;

    @Mock
    private FlmMetricHelper metricHelper;

    @Mock
    private PersistenceHandler persistenceHandler;

    @Mock
    private CmStore cmStore;

    @Mock
    private CmSectorCellStore cmSectorCellStore;

    @Mock
    private SectorBusyHourRetriever sectorBusyHourRetriever;

    @Mock
    private CellFlmKpiRetriever cellFlmKpiRetriever;

    @Mock
    private PolicyOutputEventRetriever policyOutputEventRetriever;

    @Mock
    ChangeElementSender changeElementSender;

    @Mock
    ActivationPolicySender activationPolicySender;

    private LoadBalancingExecutor objectUnderTest;

    @Test
    public void whenExecutorWithDifferentStateCalled_thenNothingHappens() throws FlmAlgorithmException {
        final Execution execution = buildExecution(ExecutionState.CELL_SETTINGS_HISTORY);
        objectUnderTest = buildLoadBalancingExecutor(execution, cmStore, sectorBusyHourRetriever, cellFlmKpiRetriever, policyOutputEventRetriever);

        objectUnderTest.execute(ExecutionState.CELL_SETTINGS_HISTORY, false, true, StringUtils.EMPTY);

        verify(changeElementSender, never()).postChangeElements(eq(EXECUTION_ID), eq(Collections.emptyList()));
        verify(activationPolicySender, never()).sendActivationPolicyToKafka(eq(EXECUTION_ID), nullable(String.class));
        verify(persistenceHandler, never()).persistExecutionStatus(eq(ExecutionState.LOAD_BALANCING), eq(false));
        verify(metricHelper, never()).incrementFlmMetric(eq(FlmMetric.FLM_LOAD_BALANCING_IN_MILLIS), anyLong());
        verify(metricHelper, never()).incrementFlmMetric(eq(FlmMetric.FLM_NUMBER_OF_CHANGE_ELEMENTS_SENT), anyLong());
        verify(metricHelper, never()).incrementFlmMetric(eq(FlmMetric.FLM_CHANGE_ELEMENT_SENDING_TIME_IN_MILLIS), anyLong());

        assertThat(execution.getState()).isEqualTo(ExecutionState.CELL_SETTINGS_HISTORY);
    }

    @Test
    public void whenNoChangeIsNeeded_thenPostChangeElementsIsNotCalled() throws FlmAlgorithmException, SQLException {
        final Execution execution = buildExecution(ExecutionState.LOAD_BALANCING);
        objectUnderTest = buildLoadBalancingExecutor(execution, cmStore, sectorBusyHourRetriever, cellFlmKpiRetriever, policyOutputEventRetriever);

        when(factory.create(eq(LoadBalancingCalculatorFactory.LoadBalancingType.LBDAR), eq(execution),
                eq(new HashMap<>()), eq(cmStore), any(), eq(buildPolicyOutputEventsWithNoOverlapping().stream().map(Pair::getLeft).collect(Collectors.toList()))))
                        .thenReturn(Optional.of(policyOutputEvents -> Collections.emptyList()));
        when(persistenceHandler.persistExecutionStatus(ExecutionState.LOAD_BALANCING, false)).thenReturn(1L);
        when(metricHelper.getTimeElapsedInMillis(anyLong())).thenReturn(1L);

        when(cmStore.getCmSectorCellStore()).thenReturn(cmSectorCellStore);
        when(cmSectorCellStore.getFullSectors()).thenReturn(buildTopologySectors());
        when(policyOutputEventRetriever.getPolicyOutputEvents(EXECUTION_ID)).thenReturn(buildPolicyOutputEventsWithNoOverlapping());

        objectUnderTest.execute(ExecutionState.LOAD_BALANCING, false, true, EXECUTION_DATE);

        verify(activationPolicySender, never()).sendActivationPolicyToKafka(eq(EXECUTION_ID), nullable(String.class));
        verify(persistenceHandler, times(ONCE)).persistExecutionStatus(eq(ExecutionState.LOAD_BALANCING), eq(false));
        verify(metricHelper, times(ONCE)).incrementFlmMetric(eq(FlmMetric.FLM_LOAD_BALANCING_IN_MILLIS), anyLong());
        verify(metricHelper, never()).incrementFlmMetric(eq(FlmMetric.FLM_NUMBER_OF_CHANGE_ELEMENTS_SENT), anyLong());
        verify(metricHelper, never()).incrementFlmMetric(eq(FlmMetric.FLM_CHANGE_ELEMENT_SENDING_TIME_IN_MILLIS), anyLong());
        assertThat(execution.getState()).isEqualTo(ExecutionState.CELL_SETTINGS_HISTORY);
    }

    @Test
    public void whenSomethingNeedsToChange_thenPostChangeElementsIsCalledOnceWithOpenLoop() throws FlmAlgorithmException, SQLException {
        final Execution execution = buildExecution(ExecutionState.LOAD_BALANCING);
        execution.setOpenLoop(true);
        objectUnderTest = buildLoadBalancingExecutor(execution, cmStore, sectorBusyHourRetriever, cellFlmKpiRetriever, policyOutputEventRetriever);

        when(factory.create(eq(LoadBalancingCalculatorFactory.LoadBalancingType.LBDAR), eq(execution), eq(new HashMap<>()), eq(cmStore),
                any(CellKpiStore.class), eq(buildPolicyOutputEventsWithNoOverlapping().stream().map(Pair::getLeft).collect(Collectors.toList()))))
                        .thenReturn(Optional
                                .of(policyOutputEvents -> Collections.singletonList(ImmutablePair.of(new ChangeElement(), new ChangeElement()))));
        when(metricHelper.getTimeElapsedInMillis(anyLong())).thenReturn(1L);

        when(cmStore.getCmSectorCellStore()).thenReturn(cmSectorCellStore);
        when(cmSectorCellStore.getFullSectors()).thenReturn(buildTopologySectors());
        when(policyOutputEventRetriever.getPolicyOutputEvents(EXECUTION_ID)).thenReturn(buildPolicyOutputEventsWithNoOverlapping());

        objectUnderTest.execute(ExecutionState.LOAD_BALANCING, false, true, EXECUTION_DATE);

        verify(changeElementSender, times(ONCE)).postChangeElements(eq(EXECUTION_ID),
                eq(Collections.singletonList(ImmutablePair.of(new ChangeElement(), new ChangeElement()))));
        verify(activationPolicySender, never()).sendActivationPolicyToKafka(eq(EXECUTION_ID), nullable(String.class));
        verify(persistenceHandler, times(ONCE)).persistExecutionStatus(eq(ExecutionState.LOAD_BALANCING), eq(false));
        verify(metricHelper, times(ONCE)).incrementFlmMetric(eq(FlmMetric.FLM_LOAD_BALANCING_IN_MILLIS), anyLong());
        verify(metricHelper, times(ONCE)).incrementFlmMetric(eq(FlmMetric.FLM_NUMBER_OF_CHANGE_ELEMENTS_SENT), anyLong());
        verify(metricHelper, times(ONCE)).incrementFlmMetric(eq(FlmMetric.FLM_CHANGE_ELEMENT_SENDING_TIME_IN_MILLIS), anyLong());
        assertThat(execution.getState()).isEqualTo(ExecutionState.CELL_SETTINGS_HISTORY);
    }

    @Test
    public void whenSomethingNeedsToChange_thenPostChangeElementsIsCalledOnceWithClosedLoop() throws FlmAlgorithmException, SQLException {
        final Execution execution = buildExecution(ExecutionState.LOAD_BALANCING);
        execution.setOpenLoop(false);
        objectUnderTest = buildLoadBalancingExecutor(execution, cmStore, sectorBusyHourRetriever, cellFlmKpiRetriever, policyOutputEventRetriever);

        when(factory.create(eq(LoadBalancingCalculatorFactory.LoadBalancingType.LBDAR), eq(execution), eq(new HashMap<>()), eq(cmStore),
                any(CellKpiStore.class), eq(buildPolicyOutputEventsWithNoOverlapping().stream().map(Pair::getLeft).collect(Collectors.toList()))))
                        .thenReturn(Optional
                                .of(policyOutputEvents -> Collections.singletonList(ImmutablePair.of(new ChangeElement(), new ChangeElement()))));
        when(metricHelper.getTimeElapsedInMillis(anyLong())).thenReturn(1L);

        when(cmStore.getCmSectorCellStore()).thenReturn(cmSectorCellStore);
        when(cmSectorCellStore.getFullSectors()).thenReturn(buildTopologySectors());
        when(policyOutputEventRetriever.getPolicyOutputEvents(EXECUTION_ID)).thenReturn(buildPolicyOutputEventsWithNoOverlapping());

        objectUnderTest.execute(ExecutionState.LOAD_BALANCING, false, true, EXECUTION_DATE);

        verify(changeElementSender, times(ONCE)).postChangeElements(eq(EXECUTION_ID),
                eq(Collections.singletonList(ImmutablePair.of(new ChangeElement(), new ChangeElement()))));
        verify(activationPolicySender, times(ONCE)).sendActivationPolicyToKafka(eq(EXECUTION_ID), nullable(String.class));
        verify(persistenceHandler, times(ONCE)).persistExecutionStatus(eq(ExecutionState.LOAD_BALANCING), eq(false));
        verify(metricHelper, times(ONCE)).incrementFlmMetric(eq(FlmMetric.FLM_LOAD_BALANCING_IN_MILLIS), anyLong());
        verify(metricHelper, times(ONCE)).incrementFlmMetric(eq(FlmMetric.FLM_NUMBER_OF_CHANGE_ELEMENTS_SENT), anyLong());
        verify(metricHelper, times(ONCE)).incrementFlmMetric(eq(FlmMetric.FLM_CHANGE_ELEMENT_SENDING_TIME_IN_MILLIS), anyLong());
        assertThat(execution.getState()).isEqualTo(ExecutionState.CELL_SETTINGS_HISTORY);
    }

    @Test
    public void whenUnknownLoadBalancingCalculatorType_thenExceptionIsThrown() throws FlmAlgorithmException, SQLException {
        final Execution execution = buildExecution(ExecutionState.LOAD_BALANCING);
        objectUnderTest = buildLoadBalancingExecutor(execution, cmStore, sectorBusyHourRetriever, cellFlmKpiRetriever, policyOutputEventRetriever);

        when(cmStore.getCmSectorCellStore()).thenReturn(cmSectorCellStore);
        when(cmSectorCellStore.getFullSectors()).thenReturn(buildTopologySectors());
        when(policyOutputEventRetriever.getPolicyOutputEvents(EXECUTION_ID)).thenReturn(buildPolicyOutputEventsWithNoOverlapping());
        expectedErrorMessage = new Condition<Throwable>() {
            @Override
            public boolean matches(final Throwable value) {
                return value.getMessage().contains("Failed to find LoadBalancingCalculator for type LBDAR");
            }
        };
        assertThatThrownBy(() -> objectUnderTest.execute(ExecutionState.LOAD_BALANCING, false, true, EXECUTION_DATE))
                .isInstanceOf(FlmAlgorithmException.class)
                .satisfies(expectedErrorMessage);

        verifyZeroInteractions(changeElementSender);
        verifyZeroInteractions(activationPolicySender);
        verifyZeroInteractions(persistenceHandler);
        verifyZeroInteractions(metricHelper);
        assertThat(execution.getState()).isEqualTo(ExecutionState.LOAD_BALANCING);
    }

    @Test
    public void whenAllSectorsAreOverlapping_thenExceptionIsThrown() throws FlmAlgorithmException, SQLException {
        final Execution execution = buildExecution(ExecutionState.LOAD_BALANCING);
        execution.setOpenLoop(false);
        objectUnderTest = buildLoadBalancingExecutor(execution, cmStore, sectorBusyHourRetriever, cellFlmKpiRetriever, policyOutputEventRetriever);

        when(policyOutputEventRetriever.getPolicyOutputEvents(EXECUTION_ID)).thenReturn(buildPolicyOutputEventsWithOverlapping());

        expectedErrorMessage = new Condition<Throwable>() {
            @Override
            public boolean matches(final Throwable value) {
                return value.getMessage().contains("All sectors are overlapping and had to be dropped");
            }
        };
        assertThatThrownBy(() -> objectUnderTest.execute(ExecutionState.LOAD_BALANCING, false, true, EXECUTION_DATE))
                .isInstanceOf(FlmAlgorithmException.class)
                .satisfies(expectedErrorMessage);

        verifyZeroInteractions(changeElementSender);
        verifyZeroInteractions(activationPolicySender);
        verifyZeroInteractions(persistenceHandler);
        verifyZeroInteractions(metricHelper);
        assertThat(execution.getState()).isEqualTo(ExecutionState.LOAD_BALANCING);
    }

    @Test
    public void whenPolicyOutputEventRetrieverFails_thenFlmAlgorithmExceptionIsThrown() throws FlmAlgorithmException, SQLException {
        final Execution execution = buildExecution(ExecutionState.LOAD_BALANCING);
        objectUnderTest = buildLoadBalancingExecutor(execution, cmStore, sectorBusyHourRetriever, cellFlmKpiRetriever, policyOutputEventRetriever);
        when(policyOutputEventRetriever.getPolicyOutputEvents(anyString())).thenThrow(SQLException.class);
        expectedErrorMessage = new Condition<Throwable>() {
            @Override
            public boolean matches(final Throwable value) {
                return value.getMessage().contains("java.sql.SQLException");
            }
        };
        assertThatThrownBy(() -> objectUnderTest.execute(ExecutionState.LOAD_BALANCING, false, true, EXECUTION_DATE))
                .isInstanceOf(FlmAlgorithmException.class)
                .satisfies(expectedErrorMessage);

        verifyZeroInteractions(changeElementSender);
        verifyZeroInteractions(activationPolicySender);
        verifyZeroInteractions(persistenceHandler);
        verifyZeroInteractions(metricHelper);
        assertThat(execution.getState()).isEqualTo(ExecutionState.LOAD_BALANCING);
    }

    private static List<Pair<PolicyOutputEvent, OverlapInfo>> buildPolicyOutputEventsWithNoOverlapping() {
        final PolicyOutputEvent policyOutputEvent = new PolicyOutputEvent("name", "version", "nameSpace", "source", "target", 1L, "FLM_1", null, null);
        return Collections.singletonList(Pair.of(policyOutputEvent, OverlapInfo.of(NOT_OVERLAPPING, "")));
    }

    private static List<Pair<PolicyOutputEvent, OverlapInfo>> buildPolicyOutputEventsWithOverlapping() {
        final PolicyOutputEvent policyOutputEvent = new PolicyOutputEvent("name", "version", "nameSpace", "source", "target", 1L, "FLM_1", null, null);
        return Collections.singletonList(Pair.of(policyOutputEvent, OverlapInfo.of(OVERLAP_DROP_NEEDED, "")));
    }

    private Execution buildExecution(final ExecutionState executionState) {
        final Execution execution = new Execution();
        execution.setId(EXECUTION_ID);
        execution.setConfigurationId(CONFIGURATION_ID);
        execution.setCustomizedGlobalSettings(new HashMap<>());
        execution.setState(executionState);
        execution.setStartTime(Timestamp.valueOf("2021-02-12 00:19:00"));
        return execution;
    }

    private LoadBalancingExecutor buildLoadBalancingExecutor(final Execution execution, final CmStore cmStore,
            final SectorBusyHourRetriever sectorBusyHourRetriever,
            final CellFlmKpiRetriever cellFlmKpiRetriever,
            final PolicyOutputEventRetriever policyOutputEventRetriever) {
        final LoadBalancingExecutor loadBalancingExecutor = new LoadBalancingExecutor(execution, cmStore, metricHelper,
                persistenceHandler, sectorBusyHourRetriever, cellFlmKpiRetriever, policyOutputEventRetriever, changeElementSender,
                activationPolicySender);
        loadBalancingExecutor.setLoadBalancingCalculatorFactory(factory);
        return loadBalancingExecutor;
    }

    private List<TopologySector> buildTopologySectors() {
        return Collections.singletonList(
                new TopologySector(1L, Collections.singleton(new Cell(1L, 1, "cellFdn", 10000, "outdoor", "undefined"))));
    }
}
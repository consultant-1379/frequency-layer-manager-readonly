/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.apache.kafka.clients.producer.Callback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObject;
import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObjects;
import com.ericsson.oss.services.sonom.activation.kafka.ActivationPolicy;
import com.ericsson.oss.services.sonom.activation.kafka.ActivationPolicyKey;
import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.api.TopologyType;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElementStatus;
import com.ericsson.oss.services.sonom.cm.service.change.api.ParameterChanges;
import com.ericsson.oss.services.sonom.cm.service.change.api.ProposedChange;
import com.ericsson.oss.services.sonom.common.kafka.producer.KafkaMessageProducer;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmCellGroupRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmNodeCellRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmNodeFrequencyRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmNodeProfileRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmSectorCellRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDao;
import com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.LbdarCalculator;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileChangesCalculatorImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.retriever.PolicyOutputEventRetriever;
import com.ericsson.oss.services.sonom.flm.loadbalancing.testutils.TestConstants;
import com.ericsson.oss.services.sonom.flm.loadbalancing.testutils.TopologyObjectBuilder;
import com.ericsson.oss.services.sonom.flm.metric.FlmMetricHelper;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.CellFlmKpiRetriever;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.SectorBusyHourRetriever;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.settings.CellFlmSettingsRetriever;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementSender;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementSenderImpl;

/**
 * Unit tests verifying interaction between {@link LoadBalancingExecutor} with classes from cm service.
 *
 * @see LoadBalancingExecutorTest for basic unit tests.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadBalancingExecutorIntTest {
    private static final String PENDING_APPROVAL = "PENDING_APPROVAL";
    private static final String PROPOSED = "PROPOSED";

    private static final String EXECUTION_ID = "FLM_1";
    private static final String SOURCE_OF_CHANGE = "alg_FLM_1";
    private static final int OSS_ID_1 = 1;
    private static final String CHANGE_ID_1 = "1";
    private static final String CHANGE_ID_2 = "2";
    private static final String CHANGE_ID_3 = "3";
    private static final String ATTRIBUTE_NODE_TYPE = "nodeType";
    private static final String ATTRIBUTE_FDN = "fdn";
    private static final String OBJECT_TYPE_IDLE_MODE_PRIO = "idlemodeprioatrelease";
    private static final String OPERATION_TYPE_CREATE = "CREATE";
    private static final String OPERATION_TYPE_MODIFY = "MODIFY";
    private static final String OPERATION_TYPE_DELETE = "DELETE";

    @Mock
    private PersistenceHandler persistenceHandlerMock;

    @Mock
    private FlmMetricHelper flmMetricHelperMock;

    @Mock
    private CmRestExecutor cmRestExecutorMock;

    @Mock
    private SectorBusyHourRetriever sectorBusyHourRetrieverMock;

    @Mock
    private CellFlmKpiRetriever cellFlmKpiRetriever;

    @Mock
    private CellFlmSettingsRetriever cellFlmSettingsRetriever;

    @Mock
    private PolicyOutputEventRetriever policyOutputEventRetriever;

    @Mock
    private KafkaMessageProducer<ActivationPolicyKey, ActivationPolicy> kafkaMessageProducerMock;

    @Mock
    private LbdarDao lbdarDao;

    @Captor
    private ArgumentCaptor<List<ChangeElement>> changeElementListArgumentCaptor;

    private ChangeElementSender changeElementSender;

    private ActivationPolicySender activationPolicySender;

    @Test
    public void whenExecutorExecutes_thenChangeElementsAreCreatedWithOpenLoop() throws FlmAlgorithmException, SQLException {
        final CmCellGroupRetriever cmCellGroupRetrieverMock = mock(CmCellGroupRetriever.class);
        final CmSectorCellRetriever cmSectorCellRetriever = new CmSectorCellRetriever(cmRestExecutorMock);
        final CmNodeCellRetriever cmNodeCellRetriever = new CmNodeCellRetriever(cmRestExecutorMock);
        final CmNodeProfileRetriever cmNodeProfileRetriever = new CmNodeProfileRetriever(cmRestExecutorMock);
        final CmNodeFrequencyRetriever cmNodeFrequencyRetriever = new CmNodeFrequencyRetriever(cmRestExecutorMock);
        ProfileChangesCalculatorImpl.setCellFlmSettingsRetriever(cellFlmSettingsRetriever);

        final Execution execution = buildOpenLoopExecution();
        final TopologyObjects sectorsWithCells = buildSectorsWithCells();
        final TopologyObjects nodesWithCells = buildNodesWithCells();
        final TopologyObjects nodesWithFrequencies = buildNodesWithFrequencies();
        final TopologyObjects nodesWithProfiles = buildNodesWithProfiles();

        when(persistenceHandlerMock.persistExecutionStatus(ExecutionState.LOAD_BALANCING, false)).thenReturn(1L);
        when(flmMetricHelperMock.getTimeElapsedInMillis(anyLong())).thenReturn(1L);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenAssociationWithLimitAndPage(TopologyType.SECTOR.toString(),
                TopologyType.CELL.toString(), 7000, null, true)).thenReturn(sectorsWithCells);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(TopologyType.NODE.toString(),
                TopologyType.CELL.toString(), 7000, null, true)).thenReturn(nodesWithCells);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(
                TopologyType.NODE.toString(), TopologyType.IDLEMODEPRIOATRELEASE.toString(), 2000, 1, false))
                        .thenReturn(nodesWithProfiles);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(
                TopologyType.NODE.toString(), TopologyType.IDLEMODEPRIOATRELEASE.toString(), 2000, 2, false))
                        .thenReturn(new TopologyObjects());
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(TopologyType.NODE.toString(),
                TopologyType.EUTRANFREQUENCY.toString(), 7000, null, true)).thenReturn(nodesWithFrequencies);
        when(cmRestExecutorMock.getTopologyObjectByOssIdAndFdn(TopologyType.IDLEMODEPRIOATRELEASE.toString(), 1, TestConstants.P5))
                .thenReturn(TestConstants.TPROFILE5);
        when(sectorBusyHourRetrieverMock.populateSectorIdToBusyHour(eq("2021-02-11"), any())).thenReturn(buildSectorBusyHours());
        when(cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis("2021-02-11T09:00:00")).thenReturn(buildCellKpisNine());
        when(cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis("2021-02-11T10:00:00")).thenReturn(buildCellKpisTen());
        when(cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis("2021-02-11T11:00:00")).thenReturn(buildCellKpisEleven());
        when(policyOutputEventRetriever.getPolicyOutputEvents(execution.getId())).thenReturn(buildPolicyOutputEvents());
        when(cellFlmSettingsRetriever.retrieveGivenCellSettingValue(anyString(), anyInt(), anyString(), anyString())).thenReturn("20");

        final CmSectorCellStore cmSectorCellStore = new CmSectorCellStore(cmSectorCellRetriever, cmCellGroupRetrieverMock, execution);

        mockCreateChangeElementsWithResponseThatReturnsCreatedStatus();

        changeElementSender = new ChangeElementSenderImpl(cmRestExecutorMock);
        activationPolicySender = new ActivationPolicySenderImpl(kafkaMessageProducerMock);

        final LoadBalancingExecutor executor = new LoadBalancingExecutor(
                execution,
                new CmStore(cmSectorCellStore,
                        new CmNodeObjectsStore(cmSectorCellStore, Arrays.asList(
                                TopologyObjectId.of(TestConstants.C1, 1),
                                TopologyObjectId.of(TestConstants.C2, 1),
                                TopologyObjectId.of(TestConstants.C3, 1),
                                TopologyObjectId.of(TestConstants.C4, 1),
                                TopologyObjectId.of(TestConstants.C5, 1),
                                TopologyObjectId.of(TestConstants.C6, 1),
                                TopologyObjectId.of(TestConstants.C7, 1),
                                TopologyObjectId.of(TestConstants.C8, 1),
                                TopologyObjectId.of(TestConstants.C9, 1),
                                TopologyObjectId.of(TestConstants.C10, 1),
                                TopologyObjectId.of(TestConstants.C11, 1)),
                                Arrays.asList(TestConstants.S1, TestConstants.S2, TestConstants.S3),
                                cmNodeCellRetriever, cmNodeProfileRetriever, cmNodeFrequencyRetriever)),
                flmMetricHelperMock, persistenceHandlerMock, sectorBusyHourRetrieverMock, cellFlmKpiRetriever, policyOutputEventRetriever,
                changeElementSender, activationPolicySender);
        executor.setLoadBalancingCalculatorFactory(new LoadBalancingCalculatorFactory(lbdarDao));

        executor.execute(ExecutionState.LOAD_BALANCING, false, true, "2021-02-12");

        verify(cmRestExecutorMock, times(1)).createChangeElementsWithResponse(changeElementListArgumentCaptor.capture());
        verify(kafkaMessageProducerMock, times(0))
                .sendKafkaMessage(anyString(), any(ActivationPolicyKey.class), any(ActivationPolicy.class), any(Callback.class));

        final List<ChangeElement> resultChangeElements = changeElementListArgumentCaptor.getValue();
        verifyResult(resultChangeElements, PENDING_APPROVAL);
    }

    @Test
    public void whenExecutorExecutes_thenChangeElementsAreCreatedWithOpenLoopAreSameAsOriginal() throws FlmAlgorithmException, SQLException {
        final CmCellGroupRetriever cmCellGroupRetrieverMock = mock(CmCellGroupRetriever.class);
        final CmSectorCellRetriever cmSectorCellRetriever = new CmSectorCellRetriever(cmRestExecutorMock);
        final CmNodeCellRetriever cmNodeCellRetriever = new CmNodeCellRetriever(cmRestExecutorMock);
        final CmNodeProfileRetriever cmNodeProfileRetriever = new CmNodeProfileRetriever(cmRestExecutorMock);
        final CmNodeFrequencyRetriever cmNodeFrequencyRetriever = new CmNodeFrequencyRetriever(cmRestExecutorMock);
        ProfileChangesCalculatorImpl.setCellFlmSettingsRetriever(cellFlmSettingsRetriever);

        final Execution execution = buildOpenLoopExecution();
        final TopologyObjects sectorsWithCells = buildSectorsWithCells();
        final TopologyObjects nodesWithCells = buildNodesWithCells();
        final TopologyObjects nodesWithFrequencies = buildNodesWithFrequencies();
        final TopologyObjects nodesWithProfiles = buildNodesWithProfiles();

        when(persistenceHandlerMock.persistExecutionStatus(ExecutionState.LOAD_BALANCING, false)).thenReturn(1L);
        when(flmMetricHelperMock.getTimeElapsedInMillis(anyLong())).thenReturn(1L);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenAssociationWithLimitAndPage(TopologyType.SECTOR.toString(),
                TopologyType.CELL.toString(), 7000, null, true)).thenReturn(sectorsWithCells);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(TopologyType.NODE.toString(),
                TopologyType.CELL.toString(), 7000, null, true)).thenReturn(nodesWithCells);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(
                TopologyType.NODE.toString(), TopologyType.IDLEMODEPRIOATRELEASE.toString(), 2000, 1, false))
                .thenReturn(nodesWithProfiles);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(
                TopologyType.NODE.toString(), TopologyType.IDLEMODEPRIOATRELEASE.toString(), 2000, 2, false))
                .thenReturn(new TopologyObjects());
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(TopologyType.NODE.toString(),
                TopologyType.EUTRANFREQUENCY.toString(), 7000, null, true)).thenReturn(nodesWithFrequencies);
        when(sectorBusyHourRetrieverMock.populateSectorIdToBusyHour(eq("2021-02-11"), any())).thenReturn(buildSectorBusyHours());
        when(cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis("2021-02-11T09:00:00")).thenReturn(buildCellKpisNine());
        when(cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis("2021-02-11T10:00:00")).thenReturn(buildCellKpisTen());
        when(cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis("2021-02-11T11:00:00")).thenReturn(buildCellKpisEleven());
        when(policyOutputEventRetriever.getPolicyOutputEvents(execution.getId())).thenReturn(buildPolicyOutputEvents());
        when(cellFlmSettingsRetriever.retrieveGivenCellSettingValue(anyString(), anyInt(), anyString(), anyString())).thenReturn("90");

        final CmSectorCellStore cmSectorCellStore = new CmSectorCellStore(cmSectorCellRetriever, cmCellGroupRetrieverMock, execution);

        mockCreateChangeElementsWithResponseThatReturnsCreatedStatus();

        changeElementSender = new ChangeElementSenderImpl(cmRestExecutorMock);
        activationPolicySender = new ActivationPolicySenderImpl(kafkaMessageProducerMock);

        final LoadBalancingExecutor executor = new LoadBalancingExecutor(
                execution,
                new CmStore(cmSectorCellStore,
                        new CmNodeObjectsStore(cmSectorCellStore, Arrays.asList(
                                TopologyObjectId.of(TestConstants.C1, 1),
                                TopologyObjectId.of(TestConstants.C2, 1),
                                TopologyObjectId.of(TestConstants.C3, 1),
                                TopologyObjectId.of(TestConstants.C4, 1),
                                TopologyObjectId.of(TestConstants.C5, 1),
                                TopologyObjectId.of(TestConstants.C6, 1),
                                TopologyObjectId.of(TestConstants.C7, 1),
                                TopologyObjectId.of(TestConstants.C8, 1),
                                TopologyObjectId.of(TestConstants.C9, 1),
                                TopologyObjectId.of(TestConstants.C10, 1),
                                TopologyObjectId.of(TestConstants.C11, 1)),
                                Arrays.asList(TestConstants.S1, TestConstants.S2, TestConstants.S3),
                                cmNodeCellRetriever, cmNodeProfileRetriever, cmNodeFrequencyRetriever)),
                flmMetricHelperMock, persistenceHandlerMock, sectorBusyHourRetrieverMock, cellFlmKpiRetriever, policyOutputEventRetriever,
                changeElementSender, activationPolicySender);
        executor.setLoadBalancingCalculatorFactory(new LoadBalancingCalculatorFactory(lbdarDao));

        executor.execute(ExecutionState.LOAD_BALANCING, false, true, "2021-02-12");

        verify(cmRestExecutorMock, times(1)).createChangeElementsWithResponse(changeElementListArgumentCaptor.capture());
        verify(kafkaMessageProducerMock, times(0))
                .sendKafkaMessage(anyString(), any(ActivationPolicyKey.class), any(ActivationPolicy.class), any(Callback.class));

        final List<ChangeElement> resultChangeElements = changeElementListArgumentCaptor.getValue();
        verifyResultForSameChangeAsOriginal(resultChangeElements, PENDING_APPROVAL);
    }

    @Test
    public void whenExecutorExecutes_thenChangeElementsAreCreatedWithClosedLoop() throws FlmAlgorithmException, SQLException {
        final CmCellGroupRetriever cmCellGroupRetrieverMock = mock(CmCellGroupRetriever.class);
        final CmSectorCellRetriever cmSectorCellRetriever = new CmSectorCellRetriever(cmRestExecutorMock);
        final CmNodeCellRetriever cmNodeCellRetriever = new CmNodeCellRetriever(cmRestExecutorMock);
        final CmNodeProfileRetriever cmNodeProfileRetriever = new CmNodeProfileRetriever(cmRestExecutorMock);
        final CmNodeFrequencyRetriever cmNodeFrequencyRetriever = new CmNodeFrequencyRetriever(cmRestExecutorMock);
        ProfileChangesCalculatorImpl.setCellFlmSettingsRetriever(cellFlmSettingsRetriever);

        final Execution execution = buildClosedLoopExecution();
        final TopologyObjects sectorsWithCells = buildSectorsWithCells();
        final TopologyObjects nodesWithCells = buildNodesWithCells();
        final TopologyObjects nodesWithFrequencies = buildNodesWithFrequencies();
        final TopologyObjects nodesWithProfiles = buildNodesWithProfiles();

        when(persistenceHandlerMock.persistExecutionStatus(ExecutionState.LOAD_BALANCING, false)).thenReturn(1L);
        when(flmMetricHelperMock.getTimeElapsedInMillis(anyLong())).thenReturn(1L);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenAssociationWithLimitAndPage(TopologyType.SECTOR.toString(),
                TopologyType.CELL.toString(), 7000, null, true)).thenReturn(sectorsWithCells);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(TopologyType.NODE.toString(),
                TopologyType.CELL.toString(), 7000, null, true)).thenReturn(nodesWithCells);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(
                TopologyType.NODE.toString(), TopologyType.IDLEMODEPRIOATRELEASE.toString(), 2000, 1, false))
                        .thenReturn(nodesWithProfiles);
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(
                TopologyType.NODE.toString(), TopologyType.IDLEMODEPRIOATRELEASE.toString(), 2000, 2, false))
                        .thenReturn(new TopologyObjects());
        when(cmRestExecutorMock.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(TopologyType.NODE.toString(),
                TopologyType.EUTRANFREQUENCY.toString(), 7000, null, true)).thenReturn(nodesWithFrequencies);
        when(cmRestExecutorMock.getTopologyObjectByOssIdAndFdn(TopologyType.IDLEMODEPRIOATRELEASE.toString(), 1, TestConstants.P5))
                .thenReturn(TestConstants.TPROFILE5);
        when(sectorBusyHourRetrieverMock.populateSectorIdToBusyHour(eq("2021-02-11"), any())).thenReturn(buildSectorBusyHours());
        when(cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis("2021-02-11T09:00:00")).thenReturn(buildCellKpisNine());
        when(cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis("2021-02-11T10:00:00")).thenReturn(buildCellKpisTen());
        when(cellFlmKpiRetriever.retrieveNotVisibleCellHourlyKpis("2021-02-11T11:00:00")).thenReturn(buildCellKpisEleven());
        when(policyOutputEventRetriever.getPolicyOutputEvents(execution.getId())).thenReturn(buildPolicyOutputEvents());
        when(cellFlmSettingsRetriever.retrieveGivenCellSettingValue(anyString(), anyInt(), anyString(), anyString())).thenReturn("20");

        final CmSectorCellStore cmSectorCellStore = new CmSectorCellStore(cmSectorCellRetriever, cmCellGroupRetrieverMock, execution);

        mockCreateChangeElementsWithResponseThatReturnsCreatedStatus();

        changeElementSender = new ChangeElementSenderImpl(cmRestExecutorMock);
        activationPolicySender = new ActivationPolicySenderImpl(kafkaMessageProducerMock);

        final LoadBalancingExecutor executor = new LoadBalancingExecutor(
                execution,
                new CmStore(cmSectorCellStore,
                        new CmNodeObjectsStore(cmSectorCellStore, Arrays.asList(
                                TopologyObjectId.of(TestConstants.C1, 1),
                                TopologyObjectId.of(TestConstants.C2, 1),
                                TopologyObjectId.of(TestConstants.C3, 1),
                                TopologyObjectId.of(TestConstants.C4, 1),
                                TopologyObjectId.of(TestConstants.C5, 1),
                                TopologyObjectId.of(TestConstants.C6, 1),
                                TopologyObjectId.of(TestConstants.C7, 1),
                                TopologyObjectId.of(TestConstants.C8, 1),
                                TopologyObjectId.of(TestConstants.C9, 1),
                                TopologyObjectId.of(TestConstants.C10, 1),
                                TopologyObjectId.of(TestConstants.C11, 1)),
                                Arrays.asList(TestConstants.S1, TestConstants.S2, TestConstants.S3),
                                cmNodeCellRetriever, cmNodeProfileRetriever, cmNodeFrequencyRetriever)),
                flmMetricHelperMock, persistenceHandlerMock, sectorBusyHourRetrieverMock, cellFlmKpiRetriever, policyOutputEventRetriever,
                changeElementSender, activationPolicySender);
        executor.setLoadBalancingCalculatorFactory(new LoadBalancingCalculatorFactory(lbdarDao));

        executor.execute(ExecutionState.LOAD_BALANCING, false, true, "2021-02-12");

        verify(cmRestExecutorMock, times(1)).createChangeElementsWithResponse(changeElementListArgumentCaptor.capture());
        verify(kafkaMessageProducerMock, times(1))
                .sendKafkaMessage(nullable(String.class), nullable(ActivationPolicyKey.class), nullable(ActivationPolicy.class), any(Callback.class));
        verify(lbdarDao, times(1)).insertLeakageCells(any(String.class), any(Long.class), any(Set.class));

        final List<ChangeElement> resultChangeElements = changeElementListArgumentCaptor.getValue();
        verifyResult(resultChangeElements, PROPOSED);
    }

    private void mockCreateChangeElementsWithResponseThatReturnsCreatedStatus() {
        final RestResponse mockedRestResponse = mock(RestResponse.class);
        when(mockedRestResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(cmRestExecutorMock.createChangeElementsWithResponse(anyList())).thenReturn(mockedRestResponse);
    }

    private Map<CellKpi, CellKpis> buildCellKpisNine() {
        final Map<CellKpi, CellKpis> cellKpis = new HashMap<>();
        cellKpis.put(new CellKpi(TestConstants.C1, OSS_ID_1, "2021-02-11T09:00:00"), new CellKpis(500, 0.35, 0, 0, 0, 4, 0));
        cellKpis.put(new CellKpi(TestConstants.C2, OSS_ID_1, "2021-02-11T09:00:00"), new CellKpis(500, 0.35, 0, 0, 0, 4, 0));
        return cellKpis;
    }

    private Map<CellKpi, CellKpis> buildCellKpisTen() {
        final Map<CellKpi, CellKpis> cellKpis = new HashMap<>();
        cellKpis.put(new CellKpi(TestConstants.C3, OSS_ID_1, "2021-02-11T10:00:00"), new CellKpis(1000, 0.5, 0, 0, 5, 0, 0));
        cellKpis.put(new CellKpi(TestConstants.C4, OSS_ID_1, "2021-02-11T10:00:00"), new CellKpis(500, 0.35, 0, 0, 0, 4, 0));
        cellKpis.put(new CellKpi(TestConstants.C5, OSS_ID_1, "2021-02-11T10:00:00"), new CellKpis(1000, 0.5, 0, 0, 5, 0, 0));
        return cellKpis;
    }

    private Map<CellKpi, CellKpis> buildCellKpisEleven() {
        final Map<CellKpi, CellKpis> cellKpis = new HashMap<>();
        cellKpis.put(new CellKpi(TestConstants.C6, OSS_ID_1, "2021-02-11T11:00:00"), new CellKpis(55.5056, 1.493, 0, 0, 687, 749, 0));
        cellKpis.put(new CellKpi(TestConstants.C7, OSS_ID_1, "2021-02-11T11:00:00"), new CellKpis(17.65, 0.2235, 242, 105, 0, 4, 0));
        cellKpis.put(new CellKpi(TestConstants.C8, OSS_ID_1, "2021-02-11T11:00:00"), new CellKpis(48.967, 0.28263, 1670, 0, 5, 0, 0));
        cellKpis.put(new CellKpi(TestConstants.C9, OSS_ID_1, "2021-02-11T11:00:00"), new CellKpis(9.378, 0.07405, 333, 0, 5, 0, 0));
        cellKpis.put(new CellKpi(TestConstants.C10, OSS_ID_1, "2021-02-11T11:00:00"), new CellKpis(4.272, 0.0296, 144, 0, 5, 0, 0));
        cellKpis.put(new CellKpi(TestConstants.C11, OSS_ID_1, "2021-02-11T11:00:00"), new CellKpis(2.472, 0.0384, 76, 0, 5, 0, 0));
        return cellKpis;
    }

    private Map<Long, String> buildSectorBusyHours() {
        final Map<Long, String> sectorBusyHours = new HashMap<>();
        sectorBusyHours.put(TestConstants.S1, "2021-02-11 09:00:00.0");
        sectorBusyHours.put(TestConstants.S2, "2021-02-11 10:00:00.0");
        sectorBusyHours.put(TestConstants.S3, "2021-02-11 11:00:00.0");
        return sectorBusyHours;
    }

    private Execution buildOpenLoopExecution() {
        return buildExecution(Boolean.TRUE);
    }

    private Execution buildClosedLoopExecution() {
        return buildExecution(Boolean.FALSE);
    }

    private Execution buildExecution(final Boolean openLoop) {
        final Execution execution = new Execution();
        execution.setConfigurationId(1);
        execution.setId(EXECUTION_ID);
        execution.setState(ExecutionState.LOAD_BALANCING);
        execution.setStartTime(Timestamp.valueOf("2021-02-12 00:26:00"));
        execution.setOpenLoop(openLoop);
        execution.setWeekendDays("Saturday,Sunday");
        final Map<String, String> customizedGlobalSettings = new HashMap<>();
        customizedGlobalSettings.put("targetPushBack", "2");
        customizedGlobalSettings.put("overrideCCalculator", "No");
        customizedGlobalSettings.put("minLbdarStepsize", "1");
        customizedGlobalSettings.put("maxLbdarStepsize",
                "[{\"BW\":\"1400\", \"value\":\"1\"}, {\"BW\":\"3000\", \"value\":\"2\"}, {\"BW\":\"5000\", \"value\":\"5\"}, {\"BW\":\"10000\", \"value\":\"20\"}, {\"BW\":\"15000\", \"value\":\"25\"}, {\"BW\":\"20000\", \"value\":\"30\"}]");
        customizedGlobalSettings.put("leakageThirdCell", "10");
        customizedGlobalSettings.put("leakageLbqImpact", "20");
        customizedGlobalSettings.put("existingHighPush", "30");

        execution.setCustomizedGlobalSettings(customizedGlobalSettings);
        return execution;
    }

    private TopologyObjects buildNodesWithFrequencies() {
        final TopologyObjects nodesWithFrequencies = new TopologyObjects();
        final TopologyObject node1WithFrequencies = TopologyObjectBuilder.buildNodeWithFrequencies(TestConstants.TNODE1,
                Collections.singletonList(TestConstants.TFREQ1));
        final TopologyObject node2WithFrequencies = TopologyObjectBuilder.buildNodeWithFrequencies(TestConstants.TNODE2,
                Collections.singletonList(TestConstants.TFREQ2));
        final TopologyObject node3WithFrequencies = TopologyObjectBuilder.buildNodeWithFrequencies(TestConstants.TNODE3,
                Arrays.asList(TestConstants.TFREQ3, TestConstants.TFREQ4));
        final TopologyObject node4WithFrequencies = TopologyObjectBuilder.buildNodeWithFrequencies(TestConstants.TNODE4,
                Collections.singletonList(TestConstants.TFREQ5));
        final TopologyObject node5WithFrequencies = TopologyObjectBuilder.buildNodeWithFrequencies(TestConstants.TNODE5,
                Arrays.asList(TestConstants.TFREQ6, TestConstants.TFREQ7, TestConstants.TFREQ8, TestConstants.TFREQ9,
                        TestConstants.TFREQ10, TestConstants.TFREQ11));
        final TopologyObject node6WithFrequencies = TopologyObjectBuilder.buildNodeWithFrequencies(TestConstants.TNODE6,
                Arrays.asList(TestConstants.TFREQ12, TestConstants.TFREQ13, TestConstants.TFREQ14, TestConstants.TFREQ15,
                        TestConstants.TFREQ16, TestConstants.TFREQ17));
        nodesWithFrequencies.setTopologyObjects(Arrays.asList(node1WithFrequencies, node2WithFrequencies, node3WithFrequencies,
                node4WithFrequencies, node5WithFrequencies, node6WithFrequencies));
        return nodesWithFrequencies;
    }

    private TopologyObjects buildNodesWithProfiles() {
        final TopologyObjects nodesWithProfiles = new TopologyObjects();
        final TopologyObject node1WithProfiles = TopologyObjectBuilder.buildNodeWithProfiles(TestConstants.TNODE1,
                Collections.singletonList(TestConstants.TPROFILE1));
        final TopologyObject node2WithProfiles = TopologyObjectBuilder.buildNodeWithProfiles(TestConstants.TNODE2,
                Collections.singletonList(TestConstants.TPROFILE2));
        final TopologyObject node3WithProfiles = TopologyObjectBuilder.buildNodeWithProfiles(TestConstants.TNODE3,
                Arrays.asList(TestConstants.TPROFILE3, TestConstants.TPROFILE4));
        final TopologyObject node5WithProfiles = TopologyObjectBuilder.buildNodeWithProfiles(TestConstants.TNODE5,
                Arrays.asList(TestConstants.TPROFILE6, TestConstants.TPROFILE7));
        final TopologyObject node6WithProfiles = TopologyObjectBuilder.buildNodeWithProfiles(TestConstants.TNODE6,
                Arrays.asList(TestConstants.TPROFILE8, TestConstants.TPROFILE9, TestConstants.TPROFILE10, TestConstants.TPROFILE11));
        nodesWithProfiles.setTopologyObjects(Arrays.asList(node1WithProfiles, node2WithProfiles, node3WithProfiles, node5WithProfiles,
                node6WithProfiles));
        return nodesWithProfiles;
    }

    private TopologyObjects buildNodesWithCells() {
        final TopologyObject node1WithCells = TopologyObjectBuilder.buildNodeWithCells(TestConstants.TNODE1,
                Collections.singletonList(TestConstants.TCELL1));
        final TopologyObject node2WithCells = TopologyObjectBuilder.buildNodeWithCells(TestConstants.TNODE2,
                Collections.singletonList(TestConstants.TCELL2));
        final TopologyObject node3WithCells = TopologyObjectBuilder.buildNodeWithCells(TestConstants.TNODE3,
                Arrays.asList(TestConstants.TCELL3, TestConstants.TCELL4));
        final TopologyObject node4WithCells = TopologyObjectBuilder.buildNodeWithCells(TestConstants.TNODE4,
                Collections.singletonList(TestConstants.TCELL5));
        final TopologyObject node5WithCells = TopologyObjectBuilder.buildNodeWithCells(TestConstants.TNODE5,
                Arrays.asList(TestConstants.TCELL6, TestConstants.TCELL7));
        final TopologyObject node6WithCells = TopologyObjectBuilder.buildNodeWithCells(TestConstants.TNODE6,
                Arrays.asList(TestConstants.TCELL8, TestConstants.TCELL9, TestConstants.TCELL10, TestConstants.TCELL11));
        final TopologyObjects nodesWithCells = new TopologyObjects();
        nodesWithCells.setTopologyObjects(Arrays.asList(node1WithCells, node2WithCells, node3WithCells, node4WithCells, node5WithCells,
                node6WithCells));
        return nodesWithCells;
    }

    private TopologyObjects buildSectorsWithCells() {
        final TopologyObjects sectorsWithCells = new TopologyObjects();
        sectorsWithCells.setTopologyObjects(Arrays.asList(TestConstants.TSECTOR1, TestConstants.TSECTOR2, TestConstants.TSECTOR3));
        return sectorsWithCells;
    }

    private void verifyResultForSameChangeAsOriginal(final List<ChangeElement> resultChangeElements, final String expectedStatus) {

        final List<ProposedChange> testProposedChanges3 = new ArrayList<>(
                buildTestProposedChanges(TestConstants.TCELL7, TestConstants.TNODE5, TestConstants.RESULTPROFILE4));

        final List<ProposedChange> testProposedChanges3Reversed = new ArrayList<>(
                buildReverseTestProposedChanges(TestConstants.TCELL7_REVERSION, TestConstants.TNODE5_REVERSION, TestConstants.RESULTPROFILE4,
                        TestConstants.RESULTPROFILE4_REVERSION));

        assertThat(resultChangeElements.size()).isEqualTo(2);
        assertThat(resultChangeElements.get(0))
                .returns(EXECUTION_ID, from(ChangeElement::getExecutionId))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getSourceOfChange))
                .returns(expectedStatus, from(ChangeElement::getStatus))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getModifiedBy))
                .returns(CHANGE_ID_3, from(ChangeElement::getChangeId))
                .returns(ChangeElement.ChangeType.OPTIMIZATION, from(ChangeElement::getChangeType))
                .returns(testProposedChanges3, ChangeElement::getProposedChanges);
        assertThat(resultChangeElements.get(1))
                .returns(EXECUTION_ID, from(ChangeElement::getExecutionId))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getSourceOfChange))
                .returns(ChangeElementStatus.PENDING_APPROVAL.toString(), from(ChangeElement::getStatus))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getModifiedBy))
                .returns(CHANGE_ID_3, from(ChangeElement::getChangeId))
                .returns(ChangeElement.ChangeType.REVERSION, from(ChangeElement::getChangeType))
                .returns(testProposedChanges3Reversed, from(ChangeElement::getProposedChanges));
    }


    private void verifyResult(final List<ChangeElement> resultChangeElements, final String expectedStatus) {
        final List<ProposedChange> testProposedChanges1 = new ArrayList<>(
                buildTestProposedChanges(TestConstants.TCELL1, TestConstants.TNODE1, TestConstants.RESULTPROFILE1));
        final List<ProposedChange> testProposedChanges1Reversed = new ArrayList<>(
                buildReverseTestProposedChanges(TestConstants.TCELL1_REVERSION, TestConstants.TNODE1_REVERSION, TestConstants.RESULTPROFILE1,
                        TestConstants.RESULTPROFILE1_REVERSION));

        final List<ProposedChange> testProposedChanges2 = new ArrayList<>(
                buildTestProposedChanges(TestConstants.TCELL3, TestConstants.TNODE3, TestConstants.RESULTPROFILE2));
        final List<ProposedChange> testProposedChanges2Target = new ArrayList<>(
                buildTestProposedChanges(TestConstants.TCELL4, TestConstants.TNODE3, TestConstants.RESULTPROFILE3));
        testProposedChanges2.addAll(testProposedChanges2Target);

        final List<ProposedChange> testProposedChanges2Reversed = new ArrayList<>(
                buildReverseTestProposedChanges(TestConstants.TCELL4_Reversion, TestConstants.TNODE3_REVERSION, TestConstants.RESULTPROFILE3,
                        TestConstants.RESULTPROFILE3_REVERSION));
        final List<ProposedChange> testProposedChanges2TargetReversed = new ArrayList<>(
                buildReverseTestProposedChanges(TestConstants.TCELL3_REVERSION, TestConstants.TNODE3_REVERSION, TestConstants.RESULTPROFILE2,
                        TestConstants.RESULTPROFILE2_REVERSION));
        testProposedChanges2Reversed.addAll(testProposedChanges2TargetReversed);

        final List<ProposedChange> testProposedChanges3 = new ArrayList<>(
                buildTestProposedChanges(TestConstants.TCELL7, TestConstants.TNODE5, TestConstants.RESULTPROFILE4));

        final List<ProposedChange> testProposedChanges3Reversed = new ArrayList<>(
                buildReverseTestProposedChanges(TestConstants.TCELL7_REVERSION, TestConstants.TNODE5_REVERSION, TestConstants.RESULTPROFILE4,
                        TestConstants.RESULTPROFILE4_REVERSION));

        assertThat(resultChangeElements.size()).isEqualTo(6);
        assertThat(resultChangeElements.get(0))
                .returns(EXECUTION_ID, from(ChangeElement::getExecutionId))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getSourceOfChange))
                .returns(expectedStatus, from(ChangeElement::getStatus))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getModifiedBy))
                .returns(CHANGE_ID_1, from(ChangeElement::getChangeId))
                .returns(ChangeElement.ChangeType.OPTIMIZATION, from(ChangeElement::getChangeType))
                .returns(testProposedChanges1, ChangeElement::getProposedChanges);
        assertThat(resultChangeElements.get(1))
                .returns(EXECUTION_ID, from(ChangeElement::getExecutionId))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getSourceOfChange))
                .returns(ChangeElementStatus.PENDING_APPROVAL.toString(), from(ChangeElement::getStatus))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getModifiedBy))
                .returns(CHANGE_ID_1, from(ChangeElement::getChangeId))
                .returns(ChangeElement.ChangeType.REVERSION, from(ChangeElement::getChangeType))
                .returns(testProposedChanges1Reversed, from(ChangeElement::getProposedChanges));
        assertThat(resultChangeElements.get(2))
                .returns(EXECUTION_ID, from(ChangeElement::getExecutionId))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getSourceOfChange))
                .returns(expectedStatus, from(ChangeElement::getStatus))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getModifiedBy))
                .returns(CHANGE_ID_2, from(ChangeElement::getChangeId))
                .returns(ChangeElement.ChangeType.OPTIMIZATION, from(ChangeElement::getChangeType))
                .returns(testProposedChanges2, from(ChangeElement::getProposedChanges));
        assertThat(resultChangeElements.get(3))
                .returns(EXECUTION_ID, from(ChangeElement::getExecutionId))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getSourceOfChange))
                .returns(ChangeElementStatus.PENDING_APPROVAL.toString(), from(ChangeElement::getStatus))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getModifiedBy))
                .returns(CHANGE_ID_2, from(ChangeElement::getChangeId))
                .returns(ChangeElement.ChangeType.REVERSION, from(ChangeElement::getChangeType))
                .returns(testProposedChanges2Reversed, from(ChangeElement::getProposedChanges));
        assertThat(resultChangeElements.get(4))
                .returns(EXECUTION_ID, from(ChangeElement::getExecutionId))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getSourceOfChange))
                .returns(expectedStatus, from(ChangeElement::getStatus))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getModifiedBy))
                .returns(CHANGE_ID_3, from(ChangeElement::getChangeId))
                .returns(ChangeElement.ChangeType.OPTIMIZATION, from(ChangeElement::getChangeType))
                .returns(testProposedChanges3, ChangeElement::getProposedChanges);
        assertThat(resultChangeElements.get(5))
                .returns(EXECUTION_ID, from(ChangeElement::getExecutionId))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getSourceOfChange))
                .returns(ChangeElementStatus.PENDING_APPROVAL.toString(), from(ChangeElement::getStatus))
                .returns(SOURCE_OF_CHANGE, from(ChangeElement::getModifiedBy))
                .returns(CHANGE_ID_3, from(ChangeElement::getChangeId))
                .returns(ChangeElement.ChangeType.REVERSION, from(ChangeElement::getChangeType))
                .returns(testProposedChanges3Reversed, from(ChangeElement::getProposedChanges));
    }

    private List<ProposedChange> buildTestProposedChanges(final TopologyObject cell, final TopologyObject node, final TopologyObject profile) {
        final List<ProposedChange> proposedChanges = new ArrayList<>();
        proposedChanges.add(new ProposedChange(1,
                node.getAttributes().get(ATTRIBUTE_NODE_TYPE).toString(),
                OBJECT_TYPE_IDLE_MODE_PRIO,
                -1L,
                node.getAttributes().get(ATTRIBUTE_FDN) + "," + cell.getAttributes().get("cgi"),
                OPERATION_TYPE_CREATE,
                buildTestParameterChangesForCreateProfile(profile)));
        proposedChanges.add(new ProposedChange(1,
                node.getAttributes().get(ATTRIBUTE_NODE_TYPE).toString(),
                TopologyType.CELL.toString(),
                cell.getId(),
                cell.getAttributes().get(ATTRIBUTE_FDN).toString(),
                OPERATION_TYPE_MODIFY,
                buildTestParameterChangesForModifyCell(node.getAttributes().get(ATTRIBUTE_FDN) + "," +
                        cell.getAttributes().get("cgi"))));
        proposedChanges.add(new ProposedChange(1,
                node.getAttributes().get(ATTRIBUTE_NODE_TYPE).toString(),
                OBJECT_TYPE_IDLE_MODE_PRIO,
                profile.getId(),
                profile.getAttributes().get(ATTRIBUTE_FDN).toString(),
                OPERATION_TYPE_DELETE,
                Collections.emptyList()));
        return proposedChanges;
    }

    private List<ProposedChange> buildReverseTestProposedChanges(final TopologyObject cell, final TopologyObject node, final TopologyObject profile1,
            final TopologyObject profile2) {
        final List<ProposedChange> proposedChanges = new ArrayList<>();
        proposedChanges.add(new ProposedChange(1,
                node.getAttributes().get(ATTRIBUTE_NODE_TYPE).toString(),
                OBJECT_TYPE_IDLE_MODE_PRIO,
                -1L,
                profile1.getAttributes().get(ATTRIBUTE_FDN).toString(),
                OPERATION_TYPE_CREATE,
                buildTestParameterChangesForCreateProfile(profile2)));
        proposedChanges.add(new ProposedChange(1,
                node.getAttributes().get(ATTRIBUTE_NODE_TYPE).toString(),
                TopologyType.CELL.toString(),
                cell.getId(),
                cell.getAttributes().get(ATTRIBUTE_FDN).toString(),
                OPERATION_TYPE_MODIFY,
                buildTestParameterChangesForModifyCell(profile1.getAttributes().get(ATTRIBUTE_FDN).toString())));
        proposedChanges.add(new ProposedChange(1,
                node.getAttributes().get(ATTRIBUTE_NODE_TYPE).toString(),
                OBJECT_TYPE_IDLE_MODE_PRIO,
                profile2.getId(),
                profile2.getAttributes().get(ATTRIBUTE_FDN).toString(),
                OPERATION_TYPE_DELETE,
                Collections.emptyList()));
        return proposedChanges;
    }

    private List<ParameterChanges> buildTestParameterChangesForModifyCell(final String profileFdn) {
        final List<ParameterChanges> parameterChanges = new ArrayList<>();
        parameterChanges.add(new ParameterChanges("idleModePrioAtReleaseRef", profileFdn));
        return parameterChanges;
    }

    private List<ParameterChanges> buildTestParameterChangesForCreateProfile(final TopologyObject profile) {
        final List<ParameterChanges> parameterChanges = new ArrayList<>();

        parameterChanges.add(new ParameterChanges("lowLoadThreshold", profile.getAttributes().get("lowLoadThreshold").toString()));
        parameterChanges.add(new ParameterChanges("lowMediumLoadThreshold", profile.getAttributes().get("lowMediumLoadThreshold").toString()));
        parameterChanges.add(new ParameterChanges("mediumLoadThreshold", profile.getAttributes().get("mediumLoadThreshold").toString()));
        parameterChanges.add(new ParameterChanges("mediumHighLoadThreshold", profile.getAttributes().get("mediumHighLoadThreshold").toString()));
        parameterChanges.add(new ParameterChanges("highLoadThreshold", profile.getAttributes().get("highLoadThreshold").toString()));
        parameterChanges.add(new ParameterChanges("lowLoadDistributionInfo", buildLoadDistributionInfo(profile, "lowLoadDistributionInfo")));
        parameterChanges
                .add(new ParameterChanges("lowMediumLoadDistributionInfo", buildLoadDistributionInfo(profile, "lowMediumLoadDistributionInfo")));
        parameterChanges.add(new ParameterChanges("mediumLoadDistributionInfo", buildLoadDistributionInfo(profile, "mediumLoadDistributionInfo")));
        parameterChanges
                .add(new ParameterChanges("mediumHighLoadDistributionInfo", buildLoadDistributionInfo(profile, "mediumHighLoadDistributionInfo")));
        parameterChanges.add(new ParameterChanges("highLoadDistributionInfo", buildLoadDistributionInfo(profile, "highLoadDistributionInfo")));
        return parameterChanges;
    }

    private String buildLoadDistributionInfo(final TopologyObject profile, final String thresholdName) {
        return profile.getAttributes().get(thresholdName).toString();
    }

    private List<Pair<PolicyOutputEvent, OverlapInfo>> buildPolicyOutputEvents() {
        final ProposedLoadBalancingQuanta quanta1 = new ProposedLoadBalancingQuanta(
                TestConstants.C1, OSS_ID_1, "50",
                Collections.singletonList(new TargetCell(TestConstants.C2, OSS_ID_1, "50")));
        final ProposedLoadBalancingQuanta quanta2 = new ProposedLoadBalancingQuanta(
                TestConstants.C3, OSS_ID_1, "50",
                Collections.singletonList(new TargetCell(TestConstants.C4, OSS_ID_1, "50")));
        final ProposedLoadBalancingQuanta quanta3 = new ProposedLoadBalancingQuanta(
                TestConstants.C7, OSS_ID_1, "8",
                Collections.singletonList(new TargetCell(TestConstants.C8, OSS_ID_1, "8")));

        final PolicyOutputEvent event1 = new PolicyOutputEvent("event", "1.0", "namespace", SOURCE_OF_CHANGE, "flm",
                TestConstants.S1, EXECUTION_ID, quanta1, Collections.emptyList());
        final PolicyOutputEvent event2 = new PolicyOutputEvent("event", "1.0", "namespace", SOURCE_OF_CHANGE, "flm",
                TestConstants.S2, EXECUTION_ID, quanta2, Collections.emptyList());
        final PolicyOutputEvent event3 = new PolicyOutputEvent("event", "1.0", "namespace", SOURCE_OF_CHANGE, "flm",
                TestConstants.S3, EXECUTION_ID, quanta3, Collections.emptyList());

        final OverlapInfo overlapInfo = OverlapInfo.of(NOT_OVERLAPPING, "");

        return Arrays.asList(Pair.of(event1, overlapInfo), Pair.of(event2, overlapInfo), Pair.of(event3, overlapInfo));
    }

    /**
     * LoadBalancingCalculatorFactory used in testing to allow a mocked {@link LbdarDao} to be injected
     */
    public class LoadBalancingCalculatorFactory extends com.ericsson.oss.services.sonom.flm.loadbalancing.LoadBalancingCalculatorFactory {

        private final LbdarDao lbdarDao;

        public LoadBalancingCalculatorFactory(final LbdarDao lbdarDao) {
            this.lbdarDao = lbdarDao;
        }

        /**
         * This method creates a LoadBalancingCalculator based on
         * {@link com.ericsson.oss.services.sonom.flm.loadbalancing.LoadBalancingCalculatorFactory.LoadBalancingType} given.
         * 
         * @param lbType
         *            a type of LoadBalancing Algorithm
         * @param execution
         *            an Execution instance
         * @param customizedGlobalSettings
         *            customized global settings from Configuration of Execution
         * @param cmStore
         *            a store that can be used to query CM data
         * @param cellKpiStore
         *            an instance of cellKpiStore
         * @param policyOutputEvents
         *            the list of policyOutputEvents used to initialize stores
         * @return it returns an optional of LoadBalancingCalculator. It is empty type is not supported
         * @throws FlmAlgorithmException
         *             if there are no {@link Cell}s after {@code inclusionList} is applied
         */
        @Override
        public Optional<LoadBalancingCalculator> create(
                final com.ericsson.oss.services.sonom.flm.loadbalancing.LoadBalancingCalculatorFactory.LoadBalancingType lbType,
                final Execution execution,
                final Map<String, String> customizedGlobalSettings,
                final CmStore cmStore,
                final CellKpiStore cellKpiStore,
                final List<PolicyOutputEvent> policyOutputEvents) throws FlmAlgorithmException {
            if (lbType == com.ericsson.oss.services.sonom.flm.loadbalancing.LoadBalancingCalculatorFactory.LoadBalancingType.LBDAR) {
                return Optional.of(new LbdarCalculator(execution,
                        cmStore,
                        cellKpiStore,
                        customizedGlobalSettings,
                        policyOutputEvents,
                        lbdarDao));
            }
            return Optional.empty();
        }
    }
}

/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.pa.policy;

import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.CELL_HANDOVER_SUCCESS_RATE;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.E_RAB_RETAINABILITY_PERCENTAGE_LOST;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.UPLINK_PUSCH_SINR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmExternalDao;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorExternalDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.messagehandler.ExecutionConsumerHandler;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionLatch;
import com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PAKpiReader;
import com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PAKpiRetriever;
import com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor.PACellIdRetrieverExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

/**
 * Unit tests for {@link PAPolicyExecutor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PAPolicyExecutorTest {
    private static final String TIMESTAMP_AT_TEN_AM = "2020-12-25 10:00:00";
    private static final String TIMESTAMP_AT_ELEVEN_AM = "2020-12-25 11:00:00";
    private static final String FLM_EXECUTION_ID = "FLM_1479249799770-162";
    private static final LocalDateTime WINDOW_START_TIME = LocalDateTime.of(2020, 12, 25, 10, 0);
    private static final String FDN_ONE = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1," +
            "EUtranCellFDD=654875_9_2";
    private static final String FDN_TWO = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1," +
            "EUtranCellFDD=654875_9_3";
    private static final String FDN_THREE = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1," +
            "EUtranCellFDD=654875_9_4";
    private static final int OSS_ID = 1;
    private static final long SECTOR_ID_ONE = 173290088340418268L;
    private static final long SECTOR_ID_TWO = 173290088340418269L;
    private static final String PA_KPI_SETTINGS_NAME = "paKpiSettings";
    private static final String NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_SETTING_NAME = "numberOfKpiDegradedHoursThreshold";
    private static final String NUMBER_OF_KPI_DEGRADED_HOURS = "4";
    private static final String PA_SETTINGS = "{\"cellHandoverSuccessRate\": " +
            "{ \"enableKPI\": true, \"confidenceInterval\": \"99\", \"relevanceThreshold\": \"99.90\" }, " +
            "\"initialAndAddedERabEstabSrHourly\": " +
            "{ \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, " +
            "\"initialAndAddedERabEstabSrQci1Hourly\": " +
            "{ \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, " +
            "\"eRabRetainabilityPercentageLostHourly\": " +
            "{ \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, " +
            "\"eRabRetainabilityPercentageLostQci1Hourly\": " +
            "{ \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, " +
            "\"avgDlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }, " +
            "\"avgUlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }, " +
            "\"ulPuschSinrHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"15\" }}";
    private static final List<String> HOURLY_CELL_KPIS = new ArrayList<>();
    private static final List<String> DEGRADATION_CELL_KPIS = new ArrayList<>();
    private static final List<CellIdentifier> CELL_IDENTIFIERS = new ArrayList<>();

    @Mock
    private PACellIdRetrieverExecutor cellIdRetrieverExecutorMock;
    @Mock
    private FlmPaPolicyInputEventHandler paPolicyInputEventHandlerMock;
    @Mock
    private KpiCellFlmExternalDao cellFlmDao;
    @Mock
    private KpiSectorExternalDao sectorDao;
    @Mock
    private PAOutputEventDao paOutputEventDao;
    @Mock
    private ExecutionConsumerHandler paExecutionConsumerHandlerMock;
    @Mock
    private PAExecutionLatch latch;

    private PAPolicyExecutor objUnderTest;

    private PAKpiRetriever paKpiRetrieverSpy;

    private PAKpiReader paKpiReader;

    static {
        CELL_IDENTIFIERS.add(new CellIdentifier(OSS_ID, FDN_ONE));
        CELL_IDENTIFIERS.add(new CellIdentifier(OSS_ID, FDN_TWO));

        HOURLY_CELL_KPIS.add(CELL_HANDOVER_SUCCESS_RATE.getKpiName());
        HOURLY_CELL_KPIS.add(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName());
        HOURLY_CELL_KPIS.add(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName());
        HOURLY_CELL_KPIS.add(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName());
        HOURLY_CELL_KPIS.add(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName());
        HOURLY_CELL_KPIS.add(UPLINK_PUSCH_SINR.getKpiName());

        DEGRADATION_CELL_KPIS.add(CELL_HANDOVER_SUCCESS_RATE.getThresholdName());
        DEGRADATION_CELL_KPIS.add(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName());
        DEGRADATION_CELL_KPIS.add(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getThresholdName());
        DEGRADATION_CELL_KPIS.add(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getThresholdName());
        DEGRADATION_CELL_KPIS.add(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getThresholdName());
        DEGRADATION_CELL_KPIS.add(UPLINK_PUSCH_SINR.getThresholdName());
    }

    @Before
    public void setUp() throws Exception {
        final PAExecution paExecution = paExecution();
        final String paStartTime = PAPolicyExecutor.timestampToUtcString(paExecution.getPaWindowStartTime());
        final String paEndTime = PAPolicyExecutor.timestampToUtcString(paExecution.getPaWindowEndTime());
        final Map<Long, List<TopologyObjectId>> sectorsAndCells = sectorsAndCells(Arrays.asList(SECTOR_ID_ONE, SECTOR_ID_TWO));

        paKpiRetrieverSpy = spy(new PAKpiRetriever(cellFlmDao, sectorDao));

        paKpiReader = spy(new PAKpiReader(paExecution, flmExecution(), paKpiRetrieverSpy, new PAExecutionLatch()));

        when(cellIdRetrieverExecutorMock.execute()).thenReturn(sectorsAndCells);

        when(cellFlmDao.getHourlyKpisForGivenCells(paStartTime, paEndTime, HOURLY_CELL_KPIS, CELL_IDENTIFIERS))
                .thenReturn(hourlyCellKpisForSectorOne());
        when(cellFlmDao.getKpisForGivenCellsPerFlmExecution(FLM_EXECUTION_ID, paStartTime, paEndTime, DEGRADATION_CELL_KPIS,
                CELL_IDENTIFIERS)).thenReturn(degradationCellKpisForSectorOne());

        objUnderTest = spy(new PAPolicyExecutor()
                .withPaExecution(paExecution)
                .withPAKpiReader(paKpiReader)
                .withFlmPaPolicyInputEventHandler(paPolicyInputEventHandlerMock)
                .withPAStageExecutor(cellIdRetrieverExecutorMock)
                .withExecutionConsumerHandler(paExecutionConsumerHandlerMock)
                .withPAOutputEventDao(paOutputEventDao)
                .withPAExecutionLatch(latch));
    }

    @Test
    public void whenPaPolicyExecutorIsCalled_thenMessagesSentToTopic() throws PAExecutionException, SQLException {
        objUnderTest.execute();

        verify(paKpiReader, times(1)).generateSectorList(anyMap());
        verify(cellIdRetrieverExecutorMock, times(1)).execute();
        verify(paKpiRetrieverSpy, times(1)).retrieveSectorHourlyKpis(anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, times(1)).retrieveSectorDegradationThresholdKpis(anyString(), anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, times(2)).retrieveCellHourlyKpis(anyString(), anyString(), anyList());
        verify(paKpiRetrieverSpy, times(2)).retrieveCellDegradationThresholdKpis(anyString(), anyString(), anyString(), anyList());
        verify(paPolicyInputEventHandlerMock, times(1)).sendToKafkaTopic(anyList(), any());
    }

    @Test
    public void whenRetrievingCellIds_andPaExecutionExceptionOccurs_thenThrowPaExecutionException() throws PAExecutionException {
        when(cellIdRetrieverExecutorMock.execute()).thenThrow(PAExecutionException.class);

        verifyExecutionFails(PAExecutionException.class);
        verify(paKpiReader, never()).generateSectorList(anyMap());
    }

    @Test
    public void whenPaPolicyExecutorIsCalled_andPaKpiReaderThrowsInterruptedException_thenThrowPaExecutionInterruptedException()
            throws PAExecutionException, InterruptedException {
        doThrow(PAExecutionInterruptedException.class).when(paKpiReader).generateSectorList(anyMap());

        verifyExecutionFails(PAExecutionInterruptedException.class);

        verify(paKpiReader, times(1)).generateSectorList(anyMap());
        verify(paExecutionConsumerHandlerMock, never()).consumeMessagesForExecution(anyString(), anyInt(), anySet());
        verify(paPolicyInputEventHandlerMock, never()).sendToKafkaTopic(anyList(), any());
        verify(paExecutionConsumerHandlerMock, never()).waitMessages(anyString(), anyInt());
    }

    @Test
    public void whenPaPolicyExecutorIsCalled_andInterruptSignalIsCalledAfterGenerateSectorList_thenThrowPaExecutionInterruptedException()
            throws PAExecutionException, InterruptedException {
        doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        verifyExecutionFails(PAExecutionInterruptedException.class);

        verify(paKpiReader, times(1)).generateSectorList(anyMap());
        verify(paExecutionConsumerHandlerMock, never()).consumeMessagesForExecution(anyString(), anyInt(), anySet());
        verify(paPolicyInputEventHandlerMock, never()).sendToKafkaTopic(anyList(), any());
        verify(paExecutionConsumerHandlerMock, never()).waitMessages(anyString(), anyInt());
    }

    @Test
    public void whenPaPolicyExecutorIsCalled_andInterruptSignalIsCalledAfterGenerateInputEvents_thenThrowPaExecutionInterruptedException()
            throws PAExecutionException, InterruptedException {
        doNothing().doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        verifyExecutionFails(PAExecutionInterruptedException.class);

        verify(paKpiReader, times(1)).generateSectorList(anyMap());
        verify(paExecutionConsumerHandlerMock, never()).consumeMessagesForExecution(anyString(), anyInt(), anySet());
        verify(paPolicyInputEventHandlerMock, never()).sendToKafkaTopic(anyList(), any());
        verify(paExecutionConsumerHandlerMock, never()).waitMessages(anyString(), anyInt());
    }

    @Test
    public void whenPaPolicyExecutorIsCalled_andInterruptSignalIsCalledAfterConsumeMessages_thenThrowPaExecutionInterruptedException()
            throws PAExecutionException, InterruptedException {
        doNothing().doNothing().doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        verifyExecutionFails(PAExecutionInterruptedException.class);

        verify(paKpiReader, times(1)).generateSectorList(anyMap());
        verify(paExecutionConsumerHandlerMock, times(1)).consumeMessagesForExecution(anyString(), anyInt(), anySet());
        verify(paPolicyInputEventHandlerMock, never()).sendToKafkaTopic(anyList(), any());
        verify(paExecutionConsumerHandlerMock, never()).waitMessages(anyString(), anyInt());
    }

    @Test
    public void whenPaPolicyExecutorIsCalled_andInterruptSignalIsCalledAfterSendToPolicyKafka_thenThrowPaExecutionInterruptedException()
            throws PAExecutionException, InterruptedException {
        doNothing().doNothing().doNothing().doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        verifyExecutionFails(PAExecutionInterruptedException.class);

        verify(paKpiReader, times(1)).generateSectorList(anyMap());
        verify(paExecutionConsumerHandlerMock, times(1)).consumeMessagesForExecution(anyString(), anyInt(), anySet());
        verify(paPolicyInputEventHandlerMock, times(1)).sendToKafkaTopic(anyList(), any());
        verify(paExecutionConsumerHandlerMock, never()).waitMessages(anyString(), anyInt());
    }

    private <T extends PAExecutionException> void verifyExecutionFails(final Class<T> exceptionType) {
        try {
            objUnderTest.execute();
            shouldHaveThrown(PAExecutionException.class);
        } catch (final PAExecutionException e) {
            assertThat(e.getClass()).isEqualTo(exceptionType);
        }
    }

    private static Map<CellIdentifier, Map<String, Map<String, Object>>> hourlyCellKpisForSectorOne() {
        return new MapBuilder<CellIdentifier, Map<String, Map<String, Object>>>()
                .with(new CellIdentifier(OSS_ID, FDN_ONE), new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 98.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), 90.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName(), 92.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(), 97.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(), 91.8D)
                                .with(UPLINK_PUSCH_SINR.getKpiName(), 91.8D)
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 99.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), 91.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName(), 93.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(), 98.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(), 92.8D)
                                .with(UPLINK_PUSCH_SINR.getKpiName(), 92.8D)
                                .build())
                        .build())
                .with(new CellIdentifier(OSS_ID, FDN_TWO), new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 98.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), 90.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName(), 92.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(), 97.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(), 91.8D)
                                .with(UPLINK_PUSCH_SINR.getKpiName(), 91.8D)
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 98.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), 90.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName(), 92.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(), 97.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(), 91.8D)
                                .with(UPLINK_PUSCH_SINR.getKpiName(), 91.8D)
                                .build())
                        .build())
                .build();
    }

    private static Map<CellIdentifier, Map<String, Map<String, Object>>> degradationCellKpisForSectorOne() {
        return new MapBuilder<CellIdentifier, Map<String, Map<String, Object>>>()
                .with(new CellIdentifier(OSS_ID, FDN_ONE), new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 99.2D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), 91.2D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getThresholdName(), null)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getThresholdName(), 98.2D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getThresholdName(), 92.2D)
                                .with(UPLINK_PUSCH_SINR.getThresholdName(), 92.2D)
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 98.2D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), null)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getThresholdName(), 92.2D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getThresholdName(), 97.2D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getThresholdName(), 91.2D)
                                .with(UPLINK_PUSCH_SINR.getThresholdName(), 91.2D)
                                .build())
                        .build())
                .with(new CellIdentifier(OSS_ID, FDN_TWO), new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), null)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), 90.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getThresholdName(), 92.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getThresholdName(), 97.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getThresholdName(), 91.8D)
                                .with(UPLINK_PUSCH_SINR.getThresholdName(), 91.8D)
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 98.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), 90.8D)
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getThresholdName(), 92.8D)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getThresholdName(), null)
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getThresholdName(), 91.8D)
                                .with(UPLINK_PUSCH_SINR.getThresholdName(), 91.8D)
                                .build())
                        .build())
                .build();
    }

    private static Map<Long, List<TopologyObjectId>> sectorsAndCells(final List<Long> sectorIds) {
        final Map<Long, List<TopologyObjectId>> sectorsAndCells = new HashMap<>();
        if (sectorIds.contains(SECTOR_ID_ONE)) {
            sectorsAndCells.put(SECTOR_ID_ONE, Arrays.asList(new TopologyObjectId(FDN_ONE, OSS_ID), new TopologyObjectId(FDN_TWO, OSS_ID)));
        }
        if (sectorIds.contains(SECTOR_ID_TWO)) {
            sectorsAndCells.put(SECTOR_ID_TWO, Collections.singletonList(new TopologyObjectId(FDN_THREE, OSS_ID)));
        }
        return sectorsAndCells;
    }

    private static PAExecution paExecution() {
        return new PAExecution(1, "0 0 2 ? * * *",
                Timestamp.valueOf(WINDOW_START_TIME), Timestamp.valueOf(WINDOW_START_TIME.plusHours(2)),
                FLM_EXECUTION_ID);
    }

    private static Execution flmExecution() {
        final Execution flmExecution = new Execution();
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setCustomizedGlobalSettings(new MapBuilder<String, String>()
                .with(PA_KPI_SETTINGS_NAME, PA_SETTINGS)
                .with(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_SETTING_NAME, NUMBER_OF_KPI_DEGRADED_HOURS).build());
        return flmExecution;
    }

    private static class MapBuilder<K, V> {
        final Map<K, V> map = new HashMap<>();

        private MapBuilder<K, V> with(final K key, final V value) {
            map.put(key, value);
            return this;
        }

        private Map<K, V> build() {
            return map;
        }
    }
}

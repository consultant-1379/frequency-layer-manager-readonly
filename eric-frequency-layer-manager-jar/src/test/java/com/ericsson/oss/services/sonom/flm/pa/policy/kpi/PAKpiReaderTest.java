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

package com.ericsson.oss.services.sonom.flm.pa.policy.kpi;

import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.CELL_HANDOVER_SUCCESS_RATE;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.E_RAB_RETAINABILITY_PERCENTAGE_LOST;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PACellGuid60Kpis.UPLINK_PUSCH_SINR;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PAKpiReader.sortKpiByKpiName;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PAKpiReader.timestampToUtcString;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PASector60Kpis.AVG_DL_PDCP_THROUGHPUT_SECTOR;
import static com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PASector60Kpis.AVG_UL_PDCP_THROUGHPUT_SECTOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmExternalDao;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorExternalDao;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionLatch;
import com.ericsson.oss.services.sonom.flm.pa.policy.FlmPaPolicyInputEventHandler;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.KpiValue;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.Cell;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.CellLevelKpi;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.RelevanceThresholdType;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.Sector;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.SectorLevelKpi;

/**
 * Unit tests for {@link PAKpiReader} class
 */

@RunWith(MockitoJUnitRunner.class)
public class PAKpiReaderTest {

    private static final String DL_UPPER_LIMIT = "1200000.0";
    private static final String LOWER_LIMIT = "0.0";
    private static final String UL_UPPER_LIMIT = "600000.0";
    private static final String TIMESTAMP_AT_TEN_AM = "2020-12-25 10:00:00";
    private static final String TIMESTAMP_AT_ELEVEN_AM = "2020-12-25 11:00:00";
    private static final String TIMESTAMP_AT_TWELVE_PM = "2020-12-25 12:00:00";
    private static final String TIMESTAMP_AT_ONE_PM = "2020-12-25 13:00:00";
    private static final String THRESHOLD = "threshold";
    private static final String VALUE = "value";
    private static final String FLM_EXECUTION_ID = "FLM_1479249799770-162";
    private static final LocalDateTime WINDOW_START_TIME = LocalDateTime.of(2020, 12, 25, 10, 0);
    private static final int OSS_ID = 1;
    private static final int ONCE = 1;
    private static final String FDN_ONE = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1," +
            "EUtranCellFDD=654875_9_2";
    private static final String FDN_TWO = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1," +
            "EUtranCellFDD=654875_9_3";
    private static final String FDN_THREE = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1," +
            "EUtranCellFDD=654875_9_4";
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
    private static final List<String> HOURLY_SECTOR_KPIS = new ArrayList<>();
    private static final List<String> DEGRADATION_SECTOR_KPIS = new ArrayList<>();
    private static final List<CellIdentifier> CELL_IDENTIFIERS = new ArrayList<>();
    private static Map<Long, List<TopologyObjectId>> sectorsAndCells = new HashMap<>();

    @Mock
    private FlmPaPolicyInputEventHandler paPolicyInputEventHandlerMock;
    @Mock
    private KpiCellFlmExternalDao cellFlmDao;
    @Mock
    private KpiSectorExternalDao sectorDao;
    @Mock
    private PAExecutionLatch latch;

    private PAKpiRetriever paKpiRetrieverSpy;

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

        HOURLY_SECTOR_KPIS.add(AVG_DL_PDCP_THROUGHPUT_SECTOR.getKpiName());
        HOURLY_SECTOR_KPIS.add(AVG_UL_PDCP_THROUGHPUT_SECTOR.getKpiName());

        DEGRADATION_SECTOR_KPIS.add(AVG_DL_PDCP_THROUGHPUT_SECTOR.getThresholdName());
        DEGRADATION_SECTOR_KPIS.add(AVG_UL_PDCP_THROUGHPUT_SECTOR.getThresholdName());
    }

    @Before
    public void setUp() throws Exception {
        final PAExecution paExecution = paExecution();
        final String paStartTime = timestampToUtcString(paExecution.getPaWindowStartTime());
        final String paEndTime = timestampToUtcString(paExecution.getPaWindowEndTime());
        sectorsAndCells = sectorsAndCells(Arrays.asList(SECTOR_ID_ONE, SECTOR_ID_TWO));
        paKpiRetrieverSpy = spy(new PAKpiRetriever(cellFlmDao, sectorDao));

        when(cellFlmDao.getHourlyKpisForGivenCells(paStartTime, paEndTime, HOURLY_CELL_KPIS, CELL_IDENTIFIERS))
                .thenReturn(hourlyCellKpisForSectorOne());
        when(cellFlmDao.getKpisForGivenCellsPerFlmExecution(FLM_EXECUTION_ID, paStartTime, paEndTime, DEGRADATION_CELL_KPIS,
                CELL_IDENTIFIERS)).thenReturn(degradationCellKpisForSectorOne());

        when(sectorDao.getSectorHourlyKpis(paStartTime, paEndTime, HOURLY_SECTOR_KPIS, Collections.singleton(SECTOR_ID_ONE)))
                .thenReturn(hourlyKpisForSectorOne());
        when(sectorDao.getSectorHourlyKpisForFlmExecution(FLM_EXECUTION_ID, paStartTime, paEndTime, DEGRADATION_SECTOR_KPIS,
                Collections.singleton(SECTOR_ID_ONE)))
                .thenReturn(degradationKpisForSectorOne());
    }

    @Test
    public void whenOneKpiAtMultipleTimestamps_thenMapOfKpisToTimestampsIsReturned() {
        final Map<String, Map<String, Object>> hourlyKpis = new HashMap<>();
        hourlyKpis.put(TIMESTAMP_AT_TEN_AM, Collections.singletonMap(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 98.8));
        hourlyKpis.put(TIMESTAMP_AT_ELEVEN_AM, Collections.singletonMap(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 85.3));
        hourlyKpis.put(TIMESTAMP_AT_TWELVE_PM, Collections.singletonMap(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 92.2));
        hourlyKpis.put(TIMESTAMP_AT_ONE_PM, Collections.singletonMap(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 93.7));

        final Map<String, Map<String, Object>> degradationKpis = new HashMap<>();
        degradationKpis.put(TIMESTAMP_AT_TEN_AM, Collections.singletonMap(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 99.9));
        degradationKpis.put(TIMESTAMP_AT_ELEVEN_AM, Collections.singletonMap(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 94.7));
        degradationKpis.put(TIMESTAMP_AT_TWELVE_PM, Collections.singletonMap(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 89.2));
        degradationKpis.put(TIMESTAMP_AT_ONE_PM, Collections.singletonMap(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 65.4));

        final Map<String, Map<String, Map<String, Object>>> sortedKpis = sortKpiByKpiName(hourlyKpis, degradationKpis,
                PACellGuid60Kpis::getThresholdNameForKpi);

        assertThat(sortedKpis)
                .containsOnlyKeys(CELL_HANDOVER_SUCCESS_RATE.getKpiName());

        final Map<String, Map<String, Object>> cellHandoverMap = sortedKpis.get(CELL_HANDOVER_SUCCESS_RATE.getKpiName());
        assertThat(cellHandoverMap)
                .containsOnlyKeys(TIMESTAMP_AT_TEN_AM, TIMESTAMP_AT_ELEVEN_AM, TIMESTAMP_AT_TWELVE_PM, TIMESTAMP_AT_ONE_PM);

        assertThat(cellHandoverMap.get(TIMESTAMP_AT_TEN_AM))
                .containsOnly(
                        entry(VALUE, 98.8),
                        entry(THRESHOLD, 99.9));

        assertThat(cellHandoverMap.get(TIMESTAMP_AT_ELEVEN_AM))
                .containsOnly(
                        entry(VALUE, 85.3),
                        entry(THRESHOLD, 94.7));

        assertThat(cellHandoverMap.get(TIMESTAMP_AT_TWELVE_PM))
                .containsOnly(
                        entry(VALUE, 92.2),
                        entry(THRESHOLD, 89.2));

        assertThat(cellHandoverMap.get(TIMESTAMP_AT_ONE_PM))
                .containsOnly(
                        entry(VALUE, 93.7),
                        entry(THRESHOLD, 65.4));
    }

    @Test
    public void whenTwoKpisAtOneTimestamp_thenMapOfKpisToTimestampsIsReturned() {
        final Map<String, Map<String, Object>> hourlyKpis = new HashMap<>();
        final Map<String, Object> tenAmKpis = new HashMap<>();
        tenAmKpis.put(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 98.8);
        tenAmKpis.put(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), 96.5);
        hourlyKpis.put(TIMESTAMP_AT_TEN_AM, tenAmKpis);

        final Map<String, Map<String, Object>> degradationKpis = new HashMap<>();
        final Map<String, Object> tenAmDegradationKpis = new HashMap<>();
        tenAmDegradationKpis.put(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 93.8);
        tenAmDegradationKpis.put(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), 97.5);
        degradationKpis.put(TIMESTAMP_AT_TEN_AM, tenAmDegradationKpis);

        final Map<String, Map<String, Map<String, Object>>> sortedKpis = sortKpiByKpiName(hourlyKpis, degradationKpis,
                PACellGuid60Kpis::getThresholdNameForKpi);

        assertThat(sortedKpis)
                .containsOnlyKeys(CELL_HANDOVER_SUCCESS_RATE.getKpiName(),
                        INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName());

        final Map<String, Map<String, Object>> cellHandoverMap = sortedKpis.get(CELL_HANDOVER_SUCCESS_RATE.getKpiName());
        assertThat(cellHandoverMap)
                .containsOnlyKeys(TIMESTAMP_AT_TEN_AM);

        assertThat(cellHandoverMap.get(TIMESTAMP_AT_TEN_AM))
                .containsOnly(
                        entry(VALUE, 98.8),
                        entry(THRESHOLD, 93.8));

        final Map<String, Map<String, Object>> erabEstabSrMap = sortedKpis.get(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName());
        assertThat(erabEstabSrMap)
                .containsOnlyKeys(TIMESTAMP_AT_TEN_AM);

        assertThat(erabEstabSrMap.get(TIMESTAMP_AT_TEN_AM))
                .containsOnly(
                        entry(VALUE, 96.5),
                        entry(THRESHOLD, 97.5));
    }

    @Test
    public void whenTwoKpisAtMultipleTimestamps_thenMapOfKpisToTimestampsIsReturned() {
        final Map<String, Map<String, Object>> hourlyKpis = new HashMap<>();
        final Map<String, Object> tenAmKpis = new HashMap<>();
        tenAmKpis.put(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 98.8);
        tenAmKpis.put(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), 96.5);
        hourlyKpis.put(TIMESTAMP_AT_TEN_AM, tenAmKpis);
        final Map<String, Object> elevenAmKpis = new HashMap<>();
        elevenAmKpis.put(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), 76.9);
        elevenAmKpis.put(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), 86.3);
        hourlyKpis.put(TIMESTAMP_AT_ELEVEN_AM, elevenAmKpis);

        final Map<String, Map<String, Object>> degradationKpis = new HashMap<>();
        final Map<String, Object> tenAmDegradationKpis = new HashMap<>();
        tenAmDegradationKpis.put(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 93.8);
        tenAmDegradationKpis.put(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), 97.5);
        degradationKpis.put(TIMESTAMP_AT_TEN_AM, tenAmDegradationKpis);
        final Map<String, Object> elevenAmDegradationKpis = new HashMap<>();
        elevenAmDegradationKpis.put(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), 64.5);
        elevenAmDegradationKpis.put(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), 99.9);
        degradationKpis.put(TIMESTAMP_AT_ELEVEN_AM, elevenAmDegradationKpis);

        final Map<String, Map<String, Map<String, Object>>> sortedKpis = sortKpiByKpiName(hourlyKpis, degradationKpis,
                PACellGuid60Kpis::getThresholdNameForKpi);

        assertThat(sortedKpis)
                .containsOnlyKeys(CELL_HANDOVER_SUCCESS_RATE.getKpiName(),
                        INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName());

        final Map<String, Map<String, Object>> cellHandoverMap = sortedKpis.get(CELL_HANDOVER_SUCCESS_RATE.getKpiName());
        assertThat(cellHandoverMap)
                .containsOnlyKeys(TIMESTAMP_AT_TEN_AM, TIMESTAMP_AT_ELEVEN_AM);

        assertThat(cellHandoverMap.get(TIMESTAMP_AT_TEN_AM))
                .containsOnly(
                        entry(VALUE, 98.8),
                        entry(THRESHOLD, 93.8));

        assertThat(cellHandoverMap.get(TIMESTAMP_AT_ELEVEN_AM))
                .containsOnly(
                        entry(VALUE, 76.9),
                        entry(THRESHOLD, 64.5));

        final Map<String, Map<String, Object>> erabEstabSrMap = sortedKpis.get(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName());
        assertThat(erabEstabSrMap)
                .containsOnlyKeys(TIMESTAMP_AT_TEN_AM, TIMESTAMP_AT_ELEVEN_AM);

        assertThat(erabEstabSrMap.get(TIMESTAMP_AT_TEN_AM))
                .containsOnly(
                        entry(VALUE, 96.5),
                        entry(THRESHOLD, 97.5));

        assertThat(erabEstabSrMap.get(TIMESTAMP_AT_ELEVEN_AM))
                .containsOnly(
                        entry(VALUE, 86.3),
                        entry(THRESHOLD, 99.9));
    }

    @Test
    public void whenGenerateSectorList_forOneSectorIdAndTwoCells_thenListWithSectorIsReturned() throws PAExecutionException {
        final Map<Long, List<TopologyObjectId>> sectorsAndCells = sectorsAndCells(Collections.singletonList(SECTOR_ID_ONE));
        final Execution flmExecution = flmExecution();
        final PAExecution paExecution = paExecution();

        final PAKpiReader objectUnderTest = new PAKpiReader(paExecution, flmExecution, paKpiRetrieverSpy, latch);

        final List<Sector> sectors = objectUnderTest.generateSectorList(sectorsAndCells);

        assertThat(sectors).hasSize(1);

        final Sector actualSector = sectors.get(0);
        assertThat(actualSector)
                .isEqualToIgnoringGivenFields(new Sector(String.valueOf(SECTOR_ID_ONE),
                        Collections.singletonMap(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_SETTING_NAME, NUMBER_OF_KPI_DEGRADED_HOURS),
                        Collections.emptyMap(), Collections.emptyList()), "kpis", "cells");

        assertThat(actualSector.getKpis()).containsOnly(
                entry(AVG_DL_PDCP_THROUGHPUT_SECTOR.getKpiName(), new SectorLevelKpi(
                        Arrays.asList(new KpiValue("98.8", TIMESTAMP_AT_TEN_AM, "98.2"),
                                new KpiValue("99.8", TIMESTAMP_AT_ELEVEN_AM, "99.2")),
                        true, LOWER_LIMIT, DL_UPPER_LIMIT)),
                entry(AVG_UL_PDCP_THROUGHPUT_SECTOR.getKpiName(), new SectorLevelKpi(
                        Arrays.asList(
                                new KpiValue("90.8", TIMESTAMP_AT_TEN_AM, "90.2"),
                                new KpiValue("91.8", TIMESTAMP_AT_ELEVEN_AM, "91.2")),
                        true, LOWER_LIMIT, UL_UPPER_LIMIT)));

        assertThat(actualSector.getCells()).hasSize(2);
        final Cell actualCellOne = actualSector.getCells().get(0);
        assertThat(actualCellOne).isEqualToIgnoringGivenFields(new Cell(FDN_ONE, OSS_ID, Collections.emptyMap()), "kpis");
        assertThat(actualCellOne.getKpis())
                .containsOnly(
                        entry(CELL_HANDOVER_SUCCESS_RATE.getKpiName(),
                                new CellLevelKpi(
                                        Arrays.asList(new KpiValue("98.8", TIMESTAMP_AT_TEN_AM, "99.2"),
                                                new KpiValue("99.8", TIMESTAMP_AT_ELEVEN_AM, "98.2")),
                                        true, "99.90", RelevanceThresholdType.MIN)),
                        entry(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(),
                                new CellLevelKpi(
                                        Arrays.asList(new KpiValue("90.8", TIMESTAMP_AT_TEN_AM, "91.2"),
                                                new KpiValue("91.8", TIMESTAMP_AT_ELEVEN_AM, "90.2")),
                                        true, "99.90", RelevanceThresholdType.MIN)),
                        entry(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName(),
                                new CellLevelKpi(
                                        Arrays.asList(new KpiValue("92.8", TIMESTAMP_AT_TEN_AM, "93.2"),
                                                new KpiValue("93.8", TIMESTAMP_AT_ELEVEN_AM, "92.2")),
                                        true, "99.90", RelevanceThresholdType.MIN)),
                        entry(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(),
                                new CellLevelKpi(
                                        Arrays.asList(new KpiValue("97.8", TIMESTAMP_AT_TEN_AM, "98.2"),
                                                new KpiValue("98.8", TIMESTAMP_AT_ELEVEN_AM, "97.2")),
                                        true, "0.01", RelevanceThresholdType.MAX)),
                        entry(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(),
                                new CellLevelKpi(
                                        Arrays.asList(new KpiValue("91.8", TIMESTAMP_AT_TEN_AM, "92.2"),
                                                new KpiValue("92.8", TIMESTAMP_AT_ELEVEN_AM, "91.2")),
                                        true, "0.01", RelevanceThresholdType.MAX)),
                        entry(UPLINK_PUSCH_SINR.getKpiName(),
                                new CellLevelKpi(
                                        Arrays.asList(new KpiValue("98.8", TIMESTAMP_AT_TEN_AM, "99.2"),
                                                new KpiValue("99.8", TIMESTAMP_AT_ELEVEN_AM, "98.2")),
                                        true, "15", RelevanceThresholdType.MIN)));

        final Cell actualCellTwo = actualSector.getCells().get(1);
        assertThat(actualCellTwo).isEqualToIgnoringGivenFields(new Cell(FDN_TWO, OSS_ID, Collections.emptyMap()), "kpis");
        assertThat(actualCellTwo.getKpis())
                .containsOnlyKeys(Arrays.stream(PACellGuid60Kpis.values()).map(PACellGuid60Kpis::getKpiName).collect(Collectors.toList()));
    }

    @Test
    public void whenRetrievingSectorKpis_andSqlExceptionOccurs_thenThrowPaExecutionException() throws PAExecutionException, SQLException {
        final Execution flmExecution = flmExecution();
        final PAExecution paExecution = paExecution();
        final Map<Long, List<TopologyObjectId>> sectorsAndCells = sectorsAndCells(Arrays.asList(SECTOR_ID_ONE, SECTOR_ID_TWO));
        when(sectorDao.getSectorHourlyKpis(anyString(), anyString(), anyList(), anySet())).thenThrow(SQLException.class);

        final PAKpiReader objectUnderTest = new PAKpiReader(paExecution, flmExecution, paKpiRetrieverSpy, new PAExecutionLatch());

        verifyExecutionFails(objectUnderTest, sectorsAndCells, PAExecutionException.class);

        verify(paKpiRetrieverSpy, times(ONCE)).retrieveSectorHourlyKpis(anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, never()).retrieveSectorDegradationThresholdKpis(anyString(), anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, never()).retrieveCellHourlyKpis(anyString(), anyString(), anyList());
        verify(paKpiRetrieverSpy, never()).retrieveCellDegradationThresholdKpis(anyString(), anyString(), anyString(), anyList());
        verify(paPolicyInputEventHandlerMock, never()).sendToKafkaTopic(anyList(), any());
    }

    @Test
    public void whenRetrievingCellKpis_andSqlExceptionOccurs_thenThrowPaExecutionException() throws PAExecutionException, SQLException {
        final Execution flmExecution = flmExecution();
        final PAExecution paExecution = paExecution();

        when(cellFlmDao.getHourlyKpisForGivenCells(anyString(), anyString(), anyList(), anyList())).thenThrow(SQLException.class);

        final PAKpiReader objectUnderTest = new PAKpiReader(paExecution, flmExecution, paKpiRetrieverSpy, new PAExecutionLatch());

        verifyExecutionFails(objectUnderTest, sectorsAndCells, PAExecutionException.class);

        verify(paKpiRetrieverSpy, times(ONCE)).retrieveSectorHourlyKpis(anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, times(ONCE)).retrieveSectorDegradationThresholdKpis(anyString(), anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, times(ONCE)).retrieveCellHourlyKpis(anyString(), anyString(), anyList());
        verify(paKpiRetrieverSpy, never()).retrieveCellDegradationThresholdKpis(anyString(), anyString(), anyString(), anyList());
        verify(paPolicyInputEventHandlerMock, never()).sendToKafkaTopic(anyList(), any());
    }

    @Test
    public void whenRetrievingPaKpiSettings_andJsonParseExceptionOccurs_thenThrowPaExecutionException() throws PAExecutionException, SQLException {
        final Execution flmExecution = flmExecution();
        final PAExecution paExecution = paExecution();
        final Map<Long, List<TopologyObjectId>> sectorsAndCells = sectorsAndCells(Arrays.asList(SECTOR_ID_ONE, SECTOR_ID_TWO));
        flmExecution.getCustomizedGlobalSettings().put(PA_KPI_SETTINGS_NAME, "\"");

        final PAKpiReader objectUnderTest = new PAKpiReader(paExecution, flmExecution, paKpiRetrieverSpy, new PAExecutionLatch());

        verifyExecutionFails(objectUnderTest, sectorsAndCells, PAExecutionException.class);

        verify(paKpiRetrieverSpy, never()).retrieveSectorHourlyKpis(anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, never()).retrieveSectorDegradationThresholdKpis(anyString(), anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, never()).retrieveCellHourlyKpis(anyString(), anyString(), anyList());
        verify(paKpiRetrieverSpy, never()).retrieveCellDegradationThresholdKpis(anyString(), anyString(), anyString(), anyList());
        verify(paPolicyInputEventHandlerMock, never()).sendToKafkaTopic(anyList(), any());
    }

    @Test
    public void whenRetrievingSectorKpis_andInterruptSignalIsCalledAtStartOfGeneratingList_thenThrowPaExecutionException()
            throws PAExecutionException, SQLException {
        final Execution flmExecution = flmExecution();
        final PAExecution paExecution = paExecution();
        final Map<Long, List<TopologyObjectId>> sectorsAndCells = sectorsAndCells(Arrays.asList(SECTOR_ID_ONE, SECTOR_ID_TWO));

        doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        final PAKpiReader objectUnderTest = new PAKpiReader(paExecution, flmExecution, paKpiRetrieverSpy, latch);

        verifyExecutionFails(objectUnderTest, sectorsAndCells, PAExecutionInterruptedException.class);

        verify(paKpiRetrieverSpy, times(ONCE)).retrieveSectorHourlyKpis(anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, times(ONCE)).retrieveSectorDegradationThresholdKpis(anyString(), anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, never()).retrieveCellHourlyKpis(anyString(), anyString(), anyList());
        verify(paKpiRetrieverSpy, never()).retrieveCellDegradationThresholdKpis(anyString(), anyString(), anyString(), anyList());
    }

    @Test
    public void whenRetrievingSectorKpis_andInterruptSignalIsCalledDuringGeneratingList_thenThrowPaExecutionException()
            throws PAExecutionException, SQLException {
        final Execution flmExecution = flmExecution();
        final PAExecution paExecution = paExecution();
        final Map<Long, List<TopologyObjectId>> sectorsAndCells = sectorsAndCells(Arrays.asList(SECTOR_ID_ONE, SECTOR_ID_TWO));

        doNothing().doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        final PAKpiReader objectUnderTest = new PAKpiReader(paExecution, flmExecution, paKpiRetrieverSpy, latch);

        verifyExecutionFails(objectUnderTest, sectorsAndCells, PAExecutionInterruptedException.class);

        verify(paKpiRetrieverSpy, times(ONCE)).retrieveSectorHourlyKpis(anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, times(ONCE)).retrieveSectorDegradationThresholdKpis(anyString(), anyString(), anyString(), anySet());
        verify(paKpiRetrieverSpy, times(ONCE)).retrieveCellHourlyKpis(anyString(), anyString(), anyList());
        verify(paKpiRetrieverSpy, times(ONCE)).retrieveCellDegradationThresholdKpis(anyString(), anyString(), anyString(), anyList());
    }

    private <T extends PAExecutionException> void verifyExecutionFails(final PAKpiReader objectUnderTest,
                                                                       final Map<Long, List<TopologyObjectId>> sectorsAndCells, final Class<T> exceptionType) {
        try {
            objectUnderTest.generateSectorList(sectorsAndCells);
            shouldHaveThrown(PAExecutionException.class);
        } catch (final PAExecutionException e) {
            assertThat(e.getClass()).isEqualTo(exceptionType);
        }
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

    private static Map<Long, Map<String, Map<String, Object>>> hourlyKpisForSectorOne() {
        return new MapBuilder<Long, Map<String, Map<String, Object>>>()
                .with(SECTOR_ID_ONE, new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(AVG_DL_PDCP_THROUGHPUT_SECTOR.getKpiName(), "98.8")
                                .with(AVG_UL_PDCP_THROUGHPUT_SECTOR.getKpiName(), "90.8")
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(AVG_DL_PDCP_THROUGHPUT_SECTOR.getKpiName(), "99.8")
                                .with(AVG_UL_PDCP_THROUGHPUT_SECTOR.getKpiName(), "91.8")
                                .build())
                        .build())
                .build();
    }

    private static Map<Long, Map<String, Map<String, Object>>> degradationKpisForSectorOne() {
        return new MapBuilder<Long, Map<String, Map<String, Object>>>()
                .with(SECTOR_ID_ONE, new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(AVG_DL_PDCP_THROUGHPUT_SECTOR.getThresholdName(), "98.2")
                                .with(AVG_UL_PDCP_THROUGHPUT_SECTOR.getThresholdName(), "90.2")
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(AVG_DL_PDCP_THROUGHPUT_SECTOR.getThresholdName(), "99.2")
                                .with(AVG_UL_PDCP_THROUGHPUT_SECTOR.getThresholdName(), "91.2")
                                .build())
                        .build())
                .build();
    }

    private static Map<CellIdentifier, Map<String, Map<String, Object>>> hourlyCellKpisForSectorOne() {
        return new MapBuilder<CellIdentifier, Map<String, Map<String, Object>>>()
                .with(new CellIdentifier(OSS_ID, FDN_ONE), new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), "98.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), "90.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName(), "92.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(), "97.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(), "91.8")
                                .with(UPLINK_PUSCH_SINR.getKpiName(), "98.8")
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), "99.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), "91.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName(), "93.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(), "98.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(), "92.8")
                                .with(UPLINK_PUSCH_SINR.getKpiName(), "99.8")
                                .build())
                        .build())
                .with(new CellIdentifier(OSS_ID, FDN_TWO), new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), "98.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), "90.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName(), "92.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(), "97.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(), "91.8")
                                .with(UPLINK_PUSCH_SINR.getKpiName(), "98.8")
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getKpiName(), "98.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), "90.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getKpiName(), "92.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(), "97.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(), "91.8")
                                .with(UPLINK_PUSCH_SINR.getKpiName(), "98.8")
                                .build())
                        .build())
                .build();
    }

    private static Map<CellIdentifier, Map<String, Map<String, Object>>> degradationCellKpisForSectorOne() {
        return new MapBuilder<CellIdentifier, Map<String, Map<String, Object>>>()
                .with(new CellIdentifier(OSS_ID, FDN_ONE), new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), "99.2")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), "91.2")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getThresholdName(), "93.2")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getThresholdName(), "98.2")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getThresholdName(), "92.2")
                                .with(UPLINK_PUSCH_SINR.getThresholdName(), "99.2")
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), "98.2")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), "90.2")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getThresholdName(), "92.2")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getThresholdName(), "97.2")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getThresholdName(), "91.2")
                                .with(UPLINK_PUSCH_SINR.getThresholdName(), "98.2")
                                .build())
                        .build())
                .with(new CellIdentifier(OSS_ID, FDN_TWO), new MapBuilder<String, Map<String, Object>>()
                        .with(TIMESTAMP_AT_TEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), "98.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), "90.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getThresholdName(), "92.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getThresholdName(), "97.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getThresholdName(), "91.8")
                                .with(UPLINK_PUSCH_SINR.getThresholdName(), "98.8")
                                .build())
                        .with(TIMESTAMP_AT_ELEVEN_AM, new MapBuilder<String, Object>()
                                .with(CELL_HANDOVER_SUCCESS_RATE.getThresholdName(), "98.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getThresholdName(), "90.8")
                                .with(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1.getThresholdName(), "92.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST.getThresholdName(), "97.8")
                                .with(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getThresholdName(), "91.8")
                                .with(UPLINK_PUSCH_SINR.getThresholdName(), "98.8")
                                .build())
                        .build())
                .build();
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

        private PAKpiReaderTest.MapBuilder<K, V> with(final K key, final V value) {
            map.put(key, value);
            return this;
        }

        private Map<K, V> build() {
            return map;
        }
    }
}

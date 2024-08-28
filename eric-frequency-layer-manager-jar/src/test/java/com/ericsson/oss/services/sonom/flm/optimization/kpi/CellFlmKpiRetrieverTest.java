/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.optimization.kpi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmDao;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmExternalDao;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

/**
 * Unit tests for {@link CellFlmKpiRetriever} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellFlmKpiRetrieverTest {

    private static final String EXECUTION_ID = "FLM_xxx";
    private static final String OUTDOOR = "outdoor";
    private static final String LOCAL_TIMESTAMP = "2020-12-02 01:00:00.0";

    private static final Cell CELL = new Cell(1000L, 1, "fdn1", 5, OUTDOOR, "undefined");
    private static final CellKpi CELL_KPI = new CellKpi(CELL.getFdn(), CELL.getOssId(), LOCAL_TIMESTAMP);
    private static final CellIdentifier CELL_ID = new CellIdentifier(CELL.getOssId(), CELL.getFdn());

    private static final Map<String, Object> CELL_1_KPIS = new HashMap<>(BusyHourCellFlmKpis.values().length);
    private static final Map<CellIdentifier, Map<String, Map<String, Object>>> T1_CELL_KPIS = new HashMap<>();

    private static final Map<String, Object> CELL_2_KPIS = new HashMap<>(CellSectorDailyKpis.values().length);
    private static final Map<CellKpi, Map<String, Object>> T2_CELL_KPIS = new HashMap<>();

    private static final Map<String, Object> CELL_3_KPIS = new HashMap<>(CellDailyKpis.values().length);
    private static final Map<CellKpi, Map<String, Object>> T3_CELL_KPIS = new HashMap<>();

    private static final Map<String, Object> CELL_4_KPIS = new HashMap<>(CellGuid60Kpis.values().length);
    private static final Map<CellIdentifier, Map<String, Map<String, Object>>> T4_CELL_KPIS = new HashMap<>();

    private static final Map<String, Object> CELL_5_KPIS = new HashMap<>(KpisCellGuid1440.values().length);
    private static final Map<CellKpi, Map<String, Object>> T5_CELL_KPIS = new HashMap<>();

    private static final Map<String, Object> CELL_6_KPIS = new HashMap<>(CellSectorFlmKpis.values().length);
    private static final Map<CellKpi, Map<String, Object>> T6_CELL_KPIS = new HashMap<>();

    private static final Map<String, Object> CELL_7_KPIS = new HashMap<>(CellSectorFlmVisibleKpis.values().length);
    private static final Map<CellKpi, Map<String, Object>> T7_CELL_KPIS = new HashMap<>();

    static {
        final Map<String, Map<String, Object>> timestampToKpis = new HashMap<>();
        CELL_1_KPIS.put(BusyHourCellFlmKpis.UNHAPPY_USERS.getKpiName(), 25d);
        CELL_1_KPIS.put(BusyHourCellFlmKpis.GOAL_FUNCTION_RESOURCE_EFFICIENCY.getKpiName(), 85d);
        CELL_1_KPIS.put(BusyHourCellFlmKpis.P_FAILING_R_MBPS.getKpiName(), 0.5);
        CELL_1_KPIS.put(BusyHourCellFlmKpis.P_FAILING_R_MBPS_DETRENDED.getKpiName(), 0.5);
        timestampToKpis.put(LOCAL_TIMESTAMP, CELL_1_KPIS);
        T1_CELL_KPIS.put(CELL_ID, timestampToKpis);

        CELL_2_KPIS.put(CellSectorDailyKpis.COVERAGE_BALANCE_RATIO_DISTANCE.getKpiName(), 112.12765957446808);
        T2_CELL_KPIS.put(CELL_KPI, CELL_2_KPIS);

        CELL_3_KPIS.put(CellDailyKpis.CONTIGUITY.getKpiName(), 100d);
        CELL_3_KPIS.put(CellDailyKpis.CELL_HANDOVER_SUCCESS_RATE.getKpiName(), null);
        CELL_3_KPIS.put(CellDailyKpis.E_RAB_RETAINABILITY_PERCENTAGE_LOST.getKpiName(), 0d);
        CELL_3_KPIS.put(CellDailyKpis.E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1.getKpiName(), 0d);
        CELL_3_KPIS.put(CellDailyKpis.INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR.getKpiName(), 0d);
        CELL_3_KPIS.put(CellDailyKpis.INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT.getKpiName(), null);
        T3_CELL_KPIS.put(CELL_KPI, CELL_3_KPIS);

        CELL_4_KPIS.put(CellGuid60Kpis.CONNECTED_USERS.getKpiName(), 11.7652777777777775);
        CELL_4_KPIS.put(CellGuid60Kpis.DISTANCE_Q1.getKpiName(), 78.2666666666666515);
        CELL_4_KPIS.put(CellGuid60Kpis.DISTANCE_Q2.getKpiName(), 156.533333333333303);
        CELL_4_KPIS.put(CellGuid60Kpis.DISTANCE_Q3.getKpiName(), 234.799999999999955);
        CELL_4_KPIS.put(CellGuid60Kpis.DISTANCE_Q4.getKpiName(), 313.066666666666606);
        CELL_4_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q1.getKpiName(), 37.9844961240310113);
        CELL_4_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q2.getKpiName(), 44.1860465116279073);
        CELL_4_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q3.getKpiName(), 4.65116279069767469);
        CELL_4_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q4.getKpiName(), 13.1782945736434112);
        timestampToKpis.put(LOCAL_TIMESTAMP, CELL_4_KPIS);
        T4_CELL_KPIS.put(CELL_ID, timestampToKpis);

        CELL_5_KPIS.put(KpisCellGuid1440.CELL_AVAILABILITY.getKpiName(), 3.0);
        T5_CELL_KPIS.put(CELL_KPI, CELL_5_KPIS);

        CELL_6_KPIS.put(CellSectorFlmKpis.TARGET_CELL_CAPACITY.getKpiName(), 6.96927777777778);
        CELL_6_KPIS.put(CellSectorFlmKpis.MAX_CONNECTED_USERS.getKpiName(), 11.404);
        CELL_6_KPIS.put(CellSectorFlmKpis.NUM_CELLS_USED_FOR_MCU_CDF_CALCULATION.getKpiName(), 6d);
        T6_CELL_KPIS.put(CELL_KPI, CELL_6_KPIS);

        CELL_7_KPIS.put(CellSectorFlmVisibleKpis.LOWER_THRESHOLD_FOR_TRANSIENT.getKpiName(), 23.0);
        CELL_7_KPIS.put(CellSectorFlmVisibleKpis.UPPER_THRESHOLD_FOR_TRANSIENT.getKpiName(), 34.5);
        T7_CELL_KPIS.put(CELL_KPI, CELL_7_KPIS);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private KpiCellFlmExternalDao kpiCellFlmExternalDao;

    @Mock
    private KpiCellFlmDao kpiCellFlmDao;

    @InjectMocks
    private CellFlmKpiRetriever objectUnderTest;

    @Test
    public void whenFlmExecutionExists_thenKpisPerCellReturnedCorrectly() throws SQLException {
        final List<String> kpiNames = Arrays.stream(BusyHourCellFlmKpis.values())
                .map(kpi -> kpi.getKpiName())
                .collect(Collectors.toList());
        doReturn(T1_CELL_KPIS).when(kpiCellFlmExternalDao).getCellHourlyFlmKpis(EXECUTION_ID, kpiNames);

        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellKpis = objectUnderTest.retrieveBusyHourCellFlmKpis(EXECUTION_ID);
        assertThat(cellKpis).containsExactlyInAnyOrderEntriesOf(T1_CELL_KPIS);
    }

    @Test
    public void whenFlmExecutionExistsForGivenDay_thenCoverageBalanceRatioKpisForGivenSectorReturnedCorrectly() throws SQLException {
        final List<String> kpiNames = Arrays.stream(CellSectorDailyKpis.values())
                .map(kpi -> kpi.getKpiName())
                .collect(Collectors.toList());
        final String executionDay = DateTimeFormatter.ISO_DATE.format(LocalDate.now().atStartOfDay());
        doReturn(T2_CELL_KPIS).when(kpiCellFlmExternalDao).getCoverageBalanceKpis(eq(executionDay), anyList(), eq(kpiNames));

        final Map<CellKpi, Map<String, Object>> cellKpis = objectUnderTest
                .retrieveCoverageBalanceFlmKpis(new ArrayList<>(), executionDay);
        assertThat(cellKpis).containsExactlyInAnyOrderEntriesOf(T2_CELL_KPIS);
    }

    @Test
    public void whenFlmExecutionExistsForGivenDay_thenCellDailyKpisReturnedCorrectly() throws SQLException {
        final List<String> kpiNames = Arrays.stream(CellDailyKpis.values())
                .map(kpi -> kpi.getKpiName())
                .collect(Collectors.toList());
        final String executionDay = DateTimeFormatter.ISO_DATE.format(LocalDate.now().atStartOfDay());
        doReturn(T3_CELL_KPIS).when(kpiCellFlmExternalDao).getCellDailyKpis(executionDay, kpiNames);

        final Map<CellKpi, Map<String, Object>> cellKpis = objectUnderTest.retrieveCellDailyKpis(executionDay);
        assertThat(cellKpis).containsExactlyInAnyOrderEntriesOf(T3_CELL_KPIS);
    }

    @Test
    public void whenFlmExecutionExistsForGivenDay_thenCellHourlyKpisReturnedCorrectly() throws SQLException {
        final List<String> kpiNames = Arrays.stream(CellGuid60Kpis.values())
                .map(kpi -> kpi.getKpiName())
                .collect(Collectors.toList());
        final String today = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
        final String startDateTime = LocalDate.parse(today, DateTimeFormatter.ISO_DATE).atStartOfDay().minusDays(1).toString();
        final String endDateTime = LocalDate.parse(today, DateTimeFormatter.ISO_DATE).atStartOfDay().toString();
        doReturn(T4_CELL_KPIS).when(kpiCellFlmExternalDao).getCellHourlyKpis(startDateTime, endDateTime, kpiNames);

        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellKpis = objectUnderTest.retrieveCellHourlyKpis(startDateTime,
                endDateTime);
        assertThat(cellKpis).containsExactlyInAnyOrderEntriesOf(T4_CELL_KPIS);
    }

    @Test
    public void whenFlmExecutionExistsForGivenDay_thenNotVisibleCellDailyKpisReturnedCorrectly() throws SQLException {
        final List<String> kpiNames = Arrays.stream(KpisCellGuid1440.values())
                .map(kpi -> kpi.getKpiName())
                .collect(Collectors.toList());
        final String executionDay = DateTimeFormatter.ISO_DATE.format(LocalDate.now().atStartOfDay());
        doReturn(T5_CELL_KPIS).when(kpiCellFlmDao).getNotVisibleCellDailyKpis(executionDay, kpiNames);

        final Map<CellKpi, Map<String, Object>> cellKpis = objectUnderTest.retrieveNotVisibleCellDailyKpis(executionDay);
        assertThat(cellKpis).containsExactlyInAnyOrderEntriesOf(T5_CELL_KPIS);
    }

    @Test
    public void whenFlmExecutionExistsForGivenDay_thenTargetCellCapacityKpisReturnedCorrectly() throws SQLException {
        final List<String> kpiNames = Arrays.stream(CellSectorFlmKpis.values())
                .map(kpi -> kpi.getKpiName())
                .collect(Collectors.toList());
        doReturn(T6_CELL_KPIS).when(kpiCellFlmDao).getNotVisibleCellSectorDailyFlmKpis(EXECUTION_ID, kpiNames);

        final Map<CellKpi, Map<String, Object>> cellKpis = objectUnderTest.retrieveNotVisibleCellSectorFlmKpis(EXECUTION_ID);
        assertThat(cellKpis).containsExactlyInAnyOrderEntriesOf(T6_CELL_KPIS);
    }

    @Test
    public void whenFlmExecutionExistsForGivenDay_thenVisibleSectorKpisReturnedCorrectly() throws SQLException {
        final List<String> kpiNames = Arrays.stream(CellSectorFlmVisibleKpis.values())
                .map(kpi -> kpi.getKpiName())
                .collect(Collectors.toList());
        doReturn(T7_CELL_KPIS).when(kpiCellFlmExternalDao).getCellSectorDailyFlmKpis(EXECUTION_ID, kpiNames);

        final Map<CellKpi, Map<String, Object>> cellKpis = objectUnderTest.retrieveVisibleCellSectorFlmKpis(EXECUTION_ID);
        assertThat(cellKpis).containsExactlyInAnyOrderEntriesOf(T7_CELL_KPIS);
    }

    @Test
    public void whenFailGettingKpisForCellPerFlmExecution_thenThrowSQLException() throws SQLException {
        doReturn(T1_CELL_KPIS).when(kpiCellFlmExternalDao).getCellHourlyFlmKpis(anyString(), anyList());
        when(kpiCellFlmExternalDao.getCellHourlyFlmKpis(anyString(), anyList())).thenThrow(SQLException.class);
        thrown.expect(SQLException.class);
        objectUnderTest.retrieveBusyHourCellFlmKpis(EXECUTION_ID);
    }

    @Test
    public void whenFailGettingKpisForCoverageBalanceRatio_thenThrowSQLException() throws SQLException {
        final String executionDay = DateTimeFormatter.ISO_DATE.format(LocalDate.now().atStartOfDay());
        doReturn(T2_CELL_KPIS).when(kpiCellFlmExternalDao).getCoverageBalanceKpis(anyString(), anyList(), anyList());
        when(kpiCellFlmExternalDao.getCoverageBalanceKpis(anyString(), anyList(), anyList())).thenThrow(SQLException.class);
        thrown.expect(SQLException.class);
        objectUnderTest.retrieveCoverageBalanceFlmKpis(new ArrayList<>(), executionDay);
    }

    @Test
    public void whenFailGettingCellDailyKpis_thenThrowSQLException() throws SQLException {
        final String executionDay = DateTimeFormatter.ISO_DATE.format(LocalDate.now().atStartOfDay());
        doReturn(T3_CELL_KPIS).when(kpiCellFlmExternalDao).getCellDailyKpis(anyString(), anyList());
        when(kpiCellFlmExternalDao.getCellDailyKpis(anyString(), anyList())).thenThrow(SQLException.class);
        thrown.expect(SQLException.class);
        objectUnderTest.retrieveCellDailyKpis(executionDay);
    }

    @Test
    public void whenFailGettingCellHourlyKpis_thenThrowSQLException() throws SQLException {
        final String today = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
        final String startDateTime = LocalDate.parse(today, DateTimeFormatter.ISO_DATE).atStartOfDay().minusDays(1).toString();
        final String endDateTime = LocalDate.parse(today, DateTimeFormatter.ISO_DATE).atStartOfDay().toString();
        doReturn(T4_CELL_KPIS).when(kpiCellFlmExternalDao).getCellHourlyKpis(anyString(), anyString(), anyList());
        when(kpiCellFlmExternalDao.getCellHourlyKpis(anyString(), anyString(), anyList())).thenThrow(SQLException.class);
        thrown.expect(SQLException.class);
        objectUnderTest.retrieveCellHourlyKpis(startDateTime, endDateTime);
    }

    @Test
    public void whenFailGettingNotVisibleCellDailyKpis_thenThrowSQLException() throws SQLException {
        final String executionDay = DateTimeFormatter.ISO_DATE.format(LocalDate.now().atStartOfDay());
        doReturn(T5_CELL_KPIS).when(kpiCellFlmDao).getNotVisibleCellDailyKpis(anyString(), anyList());
        when(kpiCellFlmDao.getNotVisibleCellDailyKpis(anyString(), anyList())).thenThrow(SQLException.class);
        thrown.expect(SQLException.class);
        objectUnderTest.retrieveNotVisibleCellDailyKpis(executionDay);
    }

    @Test
    public void whenFailGettingTargetCellCapacity_thenThrowSQLException() throws SQLException {
        doReturn(T6_CELL_KPIS).when(kpiCellFlmDao).getNotVisibleCellSectorDailyFlmKpis(anyString(), anyList());
        when(kpiCellFlmDao.getNotVisibleCellSectorDailyFlmKpis(anyString(), anyList())).thenThrow(SQLException.class);
        thrown.expect(SQLException.class);
        objectUnderTest.retrieveNotVisibleCellSectorFlmKpis(EXECUTION_ID);
    }

    @Test
    public void whenDefaultConstructorUsed_andDatabaseNotSetUp_thenThrowSQLException() throws NullPointerException, SQLException {
        objectUnderTest = new CellFlmKpiRetriever();
        thrown.expect(SQLException.class);
        objectUnderTest.retrieveBusyHourCellFlmKpis(EXECUTION_ID);
    }
}
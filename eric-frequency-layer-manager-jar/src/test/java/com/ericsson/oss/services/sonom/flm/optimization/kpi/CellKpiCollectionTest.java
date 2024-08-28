/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
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
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.metric.FlmMetricHelper;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

/**
 * Unit tests for {@link CellKpiCollection} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellKpiCollectionTest {

    private static final String EXECUTION_ID = "FLM_xxx";
    private static final String OUTDOOR = "outdoor";
    private static final String LTENRSPECTRUMSHARED = "undefined";

    private static final Cell CELL_1 = new Cell(1000L, 1, "fdn1", 1400, OUTDOOR, LTENRSPECTRUMSHARED);
    private static final Cell CELL_2 = new Cell(1001L, 1, "fdn2", 1400, OUTDOOR, LTENRSPECTRUMSHARED);
    private static final Cell CELL_3 = new Cell(2000L, 1, "fdn3", 1400, OUTDOOR, LTENRSPECTRUMSHARED);
    private static final Cell CELL_4 = new Cell(2001L, 1, "fdn4", 1400, OUTDOOR, LTENRSPECTRUMSHARED);
    private static final Cell CELL_5 = new Cell(3001L, 1, "fdn5", 1400, OUTDOOR, LTENRSPECTRUMSHARED);
    private static final Map<String, Object> CELL_1_KPIS = new HashMap<>(2);
    private static final Map<String, Object> CELL_2_KPIS = new HashMap<>(2);
    private static final Map<String, Object> CELL_3_KPIS = new HashMap<>(2);
    private static final String DATE = "2020-01-17";
    private static final String DATE_MINUS_1_DAY = "2020-01-16";
    private static final int SIX = 6;

    static {
        CELL_1_KPIS.put(BusyHourCellFlmKpis.UNHAPPY_USERS.toString(), 10.0);
        CELL_1_KPIS.put(BusyHourCellFlmKpis.GOAL_FUNCTION_RESOURCE_EFFICIENCY.toString(), 75.0);
        CELL_1_KPIS.put(CellGuid60Kpis.CONNECTED_USERS.toString(), 1.90555555555555545);
        CELL_1_KPIS.put(CellGuid60Kpis.DISTANCE_Q1.toString(), 94.9200000000000017);
        CELL_1_KPIS.put(CellGuid60Kpis.DISTANCE_Q2.toString(), 189.840000000000003);
        CELL_1_KPIS.put(CellGuid60Kpis.DISTANCE_Q3.toString(), 284.759999999999991);
        CELL_1_KPIS.put(CellGuid60Kpis.DISTANCE_Q4.toString(), 379.680000000000007);
        CELL_1_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q1.toString(), 18.518518518518519);
        CELL_1_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q2.toString(), 44.4444444444444429);
        CELL_1_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q3.toString(), 33.3333333333333357);
        CELL_1_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q4.toString(), 3.70370370370370372);
        CELL_1_KPIS.put(KpisCellGuid1440.CELL_AVAILABILITY.toString(), 1.0);

        CELL_2_KPIS.put(BusyHourCellFlmKpis.UNHAPPY_USERS.toString(), 20.0);
        CELL_2_KPIS.put(BusyHourCellFlmKpis.GOAL_FUNCTION_RESOURCE_EFFICIENCY.toString(), 80.0);
        CELL_2_KPIS.put(CellGuid60Kpis.CONNECTED_USERS.toString(), 9.905555555555555);
        CELL_2_KPIS.put(CellGuid60Kpis.DISTANCE_Q1.toString(), 114.360000000000014);
        CELL_2_KPIS.put(CellGuid60Kpis.DISTANCE_Q2.toString(), 228.720000000000027);
        CELL_2_KPIS.put(CellGuid60Kpis.DISTANCE_Q3.toString(), 343.080000000000041);
        CELL_2_KPIS.put(CellGuid60Kpis.DISTANCE_Q4.toString(), 457.440000000000055);
        CELL_2_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q1.toString(), 57.2222222222222214);
        CELL_2_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q2.toString(), 23.8888888888888893);
        CELL_2_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q3.toString(), 13.3333333333333339);
        CELL_2_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q4.toString(), 5.55555555555555536);
        CELL_2_KPIS.put(KpisCellGuid1440.CELL_AVAILABILITY.toString(), 3.0);

        CELL_3_KPIS.put(BusyHourCellFlmKpis.UNHAPPY_USERS.toString(), 25.0);
        CELL_3_KPIS.put(BusyHourCellFlmKpis.GOAL_FUNCTION_RESOURCE_EFFICIENCY.toString(), 85.0);
        CELL_3_KPIS.put(CellGuid60Kpis.CONNECTED_USERS.toString(), 11.7652777777777775);
        CELL_3_KPIS.put(CellGuid60Kpis.DISTANCE_Q1.toString(), 78.2666666666666515);
        CELL_3_KPIS.put(CellGuid60Kpis.DISTANCE_Q2.toString(), 156.533333333333303);
        CELL_3_KPIS.put(CellGuid60Kpis.DISTANCE_Q3.toString(), 234.799999999999955);
        CELL_3_KPIS.put(CellGuid60Kpis.DISTANCE_Q4.toString(), 313.066666666666606);
        CELL_3_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q1.toString(), 37.9844961240310113);
        CELL_3_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q2.toString(), 44.1860465116279073);
        CELL_3_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q3.toString(), 4.65116279069767469);
        CELL_3_KPIS.put(CellGuid60Kpis.UE_PERCENTAGE_Q4.toString(), 13.1782945736434112);
        CELL_3_KPIS.put(KpisCellGuid1440.CELL_AVAILABILITY.toString(), 3.0);
    }

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private CellFlmKpiRetriever mockedCellFlmKpiRetriever;

    @Mock
    private FlmMetricHelper mockedFlmMetricHelper;

    @InjectMocks
    private CellKpiCollection objectUnderTest;

    @Test
    public void whenSectorBusyHourKpisExist_thenCellKpisAreReturnedCorrectly() throws SQLException {
        final List<TopologySector> sectors = Arrays.asList(
                new TopologySector(1L, Arrays.asList(CELL_1, CELL_2)),
                new TopologySector(2L, Arrays.asList(CELL_3, CELL_4)));
        doReturn(createKpisForCells()).when(mockedCellFlmKpiRetriever).retrieveAllKpis(eq(DATE_MINUS_1_DAY), any(),
                any(), eq(EXECUTION_ID));

        final Map<CellIdentifier, Map<String, Object>> cellKpis = objectUnderTest.collect(sectors, EXECUTION_ID, DATE, DATE_MINUS_1_DAY);
        final CellIdentifier cell1Id = getCellIdentifier(CELL_1);
        final CellIdentifier cell2Id = getCellIdentifier(CELL_2);
        final CellIdentifier cell3Id = getCellIdentifier(CELL_3);
        final CellIdentifier cell4Id = getCellIdentifier(CELL_4);
        softly.assertThat(cellKpis).containsOnlyKeys(
                cell1Id, cell2Id, cell3Id, cell4Id);
        softly.assertThat(cellKpis.get(cell1Id)).isEqualTo(CELL_1_KPIS);
        softly.assertThat(cellKpis.get(cell2Id)).isEmpty();
        softly.assertThat(cellKpis.get(cell3Id)).isEqualTo(CELL_3_KPIS);
        softly.assertThat(cellKpis.get(cell4Id)).isEmpty();

        verifyFlmMetricMocks();
    }

    @Test
    public void whenSectorBusyHourExists_butKpisAreNotPresentForBusyHour_thenNoKpisAreReturned() throws SQLException {
        final List<TopologySector> sectors = Collections.singletonList(new TopologySector(1L, Collections.singletonList(CELL_5)));
        doReturn(createKpisForCells()).when(mockedCellFlmKpiRetriever).retrieveAllKpis(eq(DATE_MINUS_1_DAY), any(),
                any(), eq(EXECUTION_ID));

        final Map<CellIdentifier, Map<String, Object>> cellKpis = objectUnderTest.collect(sectors, EXECUTION_ID, DATE, DATE_MINUS_1_DAY);

        final CellIdentifier cell5Id = getCellIdentifier(CELL_5);
        softly.assertThat(cellKpis).containsOnlyKeys(cell5Id);
        softly.assertThat(cellKpis.get(cell5Id)).isEmpty();

        verifyFlmMetricMocks();
    }

    @Test
    public void whenNoSectorBusyHourIsNotPresent_thenNoKpisAreReturned() throws SQLException {
        final List<TopologySector> sectors = Collections.singletonList(new TopologySector(5L, Collections.singletonList(CELL_5)));
        doReturn(createKpisForCells()).when(mockedCellFlmKpiRetriever).retrieveAllKpis(eq(DATE_MINUS_1_DAY), any(),
                any(), eq(EXECUTION_ID));

        final Map<CellIdentifier, Map<String, Object>> cellKpis = objectUnderTest.collect(sectors, EXECUTION_ID, DATE, DATE_MINUS_1_DAY);

        final CellIdentifier cell5Id = getCellIdentifier(CELL_5);
        softly.assertThat(cellKpis).containsOnlyKeys(cell5Id);
        softly.assertThat(cellKpis.get(cell5Id)).isEmpty();

        verifyFlmMetricMocks();
    }

    @Test
    public void whenDuplicateCellExistsInSector_thenOnlyOneCellReturnedWithItCorrectKpis() throws SQLException {
        final List<TopologySector> sectors = Collections.singletonList(new TopologySector(4L, Arrays.asList(CELL_1, CELL_1)));
        doReturn(createKpisForCells()).when(mockedCellFlmKpiRetriever).retrieveAllKpis(eq(DATE_MINUS_1_DAY), any(),
                any(), eq(EXECUTION_ID));

        final Map<CellIdentifier, Map<String, Object>> cellKpis = objectUnderTest.collect(sectors, EXECUTION_ID, DATE, DATE_MINUS_1_DAY);

        assertThat(cellKpis).containsExactly(entry(getCellIdentifier(CELL_1), CELL_1_KPIS));

        verifyFlmMetricMocks();
    }

    @Test
    public void whenDuplicateCellExistsInTwoSectors_thenOnlyOneCellReturnedWithItCorrectKpis() throws SQLException {
        final List<TopologySector> sectors = Arrays.asList(
                new TopologySector(1L, Collections.singletonList(CELL_1)),
                new TopologySector(4L, Collections.singletonList(CELL_1)));
        doReturn(createKpisForCells()).when(mockedCellFlmKpiRetriever).retrieveAllKpis(eq(DATE_MINUS_1_DAY), any(),
                any(), eq(EXECUTION_ID));

        final Map<CellIdentifier, Map<String, Object>> cellKpis = objectUnderTest.collect(sectors, EXECUTION_ID, DATE, DATE_MINUS_1_DAY);

        assertThat(cellKpis).containsExactly(entry(getCellIdentifier(CELL_1), CELL_1_KPIS));

        verifyFlmMetricMocks();
    }

    private Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>> createKpisForCells() {
        return Tuple.of(createSectorBusyHourMap(), createCellKpisMap());
    }

    private Map<CellIdentifier, Map<String, Map<String, Object>>> createCellKpisMap() {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellKpis = new HashMap<>(3);

        final Map<String, Map<String, Object>> value1 = new HashMap<>(1);
        value1.put("12:00:00", CELL_1_KPIS);

        final Map<String, Map<String, Object>> value2 = new HashMap<>(1);
        value2.put("13:00:00", CELL_2_KPIS);

        final Map<String, Map<String, Object>> value3 = new HashMap<>(1);
        value3.put("13:00:00", CELL_3_KPIS);

        cellKpis.put(new CellIdentifier(CELL_1.getOssId(), CELL_1.getFdn()), value1);
        cellKpis.put(new CellIdentifier(CELL_2.getOssId(), CELL_2.getFdn()), value2);
        cellKpis.put(new CellIdentifier(CELL_3.getOssId(), CELL_3.getFdn()), value3);

        return cellKpis;
    }

    private Map<Long, String> createSectorBusyHourMap() {
        final Map<Long, String> sectorBusyHoursBySectorId = new HashMap<>(3);
        sectorBusyHoursBySectorId.put(1L, "12:00:00");
        sectorBusyHoursBySectorId.put(2L, "13:00:00");
        sectorBusyHoursBySectorId.put(4L, "12:00:00");
        return sectorBusyHoursBySectorId;
    }

    private CellIdentifier getCellIdentifier(final Cell cell) {
        return new CellIdentifier(cell.getOssId(), cell.getFdn());
    }

    private void verifyFlmMetricMocks() {
        verify(mockedFlmMetricHelper, times(SIX)).getTimeElapsedInMillis(anyLong());
        verify(mockedFlmMetricHelper, times(SIX)).incrementFlmMetric(any(), anyLong());
    }
}
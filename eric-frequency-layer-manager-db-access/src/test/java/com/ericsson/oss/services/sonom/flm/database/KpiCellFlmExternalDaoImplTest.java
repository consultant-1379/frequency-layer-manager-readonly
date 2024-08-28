/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database;

import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.CONNECTED_USERS;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.DISTANCE_Q1;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.DISTANCE_Q2;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.DISTANCE_Q3;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.DISTANCE_Q4;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.UE_PERCENTAGE_Q1;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.UE_PERCENTAGE_Q2;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.UE_PERCENTAGE_Q3;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.UE_PERCENTAGE_Q4;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellSectorFlmDbConstants.LOWER_THRESHOLD_FOR_TRANSIENT;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellSectorFlmDbConstants.UPPER_THRESHOLD_FOR_TRANSIENT;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.CELL_GUID_1440_TABLE;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.CELL_GUID_60_KPIS_TABLE;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.CELL_GUID_FLM_60_KPIS_TABLE;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.CELL_HANDOVER_SUCCESS_RATE;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.CELL_SECTOR_1440_FLM_TABLE;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.CELL_SECTOR_1440_TABLE;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.CONTIGUITY;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.COVERAGE_BALANCE_RATIO_DISTANCE;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.E_RAB_RETAINABILITY_PERCENTAGE_LOST;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands;
import com.ericsson.oss.services.sonom.flm.database.kpi.KpiServiceUnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

/**
 * Unit tests for {@link KpiCellFlmExternalDaoImpl} class.
 */
@RunWith(OrderedTestRunner.class)
public final class KpiCellFlmExternalDaoImplTest {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final String CELL_GUID_FLM_60_KPIS = "cell_guid_flm_60_kpis.cmds";
    private static final String CELL_GUID_60_KPIS = "cell_guid_60_kpis.cmds";
    private static final String CELL_SECTOR_FLM_1440_KPIS = "cell_sector_flm_1440_kpis.cmds";
    private static final String UNHAPPY_USERS = "unhappy_users";
    private static final String GOAL_FUNCTION_RESOURCE_EFFICIENCY = "goal_function_resource_efficiency";
    private static final String P_FAILING_R_MBPS = "p_failing_r_mbps";
    private static final String P_FAILING_R_MBPS_DETRENDED = "p_failing_r_mbps_detrended";
    private static final String APP_COVERAGE_RELIABILITY = "app_coverage_reliability";
    private static final String EXECUTION_ID = "FLM_Execution1";
    private static final String EXECUTION_ID_FOUR = "FLM_Execution4";
    private static final String FDN_ONE = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1," +
            "EUtranCellFDD=654875_9_2";
    private static final String FDN_TWO = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1," +
            "EUtranCellFDD=654875_9_3";
    private static final String FDN_THREE = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1," +
            "EUtranCellFDD=654875_9_4";
    private static final int OSS_ID = 1;
    private static final String LOCAL_TIMESTAMP = "2020-12-02 01:00:00.0";
    private static final String DAILY_LOCAL_TIMESTAMP = "2020-12-02 00:00:00.0";
    private static final String CELL_SECTOR_1440_KPIS = "cell_sector_1440_kpis.cmds";
    private static final String CELL_GUID_1440_KPIS = "cell_guid_1440_kpis.cmds";
    private static final List<String> SECTORS_IDS = new ArrayList<>(3);
    private static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new KpiServiceUnitTestDatabaseRunner();
    private static final List<String> KPI_NAMES = new ArrayList<>(3);
    private static final List<String> CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES = new ArrayList<>(9);
    private static final List<String> COVERAGE_BALANCE_KPI = new ArrayList<>(2);
    private static final List<String> HOURLY_KPIS_SUBSET_NAMES = new ArrayList<>(3);
    private static final List<String> ON_DEMAND_KPIS_SUBSET_NAMES = new ArrayList<>(2);
    private static final CellIdentifier CELL_ID = new CellIdentifier(OSS_ID, FDN_ONE);
    private static final String EXECUTION_DAY = "2020-12-02 00:00:00";
    private static final List<String> CELL_DAILY_KPI_NAMES = Arrays.asList(
            CONTIGUITY, CELL_HANDOVER_SUCCESS_RATE,
            E_RAB_RETAINABILITY_PERCENTAGE_LOST, E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1,
            INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR, INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT);
    private static final Logger LOGGER = LoggerFactory.getLogger(KpiCellFlmExternalDaoImplTest.class);
    private final KpiCellFlmExternalDaoImpl objectUnderTestKpiCellFlmExternalDao = new KpiCellFlmExternalDaoImpl(MAX_RETRY_ATTEMPTS,
            WAIT_PERIOD_IN_SECONDS);

    @Before
    public void setUp() throws IOException {
        KPI_NAMES.add(UNHAPPY_USERS);
        KPI_NAMES.add(GOAL_FUNCTION_RESOURCE_EFFICIENCY);
        KPI_NAMES.add(P_FAILING_R_MBPS);
        KPI_NAMES.add(APP_COVERAGE_RELIABILITY);
        KPI_NAMES.add(P_FAILING_R_MBPS_DETRENDED);
        COVERAGE_BALANCE_KPI.add(COVERAGE_BALANCE_RATIO_DISTANCE);
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.add(CONNECTED_USERS);
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.add(DISTANCE_Q1);
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.add(DISTANCE_Q2);
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.add(DISTANCE_Q3);
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.add(DISTANCE_Q4);
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.add(UE_PERCENTAGE_Q1);
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.add(UE_PERCENTAGE_Q2);
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.add(UE_PERCENTAGE_Q3);
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.add(UE_PERCENTAGE_Q4);
        HOURLY_KPIS_SUBSET_NAMES.add(CONNECTED_USERS);
        HOURLY_KPIS_SUBSET_NAMES.add(DISTANCE_Q1);
        HOURLY_KPIS_SUBSET_NAMES.add(DISTANCE_Q2);
        ON_DEMAND_KPIS_SUBSET_NAMES.add(UNHAPPY_USERS);
        ON_DEMAND_KPIS_SUBSET_NAMES.add(GOAL_FUNCTION_RESOURCE_EFFICIENCY);
        SECTORS_IDS.add("311");
        SECTORS_IDS.add("312");
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(CELL_GUID_FLM_60_KPIS_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createCellKpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(CELL_GUID_FLM_60_KPIS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(CELL_SECTOR_1440_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createCellSectorTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(CELL_SECTOR_1440_KPIS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(CELL_GUID_1440_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createCell1440KpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(CELL_GUID_1440_KPIS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(CELL_GUID_60_KPIS_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createCellGuid60KpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.addPrimaryKeyCellGuid60KpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(CELL_GUID_60_KPIS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(CELL_SECTOR_1440_FLM_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createCellSectorFlmKpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(CELL_SECTOR_FLM_1440_KPIS));
    }

    @After
    public void tearDown() {
        KPI_NAMES.clear();
        CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES.clear();
        COVERAGE_BALANCE_KPI.clear();
        SECTORS_IDS.clear();
    }

    @Test
    public void whenCellsExist_andKpiValuesAreInDb_thenKpisAreReturned() throws SQLException {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellsWithPopulatedKpis = objectUnderTestKpiCellFlmExternalDao
                .getCellHourlyFlmKpis(EXECUTION_ID, KPI_NAMES);
        assertThat(cellsWithPopulatedKpis).isNotNull();
        assertThat(cellsWithPopulatedKpis.get(CELL_ID)).isNotNull();
        final Map<String, Object> kpis = cellsWithPopulatedKpis.get(CELL_ID).get(LOCAL_TIMESTAMP);
        assertThat(kpis).contains(
                entry(UNHAPPY_USERS, 2),
                entry(GOAL_FUNCTION_RESOURCE_EFFICIENCY, 3),
                entry(P_FAILING_R_MBPS, 0.00640981048823874),
                entry(APP_COVERAGE_RELIABILITY, true),
                entry(P_FAILING_R_MBPS_DETRENDED, 0.631125894338963));
    }

    @Test
    public void whenCellsExist_andAllKpiValuesAreNull_thenNullIsReturned() throws SQLException {
        final String executionIdOfNullRow = "FLM_Execution2";
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellsWithNullKpis = objectUnderTestKpiCellFlmExternalDao
                .getCellHourlyFlmKpis(executionIdOfNullRow, KPI_NAMES);
        assertThat(cellsWithNullKpis).isNotNull();
        assertThat(cellsWithNullKpis.get(CELL_ID)).isNotNull();
        final Map<String, Object> kpis = cellsWithNullKpis.get(CELL_ID).get(LOCAL_TIMESTAMP);
        assertThat(kpis).containsOnly(
                entry(UNHAPPY_USERS, null),
                entry(GOAL_FUNCTION_RESOURCE_EFFICIENCY, null),
                entry(P_FAILING_R_MBPS, null),
                entry(APP_COVERAGE_RELIABILITY, null),
                entry(P_FAILING_R_MBPS_DETRENDED, null));
    }

    @Test
    public void whenCellsExist_andSomeKpiValuesAreNull_thenNullIsReturnedForNullKpis() throws SQLException {
        final String executionIdOfRowWithOneNullValue = "FLM_Execution3";
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellsWithNullKpis = objectUnderTestKpiCellFlmExternalDao
                .getCellHourlyFlmKpis(executionIdOfRowWithOneNullValue, KPI_NAMES);
        assertThat(cellsWithNullKpis).isNotNull();
        final Map<String, Object> actual = cellsWithNullKpis.get(CELL_ID).get(LOCAL_TIMESTAMP);
        assertThat(actual).containsOnly(
                entry(UNHAPPY_USERS, 2),
                entry(GOAL_FUNCTION_RESOURCE_EFFICIENCY, null),
                entry(P_FAILING_R_MBPS, 0.465090061878398),
                entry(APP_COVERAGE_RELIABILITY, true),
                entry(P_FAILING_R_MBPS_DETRENDED, 0.429739880253244));
    }

    @Test
    public void whenCellsExist_andKpiValuesAreInDbForMultipleExecutions_thenKpisAreOnlyReturnedForExecutionIdProvided() throws SQLException {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellsWithPopulatedKpis = objectUnderTestKpiCellFlmExternalDao
                .getCellHourlyFlmKpis(EXECUTION_ID, KPI_NAMES);
        assertThat(cellsWithPopulatedKpis).hasSize(1);
    }

    @Test
    public void whenSectorsExists_andCoverageBalanceRatioKpiValuesAreInDb_thenKpisAreReturned() throws SQLException {
        final Map<CellKpi, Map<String, Object>> cellsWithPopulatedKpis = objectUnderTestKpiCellFlmExternalDao.getCoverageBalanceKpis(
                EXECUTION_DAY,
                SECTORS_IDS, COVERAGE_BALANCE_KPI);
        assertThat(cellsWithPopulatedKpis).hasSize(2);
    }

    @Test
    public void whenSectorsDontExists_andCoverageBalanceRatioKpiValuesAreInDb_thenKpisAreNotReturned() throws SQLException {
        final Map<CellKpi, Map<String, Object>> cellsWithPopulatedKpis = objectUnderTestKpiCellFlmExternalDao
                .getCoverageBalanceKpis(EXECUTION_DAY, Arrays.asList("1", "314"), COVERAGE_BALANCE_KPI);
        assertThat(cellsWithPopulatedKpis).isEmpty();
    }

    @Test
    public void whenCellsExist_andKpiValuesAreInCellDailyTable_thenCellDailyKpisAreReturned() throws SQLException {
        final Map<CellKpi, Map<String, Object>> cellsDailyKpis = objectUnderTestKpiCellFlmExternalDao.getCellDailyKpis(
                EXECUTION_DAY, CELL_DAILY_KPI_NAMES);
        assertThat(cellsDailyKpis).isNotNull();
        LOGGER.info(String.valueOf(cellsDailyKpis));
        final Map<String, Object> kpis = cellsDailyKpis.get(new CellKpi(FDN_ONE, OSS_ID, DAILY_LOCAL_TIMESTAMP));
        LOGGER.info(String.valueOf(cellsDailyKpis));
        assertThat(kpis).contains(
                entry(CONTIGUITY, 10),
                entry(CELL_HANDOVER_SUCCESS_RATE, 70),
                entry(E_RAB_RETAINABILITY_PERCENTAGE_LOST, 71),
                entry(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1, 72),
                entry(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR, 73),
                entry(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT, 74));
    }

    @Test
    public void whenCellsExist_andKpiValuesAreNullInCellDailyTable_thenCellDailyKpisAreReturned() throws SQLException {
        final Map<CellKpi, Map<String, Object>> cellsDailyKpis = objectUnderTestKpiCellFlmExternalDao
                .getCellDailyKpis(EXECUTION_DAY, CELL_DAILY_KPI_NAMES);
        assertThat(cellsDailyKpis).isNotNull();
        LOGGER.info(String.valueOf(cellsDailyKpis));
        final Map<String, Object> kpis = cellsDailyKpis.get(new CellKpi(FDN_TWO, OSS_ID, DAILY_LOCAL_TIMESTAMP));
        assertThat(cellsDailyKpis).isNotNull();
        LOGGER.info(String.valueOf(cellsDailyKpis));
        assertThat(kpis).contains(
                entry(CONTIGUITY, null),
                entry(CELL_HANDOVER_SUCCESS_RATE, null),
                entry(E_RAB_RETAINABILITY_PERCENTAGE_LOST, null),
                entry(E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1, null),
                entry(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR, null),
                entry(INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT, null));
    }

    @Test
    public void whenCellsExist_andCellHourlyKpiValuesAreInDb_thenKpisAreReturned() throws SQLException {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellsWithPopulatedKpis = objectUnderTestKpiCellFlmExternalDao
                .getCellHourlyKpis("2020-12-02T01:00:00.0", "2020-12-03T01:00:00.0", CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES);
        assertThat(cellsWithPopulatedKpis).isNotNull();
        final Map<String, Object> kpis = cellsWithPopulatedKpis
                .get((new CellIdentifier(Integer.valueOf(OSS_ID), FDN_ONE))).get("2020-12-02 01:00:00.0");
        assertThat(kpis).contains(
                entry(CONNECTED_USERS, 1.9D),
                entry(DISTANCE_Q1, 94.92D),
                entry(DISTANCE_Q2, 189.84D),
                entry(DISTANCE_Q3, 284.75D),
                entry(DISTANCE_Q4, 379.68D),
                entry(UE_PERCENTAGE_Q1, 18.51D),
                entry(UE_PERCENTAGE_Q2, 44.44D),
                entry(UE_PERCENTAGE_Q3, 33.33D),
                entry(UE_PERCENTAGE_Q4, 3.7D));
    }

    @Test
    public void whenCellsExist_andAllCellHourlyKpiValuesAreNull_thenNullIsReturned() throws SQLException {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellsWithNullKpis = objectUnderTestKpiCellFlmExternalDao
                .getCellHourlyKpis("2020-12-04T02:00:00.0", "2020-12-05T02:00:00.0", CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES);
        assertThat(cellsWithNullKpis).isNotNull();
        final Map<String, Object> kpis = cellsWithNullKpis
                .get(new CellIdentifier(Integer.valueOf(OSS_ID), FDN_TWO)).get("2020-12-04 02:00:00.0");

        assertThat(kpis).contains(
                entry(CONNECTED_USERS, null),
                entry(DISTANCE_Q1, null),
                entry(DISTANCE_Q2, null),
                entry(DISTANCE_Q3, null),
                entry(DISTANCE_Q4, null),
                entry(UE_PERCENTAGE_Q1, null),
                entry(UE_PERCENTAGE_Q2, null),
                entry(UE_PERCENTAGE_Q3, null),
                entry(UE_PERCENTAGE_Q4, null));
    }

    @Test
    public void whenCellsExist_andSomeCellHourlyKpiValuesAreNull_thenNullIsReturnedForNullKpis() throws SQLException {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellsWithPopulatedKpis = objectUnderTestKpiCellFlmExternalDao
                .getCellHourlyKpis("2020-12-06T03:00:00.0", "2020-12-07T03:00:00.0", CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES);
        assertThat(cellsWithPopulatedKpis).isNotNull();
        final Map<String, Object> kpis = cellsWithPopulatedKpis
                .get(new CellIdentifier(Integer.valueOf(OSS_ID), FDN_THREE)).get("2020-12-06 03:00:00.0");

        assertThat(kpis).contains(
                entry(CONNECTED_USERS, 9.9),
                entry(DISTANCE_Q1, 114.36),
                entry(DISTANCE_Q2, null),
                entry(DISTANCE_Q3, 343.08),
                entry(DISTANCE_Q4, 457.44),
                entry(UE_PERCENTAGE_Q1, 57.22),
                entry(UE_PERCENTAGE_Q2, 23.88),
                entry(UE_PERCENTAGE_Q3, null),
                entry(UE_PERCENTAGE_Q4, 5.55));
    }

    @Test
    public void whenCellsExist_andCellHourlyKpiValuesAreInDbForMultipleExecutions_thenKpisAreOnlyReturnedForExecutionDayProvided()
            throws SQLException {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellsWithPopulatedKpis = objectUnderTestKpiCellFlmExternalDao
                .getCellHourlyKpis("2020-12-02T01:00:00.0", "2020-12-03T01:00:00.0", CONNECTED_USERS_AND_UE_TA_DIST_KPI_NAMES);
        assertThat(cellsWithPopulatedKpis).hasSize(1);
    }

    @Test
    public void whenCellsExist_andVisibleCellSectorKpiValueIsInDb_thenKpiIsReturned() throws SQLException {
        final List<String> cellSectorKpiNames = new ArrayList<>(
                Arrays.asList(LOWER_THRESHOLD_FOR_TRANSIENT, UPPER_THRESHOLD_FOR_TRANSIENT));
        final String executionId = "FLM_1614122399044-155";
        final String localTimestamp = "2021-02-22 00:00:00.0";

        final Map<CellKpi, Map<String, Object>> cellsWithPopulatedKpi = objectUnderTestKpiCellFlmExternalDao
                .getCellSectorDailyFlmKpis(executionId, cellSectorKpiNames);
        assertThat(cellsWithPopulatedKpi).isNotNull();
        final Map<String, Object> fdnOnekpis = cellsWithPopulatedKpi.get(new CellKpi(FDN_ONE, OSS_ID, localTimestamp));
        final Map<String, Object> fdnTwokpis = cellsWithPopulatedKpi.get(new CellKpi(FDN_TWO, OSS_ID, localTimestamp));

        assertThat(fdnOnekpis).contains(
                entry(LOWER_THRESHOLD_FOR_TRANSIENT, Double.valueOf(23.0)),
                entry(UPPER_THRESHOLD_FOR_TRANSIENT, Double.valueOf(34.5))
        );

        assertThat(fdnTwokpis).contains(
                entry(LOWER_THRESHOLD_FOR_TRANSIENT, null),
                entry(UPPER_THRESHOLD_FOR_TRANSIENT, null)
        );
    }

    @Test
    public void whenCellsExist_andNoKpiForTimeWindow_thenEmptyMapIsReturned() throws SQLException {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cells = objectUnderTestKpiCellFlmExternalDao
                .getHourlyKpisForGivenCells("2021-05-05T23:00:00", "2021-05-06T00:00:00", HOURLY_KPIS_SUBSET_NAMES,
                        Collections.singletonList(CELL_ID));

        assertThat(cells).isEmpty();
    }

    @Test
    public void whenThreeCellsExist_andKpisForTimeWindowExisted_andTwoCellsRequested_thenKpisAreReturnedForTwoRequestedCells() throws SQLException {
        final CellIdentifier CELL_ID_TWO = new CellIdentifier(OSS_ID, FDN_TWO);

        final Map<CellIdentifier, Map<String, Map<String, Object>>> cells = objectUnderTestKpiCellFlmExternalDao
                .getHourlyKpisForGivenCells("2021-05-05T23:00:00.0", "2021-05-06T01:00:00.0",
                        HOURLY_KPIS_SUBSET_NAMES, Arrays.asList(CELL_ID, CELL_ID_TWO));

        assertThat(cells)
                .containsOnlyKeys(CELL_ID, CELL_ID_TWO);

        assertThat(cells.get(CELL_ID).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(CONNECTED_USERS, 1.9),
                        entry(DISTANCE_Q1, 94.92),
                        entry(DISTANCE_Q2, 189.84));

        assertThat(cells.get(CELL_ID_TWO).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(CONNECTED_USERS, null),
                        entry(DISTANCE_Q1, null),
                        entry(DISTANCE_Q2, null));
    }

    @Test
    public void whenThreeCellsExist_andKpisForMultipleHoursOfTimeWindowExisted_andTwoCellsRequested_thenKpisAreReturnedForTwoRequestedCells()
            throws SQLException {
        final CellIdentifier CELL_ID_TWO = new CellIdentifier(OSS_ID, FDN_TWO);

        final Map<CellIdentifier, Map<String, Map<String, Object>>> cells = objectUnderTestKpiCellFlmExternalDao
                .getHourlyKpisForGivenCells("2021-05-05T23:00:00.0", "2021-05-06T02:00:00.0",
                        HOURLY_KPIS_SUBSET_NAMES, Arrays.asList(CELL_ID, CELL_ID_TWO));

        assertThat(cells)
                .containsOnlyKeys(CELL_ID, CELL_ID_TWO);

        assertThat(cells.get(CELL_ID).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(CONNECTED_USERS, 1.9),
                        entry(DISTANCE_Q1, 94.92),
                        entry(DISTANCE_Q2, 189.84));

        assertThat(cells.get(CELL_ID).get("2021-05-06 01:00:00.0"))
                .containsOnly(
                        entry(CONNECTED_USERS, null),
                        entry(DISTANCE_Q1, null),
                        entry(DISTANCE_Q2, 162.93));

        assertThat(cells.get(CELL_ID_TWO).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(CONNECTED_USERS, null),
                        entry(DISTANCE_Q1, null),
                        entry(DISTANCE_Q2, null));

        assertThat(cells.get(CELL_ID_TWO).get("2021-05-06 01:00:00.0"))
                .containsOnly(
                        entry(CONNECTED_USERS, 3.7),
                        entry(DISTANCE_Q1, 103.34),
                        entry(DISTANCE_Q2, null));
    }

    @Test
    public void whenCellsExist_forFlmExecution_andNoKpiForTimeWindow_thenEmptyMapIsReturned() throws SQLException {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cells = objectUnderTestKpiCellFlmExternalDao
                .getKpisForGivenCellsPerFlmExecution(EXECUTION_ID_FOUR, "2021-05-05T23:00:00", "2021-05-06T00:00:00", ON_DEMAND_KPIS_SUBSET_NAMES,
                        Collections.singletonList(CELL_ID));

        assertThat(cells).isEmpty();
    }

    @Test
    public void whenThreeCellsExist_forFlmExecution_andKpisForTimeWindowExisted_andTwoCellsRequested_thenKpisAreReturnedForTwoRequestedCells()
            throws SQLException {
        final CellIdentifier CELL_ID_TWO = new CellIdentifier(OSS_ID, FDN_TWO);

        final Map<CellIdentifier, Map<String, Map<String, Object>>> cells = objectUnderTestKpiCellFlmExternalDao
                .getKpisForGivenCellsPerFlmExecution(EXECUTION_ID_FOUR, "2021-05-05T23:00:00.0", "2021-05-06T01:00:00.0",
                        ON_DEMAND_KPIS_SUBSET_NAMES, Arrays.asList(CELL_ID, CELL_ID_TWO));

        assertThat(cells)
                .containsOnlyKeys(CELL_ID, CELL_ID_TWO);

        assertThat(cells.get(CELL_ID).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(UNHAPPY_USERS, 2),
                        entry(GOAL_FUNCTION_RESOURCE_EFFICIENCY, 3));

        assertThat(cells.get(CELL_ID_TWO).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(UNHAPPY_USERS, 4),
                        entry(GOAL_FUNCTION_RESOURCE_EFFICIENCY, 4));
    }

    @Test
    public void whenThreeCellsExist_forFlmExecution_andKpisForMultipleHoursOfTimeWindowExisted_andTwoCellsRequested_thenKpisAreReturnedForTwoRequestedCells()
            throws SQLException {
        final CellIdentifier CELL_ID_TWO = new CellIdentifier(OSS_ID, FDN_TWO);

        final Map<CellIdentifier, Map<String, Map<String, Object>>> cells = objectUnderTestKpiCellFlmExternalDao
                .getKpisForGivenCellsPerFlmExecution(EXECUTION_ID_FOUR, "2021-05-05T23:00:00.0", "2021-05-06T02:00:00.0",
                        ON_DEMAND_KPIS_SUBSET_NAMES, Arrays.asList(CELL_ID, CELL_ID_TWO));

        assertThat(cells)
                .containsOnlyKeys(CELL_ID, CELL_ID_TWO);

        assertThat(cells.get(CELL_ID).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(UNHAPPY_USERS, 2),
                        entry(GOAL_FUNCTION_RESOURCE_EFFICIENCY, 3));

        assertThat(cells.get(CELL_ID).get("2021-05-06 01:00:00.0"))
                .containsOnly(
                        entry(UNHAPPY_USERS, 3),
                        entry(GOAL_FUNCTION_RESOURCE_EFFICIENCY, 6));

        assertThat(cells.get(CELL_ID_TWO).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(UNHAPPY_USERS, 4),
                        entry(GOAL_FUNCTION_RESOURCE_EFFICIENCY, 4));

        assertThat(cells.get(CELL_ID_TWO).get("2021-05-06 01:00:00.0"))
                .containsOnly(
                        entry(UNHAPPY_USERS, null),
                        entry(GOAL_FUNCTION_RESOURCE_EFFICIENCY, 5));
    }

    public Map<Integer, Set<String>> buildOssIdToFdnMap() {
        final Map<Integer, Set<String>> ossIdToFdn = new HashMap<>(1);
        final Set<String> fdns = new HashSet<>();
        fdns.add(FDN_ONE);
        ossIdToFdn.put(OSS_ID, fdns);
        return ossIdToFdn;
    }
}

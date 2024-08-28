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

package com.ericsson.oss.services.sonom.flm.database;

import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.CELL_AVAILABILITY;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellSectorFlmDbConstants.MAX_CONNECTED_USERS_DAILY;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellSectorFlmDbConstants.NUM_VALUES_USED_FOR_MCU_CDF_CALCULATION_DAILY;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellSectorFlmDbConstants.TARGET_CELL_CAPACITY;
import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.KPI_CELL_GUID_1440_KPIS_TABLE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands;
import com.ericsson.oss.services.sonom.flm.database.kpi.KpiServiceUnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;

/**
 * Unit tests for {@link KpiCellFlmDaoImpl} class.
 */
public class KpiCellFlmDaoImplTest {

    private static final int INVALID_VALUE = -1;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final String KPI_CELL_GUID_60_TABLE = "kpi_cell_guid_60";
    private static final String KPI_CELL_SECTOR_FLM_1440_TABLE = "kpi_cell_sector_flm_1440";
    private static final String KPI_CELL_GUID_60_CMDS = "kpi_cell_guid_60.cmds";
    private static final String KPI_CELL_GUID_1440_KPIS = "kpi_cell_guid_1440.cmds";
    private static final String KPI_CELL_SECTOR_FLM_1440_CMDS = "kpi_cell_sector_flm_1440.cmds";

    private static final String PM_IDLE_MODE_REL_DISTR_HIGH_LOAD = "pm_idle_mode_rel_distr_high_load";
    private static final String PM_IDLE_MODE_REL_DISTR_MEDIUM_HIGH_LOAD = "pm_idle_mode_rel_distr_medium_high_load";
    private static final String PM_IDLE_MODE_REL_DISTR_MEDIUM_LOAD = "pm_idle_mode_rel_distr_medium_load";
    private static final String PM_IDLE_MODE_REL_DISTR_LOW_MEDIUM_LOAD = "pm_idle_mode_rel_distr_low_medium_load";
    private static final String PM_IDLE_MODE_REL_DISTR_LOW_LOAD = "pm_idle_mode_rel_distr_low_load";
    private static final String SUBSCRIPTION_RATIO = "subscription_ratio";
    private static final String CONNECTED_USERS = "connected_users";
    private static final List<String> hourlyKpiNames = new ArrayList<>(
            Arrays.asList(PM_IDLE_MODE_REL_DISTR_HIGH_LOAD, PM_IDLE_MODE_REL_DISTR_MEDIUM_HIGH_LOAD,
                    PM_IDLE_MODE_REL_DISTR_MEDIUM_LOAD, PM_IDLE_MODE_REL_DISTR_LOW_MEDIUM_LOAD, PM_IDLE_MODE_REL_DISTR_LOW_MEDIUM_LOAD,
                    PM_IDLE_MODE_REL_DISTR_LOW_LOAD, SUBSCRIPTION_RATIO, CONNECTED_USERS));

    private static final List<String> dailyKpiNames = new ArrayList<>(Arrays.asList(CELL_AVAILABILITY));

    private static final String FDN_ONE = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=654875_9_2";
    private static final String FDN_TWO = "SubNetwork=MKT_054,MeContext=654875_NODEHOST_RSF,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=654875_9_3";
    private static final int OSS_ID = 1;
    private static final String LOCAL_TIMESTAMP_ONE = "2020-12-02 01:00:00.0";
    private static final String LOCAL_TIMESTAMP_TWO = "2020-12-02 02:00:00.0";
    private static final String LOCAL_TIMESTAMP_THREE = "2020-12-02 03:00:00.0";
    private static final String LOCAL_TIMESTAMP_FOUR = "2020-12-02 04:00:00.0";

    private static final CellKpi CELL_KPI_TS_ONE_FDN_ONE = new CellKpi(FDN_ONE, OSS_ID, LOCAL_TIMESTAMP_ONE);
    private static final CellKpi CELL_KPI_TS_TWO_FDN_ONE = new CellKpi(FDN_ONE, OSS_ID, LOCAL_TIMESTAMP_TWO);
    private static final CellKpi CELL_KPI_TS_THREE_FDN_ONE = new CellKpi(FDN_ONE, OSS_ID, LOCAL_TIMESTAMP_THREE);
    private static final CellKpi CELL_KPI_TS_FOUR_FDN_ONE = new CellKpi(FDN_ONE, OSS_ID, LOCAL_TIMESTAMP_FOUR);
    private static final CellKpi CELL_KPI_TS_THREE_FDN_TWO = new CellKpi(FDN_TWO, OSS_ID, LOCAL_TIMESTAMP_THREE);
    private static final CellKpi CELL_KPI_TS_FOUR_FDN_TWO = new CellKpi(FDN_TWO, OSS_ID, LOCAL_TIMESTAMP_FOUR);
    private static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new KpiServiceUnitTestDatabaseRunner();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private final KpiCellFlmDaoImpl objectUnderTestKpiCellDao = new KpiCellFlmDaoImpl(MAX_RETRY_ATTEMPTS,
            WAIT_PERIOD_IN_SECONDS);

    @Before
    public void setUp() throws IOException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(KPI_CELL_GUID_60_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createKpiCellGuid60Table());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_CELL_GUID_60_CMDS));

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(KPI_CELL_GUID_1440_KPIS_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createKpiCellGuid1440KpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.addPrimaryKeyKpiCellGuid1440KpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_CELL_GUID_1440_KPIS));

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(KPI_CELL_SECTOR_FLM_1440_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createKpiCellSectorFlm1440Table());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_CELL_SECTOR_FLM_1440_CMDS));
    }

    @Test
    public void whenFilteredByLocalTimeStampAndOneCellKpisReturned_thenKpisAreReturnedForThatTime()
            throws SQLException {
        final String localDateTime = "2020-12-02 01:00:00.0";
        final Map<CellKpi, CellKpis> cellsWithPopulatedKpis = objectUnderTestKpiCellDao.getNotVisibleCellHourlyKpis(
                hourlyKpiNames, localDateTime);
        softly.assertThat(cellsWithPopulatedKpis).isNotNull();

        softly.assertThat(cellsWithPopulatedKpis.get(CELL_KPI_TS_TWO_FDN_ONE)).isNull();
        softly.assertThat(cellsWithPopulatedKpis.get(CELL_KPI_TS_THREE_FDN_ONE)).isNull();
        softly.assertThat(cellsWithPopulatedKpis.get(CELL_KPI_TS_FOUR_FDN_ONE)).isNull();
        softly.assertThat(cellsWithPopulatedKpis.get(CELL_KPI_TS_THREE_FDN_TWO)).isNull();
        softly.assertThat(cellsWithPopulatedKpis.get(CELL_KPI_TS_FOUR_FDN_TWO)).isNull();

        final CellKpis kpis = cellsWithPopulatedKpis.get(CELL_KPI_TS_ONE_FDN_ONE);

        softly.assertThat(kpis.getPmIdleModeRelDistrHighLoad()).isEqualTo(125);
        softly.assertThat(kpis.getPmIdleModeRelDistrMediumHighLoad()).isEqualTo(47);
        softly.assertThat(kpis.getPmIdleModeRelDistrMediumLoad()).isEqualTo(35);
        softly.assertThat(kpis.getPmIdleModeRelDistrLowMediumLoad()).isEqualTo(66);
        softly.assertThat(kpis.getPmIdleModeRelDistrLowLoad()).isEqualTo(33);
        softly.assertThat(kpis.getSubscriptionRatio()).isEqualTo(1.5);
        softly.assertThat(kpis.getConnectedUsers()).isEqualTo(670.67);
    }

    @Test
    public void whenFilteredByLocalTimeStampAndTwoCellKpisReturned_thenKpisAreReturnedForThatTimeAndCells()
            throws SQLException {
        final String localDateTime = "2020-12-02 03:00:00.0";
        final Map<CellKpi, CellKpis> cellsWithPopulatedKpis = objectUnderTestKpiCellDao.getNotVisibleCellHourlyKpis(
                hourlyKpiNames, localDateTime);
        softly.assertThat(cellsWithPopulatedKpis).isNotNull();

        softly.assertThat(cellsWithPopulatedKpis.get(CELL_KPI_TS_ONE_FDN_ONE)).isNull();
        softly.assertThat(cellsWithPopulatedKpis.get(CELL_KPI_TS_TWO_FDN_ONE)).isNull();
        softly.assertThat(cellsWithPopulatedKpis.get(CELL_KPI_TS_FOUR_FDN_ONE)).isNull();
        softly.assertThat(cellsWithPopulatedKpis.get(CELL_KPI_TS_FOUR_FDN_TWO)).isNull();

        CellKpis kpis = cellsWithPopulatedKpis.get(CELL_KPI_TS_THREE_FDN_ONE);

        softly.assertThat(kpis.getPmIdleModeRelDistrHighLoad()).isEqualTo(25);
        softly.assertThat(kpis.getPmIdleModeRelDistrMediumHighLoad()).isEqualTo(68);
        softly.assertThat(kpis.getPmIdleModeRelDistrMediumLoad()).isEqualTo(29);
        softly.assertThat(kpis.getPmIdleModeRelDistrLowMediumLoad()).isEqualTo(98);
        softly.assertThat(kpis.getPmIdleModeRelDistrLowLoad()).isEqualTo(85);
        softly.assertThat(kpis.getSubscriptionRatio()).isEqualTo(0.74);
        softly.assertThat(kpis.getConnectedUsers()).isEqualTo(INVALID_VALUE);

        kpis = cellsWithPopulatedKpis.get(CELL_KPI_TS_THREE_FDN_TWO);

        softly.assertThat(kpis.getPmIdleModeRelDistrHighLoad()).isEqualTo(79);
        softly.assertThat(kpis.getPmIdleModeRelDistrMediumHighLoad()).isEqualTo(154);
        softly.assertThat(kpis.getPmIdleModeRelDistrMediumLoad()).isEqualTo(32);
        softly.assertThat(kpis.getPmIdleModeRelDistrLowMediumLoad()).isEqualTo(41);
        softly.assertThat(kpis.getPmIdleModeRelDistrLowLoad()).isEqualTo(87);
        softly.assertThat(kpis.getSubscriptionRatio()).isEqualTo(0.7);
        softly.assertThat(kpis.getConnectedUsers()).isEqualTo(668.9);
    }

    @Test
    public void whenFilteredByLocalTimeStampAndNoCellKpisReturned_thenEmptyListReturned()
            throws SQLException {
        final String localDateTime = "2020-12-02 05:00:00.0";
        final Map<CellKpi, CellKpis> cellsWithPopulatedKpis = objectUnderTestKpiCellDao.getNotVisibleCellHourlyKpis(
                hourlyKpiNames, localDateTime);

        assertThat(cellsWithPopulatedKpis).isEmpty();
    }

    @Test
    public void whenCellsExist_andNotVisibleCellDailyKpiValueIsInDb_thenKpiIsReturned() throws SQLException {
        final Map<CellKpi, Map<String, Object>> cellsWithPopulatedKpi = objectUnderTestKpiCellDao
                .getNotVisibleCellDailyKpis(LOCAL_TIMESTAMP_ONE, dailyKpiNames);
        assertThat(cellsWithPopulatedKpi).isNotNull();
        final Map<String, Object> kpis = cellsWithPopulatedKpi.get(new CellKpi(FDN_ONE, OSS_ID, LOCAL_TIMESTAMP_ONE));
        assertThat(kpis).containsEntry(CELL_AVAILABILITY, 1.0D);
    }

    @Test
    public void whenCellsExist_andNotVisibleCellDailyKpiValueIsNull_thenNullIsReturned()
            throws SQLException {
        final String localTimestamp = "2020-12-02 02:00:00.0";
        final Map<CellKpi, Map<String, Object>> cellsWithNullKpi = objectUnderTestKpiCellDao
                .getNotVisibleCellDailyKpis(localTimestamp, dailyKpiNames);
        assertThat(cellsWithNullKpi).isNotNull();
        final Map<String, Object> kpis = cellsWithNullKpi.get(new CellKpi(FDN_TWO, OSS_ID, localTimestamp));
        assertThat(kpis.get(CELL_AVAILABILITY)).isNull();
    }

    @Test
    public void whenCellsExist_andNotVisibleCellDailyKpiValueIsInDbForMultipleExecutions_thenKpiIsOnlyReturnedForExecutionDayProvided()
            throws SQLException {
        final Map<CellKpi, Map<String, Object>> cellsWithPopulatedKpis = objectUnderTestKpiCellDao
                .getNotVisibleCellDailyKpis(LOCAL_TIMESTAMP_ONE, dailyKpiNames);
        assertThat(cellsWithPopulatedKpis).hasSize(1);
    }

    @Test
    public void whenCellsExist_andNotVisibleCellSectorKpiValueIsInDb_thenKpiIsReturned() throws SQLException {
        final List<String> cellSectorKpiNames = new ArrayList<>(
                Arrays.asList(MAX_CONNECTED_USERS_DAILY, NUM_VALUES_USED_FOR_MCU_CDF_CALCULATION_DAILY, TARGET_CELL_CAPACITY));
        final String executionId = "FLM_1614122399044-155";
        final String localTimestamp = "2021-02-22 00:00:00.0";
        final Map<CellKpi, Map<String, Object>> cellsWithPopulatedKpi = objectUnderTestKpiCellDao
                .getNotVisibleCellSectorDailyFlmKpis(executionId, cellSectorKpiNames);
        assertThat(cellsWithPopulatedKpi).isNotNull();
        final Map<String, Object> kpis = cellsWithPopulatedKpi.get(new CellKpi(FDN_ONE, OSS_ID, localTimestamp));

        assertThat(kpis).hasSize(3)
                .containsEntry(MAX_CONNECTED_USERS_DAILY, 11.404)
                .containsEntry(TARGET_CELL_CAPACITY, 5.804)
                .containsEntry(NUM_VALUES_USED_FOR_MCU_CDF_CALCULATION_DAILY, 6);
    }

    @Test
    public void whenCellsExist_andNotVisibleCellSectorFlmKpiValueIsNull_thenNullIsReturned()
            throws SQLException {
        final List<String> cellSectorKpiNames = new ArrayList<>(
                Arrays.asList(MAX_CONNECTED_USERS_DAILY, NUM_VALUES_USED_FOR_MCU_CDF_CALCULATION_DAILY, TARGET_CELL_CAPACITY));
        final String localTimestamp = "2021-02-22 00:00:00.0";
        final String executionId = "FLM_1614122399044-155";
        final Map<CellKpi, Map<String, Object>> cellsWithNullKpi = objectUnderTestKpiCellDao
                .getNotVisibleCellSectorDailyFlmKpis(executionId, cellSectorKpiNames);
        assertThat(cellsWithNullKpi).isNotNull();
        final Map<String, Object> kpis = cellsWithNullKpi.get(new CellKpi(FDN_TWO, OSS_ID, localTimestamp));

        assertThat(kpis).hasSize(3)
                .containsEntry(MAX_CONNECTED_USERS_DAILY, null)
                .containsEntry(TARGET_CELL_CAPACITY, null)
                .containsEntry(NUM_VALUES_USED_FOR_MCU_CDF_CALCULATION_DAILY, null);
    }
}
/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.database;

import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.AVG_DL_PDCP_THROUGHPUT_SECTOR;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.SECTOR_60_KPIS_TABLE;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.SECTOR_FLM_60_KPIS_TABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands;
import com.ericsson.oss.services.sonom.flm.database.kpi.KpiServiceUnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;

/**
 * Unit tests for {@link KpiSectorExternalDaoImpl} class.
 */
@RunWith(OrderedTestRunner.class)
public class KpiSectorExternalDaoImplTest {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final String SECTOR_1440_KPIS_TABLE = "sector_1440_kpis";
    private static final String SECTOR_1440_BUSY_HOUR = "sector_1440_busy_hour.cmds";
    private static final String SECTOR_60_CMDS = "sector_60_kpis.cmds";
    private static final String SECTOR_FLM_60_CMDS = "sector_flm_60_kpis.cmds";
    private static final List<String> SECTOR_IDS = new ArrayList<>(3);
    private static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new KpiServiceUnitTestDatabaseRunner();
    private static final String EXECUTION_ID = "FLM_Execution1";
    private static final String EXECUTION_DAY = "2020-12-02 00:00:00";
    private static final long KEYS_311 = 311L;
    private static final long KEYS_312 = 312L;
    private final KpiSectorExternalDaoImpl objectUnderTestKpiSectorExternalDao = new KpiSectorExternalDaoImpl(MAX_RETRY_ATTEMPTS,
            WAIT_PERIOD_IN_SECONDS);

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void setUp() throws IOException {
        SECTOR_IDS.add("311");
        SECTOR_IDS.add("312");
        SECTOR_IDS.add("313");
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(SECTOR_1440_KPIS_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createSector1440KpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(SECTOR_1440_BUSY_HOUR));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(SECTOR_60_KPIS_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createSector60KpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(SECTOR_60_CMDS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(SECTOR_FLM_60_KPIS_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createSectorFlm60KpiTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(SECTOR_FLM_60_CMDS));
    }

    @Test
    public void whenSectorsExist_busyHourValueIsPresent_busyHourCanBeRetrieved() throws SQLException {
        final Map<Long, String> idToBusyHour = objectUnderTestKpiSectorExternalDao.getSectorBusyHourForSectorIds(EXECUTION_DAY, SECTOR_IDS);
        verifyBusyHours(idToBusyHour);
    }

    @Test
    public void whenSectorsExist_andSectorsExistThatAreNotInSectorList_onlySectorsInSectorListAreReturned() throws SQLException {
        final Map<Long, String> idToBusyHour = objectUnderTestKpiSectorExternalDao.getSectorBusyHourForSectorIds(EXECUTION_DAY, SECTOR_IDS);
        assertThat(idToBusyHour).hasSize(3);
    }

    @Test
    public void whenSectorsExist_andMoreThanOneDayOfDataIsPresent_onlySectorsOnDayQueriedIsReturned() throws SQLException {
        final Map<Long, String> idToBusyHour = objectUnderTestKpiSectorExternalDao.getSectorBusyHourForSectorIds(EXECUTION_DAY, SECTOR_IDS);
        assertThat(idToBusyHour).hasSize(3);
    }

    @Test
    public void whenSectorsExist_andSectorBusyHourIsNullForSector_theSectorIsNotIncludedInResult() throws SQLException {
        SECTOR_IDS.add("315");
        final Map<Long, String> idToBusyHour = objectUnderTestKpiSectorExternalDao.getSectorBusyHourForSectorIds(EXECUTION_DAY, SECTOR_IDS);
        assertThat(idToBusyHour).hasSize(3);
    }

    @Test
    public void whenSectorsExist_andNoKpiForTimeWindow_thenEmptyMapIsReturned() throws SQLException {
        final Map<Long, Map<String, Map<String, Object>>> cells = objectUnderTestKpiSectorExternalDao
                .getSectorHourlyKpis("2021-05-05T23:00:00", "2021-05-06T00:00:00", Collections.singletonList(AVG_DL_PDCP_THROUGHPUT_SECTOR),
                        Collections.singletonList(KEYS_311));

        assertThat(cells).isEmpty();
    }

    @Test
    public void whenSectorsExist_andKpisForTimeWindowExisted_andTwoSectorsRequested_thenKpisAreReturnedForTwoRequestedSectors() throws SQLException {
        final Map<Long, Map<String, Map<String, Object>>> sectors = objectUnderTestKpiSectorExternalDao
                .getSectorHourlyKpis("2021-05-05T23:00:00", "2021-05-06T01:00:00",
                        Collections.singletonList(AVG_DL_PDCP_THROUGHPUT_SECTOR), Arrays.asList(KEYS_311, KEYS_312));

        assertThat(sectors)
                .containsOnlyKeys(KEYS_311, KEYS_312);

        softly.assertThat(sectors.get(KEYS_311).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR, 94.5));

        softly.assertThat(sectors.get(KEYS_312).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR, 98.4));
    }

    @Test
    public void whenSectorsExist_andKpisForMultipleHoursOfTimeWindowExisted_andTwoSectorsRequested_thenKpisAreReturnedForTwoRequestedSectors()
            throws SQLException {
        final Map<Long, Map<String, Map<String, Object>>> sectors = objectUnderTestKpiSectorExternalDao
                .getSectorHourlyKpis("2021-05-05T23:00:00", "2021-05-06T02:00:00",
                        Collections.singletonList(AVG_DL_PDCP_THROUGHPUT_SECTOR), Arrays.asList(KEYS_311, KEYS_312));

        assertThat(sectors)
                .containsOnlyKeys(KEYS_311, KEYS_312);

        softly.assertThat(sectors.get(KEYS_311).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR, 94.5));

        softly.assertThat(sectors.get(KEYS_311).get("2021-05-06 01:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR, 93.5));

        softly.assertThat(sectors.get(KEYS_312).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR, 98.4));

        softly.assertThat(sectors.get(KEYS_312).get("2021-05-06 01:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR, 87.4));
    }

    @Test
    public void whenSectorsExist_forFlmExecution_andNoKpiForTimeWindow_thenEmptyMapIsReturned() throws SQLException {
        final Map<Long, Map<String, Map<String, Object>>> cells = objectUnderTestKpiSectorExternalDao
                .getSectorHourlyKpisForFlmExecution(EXECUTION_ID, "2021-05-05T23:00:00", "2021-05-06T00:00:00",
                        Collections.singletonList(AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION), Collections.singletonList(KEYS_311));

        assertThat(cells).isEmpty();
    }

    @Test
    public void whenSectorsExist_forFlmExecution_andKpisForTimeWindowExisted_andTwoSectorsRequested_thenKpisAreReturnedForTwoRequestedSectors()
            throws SQLException {
        final Map<Long, Map<String, Map<String, Object>>> sectors = objectUnderTestKpiSectorExternalDao
                .getSectorHourlyKpisForFlmExecution(EXECUTION_ID, "2021-05-05T23:00:00", "2021-05-06T01:00:00",
                        Collections.singletonList(AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION), Arrays.asList(KEYS_311, KEYS_312));

        assertThat(sectors)
                .containsOnlyKeys(KEYS_311, KEYS_312);

        softly.assertThat(sectors.get(KEYS_311).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION, 98.8));

        softly.assertThat(sectors.get(KEYS_312).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION, 96.7));
    }

    @Test
    public void whenSectorsExist_forFlmExecution_andKpisForMultipleHoursOfTimeWindow_andTwoSectorsRequested_thenKpisAreReturnedRequestedSectors()
            throws SQLException {
        final Map<Long, Map<String, Map<String, Object>>> sectors = objectUnderTestKpiSectorExternalDao
                .getSectorHourlyKpisForFlmExecution(EXECUTION_ID, "2021-05-05T23:00:00", "2021-05-06T02:00:00",
                        Collections.singletonList(AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION), Arrays.asList(KEYS_311, KEYS_312));

        assertThat(sectors)
                .containsOnlyKeys(KEYS_311, KEYS_312);

        softly.assertThat(sectors.get(KEYS_311).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION, 98.8));

        softly.assertThat(sectors.get(KEYS_311).get("2021-05-06 01:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION, null));

        softly.assertThat(sectors.get(KEYS_312).get("2021-05-06 00:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION, 96.7));

        softly.assertThat(sectors.get(KEYS_312).get("2021-05-06 01:00:00.0"))
                .containsOnly(
                        entry(AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION, 92.3));
    }

    private void verifyBusyHours(final Map<Long, String> idToBusyHour) {
        assertThat(idToBusyHour).contains(
                entry(KEYS_311, "2020-12-02 01:00:00.0"),
                entry(KEYS_312, "2020-12-02 02:00:00.0"),
                entry(313L, "2020-12-02 03:00:00.0"));
    }

}

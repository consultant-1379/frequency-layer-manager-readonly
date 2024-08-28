/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands;
import com.ericsson.oss.services.sonom.flm.database.kpi.KpiServiceUnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;

/**
 * Unit tests for {@link KpiSectorDaoImpl} class.
 */
public class KpiSectorDaoImplTest {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final int FIVE_DAYS = 5;
    private static final int AMOUNT_TO_SUBTRACT = 5;

    private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.of(2020, Month.MARCH, 2, 0, 0);
    private static final String SECTOR_ID_123 = "123";
    private static final long SECTOR_ID_1234 = 1_234L;

    private static final String KPI_CELL_WITH_FIVE_DAYS_UNAVAILABLE = "kpi_cell_with_five_days_unavailable.cmds";
    private static final String KPI_SECTOR_DATA = "kpi_sector_data.cmds";
    private static final String KPI_CELL_WITH_FIVE_DAYS_AVAILABLE = "kpi_cell_with_five_days_available.cmds";
    private static final String KPI_CELL_WITH_FOUR_DAYS_UNAVAILABLE = "kpi_cell_with_four_days_unavailable.cmds";
    private static final String KPI_CELL_WITH_FIVE_DAYS_NULL_AVAILABILITY = "kpi_cell_with_five_days_null_availability.cmds";
    private static final String KPI_CELL_WITH_FIVE_DAYS_UNAVAILABLE_BUT_MOST_RECENT_DAY_AVAILABLE =
            "kpi_cell_with_five_days_unavailable_but_most_recent_day_available.cmds";
    private static final String FDN1 = "SubNetwork=SON,MeContext=nodeName,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=1";
    private static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new KpiServiceUnitTestDatabaseRunner();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final KpiSectorDaoImpl objectUnderTestKpiSectorDao = new KpiSectorDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);

    @Mock
    private KpiDatabaseAccess databaseAccess;

    @InjectMocks
    private final KpiSectorDaoImpl kpiSectorDaoWithMockedDbAccess = new KpiSectorDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);

    @Before
    public void setUp() throws IOException {
        initMocks(this);

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(KpiSectorDbConstants.KPI_SECTOR_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(KpiSectorDbConstants.KPI_CELL_GUID_1440_TABLE));

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createKpiSectorTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_SECTOR_DATA));

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createKpiCellTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.addPrimaryKeyKpiCellTable());
    }

    @Test
    public void whenSectorRefCellUnavailableForFiveDays_thenSectorIsReturned() throws SQLException, IOException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_CELL_WITH_FIVE_DAYS_UNAVAILABLE));

        final Set<Long> sectorIds = objectUnderTestKpiSectorDao.getKpiSectorIdsWithUnavailableRefCell(CURRENT_DATE_TIME.minus(AMOUNT_TO_SUBTRACT, ChronoUnit.DAYS));

        verifySectorIsReturned(sectorIds);
    }

    @Test
    public void whenSectorRefCellAvailabilityForFiveDays_thenSectorIsNotReturned() throws IOException, SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_CELL_WITH_FIVE_DAYS_AVAILABLE));

        final Set<Long> sectorIds = objectUnderTestKpiSectorDao
                .getKpiSectorIdsWithUnavailableRefCell(CURRENT_DATE_TIME.minus(FIVE_DAYS, ChronoUnit.DAYS));

        verifyNoSectorsReturned(sectorIds);
    }

    @Test
    public void whenSectorRefCellUnavailableForFourDays_thenSectorIsNotReturned() throws IOException, SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_CELL_WITH_FOUR_DAYS_UNAVAILABLE));

        final Set<Long> sectorIds = objectUnderTestKpiSectorDao.getKpiSectorIdsWithUnavailableRefCell(CURRENT_DATE_TIME.minus(AMOUNT_TO_SUBTRACT, ChronoUnit.DAYS));

        verifyNoSectorsReturned(sectorIds);
    }

    @Test
    public void whenSectorRefCellHasNoCellAvailability_thenSectorIsNotReturned() throws SQLException {
        final Set<Long> sectorIds = objectUnderTestKpiSectorDao.getKpiSectorIdsWithUnavailableRefCell(CURRENT_DATE_TIME.minus(AMOUNT_TO_SUBTRACT, ChronoUnit.DAYS));

        verifyNoSectorsReturned(sectorIds);
    }

    @Test
    public void whenSectorRefCellHasNullCellAvailability_thenSectorIsNotReturned() throws IOException, SQLException {
        UNIT_TEST_DATABASE_RUNNER
                .executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_CELL_WITH_FIVE_DAYS_NULL_AVAILABILITY));

        final Set<Long> sectorIds = objectUnderTestKpiSectorDao.getKpiSectorIdsWithUnavailableRefCell(CURRENT_DATE_TIME.minus(AMOUNT_TO_SUBTRACT, ChronoUnit.DAYS));

        verifyNoSectorsReturned(sectorIds);
    }

    @Test
    public void whenSectorIsRemovedButRefCellUnavailableForFiveDays_thenSectorIsNotReturned() throws IOException, SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.deleteTableData(KpiSectorDbConstants.KPI_SECTOR_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_CELL_WITH_FIVE_DAYS_UNAVAILABLE));

        final Set<Long> sectorIds = objectUnderTestKpiSectorDao.getKpiSectorIdsWithUnavailableRefCell(CURRENT_DATE_TIME.minus(AMOUNT_TO_SUBTRACT, ChronoUnit.DAYS));

        verifyNoSectorsReturned(sectorIds);
    }

    @Test
    public void whenSectorRefCellUnavailableForFiveDaysButMostRecentDayCellIsAvailable_thenSectorIsNotReturned() throws IOException, SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                KpiDbCommands.getSqlCommandsFromResourceFile(KPI_CELL_WITH_FIVE_DAYS_UNAVAILABLE_BUT_MOST_RECENT_DAY_AVAILABLE));

        final Set<Long> sectorIds = objectUnderTestKpiSectorDao.getKpiSectorIdsWithUnavailableRefCell(CURRENT_DATE_TIME.minus(AMOUNT_TO_SUBTRACT, ChronoUnit.DAYS));

        verifyNoSectorsReturned(sectorIds);
    }

    @Test
    public void whenRetrievingSectorIds_thenOnlyNonNullReferenceCellSectorsAreReturned() throws IOException, SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createKpiSectorTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.deleteTableData(KpiSectorDbConstants.KPI_SECTOR_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_SECTOR_DATA));

        final Set<Long> sectorIds = objectUnderTestKpiSectorDao.getKpiSectorIdsWithRefCell();

        verifySectorIsReturned(sectorIds);
    }

    @Test
    public void whenRetrievingSectorIdsFromEmptyTable_thenNonNullObjectReturned() throws IOException, SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createKpiSectorTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.deleteTableData(KpiSectorDbConstants.KPI_SECTOR_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_SECTOR_DATA));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.deleteTableData(KpiSectorDbConstants.KPI_SECTOR_TABLE));

        final Set<Long> sectorIds = objectUnderTestKpiSectorDao.getKpiSectorIdsWithRefCell();

        verifyNoSectorsReturned(sectorIds);
    }

    @Test
    public void whenRetrievingSectorIdsAndRefCell_thenMapIsPopulatedWithDataFromDB() throws IOException, SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.createKpiSectorTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.deleteTableData(KpiSectorDbConstants.KPI_SECTOR_TABLE));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.getSqlCommandsFromResourceFile(KPI_SECTOR_DATA));

        final Map<Long, String> sectorIdsAndRefCells = objectUnderTestKpiSectorDao.getSectorIdsAndRefCell();

        assertThat(sectorIdsAndRefCells)
                .containsExactly(entry(123L, FDN1));
    }

    @Test
    public void whenRetrievingSectorIdsAndRefCell_ExceptionOccurred_AndRetryCallsMadeToDatabaseTwoTimes_thenMapReturnedInSecondCall()
            throws SQLException {
        final Map<Long, String> sectorIdsMap = new HashMap<>();
        sectorIdsMap.put(SECTOR_ID_1234, FDN1);
        when(databaseAccess.executeQuery(anyString(), any(), any(PreparedStatementHandler.class))).thenThrow(SQLException.class)
                .thenReturn(sectorIdsMap);

        final Map<Long, String> sectorIdsAndRefCells = kpiSectorDaoWithMockedDbAccess.getSectorIdsAndRefCell();

        assertThat(sectorIdsAndRefCells)
                .containsOnly(entry(SECTOR_ID_1234, FDN1));
        verify(databaseAccess, times(2)).executeQuery(anyString(), any(), any(PreparedStatementHandler.class));
    }

    @Test
    public void whenRetrievingSectorIds_ThenSectorIdsReturned_AndNoRepeatCallsMadeToDatabase() throws SQLException {
        when(databaseAccess.executeQuery(anyString(), any(), any(PreparedStatementHandler.class))).thenReturn(Collections.singleton(SECTOR_ID_1234));
        final Set<Long> sectorIds = kpiSectorDaoWithMockedDbAccess.getKpiSectorIdsWithRefCell();
        assertThat(sectorIds).containsExactly(SECTOR_ID_1234);
        verify(databaseAccess, times(1)).executeQuery(anyString(), any(), any(PreparedStatementHandler.class));
    }

    @Test
    public void whenRetrievingSectorIds_ExceptionOccurred_AndRetryCallsMadeToDatabaseTwoTimes_thenSetReturnedInSecondCall() throws SQLException {
        final long id = 12_345L;
        when(databaseAccess.executeQuery(anyString(), any(), any(PreparedStatementHandler.class))).thenThrow(SQLException.class)
                .thenReturn(Collections.singleton(id));
        final Set<Long> sectorIds = kpiSectorDaoWithMockedDbAccess.getKpiSectorIdsWithRefCell();
        assertThat(sectorIds).containsExactly(id);
        verify(databaseAccess, times(2)).executeQuery(anyString(), any(), any(PreparedStatementHandler.class));
    }

    @Test
    public void whenRetrievingSectorIds_ExceptionOccurred_AndRetryCallsMadeToDatabaseThreeTimes_thenExceptionIsThrown() throws SQLException {
        when(databaseAccess.executeQuery(anyString(), any(), any(PreparedStatementHandler.class))).thenThrow(SQLException.class);
        try {
            kpiSectorDaoWithMockedDbAccess.getKpiSectorIdsWithRefCell();
            shouldHaveThrown(SQLException.class);
        } catch (final SQLException e) {
            verify(databaseAccess, times(3)).executeQuery(anyString(), any(), any(PreparedStatementHandler.class));
        }
    }

    @Test
    public void whenTheSectorTableIsNotAvailable_thenExceptionIsThrown() throws SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(KpiDbCommands.dropTable(KpiSectorDbConstants.KPI_SECTOR_TABLE));
        thrown.expect(SQLException.class);
        objectUnderTestKpiSectorDao.getKpiSectorIdsWithRefCell();
    }

    private static void verifySectorIsReturned(final Set<Long> sectorIds) {
        assertThat(sectorIds).containsExactly(Long.valueOf(SECTOR_ID_123));
    }

    private static void verifyNoSectorsReturned(final Set<Long> sectorIds) {
        assertThat(sectorIds).isEmpty();
    }

}

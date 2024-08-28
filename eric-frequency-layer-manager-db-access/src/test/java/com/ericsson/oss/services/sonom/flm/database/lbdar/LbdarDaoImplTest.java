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
package com.ericsson.oss.services.sonom.flm.database.lbdar;

import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.LEAKAGE_CELLS;
import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.SECTOR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbCommands;
import com.ericsson.oss.services.sonom.flm.database.flm.FlmServiceUnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;
import com.google.gson.Gson;

/**
 * Unit test for {@link LbdarDaoImpl} class.
 */
public class LbdarDaoImplTest {
    private static final Gson GSON = new Gson();

    private static final String EXECUTION_ID_123 = "FLM_1630195894-123";
    private static final String EXECUTION_ID_WITH_NO_LEAKAGE = "FLM_1630195894-124";
    private static final long SECTOR_ID_WITH_MULTIPLE_LEAKAGE_CELLS = 12345L;
    private static final long SECTOR_ID_WITH_SINGLE_LEAKAGE_CELL = 23456L;
    private static final long SECTOR_ID_WITH_EMPTY_LEAKAGE_CELLS = 34567L;
    private static final long SECTOR_ID_NOT_IN_TABLE = 45678L;

    private static final List<LeakageCell> MULTIPLE_LEAKAGE_CELLS = new ArrayList<>();
    private static final List<LeakageCell> SINGLE_LEAKAGE_CELL = new ArrayList<>();
    private static final List<LeakageCell> EMPTY_LEAKAGE_CELLS = new ArrayList<>();

    static {
        MULTIPLE_LEAKAGE_CELLS.add(new LeakageCell(
                "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1," +
                        "EUtranCellFDD=LTE02ERBS00003-1",
                1));
        MULTIPLE_LEAKAGE_CELLS.add(new LeakageCell(
                "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1," +
                        "EUtranCellFDD=LTE02ERBS00003-2",
                1));
        SINGLE_LEAKAGE_CELL.add(new LeakageCell(
                "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1," +
                        "EUtranCellFDD=LTE02ERBS00003-1",
                1));
    }

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final LbdarDao LBDAR_DAO_H2_DB = new LbdarDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);

    private static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new FlmServiceUnitTestDatabaseRunner();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @InjectMocks
    public final LbdarDao objectUnderTest = new LbdarDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);

    @Mock
    private FlmDatabaseAccess databaseAccessMock;

    private Set<LeakageCell> result;

    @Before
    public void setUp() {
        initMocks(this);

        dropAndCreateTables();
        insertTestData();
    }

    @Test
    public void whenInsertLeakageCells_thenUpdateCountReturned() throws SQLException {
        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class)))
                .thenReturn(1);

        final int nrOfRecordsInserted = objectUnderTest.insertLeakageCells(EXECUTION_ID_123, SECTOR_ID_WITH_MULTIPLE_LEAKAGE_CELLS,
                new HashSet<>(MULTIPLE_LEAKAGE_CELLS));
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        verify(databaseAccessMock, times(1)).executeUpdate(anyString(), any(Object[].class));
    }

    @Test
    public void whenInsertLeakageCellsWithNull_thenEmptyJsonListIsStoredAndUpdateCountReturned() throws SQLException {
        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class)))
                .thenReturn(1);

        final int nrOfRecordsInserted = objectUnderTest.insertLeakageCells(EXECUTION_ID_123, SECTOR_ID_WITH_MULTIPLE_LEAKAGE_CELLS,
                null);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        verify(databaseAccessMock, times(1)).executeUpdate(anyString(), any(Object[].class));
    }

    @Test
    public void whenFailToInsertLeakageCells_thenSQLExceptionThrown() throws SQLException {
        when(databaseAccessMock.executeUpdate(
                anyString(), any(Object[].class)))
                .thenThrow(new SQLException());
        thrown.expect(SQLException.class);
        objectUnderTest.insertLeakageCells(EXECUTION_ID_123, SECTOR_ID_WITH_MULTIPLE_LEAKAGE_CELLS,
                new HashSet<>(MULTIPLE_LEAKAGE_CELLS));
    }

    @Test
    public void whenReadingLeakageCellsWhereMultipleLeakageCellsExists_thenCorrectLeakageCellsAreReturned() throws SQLException {
        result = LBDAR_DAO_H2_DB.getLeakageCells(EXECUTION_ID_123, SECTOR_ID_WITH_MULTIPLE_LEAKAGE_CELLS);
        assertThat(result).containsExactlyElementsOf(MULTIPLE_LEAKAGE_CELLS);
    }

    @Test
    public void whenReadingLeakageCellsWhereOneLeakageCellsExists_thenCorrectLeakageCellIsReturned() throws SQLException {
        result = LBDAR_DAO_H2_DB.getLeakageCells(EXECUTION_ID_123, SECTOR_ID_WITH_SINGLE_LEAKAGE_CELL);
        assertThat(result).containsExactlyElementsOf(SINGLE_LEAKAGE_CELL);
    }

    @Test
    public void whenReadingLeakageCellsWhereNoLeakageCellsExists_thenAnEmptySetIsReturned() throws SQLException {
        result = LBDAR_DAO_H2_DB.getLeakageCells(EXECUTION_ID_123, SECTOR_ID_WITH_EMPTY_LEAKAGE_CELLS);
        assertThat(result).isEmpty();
    }

    @Test
    public void whenReadingLeakageCellsWhereSectorDoesNotExist_thenAnEmptySetIsReturned() throws SQLException {
        result = LBDAR_DAO_H2_DB.getLeakageCells(EXECUTION_ID_123, SECTOR_ID_NOT_IN_TABLE);
        assertThat(result).isEmpty();
    }

    @Test
    public void whenReadingLeakageCellsWhereExecutionDoesNotExist_thenAnEmptySetIsReturned() throws SQLException {
        result = LBDAR_DAO_H2_DB.getLeakageCells(EXECUTION_ID_WITH_NO_LEAKAGE, SECTOR_ID_NOT_IN_TABLE);
        assertThat(result).isEmpty();
    }

    @Test
    public void whenTablesCannotBeRead_thenExceptionIsThrown() throws SQLException {
        dropTables();
        thrown.expect(SQLException.class);
        LBDAR_DAO_H2_DB.getLeakageCells(EXECUTION_ID_123, 12345L);
    }

    private void dropAndCreateTables() {
        dropTables();
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(LbdarDbCommands.createLbdarTable());
    }

    private void dropTables() {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.dropTable(LbdarDbConstants.FLM_LBDAR));
    }

    private void insertTestData() {
        final List<String> insertQueries = new ArrayList<>();
        insertQueries.add(buildLbdarEntry(SECTOR_ID_WITH_MULTIPLE_LEAKAGE_CELLS, MULTIPLE_LEAKAGE_CELLS));
        insertQueries.add(buildLbdarEntry(SECTOR_ID_WITH_SINGLE_LEAKAGE_CELL, SINGLE_LEAKAGE_CELL));
        insertQueries.add(buildLbdarEntry(SECTOR_ID_WITH_EMPTY_LEAKAGE_CELLS, EMPTY_LEAKAGE_CELLS));

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(insertQueries);
    }

    private String buildLbdarEntry(final Long sector, final List<LeakageCell> leakageCells) {
        return String.format("INSERT INTO public.flm_lbdar (%s, %s, %s) " +
                        "VALUES ('%s', '%s', '%s')",
                EXECUTION_ID,
                SECTOR_ID,
                LEAKAGE_CELLS,
                EXECUTION_ID_123,
                sector,
                GSON.toJson(leakageCells));
    }

}
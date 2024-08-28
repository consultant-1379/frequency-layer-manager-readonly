/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.settings.history;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.CELL_SETTINGS_HISTORY;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.CLEAN_CELL_SETTINGS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsHistoryDao;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * Unit tests for {@link CellSettingsHistoryProcessor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellSettingsHistoryProcessorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    Execution executionMock;

    @Mock
    ExecutionDao executionDaoMock;

    @Mock
    CellSettingsHistoryDao cellSettingsHistoryDaoMock;

    @InjectMocks
    CellSettingsHistoryProcessor objectUnderTest = new CellSettingsHistoryProcessor(executionMock, executionDaoMock, cellSettingsHistoryDaoMock);

    @Mock
    FlmDatabaseAccess databaseAccessMock;

    @Mock
    Connection connectionMock;

    @Mock
    PreparedStatement preparedStatementMock;

    @Mock
    PersistenceHandler persistenceHandlerMock;

    private static final String EXECUTION_ID_TEST = "execution_id_test";

    @Before
    public void init() throws FlmAlgorithmException, SQLException {
        initMocks(this);
        when(databaseAccessMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
        when(persistenceHandlerMock.persistExecutionStatus(any(), anyBoolean())).thenReturn(1L);
        when(cellSettingsHistoryDaoMock.copyCellSettings(any())).thenReturn(1);
    }

    @Test
    public void whenProcessCleanUpRequestCalled_thenStatementIsExecuted_andExecutionStatusPersisted()
            throws FlmAlgorithmException, SQLException {
        objectUnderTest.processCleanUpRequest(CLEAN_CELL_SETTINGS, true, 1, "FLM_4645745");

        final InOrder inOrder = inOrder(preparedStatementMock, persistenceHandlerMock);
        inOrder.verify(preparedStatementMock, times(1)).execute();
        inOrder.verify(persistenceHandlerMock, times(1)).persistExecutionStatus(any(), anyBoolean());
    }

    @Test
    public void whenProcessCleanUpRequestCalledButDBFails_thenExceptionReturned() throws FlmAlgorithmException, SQLException {
        when(preparedStatementMock.execute()).thenThrow(SQLException.class);

        thrown.expect(SQLException.class);
        objectUnderTest.processCleanUpRequest(CLEAN_CELL_SETTINGS, true, 1, "FLM_4645745");
    }

    @Test
    public void whenInsertCellSettingsCalled_thenCopyCellSettings_andExecutionStatusPersisted()
            throws FlmAlgorithmException, SQLException {

        objectUnderTest.insertCellSettingsIntoHistoricalTable(CELL_SETTINGS_HISTORY, EXECUTION_ID_TEST, true);

        final InOrder inOrder = inOrder(cellSettingsHistoryDaoMock, persistenceHandlerMock);
        inOrder.verify(cellSettingsHistoryDaoMock, times(1)).copyCellSettings(eq(EXECUTION_ID_TEST));
        inOrder.verify(persistenceHandlerMock, times(1)).persistExecutionStatus(eq(CELL_SETTINGS_HISTORY), eq(true));
    }
}
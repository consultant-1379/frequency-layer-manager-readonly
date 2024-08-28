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
package com.ericsson.oss.services.sonom.flm.database.settings;

import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getAllColumnNamesForCellConfigHistory;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getAllColumnNamesForInsertionSelectForCellConfigHistory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

/**
 * Unit tests for {@link CellSettingsHistoryDaoImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellSettingsHistoryDaoImplTest {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final int ONCE = 1;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    CellSettingsHistoryDaoImpl objectUnderTest = new CellSettingsHistoryDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);

    @Mock
    FlmDatabaseAccess databaseAccessMock;

    @Mock
    Connection connectionMock;

    @Mock
    PreparedStatement preparedStatementMock;

    private static final String EXECUTION_ID = "ID1";

    @Before
    public void init() throws SQLException {
        initMocks(this);
        when(databaseAccessMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
    }

    @Test
    public void whenCopyCellSettingsCalled_thenCopiedRowsReturned() throws SQLException {
        final InOrder inOrder = inOrder(preparedStatementMock);
        when(preparedStatementMock.getUpdateCount()).thenReturn(1);

        final int result = objectUnderTest.copyCellSettings(EXECUTION_ID);

        assertThat(result).isEqualTo(1);
        assertThat(getAllColumnNamesForCellConfigHistory()).isNotNull();
        assertThat(getAllColumnNamesForInsertionSelectForCellConfigHistory()).isNotNull();
        inOrder.verify(preparedStatementMock, times(ONCE)).setString(anyInt(), anyString());
        inOrder.verify(preparedStatementMock, times(ONCE)).execute();
        inOrder.verify(preparedStatementMock, times(ONCE)).getUpdateCount();
    }

    @Test
    public void whenCopyCellSettingsCalledButDBFails_thenExceptionReturned() throws SQLException {
        when(preparedStatementMock.execute()).thenThrow(SQLException.class);
        thrown.expect(SQLException.class);
        objectUnderTest.copyCellSettings(EXECUTION_ID);
    }

    @Test
    public void whenCopyCellSettingsCalledButDBFailsTwice_thenResultReturnedOnThirdRetry() throws SQLException {
        final InOrder inOrder = inOrder(preparedStatementMock);
        when(preparedStatementMock.execute())
                .thenThrow(SQLException.class)
                .thenThrow(SQLException.class)
                .thenReturn(true);
        when(preparedStatementMock.getUpdateCount()).thenReturn(1);

        final int result = objectUnderTest.copyCellSettings(EXECUTION_ID);

        assertThat(result).isEqualTo(1);
        assertThat(getAllColumnNamesForCellConfigHistory()).isNotNull();
        assertThat(getAllColumnNamesForInsertionSelectForCellConfigHistory()).isNotNull();
        inOrder.verify(preparedStatementMock, times(ONCE)).setString(anyInt(), anyString());
        inOrder.verify(preparedStatementMock, times(ONCE)).execute();
        inOrder.verify(preparedStatementMock, times(ONCE)).getUpdateCount();
    }
}
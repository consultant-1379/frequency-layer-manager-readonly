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

package com.ericsson.oss.services.sonom.flm.database.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.flm.FlmServiceUnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.handlers.ResultHandler;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.pa.handlers.PAOutputEventHandler;
import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.utils.PAPolicyOutputEventCompare;
import com.ericsson.oss.services.sonom.flm.database.utils.PaPolicyOutputEventBuilder;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;
import com.google.gson.JsonSyntaxException;

/**
 * Unit test for {@link PAOutputEventDaoImpl} class.
 */
@RunWith(OrderedTestRunner.class)
public class PAOutputEventDaoImplTest {

    public static final String EXECUTION_ID = "1";
    private static final String FLM_PA_EXECUTION_ID = "FLM_PA_execution_one";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final String FLM_EXECUTION_ID = "FLM_1479249799770-162";
    private static final PAOutputEventDao PA_EXECUTION_DAO_WITH_H2_DB = new PAOutputEventDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);
    private static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new FlmServiceUnitTestDatabaseRunner();
    private static final String SECTOR_ID = "101";

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    private FlmDatabaseAccess databaseAccessMock;

    @Mock
    PAOutputEventHandler paOutputEventHandler;

    @InjectMocks
    private final PAOutputEventDao paExecutionDaoWithMockedDbAccess = new PAOutputEventDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);

    @Before
    public void setUp() {
        initMocks(this);

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.dropTable(PAExecutionDbConstants.PA_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.createPaExecutionTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.dropTable(PAExecutionDbConstants.PA_OUTPUT_EVENTS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.createPaOutputEventsTable());
    }

    @Test
    public void whenInsertingPaPolicyOutputEvent_thenPaExecutionIdReturned() throws SQLException {
        final PaPolicyOutputEvent paPolicyOutputEvent = PaPolicyOutputEventBuilder.buildPaPolicyOutputEvent(FLM_EXECUTION_ID, FLM_PA_EXECUTION_ID,
                SECTOR_ID);

        // must use mock because h2 doesn't support return arg for insert queries
        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class)))
                .thenReturn(paPolicyOutputEvent.getPaExecutionId());

        assertThat(paExecutionDaoWithMockedDbAccess.insertPaPolicyOutputEvent(paPolicyOutputEvent)).isEqualTo(paPolicyOutputEvent.getPaExecutionId());
    }

    @Test
    public void whenFailToInsertPaPolicyOutputEvent_thenSQLExceptionThrown() throws SQLException {
        final PaPolicyOutputEvent paPolicyOutputEvent = PaPolicyOutputEventBuilder.buildPaPolicyOutputEvent(FLM_EXECUTION_ID, FLM_PA_EXECUTION_ID,
                SECTOR_ID);

        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class)))
                .thenThrow(new SQLException());

        thrown.expect(SQLException.class);

        paExecutionDaoWithMockedDbAccess.insertPaPolicyOutputEvent(paPolicyOutputEvent);
    }

    @Test
    public void whenGettingPaPolicyOutputEvent_thenPaPolicyOutputEventIsReturned() throws SQLException {
        final PaPolicyOutputEvent paPolicyOutputEvent = PaPolicyOutputEventBuilder.buildPaPolicyOutputEvent(FLM_EXECUTION_ID, FLM_PA_EXECUTION_ID,
                SECTOR_ID);

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.insertPaPolicyOutputEvent(Collections.singletonList(paPolicyOutputEvent)));

        final PaPolicyOutputEvent returnedEvent = PA_EXECUTION_DAO_WITH_H2_DB.getPaPolicyOutputEventById(FLM_PA_EXECUTION_ID).get(0);
        assertThat(PAPolicyOutputEventCompare.testEquals(paPolicyOutputEvent, returnedEvent)).isTrue();
    }

    @Test
    public void whenFailToGetPaPolicyOutputEvent_thenSQLExceptionThrown() throws SQLException {
        when(databaseAccessMock.executeQuery(anyString(), any(ResultHandler.class)))
                .thenThrow(new SQLException());
        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class)))
                .thenThrow(new SQLException());

        thrown.expect(SQLException.class);

        paExecutionDaoWithMockedDbAccess.getPaPolicyOutputEventById(FLM_PA_EXECUTION_ID);
    }

    @Test
    public void whenDegradationStatusIsMalformed_thenSQLExceptionThrown() throws SQLException {
        when(paOutputEventHandler.populate(any(ResultSet.class)))
                .thenThrow(new JsonSyntaxException("Degradation Status Malformed"));

        thrown.expect(SQLException.class);

        paExecutionDaoWithMockedDbAccess.getPaPolicyOutputEventById(FLM_PA_EXECUTION_ID);
    }
}

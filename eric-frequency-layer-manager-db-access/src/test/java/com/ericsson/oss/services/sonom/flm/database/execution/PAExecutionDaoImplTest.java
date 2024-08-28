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

import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.FAILED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.SCHEDULED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.STARTED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.SUCCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.database.pa.handlers.PAExecutionHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

/**
 * Unit test for {@link PAExecutionDaoImpl} class.
 */
@RunWith(OrderedTestRunner.class)
public class PAExecutionDaoImplTest {

    public static final String EXECUTION_ID = "1";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final String FLM_EXECUTION_ID = "FLM_1479249799770-162";
    private static final PAExecutionDao PA_EXECUTION_DAO_WITH_H2_DB = new PAExecutionDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);
    private static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new FlmServiceUnitTestDatabaseRunner();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    private FlmDatabaseAccess databaseAccessMock;

    @InjectMocks
    private final PAExecutionDao paExecutionDaoWithMockedDbAccess = new PAExecutionDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);

    // All tests below here use mock DatabaseAccess object until next comment says otherwise.

    @Before
    public void setUp() {
        initMocks(this);

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.dropTable(PAExecutionDbConstants.PA_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.createPaExecutionTable());
    }

    @Test
    public void whenInsertingPaExecution_thenPaExecutionIdReturned() throws SQLException {
        final PAExecution paExecution = new PAExecutionBuilder().paWindow(1).state(SCHEDULED).flmExecutionId(FLM_EXECUTION_ID).build();

        // must use mock because h2 doesn't support return arg for insert queries
        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class))).thenReturn(paExecution.getId());

        assertThat(paExecutionDaoWithMockedDbAccess.insert(paExecution)).isEqualTo(paExecution.getId());
    }

    @Test
    public void whenFailToInsertPaExecution_thenSQLExceptionThrown() throws SQLException {
        final PAExecution paExecution = new PAExecutionBuilder().state(SCHEDULED).build();

        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class)))
                .thenThrow(new SQLException());

        thrown.expect(SQLException.class);

        paExecutionDaoWithMockedDbAccess.insert(paExecution);
    }

    @Test
    public void whenFailToInsertPaExecutionTwoTimes_thenPaExecutionIdReturnedOnThirdRetry() throws SQLException {
        final PAExecution paExecution = new PAExecutionBuilder().state(SCHEDULED).build();

        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(paExecution.getId());

        assertThat(paExecutionDaoWithMockedDbAccess.insert(paExecution)).isEqualTo(paExecution.getId());
    }

    @Test
    public void whenFailToInsertPaExecutionFourTimes_thenSqlExceptionIsThrownOnThirdRetry() throws SQLException {
        final PAExecution paExecution = new PAExecutionBuilder().state(SCHEDULED).build();

        thrown.expect(SQLException.class);

        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenThrow(new SQLException());

        paExecutionDaoWithMockedDbAccess.insert(paExecution);
    }

    @Test
    public void whenFailUpdatingPaExecutionTwoTimes_thenUpdatedRowCountReturnedOnThirdRetry() throws SQLException {
        final PAExecution paExecution = new PAExecutionBuilder().state(SCHEDULED).build();

        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(1);

        assertThat(paExecutionDaoWithMockedDbAccess.update(paExecution)).isEqualTo(1);
    }

    @Test
    public void whenFailGettingPaExecutionsByFlmExecutionIdTwoTimes_thenPaExecutionsAreReturnedOnThirdRetry() throws SQLException {
        final List<PAExecution> paExecutions = new PAExecutionScheduleBuilder().build();

        when(databaseAccessMock.executeQuery(anyString(), any(PAExecutionHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(paExecutions);

        assertThat(paExecutionDaoWithMockedDbAccess.getPAExecutions(FLM_EXECUTION_ID)).isEqualTo(paExecutions);
    }

    @Test
    public void whenFailGettingPAExecutionsByPAExecutionStatesTwoTimes_thenPAExecutionsAreReturnedOnThirdRetry() throws SQLException {
        final List<PAExecution> paExecutions = Arrays.asList(new PAExecutionBuilder().paWindow(1).flmExecutionId(FLM_EXECUTION_ID).state(STARTED).build(),
                new PAExecutionBuilder().paWindow(2).flmExecutionId(FLM_EXECUTION_ID).state(STARTED).build());

        when(databaseAccessMock.executeQuery(anyString(), any(PAExecutionHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(paExecutions);

        final Map<String, List<PAExecution>> flmExecutionIdToPAExecutions = paExecutionDaoWithMockedDbAccess.getPAExecutionsInStates(STARTED, SCHEDULED);

        flmExecutionIdToPAExecutions.get(FLM_EXECUTION_ID).forEach(paExecution -> assertThat(paExecution.getState())
                .isIn(STARTED, SCHEDULED));
    }

    @Test
    public void whenFailGettingPAExecutionsByPAExecutionStatesThreeTimes_thenSQLExceptionAreReturnedOnThirdRetry() throws SQLException {
        when(databaseAccessMock.executeQuery(anyString(), any(PAExecutionHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenThrow(new SQLException());

        thrown.expect(SQLException.class);

        paExecutionDaoWithMockedDbAccess.getPAExecutionsInStates(any(PAExecutionState.class));
    }

    // All tests below here use real DatabaseAccess and H2DB.
    @Test
    public void whenUpdatingPaExecution_thenUpdatedRowCountReturned() throws SQLException {
        final PAExecution testExecution = new PAExecutionBuilder().state(SCHEDULED).build();

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.insertExecution(Collections.singletonList(testExecution)));
        testExecution.setState(STARTED);

        assertThat(PA_EXECUTION_DAO_WITH_H2_DB.update(testExecution)).isEqualTo(1);
    }

    @Test
    public void whenUpdatingPaExecution_thenCorrectNumberOfInputEventsSentReturned() throws SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.deleteAllEntriesFromTable(PAExecutionDbConstants.PA_EXECUTIONS));

        final PAExecution testExecution = new PAExecutionBuilder().state(STARTED).build();
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.insertExecution(Collections.singletonList(testExecution)));

        testExecution.setNumPaPolicyInputEventsSent(150);

        assertThat(PA_EXECUTION_DAO_WITH_H2_DB.update(testExecution)).isEqualTo(1);

        final List<PAExecution> actualExecutions = PA_EXECUTION_DAO_WITH_H2_DB.getPAExecutions(FLM_EXECUTION_ID);

        assertThat(actualExecutions).containsExactly(testExecution);
    }

    @Test
    public void whenUpdatingPaExecutionThatDoesntExist_thenZeroRecordsUpdated() throws SQLException {
        final PAExecution testExecution = new PAExecutionBuilder().state(SCHEDULED).build();

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.deleteAllEntriesFromTable(PAExecutionDbConstants.PA_EXECUTIONS));

        assertThat(PA_EXECUTION_DAO_WITH_H2_DB.update(testExecution)).isZero();
    }

    @Test
    public void whenFailUpdatingPaExecutionThatDoesntExist_thenSQLExceptionThrown() throws SQLException {
        thrown.expect(SQLException.class);
        PA_EXECUTION_DAO_WITH_H2_DB.update(null);
    }

    @Test
    public void whenGettingPaExecutionByFlmExecutionId_thenCorrectPaExecutionsAreReturned() throws SQLException {
        final List<PAExecution> expectedExecutions = new PAExecutionScheduleBuilder().build();

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.deleteAllEntriesFromTable(PAExecutionDbConstants.PA_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.insertExecution(expectedExecutions));

        final List<PAExecution> actualExecutions = PA_EXECUTION_DAO_WITH_H2_DB.getPAExecutions(FLM_EXECUTION_ID);

        assertThat(actualExecutions).isEqualTo(expectedExecutions);
    }

    @Test
    public void whenGettingPaExecutionByFlmExecutionIdThatDoesntExist_thenEmptyListIsReturned() throws SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.deleteAllEntriesFromTable(PAExecutionDbConstants.PA_EXECUTIONS));

        final List<PAExecution> paExecutions = PA_EXECUTION_DAO_WITH_H2_DB.getPAExecutions(FLM_EXECUTION_ID);

        assertThat(paExecutions).isEmpty();
    }

    @Test
    public void whenGettingPAExecutionsByPAExecutionStates_andStatesExist_thenCorrectPAExecutionsAreReturned() throws SQLException {
        final List<PAExecution> paExecutions = new ArrayList<>();
        paExecutions.add(new PAExecutionBuilder().paWindow(1).state(STARTED).flmExecutionId(FLM_EXECUTION_ID).build());
        paExecutions.add(new PAExecutionBuilder().paWindow(2).state(SCHEDULED).flmExecutionId(FLM_EXECUTION_ID).build());
        paExecutions.add(new PAExecutionBuilder().paWindow(3).state(SCHEDULED).flmExecutionId(FLM_EXECUTION_ID).build());

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.insertExecution(paExecutions));

        final Map<String, List<PAExecution>> flmExecutionIdToPAExecutions = PA_EXECUTION_DAO_WITH_H2_DB.getPAExecutionsInStates(STARTED, SCHEDULED);

        assertThat(flmExecutionIdToPAExecutions).hasSize(1);
        assertThat(flmExecutionIdToPAExecutions.get(FLM_EXECUTION_ID)).hasSize(3);
        flmExecutionIdToPAExecutions.get(FLM_EXECUTION_ID).forEach(paExecution -> assertThat(paExecution.getState())
                .isIn(STARTED, SCHEDULED));
    }

    @Test
    public void whenGettingPAExecutionsByPAExecutionStates_andStatesDoNotExist_thenEmptyListOfPAExecutionsAreReturned() throws SQLException {
        final List<PAExecution> paExecutions = new ArrayList<>();
        paExecutions.add(new PAExecutionBuilder().paWindow(1).state(FAILED).flmExecutionId(FLM_EXECUTION_ID).build());
        paExecutions.add(new PAExecutionBuilder().paWindow(2).state(FAILED).flmExecutionId(FLM_EXECUTION_ID).build());
        paExecutions.add(new PAExecutionBuilder().paWindow(3).state(FAILED).flmExecutionId(FLM_EXECUTION_ID).build());

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.insertExecution(paExecutions));

        final Map<String, List<PAExecution>> flmExecutionIdToPAExecutions = PA_EXECUTION_DAO_WITH_H2_DB.getPAExecutionsInStates(STARTED, SCHEDULED);

        assertThat(flmExecutionIdToPAExecutions).hasSize(0);
    }

    @Test
    public void whenGettingPAExecutionsByPAExecutionStates_thenGroupPAExecutionsToFlmExecutionId() throws SQLException {
        final String executionIdA = "FLM_479249799770A";
        final String executionIdB = "FLM_479249799770B";

        final List<PAExecution> paExecutions = new ArrayList<>();
        paExecutions.add(new PAExecutionBuilder().paWindow(1).state(STARTED).flmExecutionId(executionIdA).build());
        paExecutions.add(new PAExecutionBuilder().paWindow(2).state(SCHEDULED).flmExecutionId(executionIdA).build());
        paExecutions.add(new PAExecutionBuilder().paWindow(3).state(FAILED).flmExecutionId(executionIdA).build());

        paExecutions.add(new PAExecutionBuilder().paWindow(1).state(SCHEDULED).flmExecutionId(executionIdB).build());
        paExecutions.add(new PAExecutionBuilder().paWindow(2).state(SUCCEEDED).flmExecutionId(executionIdB).build());
        paExecutions.add(new PAExecutionBuilder().paWindow(3).state(SCHEDULED).flmExecutionId(executionIdB).build());

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(PADbCommands.insertExecution(paExecutions));

        final Map<String, List<PAExecution>> flmExecutionIdToPAExecutions = PA_EXECUTION_DAO_WITH_H2_DB.getPAExecutionsInStates(STARTED, SCHEDULED);

        flmExecutionIdToPAExecutions.entrySet().stream()
                .forEach((flmIdToPaExecution) -> {
                    //getting a list of FlmExecutionId associated with each PAExecution Object
                    final List<String> listOfFlmExecutionIdOfEachPAExecution = flmIdToPaExecution.getValue().stream()
                            .map(PAExecution::getFlmExecutionId).collect(Collectors.toList());
                    //asserting each FlmId in the list to be equal to the KEY of the map
                    listOfFlmExecutionIdOfEachPAExecution.stream().forEach(flmId -> assertThat(flmIdToPaExecution.getKey()).isEqualTo(flmId));
                });

        flmExecutionIdToPAExecutions.entrySet().stream()
                .flatMap(key -> key.getValue().stream())
                .forEach(paExecution -> assertThat(paExecution.getState()).isIn(STARTED, SCHEDULED));
    }

    @Test
    public void testPaExecutionBuilder() {
        final PAExecution paExecution = new PAExecutionBuilder()
                .start(new Timestamp(System.currentTimeMillis()))
                .schedule("0 0 1 ? * * *")
                .paWindow(2)
                .paWindowStartTime(new Timestamp(System.currentTimeMillis()))
                .paWindowEndTime(new Timestamp(System.currentTimeMillis()))
                .state(STARTED)
                .flmExecutionId("FLM_1234")
                .numPaPolicyInputEventsSent(123)
                .build();

        assertThat(paExecution).isNotNull();
    }

    @Test
    public void testPaExecutionScheduleBuilder() {
        final List<PAExecution> paExecutions = new PAExecutionScheduleBuilder().build();

        assertThat(paExecutions).hasSize(3);
    }

    static class PAExecutionBuilder {
        private String schedule = "0 0 2 ? * * *";
        private Integer paWindow = 1;
        private Timestamp paWindowStartTime = new Timestamp(System.currentTimeMillis());
        private Timestamp paWindowEndTime = paWindowStartTime;
        private PAExecutionState state = SCHEDULED;
        private String flmExecutionId = FLM_EXECUTION_ID;
        private Integer numPaPolicyInputEventsSent = 0;

        protected PAExecutionBuilder start(final Timestamp time) {
            paWindowStartTime = time;
            paWindowEndTime = paWindowStartTime;
            return this;
        }

        protected PAExecutionBuilder schedule(final String schedule) {
            this.schedule = schedule;
            return this;
        }

        protected PAExecutionBuilder paWindow(final int paWindow) {
            this.paWindow = paWindow;
            return this;
        }

        protected PAExecutionBuilder paWindowStartTime(final Timestamp paWindowStartTime) {
            this.paWindowStartTime = paWindowStartTime;
            return this;
        }

        protected PAExecutionBuilder paWindowEndTime(final Timestamp paWindowEndTime) {
            this.paWindowEndTime = paWindowEndTime;
            return this;
        }

        protected PAExecutionBuilder state(final PAExecutionState state) {
            this.state = state;
            return this;
        }

        protected PAExecutionBuilder flmExecutionId(final String flmExecutionId) {
            this.flmExecutionId = flmExecutionId;
            return this;
        }

        protected PAExecutionBuilder numPaPolicyInputEventsSent(final Integer numPaPolicyInputEventsSent) {
            this.numPaPolicyInputEventsSent = numPaPolicyInputEventsSent;
            return this;
        }

        protected PAExecution build() {
            final PAExecution paExecution = new PAExecution(
                    paWindow,
                    schedule,
                    paWindowStartTime,
                    paWindowEndTime,
                    flmExecutionId);
            paExecution.setState(state);
            return paExecution;
        }
    }

    static class PAExecutionScheduleBuilder {
        private final Timestamp time = new Timestamp(System.currentTimeMillis());

        protected List<PAExecution> build() {
            return Arrays.asList(
                    new PAExecutionBuilder().paWindow(1).start(time).state(SUCCEEDED).flmExecutionId(FLM_EXECUTION_ID).build(),
                    new PAExecutionBuilder().paWindow(2).start(time).state(STARTED).flmExecutionId(FLM_EXECUTION_ID).build(),
                    new PAExecutionBuilder().paWindow(3).start(time).state(SCHEDULED).flmExecutionId(FLM_EXECUTION_ID).build());
        }
    }

}

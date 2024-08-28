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

package com.ericsson.oss.services.sonom.flm.database.execution;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.FAILED;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_1;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_2;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SUCCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.flm.FlmServiceUnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.handlers.ExecutionHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.ExecutionSummaryHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.ResultHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Unit test for {@link ExecutionDaoImpl} class.
 */
@RunWith(OrderedTestRunner.class)
public class ExecutionDaoImplTest {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final String EXECUTION_ID = "FLM_1479249799770-162";
    private static final ExecutionDao EXECUTION_DAO_WITH_H2_DB = new ExecutionDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);
    private static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new FlmServiceUnitTestDatabaseRunner();
    private static int createExecutionsCounter;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private final ExecutionDao executionDaoWithMockedDbAccess = new ExecutionDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);
    @Mock
    private FlmDatabaseAccess databaseAccessMock;

    // All tests below here use mock DatabaseAccess object until next comment says otherwise.

    @Before
    public void setUp() {
        initMocks(this);

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.dropTable(ExecutionDbConstants.FLM_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.createExecutionTable());
    }

    @Test
    public void whenInsertingExecution_thenIdReturned() throws SQLException {
        final Execution execution = new ExecutionBuilder().state(KPI_PROCESSING_GROUP_1).build().get(0);

        // must use mock because h2 doesn't support return arg for insert queries
        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class)))
                .thenReturn(EXECUTION_ID);

        assertThat(executionDaoWithMockedDbAccess.insert(execution)).isEqualTo(EXECUTION_ID);
    }

    @Test
    public void whenFailToInsertExecution_thenSQLExceptionThrown() throws SQLException {
        final Execution execution = new ExecutionBuilder().state(KPI_PROCESSING_GROUP_1).build().get(0);

        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class)))
                .thenThrow(new SQLException());
        thrown.expect(SQLException.class);
        executionDaoWithMockedDbAccess.insert(execution);
    }

    @Test
    public void whenFailToInsertExecutionTwoTimes_thenExecutionReturnedOnThirdRetry() throws SQLException {
        final Execution execution = new ExecutionBuilder().state(KPI_PROCESSING_GROUP_1).build().get(0);

        when(databaseAccessMock.executeInsert(anyString(), any(ResultHandler.class), any(Object[].class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(EXECUTION_ID);

        assertThat(executionDaoWithMockedDbAccess.insert(execution)).isEqualTo(EXECUTION_ID);
    }

    @Test
    public void whenFailUpdatingExecutionTwoTimes_thenUpdatedRowCountReturnedOnThirdRetry() throws SQLException {
        final Execution testExecution = new ExecutionBuilder().state(KPI_PROCESSING_GROUP_1).build().get(0);

        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(1);
        assertThat(executionDaoWithMockedDbAccess.update(testExecution)).isEqualTo(1);
    }

    @Test
    public void whenFailGettingExecutionByIdTwoTimes_thenExecutionReturnedOnThirdRetry() throws SQLException {
        final List<Execution> testExecutions = new ExecutionBuilder().build();
        when(databaseAccessMock.executeQuery(anyString(), any(ExecutionHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(testExecutions);
        assertThat(executionDaoWithMockedDbAccess.get(EXECUTION_ID)).isEqualTo(testExecutions.get(0));
    }

    @Test
    public void whenFailGettingAllExecutions_thenSQLExceptionThrown() throws SQLException {
        when(databaseAccessMock.executeQuery(anyString(), any(ExecutionSummaryHandler.class)))
                .thenThrow(new SQLException());

        thrown.expect(SQLException.class);
        executionDaoWithMockedDbAccess.getAllSummaries();
    }

    @Test
    public void whenFailGettingAllExecutionsTwoTimes_thenExecutionsReturnedOnThirdRetry() throws SQLException {
        final List<ExecutionSummary> testExecutions = new ExecutionSummaryBuilder().build();
        when(databaseAccessMock.executeQuery(anyString(), any(ExecutionSummaryHandler.class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(testExecutions);
        assertThat(executionDaoWithMockedDbAccess.getAllSummaries()).isEqualTo(testExecutions);
    }

    @Test
    public void whenFailGettingAllExecutionsInStates_thenSQLExceptionThrown() throws SQLException {
        when(databaseAccessMock.executeQuery(anyString(), any(ExecutionHandler.class)))
                .thenThrow(new SQLException());
        thrown.expect(SQLException.class);
        executionDaoWithMockedDbAccess.getExecutionsInStates(KPI_PROCESSING_GROUP_1);
    }

    @Test
    public void whenFailGettingAllExecutionsInStatesTwoTimes_thenExecutionsReturnedOnThirdRetry() throws SQLException {
        final List<Execution> testExecutions = new ExecutionBuilder().state(KPI_PROCESSING_GROUP_1).build();
        when(databaseAccessMock.executeQuery(anyString(), any(ExecutionHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(testExecutions);
        assertThat(executionDaoWithMockedDbAccess.getExecutionsInStates(KPI_PROCESSING_GROUP_1)).isEqualTo(testExecutions);
    }

    // All tests below here use real DatabaseAccess and H2DB.
    @Test
    public void whenFailUpdatingExecutionThatDoesntExist_thenZeroRecordsUpdated() throws SQLException {
        final Execution testExecution = new ExecutionBuilder().state(KPI_PROCESSING_GROUP_1).build().get(0);
        assertThat(EXECUTION_DAO_WITH_H2_DB.update(testExecution)).isZero();
    }

    @Test
    public void whenFailUpdatingExecutionThatDoesntExist_thenSQLExceptionThrown() throws SQLException {
        thrown.expect(SQLException.class);
        EXECUTION_DAO_WITH_H2_DB.update(null);
    }

    @Test
    public void whenUpdatingExecution_thenUpdatedRowCountReturned() throws SQLException {
        final Execution testExecution = new ExecutionBuilder().state(KPI_PROCESSING_GROUP_1).build().get(0);
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.insertExecution(Collections.singletonList(testExecution)));
        testExecution.setId(EXECUTION_ID);

        assertThat(EXECUTION_DAO_WITH_H2_DB.update(testExecution)).isEqualTo(1);
    }

    @Test
    public void whenGettingExecutionById_thenCorrectExecutionReturned() throws SQLException {
        final List<Execution> executionsToCreate = new ExecutionBuilder()
                .state(KPI_PROCESSING_GROUP_1)
                .withFullExecution(true)
                .build();
        final Execution expectedExecution = executionsToCreate.get(0);
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.insertExecution(executionsToCreate));

        final Execution actualExecution = EXECUTION_DAO_WITH_H2_DB.get(EXECUTION_ID);

        assertThat(actualExecution).isEqualTo(expectedExecution);
    }

    @Test
    public void whenGettingExecutionByIdAndItDoesntExist_thenSqlExceptionThrown() throws SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));
        thrown.expect(SQLException.class);
        EXECUTION_DAO_WITH_H2_DB.get(EXECUTION_ID);
    }

    @Test
    public void whenGettingExecutionByIdWithoutRetryAndItDoesntExist_thenNullIsReturned() throws SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));
        final Execution actualExecution = EXECUTION_DAO_WITH_H2_DB.getWithoutRetry(EXECUTION_ID);
        assertThat(actualExecution).isNull();
    }

    @Test
    public void whenGettingAllExecutions_thenAllExecutionsReturned() throws SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER
                .executeSqlCommands(ExecutionDbCommands.insertExecution(new ExecutionBuilder().state(KPI_PROCESSING_GROUP_1).build()));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands.insertExecution(new ExecutionBuilder().executionId("FLM_1479249799771").state(KPI_PROCESSING_GROUP_2).build()));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands.insertExecution(new ExecutionBuilder().executionId("FLM_1479249799772").state(SUCCEEDED).build()));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands.insertExecution(new ExecutionBuilder().executionId("FLM_1479249799773").state(FAILED).build()));

        assertThat(EXECUTION_DAO_WITH_H2_DB.getAllSummaries()).hasSize(4);
    }

    @Test
    public void whenGettingAllExecutionsAndNoneExist_thenEmptyListReturned() throws SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));

        assertThat(EXECUTION_DAO_WITH_H2_DB.getAllSummaries()).isEmpty();
    }

    @Test
    public void whenGettingExecutionsInStates_thenResultsOrderedByConfigurationIdAndStartTime() throws SQLException, InterruptedException {
        final String executionId = "FLM_1479249799770";
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands.insertExecution(create2ExecutionsWithUniqueConfigurationIdAndTimeStamp(KPI_PROCESSING_GROUP_1, executionId)));
        TimeUnit.MILLISECONDS.sleep(5);
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands
                        .insertExecution(create2ExecutionsWithUniqueConfigurationIdAndTimeStamp(KPI_PROCESSING_GROUP_2,
                                executionId + "123")));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands.insertExecution(new ExecutionBuilder().executionId(executionId + "212").state(SUCCEEDED).build()));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands.insertExecution(new ExecutionBuilder().executionId(executionId + "313").state(FAILED).build()));

        final Map<String, Execution> result = new HashMap<>();
        EXECUTION_DAO_WITH_H2_DB.getExecutionsInStates(KPI_PROCESSING_GROUP_1, KPI_PROCESSING_GROUP_2)
                .forEach(execution -> result.put(execution.getId(), execution));

        assertThat(result)
                .as("Unexpected executions in result")
                .hasSize(4);

        final Execution firstExecutionInList = result.get("FLM_14792497997701");
        final Execution secondExecutionInList = result.get("FLM_14792497997701231");

        softly.assertThat(firstExecutionInList.getState()).isEqualTo(KPI_PROCESSING_GROUP_1);
        softly.assertThat(secondExecutionInList.getState()).isEqualTo(KPI_PROCESSING_GROUP_2);
        softly.assertThat(firstExecutionInList.getConfigurationId()).isEqualTo(1);
        softly.assertThat(secondExecutionInList.getConfigurationId()).isEqualTo(1);
        softly.assertThat(secondExecutionInList.getStartTime()).isAfter(firstExecutionInList.getStartTime());

        final Execution thirdExecutionInList = result.get("FLM_14792497997702");
        final Execution fourthExecutionInList = result.get("FLM_14792497997701232");

        softly.assertThat(thirdExecutionInList.getState()).isEqualTo(KPI_PROCESSING_GROUP_1);
        softly.assertThat(fourthExecutionInList.getState()).isEqualTo(KPI_PROCESSING_GROUP_2);
        softly.assertThat(thirdExecutionInList.getConfigurationId()).isEqualTo(2);
        softly.assertThat(fourthExecutionInList.getConfigurationId()).isEqualTo(2);
        softly.assertThat(fourthExecutionInList.getStartTime()).isAfter(thirdExecutionInList.getStartTime());
    }

    @Test
    public void whenGettingExecutionsInSingleState_thenOnlyExecutionsInThatStateReturned() throws SQLException {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.deleteAllEntriesFromTable(ExecutionDbConstants.FLM_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands.insertExecution(create2ExecutionsWithUniqueConfigurationIdAndTimeStamp(KPI_PROCESSING_GROUP_1, EXECUTION_ID)));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands
                        .insertExecution(create2ExecutionsWithUniqueConfigurationIdAndTimeStamp(KPI_PROCESSING_GROUP_2,
                                EXECUTION_ID + "456")));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.insertExecution(new ExecutionBuilder().state(SUCCEEDED).build()));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(
                ExecutionDbCommands.insertExecution(new ExecutionBuilder().executionId(
                        EXECUTION_ID + "123").state(FAILED).build()));

        final List<Execution> result = EXECUTION_DAO_WITH_H2_DB.getExecutionsInStates(KPI_PROCESSING_GROUP_1);
        assertThat(result)
                .as("Unexpected executions in result")
                .hasSize(2);

        result.forEach(execution -> softly.assertThat(execution.getState())
                .as("Unexpected state in result '%s'", execution)
                .isEqualTo(KPI_PROCESSING_GROUP_1));
    }

    private static List<Execution> create2ExecutionsWithUniqueConfigurationIdAndTimeStamp(final ExecutionState state,
            final String executionIdFormat) {
        final int count = 2;
        final List<Execution> executions = new ArrayList<>(count);
        final long fixedExecutionTime = LocalDateTime.parse("2020-10-13T12:10:00")
                .atZone(ZoneId.of("Z")) // use UTC
                .toInstant().toEpochMilli();
        final long CONST_TIME_BUFFER = 10L; // clock step size in loop

        for (int i = 1; i <= count; i++) {
            final long executionTime = fixedExecutionTime + (i + createExecutionsCounter) * CONST_TIME_BUFFER;
            executions.addAll(new ExecutionBuilder().executionId(executionIdFormat + i).state(state)
                    .startTime(new Timestamp(executionTime)).configurationId(i).build());
            createExecutionsCounter++;
        }
        return executions;
    }

    @Test
    public void testExecutionBuilder() {
        final List<Execution> list = new ExecutionBuilder()
                .executionId("lkjhij")
                .startTime(Timestamp.valueOf("2021-03-18 17:16:40.957"))
                .configurationId(111)
                .state(KPI_PROCESSING_GROUP_2)
                .schedule("0 0 0 1 1 ? 2200")
                .retryAttempts(3)
                .calculationId("calcId")
                .additionalExecutionInformation("")
                .numSectorsToEvaluateForOptimization(11112)
                .numOptimizationElementsSent(11111)
                .numOptimizationElementsReceived(232)
                .numOptimizationLbqs(232)
                .numChangesWrittenToCmDb(232)
                .numChangesNotWrittenToCmDb(0)
                .openLoop(false)
                .inclusionList(Collections.emptyList())
                .exclusionList(Collections.emptyList())
                .weekendDays("Monday")
                .enablePA(false)
                .withFullExecution(true)
                .build();
        assertThat(list).hasSize(1);
    }

    static class ExecutionBuilder {
        // default values for a test execution
        private static final long BASE_EXECUTION_TIME = 1_611_751_613_017L;
        private String cronExpression = "0 0 2 ? * * *";
        private int configurationId = 1;
        private String executionId = EXECUTION_ID;
        private int retryAttempts;
        private String calculationId = "CALCULATION_ID_0";
        private ExecutionState executionState = KPI_PROCESSING_GROUP_1;
        private final Map<String, String> customizedGlobalSettings = getSettings("defaultCustomGlobalSettings.json");
        private final Map<String, String> customizedDefaultSettings = getSettings("defaultCustomDefaultSettings.json");
        private final List<CustomizedGroup> groups = Collections.emptyList();
        private String additionalExecutionInformation = "";
        private int numSectorsToEvaluateForOptimization;
        private int numOptimizationElementsSent;
        private int numOptimizationElementsReceived;
        private int numOptimizationLbqs;
        private int numChangesWrittenToCmDb;
        private int numChangesNotWrittenToCmDb;
        private boolean openLoop = true;
        private List<Group> inclusionList = Collections.emptyList();
        private List<Group> exclusionList = Collections.emptyList();
        private String weekendDays = "Saturday,Sunday";
        private boolean enablePA = true;
        private boolean isFullExecution = true;

        private final Timestamp stateModifiedTime = new Timestamp(BASE_EXECUTION_TIME);
        private Timestamp startTime = new Timestamp(BASE_EXECUTION_TIME);

        protected ExecutionBuilder executionId(final String executionId) {
            this.executionId = executionId;
            return this;
        }

        protected ExecutionBuilder startTime(final Timestamp timeStamp) {
            startTime = timeStamp;
            return this;
        }

        protected ExecutionBuilder configurationId(final int configurationId) {
            this.configurationId = configurationId;
            return this;
        }

        protected ExecutionBuilder state(final ExecutionState state) {
            executionState = state;
            return this;
        }

        protected ExecutionBuilder schedule(final String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        protected ExecutionBuilder retryAttempts(final int retryAttempts) {
            this.retryAttempts = retryAttempts;
            return this;
        }

        protected ExecutionBuilder calculationId(final String calculationId) {
            this.calculationId = calculationId;
            return this;
        }

        protected ExecutionBuilder additionalExecutionInformation(final String additionalExecutionInformation) {
            this.additionalExecutionInformation = additionalExecutionInformation;
            return this;
        }

        protected ExecutionBuilder numSectorsToEvaluateForOptimization(final int numSectorsToEvaluateForOptimization) {
            this.numSectorsToEvaluateForOptimization = numSectorsToEvaluateForOptimization;
            return this;
        }

        protected ExecutionBuilder numOptimizationElementsSent(final int numOptimizationElementsSent) {
            this.numOptimizationElementsSent = numOptimizationElementsSent;
            return this;
        }

        protected ExecutionBuilder numOptimizationElementsReceived(final int numOptimizationElementsReceived) {
            this.numOptimizationElementsReceived = numOptimizationElementsReceived;
            return this;
        }

        protected ExecutionBuilder numOptimizationLbqs(final int numOptimizationLbqs) {
            this.numOptimizationLbqs = numOptimizationLbqs;
            return this;
        }

        protected ExecutionBuilder numChangesWrittenToCmDb(final int numChangesWrittenToCmDb) {
            this.numChangesWrittenToCmDb = numChangesWrittenToCmDb;
            return this;
        }

        protected ExecutionBuilder numChangesNotWrittenToCmDb(final int numChangesNotWrittenToCmDb) {
            this.numChangesNotWrittenToCmDb = numChangesNotWrittenToCmDb;
            return this;
        }

        protected ExecutionBuilder openLoop(final boolean openLoop) {
            this.openLoop = openLoop;
            return this;
        }

        protected ExecutionBuilder inclusionList(final List<Group> inclusionList) {
            this.inclusionList = inclusionList;
            return this;
        }

        protected ExecutionBuilder exclusionList(final List<Group> exclusionList) {
            this.exclusionList = exclusionList;
            return this;
        }

        protected ExecutionBuilder weekendDays(final String weekendDays) {
            this.weekendDays = weekendDays;
            return this;
        }

        protected ExecutionBuilder enablePA(final boolean enablePA) {
            this.enablePA = enablePA;
            return this;
        }

        protected ExecutionBuilder withFullExecution(final boolean isFullExecution) {
            this.isFullExecution = isFullExecution;
            return this;
        }

        protected List<Execution> build() {
            final Execution execution = new Execution();
            execution.setId(executionId);
            execution.setStateModifiedTime(stateModifiedTime);
            execution.setStartTime(startTime);
            execution.setConfigurationId(configurationId);
            execution.setState(executionState);
            execution.setSchedule(cronExpression);
            execution.setRetryAttempts(retryAttempts);
            execution.setCalculationId(calculationId);
            execution.setCustomizedGlobalSettings(customizedGlobalSettings);
            execution.setCustomizedDefaultSettings(customizedDefaultSettings);
            execution.setGroups(groups);
            execution.setAdditionalExecutionInformation(additionalExecutionInformation);
            execution.setNumSectorsToEvaluateForOptimization(numSectorsToEvaluateForOptimization);
            execution.setNumOptimizationElementsSent(numOptimizationElementsSent);
            execution.setNumOptimizationElementsReceived(numOptimizationElementsReceived);
            execution.setNumOptimizationLbqs(numOptimizationLbqs);
            execution.setNumChangesWrittenToCmDb(numChangesWrittenToCmDb);
            execution.setNumChangesNotWrittenToCmDb(numChangesNotWrittenToCmDb);
            execution.setOpenLoop(openLoop);
            execution.setInclusionList(inclusionList);
            execution.setExclusionList(exclusionList);
            execution.setWeekendDays(weekendDays);
            execution.setEnablePA(enablePA);
            execution.setFullExecution(isFullExecution);
            return Collections.singletonList(execution);
        }

        private static Map<String, String> getSettings(final String filePath) {
            try {
                final String resource = ResourceLoaderUtils.getClasspathResourceAsString(filePath);
                return new Gson().fromJson(resource, new TypeToken<Map<String, String>>() {
                }.getType());
            } catch (final IOException e) {
                fail(String.format("Test setup failure. Error loading resource through filepath:%s", filePath), e);
                return null;
            }
        }
    }

    static class ExecutionSummaryBuilder {

        // default values for a test execution
        private final long executionTime = System.currentTimeMillis();
        private String cronExpression = "0 0 2 ? * * *";
        private int configurationId = 1;
        private String executionId = EXECUTION_ID;
        private ExecutionState executionState = KPI_PROCESSING_GROUP_1;
        private boolean openLoop = true;

        private Timestamp startTime = new Timestamp(executionTime);
        private Timestamp stateModifiedTime = new Timestamp(executionTime);

        protected ExecutionSummaryBuilder executionId(final String executionId) {
            this.executionId = executionId;
            return this;
        }

        protected ExecutionSummaryBuilder startTime(final Timestamp timeStamp) {
            startTime = timeStamp;
            return this;
        }

        protected ExecutionSummaryBuilder stateModifiedTime(final Timestamp timeStamp) {
            stateModifiedTime = timeStamp;
            return this;
        }

        protected ExecutionSummaryBuilder configurationId(final int configurationId) {
            this.configurationId = configurationId;
            return this;
        }

        protected ExecutionSummaryBuilder state(final ExecutionState state) {
            executionState = state;
            return this;
        }

        protected ExecutionSummaryBuilder schedule(final String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        protected ExecutionSummaryBuilder openLoop(final boolean openLoop) {
            this.openLoop = openLoop;
            return this;
        }

        protected List<ExecutionSummary> build() {
            final ExecutionSummary execution = new ExecutionSummary();
            execution.setId(executionId);
            execution.setStartTime(startTime);
            execution.setStateModifiedTime(stateModifiedTime);
            execution.setConfigurationId(configurationId);
            execution.setState(executionState);
            execution.setSchedule(cronExpression);
            execution.setOpenLoop(openLoop);
            return Collections.singletonList(execution);
        }
    }
}

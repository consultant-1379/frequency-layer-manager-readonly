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

import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.ADDITIONAL_EXECUTION_INFORMATION;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.CALCULATION_ID;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.CONFIGURATION_ID;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.CUSTOMIZED_DEFAULT_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.CUSTOMIZED_GLOBAL_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.ENABLE_PA;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.EXCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.FULL_EXECUTION;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.GROUPS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.ID;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.INCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_CHANGES_NOT_WRITTEN_TO_CM_DB;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_CHANGES_WRITTEN_TO_CM_DB;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_OPTIMIZATION_ELEMENTS_RECEIVED;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_OPTIMIZATION_ELEMENTS_SENT;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_OPTIMIZATION_LBQS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.OPEN_LOOP;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.RETRY_ATTEMPTS;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.SCHEDULE;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.START_TIME;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.STATE;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.STATE_MODIFIED_TIME;
import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.WEEKEND_DAYS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.google.gson.Gson;

/**
 * Class which contains SQL queries for the {@link ExecutionDaoImplTest} unit tests.
 */
public final class ExecutionDbCommands {

    private ExecutionDbCommands() {
        // intentionally private, utility class
    }

    public static List<String> deleteTableData(final String table) {
        return Collections.singletonList(String.format("DELETE FROM %s", table));
    }

    public static List<String> dropTable(final String table) {
        return Collections.singletonList(String.format("DROP TABLE IF EXISTS %s", table));
    }

    public static List<String> deleteAllEntriesFromTable(final String table) {
        return Collections.singletonList(String.format("DELETE FROM %s", table));
    }

    public static List<String> getSqlCommandsFromResourceFile(final String sqlFile) throws IOException {
        final String path = new File("src/test/resources/" + sqlFile).getAbsolutePath();
        return new ArrayList<>(Files.readAllLines(Paths.get(path)));
    }

    public static List<String> createExecutionTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS flm_executions(%n" +
                                "    %s VARCHAR(255) PRIMARY KEY,%n" +
                                "    %s bigint NOT NULL,%n" + // for purposes of test, does not need configuration table reference
                                "    %s TIMESTAMP,%n" +
                                "    %s VARCHAR(50),%n" +
                                "    %s TIMESTAMP,%n" +
                                "    %s VARCHAR(255),%n" +
                                "    %s VARCHAR(100) NOT NULL,%n" +
                                "    %s INTEGER,%n" +
                                "    %s VARCHAR(100),%n" +
                                "    %s TEXT,%n" +
                                "    %s TEXT,%n" +
                                "    %s TEXT,%n" +
                                "    %s INTEGER,%n" +
                                "    %s INTEGER,%n" +
                                "    %s INTEGER,%n" +
                                "    %s INTEGER,%n" +
                                "    %s INTEGER,%n" +
                                "    %s INTEGER,%n" +
                                "    %s BOOLEAN,%n" +
                                "    %s TEXT,%n" +
                                "    %s TEXT,%n" +
                                "    %s TEXT,%n" +
                                "    %s BOOLEAN,%n" +
                                "    %s BOOLEAN%n" +
                                "    );",
                        ID,
                        CONFIGURATION_ID,
                        START_TIME,
                        STATE,
                        STATE_MODIFIED_TIME,
                        ADDITIONAL_EXECUTION_INFORMATION,
                        SCHEDULE,
                        RETRY_ATTEMPTS,
                        CALCULATION_ID,
                        CUSTOMIZED_GLOBAL_SETTINGS,
                        CUSTOMIZED_DEFAULT_SETTINGS,
                        GROUPS,
                        NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION,
                        NUM_OPTIMIZATION_ELEMENTS_SENT,
                        NUM_OPTIMIZATION_ELEMENTS_RECEIVED,
                        NUM_OPTIMIZATION_LBQS,
                        NUM_CHANGES_WRITTEN_TO_CM_DB,
                        NUM_CHANGES_NOT_WRITTEN_TO_CM_DB,
                        OPEN_LOOP,
                        INCLUSION_LIST,
                        EXCLUSION_LIST,
                        WEEKEND_DAYS,
                        ENABLE_PA,
                        FULL_EXECUTION));
    }

    public static List<String> insertExecution(final List<Execution> executions) {
        final Gson gson = new Gson();
        final List<String> insertQueries = new ArrayList<>(executions.size());
        for (final Execution execution : executions) {
            insertQueries.add(String.format(
                    "insert into flm_executions (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) " +
                            "values ('%s','%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                    ID,
                    STATE,
                    STATE_MODIFIED_TIME,
                    ADDITIONAL_EXECUTION_INFORMATION,
                    CONFIGURATION_ID,
                    START_TIME,
                    SCHEDULE,
                    RETRY_ATTEMPTS,
                    CALCULATION_ID,
                    CUSTOMIZED_GLOBAL_SETTINGS,
                    CUSTOMIZED_DEFAULT_SETTINGS,
                    GROUPS,
                    NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION,
                    NUM_OPTIMIZATION_ELEMENTS_SENT,
                    NUM_OPTIMIZATION_ELEMENTS_RECEIVED,
                    NUM_OPTIMIZATION_LBQS,
                    NUM_CHANGES_WRITTEN_TO_CM_DB,
                    NUM_CHANGES_NOT_WRITTEN_TO_CM_DB,
                    OPEN_LOOP,
                    INCLUSION_LIST,
                    EXCLUSION_LIST,
                    WEEKEND_DAYS,
                    ENABLE_PA,
                    FULL_EXECUTION,
                    execution.getId(),
                    execution.getState(),
                    execution.getStateModifiedTime(),
                    execution.getAdditionalExecutionInformation(),
                    execution.getConfigurationId(),
                    execution.getStartTime(),
                    execution.getSchedule(),
                    execution.getRetryAttempts(),
                    execution.getCalculationId(),
                    gson.toJson(execution.getCustomizedGlobalSettings()),
                    gson.toJson(execution.getCustomizedDefaultSettings()),
                    execution.getGroups(),
                    execution.getNumSectorsToEvaluateForOptimization(),
                    execution.getNumOptimizationElementsSent(),
                    execution.getNumOptimizationElementsReceived(),
                    execution.getNumOptimizationLbqs(),
                    execution.getNumChangesWrittenToCmDb(),
                    execution.getNumChangesNotWrittenToCmDb(),
                    execution.isOpenLoop(),
                    execution.getInclusionList(),
                    execution.getExclusionList(),
                    execution.getWeekendDays(),
                    execution.isEnablePA(),
                    execution.isFullExecution()));
        }
        return insertQueries;
    }
}

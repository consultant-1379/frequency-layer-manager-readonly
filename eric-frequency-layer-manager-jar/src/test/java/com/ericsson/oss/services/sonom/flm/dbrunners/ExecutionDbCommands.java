/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.dbrunners;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ericsson.oss.services.sonom.flm.executor.FlmAlgorithmExecutorTest;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.google.gson.Gson;

/**
 * Class which contains SQL queries for the {@link FlmAlgorithmExecutorTest} unit tests.
 */
public final class ExecutionDbCommands {
    private static final Gson GSON = new Gson();

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
            final Map<String, Object> queryMap = new HashMap<>();
            queryMap.put(ID, execution.getId());
            queryMap.put(STATE, execution.getState());
            queryMap.put(STATE_MODIFIED_TIME, execution.getStateModifiedTime());
            queryMap.put(ADDITIONAL_EXECUTION_INFORMATION, execution.getAdditionalExecutionInformation());
            queryMap.put(CONFIGURATION_ID, execution.getConfigurationId());
            queryMap.put(START_TIME, execution.getStartTime());
            queryMap.put(SCHEDULE, execution.getSchedule());
            queryMap.put(RETRY_ATTEMPTS, execution.getRetryAttempts());
            queryMap.put(CALCULATION_ID, execution.getCalculationId());
            queryMap.put(CUSTOMIZED_GLOBAL_SETTINGS, gson.toJson(execution.getCustomizedGlobalSettings()));
            queryMap.put(CUSTOMIZED_DEFAULT_SETTINGS, gson.toJson(execution.getCustomizedDefaultSettings()));
            queryMap.put(GROUPS, gson.toJson(execution.getGroups()));
            queryMap.put(NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION, execution.getNumSectorsToEvaluateForOptimization());
            queryMap.put(NUM_OPTIMIZATION_ELEMENTS_SENT, execution.getNumOptimizationElementsSent());
            queryMap.put(NUM_OPTIMIZATION_ELEMENTS_RECEIVED, execution.getNumOptimizationElementsReceived());
            queryMap.put(NUM_OPTIMIZATION_LBQS, execution.getNumOptimizationLbqs());
            queryMap.put(NUM_CHANGES_WRITTEN_TO_CM_DB, execution.getNumChangesWrittenToCmDb());
            queryMap.put(NUM_CHANGES_NOT_WRITTEN_TO_CM_DB, execution.getNumChangesNotWrittenToCmDb());
            queryMap.put(OPEN_LOOP, execution.isOpenLoop());
            queryMap.put(INCLUSION_LIST, gson.toJson(execution.getInclusionList()));
            queryMap.put(EXCLUSION_LIST, gson.toJson(execution.getExclusionList()));
            queryMap.put(WEEKEND_DAYS, execution.getWeekendDays());
            queryMap.put(ENABLE_PA, execution.isEnablePA());
            queryMap.put(FULL_EXECUTION, execution.isFullExecution());
            String variables = "insert into flm_executions (";
            String operands = ") values (";
            for (final Map.Entry<String, Object> entry : queryMap.entrySet()) {
                if (!Objects.isNull(entry.getValue())) {
                    variables += entry.getKey() + " ,";
                    operands += "'" + entry.getValue() + "'" + " ,";
                }
            }
            insertQueries.add(variables.substring(0, variables.length() - 2) + operands.substring(0, operands.length() - 2) + ")");
        }
        return insertQueries;
    }

    public static String performUpdate(final Execution execution) {
        final Gson gson = new Gson();
        final Map<String, Object> queryMap = new HashMap<>();
        final String query;
        queryMap.put(STATE, execution.getState());
        queryMap.put(STATE_MODIFIED_TIME, execution.getStateModifiedTime());
        queryMap.put(ADDITIONAL_EXECUTION_INFORMATION, execution.getAdditionalExecutionInformation());
        queryMap.put(CONFIGURATION_ID, execution.getConfigurationId());
        queryMap.put(SCHEDULE, execution.getSchedule());
        queryMap.put(RETRY_ATTEMPTS, execution.getRetryAttempts());
        queryMap.put(CALCULATION_ID, execution.getCalculationId());
        queryMap.put(CUSTOMIZED_GLOBAL_SETTINGS, GSON.toJson(execution.getCustomizedGlobalSettings()));
        queryMap.put(CUSTOMIZED_DEFAULT_SETTINGS, GSON.toJson(execution.getCustomizedDefaultSettings()));
        queryMap.put(GROUPS, gson.toJson(execution.getGroups()));
        queryMap.put(NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION, execution.getNumSectorsToEvaluateForOptimization());
        queryMap.put(NUM_OPTIMIZATION_ELEMENTS_SENT, execution.getNumOptimizationElementsSent());
        queryMap.put(NUM_OPTIMIZATION_ELEMENTS_RECEIVED, execution.getNumOptimizationElementsReceived());
        queryMap.put(NUM_OPTIMIZATION_LBQS, execution.getNumOptimizationLbqs());
        queryMap.put(NUM_CHANGES_WRITTEN_TO_CM_DB, execution.getNumChangesWrittenToCmDb());
        queryMap.put(NUM_CHANGES_NOT_WRITTEN_TO_CM_DB, execution.getNumChangesNotWrittenToCmDb());
        queryMap.put(OPEN_LOOP, execution.isOpenLoop());
        queryMap.put(INCLUSION_LIST, gson.toJson(execution.getInclusionList()));
        queryMap.put(EXCLUSION_LIST, gson.toJson(execution.getExclusionList()));
        queryMap.put(WEEKEND_DAYS, execution.getWeekendDays());
        queryMap.put(ENABLE_PA, execution.isEnablePA());
        queryMap.put(FULL_EXECUTION, execution.isFullExecution());
        String variables = "update flm_executions set ";
        for (final Map.Entry<String, Object> entry : queryMap.entrySet()) {
            if (!Objects.isNull(entry.getValue())) {
                variables += entry.getKey() + " = '" + entry.getValue() + "' ,";
            }

        }
        query = variables.substring(0, variables.length() - 2) + "where id = '" + execution.getId() + "' ;";
        return query;
    }
}

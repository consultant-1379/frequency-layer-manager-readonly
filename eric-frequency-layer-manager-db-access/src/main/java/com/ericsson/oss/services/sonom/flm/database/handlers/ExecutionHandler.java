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
package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Creates a {@link List} of execution's represented as {@link Execution}.
 */
public class ExecutionHandler implements ResultHandler<List<Execution>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionHandler.class);
    private static final Gson GSON = new Gson();

    @Override
    public List<Execution> populate(final ResultSet resultSet) throws SQLException {
        final List<Execution> executions = new ArrayList<>();

        while (resultSet.next()) {
            final Execution execution = new Execution();
            execution.setId(resultSet.getString(ExecutionDbConstants.ID));
            execution.setConfigurationId(resultSet.getInt(ExecutionDbConstants.CONFIGURATION_ID));
            execution.setStartTime(resultSet.getTimestamp(ExecutionDbConstants.START_TIME));
            execution.setState(ExecutionState.valueOf(resultSet.getString(ExecutionDbConstants.STATE)));
            execution.setSchedule(resultSet.getString(ExecutionDbConstants.SCHEDULE));
            execution.setStateModifiedTime(resultSet.getTimestamp(ExecutionDbConstants.STATE_MODIFIED_TIME));
            execution.setAdditionalExecutionInformation(resultSet.getString(ExecutionDbConstants.ADDITIONAL_EXECUTION_INFORMATION));
            execution.setRetryAttempts(resultSet.getInt(ExecutionDbConstants.RETRY_ATTEMPTS));
            execution.setCalculationId(resultSet.getString(ExecutionDbConstants.CALCULATION_ID));
            execution.setNumOptimizationElementsReceived(resultSet.getInt(ExecutionDbConstants.NUM_OPTIMIZATION_ELEMENTS_RECEIVED));
            execution.setNumOptimizationElementsSent(resultSet.getInt(ExecutionDbConstants.NUM_OPTIMIZATION_ELEMENTS_SENT));
            execution.setNumSectorsToEvaluateForOptimization(resultSet.getInt(ExecutionDbConstants.NUM_SECTORS_TO_EVALUATE_FOR_OPTIMIZATION));
            execution.setNumOptimizationLbqs(resultSet.getInt(ExecutionDbConstants.NUM_OPTIMIZATION_LBQS));
            execution.setNumChangesWrittenToCmDb(resultSet.getInt(ExecutionDbConstants.NUM_CHANGES_WRITTEN_TO_CM_DB));
            execution.setNumChangesNotWrittenToCmDb(resultSet.getInt(ExecutionDbConstants.NUM_CHANGES_NOT_WRITTEN_TO_CM_DB));
            execution.setOpenLoop(resultSet.getBoolean(ExecutionDbConstants.OPEN_LOOP));
            execution.setWeekendDays(resultSet.getString(ExecutionDbConstants.WEEKEND_DAYS));
            execution.setEnablePA(resultSet.getBoolean(ExecutionDbConstants.ENABLE_PA));
            execution.setFullExecution(resultSet.getBoolean(ExecutionDbConstants.FULL_EXECUTION));
            try {
                execution.setCustomizedGlobalSettings(GSON.fromJson(resultSet.getString(
                        ExecutionDbConstants.CUSTOMIZED_GLOBAL_SETTINGS), new TypeToken<Map<String, String>>() {
                        }.getType()));
                execution.setCustomizedDefaultSettings(GSON.fromJson(resultSet.getString(
                        ExecutionDbConstants.CUSTOMIZED_DEFAULT_SETTINGS), new TypeToken<Map<String, String>>() {
                        }.getType()));
                execution.setGroups(Arrays.asList(GSON.fromJson(
                        resultSet.getString(ExecutionDbConstants.GROUPS), CustomizedGroup[].class)));
                execution.setInclusionList(Arrays.asList(GSON.fromJson(
                        resultSet.getString(ExecutionDbConstants.INCLUSION_LIST), Group[].class)));
                execution.setExclusionList(Arrays.asList(GSON.fromJson(
                        resultSet.getString(ExecutionDbConstants.EXCLUSION_LIST), Group[].class)));
            } catch (final JsonSyntaxException e) {
                LOGGER.error("Failed to parse customized global settings, customized default settings and groups for execution: {}", execution, e);
            }
            executions.add(execution);
        }
        LOGGER.debug("Populated list of executions: {}", executions);
        return executions;
    }
}

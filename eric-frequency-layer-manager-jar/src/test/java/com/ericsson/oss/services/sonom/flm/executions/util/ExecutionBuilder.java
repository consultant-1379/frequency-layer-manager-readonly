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

package com.ericsson.oss.services.sonom.flm.executions.util;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_1;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;

public class ExecutionBuilder {

    // default values for a test execution
    private String id = "FLM_1600701252";
    private final long executionTime = System.currentTimeMillis();
    private String cronExpression = "0 0 2 ? * * *";
    private int configurationId = 1;
    private int retryAttempts;
    private ExecutionState executionState = KPI_PROCESSING_GROUP_1;
    private Map<String, String> customizedDefaultSettings;
    private List<CustomizedGroup> groups = Collections.emptyList();
    private Boolean enablePA;
    private Boolean isFullExecution;

    private Timestamp stateModifiedTime = new Timestamp(executionTime);
    private Timestamp startTime = new Timestamp(executionTime);

    public ExecutionBuilder id(final String id) {
        this.id = id;
        return this;
    }

    public ExecutionBuilder stateModifiedTime(final Timestamp timeStamp) {
        stateModifiedTime = timeStamp;
        return this;
    }

    public ExecutionBuilder startTime(final Timestamp timeStamp) {
        startTime = timeStamp;
        return this;
    }

    public ExecutionBuilder configurationId(final int configurationId) {
        this.configurationId = configurationId;
        return this;
    }

    public ExecutionBuilder state(final ExecutionState state) {
        executionState = state;
        return this;
    }

    public ExecutionBuilder schedule(final String cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    public ExecutionBuilder retryAttempts(final int retryAttempts) {
        this.retryAttempts = retryAttempts;
        return this;
    }

    public ExecutionBuilder customizedDefaultSettings(final Map<String, String> customizedDefaultSettings) {
        this.customizedDefaultSettings = customizedDefaultSettings;
        return this;
    }

    public ExecutionBuilder groups(final List<CustomizedGroup> groups) {
        this.groups = groups;
        return this;
    }

    public ExecutionBuilder enablePA(final boolean enablePA) {
        this.enablePA = enablePA;
        return this;
    }

    public ExecutionBuilder withFullExecution(final boolean isFullExecution) {
        this.isFullExecution = isFullExecution;
        return this;
    }

    public Execution build() {
        final Execution execution = new Execution();
        execution.setId(id);
        execution.setStateModifiedTime(stateModifiedTime);
        execution.setStartTime(startTime);
        execution.setConfigurationId(configurationId);
        execution.setState(executionState);
        execution.setSchedule(cronExpression);
        execution.setRetryAttempts(retryAttempts);
        execution.setCustomizedDefaultSettings(customizedDefaultSettings);
        execution.setGroups(groups);
        execution.setFullExecution(isFullExecution);
        execution.setEnablePA(enablePA);

        return execution;
    }
}

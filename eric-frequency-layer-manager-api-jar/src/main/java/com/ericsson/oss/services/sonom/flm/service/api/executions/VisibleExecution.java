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
package com.ericsson.oss.services.sonom.flm.service.api.executions;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Class to store and represent execution ({@link Execution}) attributes that can be visible over the REST interface.
 */
public class VisibleExecution {

    private final String id;
    private final Integer configurationId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Timestamp startTime;
    private final ExecutionState state;
    private final Map<String, Object> additionalStateInformation;
    private final String schedule;
    private final Map<String, String> customizedGlobalSettings;
    private final Map<String, String> customizedDefaultSettings;
    private final List<CustomizedGroup> groups;
    private final Boolean openLoop;
    private final List<Group> inclusionList;
    private final List<Group> exclusionList;
    private final String weekendDays;
    private final Boolean enablePA;
    private final Boolean isFullExecution;

    public VisibleExecution(final Execution execution) {
        id = execution.getId();
        configurationId = execution.getConfigurationId();
        startTime = execution.getStartTime();
        state = execution.getState();
        additionalStateInformation = generateAdditionalStateInformation(execution);
        schedule = execution.getSchedule();
        customizedGlobalSettings = execution.getCustomizedGlobalSettings();
        customizedDefaultSettings = execution.getCustomizedDefaultSettings();
        groups = execution.getGroups();
        openLoop = execution.isOpenLoop();
        inclusionList = execution.getInclusionList();
        exclusionList = execution.getExclusionList();
        weekendDays = execution.getWeekendDays();
        enablePA = execution.isEnablePA();
        isFullExecution = execution.isFullExecution();
    }

    public String getId() {
        return id;
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public Timestamp getStartTime() {
        return new Timestamp(startTime.getTime());
    }

    public ExecutionState getState() {
        return state;
    }

    public Map<String, Object> getAdditionalStateInformation() {
        return additionalStateInformation;
    }

    public String getSchedule() {
        return schedule;
    }

    public Map<String, String> getCustomizedGlobalSettings() {
        return customizedGlobalSettings;
    }

    public Map<String, String> getCustomizedDefaultSettings() {
        return customizedDefaultSettings;
    }

    public List<CustomizedGroup> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Boolean isOpenLoop() {
        return openLoop;
    }

    public List<Group> getInclusionList() {
        return Collections.unmodifiableList(inclusionList);
    }

    public List<Group> getExclusionList() {
        return Collections.unmodifiableList(exclusionList);
    }

    public String getWeekendDays() {
        return weekendDays;
    }

    public Boolean isEnablePA() {
        return enablePA;
    }

    public Boolean isFullExecution() {
        return isFullExecution;
    }

    private Map<String, Object> generateAdditionalStateInformation(final Execution execution) {
        final Gson gson = new Gson();
        final Map<String, String> stringMap = new LinkedHashMap<>();
        final Map<String, Integer> integerMap = new LinkedHashMap<>();

        stringMap.put("additionalExecutionInformation", execution.getAdditionalExecutionInformation());

        integerMap.put("numSectorsToEvaluateForOptimization", execution.getNumSectorsToEvaluateForOptimization());
        integerMap.put("numOptimizationElementsSent", execution.getNumOptimizationElementsSent());
        integerMap.put("numOptimizationElementsReceived", execution.getNumOptimizationElementsReceived());
        integerMap.put("numOptimizationLbqs", execution.getNumOptimizationLbqs());
        integerMap.put("numChangesWrittenToCmDb", execution.getNumChangesWrittenToCmDb());
        integerMap.put("numChangesNotWrittenToCmDb", execution.getNumChangesNotWrittenToCmDb());

        final String jsonIntMap = gson.toJson(integerMap);

        final Map<String, Object> fromJsonStringMap = gson.fromJson(gson.toJson(stringMap), new TypeToken<Map<String, String>>() {
        }.getType());

        final Map<String, Object> fromJsonIntMap = gson.fromJson(jsonIntMap, new TypeToken<Map<String, Integer>>() {
        }.getType());

        fromJsonStringMap.putAll(fromJsonIntMap);

        return fromJsonStringMap;
    }
}

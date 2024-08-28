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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;

/**
 * Class to store and represent execution information.
 */
public class Execution extends ExecutionSummary implements Serializable {

    private static final long serialVersionUID = 7457095640360993277L;

    private Integer retryAttempts;
    private String calculationId;
    private Map<String, String> customizedGlobalSettings;
    private Map<String, String> customizedDefaultSettings;
    private List<CustomizedGroup> groups;
    private String additionalExecutionInformation;
    private Integer numSectorsToEvaluateForOptimization;
    private Integer numOptimizationElementsSent;
    private Integer numOptimizationElementsReceived;
    private Integer numOptimizationLbqs;
    private Integer numChangesWrittenToCmDb;
    private Integer numChangesNotWrittenToCmDb;
    private List<Group> inclusionList;
    private List<Group> exclusionList;
    private String weekendDays;
    private Boolean enablePA;

    public Integer getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(final Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public String getCalculationId() {
        return calculationId;
    }

    public void setCalculationId(final String calculationId) {
        this.calculationId = calculationId;
    }

    public Map<String, String> getCustomizedGlobalSettings() {
        return customizedGlobalSettings;
    }

    public void setCustomizedGlobalSettings(final Map<String, String> customizedGlobalSettings) {
        this.customizedGlobalSettings = customizedGlobalSettings;
    }

    public Map<String, String> getCustomizedDefaultSettings() {
        return customizedDefaultSettings;
    }

    public void setCustomizedDefaultSettings(final Map<String, String> customizedDefaultSettings) {
        this.customizedDefaultSettings = customizedDefaultSettings;
    }

    public List<CustomizedGroup> getGroups() {
        return groups; //NOSONAR it's a POJO
    }

    public void setGroups(final List<CustomizedGroup> groups) {
        this.groups = groups; //NOSONAR it's a POJO
    }

    public String getAdditionalExecutionInformation() {
        return additionalExecutionInformation;
    }

    public void setAdditionalExecutionInformation(final String additionalExecutionInformation) {
        this.additionalExecutionInformation = additionalExecutionInformation;
    }

    public Integer getNumSectorsToEvaluateForOptimization() {
        return numSectorsToEvaluateForOptimization;
    }

    public void setNumSectorsToEvaluateForOptimization(final Integer numSectorsToEvaluateForOptimization) {
        this.numSectorsToEvaluateForOptimization = numSectorsToEvaluateForOptimization;
    }

    public Integer getNumOptimizationElementsSent() {
        return numOptimizationElementsSent;
    }

    public void setNumOptimizationElementsSent(final Integer numOptimizationElementsSent) {
        this.numOptimizationElementsSent = numOptimizationElementsSent;
    }

    public Integer getNumOptimizationElementsReceived() {
        return numOptimizationElementsReceived;
    }

    public void setNumOptimizationElementsReceived(final Integer numOptimizationElementsReceived) {
        this.numOptimizationElementsReceived = numOptimizationElementsReceived;
    }

    public Integer getNumOptimizationLbqs() {
        return numOptimizationLbqs;
    }

    public void setNumOptimizationLbqs(final Integer numOptimizationLbqs) {
        this.numOptimizationLbqs = numOptimizationLbqs;
    }

    public Integer getNumChangesWrittenToCmDb() {
        return numChangesWrittenToCmDb;
    }

    public void setNumChangesWrittenToCmDb(final Integer numChangesWrittenToCmDb) {
        this.numChangesWrittenToCmDb = numChangesWrittenToCmDb;
    }

    public Integer getNumChangesNotWrittenToCmDb() {
        return numChangesNotWrittenToCmDb;
    }

    public void setNumChangesNotWrittenToCmDb(final Integer numChangesNotWrittenToCmDb) {
        this.numChangesNotWrittenToCmDb = numChangesNotWrittenToCmDb;
    }

    public List<Group> getExclusionList() {
        return exclusionList; //NOSONAR it's a POJO
    }

    public void setExclusionList(final List<Group> exclusionList) {
        this.exclusionList = exclusionList; //NOSONAR it's a POJO
    }

    public List<Group> getInclusionList() {
        return inclusionList; //NOSONAR it's a POJO
    }

    public void setInclusionList(final List<Group> inclusionList) {
        this.inclusionList = inclusionList; //NOSONAR it's a POJO
    }

    public String getWeekendDays() {
        return weekendDays;
    }

    public void setWeekendDays(final String weekendDays) {
        this.weekendDays = weekendDays;
    }

    public Boolean isEnablePA() {
        return enablePA;
    }

    public void setEnablePA(final Boolean enablePA) {
        this.enablePA = enablePA;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Execution execution = (Execution) o;
        return Objects.equals(super.getId(), execution.getId()) &&
                Objects.equals(super.getConfigurationId(), execution.getConfigurationId()) &&
                Objects.equals(super.getStartTime(), execution.getStartTime()) &&
                super.getState() == execution.getState() &&
                Objects.equals(super.getStateModifiedTime(), execution.getStateModifiedTime()) &&
                Objects.equals(super.getSchedule(), execution.getSchedule()) &&
                Objects.equals(retryAttempts, execution.retryAttempts) &&
                Objects.equals(calculationId, execution.calculationId) &&
                Objects.equals(customizedGlobalSettings, execution.customizedGlobalSettings) &&
                Objects.equals(customizedDefaultSettings, execution.customizedDefaultSettings) &&
                Objects.equals(groups, execution.groups) &&
                Objects.equals(additionalExecutionInformation, execution.getAdditionalExecutionInformation()) &&
                Objects.equals(numSectorsToEvaluateForOptimization, execution.numSectorsToEvaluateForOptimization) &&
                Objects.equals(numOptimizationElementsSent, execution.numOptimizationElementsSent) &&
                Objects.equals(numOptimizationElementsReceived, execution.numOptimizationElementsReceived) &&
                Objects.equals(numOptimizationLbqs, execution.numOptimizationLbqs) &&
                Objects.equals(numChangesWrittenToCmDb, execution.numChangesWrittenToCmDb) &&
                Objects.equals(numChangesNotWrittenToCmDb, execution.numChangesNotWrittenToCmDb) &&
                Objects.equals(super.isOpenLoop(), execution.isOpenLoop()) &&
                Objects.equals(inclusionList, execution.inclusionList) &&
                Objects.equals(exclusionList, execution.exclusionList) &&
                Objects.equals(weekendDays, execution.weekendDays) &&
                Objects.equals(enablePA, execution.enablePA) &&
                Objects.equals(super.isFullExecution(), execution.isFullExecution());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.getId(), super.getConfigurationId(), super.getStartTime(), super.getState(),
                super.getStateModifiedTime(), super.getSchedule(), retryAttempts, calculationId,
                customizedGlobalSettings, customizedDefaultSettings, groups, additionalExecutionInformation,
                numOptimizationElementsSent, numOptimizationElementsReceived, numOptimizationLbqs, numChangesWrittenToCmDb,
                numChangesNotWrittenToCmDb, super.isOpenLoop(), inclusionList, exclusionList, weekendDays, enablePA, super.isFullExecution());
    }

    @Override
    public String toString() {
        return String.format("%s:: {id: '%s', configurationId: '%s', startTime: '%s', state: '%s', stateModifiedTime '%s', " +
                "schedule '%s', retryAttempts '%s', calculationId '%s', customizedGlobalSettings '%s', customizedDefaultSettings '%s', " +
                "groups '%s', additionalExecutionInformation '%s', numSectorsToEvaluateForOptimization '%s', numOptimizationElementsSent '%s', " +
                "numOptimizationElementsReceived '%s', numOptimizationLbqs '%s', numChangesWrittenToCmDb '%s', numChangesNotWrittenToCmDb: '%s', " +
                "openLoop '%s', inclusionList '%s', exclusionList '%s', weekendDays '%s', enablePA '%s', isFullExecution '%s'}",
                getClass().getSimpleName(), super.getId(), super.getConfigurationId(), super.getStartTime(),
                super.getState(), super.getStateModifiedTime(), super.getSchedule(), retryAttempts, calculationId,
                customizedGlobalSettings, customizedDefaultSettings, groups, additionalExecutionInformation, numSectorsToEvaluateForOptimization,
                numOptimizationElementsSent, numOptimizationElementsReceived, numOptimizationLbqs, numChangesWrittenToCmDb,
                numChangesNotWrittenToCmDb, super.isOpenLoop(), inclusionList, exclusionList, weekendDays, enablePA, super.isFullExecution());
    }
}

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
package com.ericsson.oss.services.sonom.flm.service.api.executions;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Class to store and represent reduced execution information when retrieving all executions.
 */
public class ExecutionSummary implements Serializable {

    private static final long serialVersionUID = 3501943223412521033L;

    private String id;
    private Integer configurationId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Timestamp startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Timestamp stateModifiedTime;
    private ExecutionState state;
    private String schedule;
    private Boolean openLoop;
    private Boolean isFullExecution;

    public ExecutionSummary(final ExecutionSummary executionSummary) {
        this.id = executionSummary.id;
        this.configurationId = executionSummary.configurationId;
        this.startTime = executionSummary.startTime;
        this.stateModifiedTime = executionSummary.stateModifiedTime;
        this.state = executionSummary.state;
        this.schedule = executionSummary.schedule;
        this.openLoop = executionSummary.openLoop;
        this.isFullExecution = executionSummary.isFullExecution;
    }

    public ExecutionSummary() {

    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(final Integer configurationId) {
        this.configurationId = configurationId;
    }

    public Timestamp getStartTime() {
        return new Timestamp(startTime.getTime());
    }

    public void setStartTime(final Timestamp startTime) {
        this.startTime = new Timestamp(startTime.getTime());
    }

    public Timestamp getStateModifiedTime() {
        return new Timestamp(stateModifiedTime.getTime());
    }

    public void setStateModifiedTime(final Timestamp stateModifiedTime) {
        this.stateModifiedTime = new Timestamp(stateModifiedTime.getTime());
    }

    public ExecutionState getState() {
        return state;
    }

    public void setState(final ExecutionState state) {
        this.state = state;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(final String schedule) {
        this.schedule = schedule;
    }

    public Boolean isOpenLoop() {
        return openLoop;
    }

    public void setOpenLoop(final Boolean openLoop) {
        this.openLoop = openLoop;
    }

    public Boolean isFullExecution() {
        return isFullExecution;
    }

    public void setFullExecution(final Boolean isFullExecution) {
        this.isFullExecution = isFullExecution;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ExecutionSummary execution = (ExecutionSummary) o;
        return Objects.equals(id, execution.id) &&
                Objects.equals(configurationId, execution.configurationId) &&
                Objects.equals(startTime, execution.startTime) &&
                Objects.equals(stateModifiedTime, execution.stateModifiedTime) &&
                state == execution.state &&
                Objects.equals(schedule, execution.schedule) &&
                Objects.equals(openLoop, execution.openLoop) &&
                Objects.equals(isFullExecution, execution.isFullExecution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, configurationId, startTime, stateModifiedTime, state, schedule, openLoop, isFullExecution);
    }

    @Override
    public String toString() {
        return String.format("%s:: {id: '%s', configurationId: '%s', startTime: '%s', stateModifiedTime: '%s', " +
                "state: '%s', schedule '%s', openLoop '%s', isFullExecution '%s'}",
                getClass().getSimpleName(), id, configurationId, startTime, stateModifiedTime, state, schedule, openLoop, isFullExecution);
    }
}

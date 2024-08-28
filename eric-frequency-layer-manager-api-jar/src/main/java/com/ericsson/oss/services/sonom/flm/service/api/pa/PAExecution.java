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

package com.ericsson.oss.services.sonom.flm.service.api.pa;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Class to store and represent {@link PAExecution} information.
 */
public class PAExecution implements Serializable {

    private static final long serialVersionUID = 7457482970360993277L;

    private final String id;
    private final Integer paWindow;
    private final String schedule;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Timestamp paWindowStartTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Timestamp paWindowEndTime;
    private PAExecutionState state;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Timestamp stateModifiedTime;
    private final String flmExecutionId;
    private Integer numPaPolicyInputEventsSent;

    public PAExecution(final Integer paWindow, final String schedule, final Timestamp paWindowStartTime, final Timestamp paWindowEndTime,
                       final String flmExecutionId) {
        this.paWindow = paWindow;
        this.schedule = schedule;
        this.paWindowStartTime = new Timestamp(paWindowStartTime.getTime());
        this.paWindowEndTime = new Timestamp(paWindowEndTime.getTime());
        this.flmExecutionId = flmExecutionId;
        numPaPolicyInputEventsSent = 0;

        this.id = flmExecutionId + "_" + paWindow;
    }

    public PAExecution(final String id, final Integer paWindow, final String schedule,
                       final Timestamp paWindowStartTime, final Timestamp paWindowEndTime,
                       final Timestamp stateModifiedTime, final String flmExecutionId) {
        this.id = id;
        this.paWindow = paWindow;
        this.schedule = schedule;
        this.paWindowStartTime = new Timestamp(paWindowStartTime.getTime());
        this.paWindowEndTime = new Timestamp(paWindowEndTime.getTime());
        this.flmExecutionId = flmExecutionId;
        this.stateModifiedTime = new Timestamp(stateModifiedTime.getTime());
    }

    public String getId() {
        return id;
    }

    public Integer getPaWindow() {
        return paWindow;
    }

    public String getSchedule() {
        return schedule;
    }

    public Timestamp getPaWindowStartTime() {
        return new Timestamp(paWindowStartTime.getTime());
    }

    public Timestamp getPaWindowEndTime() {
        return new Timestamp(paWindowEndTime.getTime());
    }

    public PAExecutionState getState() {
        return state;
    }

    public void setState(final PAExecutionState paExecutionState) {
        this.state = paExecutionState;
    }

    public String getFlmExecutionId() {
        return flmExecutionId;
    }

    public Integer getNumPaPolicyInputEventsSent() {
        return numPaPolicyInputEventsSent;
    }

    public void setNumPaPolicyInputEventsSent(final Integer numPaPolicyInputEventsSent) {
        this.numPaPolicyInputEventsSent = numPaPolicyInputEventsSent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (Objects.isNull(o) || getClass() != o.getClass()) {
            return false;
        }
        final PAExecution that = (PAExecution) o;
        return Objects.equals(getPaWindow(), that.getPaWindow()) &&
                Objects.equals(getSchedule(), that.getSchedule()) &&
                Objects.equals(paWindowStartTime, that.paWindowStartTime) &&
                Objects.equals(paWindowEndTime, that.paWindowEndTime) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(flmExecutionId, that.flmExecutionId) &&
                Objects.equals(numPaPolicyInputEventsSent, that.numPaPolicyInputEventsSent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPaWindow(), getSchedule(), paWindowStartTime, paWindowEndTime, getState(),
                flmExecutionId, numPaPolicyInputEventsSent);
    }

    @Override
    public String toString() {
        return "PAExecution{" +
                "id=" + id +
                ", paWindow=" + paWindow +
                ", schedule='" + schedule + '\'' +
                ", paWindowStartTime=" + paWindowStartTime +
                ", paWindowEndTime=" + paWindowEndTime +
                ", state=" + state +
                ", stateModifiedTime=" + stateModifiedTime +
                ", flmExecutionId='" + flmExecutionId + '\'' +
                ", numPaPolicyInputEventsSent=" + numPaPolicyInputEventsSent +
                '}';
    }
}
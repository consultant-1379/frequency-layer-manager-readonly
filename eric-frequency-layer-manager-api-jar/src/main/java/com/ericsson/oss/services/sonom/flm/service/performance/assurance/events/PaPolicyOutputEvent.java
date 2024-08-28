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

package com.ericsson.oss.services.sonom.flm.service.performance.assurance.events;

import java.util.Objects;

import com.ericsson.oss.services.sonom.flm.service.events.PolicyEvent;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation.DegradationStatus;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.Sector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Object used to store details to be sent from the Policy-Engine through a Kafka message bus(FLM Performance Assurance Output Event Object).
 */
public class PaPolicyOutputEvent extends PolicyEvent {

    private static final Gson GSON_WITH_SUPPORT_FOR_NEGATIVE_INFINITY_DOUBLES = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    private final String flmExecutionId;
    private final String paExecutionId;
    private final Integer paWindow;
    private final Sector sector;
    private final DegradationStatus degradationStatus;

    public PaPolicyOutputEvent(final String flmExecutionId, final String paExecutionId, final Integer paWindow, final Sector sector,
            final DegradationStatus degradationStatus) {
        super("com.ericsson.oss.services.sonom.events",
                "FlmPaPolicyOutputEvent",
                "0.0.1",
                "source",
                "target");
        this.flmExecutionId = flmExecutionId;
        this.paExecutionId = paExecutionId;
        this.paWindow = paWindow;
        this.sector = sector;
        this.degradationStatus = degradationStatus;
    }

    public PaPolicyOutputEvent(final String flmExecutionId, final String paExecutionId, final Integer paWindow, final String sectorId,
            final DegradationStatus degradationStatus) {
        this(flmExecutionId, paExecutionId, paWindow, new Sector(sectorId), degradationStatus);
    }

    public String getFlmExecutionId() {
        return flmExecutionId;
    }

    public String getPaExecutionId() {
        return paExecutionId;
    }

    public Integer getPaWindow() {
        return paWindow;
    }

    public Sector getSector() {
        return sector;
    }

    public DegradationStatus getDegradationStatus() {
        return degradationStatus;
    }

    public String toJson() {
        return GSON_WITH_SUPPORT_FOR_NEGATIVE_INFINITY_DOUBLES.toJson(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PaPolicyOutputEvent that = (PaPolicyOutputEvent) o;
        return Objects.equals(nameSpace, that.nameSpace) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(source, that.source) &&
                Objects.equals(target, that.target) &&
                Objects.equals(flmExecutionId, that.flmExecutionId) &&
                Objects.equals(paExecutionId, that.paExecutionId) &&
                Objects.equals(paWindow, that.paWindow) &&
                Objects.equals(sector, that.sector) &&
                Objects.equals(degradationStatus, that.degradationStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameSpace, name, version, source, target, flmExecutionId, paExecutionId, paWindow, sector, degradationStatus);
    }

    @Override
    public String toString() {
        return String.format(
                "%s:: {name: '%s',version: '%s', nameSpace: '%s', source: '%s', target: '%s', flmExecutionId: '%s', " +
                        "paExecutionId: '%s', paExecutionId: '%s', sector: '%s', degradationStatus: '%s'}",
                getClass().getSimpleName(), name, version, nameSpace, source, target, flmExecutionId, paExecutionId, paWindow,
                sector, degradationStatus);
    }
}

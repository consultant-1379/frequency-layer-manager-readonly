/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Object used to store details to be sent to the Policy-Engine through a Kafka message bus (Input Event Object).
 */
public class PolicyInputEvent extends PolicyEvent {
    private static final long serialVersionUID = 5873002275671295357L;
    private static final Gson GSON_WITH_SUPPORT_FOR_NEGATIVE_INFINITY_DOUBLES = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    private final String sectorId;
    private final String executionId;
    private final List<OptimizationCell> optimizationCells;

    public PolicyInputEvent(final List<OptimizationCell> optimizationCells, final String sectorId, final String executionId) {
        super("com.ericsson.oss.services.sonom.events", "FlmPolicyInputEvent", "0.0.1", "source", "target");
        this.optimizationCells = new ArrayList<>(optimizationCells);
        this.sectorId = sectorId;
        this.executionId = executionId;
    }

    public String getSectorId() {
        return sectorId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public List<OptimizationCell> getOptimizationCells() {

        return new ArrayList<>(optimizationCells);
    }

    public String toJson() {
        return GSON_WITH_SUPPORT_FOR_NEGATIVE_INFINITY_DOUBLES.toJson(this);
    }

    @Override
    public String toString() {
        return String.format(
                "%s:: { nameSpace: '%s', name: '%s', version: '%s', source: '%s', target: '%s', sectorId: '%s', executionId: '%s', "
                        + "optimizationCells: '%s'}",
                getClass().getSimpleName(), nameSpace, name, version, source, target, sectorId, executionId, optimizationCells.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameSpace, name, version, source, target, sectorId, executionId, optimizationCells);

    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PolicyInputEvent that = (PolicyInputEvent) o;
        return Objects.equals(nameSpace, that.getNameSpace())
                && Objects.equals(name, that.getName()) && Objects.equals(version, that.getVersion())
                && Objects.equals(source, that.getSource()) && Objects.equals(target, that.getTarget())
                && Objects.equals(sectorId, that.getSectorId())
                && Objects.equals(executionId, that.getExecutionId())
                && Objects.equals(optimizationCells, that.getOptimizationCells());

    }

}

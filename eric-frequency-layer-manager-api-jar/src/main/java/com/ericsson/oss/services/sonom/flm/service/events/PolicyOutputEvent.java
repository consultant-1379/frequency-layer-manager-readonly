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

import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * Object used to store details to be sent from the Policy-Engine through a Kafka message bus (Output Event Object).
 */
public class PolicyOutputEvent extends PolicyEvent {

    private static final Gson GSON_WITH_SUPPORT_FOR_NEGATIVE_INFINITY_DOUBLES = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    private Long sectorId;
    private String executionId;

    @SerializedName("proposedLoadBalancingQuanta")
    private ProposedLoadBalancingQuanta loadBalancingQuanta;
    private List<OptimizationCell> optimizationCells = new ArrayList<>();

    public PolicyOutputEvent(final ProposedLoadBalancingQuanta loadBalancingQuanta) {
        super("com.ericsson.oss.services.sonom.events",
                "FlmPolicyOutputEvent",
                "0.0.1",
                "source",
                "target");
        this.loadBalancingQuanta = loadBalancingQuanta;
    }

    @SuppressWarnings("squid:S00107")
    public PolicyOutputEvent(final String name,
            final String version,
            final String nameSpace,
            final String source,
            final String target,
            final Long sectorId,
            final String executionId,
            final ProposedLoadBalancingQuanta loadBalancingQuanta,
            final List<OptimizationCell> optimizationCells) {

        // constructor needed to correctly deserialize output of FLM policy
        super(nameSpace, name, version, source, target);
        this.sectorId = sectorId;
        this.executionId = executionId;
        this.loadBalancingQuanta = loadBalancingQuanta;
        initializeOptimizationCells(optimizationCells);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(final String nameSpace) {
        this.nameSpace = nameSpace;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    @Override
    public String getTarget() {
        return target;
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getSourceCellFdn() {
        return loadBalancingQuanta == null ? null : loadBalancingQuanta.getSourceCellFdn();
    }

    public Integer getSourceCellOssdId() {
        return loadBalancingQuanta == null ? null : loadBalancingQuanta.getSourceCellOssId();
    }

    public void setSectorId(final Long sectorId) {
        this.sectorId = sectorId;
    }

    public void setExecutionId(final String executionId) {
        this.executionId = executionId;
    }

    public List<OptimizationCell> getOptimizationCells() {
        return new ArrayList<>(optimizationCells);
    }

    public void setOptimizationCells(final List<OptimizationCell> optimizationCells) {
        initializeOptimizationCells(optimizationCells);
    }

    public ProposedLoadBalancingQuanta getLoadBalancingQuanta() {
        return loadBalancingQuanta;
    }

    public void setLoadBalancingQuanta(final ProposedLoadBalancingQuanta loadBalancingQuanta) {
        this.loadBalancingQuanta = loadBalancingQuanta;
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
        final PolicyOutputEvent that = (PolicyOutputEvent) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(nameSpace, that.nameSpace)
                && Objects.equals(source, that.source) && Objects.equals(target, that.target) && Objects.equals(sectorId, that.sectorId)
                && Objects.equals(executionId, that.executionId) && Objects.equals(optimizationCells, that.optimizationCells)
                && Objects.equals(loadBalancingQuanta, that.loadBalancingQuanta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, nameSpace, source, target, sectorId, executionId, loadBalancingQuanta, optimizationCells);
    }

    @Override
    public String toString() {
        return String.format(
                "%s:: {name: '%s',version: '%s', nameSpace: '%s', source: '%s', target: '%s', sectorId: '%d', executionId: '%s'"
                        + ", loadBalancingQuanta: '%s', optimizationCells: '%s'}",
                getClass().getSimpleName(), name, version, nameSpace, source, target, sectorId, executionId, String.valueOf(loadBalancingQuanta),
                String.valueOf(optimizationCells));
    }

    private void initializeOptimizationCells(final List<OptimizationCell> optimizationCells) {
        if (optimizationCells == null) {
            this.optimizationCells = new ArrayList<>();
        } else {
            this.optimizationCells = new ArrayList<>(optimizationCells);
        }
    }
}

/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.KpiValue;

/**
 * A POJO to represent a cell level KPI to be passed to the FLM performance assurance policy.
 */
public class CellLevelKpi implements Serializable {

    private static final long serialVersionUID = 92883852154776798L;

    private final List<KpiValue> kpiValue;
    private final boolean enabled;
    private final String relevanceThreshold;
    private final RelevanceThresholdType relevanceThresholdType;

    public CellLevelKpi(final List<KpiValue> kpiValue, final boolean enabled, final String relevanceThreshold,
            final RelevanceThresholdType relevanceThresholdType) {
        this.kpiValue = new ArrayList<>(kpiValue);
        this.enabled = enabled;
        this.relevanceThreshold = relevanceThreshold;
        this.relevanceThresholdType = relevanceThresholdType;
    }

    public List<KpiValue> getKpiValue() {
        return new ArrayList<>(kpiValue);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRelevanceThreshold() {
        return relevanceThreshold;
    }

    public RelevanceThresholdType getRelevanceThresholdType() {
        return relevanceThresholdType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CellLevelKpi kpi = (CellLevelKpi) o;

        final List<KpiValue> listOne = new ArrayList<>(kpiValue);
        final List<KpiValue> listTwo = new ArrayList<>(kpi.kpiValue);
        Collections.sort(listOne);
        Collections.sort(listTwo);
        return enabled == kpi.enabled &&
                Objects.equals(listOne, listTwo) &&
                Objects.equals(relevanceThreshold, kpi.relevanceThreshold) &&
                Objects.equals(relevanceThresholdType, kpi.relevanceThresholdType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kpiValue, enabled, relevanceThreshold, relevanceThresholdType);
    }

    @Override
    public String toString() {
        return String.format("%s:: { kpiValue: '%s', enabled: '%s', relevanceThreshold: '%s', relevanceThresholdType: '%s' }",
                getClass().getSimpleName(), kpiValue, enabled, relevanceThreshold, relevanceThresholdType);
    }

}

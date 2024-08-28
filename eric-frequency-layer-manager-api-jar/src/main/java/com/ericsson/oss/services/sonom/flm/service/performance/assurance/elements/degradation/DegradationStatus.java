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

package com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation;

import java.util.Map;
import java.util.Objects;

/**
 * A POJO to represent the degradation status to be used as output from the FLM performance Assurance policy.
 */
public class DegradationStatus {

    private final String verdict;
    private final Map<String, DegradedSectorKpi> degradedSectorKpis;
    private final Map<String, DegradedCellKpi> degradedCellKpis;

    public DegradationStatus(final String verdict, final Map<String, DegradedSectorKpi> degradedSectorKpis,
            final Map<String, DegradedCellKpi> degradedCellKpis) {
        this.verdict = verdict;
        this.degradedSectorKpis = degradedSectorKpis;
        this.degradedCellKpis = degradedCellKpis;
    }

    public String getVerdict() {
        return verdict;
    }

    public Map<String, DegradedCellKpi> getDegradedCellKpis() {
        return degradedCellKpis;
    }

    public Map<String, DegradedSectorKpi> getDegradedSectorKpis() {
        return degradedSectorKpis;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DegradationStatus that = (DegradationStatus) o;
        return Objects.equals(verdict, that.verdict) &&
                Objects.equals(degradedSectorKpis, that.degradedSectorKpis) &&
                Objects.equals(degradedCellKpis, that.degradedCellKpis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verdict, degradedSectorKpis, degradedCellKpis);
    }

    @Override
    public String toString() {
        return String.format("%s:: { verdict: '%s', degradedSectorKpis: '%s', degradedCellKpis: '%s' }",
                getClass().getSimpleName(), verdict, degradedSectorKpis, degradedCellKpis);
    }
}

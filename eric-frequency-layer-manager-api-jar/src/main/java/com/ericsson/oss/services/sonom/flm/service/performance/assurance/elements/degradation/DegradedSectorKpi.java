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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A degraded sector level KPI to be used as output from the FLM performance Assurance policy.
 */
public class DegradedSectorKpi {

    private final Map<String, List<String>> sectorIdToDegradedTimestamps;

    public DegradedSectorKpi(final Map<String, List<String>> sectorIdToDegradedTimestamps) {
        this.sectorIdToDegradedTimestamps = sectorIdToDegradedTimestamps;
    }

    public Map<String, List<String>> getSectorIdToDegradedTimestamps() {
        return sectorIdToDegradedTimestamps;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DegradedSectorKpi that = (DegradedSectorKpi) o;
        return Objects.equals(sectorIdToDegradedTimestamps, that.sectorIdToDegradedTimestamps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectorIdToDegradedTimestamps);
    }

    @Override
    public String toString() {
        return String.format("%s:: { sectorIdToDegradedTimestamps: '%s' }",
                getClass().getSimpleName(), sectorIdToDegradedTimestamps);
    }
}

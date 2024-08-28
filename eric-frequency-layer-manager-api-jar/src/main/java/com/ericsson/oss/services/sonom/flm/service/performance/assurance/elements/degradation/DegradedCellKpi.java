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
 * A degraded cell level KPI to be used as output from the FLM performance Assurance policy.
 */
public class DegradedCellKpi {

    private final Map<String, Map<String, List<String>>> ossIdToFdnToDegradedTimestamps;

    public DegradedCellKpi(final Map<String, Map<String, List<String>>> ossIdToFdnToDegradedTimestamps) {
        this.ossIdToFdnToDegradedTimestamps = ossIdToFdnToDegradedTimestamps;
    }

    public Map<String, Map<String, List<String>>> getOssIdToFdnToDegradedTimestamps() {
        return ossIdToFdnToDegradedTimestamps;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DegradedCellKpi that = (DegradedCellKpi) o;
        return Objects.equals(ossIdToFdnToDegradedTimestamps, that.ossIdToFdnToDegradedTimestamps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ossIdToFdnToDegradedTimestamps);
    }

    @Override
    public String toString() {
        return String.format("%s:: { ossIdToFdnToDegradedTimestamps: '%s' }",
                getClass().getSimpleName(), ossIdToFdnToDegradedTimestamps);
    }
}

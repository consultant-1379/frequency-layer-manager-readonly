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

package com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements;

import java.io.Serializable;
import java.util.Objects;

/**
 * A POJO to represent the value and timestamp of a KPI to be passed to the FLM performance assurance policy.
 */
public class KpiValue implements Comparable<KpiValue>, Serializable {

    private static final long serialVersionUID = 1288476832154966798L;

    private final String value;
    private final String timestamp;
    private final String threshold;

    public KpiValue(final String value, final String timestamp, final String threshold) {
        this.value = value;
        this.timestamp = timestamp;
        this.threshold = threshold;
    }

    public String getValue() {
        return value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getThreshold() {
        return threshold;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final KpiValue kpiValue = (KpiValue) o;
        return Objects.equals(value, kpiValue.value) &&
                Objects.equals(timestamp, kpiValue.timestamp) &&
                Objects.equals(threshold, kpiValue.threshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, timestamp, threshold);
    }

    @Override
    public String toString() {
        return String.format("%s:: { value: '%s', timestamp: '%s' , threshold: '%s'}",
                getClass().getSimpleName(), value, timestamp, threshold);
    }

    @Override
    public int compareTo(final KpiValue kpiValue) {
        int cmp = timestamp.compareTo(kpiValue.timestamp);
        if (cmp == 0) {
            cmp = value.compareTo(kpiValue.value);
        }
        if (cmp == 0) {
            cmp = threshold.compareTo(kpiValue.threshold);
        }
        return cmp;
    }
}

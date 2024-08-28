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

package com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.KpiValue;

/**
 * A POJO to represent a sector level KPI to be passed to the FLM performance assurance policy.
 */
public class SectorLevelKpi implements Serializable {

    private static final long serialVersionUID = 955861289677948566L;

    private final List<KpiValue> kpiValue;
    private final boolean enabled;
    private final String lowerRangeLimit;
    private final String upperRangeLimit;

    public SectorLevelKpi(final List<KpiValue> kpiValue, final boolean enabled, final String lowerRangeLimit, final String upperRangeLimit) {
        this.kpiValue = new ArrayList<>(kpiValue);
        this.enabled = enabled;
        this.lowerRangeLimit = lowerRangeLimit;
        this.upperRangeLimit = upperRangeLimit;
    }

    public List<KpiValue> getKpiValue() {
        return new ArrayList<>(kpiValue);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getLowerRangeLimit() {
        return lowerRangeLimit;
    }

    public String getUpperRangeLimit() {
        return upperRangeLimit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SectorLevelKpi kpi = (SectorLevelKpi) o;
        final List<KpiValue> listOne = new ArrayList<>(kpiValue);
        final List<KpiValue> listTwo = new ArrayList<>(kpi.kpiValue);
        Collections.sort(listOne);
        Collections.sort(listTwo);
        return enabled == kpi.enabled &&
                lowerRangeLimit.equals(kpi.getLowerRangeLimit()) &&
                upperRangeLimit.equals(kpi.getUpperRangeLimit()) &&
                Objects.equals(listOne, listTwo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kpiValue, enabled, lowerRangeLimit, upperRangeLimit);
    }

    @Override
    public String toString() {
        return String.format("%s:: { kpiValue: '%s', enabled: '%s', lowerRangeLimit: '%s', upperRangeLimit: '%s' }",
                getClass().getSimpleName(), kpiValue, enabled, lowerRangeLimit, upperRangeLimit);
    }
}

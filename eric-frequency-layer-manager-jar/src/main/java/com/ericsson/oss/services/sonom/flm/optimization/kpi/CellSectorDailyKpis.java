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

package com.ericsson.oss.services.sonom.flm.optimization.kpi;

/**
 * Enum of daily cell sector KPIs.
 */
public enum CellSectorDailyKpis {
    COVERAGE_BALANCE_RATIO_DISTANCE("coverage_balance_ratio_distance");

    private final String kpiName;

    CellSectorDailyKpis(final String kpiName) {
        this.kpiName = kpiName;
    }

    public String getKpiName() {
        return kpiName;
    }
}

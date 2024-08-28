/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020- 2021
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
 * Defines the Busy Hour KPIs for Cells Per FLM execution.
 */
public enum BusyHourCellFlmKpis {

    UNHAPPY_USERS("unhappy_users"),
    GOAL_FUNCTION_RESOURCE_EFFICIENCY("goal_function_resource_efficiency"),
    P_FAILING_R_MBPS("p_failing_r_mbps"),
    P_FAILING_R_MBPS_DETRENDED("p_failing_r_mbps_detrended"),
    APP_COVERAGE_RELIABILITY("app_coverage_reliability");

    private final String kpiName;

    BusyHourCellFlmKpis(final String kpiName) {
        this.kpiName = kpiName;
    }

    public String getKpiName() {
        return kpiName;
    }

}

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
 * Defines the Busy Hour KPIs for Cells.
 */
public enum BusyHourCellKpis {

    PM_IDLE_MODE_REL_DISTR_HIGH_LOAD("pm_idle_mode_rel_distr_high_load"),
    PM_IDLE_MODE_REL_DISTR_MEDIUM_HIGH_LOAD("pm_idle_mode_rel_distr_medium_high_load"),
    PM_IDLE_MODE_REL_DISTR_MEDIUM_LOAD("pm_idle_mode_rel_distr_medium_load"),
    PM_IDLE_MODE_REL_DISTR_LOW_MEDIUM_LOAD("pm_idle_mode_rel_distr_low_medium_load"),
    PM_IDLE_MODE_REL_DISTR_LOW_LOAD("pm_idle_mode_rel_distr_low_load"),
    CONNECTED_USERS("connected_users"),
    SUBSCRIPTION_RATIO("subscription_ratio");

    private final String kpiName;

    BusyHourCellKpis(final String kpiName) {
        this.kpiName = kpiName;
    }

    public String getKpiName() {
        return kpiName;
    }

}

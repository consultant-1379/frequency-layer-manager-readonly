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
 * Defines the visible Cell Sector FLM KPIs found in the kpi_cell_sector_flm_1440 table.
 */
public enum CellSectorFlmVisibleKpis {
    LOWER_THRESHOLD_FOR_TRANSIENT("lower_threshold_for_transient"),
    UPPER_THRESHOLD_FOR_TRANSIENT("upper_threshold_for_transient");

    private final String kpiName;

    CellSectorFlmVisibleKpis(final String kpiName) {
        this.kpiName = kpiName;
    }

    public String getKpiName() {
        return kpiName;
    }
}

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
 * Defines the Cell Sector Flm KPIs. They are the KPIs found in the kpi_cell_sector_flm_1440 table.
 */
public enum CellSectorFlmKpis {
    TARGET_CELL_CAPACITY("target_cell_capacity"),
    MAX_CONNECTED_USERS("max_connected_users_daily"),
    NUM_CELLS_USED_FOR_MCU_CDF_CALCULATION("num_values_used_for_mcu_cdf_calculation_daily");

    private final String kpiName;

    CellSectorFlmKpis(final String kpiName) {
        this.kpiName = kpiName;
    }

    public String getKpiName() {
        return kpiName;
    }
}

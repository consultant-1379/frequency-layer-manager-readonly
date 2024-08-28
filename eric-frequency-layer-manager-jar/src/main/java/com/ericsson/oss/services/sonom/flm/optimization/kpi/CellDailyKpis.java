/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.optimization.kpi;

/**
 * Defines the Cell Daily KPIs. They are the KPIs found in the <code>cell_guid_1440_kpis</code> table.
 */
public enum CellDailyKpis {

    CONTIGUITY("contiguity"),
    CELL_HANDOVER_SUCCESS_RATE("cell_handover_success_rate"),
    E_RAB_RETAINABILITY_PERCENTAGE_LOST("e_rab_retainability_percentage_lost"),
    E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1("e_rab_retainability_percentage_lost_qci1"),
    INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR("initial_and_added_e_rab_establishment_sr"),
    INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT("initial_and_added_e_rab_establishment_sr_for_qci1"),
    SYNTHETIC_COUNTER_CELL_RELIABILITY_DAILY("synthetic_counter_cell_reliability_daily");

    private final String kpiName;

    CellDailyKpis(final String kpiName) {
        this.kpiName = kpiName;
    }

    public String getKpiName() {
        return kpiName;
    }

}

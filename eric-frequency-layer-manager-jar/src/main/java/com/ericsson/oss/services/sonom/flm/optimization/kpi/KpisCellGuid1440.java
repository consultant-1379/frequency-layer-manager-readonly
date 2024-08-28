/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.optimization.kpi;

/**
 * Defines all KPIs in kpi_cell_guid_1440 table.
 */
public enum KpisCellGuid1440 {

    CELL_AVAILABILITY("cell_availability"),
    KPI_CELL_RELIABILITY_DAILY("kpi_cell_reliability_daily");

    private final String kpiName;

    KpisCellGuid1440(final String kpiName) {
        this.kpiName = kpiName;
    }

    public String getKpiName() {
        return kpiName;
    }

}
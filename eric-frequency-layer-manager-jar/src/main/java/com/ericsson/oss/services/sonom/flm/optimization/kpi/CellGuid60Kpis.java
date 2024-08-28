/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
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
 * Defines all KPIs in cell_guid_60_kpis table.
 */
public enum CellGuid60Kpis {

    CONNECTED_USERS("connected_users"),
    DISTANCE_Q1("distance_q1"),
    DISTANCE_Q2("distance_q2"),
    DISTANCE_Q3("distance_q3"),
    DISTANCE_Q4("distance_q4"),
    UE_PERCENTAGE_Q1("ue_percentage_q1"),
    UE_PERCENTAGE_Q2("ue_percentage_q2"),
    UE_PERCENTAGE_Q3("ue_percentage_q3"),
    UE_PERCENTAGE_Q4("ue_percentage_q4"),
    PERCENTAGE_ENDC_USERS("percentage_endc_users"),
    ENDC_SPID115_UES("endc_spid115_ues"),
    UL_PUSCH_SINR_HOURLY("ul_pusch_sinr_hourly"),
    SAMPLES_RSRP_TA_Q1("num_samples_rsrp_ta_q1"),
    SAMPLES_RSRP_TA_Q2("num_samples_rsrp_ta_q2"),
    SAMPLES_RSRP_TA_Q3("num_samples_rsrp_ta_q3"),
    SAMPLES_RSRP_TA_Q4("num_samples_rsrp_ta_q4"),
    BAD_SAMPLES_RSRP_TA_Q1("num_bad_samples_rsrp_ta_q1"),
    BAD_SAMPLES_RSRP_TA_Q2("num_bad_samples_rsrp_ta_q2"),
    BAD_SAMPLES_RSRP_TA_Q3("num_bad_samples_rsrp_ta_q3"),
    BAD_SAMPLES_RSRP_TA_Q4("num_bad_samples_rsrp_ta_q4"),
    BAD_RSRP_PERCENTAGE_Q1("bad_rsrp_percentage_q1"),
    BAD_RSRP_PERCENTAGE_Q2("bad_rsrp_percentage_q2"),
    BAD_RSRP_PERCENTAGE_Q3("bad_rsrp_percentage_q3"),
    BAD_RSRP_PERCENTAGE_Q4("bad_rsrp_percentage_q4");

    private final String kpiName;

    CellGuid60Kpis(final String kpiName) {
        this.kpiName = kpiName;
    }

    public String getKpiName() {
        return kpiName;
    }

}
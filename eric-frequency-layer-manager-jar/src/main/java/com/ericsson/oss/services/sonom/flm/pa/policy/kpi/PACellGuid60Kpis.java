/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.pa.policy.kpi;

import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.RelevanceThresholdType;

/**
 * Defines the PA Cell KPIs.
 */
public enum PACellGuid60Kpis {
    CELL_HANDOVER_SUCCESS_RATE(
            "cell_handover_success_rate_hourly", "cell_handover_success_rate_degradation", "cellHandoverSuccessRate",
            RelevanceThresholdType.MIN),
    INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR(
            "initial_and_added_e_rab_establishment_sr_hourly", "initial_and_added_e_rab_estab_sr_degradation", "initialAndAddedERabEstabSrHourly",
            RelevanceThresholdType.MIN),
    INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI1(
            "initial_and_added_e_rab_establishment_sr_for_qci1_hourly", "initial_and_added_e_rab_estab_sr_qci1_degradation",
            "initialAndAddedERabEstabSrQci1Hourly", RelevanceThresholdType.MIN),
    E_RAB_RETAINABILITY_PERCENTAGE_LOST(
            "e_rab_retainability_percentage_lost_hourly", "e_rab_retainability_percentage_lost_degradation", "eRabRetainabilityPercentageLostHourly",
            RelevanceThresholdType.MAX),
    E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1(
            "e_rab_retainability_percentage_lost_qci1_hourly", "e_rab_retainability_percentage_lost_qci1_degradation",
            "eRabRetainabilityPercentageLostQci1Hourly", RelevanceThresholdType.MAX),
    UPLINK_PUSCH_SINR(
            "ul_pusch_sinr_hourly", "uplink_pusch_sinr_degradation", "ulPuschSinrHourly",
            RelevanceThresholdType.MIN);

    private final String kpiName;
    private final String thresholdName;
    private final String settingName;
    private final RelevanceThresholdType thresholdType;

    PACellGuid60Kpis(final String kpiName, final String thresholdName, final String settingName, final RelevanceThresholdType thresholdType) {
        this.kpiName = kpiName;
        this.thresholdName = thresholdName;
        this.settingName = settingName;
        this.thresholdType = thresholdType;
    }

    public String getKpiName() {
        return kpiName;
    }

    public String getThresholdName() {
        return thresholdName;
    }

    public String getSettingName() {
        return settingName;
    }

    public RelevanceThresholdType getThresholdType() {
        return thresholdType;
    }

    /**
     * Retrieve the PA Cell 60 KPI given the hourly KPI name.
     *
     * @param kpiName
     *            the name of the hourly KPI.
     * @return the PA Cell 60 KPI.
     */
    public static PACellGuid60Kpis getKpiForName(final String kpiName) {
        if (kpiName != null && !kpiName.isEmpty()) {
            for (final PACellGuid60Kpis kpi : values()) {
                if (kpi.getKpiName().equalsIgnoreCase(kpiName)) {
                    return kpi;
                }
            }
        }
        throw new IllegalArgumentException(String.format("Invalid PA Cell KPI name (%s).", kpiName));
    }

    /**
     * Retrieve the PA Cell 60 KPI degradation threshold name given the hourly KPI name.
     *
     * @param kpiName
     *            the name of the degradation threshold KPI.
     * @return the degradation threshold KPI name.
     */
    public static String getThresholdNameForKpi(final String kpiName) {
        return getKpiForName(kpiName).getThresholdName();
    }
}

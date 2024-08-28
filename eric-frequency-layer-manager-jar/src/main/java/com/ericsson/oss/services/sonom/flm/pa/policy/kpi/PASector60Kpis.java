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
package com.ericsson.oss.services.sonom.flm.pa.policy.kpi;

/**
 * Defines the PA Sector KPIs.
 */
public enum PASector60Kpis {
    AVG_DL_PDCP_THROUGHPUT_SECTOR(
            "avg_dl_pdcp_throughput_sector", "avg_dl_pdcp_throughput_sector_degradation", "avgDlPdcpThroughputSector", "0.0", "1200000.0"),
    AVG_UL_PDCP_THROUGHPUT_SECTOR(
            "avg_ul_pdcp_throughput_sector", "avg_ul_pdcp_throughput_sector_degradation", "avgUlPdcpThroughputSector", "0.0", "600000.0");

    private final String kpiName;
    private final String thresholdName;
    private final String settingName;
    private final String lowerRangeLimit;
    private final String upperRangeLimit;

    PASector60Kpis(final String kpiName, final String thresholdName, final String settingName, final String lowerRangeLimit,
            final String upperRangeLimit) {
        this.kpiName = kpiName;
        this.thresholdName = thresholdName;
        this.settingName = settingName;
        this.lowerRangeLimit = lowerRangeLimit;
        this.upperRangeLimit = upperRangeLimit;
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

    public String getLowerRangeLimit() {
        return lowerRangeLimit;
    }

    public String getUpperRangeLimit() {
        return upperRangeLimit;
    }

    /**
     * Retrieve the PA Sector 60 KPI given the hourly KPI name.
     *
     * @param kpiName
     *            the name of the hourly KPI.
     * @return the PA Sector 60 KPI.
     */
    public static PASector60Kpis getKpiForName(final String kpiName) {
        if (kpiName != null && !kpiName.isEmpty()) {
            for (final PASector60Kpis kpi : values()) {
                if (kpi.getKpiName().equalsIgnoreCase(kpiName)) {
                    return kpi;
                }
            }
        }
        throw new IllegalArgumentException(String.format("Invalid PA Sector KPI name (%s).", kpiName));
    }

    /**
     * Retrieve the PA Sector 60 KPI degradation threshold name given the hourly KPI name.
     *
     * @param kpiName
     *            the name of the degradation threshold KPI.
     * @return the degradation threshold KPI name.
     */
    public static String getThresholdNameForKpi(final String kpiName) {
        return getKpiForName(kpiName).getThresholdName();
    }
}

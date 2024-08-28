/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.settings;

/**
 * Holder for cell settings attribute name constants.
 */
public final class CellSettingsAttributeNames {

    // custom global settings
    public static final String QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME = "qosForCapacityEstimation";
    public static final String PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME = "percentileForMaxConnectedUser";
    public static final String MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME = "minNumCellForCDFCalculation";
    public static final String THRESHOLD_TARGET_PUSH_BACK = "targetPushBack";
    public static final String THRESHOLD_OVERRIDE_C_CALCULATOR = "overrideCCalculator";
    public static final String THRESHOLD_MINIMUM_LBDAR_STEPSIZE = "minLbdarStepsize";
    public static final String THRESHOLD_MAXIMUM_LBDAR_STEPSIZE = "maxLbdarStepsize";
    public static final String THRESHOLD_LEAKAGE_THIRD_CELL = "leakageThirdCell";
    public static final String THRESHOLD_LEAKAGE_LBQ_IMPACT = "leakageLbqImpact";
    public static final String THRESHOLD_EXISTING_HIGH_PUSH = "existingHighPush";
    public static final String NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME = "numberOfKpiDegradedHoursThreshold";
    public static final String PA_KPI_SETTINGS = "paKpiSettings";

    // custom default settings
    public static final String TARGET_THROUGHPUT_R_ATTR_NAME = "targetThroughputR(Mbps)";
    public static final String DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME = "deltaGFSOptimizationThreshold";
    public static final String TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME = "targetSourceCoverageBalanceRatioThreshold";
    public static final String SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME = "sourceTargetSamplesOverlapThreshold";
    public static final String TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME = "targetSourceContiguityRatioThreshold";
    public static final String LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME = 
            "loadBalancingThresholdForInitialAndAddedErabEstabSuccRate";
    public static final String LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME = 
            "loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1";
    public static final String LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME = "loadBalancingThresholdForErabPercentageLost";
    public static final String LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME = 
            "loadBalancingThresholdForErabPercentageLostForQci1";
    public static final String LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME = "loadBalancingThresholdForCellHoSuccRate";
    public static final String LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME = "loadBalancingThresholdForCellAvailability";
    public static final String OPTIMIZATION_SPEED_ATTR_NAME = "optimizationSpeed";
    public static final String MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME = "minRopsForAppCovReliability";
    public static final String MINIMUM_SOURCE_RETAINED_NAME = "minimumSourceRetained";
    public static final String MIN_NUM_CQI_SAMPLES_ATTR_NAME = "minNumCqiSamples";
    public static final String MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME = "minNumSamplesForTransientCalculation";
    public static final String SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME = "sigmaForTransientCalculation";
    public static final String LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME = "loadBalancingThresholdForEndcUsers";
    public static final String ENABLE_ESS_SETTING_ATTR_NAME = "essEnabled";
    public static final String UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME = "ulPuschSinrRatioThreshold";
    public static final String MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME = "minTargetUlPuschSinr";
    public static final String PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME = "percentageBadRsrpRatioThreshold";
    public static final String MIN_CONNECTED_USERS_ATTR_NAME = "minConnectedUsers";

    private CellSettingsAttributeNames() {

    }
}

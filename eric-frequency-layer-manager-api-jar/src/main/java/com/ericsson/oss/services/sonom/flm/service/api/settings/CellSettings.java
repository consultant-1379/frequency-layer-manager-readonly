/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.api.settings;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class to store and represent Cell Settings Information.
 */
@JsonPropertyOrder({ "id", "oss_id", "fdn", "qos_for_capacity_estimation", "percentile_for_max_connected_user", "min_num_cell_for_cdf_calculation",
        "target_throughput_r", "delta_gfs_optimization_threshold", "target_source_coverage_balance_ratio_threshold",
        "source_target_samples_overlap_threshold", "target_source_contiguity_ratio_threshold",
        "lbt_for_initial_and_added_erab_estab_succ_rate", "lbt_for_initial_and_added_erab_estab_succ_rate_for_qci1",
        "load_balancing_threshold_for_erab_percentage_lost", "load_balancing_threshold_for_erab_percentage_lost_for_qci1",
        "load_balancing_threshold_for_cell_ho_succ_rate", "load_balancing_threshold_for_cell_availability", "optimization_speed",
        "optimization_speed_factor_table", "bandwidth_to_step_size_table", "loadBalancingThresholdForEndcUsers",
        "essEnabled", "exclusion_list", "minimum_source_retained", "min_rops_for_app_cov_reliability", "min_num_cqi_samples",
        "min_num_samples_for_transient_calculation", "sigma_for_transient_calculation", "uplink_pusch_sinr_ratio_threshold",
        "min_target_uplink_pusch_sinr", "percentage_bad_rsrp_ratio_threshold", "min_connected_users",
        "execution_id", "configuration_id", "sector_id" })
public class CellSettings implements Serializable {

    private static final long serialVersionUID = 7413309072761023129L;

    private Long id;
    private Integer ossId;
    private String fdn;
    private Double qosForCapacityEstimation;
    private Double percentileForMaxConnectedUser;
    private Integer minNumCellForCdfCalculation;
    private Double targetThroughputR;
    private Double deltaGfsOptimizationThreshold;
    private Double targetSourceCoverageBalanceRatioThreshold;
    private Double sourceTargetSamplesOverlapThreshold;
    private Double targetSourceContiguityRatioThreshold;
    private Double loadBalancingThresholdForInitialAndAddedErabEstabSuccRate;
    private Double loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1;
    private Double loadBalancingThresholdForErabPercentageLost;
    private Double loadBalancingThresholdForErabPercentageLostForQci1;
    private Double loadBalancingThresholdForCellHoSuccRate;
    private Double loadBalancingThresholdForCellAvailability;
    private String optimizationSpeed;
    private String optimizationSpeedFactorTable;
    private String bandwidthToStepSizeTable;
    private String exclusionList;
    private Integer minimumSourceRetained;
    private Integer minRopsForAppCovReliability;
    private Integer minNumCqiSamples;
    private Integer minNumSamplesForTransientCalculation;
    private Integer sigmaForTransientCalculation;
    private Double ulPuschSinrRatioThreshold;
    private Integer minTargetUlPuschSinr;
    private Double percentageBadRsrpRatioThreshold;
    private Integer minConnectedUsers;
    private Double loadBalancingThresholdForEndcUsers;
    private Integer numCallsCellHourlyReliabilityThresholdInHours;
    private Integer syntheticCountersCellReliabilityThresholdInRops;
    private Boolean essEnabled;
    private String executionId;
    private Integer configurationId;
    private Long sectorId;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getOssId() {
        return ossId;
    }

    public void setOssId(final Integer ossId) {
        this.ossId = ossId;
    }

    public String getFdn() {
        return fdn;
    }

    public void setFdn(final String fdn) {
        this.fdn = fdn;
    }

    public Double getQosForCapacityEstimation() {
        return qosForCapacityEstimation;
    }

    public void setQosForCapacityEstimation(final Double qosForCapacityEstimation) {
        this.qosForCapacityEstimation = qosForCapacityEstimation;
    }

    public Double getPercentileForMaxConnectedUser() {
        return percentileForMaxConnectedUser;
    }

    public void setPercentileForMaxConnectedUser(final Double percentileForMaxConnectedUser) {
        this.percentileForMaxConnectedUser = percentileForMaxConnectedUser;
    }

    public Integer getMinNumCellForCdfCalculation() {
        return minNumCellForCdfCalculation;
    }

    public void setMinNumCellForCdfCalculation(final Integer minNumCellForCdfCalculation) {
        this.minNumCellForCdfCalculation = minNumCellForCdfCalculation;
    }

    public Double getDeltaGfsOptimizationThreshold() {
        return deltaGfsOptimizationThreshold;
    }

    public void setDeltaGfsOptimizationThreshold(final Double deltaGfsOptimizationThreshold) {
        this.deltaGfsOptimizationThreshold = deltaGfsOptimizationThreshold;
    }

    public String getOptimizationSpeed() {
        return optimizationSpeed;
    }

    public void setOptimizationSpeed(final String optimizationSpeed) {
        this.optimizationSpeed = optimizationSpeed;
    }

    public Double getTargetThroughputR() {
        return targetThroughputR;
    }

    public void setTargetThroughputR(final Double targetthroughputr) {
        targetThroughputR = targetthroughputr;
    }

    public String getOptimizationSpeedFactorTable() {
        return optimizationSpeedFactorTable;
    }

    public void setOptimizationSpeedFactorTable(final String optimizationSpeedFactorTable) {
        this.optimizationSpeedFactorTable = optimizationSpeedFactorTable;
    }

    public String getBandwidthToStepSizeTable() {
        return bandwidthToStepSizeTable;
    }

    public void setBandwidthToStepSizeTable(final String bandwidthToStepSizeTable) {
        this.bandwidthToStepSizeTable = bandwidthToStepSizeTable;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(final String executionId) {
        this.executionId = executionId;
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(final Integer configurationId) {
        this.configurationId = configurationId;
    }

    public Double getTargetSourceCoverageBalanceRatioThreshold() {
        return targetSourceCoverageBalanceRatioThreshold;
    }

    public void setTargetSourceCoverageBalanceRatioThreshold(final Double targetSourceCoverageBalanceRatioThreshold) {
        this.targetSourceCoverageBalanceRatioThreshold = targetSourceCoverageBalanceRatioThreshold;
    }

    public Double getSourceTargetSamplesOverlapThreshold() {
        return sourceTargetSamplesOverlapThreshold;
    }

    public void setSourceTargetSamplesOverlapThreshold(final Double sourceTargetSamplesOverlapThreshold) {
        this.sourceTargetSamplesOverlapThreshold = sourceTargetSamplesOverlapThreshold;
    }

    public Double getTargetSourceContiguityRatioThreshold() {
        return targetSourceContiguityRatioThreshold;
    }

    public void setTargetSourceContiguityRatioThreshold(final Double targetSourceContiguityRatioThreshold) {
        this.targetSourceContiguityRatioThreshold = targetSourceContiguityRatioThreshold;
    }

    public Double getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate() {
        return loadBalancingThresholdForInitialAndAddedErabEstabSuccRate;
    }

    public void setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate(final Double loadBalancingThresholdForInitialAndAddedErabEstabSuccRate) {
        this.loadBalancingThresholdForInitialAndAddedErabEstabSuccRate = loadBalancingThresholdForInitialAndAddedErabEstabSuccRate;
    }

    public Double getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1() {
        return loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1;
    }

    public void setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1(
            final Double loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1) {
        this.loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1 = loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1;
    }

    public Double getLoadBalancingThresholdForErabPercentageLost() {
        return loadBalancingThresholdForErabPercentageLost;
    }

    public void setLoadBalancingThresholdForErabPercentageLost(final Double loadBalancingThresholdForErabPercentageLost) {
        this.loadBalancingThresholdForErabPercentageLost = loadBalancingThresholdForErabPercentageLost;
    }

    public Double getLoadBalancingThresholdForErabPercentageLostForQci1() {
        return loadBalancingThresholdForErabPercentageLostForQci1;
    }

    public void setLoadBalancingThresholdForErabPercentageLostForQci1(final Double loadBalancingThresholdForErabPercentageLostForQci1) {
        this.loadBalancingThresholdForErabPercentageLostForQci1 = loadBalancingThresholdForErabPercentageLostForQci1;
    }

    public Double getLoadBalancingThresholdForCellHoSuccRate() {
        return loadBalancingThresholdForCellHoSuccRate;
    }

    public void setLoadBalancingThresholdForCellHoSuccRate(final Double loadBalancingThresholdForCellHoSuccRate) {
        this.loadBalancingThresholdForCellHoSuccRate = loadBalancingThresholdForCellHoSuccRate;
    }

    public Double getLoadBalancingThresholdForCellAvailability() {
        return loadBalancingThresholdForCellAvailability;
    }

    public void setLoadBalancingThresholdForCellAvailability(final Double loadBalancingThresholdForCellAvailability) {
        this.loadBalancingThresholdForCellAvailability = loadBalancingThresholdForCellAvailability;
    }

    public String getExclusionList() {
        return exclusionList;
    }

    public void setExclusionList(final String exclusionList) {
        this.exclusionList = exclusionList;
    }

    public Integer getMinRopsForAppCovReliability() {
        return minRopsForAppCovReliability;
    }

    public void setMinRopsForAppCovReliability(final Integer minRopsForAppCovReliability) {
        this.minRopsForAppCovReliability = minRopsForAppCovReliability;
    }

    public Integer getMinNumCqiSamples() {
        return minNumCqiSamples;
    }

    public void setMinNumCqiSamples(final Integer minNumCqiSamples) {
        this.minNumCqiSamples = minNumCqiSamples;
    }

    public Integer getMinNumSamplesForTransientCalculation() {
        return minNumSamplesForTransientCalculation;
    }

    public void setMinNumSamplesForTransientCalculation(final Integer minNumSamplesForTransientCalculation) {
        this.minNumSamplesForTransientCalculation = minNumSamplesForTransientCalculation;
    }

    public Integer getSigmaForTransientCalculation() {
        return sigmaForTransientCalculation;
    }

    public void setSigmaForTransientCalculation(final Integer sigmaForTransientCalculation) {
        this.sigmaForTransientCalculation = sigmaForTransientCalculation;
    }

    public Double getUlPuschSinrRatioThreshold() {
        return ulPuschSinrRatioThreshold;
    }

    public void setUlPuschSinrRatioThreshold(final Double ulPuschSinrRatioThreshold) {
        this.ulPuschSinrRatioThreshold = ulPuschSinrRatioThreshold;
    }

    public Integer getMinTargetUlPuschSinr() {
        return minTargetUlPuschSinr;
    }

    public void setMinTargetUlPuschSinr(final Integer minTargetUlPuschSinr) {
        this.minTargetUlPuschSinr = minTargetUlPuschSinr;
    }

    public Double getPercentageBadRsrpRatioThreshold() {
        return percentageBadRsrpRatioThreshold;
    }

    public void setPercentageBadRsrpRatioThreshold(final Double percentageBadRsrpRatioThreshold) {
        this.percentageBadRsrpRatioThreshold = percentageBadRsrpRatioThreshold;
    }

    public Double getloadBalancingThresholdForEndcUsers() {
        return loadBalancingThresholdForEndcUsers;
    }

    public void setloadBalancingThresholdForEndcUsers(final Double loadBalancingThresholdForEndcUsers) {
        this.loadBalancingThresholdForEndcUsers = loadBalancingThresholdForEndcUsers;
    }

    public Boolean getEssEnabled() {
        return essEnabled;
    }

    public void setEssEnabled(final Boolean essEnabled) {
        this.essEnabled = essEnabled;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(final Long sectorId) {
        this.sectorId = sectorId;
    }

    public Integer getNumCallsCellHourlyReliabilityThresholdInHours() {
        return numCallsCellHourlyReliabilityThresholdInHours;
    }

    public void setNumCallsCellHourlyReliabilityThresholdInHours(final Integer numCallsCellHourlyReliabilityThresholdInHours) {
        this.numCallsCellHourlyReliabilityThresholdInHours = numCallsCellHourlyReliabilityThresholdInHours;
    }

    public Integer getSyntheticCountersCellReliabilityThresholdInRops() {
        return syntheticCountersCellReliabilityThresholdInRops;
    }

    public void setSyntheticCountersCellReliabilityThresholdInRops(final Integer syntheticCountersCellReliabilityThresholdInRops) {
        this.syntheticCountersCellReliabilityThresholdInRops = syntheticCountersCellReliabilityThresholdInRops;
    }

    public Integer getMinConnectedUsers() {
        return minConnectedUsers;
    }

    public void setMinConnectedUsers(final Integer minConnectedUsers) {
        this.minConnectedUsers = minConnectedUsers;
    }

    public Integer getMinimumSourceRetained() {
        return minimumSourceRetained;
    }

    public void setMinimumSourceRetained(final Integer minimumSourceRetained) {
        this.minimumSourceRetained = minimumSourceRetained;
    }

    @Override
    public String toString() {
        final String quote = "'";
        return new StringJoiner(", ", CellSettings.class.getSimpleName() + ":: {", "}")
                .add("id=" + id)
                .add("ossId=" + ossId)
                .add("fdn= '" + fdn + quote)
                .add("qosForCapacityEstimation= '" + qosForCapacityEstimation + quote)
                .add("percentileForMaxConnectedUser= '" + percentileForMaxConnectedUser + quote)
                .add("minNumCellForCdfCalculation= '" + minNumCellForCdfCalculation + quote)
                .add("targetThroughputR= '" + targetThroughputR + quote)
                .add("deltaGfsOptimizationThreshold= '" + deltaGfsOptimizationThreshold + quote)
                .add("targetSourceCoverageBalanceRatioThreshold= '" + targetSourceCoverageBalanceRatioThreshold + quote)
                .add("sourceTargetSamplesOverlapThreshold= '" + sourceTargetSamplesOverlapThreshold + quote)
                .add("targetSourceContiguityRatioThreshold= '" + targetSourceContiguityRatioThreshold + quote)
                .add("loadBalancingThresholdForInitialAndAddedErabEstabSuccRate= '" + loadBalancingThresholdForInitialAndAddedErabEstabSuccRate
                        + quote)
                .add("loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1= '"
                        + loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1 + quote)
                .add("loadBalancingThresholdForErabPercentageLost= '" + loadBalancingThresholdForErabPercentageLost + quote)
                .add("loadBalancingThresholdForErabPercentageLostForQci1= '" + loadBalancingThresholdForErabPercentageLostForQci1 + quote)
                .add("loadBalancingThresholdForCellHoSuccRate= '" + loadBalancingThresholdForCellHoSuccRate + quote)
                .add("loadBalancingThresholdForCellAvailability= '" + loadBalancingThresholdForCellAvailability + quote)
                .add("optimizationSpeed= ''" + optimizationSpeed + quote + quote)
                .add("optimizationSpeedFactorTable= ''" + optimizationSpeedFactorTable + quote + quote)
                .add("bandwidthToStepSizeTable= ''" + bandwidthToStepSizeTable + quote + quote)
                .add("loadBalancingThresholdForEndcUsers= '" + loadBalancingThresholdForEndcUsers + quote)
                .add("numCallsCellHourlyReliabilityThresholdInHours= '" + numCallsCellHourlyReliabilityThresholdInHours + quote)
                .add("syntheticCountersCellReliabilityThresholdInRops= '" + syntheticCountersCellReliabilityThresholdInRops + quote)
                .add("essEnabled= ''" + essEnabled + quote + quote)
                .add("exclusionList= ''" + exclusionList + quote + quote)
                .add("executionId= ''" + executionId + quote + quote)
                .add("minimumSourceRetained= ''" + minimumSourceRetained + quote + quote)
                .add("minRopsForAppCovReliability= ''" + minRopsForAppCovReliability + quote + quote)
                .add("minNumCqiSamples= ''" + minNumCqiSamples + quote + quote)
                .add("minNumSamplesForTransientCalculation= ''" + minNumSamplesForTransientCalculation + quote + quote)
                .add("sigmaForTransientCalculation= ''" + sigmaForTransientCalculation + quote + quote)
                .add("ulPuschSinrRatioThreshold= ''" + ulPuschSinrRatioThreshold + quote + quote)
                .add("minTargetUlPuschSinr= ''" + minTargetUlPuschSinr + quote + quote)
                .add("percentageBadRsrpRatioThreshold= ''" + percentageBadRsrpRatioThreshold + quote + quote)
                .add("minConnectedUsers= ''" + minConnectedUsers + quote + quote)
                .add("configurationId= '" + configurationId + quote)
                .add("sectorId= ''" + sectorId + quote)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CellSettings that = (CellSettings) o;
        return Objects.equals(id, that.id)
                && Objects.equals(ossId, that.ossId)
                && Objects.equals(fdn, that.fdn)
                && Objects.equals(qosForCapacityEstimation, that.qosForCapacityEstimation)
                && Objects.equals(percentileForMaxConnectedUser, that.percentileForMaxConnectedUser)
                && Objects.equals(minNumCellForCdfCalculation, that.minNumCellForCdfCalculation)
                && Objects.equals(targetThroughputR, that.targetThroughputR)
                && Objects.equals(deltaGfsOptimizationThreshold, that.deltaGfsOptimizationThreshold)
                && Objects.equals(targetSourceCoverageBalanceRatioThreshold, that.targetSourceCoverageBalanceRatioThreshold)
                && Objects.equals(sourceTargetSamplesOverlapThreshold, that.sourceTargetSamplesOverlapThreshold)
                && Objects.equals(targetSourceContiguityRatioThreshold, that.targetSourceContiguityRatioThreshold)
                && Objects.equals(loadBalancingThresholdForInitialAndAddedErabEstabSuccRate,
                        that.loadBalancingThresholdForInitialAndAddedErabEstabSuccRate)
                && Objects.equals(loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1,
                        that.loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1)
                && Objects.equals(loadBalancingThresholdForErabPercentageLost, that.loadBalancingThresholdForErabPercentageLost)
                && Objects.equals(loadBalancingThresholdForErabPercentageLostForQci1, that.loadBalancingThresholdForErabPercentageLostForQci1)
                && Objects.equals(loadBalancingThresholdForCellHoSuccRate, that.loadBalancingThresholdForCellHoSuccRate)
                && Objects.equals(loadBalancingThresholdForCellAvailability, that.loadBalancingThresholdForCellAvailability)
                && Objects.equals(optimizationSpeed, that.optimizationSpeed)
                && Objects.equals(optimizationSpeedFactorTable, that.optimizationSpeedFactorTable)
                && Objects.equals(bandwidthToStepSizeTable, that.bandwidthToStepSizeTable)
                && Objects.equals(loadBalancingThresholdForEndcUsers, that.loadBalancingThresholdForEndcUsers)
                && Objects.equals(numCallsCellHourlyReliabilityThresholdInHours, that.numCallsCellHourlyReliabilityThresholdInHours)
                && Objects.equals(syntheticCountersCellReliabilityThresholdInRops, that.syntheticCountersCellReliabilityThresholdInRops)
                && Objects.equals(essEnabled, that.essEnabled)
                && Objects.equals(exclusionList, that.exclusionList)
                && Objects.equals(minimumSourceRetained, that.minimumSourceRetained)
                && Objects.equals(minRopsForAppCovReliability, that.minRopsForAppCovReliability)
                && Objects.equals(minNumCqiSamples, that.minNumCqiSamples)
                && Objects.equals(minNumSamplesForTransientCalculation, that.minNumSamplesForTransientCalculation)
                && Objects.equals(sigmaForTransientCalculation, that.sigmaForTransientCalculation)
                && Objects.equals(ulPuschSinrRatioThreshold, that.ulPuschSinrRatioThreshold)
                && Objects.equals(minTargetUlPuschSinr, that.minTargetUlPuschSinr)
                && Objects.equals(percentageBadRsrpRatioThreshold, that.percentageBadRsrpRatioThreshold)
                && Objects.equals(minConnectedUsers, that.minConnectedUsers)
                && Objects.equals(executionId, that.executionId)
                && Objects.equals(configurationId, that.configurationId)
                && Objects.equals(sectorId, that.sectorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ossId, fdn, qosForCapacityEstimation, percentileForMaxConnectedUser, minNumCellForCdfCalculation,
                targetThroughputR, deltaGfsOptimizationThreshold, targetSourceCoverageBalanceRatioThreshold, sourceTargetSamplesOverlapThreshold,
                targetSourceContiguityRatioThreshold, loadBalancingThresholdForInitialAndAddedErabEstabSuccRate,
                loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1, loadBalancingThresholdForErabPercentageLost,
                loadBalancingThresholdForErabPercentageLostForQci1, loadBalancingThresholdForCellHoSuccRate,
                loadBalancingThresholdForCellAvailability, optimizationSpeed, optimizationSpeedFactorTable, bandwidthToStepSizeTable,
                loadBalancingThresholdForEndcUsers, numCallsCellHourlyReliabilityThresholdInHours, syntheticCountersCellReliabilityThresholdInRops,
                essEnabled, exclusionList, minimumSourceRetained, minRopsForAppCovReliability, minNumCqiSamples, minNumSamplesForTransientCalculation,
                sigmaForTransientCalculation, ulPuschSinrRatioThreshold, minTargetUlPuschSinr, percentageBadRsrpRatioThreshold, minConnectedUsers,
                executionId, configurationId, sectorId);
    }
}

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
var cellWithValidData =
    {
         "fdn": "SubNetwork=ONRM_ROOT_MO,SubNetwork=SubNet1,MeContext=Athlone1,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=001",
         "ossId":1,
         "kpis": {
            "p_failing_r_mbps": "0.009699715397572167",
            "target_cell_capacity": "0.7",
            "goal_function_resource_efficiency": "2",
            "unhappy_users": "2.0",
            "contiguity": "100",
            "coverage_balance_ratio_distance": "5.4",
            "distance_q1": "1.2",
            "distance_q2": "3.1",
            "distance_q3": "5.31",
            "distance_q4": "7.25",
            "ue_percentage_q1": "25",
            "ue_percentage_q2": "12",
            "ue_percentage_q3": "40",
            "ue_percentage_q4": "23",
            "connected_users": "15",
            "app_coverage_reliability": "true",
            "kpi_cell_reliability_daily": "1"
          },
          "settings": {
            "target_throughput_r": "5.0",
            "delta_gfs_optimization_threshold": "0.3",
            "percentile_for_max_connected_user": "40.0",
            "min_num_cell_for_cdf_calculation": "20.0",
            "qos_for_capacity_estimation": "0.5",
            "target_source_coverage_balance_ratio_threshold": "0.9",
            "source_target_samples_overlap_threshold": "70",
            "target_source_contiguity_ratio_threshold": "0.9",
            "lb_threshold_for_initial_erab_estab_succ_rate": "98",
            "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1": "98.5",
            "lb_threshold_for_erab_percentage_lost": "2",
            "lb_threshold_for_erab_percentage_lost_for_qci1": "1.5",
            "lb_threshold_for_cell_ho_succ_rate": "70",
            "lb_threshold_for_cell_availability": "70",
            "optimization_speed_factor_table": "slow=6, normal=4, fast=2",
            "bandwidth_to_step_size_table": "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10",
            "num_calls_cell_hourly_reliability_threshold_in_hours": "1"
          },
          "cmAttributes": {
            "bandwidth": "5000",
            "caimc": "undefined"
          }
    };

// Define cells with missing kpis
var cellWithMissingMandatoryKpiPFailingRMbps = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiPFailingRMbps.kpis.p_failing_r_mbps;
var cellWithMissingMandatoryKpiGoalFunctionResourceEfficiency = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiGoalFunctionResourceEfficiency.kpis.goal_function_resource_efficiency;
var cellWithMissingMandatoryKpiUnhappyUsers = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiUnhappyUsers.kpis.unhappy_users;
var cellWithMissingMandatoryKpiContiguity = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiContiguity.kpis.contiguity;
var cellWithMissingMandatoryKpiCoverageBalanceRatioDistance = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiCoverageBalanceRatioDistance.kpis.coverage_balance_ratio_distance;
var cellWithMissingMandatoryKpiDistanceQ1 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiDistanceQ1.kpis.distance_q1;
var cellWithMissingMandatoryKpiDistanceQ2 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiDistanceQ2.kpis.distance_q2;
var cellWithMissingMandatoryKpiDistanceQ3 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiDistanceQ3.kpis.distance_q3;
var cellWithMissingMandatoryKpiDistanceQ4 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiDistanceQ4.kpis.distance_q4;
var cellWithMissingMandatoryKpiUePercentageQ1 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiUePercentageQ1.kpis.ue_percentage_q1;
var cellWithMissingMandatoryKpiUePercentageQ2 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiUePercentageQ2.kpis.ue_percentage_q2;
var cellWithMissingMandatoryKpiUePercentageQ3 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiUePercentageQ3.kpis.ue_percentage_q3;
var cellWithMissingMandatoryKpiUePercentageQ4 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiUePercentageQ4.kpis.ue_percentage_q4;
var cellWithMissingMandatoryKpiConnectedUsers = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiConnectedUsers.kpis.connected_users;
var cellWithMissingMandatoryKpiAppCoverageReliability = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingMandatoryKpiAppCoverageReliability.kpis.app_coverage_reliability;

// Define cells with empty kpis
var cellWithNullMandatoryKpiPFailingRMbps = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiPFailingRMbps.kpis.p_failing_r_mbps = "null";
var cellWithNullMandatoryKpiGoalFunctionResourceEfficiency = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiGoalFunctionResourceEfficiency.kpis.goal_function_resource_efficiency = "null";
var cellWithNullMandatoryKpiUnhappyUsers = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiUnhappyUsers.kpis.unhappy_users = "null";
var cellWithNullMandatoryKpiContiguity = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiContiguity.kpis.contiguity = "null";
var cellWithNullMandatoryKpiCoverageBalanceRatioDistance = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiCoverageBalanceRatioDistance.kpis.coverage_balance_ratio_distance = "null";
var cellWithNullMandatoryKpiDistanceQ1 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiDistanceQ1.kpis.distance_q1 = "null";
var cellWithNullMandatoryKpiDistanceQ2 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiDistanceQ2.kpis.distance_q2 = "null";
var cellWithNullMandatoryKpiDistanceQ3 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiDistanceQ3.kpis.distance_q3 = "null";
var cellWithNullMandatoryKpiDistanceQ4 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiDistanceQ4.kpis.distance_q4 = "null";
var cellWithNullMandatoryKpiUePercentageQ1 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiUePercentageQ1.kpis.eu_ue_percentage_q1 = "null";
var cellWithNullMandatoryKpiUePercentageQ2 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiUePercentageQ2.kpis.eu_ue_percentage_q2 = "null";
var cellWithNullMandatoryKpiUePercentageQ3 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiUePercentageQ3.kpis.eu_ue_percentage_q3 = "null";
var cellWithNullMandatoryKpiUePercentageQ4 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiUePercentageQ4.kpis.eu_ue_percentage_q4 = "null";
var cellWithNullMandatoryKpiConnectedUsers = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiConnectedUsers.kpis.connected_users = "null";
var cellWithNullMandatoryKpiAppCoverageReliability = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullMandatoryKpiAppCoverageReliability.kpis.app_coverage_reliability = "null";

// Define cells with missing settings
var cellWithMissingSettingTargetThroughputR = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingTargetThroughputR.settings.target_throughput_r;
var cellWithMissingSettingDeltaGfsOptimizationThreshold= JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingDeltaGfsOptimizationThreshold.settings.delta_gfs_optimization_threshold;
var cellWithMissingSettingPercentileForMaxConnectedUser= JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingPercentileForMaxConnectedUser.settings.percentile_for_max_connected_user;
var cellWithMissingSettingMinNumCellForCdfCalculation= JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingMinNumCellForCdfCalculation.settings.min_num_cell_for_cdf_calculation;
var cellWithMissingSettingQosForCapacityEstimation= JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingQosForCapacityEstimation.settings.qos_for_capacity_estimation;

var cellWithMissingSettingTargetSourceCoverageBalanceRatioThreshold = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingTargetSourceCoverageBalanceRatioThreshold.settings.target_source_coverage_balance_ratio_threshold;
var cellWithMissingSettingSourceTargetSamplesOverlapThreshold = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingSourceTargetSamplesOverlapThreshold.settings.source_target_samples_overlap_threshold;
var cellWithMissingSettingTargetSourceContiguityRatioThreshold = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingTargetSourceContiguityRatioThreshold.settings.target_source_contiguity_ratio_threshold;
var cellWithMissingSettingLBThresholdForInitialAndAddedErabEstabSuccRate = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingLBThresholdForInitialAndAddedErabEstabSuccRate.settings.lb_threshold_for_initial_erab_estab_succ_rate;
var cellWithMissingSettingLBThresholdForInitialAndAddedErabEstabSuccRateForQci1 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingLBThresholdForInitialAndAddedErabEstabSuccRateForQci1.settings.lb_threshold_for_initial_erab_estab_succ_rate_for_qci1;
var cellWithMissingSettingLBThresholdForErabPercentageLost = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingLBThresholdForErabPercentageLost.settings.lb_threshold_for_erab_percentage_lost;
var cellWithMissingSettingLBThresholdForErabPercentageLostForQci1 = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingLBThresholdForErabPercentageLostForQci1.settings.lb_threshold_for_erab_percentage_lost_for_qci1;
var cellWithMissingSettingLBThresholdForCellHoSuccRate = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingLBThresholdForCellHoSuccRate.settings.lb_threshold_for_cell_ho_succ_rate;
var cellWithMissingSettingLBThresholdForCellAvailability = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingLBThresholdForCellAvailability.settings.lb_threshold_for_cell_availability;
var cellWithMissingSettingOptimizationSpeedFactorTable = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingOptimizationSpeedFactorTable.settings.optimization_speed_factor_table;
var cellWithMissingSettingBandwidthToStepSizeTable = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingSettingBandwidthToStepSizeTable.settings.bandwidth_to_step_size_table;

// Define cells with empty settings
var cellWithNullSettingTargetThroughputR = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingTargetThroughputR.settings.target_throughput_r = "null";
var cellWithNullSettingDeltaGfsOptimizationThreshold = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingDeltaGfsOptimizationThreshold.settings.delta_gfs_optimization_threshold = "null";
var cellWithNullSettingPercentileForMaxConnectedUser = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingPercentileForMaxConnectedUser.settings.percentile_for_max_connected_user = "null";
var cellWithNullSettingMinNumCellForCdfCalculation = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingMinNumCellForCdfCalculation.settings.min_num_cell_for_cdf_calculation = "null";
var cellWithNullSettingQosForCapacityEstimation = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingQosForCapacityEstimation.settings.qos_for_capacity_estimation = "null";

var cellWithNullSettingTargetSourceCoverageBalanceRatioThreshold = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingTargetSourceCoverageBalanceRatioThreshold.settings.target_source_coverage_balance_ratio_threshold = "null";
var cellWithNullSettingSourceTargetSamplesOverlapThreshold = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingSourceTargetSamplesOverlapThreshold.settings.source_target_samples_overlap_threshold = "null";
var cellWithNullSettingTargetSourceContiguityRatioThreshold = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingTargetSourceContiguityRatioThreshold.settings.target_source_contiguity_ratio_threshold = "null";
var cellWithNullSettingLBThresholdForInitialAndAddedErabEstabSuccRate = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingLBThresholdForInitialAndAddedErabEstabSuccRate.settings.lb_threshold_for_initial_erab_estab_succ_rate = "null";
var cellWithNullSettingLBThresholdForInitialAndAddedErabEstabSuccRateForQci1 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingLBThresholdForInitialAndAddedErabEstabSuccRateForQci1.settings.lb_threshold_for_initial_erab_estab_succ_rate_for_qci1 = "null";
var cellWithNullSettingLBThresholdForErabPercentageLost = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingLBThresholdForErabPercentageLost.settings.lb_threshold_for_erab_percentage_lost = "null";
var cellWithNullSettingLBThresholdForErabPercentageLostForQci1 = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingLBThresholdForErabPercentageLostForQci1.settings.lb_threshold_for_erab_percentage_lost_for_qci1 = "null";
var cellWithNullSettingLBThresholdForCellHoSuccRate = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingLBThresholdForCellHoSuccRate.settings.lb_threshold_for_cell_ho_succ_rate = "null";
var cellWithNullSettingLBThresholdForCellAvailability = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingLBThresholdForCellAvailability.settings.lb_threshold_for_cell_availability = "null";
var cellWithNullSettingOptimizationSpeedFactorTable = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingOptimizationSpeedFactorTable.settings.optimization_speed_factor_table = "null";
var cellWithNullSettingBandwidthToStepSizeTable = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullSettingBandwidthToStepSizeTable.settings.bandwidth_to_step_size_table = "null";

// Define cell with missing CM attributes
var cellWithMissingCmData = JSON.parse(JSON.stringify(cellWithValidData));
delete cellWithMissingCmData.cmAttributes.bandwidth;

// Define cell with empty CM attributes
var cellWithNullCmData = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullCmData.cmAttributes.bandwidth = "null";

var cellWithNullCaimcData = JSON.parse(JSON.stringify(cellWithValidData));
cellWithNullCaimcData.cmAttributes.caimc = "null";

var testEmptyMissingValues = [
    {
        description: "Input Event screener proceeds with optimization when there are no empty or missing values",
        result: [
                cellWithValidData
            ],
        data: [
            cellWithValidData
        ]
    },
    {
        description: "When one of optimization cells is received with one of the required kpis missing, then the cell is excluded from the optimization.",
        result:  [],
        data: [
            cellWithMissingMandatoryKpiPFailingRMbps,
            cellWithMissingMandatoryKpiGoalFunctionResourceEfficiency,
            cellWithMissingMandatoryKpiUnhappyUsers,
            cellWithMissingMandatoryKpiContiguity,
            cellWithMissingMandatoryKpiCoverageBalanceRatioDistance,
            cellWithMissingMandatoryKpiDistanceQ1,
            cellWithMissingMandatoryKpiDistanceQ2,
            cellWithMissingMandatoryKpiDistanceQ3,
            cellWithMissingMandatoryKpiDistanceQ4,
            cellWithMissingMandatoryKpiConnectedUsers,
            cellWithMissingMandatoryKpiAppCoverageReliability
        ]
    },
    {
        description: "When one of the optimization cells is received with a null value for one of the required kpis, then the cell is excluded from the optimization.",
        result:  [],
        data: [
            cellWithNullMandatoryKpiPFailingRMbps,
            cellWithNullMandatoryKpiGoalFunctionResourceEfficiency,
            cellWithNullMandatoryKpiUnhappyUsers,
            cellWithNullMandatoryKpiContiguity,
            cellWithNullMandatoryKpiCoverageBalanceRatioDistance,
            cellWithNullMandatoryKpiDistanceQ1,
            cellWithNullMandatoryKpiDistanceQ2,
            cellWithNullMandatoryKpiDistanceQ3,
            cellWithNullMandatoryKpiDistanceQ4,
            cellWithNullMandatoryKpiConnectedUsers,
            cellWithNullMandatoryKpiAppCoverageReliability
        ]
    },
    {
        description: "When one of the optimization cells is received with one of the required settings missing, then the cell is excluded from the optimization.",
        result:  [],
        data: [
            cellWithMissingSettingTargetThroughputR,
            cellWithMissingSettingDeltaGfsOptimizationThreshold,
            cellWithMissingSettingPercentileForMaxConnectedUser,
            cellWithMissingSettingMinNumCellForCdfCalculation,
            cellWithMissingSettingQosForCapacityEstimation,
            cellWithMissingSettingTargetSourceCoverageBalanceRatioThreshold,
            cellWithMissingSettingSourceTargetSamplesOverlapThreshold,
            cellWithMissingSettingTargetSourceContiguityRatioThreshold,
            cellWithMissingSettingLBThresholdForInitialAndAddedErabEstabSuccRate,
            cellWithMissingSettingLBThresholdForInitialAndAddedErabEstabSuccRateForQci1,
            cellWithMissingSettingLBThresholdForErabPercentageLost,
            cellWithMissingSettingLBThresholdForErabPercentageLostForQci1,
            cellWithMissingSettingLBThresholdForCellHoSuccRate,
            cellWithMissingSettingLBThresholdForCellAvailability,
            cellWithMissingSettingOptimizationSpeedFactorTable,
            cellWithMissingSettingBandwidthToStepSizeTable
        ]
    },
    {
        description: "When one of the optimization cells is received with a null value for one of the required settings, then the cell is excluded from the optimization.",
        result:  [],
        data: [
            cellWithNullSettingTargetThroughputR,
            cellWithNullSettingDeltaGfsOptimizationThreshold,
            cellWithNullSettingPercentileForMaxConnectedUser,
            cellWithNullSettingMinNumCellForCdfCalculation,
            cellWithNullSettingQosForCapacityEstimation,
            cellWithNullSettingTargetSourceCoverageBalanceRatioThreshold,
            cellWithNullSettingSourceTargetSamplesOverlapThreshold,
            cellWithNullSettingTargetSourceContiguityRatioThreshold,
            cellWithNullSettingLBThresholdForInitialAndAddedErabEstabSuccRate,
            cellWithNullSettingLBThresholdForInitialAndAddedErabEstabSuccRateForQci1,
            cellWithNullSettingLBThresholdForErabPercentageLost,
            cellWithNullSettingLBThresholdForErabPercentageLostForQci1,
            cellWithNullSettingLBThresholdForCellHoSuccRate,
            cellWithNullSettingLBThresholdForCellAvailability,
            cellWithNullSettingOptimizationSpeedFactorTable,
            cellWithNullSettingBandwidthToStepSizeTable
        ]
    },
    {
        description: "When one of the optimization cells is received with one of the required cm data missing, then the cell is excluded from the optimization.",
        result:  [],
        data: [
            cellWithMissingCmData
        ]
    },
    {
        description: "When one of the optimization cells is received with a null value for one of the required cm data, then the cell is excluded from the optimization.",
        result:  [],
        data: [
            cellWithNullCmData
        ]
    },
    {
            description: "When one of the optimization cells is received with a null value for one of the required cm data, then the cell is excluded from the optimization.",
            result:  [],
            data: [
                cellWithNullCaimcData
            ]
        }
];

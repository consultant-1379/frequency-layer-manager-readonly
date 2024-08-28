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

var OptimizationField = (function () {

 var REQUIRED_KPIS = ["p_failing_r_mbps",
                      "goal_function_resource_efficiency",
                      "unhappy_users",
                      "contiguity",
                      "coverage_balance_ratio_distance",
                      "distance_q1",
                      "distance_q2",
                      "distance_q3",
                      "distance_q4",
                      "ue_percentage_q1",
                      "ue_percentage_q2",
                      "ue_percentage_q3",
                      "ue_percentage_q4",
                      "connected_users",
                      "app_coverage_reliability",
                      "kpi_cell_reliability_daily"];

 var REQUIRED_SETTINGS = ["target_throughput_r",
                          "delta_gfs_optimization_threshold",
                          "percentile_for_max_connected_user",
                          "min_num_cell_for_cdf_calculation",
                          "qos_for_capacity_estimation",
                          "target_source_coverage_balance_ratio_threshold",
                          "source_target_samples_overlap_threshold",
                          "target_source_contiguity_ratio_threshold",
                          "lb_threshold_for_initial_erab_estab_succ_rate",
                          "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1",
                          "lb_threshold_for_erab_percentage_lost",
                          "lb_threshold_for_erab_percentage_lost_for_qci1",
                          "lb_threshold_for_cell_ho_succ_rate",
                          "lb_threshold_for_cell_availability",
                          "optimization_speed_factor_table",
                          "bandwidth_to_step_size_table",
                          "num_calls_cell_hourly_reliability_threshold_in_hours"];

 var REQUIRED_CM_ATTRIBUTES = ["bandwidth","caimc"];

 return {
    REQUIRED_KPIS : REQUIRED_KPIS,
    REQUIRED_SETTINGS : REQUIRED_SETTINGS,
    REQUIRED_CM_ATTRIBUTES : REQUIRED_CM_ATTRIBUTES
 };
})();

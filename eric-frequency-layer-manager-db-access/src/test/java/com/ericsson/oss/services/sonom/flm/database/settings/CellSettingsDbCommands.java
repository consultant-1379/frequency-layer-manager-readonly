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
package com.ericsson.oss.services.sonom.flm.database.settings;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsWithOverlapInfoDaoImplTest;

/**
 * Class which contains SQL queries for the {@link OptimizationsWithOverlapInfoDaoImplTest} unit tests.
 */
public final class CellSettingsDbCommands {
    private CellSettingsDbCommands() {

    }

    public static List<String> createTable(final String table) {
        return Collections.singletonList(String.format("CREATE TABLE IF NOT EXISTS %s(%n" +
                "    id bigint PRIMARY KEY,%n" +
                "    oss_id integer NOT NULL,%n" +
                "    fdn varchar NOT NULL,%n" +
                "    execution_id VARCHAR(255) NOT NULL,%n" +
                "    configuration_id int NOT NULL,%n" +
                "    qos_for_capacity_estimation float NOT NULL,%n" +
                "    percentile_for_max_connected_user integer NOT NULL,%n" +
                "    min_num_cell_for_cdf_calculation integer NOT NULL,%n" +
                "    target_throughput_r float NOT NULL,%n" +
                "    delta_gfs_optimization_threshold float NOT NULL,%n" +
                "    target_source_coverage_balance_ratio_threshold float NOT NULL,%n" +
                "    source_target_samples_overlap_threshold float NOT NULL,%n" +
                "    target_source_contiguity_ratio_threshold float NOT NULL,%n" +
                "    lb_threshold_for_initial_erab_estab_succ_rate float NOT NULL,%n" +
                "    lb_threshold_for_initial_erab_estab_succ_rate_for_qci1 float NOT NULL,%n" +
                "    lb_threshold_for_erab_percentage_lost float NOT NULL,%n" +
                "    lb_threshold_for_erab_percentage_lost_for_qci1 float NOT NULL,%n" +
                "    lb_threshold_for_cell_ho_succ_rate float NOT NULL,%n" +
                "    lb_threshold_for_cell_availability float NOT NULL,%n" +
                "    optimization_speed VARCHAR(6) NOT NULL,%n" +
                "    optimization_speed_factor_table VARCHAR (30) NOT NULL,%n" +
                "    bandwidth_to_step_size_table VARCHAR (60) NOT NULL,%n" +
                "    lb_threshold_for_endc_users float NOT NULL,%n" +
                "    ess_enabled BOOLEAN NOT NULL,%n" +
                "    num_calls_cell_hourly_reliability_threshold_in_hours integer NOT NULL,%n" +
                "    synthetic_counters_cell_reliability_threshold_in_rops integer NOT NULL,%n" +
                "    exclusion_list text,%n" +
                "    min_rops_for_app_cov_reliability int NOT NULL,%n" +
                "    min_num_cqi_samples int NOT NULL,%n" +
                "    min_num_samples_for_transient_calculation int NOT NULL,%n" +
                "    sigma_for_transient_calculation int NOT NULL,%n" +
                "    sector_id bigint%n" +
                ");", table));
    }
}

/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.test.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;

/**
 * Data defined for the Policy Cucumber integration tests. The following sectors are used in the different policy states. Screened out sectors are not
 * available to other states. gfs_screener - sector_with_one_cell (sector screened out) - delta_gfs_difference_below_threshold (sector screened out) -
 * delta_gfs_difference_above_threshold (sector screened out) - delta_gfs_difference_equal_to_threshold (sector screened out) -
 * delta_gfs_inconsistent_target_throughput_r (sector screened out) - delta_gfs_inconsistent_threshold (sector screened out) -
 * delta_gfs_and_target_throughput_r_inconsistent (sector screened out) IdentifySourceAndTargetCells -
 * Imbalanced_Sector_with_2_Cells_173290088340418268 - Imbalanced_Sector_with_4_Cells_173290459927812150 -
 * Balanced_Sector_with_4_Cells_173290409770210514 - Imbalanced_Sector_with_6_Cells_173290089656102500 Contiguity_screener -
 * Imbalanced_Sector_with_4_Cells_173290089656102501 - Imbalanced_Sector_with_4_Cells_173290089656102502 -
 * Imbalanced_Sector_with_4_Cells_173290089656102503 - Imbalanced_Sector_with_4_Cells_173290459927812950 -
 * Imbalanced_Sector_with_6_Cells_173290089656102900 CoverageBalanceRatioDistanceRatio_screener - Imbalanced_Sector_with_6_Cells_173290089656102600 -
 * Imbalanced_Sector_with_4_Cells_173290459927812850 - Imbalanced_Sector_with_4_Cells_173290089656102802 -
 * Imbalanced_Sector_with_6_Cells_173290089656102800 (sector screened out) - Imbalanced_Sector_with_2_Cells_173290088340418968 (sector screened out)
 */

public final class CucumberData {

    private final static Map<String, String> MANDATORY_KPIS = new HashMap<>(17);
    private final static Map<String, String> OPTIONAL_KPIS = new HashMap<>(8);

    private final static Map<String, String> MANDATORY_SETTINGS = new HashMap<>(21);
    private final static Map<String, String> MANDATORY_CM_ATTRIBUTES = new HashMap<>(2);

    static {
        MANDATORY_KPIS.put("p_failing_r_mbps", "0.1");
        MANDATORY_KPIS.put("goal_function_resource_efficiency", "0.64018373040423");
        MANDATORY_KPIS.put("unhappy_users", "10");
        MANDATORY_KPIS.put("contiguity", "100");
        MANDATORY_KPIS.put("coverage_balance_ratio_distance", "100.0");
        MANDATORY_KPIS.put("distance_q1", "2.5");
        MANDATORY_KPIS.put("distance_q2", "5");
        MANDATORY_KPIS.put("distance_q3", "7.5");
        MANDATORY_KPIS.put("distance_q4", "10");
        MANDATORY_KPIS.put("connected_users", "21");
        MANDATORY_KPIS.put("ue_percentage_q1", "25");
        MANDATORY_KPIS.put("ue_percentage_q2", "40");
        MANDATORY_KPIS.put("ue_percentage_q3", "15");
        MANDATORY_KPIS.put("ue_percentage_q4", "20");
        MANDATORY_KPIS.put("app_coverage_reliability", "true");
        MANDATORY_KPIS.put("kpi_cell_reliability_daily", "1");
        MANDATORY_KPIS.put("synthetic_counter_cell_reliability_daily", "2");

        OPTIONAL_KPIS.put("e_rab_retainability_percentage_lost", "0.0558157793048175");
        OPTIONAL_KPIS.put("e_rab_retainability_percentage_lost_qci1", "0");
        OPTIONAL_KPIS.put("initial_and_added_e_rab_establishment_sr", "99.9435588200893");
        OPTIONAL_KPIS.put("initial_and_added_e_rab_establishment_sr_for_qci1", "99.9435588200893");
        OPTIONAL_KPIS.put("cell_availability", "100.0");
        OPTIONAL_KPIS.put("cell_handover_success_rate", "100");
        OPTIONAL_KPIS.put("percentage_endc_users", "40.0");
        OPTIONAL_KPIS.put("ul_pusch_sinr_hourly", "100");
        OPTIONAL_KPIS.put("num_samples_rsrp_ta_q1", "10");
        OPTIONAL_KPIS.put("num_samples_rsrp_ta_q2", "10");
        OPTIONAL_KPIS.put("num_samples_rsrp_ta_q3", "10");
        OPTIONAL_KPIS.put("num_samples_rsrp_ta_q4", "10");
        OPTIONAL_KPIS.put("num_bad_samples_rsrp_ta_q1", "1");
        OPTIONAL_KPIS.put("num_bad_samples_rsrp_ta_q2", "1");
        OPTIONAL_KPIS.put("num_bad_samples_rsrp_ta_q3", "1");
        OPTIONAL_KPIS.put("num_bad_samples_rsrp_ta_q4", "1");

        MANDATORY_SETTINGS.put("target_throughput_r", "5");
        MANDATORY_SETTINGS.put("delta_gfs_optimization_threshold", "0.3");
        MANDATORY_SETTINGS.put("percentile_for_max_connected_user", "0.7");
        MANDATORY_SETTINGS.put("min_num_cell_for_cdf_calculation", "15");
        MANDATORY_SETTINGS.put("qos_for_capacity_estimation", "0.7");
        MANDATORY_SETTINGS.put("target_source_coverage_balance_ratio_threshold", "0.9");
        MANDATORY_SETTINGS.put("source_target_samples_overlap_threshold", "70");
        MANDATORY_SETTINGS.put("target_source_contiguity_ratio_threshold", "0.9");
        MANDATORY_SETTINGS.put("lb_threshold_for_initial_erab_estab_succ_rate", "98");
        MANDATORY_SETTINGS.put("lb_threshold_for_initial_erab_estab_succ_rate_for_qci1", "98.5");
        MANDATORY_SETTINGS.put("lb_threshold_for_erab_percentage_lost", "2");
        MANDATORY_SETTINGS.put("lb_threshold_for_erab_percentage_lost_for_qci1", "1.5");
        MANDATORY_SETTINGS.put("lb_threshold_for_cell_ho_succ_rate", "70");
        MANDATORY_SETTINGS.put("lb_threshold_for_cell_availability", "70");
        MANDATORY_SETTINGS.put("optimization_speed_factor_table", "slow=6, normal=4, fast=2");
        MANDATORY_SETTINGS.put("lb_threshold_for_endc_users", "50.0");
        MANDATORY_SETTINGS.put("num_calls_cell_hourly_reliability_threshold_in_hours", "1");
        MANDATORY_SETTINGS.put("synthetic_counters_cell_reliability_threshold_in_rops", "1");
        MANDATORY_SETTINGS.put("bandwidth_to_step_size_table", "1400=1, 3000=1, 5000=2, 10000=4, 15000=5, 20000=10");
        MANDATORY_SETTINGS.put("uplink_pusch_sinr_ratio_threshold", "0.8");
        MANDATORY_SETTINGS.put("min_target_uplink_pusch_sinr", "5");
        MANDATORY_SETTINGS.put("percentage_bad_rsrp_ratio_threshold", "1.2");
        MANDATORY_CM_ATTRIBUTES.put("bandwidth", "5000");
        MANDATORY_CM_ATTRIBUTES.put("caimc", "undefined");
    }

    private CucumberData() {

    }

    public static List<OptimizationCell> cellFactory(final String id) {
        final String defaultCellFdn = "001";
        final String optimizationCellFdn = "002";

        final List<OptimizationCell> optimizationCells = new ArrayList<>();

        switch (id) {
            case "sector_with_two_cell":
                optimizationCells.add(buildDefaultOptimizationCell(defaultCellFdn));
                optimizationCells.add(buildDefaultOptimizationCell(optimizationCellFdn));
                break;
            case "sector_with_one_cell":
                optimizationCells.add(buildDefaultOptimizationCell(defaultCellFdn));
                break;
            case "delta_gfs_difference_below_threshold":
                optimizationCells.add(buildDefaultOptimizationCell(defaultCellFdn));
                optimizationCells.add(buildOptimizationCell001(optimizationCellFdn));
                break;
            case "delta_gfs_difference_above_threshold":
                optimizationCells.add(buildDefaultOptimizationCell(defaultCellFdn));
                optimizationCells.add(buildOptimizationCell002(optimizationCellFdn));
                break;
            case "delta_gfs_difference_equal_to_threshold":
                optimizationCells.add(buildDefaultOptimizationCell(defaultCellFdn));
                optimizationCells.add(buildOptimizationCell003(optimizationCellFdn));
                break;
            case "delta_gfs_inconsistent_target_throughput_r":
                optimizationCells.add(buildDefaultOptimizationCell(defaultCellFdn));
                optimizationCells.add(buildOptimizationCell004(optimizationCellFdn));
                break;
            case "delta_gfs_inconsistent_threshold":
                optimizationCells.add(buildDefaultOptimizationCell(defaultCellFdn));
                optimizationCells.add(buildOptimizationCell005(optimizationCellFdn));
                break;
            case "delta_gfs_and_target_throughput_r_inconsistent":
                optimizationCells.add(buildDefaultOptimizationCell(defaultCellFdn));
                optimizationCells.add(buildOptimizationCell006(optimizationCellFdn));
                break;
            case "Imbalanced_Sector_with_2_Cells_173290088340418268":
                optimizationCells.add(buildOptimizationCell054234_2("054234_2"));
                optimizationCells.add(buildOptimizationCell054234_2_2("054234_2_2"));
                break;
            case "Imbalanced_Sector_with_4_Cells_173290459927812150":
                optimizationCells.add(buildOptimizationCell054343_3_9("054343_3_9"));
                optimizationCells.add(buildOptimizationCell054343_3("054343_3"));
                optimizationCells.add(buildOptimizationCell054343_3_2("054343_3_2"));
                optimizationCells.add(buildOptimizationCell054343_3_4("054343_3_4"));
                break;
            case "Balanced_Sector_with_4_Cells_173290409770210514":
                optimizationCells.add(buildOptimizationCell054145_3("054145_3"));
                optimizationCells.add(buildOptimizationCell054147_4("054147_4"));
                optimizationCells.add(buildOptimizationCell054145_3_2("054145_3_2"));
                optimizationCells.add(buildOptimizationCell054147_4_2("054147_4_2"));
                break;
            case "Imbalanced_Sector_with_6_Cells_173290089656102500":
                optimizationCells.add(buildOptimizationCell054444_1("054444_1"));
                optimizationCells.add(buildOptimizationCell054444_1_2("054444_1_2"));
                optimizationCells.add(buildOptimizationCell054444_1_4("054444_1_4"));
                optimizationCells.add(buildOptimizationCell054444_1_9("054444_1_9"));
                optimizationCells.add(buildOptimizationCell054444_2("054444_2"));
                optimizationCells.add(buildOptimizationCell054444_2_2("054444_2_2"));
                break;
            case "Sector_with_All_Cells_missing_mandatory_data":
                // see feature file for removed values
                optimizationCells.add(buildDefaultOptimizationCell("cell_missing_kpi"));
                optimizationCells.add(buildDefaultOptimizationCell("cell_missing_setting"));
                optimizationCells.add(buildDefaultOptimizationCell("cell_missing_cm_data"));
                optimizationCells.add(buildDefaultOptimizationCell("cell_empty_kpi"));
                optimizationCells.add(buildDefaultOptimizationCell("cell_empty_setting"));
                optimizationCells.add(buildDefaultOptimizationCell("cell_empty_cm_data"));
                break;
            case "Imbalanced_Sector_with_4_Cells_173290459927812950":
                optimizationCells.add(buildOptimizationCell054950_3_9("054950_3_9"));
                optimizationCells.add(buildOptimizationCell054950_3("054950_3"));
                optimizationCells.add(buildOptimizationCell054950_3_2("054950_3_2"));
                optimizationCells.add(buildOptimizationCell054950_3_4("054950_3_4"));
                break;
            case "Imbalanced_Sector_with_6_Cells_173290089656102900":
                optimizationCells.add(buildOptimizationCell054900_1("054900_1"));
                optimizationCells.add(buildOptimizationCell054900_1_2("054900_1_2"));
                optimizationCells.add(buildOptimizationCell054900_1_4("054900_1_4"));
                optimizationCells.add(buildOptimizationCell054900_1_9("054900_1_9"));
                optimizationCells.add(buildOptimizationCell054900_2("054900_2"));
                optimizationCells.add(buildOptimizationCell054900_2_2("054900_2_2"));
                break;
            case "Imbalanced_Sector_with_4_Cells_173290089656102501":
                optimizationCells.add(buildOptimizationCell054445_1_4("054445_1_4"));
                optimizationCells.add(buildOptimizationCell054445_1_9("054445_1_9"));
                optimizationCells.add(buildOptimizationCell054445_2("054445_2"));
                optimizationCells.add(buildOptimizationCell054445_2_2("054445_2_2"));
                break;
            case "Imbalanced_Sector_with_4_Cells_173290089656102502":
                optimizationCells.add(buildOptimizationCell054447_1_4("054447_1_4"));
                optimizationCells.add(buildOptimizationCell054447_1_9("054447_1_9"));
                optimizationCells.add(buildOptimizationCell054447_2("054447_2"));
                optimizationCells.add(buildOptimizationCell054447_2_2("054447_2_2"));
                break;
            case "Imbalanced_Sector_with_4_Cells_173290089656102503":
                optimizationCells.add(buildOptimizationCell054446_1_4("054446_1_4"));
                optimizationCells.add(buildOptimizationCell054446_1_9("054446_1_9"));
                optimizationCells.add(buildOptimizationCell054446_2("054446_2"));
                optimizationCells.add(buildOptimizationCell054446_2_2("054446_2_2"));
                break;
            case "Imbalanced_Sector_with_4_Cells_173290089656102902":
                optimizationCells.add(buildOptimizationCell054902_1_4("054902_1_4"));
                optimizationCells.add(buildOptimizationCell054902_1_9("054902_1_9"));
                optimizationCells.add(buildOptimizationCell054902_2("054902_2"));
                optimizationCells.add(buildOptimizationCell054902_2_2("054902_2_2"));
                break;
            case "Imbalanced_Sector_with_8_Cells_173290089656102600":
                optimizationCells.add(buildOptimizationCell054448_1("054448_1"));
                optimizationCells.add(buildOptimizationCell054448_1_9("054448_1_9"));
                optimizationCells.add(buildOptimizationCell054448_3("054448_3"));
                optimizationCells.add(buildOptimizationCell054448_2("054448_2"));
                optimizationCells.add(buildOptimizationCell054448_2_2("054448_2_2"));
                optimizationCells.add(buildOptimizationCell054448_2_4("054448_2_4"));
                optimizationCells.add(buildOptimizationCell054448_3_2("054448_3_2"));
                optimizationCells.add(buildOptimizationCell054448_3_9("054448_3_9"));
                break;
            case "Imbalanced_Sector_with_6_Cells_173290089656102800":
                optimizationCells.add(buildOptimizationCell054800_1("054800_1"));
                optimizationCells.add(buildOptimizationCell054800_1_2("054800_1_2"));
                optimizationCells.add(buildOptimizationCell054800_1_4("054800_1_4"));
                optimizationCells.add(buildOptimizationCell054800_1_9("054800_1_9"));
                optimizationCells.add(buildOptimizationCell054800_2("054800_2"));
                optimizationCells.add(buildOptimizationCell054800_2_2("054800_2_2"));
                break;
            case "Imbalanced_Sector_with_4_Cells_173290459927812850":
                optimizationCells.add(buildOptimizationCell054850_3_9("054850_3_9"));
                optimizationCells.add(buildOptimizationCell054850_3("054850_3"));
                optimizationCells.add(buildOptimizationCell054850_3_2("054850_3_2"));
                optimizationCells.add(buildOptimizationCell054850_3_4("054850_3_4"));
                break;
            case "Imbalanced_Sector_with_2_Cells_173290088340418968":
                optimizationCells.add(buildOptimizationCell054968_2("054968_2"));
                optimizationCells.add(buildOptimizationCell054968_2_2("054968_2_2"));
                break;
            default:
                throw new IllegalArgumentException("Invalid ID: '" + id + "'");
        }

        return optimizationCells;
    }

    public static OptimizationCell buildDefaultOptimizationCell(final String fdn) {
        final OptimizationCell optimizationCell = new OptimizationCell(fdn, 1);
        addKpis(optimizationCell, MANDATORY_KPIS);
        addKpis(optimizationCell, OPTIONAL_KPIS);
        addSettings(optimizationCell, MANDATORY_SETTINGS);
        addCmAttributes(optimizationCell, MANDATORY_CM_ATTRIBUTES);
        return optimizationCell;
    }

    private static void addKpis(final OptimizationCell optimizationCell, final Map<String, String> mandatoryKpis) {
        for (final Map.Entry<String, String> entry : mandatoryKpis.entrySet()) {
            optimizationCell.addKpi(entry.getKey(), entry.getValue());
        }
    }

    private static void addSettings(final OptimizationCell optimizationCell, final Map<String, String> settings) {
        for (final Map.Entry<String, String> entry : settings.entrySet()) {
            optimizationCell.addSetting(entry.getKey(), entry.getValue());
        }
    }

    private static void addCmAttributes(final OptimizationCell optimizationCell, final Map<String, String> cmAttributes) {
        for (final Map.Entry<String, String> entry : cmAttributes.entrySet()) {
            optimizationCell.addCmAttribute(entry.getKey(), entry.getValue());
        }
    }

    private static OptimizationCell buildOptimizationCell001(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.60018373040423");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell002(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.24018373040423");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell003(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.34018373040423006");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell004(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addSetting("target_throughput_r", "2");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell005(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addSetting("delta_gfs_optimization_threshold", "0.5");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell006(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addSetting("target_throughput_r", "2");
        optimizationCell.addSetting("delta_gfs_optimization_threshold", "0.5");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054234_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("unhappy_users", "0.545855356858811");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054234_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.97165574442653");
        optimizationCell.addKpi("unhappy_users", "0.645855356858811");

        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054343_3_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.406027043555072");
        optimizationCell.addKpi("unhappy_users", "0.645855356858811");

        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054343_3(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.529314794871641");
        optimizationCell.addKpi("unhappy_users", "0.645855356858811");

        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054343_3_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.828850312445342");
        optimizationCell.addKpi("unhappy_users", "1.54536953213271");

        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054343_3_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.900317058100273");
        optimizationCell.addKpi("unhappy_users", "20.743089638662777");

        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054145_3(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.911262002743484");
        optimizationCell.addKpi("unhappy_users", "0.0417244739536881");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054147_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.974807751461089");
        optimizationCell.addKpi("unhappy_users", "0.0403295229106036");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054145_3_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.994125354670572");
        optimizationCell.addKpi("unhappy_users", "0.00236496996673607");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054147_4_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.997014575894164");
        optimizationCell.addKpi("unhappy_users", "0.135612962188048");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054444_1(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.933248616471435");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");

        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054444_1_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.602045667098208");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054444_1_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.534909938121602");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054444_1_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.358874105661037");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054444_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.924184080627752");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054444_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.840501303383472");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054950_3_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.406027043555072");
        optimizationCell.addKpi("unhappy_users", "0.645855356858811");
        optimizationCell.addKpi("contiguity", "38.3333333333333");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054950_3(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.529314794871641");
        optimizationCell.addKpi("unhappy_users", "0.645855356858811");
        optimizationCell.addKpi("contiguity", "3.33333333333333");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054950_3_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.828850312445342");
        optimizationCell.addKpi("unhappy_users", "1.54536953213271");
        optimizationCell.addKpi("contiguity", "35.4609929078014");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054950_3_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.900317058100273");
        optimizationCell.addKpi("unhappy_users", "20.743089638662777");
        optimizationCell.addKpi("contiguity", "23.5632183908046");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054900_1(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.933248616471435");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        optimizationCell.addKpi("contiguity", "97.3209936678032");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054900_1_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.602045667098208");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("contiguity", "97.3209936678032");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054900_1_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.534909938121602");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("contiguity", "14.7196261682243");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054900_1_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.358874105661037");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("contiguity", "38.8121212");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054900_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.924184080627752");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        optimizationCell.addKpi("contiguity", "80.0");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054900_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.840501303383472");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        optimizationCell.addKpi("contiguity", "44.3181818181818");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054445_1_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.534909938121602");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("contiguity", "14.7196261682243");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054445_1_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.358874105661037");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("contiguity", "39.8121212");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054445_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.924184080627752");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        optimizationCell.addKpi("contiguity", "80.0");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054445_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.840501303383472");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        optimizationCell.addKpi("contiguity", "44.3181818181818");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054447_1_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.534909938121602");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("contiguity", "84.7196261682243");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054447_1_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.358874105661037");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("contiguity", "89.8121212");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054447_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.924184080627752");
        optimizationCell.addKpi("unhappy_users", "90.592511239070919");
        optimizationCell.addKpi("contiguity", "0.0");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054447_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.840501303383472");
        optimizationCell.addKpi("unhappy_users", "89.592511239070919");
        optimizationCell.addKpi("contiguity", "80.0");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054446_1_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.534909938121602");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("contiguity", "14.7196261682243");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054446_1_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.358874105661037");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("contiguity", "39.8121212");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054446_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.924184080627752");
        optimizationCell.addKpi("unhappy_users", "90.592511239070919");
        optimizationCell.addKpi("contiguity", "0.0");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054446_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.840501303383472");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        optimizationCell.addKpi("contiguity", "0.0");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054902_1_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.534909938121602");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("coverage_balance_ratio_distance", "60.6123420083598532");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054902_1_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.558874105661037");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054902_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.644184080627752");
        optimizationCell.addKpi("unhappy_users", "90.592511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054902_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.940501303383472");
        optimizationCell.addKpi("unhappy_users", "89.592511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054448_1(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.933248616471435");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054448_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.777599311346782");
        optimizationCell.addKpi("unhappy_users", "45.052511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054448_3(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.966352774049607");
        optimizationCell.addKpi("unhappy_users", "20.092511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054448_1_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.434909938121602");
        optimizationCell.addKpi("unhappy_users", "76.0995926937517");
        optimizationCell.addKpi("coverage_balance_ratio_distance", "23.4050611054374507");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054448_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.558874105661037");
        optimizationCell.addKpi("unhappy_users", "78.24862155388474");
        optimizationCell.addKpi("coverage_balance_ratio_distance", "69.4098331507027");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054448_2_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.534909938121602");
        optimizationCell.addKpi("unhappy_users", "52.3161982735189");
        optimizationCell.addKpi("coverage_balance_ratio_distance", "52.3382867132867");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054448_3_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.860501303383472");
        optimizationCell.addKpi("unhappy_users", "0.582511239070919");
        optimizationCell.addKpi("coverage_balance_ratio_distance", "12.3382867132867");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054448_3_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.637163406885934");
        optimizationCell.addKpi("unhappy_users", "6.69508133857572");
        optimizationCell.addKpi("coverage_balance_ratio_distance", "60.6123420083598532");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054800_1(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.933248616471435");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054800_1_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.602045667098208");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        optimizationCell.addKpi("coverage_balance_ratio_distance", "89.999999999");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054800_1_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.834909938121602");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054800_1_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.898874105661037");
        optimizationCell.addKpi("unhappy_users", "13.4191211495669");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054800_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.924184080627752");
        optimizationCell.addKpi("unhappy_users", "0.592511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054800_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.840501303383472");
        optimizationCell.addKpi("unhappy_users", "10.592511239070919");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054850_3_9(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.406027043555072");
        optimizationCell.addKpi("unhappy_users", "0.645855356858811");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054850_3(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.529314794871641");
        optimizationCell.addKpi("unhappy_users", "0.645855356858811");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054850_3_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.828850312445342");
        optimizationCell.addKpi("unhappy_users", "11.54536953213271");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054850_3_4(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.900317058100273");
        optimizationCell.addKpi("unhappy_users", "20.743089638662777");
        optimizationCell.addKpi("coverage_balance_ratio_distance", "0.0");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054968_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.66165574442653");
        return optimizationCell;
    }

    private static OptimizationCell buildOptimizationCell054968_2_2(final String fdn) {
        final OptimizationCell optimizationCell = buildDefaultOptimizationCell(fdn);
        optimizationCell.addKpi("goal_function_resource_efficiency", "0.97165574442653");
        optimizationCell.addKpi("coverage_balance_ratio_distance", "0.0");
        return optimizationCell;
    }
}
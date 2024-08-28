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
package com.ericsson.oss.services.sonom.flm.database.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to store column names for the cell_configuration table.
 */
public final class CellSettingsDbConstants {
    public static final String CELL_CONFIGURATION_HISTORY = "cell_configuration_history";
    public static final String CELL_CONFIGURATION = "cell_configuration";
    public static final String ID = "id";
    public static final String OSS_ID = "oss_id";
    public static final String FDN = "fdn";
    public static final String EXECUTION_ID = "execution_id";
    public static final String CONFIGURATION_ID = "configuration_id";
    public static final String EXCLUSION_LIST = "exclusion_list";
    public static final String MIN_ROPS_FOR_APP_COV_RELIABILITY = "min_rops_for_app_cov_reliability";
    public static final String MINIMUM_SOURCE_RETAINED = "minimum_source_retained";
    public static final String MIN_NUM_CQI_SAMPLES = "min_num_cqi_samples";
    public static final String SECTOR_ID = "sector_id";
    public static final String CREATED = "created";
    public static final String COMMA = ",";

    /**
     * Convenient list of FLM specific settings
     */
    private static final List<String> FLM_SPECIFIC_SETTINGS = Arrays.asList(EXCLUSION_LIST, MINIMUM_SOURCE_RETAINED, MIN_ROPS_FOR_APP_COV_RELIABILITY,
            MIN_NUM_CQI_SAMPLES, SECTOR_ID);
    private static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";

    /**
     * Holder enum for policy input settings.
     */
    public enum PolicyInputSettings {
        QOS_FOR_CAPACITY_ESTIMATION("qos_for_capacity_estimation"),
        PERCENTILE_FOR_MAX_CONNECTED_USER("percentile_for_max_connected_user"),
        MIN_NUM_CELL_FOR_CDF_CALCULATION("min_num_cell_for_cdf_calculation"),
        TARGET_THROUGHPUT_R("target_throughput_r"),
        DELTA_GFS_OPTIMIZATION_THRESHOLD("delta_gfs_optimization_threshold"),
        TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD("target_source_coverage_balance_ratio_threshold"),
        SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD("source_target_samples_overlap_threshold"),
        TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD("target_source_contiguity_ratio_threshold"),
        LB_THRESHOLD_FOR_INITIAL_ERAB_ESTAB_SUCC_RATE("lb_threshold_for_initial_erab_estab_succ_rate"),
        LB_THRESHOLD_FOR_INITIAL_ERAB_ESTAB_SUCC_RATE_FOR_QCI1("lb_threshold_for_initial_erab_estab_succ_rate_for_qci1"),
        LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST("lb_threshold_for_erab_percentage_lost"),
        LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1("lb_threshold_for_erab_percentage_lost_for_qci1"),
        LB_THRESHOLD_FOR_CELL_HO_SUCC_RATE("lb_threshold_for_cell_ho_succ_rate"),
        LB_THRESHOLD_FOR_CELL_AVAILABILITY("lb_threshold_for_cell_availability"),
        OPTIMIZATION_SPEED("optimization_speed"),
        OPTIMIZATION_SPEED_FACTOR_TABLE("optimization_speed_factor_table"),
        BANDWIDTH_TO_STEP_SIZE_TABLE("bandwidth_to_step_size_table"),
        LB_THRESHOLD_FOR_ENDC_USERS("lb_threshold_for_endc_users"),
        NUM_CALLS_CELL_HOURLY_RELIABILITY_THRESHOLD_IN_HOURS("num_calls_cell_hourly_reliability_threshold_in_hours"),
        SYNTHETIC_COUNTERS_CELL_RELIABILITY_THRESHOLD_IN_ROPS("synthetic_counters_cell_reliability_threshold_in_rops"),
        ENABLE_ESS_SETTING("ess_enabled"),
        MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION("min_num_samples_for_transient_calculation"),
        SIGMA_FOR_TRANSIENT_CALCULATION("sigma_for_transient_calculation"),
        UPLINK_PUSCH_SINR_RATIO_THRESHOLD("uplink_pusch_sinr_ratio_threshold"),
        MIN_TARGET_UPLINK_PUSCH_SINR("min_target_uplink_pusch_sinr"),
        PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD("percentage_bad_rsrp_ratio_threshold"),
        MIN_CONNECTED_USERS("min_connected_users");

        private final String setting;

        PolicyInputSettings(final String setting) {
            this.setting = setting;

        }

        String getValue() {
            return setting;
        }
    }

    private CellSettingsDbConstants() {
    }

    /**
     * Returns all column names in format for select query.
     *
     * @return a comma separated list of column names.
     */
    public static String getAllColumnNames() {
        final char comma = ',';
        final StringBuilder columnNames = new StringBuilder();
        return columnNames.append(ID).append(comma)
                .append(OSS_ID).append(comma)
                .append(FDN).append(comma)
                .append(EXECUTION_ID).append(comma)
                .append(CONFIGURATION_ID).append(comma)
                .append(getPolicyInputSettingsColumns()).append(comma)
                .append(EXCLUSION_LIST).append(comma)
                .append(MINIMUM_SOURCE_RETAINED).append(comma)
                .append(MIN_ROPS_FOR_APP_COV_RELIABILITY).append(comma)
                .append(MIN_NUM_CQI_SAMPLES).append(comma)
                .append(SECTOR_ID).toString();
    }

    /**
     * Returns all policy input column names in format for select query.
     *
     * @return a comma separated list of column names.
     */
    public static String getPolicyInputSettingsColumns() {
        return Stream.of(PolicyInputSettings.values())
                .map(PolicyInputSettings::getValue)
                .collect(Collectors.joining(COMMA));
    }

    /**
     * Returns insert variable string for all columns.
     *
     * @return a comma separated list of '?'s.
     */
    public static String getInsertStatementString() {
        return getAllColumnNames().replaceAll("\\w+", "?");
    }

    /**
     * Returns all column names in format for select query.
     *
     * @return a comma separated list of column names.
     */
    public static String getAllColumnNamesForCellConfigHistory() {
        final List<String> columns = new ArrayList<>(Arrays.asList(ID, OSS_ID, FDN, EXECUTION_ID, CREATED));
        columns.addAll(getSettingsColumnNamesAsList());
        return String.join(COMMA, columns);
    }

    /**
     * Returns all column names in format for insert select query.
     *
     * @return a comma separated list of column names.
     */
    public static String getAllColumnNamesForInsertionSelectForCellConfigHistory() {
        final List<String> columns = new ArrayList<>(Arrays.asList(ID, OSS_ID, FDN, EXECUTION_ID, CURRENT_TIMESTAMP));
        columns.addAll(getSettingsColumnNamesAsList());
        return String.join(COMMA, columns);
    }

    /**
     * Returns the all the updatable columns in the sql format for an upsert statement.
     *
     * @return a {@link String} containing the sql string format for all the columns to be updated in an upsert statement
     */
    public static String getUpdateSetStatementString() {
        final List<String> setStatementList = getStringWithExcludedStatement(new ArrayList<>(Arrays.asList(OSS_ID, FDN, CONFIGURATION_ID)));
        setStatementList.addAll(getStringWithExcludedStatement(getSettingsColumnNamesAsList()));
        return String.join(COMMA, setStatementList);
    }

    /**
     * Returns all setting column names in a list.
     *
     * @return a {@link List} of string setting column names
     */
    static List<String> getSettingsColumnNamesAsList() {
        final List<String> columns = new ArrayList<>();
        columns.addAll(Stream.of(PolicyInputSettings.values())
                .map(PolicyInputSettings::getValue)
                .collect(Collectors.toList()));
        columns.addAll(FLM_SPECIFIC_SETTINGS);
        return columns;
    }

    private static List<String> getStringWithExcludedStatement(final List<String> columns) {
        final List<String> setStatementList = new ArrayList<>(columns.size());
        for (final String column : columns) {
            setStatementList.add(getColumnFormattedWithExcludedStatement(column));
        }
        return setStatementList;
    }

    private static String getColumnFormattedWithExcludedStatement(final String columnName) {
        return columnName + "=excluded." + columnName;
    }
}

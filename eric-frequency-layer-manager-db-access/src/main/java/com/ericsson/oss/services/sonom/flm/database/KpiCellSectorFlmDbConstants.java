/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database;

/**
 * Class to store column names for the KPI cell sector flm table.
 */
public final class KpiCellSectorFlmDbConstants {

    public static final String CELL_FDN = "cell_fdn";
    public static final String CELL_ID = "cell_id";
    public static final String CELL_OSS_ID = "cell_oss_id";
    public static final String EXECUTION_ID = "execution_id";
    public static final String SECTOR_ID = "sector_id";
    public static final String EXECUTION_SECTOR_BUSY_HOUR = "execution_sector_busy_hour";
    public static final String MAX_CONNECTED_USERS_DAILY = "max_connected_users_daily";
    public static final String LOWER_THRESHOLD_FOR_TRANSIENT = "lower_threshold_for_transient";
    public static final String UPPER_THRESHOLD_FOR_TRANSIENT = "upper_threshold_for_transient";
    public static final String FREQ_BAND_DAILY = "freq_band_daily";
    public static final String BANDWIDTH_DAILY = "bandwidth_daily";
    public static final String TARGET_CELL_CAPACITY = "target_cell_capacity";
    public static final String NUM_VALUES_USED_FOR_MCU_CDF_CALCULATION_DAILY = "num_values_used_for_mcu_cdf_calculation_daily";
    public static final String TARGET_THROUGHPUT_R_DAILY = "target_throughput_r_daily";
    public static final String ACTUAL_CONNECTED_USERS = "actual_connected_users";
    public static final String UTC_TIMESTAMP = "utc_timestamp";
    public static final String LOCAL_TIMESTAMP = "local_timestamp";

    private KpiCellSectorFlmDbConstants() {

    }

}
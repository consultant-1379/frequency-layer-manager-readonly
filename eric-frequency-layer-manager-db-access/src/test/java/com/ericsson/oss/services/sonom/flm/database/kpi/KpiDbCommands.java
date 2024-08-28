/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database.kpi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants;
import com.ericsson.oss.services.sonom.flm.database.KpiCellSectorFlmDbConstants;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants;

/**
 * Class which contains CRUD operations on the KPI Sector table within the <code>eric-pm-kpi-calculator</code> service.
 */
public final class KpiDbCommands {

    public static final String CELL_SECTOR_1440_TABLE = "cell_sector_1440_kpis";
    public static final String CELL_SECTOR_1440_FLM_TABLE = "cell_sector_flm_1440_kpis";
    public static final String CELL_GUID_1440_TABLE = "cell_guid_1440_kpis";
    public static final String CELL_GUID_60_KPIS_TABLE = "cell_guid_60_kpis";
    public static final String CELL_GUID_FLM_60_KPIS_TABLE = "cell_guid_flm_60_kpis";
    public static final String KPI_CELL_GUID_1440_KPIS_TABLE = "kpi_cell_guid_1440";
    public static final String COVERAGE_BALANCE_RATIO_DISTANCE = "coverage_balance_ratio_distance";
    public static final String CONTIGUITY = "contiguity";
    public static final String CELL_HANDOVER_SUCCESS_RATE = "cell_handover_success_rate";
    public static final String E_RAB_RETAINABILITY_PERCENTAGE_LOST = "e_rab_retainability_percentage_lost";
    public static final String E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1 = "e_rab_retainability_percentage_lost_qci1";
    public static final String INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR = "initial_and_added_e_rab_establishment_sr";
    public static final String INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT = "initial_and_added_e_rab_establishment_sr_for_qci1";

    private static final String KPI_CELL_SECTOR_FLM_1440_TABLE = "kpi_cell_sector_flm_1440";
    private static final String SECTOR_1440_KPIS_TABLE = "sector_1440_kpis";
    private static final String KPI_CELL_GUID_60_TABLE = "kpi_cell_guid_60";
    private static final String UNHAPPY_USERS_COLUMN = "unhappy_users";
    private static final String SECTOR_BUSY_HOUR_COLUMN = "sector_busy_hour";
    private static final String EXECUTION_ID_COLUMN = "execution_id";
    private static final String GOAL_FUNCTION_RESOURCE_EFFICIENCY_COLUMN = "goal_function_resource_efficiency";
    private static final String P_FAILING_R_MBPS = "p_failing_r_mbps";
    private static final String P_FAILING_R_MBPS_DETRENDED = "p_failing_r_mbps_detrended";
    private static final String APP_COVERAGE_RELIABILITY = "app_coverage_reliability";
    private static final String COVERAGE_BALANCE_RATIO_SIGNAL = "coverage_balance_ratio_signal";

    private KpiDbCommands() {
        // intentionally private, utility class
    }

    public static List<String> createKpiSectorTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY, %s VARCHAR(255));",
                        KpiSectorDbConstants.KPI_SECTOR_TABLE, KpiSectorDbConstants.SECTOR_ID_COLUMN,
                        KpiSectorDbConstants.REFERENCE_CELL_SECTOR_FDN_COLUMN));
    }

    public static List<String> createKpiCellTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s INTEGER NOT NULL, %s INTEGER NOT NULL, %s VARCHAR(255) NOT NULL, "
                                + "%s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s INTEGER);",
                        KpiSectorDbConstants.KPI_CELL_GUID_1440_TABLE, KpiSectorDbConstants.GUID_COLUMN,
                        KpiSectorDbConstants.OSS_ID_COLUMN, KpiSectorDbConstants.FDN_COLUMN,
                        KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN, KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN,
                        KpiSectorDbConstants.CELL_AVAILABILITY_COLUMN));
    }

    public static List<String> createKpiCellGuid60Table() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s LONG NOT NULL, %s INTEGER NOT NULL, %s VARCHAR(255) NOT NULL, " +
                                "%s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL, " +
                                "%s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, " +
                                "%s FLOAT, %s FLOAT);",
                        KPI_CELL_GUID_60_TABLE, KpiCellDbConstants.GUID,
                        KpiCellDbConstants.OSS_ID, KpiCellDbConstants.FDN,
                        KpiCellDbConstants.UTC_TIMESTAMP, KpiCellDbConstants.LOCAL_TIMESTAMP,
                        KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_HIGH_LOAD, KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_MEDIUM_HIGH_LOAD,
                        KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_MEDIOUM_LOAD, KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_LOW_MEDIUM_LOAD,
                        KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_LOW_LOAD, KpiCellDbConstants.SUBSCRIPTION_RATIO,
                        KpiCellDbConstants.CONNECTED_USERS));
    }

    public static List<String> createSector1440KpiTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER, %3$s TIMESTAMP, %4$s TIMESTAMP, PRIMARY KEY(%2$s,  %3$s));",
                        SECTOR_1440_KPIS_TABLE, KpiSectorDbConstants.SECTOR_ID_COLUMN,
                        KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN,
                        SECTOR_BUSY_HOUR_COLUMN));
    }

    public static List<String> createSector60KpiTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER, %3$s TIMESTAMP, %4$s TIMESTAMP, %5$s FLOAT, %6$s FLOAT, PRIMARY KEY(%2$s,  %3$s));",
                        KpiSectorDbConstants.SECTOR_60_KPIS_TABLE, KpiSectorDbConstants.SECTOR_ID_COLUMN,
                        KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN,
                        KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN,
                        KpiSectorDbConstants.AVG_DL_PDCP_THROUGHPUT_SECTOR,
                        KpiSectorDbConstants.AVG_UL_PDCP_THROUGHPUT_SECTOR));
    }

    public static List<String> createSectorFlm60KpiTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %1$s (%2$s INTEGER, %3$s VARCHAR(255) NOT NULL, %4$s TIMESTAMP, %5$s TIMESTAMP, %6$s FLOAT, %7$s FLOAT, PRIMARY KEY(%2$s,  %4$s));",
                        KpiSectorDbConstants.SECTOR_FLM_60_KPIS_TABLE, KpiSectorDbConstants.SECTOR_ID_COLUMN, EXECUTION_ID_COLUMN,
                        KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN,
                        KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN,
                        KpiSectorDbConstants.AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION,
                        KpiSectorDbConstants.AVG_UL_PDCP_THROUGHPUT_SECTOR_DEGRADATION));
    }

    public static List<String> createCellKpiTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s LONG NOT NULL, %s INTEGER NOT NULL, %s VARCHAR(255) NOT NULL, %s VARCHAR(255) NOT NULL, "
                                + "%s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s INTEGER, %s INTEGER, "
                                + "%s DOUBLE PRECISION, %s BOOLEAN, %s DOUBLE PRECISION);",
                        CELL_GUID_FLM_60_KPIS_TABLE, KpiSectorDbConstants.GUID_COLUMN,
                        KpiSectorDbConstants.OSS_ID_COLUMN, EXECUTION_ID_COLUMN, KpiSectorDbConstants.FDN_COLUMN,
                        KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN, KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN,
                        UNHAPPY_USERS_COLUMN, GOAL_FUNCTION_RESOURCE_EFFICIENCY_COLUMN, P_FAILING_R_MBPS, APP_COVERAGE_RELIABILITY,
                        P_FAILING_R_MBPS_DETRENDED));
    }

    public static List<String> createCell1440KpiTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s VARCHAR(255) NOT NULL, %s LONG NOT NULL, %s INTEGER NOT NULL, " +
                                "%s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL, " +
                                "%s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER);",
                        CELL_GUID_1440_TABLE, KpiSectorDbConstants.FDN_COLUMN,
                        KpiSectorDbConstants.GUID_COLUMN, KpiSectorDbConstants.OSS_ID_COLUMN,
                        KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN, KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN,
                        CONTIGUITY, CELL_HANDOVER_SUCCESS_RATE,
                        E_RAB_RETAINABILITY_PERCENTAGE_LOST, E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1,
                        INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR, INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT));
    }

    public static List<String> createCellSectorTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s VARCHAR(255) NOT NULL, %s LONG NOT NULL, %s INTEGER NOT NULL, %s VARCHAR(255) NOT NULL," +
                                " %s LONG NOT NULL, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s " +
                                "INTEGER, %s INTEGER);",
                        CELL_SECTOR_1440_TABLE, KpiSectorDbConstants.CELL_FDN,
                        KpiSectorDbConstants.CELL_ID, KpiSectorDbConstants.CELL_OSS_ID, KpiSectorDbConstants.FDN_COLUMN,
                        KpiSectorDbConstants.SECTOR_ID_COLUMN, KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN, KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN,
                        COVERAGE_BALANCE_RATIO_DISTANCE, COVERAGE_BALANCE_RATIO_SIGNAL));
    }

    public static List<String> addPrimaryKeyKpiCellTable() {
        return Collections.singletonList(
                String.format(
                        "ALTER TABLE %s ADD CONSTRAINT KPI_CELL_PKEY PRIMARY KEY (%s, %s, %s);",
                        KpiSectorDbConstants.KPI_CELL_GUID_1440_TABLE, KpiSectorDbConstants.OSS_ID_COLUMN,
                        KpiSectorDbConstants.FDN_COLUMN, KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN));
    }

    public static List<String> createCellGuid60KpiTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s VARCHAR(255) NOT NULL, %s LONG NOT NULL, %s INTEGER NOT NULL, " +
                                "%s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL, " +
                                "%s DOUBLE PRECISION, %s DOUBLE PRECISION, %s DOUBLE PRECISION, %s DOUBLE PRECISION, " +
                                "%s DOUBLE PRECISION, %s DOUBLE PRECISION, %s DOUBLE PRECISION, %s DOUBLE PRECISION, " +
                                "%s DOUBLE PRECISION);",
                        CELL_GUID_60_KPIS_TABLE, KpiSectorDbConstants.FDN_COLUMN,
                        KpiSectorDbConstants.GUID_COLUMN, KpiSectorDbConstants.OSS_ID_COLUMN,
                        KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN, KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN,
                        KpiCellDbConstants.CONNECTED_USERS, KpiCellDbConstants.DISTANCE_Q1,
                        KpiCellDbConstants.DISTANCE_Q2, KpiCellDbConstants.DISTANCE_Q3,
                        KpiCellDbConstants.DISTANCE_Q4, KpiCellDbConstants.UE_PERCENTAGE_Q1,
                        KpiCellDbConstants.UE_PERCENTAGE_Q2, KpiCellDbConstants.UE_PERCENTAGE_Q3,
                        KpiCellDbConstants.UE_PERCENTAGE_Q4));
    }

    public static List<String> addPrimaryKeyCellGuid60KpiTable() {
        return Collections.singletonList(
                String.format(
                        "ALTER TABLE %s ADD CONSTRAINT cell_guid_60_kpis_pkey PRIMARY KEY (%s, %s, %s, %s);",
                        CELL_GUID_60_KPIS_TABLE, KpiSectorDbConstants.OSS_ID_COLUMN, KpiSectorDbConstants.GUID_COLUMN,
                        KpiSectorDbConstants.FDN_COLUMN, KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN));
    }

    public static List<String> createKpiCellGuid1440KpiTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s VARCHAR(255) NOT NULL, %s LONG NOT NULL, %s INTEGER NOT NULL, " +
                                "%s TIMESTAMP WITHOUT TIME ZONE NOT NULL, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL, " +
                                "%s DOUBLE PRECISION);",
                        KPI_CELL_GUID_1440_KPIS_TABLE, KpiSectorDbConstants.FDN_COLUMN,
                        KpiSectorDbConstants.GUID_COLUMN, KpiSectorDbConstants.OSS_ID_COLUMN,
                        KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN, KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN,
                        KpiCellDbConstants.CELL_AVAILABILITY));
    }

    public static List<String> createKpiCellSectorFlm1440Table() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s VARCHAR(255) NOT NULL," +
                                " %s LONG NOT NULL, %s INTEGER NOT NULL, %s VARCHAR(255) NOT NULL,  %s LONG NOT NULL, " +
                                "%s TIMESTAMP WITHOUT TIME ZONE, %s DOUBLE PRECISION, %s INTEGER, %s INTEGER, " +
                                "%s DOUBLE PRECISION, %s INTEGER, %s DOUBLE PRECISION, %s DOUBLE PRECISION, " +
                                "%s TIMESTAMP WITHOUT TIME ZONE, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL);",
                        KPI_CELL_SECTOR_FLM_1440_TABLE, KpiCellSectorFlmDbConstants.CELL_FDN,
                        KpiCellSectorFlmDbConstants.CELL_ID, KpiCellSectorFlmDbConstants.CELL_OSS_ID,
                        KpiCellSectorFlmDbConstants.EXECUTION_ID, KpiCellSectorFlmDbConstants.SECTOR_ID,
                        KpiCellSectorFlmDbConstants.EXECUTION_SECTOR_BUSY_HOUR, KpiCellSectorFlmDbConstants.MAX_CONNECTED_USERS_DAILY,
                        KpiCellSectorFlmDbConstants.FREQ_BAND_DAILY, KpiCellSectorFlmDbConstants.BANDWIDTH_DAILY,
                        KpiCellSectorFlmDbConstants.TARGET_CELL_CAPACITY, KpiCellSectorFlmDbConstants.NUM_VALUES_USED_FOR_MCU_CDF_CALCULATION_DAILY,
                        KpiCellSectorFlmDbConstants.TARGET_THROUGHPUT_R_DAILY, KpiCellSectorFlmDbConstants.ACTUAL_CONNECTED_USERS,
                        KpiCellSectorFlmDbConstants.UTC_TIMESTAMP, KpiCellSectorFlmDbConstants.LOCAL_TIMESTAMP));
    }

    public static List<String> createCellSectorFlmKpiTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s (%s VARCHAR(255) NOT NULL," +
                                " %s LONG NOT NULL, %s INTEGER NOT NULL, %s VARCHAR(255) NOT NULL,  %s LONG NOT NULL, " +
                                "%s TIMESTAMP WITHOUT TIME ZONE, %s TIMESTAMP WITHOUT TIME ZONE NOT NULL, " +
                                "%s FLOAT, %s FLOAT);",
                        CELL_SECTOR_1440_FLM_TABLE, KpiCellSectorFlmDbConstants.CELL_FDN,
                        KpiCellSectorFlmDbConstants.CELL_ID, KpiCellSectorFlmDbConstants.CELL_OSS_ID,
                        KpiCellSectorFlmDbConstants.EXECUTION_ID, KpiCellSectorFlmDbConstants.SECTOR_ID,
                        KpiCellSectorFlmDbConstants.UTC_TIMESTAMP, KpiCellSectorFlmDbConstants.LOCAL_TIMESTAMP,
                        KpiCellSectorFlmDbConstants.LOWER_THRESHOLD_FOR_TRANSIENT, KpiCellSectorFlmDbConstants.UPPER_THRESHOLD_FOR_TRANSIENT));
    }

    public static List<String> addPrimaryKeyKpiCellGuid1440KpiTable() {
        return Collections.singletonList(
                String.format(
                        "ALTER TABLE %s ADD CONSTRAINT kpi_cell_guid_1440_pkey PRIMARY KEY (%s, %s, %s, %s);",
                        KPI_CELL_GUID_1440_KPIS_TABLE, KpiSectorDbConstants.OSS_ID_COLUMN, KpiSectorDbConstants.GUID_COLUMN,
                        KpiSectorDbConstants.FDN_COLUMN, KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN));
    }

    public static List<String> deleteTableData(final String table) {
        return Collections.singletonList(String.format("DELETE FROM %s", table));
    }

    public static List<String> dropTable(final String table) {
        return Collections.singletonList(String.format("DROP TABLE IF EXISTS %s", table));
    }

    public static List<String> getSqlCommandsFromResourceFile(final String sqlFile) throws IOException {
        final String path = new File("src/test/resources/" + sqlFile).getAbsolutePath();
        return new ArrayList<>(Files.readAllLines(Paths.get(path)));
    }

}
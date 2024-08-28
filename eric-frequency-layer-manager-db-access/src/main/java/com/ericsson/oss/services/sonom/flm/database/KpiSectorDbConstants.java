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
 * Class to store column names for the KPI Sector table.
 */
public final class KpiSectorDbConstants {

    public static final String KPI_SECTOR_TABLE = "kpi_sector";
    public static final String SECTOR_ID_COLUMN = "sector_id";
    public static final String REFERENCE_CELL_SECTOR_FDN_COLUMN = "reference_cell_sector_fdn";
    public static final String KPI_CELL_GUID_1440_TABLE = "kpi_cell_guid_1440";
    public static final String GUID_COLUMN = "guid";
    public static final String OSS_ID_COLUMN = "oss_id";
    public static final String FDN_COLUMN = "fdn";
    public static final String CELL_AVAILABILITY_COLUMN = "cell_availability";
    public static final String LOCAL_TIMESTAMP_COLUMN = "local_timestamp";
    public static final String UTC_TIMESTAMP_COLUMN = "utc_timestamp";
    public static final String CELL_FDN = "cell_fdn";
    public static final String CELL_ID = "cell_id";
    public static final String CELL_OSS_ID = "cell_oss_id";
    public static final String SECTOR_60_KPIS_TABLE = "sector_60_kpis";
    public static final String SECTOR_FLM_60_KPIS_TABLE = "sector_flm_60_kpis";
    public static final String AVG_UL_PDCP_THROUGHPUT_SECTOR = "avg_ul_pdcp_throughput_sector";
    public static final String AVG_DL_PDCP_THROUGHPUT_SECTOR = "avg_dl_pdcp_throughput_sector";
    public static final String AVG_UL_PDCP_THROUGHPUT_SECTOR_DEGRADATION = "avg_ul_pdcp_throughput_sector_degradation";
    public static final String AVG_DL_PDCP_THROUGHPUT_SECTOR_DEGRADATION = "avg_dl_pdcp_throughput_sector_degradation";

    private KpiSectorDbConstants() {

    }
}

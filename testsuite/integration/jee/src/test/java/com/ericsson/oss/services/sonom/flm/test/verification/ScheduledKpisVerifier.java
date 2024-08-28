/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.test.verification;

import static com.ericsson.oss.services.sonom.common.test.sql.SqlAssertions.assertTableContent;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;
import com.ericsson.oss.services.sonom.flm.test.util.CsvReader;
import com.ericsson.oss.services.sonom.flm.test.util.ExternalUserDatabaseProperties;

/**
 * Class used to encapsulate the verification of the scheduled KPIs calculated for FLM by the KPI calculator.
 */
public final class ScheduledKpisVerifier {
    private static final String CELL_GUID_60_VIEW = "cell_guid_60_kpis";
    private static final String CELL_GUID_60_ASSERTION_FILE = "csv-assertions/kpi_exporter/scheduled_kpis/cell_guid_60_kpis_assertions.csv";
    private static final String CELL_GUID_1440_VIEW = "cell_guid_1440_kpis";
    private static final String CELL_GUID_1440_ASSERTION_FILE = "csv-assertions/kpi_exporter/scheduled_kpis/cell_guid_1440_kpis_assertions.csv";
    private static final String KPI_CELL_GUID_60_TABLE = "kpi_cell_guid_60";
    private static final String KPI_CELL_GUID_60_ASSERTION_FILE = "csv-assertions/kpi_service_user/scheduled_kpis/kpi_cell_guid_60_assertions.csv";
    private static final String KPI_CELL_GUID_1440_TABLE = "kpi_cell_guid_1440";
    private static final String KPI_CELL_GUID_1440_ASSERTION_FILE = "csv-assertions/kpi_service_user/scheduled_kpis/kpi_cell_guid_1440_assertions.csv";
    private static final String KPI_CELL_SECTOR_60_TABLE = "kpi_cell_sector_60";
    private static final String KPI_CELL_SECTOR_60_ASSERTION_FILE = "csv-assertions/kpi_service_user/scheduled_kpis/kpi_cell_sector_60_assertions.csv";
    private static final String KPI_SECTOR_60_TABLE = "kpi_sector_60";
    private static final String KPI_SECTOR_60_ASSERTION_FILE = "csv-assertions/kpi_service_user/scheduled_kpis/kpi_sector_60_assertions.csv";
    private static final String CELL_SECTOR_60_VIEW = "cell_sector_60_kpis";
    private static final String CELL_SECTOR_60_ASSERTION_FILE = "csv-assertions/kpi_exporter/scheduled_kpis/cell_sector_60_kpis_assertions.csv";
    private static final String SECTOR_60_VIEW = "sector_60_kpis";
    private static final String SECTOR_60_ASSERTION_FILE = "csv-assertions/kpi_exporter/scheduled_kpis/sector_60_kpis_assertions.csv";
    private static final String TODAY_DATE_TIME_IDENTIFIER = "<TODAY_DATE_TIME>";
    private static final String TODAY_MINUS_THREE_DAY = "<TODAY_MINUS_THREE_DAY>";
    private static final String TODAY_MINUS_ONE_DAY = "<TODAY_MINUS_ONE_DAY>";
    private static final String TODAY = "<TODAY>";
    private static final int HOUR_OFFSET_START_OF_DAY = 02;

    private ScheduledKpisVerifier() {
    }

    /**
     * As user kpi_service_user verify scheduled KPIs are calculated and stored in the relevant table in the kpi_service_db.
     */
    public static void verifyIntermediateKpisAreCalculatedAndStoredInDatabase() {
        verifyTableContent(KPI_CELL_GUID_60_TABLE, KPI_CELL_GUID_60_ASSERTION_FILE, DatabaseProperties.getKpiServiceJdbcProperties());
        verifyTableContent(KPI_CELL_GUID_1440_TABLE, KPI_CELL_GUID_1440_ASSERTION_FILE, DatabaseProperties.getKpiServiceJdbcProperties());
        verifyTableContent(KPI_CELL_SECTOR_60_TABLE, KPI_CELL_SECTOR_60_ASSERTION_FILE, DatabaseProperties.getKpiServiceJdbcProperties());

        final Map<String, String> valueMap = new HashMap<>();
        final LocalDate dateToday = LocalDate.now(ZoneOffset.UTC);
        final String dateTimeToday = dateToday.atStartOfDay().plusHours(HOUR_OFFSET_START_OF_DAY)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        valueMap.put(TODAY_DATE_TIME_IDENTIFIER, dateTimeToday);
        valueMap.put(TODAY_MINUS_THREE_DAY, dateToday.minusDays(3).toString());
        valueMap.put(TODAY_MINUS_ONE_DAY, dateToday.minusDays(1).toString());
        valueMap.put(TODAY, dateToday.toString());
        verifyTableContentWithDynamicValues(KPI_SECTOR_60_TABLE, KPI_SECTOR_60_ASSERTION_FILE,
                DatabaseProperties.getKpiServiceJdbcProperties(), valueMap);
    }

    /**
     * As user kpi_exporter verify visible KPIs are calculated and stored in the relevant view in the kpi_service_db.
     */
    public static void verifyVisibleKpisAreCalculatedAndStoredInDatabase() {
        verifyTableContent(CELL_GUID_1440_VIEW, CELL_GUID_1440_ASSERTION_FILE,
                ExternalUserDatabaseProperties.getExternalUserKpiServiceJdbcProperties());
    }

    /**
     * As user kpi_exporter verify visible KPIs are calculated and stored in the relevant view in the kpi_service_db for
     * PA KPIs.
     */
    public static void verifyVisibleKpisAreCalculatedAndStoredInDatabaseForPAKPIs() {
        verifyTableContent(CELL_SECTOR_60_VIEW,CELL_SECTOR_60_ASSERTION_FILE,
                ExternalUserDatabaseProperties.getExternalUserKpiServiceJdbcProperties());
        final Map<String, String> valueMap = new HashMap<>();
        final LocalDate dateToday = LocalDate.now(ZoneOffset.UTC);
        final String dateTimeToday = dateToday.atStartOfDay().plusHours(HOUR_OFFSET_START_OF_DAY)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        valueMap.put(TODAY_DATE_TIME_IDENTIFIER, dateTimeToday);
        valueMap.put(TODAY_MINUS_THREE_DAY, dateToday.minusDays(3).toString());
        valueMap.put(TODAY_MINUS_ONE_DAY, dateToday.minusDays(1).toString());
        valueMap.put(TODAY, dateToday.toString());
        verifyTableContentWithDynamicValues(SECTOR_60_VIEW, SECTOR_60_ASSERTION_FILE,
                ExternalUserDatabaseProperties.getExternalUserKpiServiceJdbcProperties(), valueMap);
        verifyTableContent(CELL_GUID_60_VIEW,CELL_GUID_60_ASSERTION_FILE,
                ExternalUserDatabaseProperties.getExternalUserKpiServiceJdbcProperties());
    }

    private static void verifyTableContent(final String table, final String csvFilePath, final Properties jdbcProperties) {
        final List<String> expectedRows = CsvReader.getCsvAsList(Boolean.TRUE, csvFilePath);
        final List<String> wantedColumns = CsvReader.getHeader(expectedRows);
        expectedRows.remove(0); //remove header row
        assertTableContent(table, wantedColumns, expectedRows, DatabaseProperties.getKpiServiceJdbcConnection(), jdbcProperties);
    }

    private static void verifyTableContentWithDynamicValues(final String table, final String csvFilePath, final Properties jdbcProperties,
            final Map<String, String> valueMap) {
        final List<String> expectedRows = CsvReader.getCsvAsList(Boolean.TRUE, csvFilePath);
        final List<String> expectedRowsWithReplacedValues = new ArrayList<>();

        for (final Map.Entry<String, String> entry : valueMap.entrySet()) {
            expectedRows.forEach(
                    item -> {
                        if (item.contains(entry.getKey())) {
                            expectedRowsWithReplacedValues.add(item.replaceAll(entry.getKey(), entry.getValue()));
                        }
                    });
        }

        final List<String> wantedColumns = CsvReader.getHeader(expectedRows);
        assertTableContent(table, wantedColumns, expectedRowsWithReplacedValues, DatabaseProperties.getKpiServiceJdbcConnection(), jdbcProperties);
    }
}
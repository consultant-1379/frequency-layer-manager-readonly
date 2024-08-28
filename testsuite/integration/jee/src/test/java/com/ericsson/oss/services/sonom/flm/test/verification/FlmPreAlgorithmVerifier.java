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

import static com.ericsson.oss.services.sonom.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static com.ericsson.oss.services.sonom.common.test.sql.SqlAssertions.assertTableContent;
import static com.ericsson.oss.services.sonom.flm.test.util.ExternalUserDatabaseProperties.getExternalUserKpiServiceJdbcProperties;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;
import com.ericsson.oss.services.sonom.flm.test.util.CsvReader;

/**
 * Class used to encapsulate the verification of the KPIs calculated for FLM by the KPI calculator (both external views and internal tables).
 */
public final class FlmPreAlgorithmVerifier {
    public static final String CELL_SECTOR_1440_ASSERTION_FILE = "csv-assertions/kpi_exporter/cell_sector_1440_kpis_assertions.csv";
    public static final String CELL_GUID_FLM_60_ASSERTION_FILE = "csv-assertions/kpi_exporter/cell_guid_flm_60_kpis_assertions.csv";
    public static final String CELL_SECTOR_FLM_1440_ASSERTION_FILE = "csv-assertions/kpi_exporter/cell_sector_flm_1440_kpis_assertions.csv";
    public static final String CELL_GUID_FLM_60_ASSERTION_FILE_FOR_PA = "csv-assertions/kpi_exporter/cell_guid_flm_60_kpis_assertions_for_pa.csv";
    public static final String SECTOR_FLM_60_ASSERTION_FILE_FOR_PA = "csv-assertions/kpi_exporter/sector_flm_60_kpis_assertions_for_pa.csv";
    public static final String KPI_CELL_GUID_1440_ASSERTION_FILE = "csv-assertions/kpi_service_user/kpi_cell_guid_1440_assertions.csv";
    public static final String KPI_CELL_SECTOR_1440_ASSERTION_FILE = "csv-assertions/kpi_service_user/kpi_cell_sector_1440_assertions.csv";
    public static final String KPI_CELL_SECTOR_FLM_1440_ASSERTION_FILE = "csv-assertions/kpi_service_user/kpi_cell_sector_flm_1440_assertions.csv";
    public static final String KPI_CELL_SECTOR_ASSERTION_FILE = "csv-assertions/kpi_service_user/kpi_cell_sector_assertions.csv";
    public static final String KPI_SECTOR_ASSERTION_FILE = "csv-assertions/kpi_service_user/kpi_sector_assertions.csv";
    public static final String KPI_CELL_GUID_FLM_60_ASSERTION_FILE = "csv-assertions/kpi_service_user/kpi_cell_guid_flm_60_assertions.csv";
    public static final String KPI_SECTOR_1440_ASSERTION_FILE = "csv-assertions/kpi_service_user/kpi_sector_1440_assertions.csv";
    public static final String KPI_FREQBAND_BANDWIDTH_TARGET_THROUGHPUT_R_1440_ASSERTION_FILE = "csv-assertions/kpi_service_user/kpi_freqband_bandwidth_r_flm_1440_assertions.csv";
    public static final String KPI_CELL_GUID_FLM_1440_ASSERTION_FILE = "csv-assertions/kpi_service_user/kpi_cell_guid_flm_1440_assertions.csv";
    private static final Logger LOGGER = LoggerFactory.getLogger(FlmPreAlgorithmVerifier.class);
    private static final String NO_ERROR_MESSAGE = null;
    private static final FlmPreAlgorithmVerifier INSTANCE = new FlmPreAlgorithmVerifier();

    private FlmPreAlgorithmVerifier() {
    }

    public static FlmPreAlgorithmVerifier verifyTableContent(final ContentDatasource datasource) {
        LOGGER.info("----------------------------------------------------------------------------------------");
        LOGGER.info("Table: {} is checked against {}", datasource.getTableName(), datasource.getCsvFilePath());

        final List<String> expectedRows = CsvReader.getCsvAsList(Boolean.TRUE, datasource.getCsvFilePath());
        final List<String> wantedColumns = CsvReader.getHeader(expectedRows);
        expectedRows.remove(0); //remove header row

        assertTableContent(datasource.getTableName(),
                wantedColumns,
                expectedRows,
                DatabaseProperties.getKpiServiceJdbcConnection(),
                datasource.getProperties());

        return INSTANCE;
    }

    public static FlmPreAlgorithmVerifier verifyTableContentWithDynamicValues(final DynamicContentDataSource datasource) {
        LOGGER.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        LOGGER.info("Table: {} is checked against {}", datasource.getTableName(), datasource.getCsvFilePath());

        final List<String> expectedRows = CsvReader.getCsvAsList(Boolean.TRUE, datasource.getCsvFilePath());
        final List<String> expectedRowsWithReplacedValues = new ArrayList<>();

        for (String currentRow : expectedRows) {
            for (final Entry<String, String> dynamicValue : datasource.getValueMap().entrySet()) {
                currentRow = currentRow.replaceAll(dynamicValue.getKey(), dynamicValue.getValue());
            }
            expectedRowsWithReplacedValues.add(currentRow);
        }

        final List<String> wantedColumns = CsvReader.getHeader(expectedRows);
        expectedRowsWithReplacedValues.remove(0); //remove header row

        LOGGER.info("Filter: {} is applied on table: {}",
                datasource.getFilter().isEmpty()
                        ? "None"
                        : datasource.getFilter(),
                datasource.getTableName());
        assertTableContent(NO_ERROR_MESSAGE,
                datasource.getTableName(),
                datasource.getFilter(),
                wantedColumns,
                expectedRowsWithReplacedValues,
                DatabaseProperties.getKpiServiceJdbcConnection(),
                datasource.getProperties());

        return INSTANCE;
    }

    public FlmPreAlgorithmVerifier thenVerifyTableContent(final ContentDatasource dataSource) {
        return verifyTableContent(dataSource);
    }

    public FlmPreAlgorithmVerifier thenVerifyTableContentWithDynamicValues(final DynamicContentDataSource datasource) {
        return verifyTableContentWithDynamicValues(datasource);
    }

    public enum TableSource {
        CELL_GUID_FLM_60_VIEW("cell_guid_flm_60_kpis", getExternalUserKpiServiceJdbcProperties()),
        SECTOR_FLM_60_VIEW("sector_flm_60_kpis", getExternalUserKpiServiceJdbcProperties()),
        CELL_SECTOR_1440_VIEW("cell_sector_1440_kpis", getExternalUserKpiServiceJdbcProperties()),
        CELL_SECTOR_FLM_1440_VIEW("cell_sector_flm_1440_kpis", getExternalUserKpiServiceJdbcProperties()),
        KPI_CELL_GUID_1440_TABLE("kpi_cell_guid_1440", getKpiServiceJdbcProperties()),
        KPI_CELL_SECTOR_FLM_1440_TABLE("kpi_cell_sector_flm_1440", getKpiServiceJdbcProperties()),
        KPI_CELL_SECTOR_1440_TABLE("kpi_cell_sector_1440", getKpiServiceJdbcProperties()),
        KPI_CELL_SECTOR_TABLE("kpi_cell_sector", getKpiServiceJdbcProperties()),
        KPI_SECTOR_TABLE("kpi_sector", getKpiServiceJdbcProperties()),
        KPI_CELL_GUID_FLM_60_TABLE("kpi_cell_guid_flm_60", getKpiServiceJdbcProperties()),
        KPI_SECTOR_1440_TABLE("kpi_sector_1440", getKpiServiceJdbcProperties()),
        KPI_FREQBAND_BANDWIDTH_TARGET_THROUGHPUT_R_1440_TABLE("kpi_freqband_bandwidth_r_flm_1440", getKpiServiceJdbcProperties()),
        KPI_CELL_GUID_FLM_1440_TABLE("kpi_cell_guid_flm_1440", getKpiServiceJdbcProperties());

        private final String tableName;
        private final Properties properties;

        TableSource(final String tableName, final Properties properties) {
            this.tableName = tableName;
            this.properties = properties;
        }

        public String getTableName() {
            return tableName;
        }

        public Properties getProperties() {
            return properties;
        }
    }

    public static class ContentDatasource {
        protected final TableSource tablesource;
        protected final String csvFilePath;

        private ContentDatasource(final TableSource tablesource, final String csvFilePath) {
            this.tablesource = tablesource;
            this.csvFilePath = csvFilePath;
        }

        public static ContentDatasource of(final TableSource tablesource, final String csvFilePath) {
            return new ContentDatasource(tablesource, csvFilePath);
        }

        public String getTableName() {
            return tablesource.getTableName();
        }

        public Properties getProperties() {
            return tablesource.getProperties();
        }

        public String getCsvFilePath() {
            return csvFilePath;
        }
    }

    public static final class DynamicContentDataSource extends ContentDatasource {
        private final ValueMap valueMap;

        private DynamicContentDataSource(final TableSource tablesource, final String csvFilePath, final ValueMap valueMap) {
            super(tablesource, csvFilePath);
            this.valueMap = valueMap;
        }

        public static DynamicContentDataSource of(final TableSource tablesource, final String csvFilePath, final ValueMap valueMap) {
            return new DynamicContentDataSource(tablesource, csvFilePath, valueMap);
        }

        public Map<String, String> getValueMap() {
            return valueMap.getValues();
        }

        public String getFilter() {
            return valueMap.getFilter();
        }
    }

    public static final class ValueMap {
        private final Map<String, String> values;
        private String filter = StringUtils.EMPTY;

        public ValueMap(final Map<String, String> values) {
            this.values = new HashMap<>(values);
        }

        public ValueMap(final Map<String, String> values, final String filter) {
            this(values);
            this.filter = filter;
        }

        public Map<String, String> getValues() {
            return Collections.unmodifiableMap(values);
        }

        public String getFilter() {
            return filter;
        }
    }

    public static final class ValueMapBuilder {
        private static final String EXECUTION_ID_KEY = "<EXECUTION_ID>";
        private static final String LAST_BUSINESS_DAY = "<LAST_BUSINESS_DAY>";

        private final Map<String, String> valueMap;
        private String filter;

        private ValueMapBuilder() {
            valueMap = new HashMap<>();
            filter = StringUtils.EMPTY;
        }

        private ValueMapBuilder(final ValueMap valueMap) {
            this.valueMap = new HashMap<>(valueMap.getValues());
            filter = valueMap.getFilter();
        }

        public static ValueMapBuilder create() {
            return new ValueMapBuilder();
        }

        public static ValueMapBuilder from(final ValueMap valueMap) {
            return new ValueMapBuilder(valueMap);
        }

        public ValueMapBuilder addExecutionId(final String executionId) {
            valueMap.put(EXECUTION_ID_KEY, executionId);
            return this;
        }

        /**
         * Due to {@code weekend days} setup last business day is considered to be today minus three days.
         * 
         * @return instance of the builder
         */
        public ValueMapBuilder addLastBusinessDay() {
            valueMap.put(LAST_BUSINESS_DAY, LocalDate.now(ZoneOffset.UTC).minusDays(3).toString());
            return this;
        }

        public ValueMapBuilder applyFilter(final String filter, final Object... args) {
            this.filter = String.format(filter, args);
            return this;
        }

        public ValueMapBuilder discardFilter() {
            filter = StringUtils.EMPTY;
            return this;
        }

        public ValueMapBuilder add(final String key, final String value) {
            valueMap.put(key, value);
            return this;
        }

        public ValueMap build() {
            return new ValueMap(valueMap, filter);
        }
    }
}

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

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;
import com.ericsson.oss.services.sonom.flm.test.util.CsvReader;

/**
 * Class used to encapsulate the verification of the Cell Settings for FLM.
 */
public final class CellSettingsVerifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellSettingsVerifier.class);

    private static final String CELL_CONFIGURATION_TABLE = "cell_configuration";
    private static final String CELL_SETTINGS_ASSERTION_FILE = "csv-assertions/flm_service_user/cell_settings_assertions.csv";

    private CellSettingsVerifier() {
    }

    /**
     * As user kpi_service_user verify Cell Settings are applied and stored in the relevant table in the flm_service_db.
     */
    public static void verifyCellSettingsAreStoredInDatabase(final String executionId) {
        verifyTableContent(CELL_CONFIGURATION_TABLE, CELL_SETTINGS_ASSERTION_FILE, DatabaseProperties.getFlmJdbcProperties(), executionId);
    }

    private static void verifyTableContent(final String table, final String csvFilePath, final Properties jdbcProperties, final String executionId) {
        final List<String> expectedRows = populateExpectedCellSettingsExecutionId(CsvReader.getCsvAsList(Boolean.TRUE, csvFilePath), executionId);
        final List<String> wantedColumns = CsvReader.getHeader(expectedRows);
        expectedRows.remove(0); //remove header row

        LOGGER.info("Expected rows: {}", expectedRows.size());

        assertTableContent(table, wantedColumns, expectedRows, DatabaseProperties.getFlmJdbcConnection(), jdbcProperties);
    }

    private static List<String> populateExpectedCellSettingsExecutionId(final List<String> expectedRows, final String executionId) {
       return expectedRows.stream().map(row -> row.replaceAll("<execution_id>", executionId)).collect(Collectors.toList());
    }
}
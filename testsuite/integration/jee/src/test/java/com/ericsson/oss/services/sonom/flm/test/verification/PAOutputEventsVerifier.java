/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
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

import static com.ericsson.oss.services.sonom.common.env.DatabaseProperties.getFlmJdbcConnection;
import static com.ericsson.oss.services.sonom.common.env.DatabaseProperties.getFlmJdbcProperties;
import static com.ericsson.oss.services.sonom.common.test.rest.ResponseAssertions.assertThat;
import static com.ericsson.oss.services.sonom.common.test.sql.SqlAssertions.assertCountQuery;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.DEGRADATION_STATUS;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.SECTOR_ID;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.test.util.CsvReader;
import com.google.gson.Gson;

public final class PAOutputEventsVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(PAOutputEventsVerifier.class);
    private static final Gson GSON = new Gson();
    private static final String CSV_DELIMITER = ", ";
    private static final String PA_OUTPUT_EVENTS_TABLE = "pa_output_events";
    private static final String PA_OUTPUT_EVENTS_ASSERTION_FILE = "csv-assertions/flm_service_user/pa_output_events_assertions.csv";
    private static final String PA_OUTPUT_ELEMENTS_COUNT_QUERY = "SELECT COUNT(*) FROM %s WHERE %s='%s'";
    private static final String YESTERDAY = "<YESTERDAY>";

    private PAOutputEventsVerifier() {
    }

    public static void assertSentAndReceivedPolicyOutputEventsCountIsEqual(final PAExecution paExecution) {
        final String paOutputEventsCountQuery = String.format(PA_OUTPUT_ELEMENTS_COUNT_QUERY, PA_OUTPUT_EVENTS_TABLE, PA_EXECUTION_ID,
                paExecution.getId());

        assertCountQuery(paExecution.getNumPaPolicyInputEventsSent(), paOutputEventsCountQuery, getFlmJdbcConnection(), getFlmJdbcProperties());
    }

    public static void verifyDegradationStatus(final PAExecution paExecution) {
        final List<String> expectedRows = CsvReader.getCsvAsListOfRowsContainingString(Boolean.TRUE, PA_OUTPUT_EVENTS_ASSERTION_FILE,
                paExecution.getId());
        final List<String> wantedColumns = CsvReader.getHeader(expectedRows);
        expectedRows.remove(0); // Remove header row

        final List<PaPolicyOutputEvent> actualOutputEvents = getPaOutputEventsByPaExecutionId(paExecution.getId());

        assertThat(actualOutputEvents).hasSize(expectedRows.size());

        for (final String row : expectedRows) {
            final List<String> expectedValues = Arrays.asList(row.split(CSV_DELIMITER));
            expectedValues.replaceAll(s -> s.replaceAll(YESTERDAY, LocalDate.now().minusDays(1).toString()));

            final PaPolicyOutputEvent outputEventToVerify = getOutputEventBySectorId(actualOutputEvents, expectedValues.get(0));

            assertThat(outputEventToVerify).isNotNull();
            assertOutputEventMatchesExpectedValues(expectedValues, wantedColumns, outputEventToVerify);
        }
    }

    private static void assertOutputEventMatchesExpectedValues(final List<String> expectedValues, final List<String> wantedColumns,
                                                               final PaPolicyOutputEvent outputEventToVerify) {
        for (int i = 0; i < wantedColumns.size(); i++) {
            assertOnWantedColumn(wantedColumns.get(i), expectedValues.get(i), outputEventToVerify);
        }
    }

    private static void assertOnWantedColumn(final String column, final String expectedValue, final PaPolicyOutputEvent outputEvent) {
        LOGGER.info("Asserting PA Policy Output Event '{}' contains {} '{}'", outputEvent.getSector().getSectorId(), column, expectedValue);
        switch (column) {
            case PA_EXECUTION_ID:
                assertThat(outputEvent.getPaExecutionId()).isEqualTo(expectedValue);
                break;
            case SECTOR_ID:
                assertThat(outputEvent.getSector().getSectorId()).isEqualTo(expectedValue);
                break;
            case DEGRADATION_STATUS:
                if (outputEvent.getDegradationStatus().getDegradedSectorKpis().isEmpty() && outputEvent.getDegradationStatus().getDegradedCellKpis().isEmpty()) {
                    assertThat(GSON.toJson(outputEvent.getDegradationStatus())).isEqualTo(expectedValue);
                } else {
                    sortPaPolicyOutputEventTimeStamps(outputEvent);
                    assertThat(GSON.toJson(outputEvent.getDegradationStatus())).isEqualTo(expectedValue);
                }
                break;
            default:
                break;
        }
    }

    private static void sortPaPolicyOutputEventTimeStamps(final PaPolicyOutputEvent outputEvent) {
        final boolean degradedSectorKpisNotEmpty = outputEvent.getDegradationStatus().getDegradedSectorKpis().isEmpty() ? false : true;
        final boolean degradedCellKpisNotEmpty = outputEvent.getDegradationStatus().getDegradedCellKpis().isEmpty() ? false : true;

        if (degradedSectorKpisNotEmpty && degradedCellKpisNotEmpty) {
            sortPaPolicyOutputEventDegradedSectorKpiTimestamps(outputEvent);
            sortPaPolicyOutputEventDegradedCellKpiTimestamps(outputEvent);
        } else if (degradedSectorKpisNotEmpty) {
            sortPaPolicyOutputEventDegradedSectorKpiTimestamps(outputEvent);
        } else if (degradedCellKpisNotEmpty) {
            sortPaPolicyOutputEventDegradedCellKpiTimestamps(outputEvent);
        }
    }

    private static void sortPaPolicyOutputEventDegradedSectorKpiTimestamps(final PaPolicyOutputEvent outputEvent) {
        outputEvent.getDegradationStatus().getDegradedSectorKpis().values()
                .forEach((v) -> v.getSectorIdToDegradedTimestamps().values().forEach(Collections::sort));
    }

    private static void sortPaPolicyOutputEventDegradedCellKpiTimestamps(final PaPolicyOutputEvent outputEvent) {
        outputEvent.getDegradationStatus().getDegradedCellKpis().values()
                .forEach((v) -> v.getOssIdToFdnToDegradedTimestamps().values().forEach((v1) -> v1.values().forEach(Collections::sort)));
    }

    private static List<PaPolicyOutputEvent> getPaOutputEventsByPaExecutionId(final String flmExecutionId) {
        final PAOutputEventDao outputEventDao = new PAOutputEventDaoImpl(3, 60);
        List<PaPolicyOutputEvent> paOutputEvents = Collections.emptyList();
        try {
            paOutputEvents = outputEventDao.getPaPolicyOutputEventById(flmExecutionId);
        } catch (final SQLException e) {
            LOGGER.error("{}: Failed to retrieve PAOutputEvents with PA execution ID '{}'", SQLException.class.getSimpleName(), flmExecutionId, e);
        }

        return paOutputEvents;
    }

    private static PaPolicyOutputEvent getOutputEventBySectorId(final List<PaPolicyOutputEvent> outputEvents, final String sectorId) {
        for (final PaPolicyOutputEvent outputEvent : outputEvents) {
            if (sectorId.equals(String.valueOf(outputEvent.getSector().getSectorId()))) {
                return outputEvent;
            }
        }

        LOGGER.error("PA Policy Output Event not found with sector_id '{}'", sectorId);
        return null;
    }
}

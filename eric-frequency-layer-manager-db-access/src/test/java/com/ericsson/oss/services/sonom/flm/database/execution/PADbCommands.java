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

package com.ericsson.oss.services.sonom.flm.database.execution;

import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.DEGRADATION_STATUS;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.FLM_EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.ID;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.NUM_PA_POLICY_INPUT_EVENTS_SENT;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_WINDOW;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_WINDOW_END_TIME;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.PA_WINDOW_START_TIME;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.SCHEDULE;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.SECTOR_ID;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.STATE;
import static com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants.STATE_MODIFIED_TIME;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;
import com.google.gson.Gson;

/**
 * Class which contains SQL queries for the {@link PAExecutionDaoImplTest} and {@link PAExecutionDaoImplTest} unit tests.
 */
final class PADbCommands {

    private static final Gson GSON = new Gson();

    private PADbCommands() {
        // intentionally private, utility class
    }

    static List<String> dropTable(final String table) {
        return Collections.singletonList(String.format("DROP TABLE IF EXISTS %s", table));
    }

    static List<String> deleteAllEntriesFromTable(final String table) {
        return Collections.singletonList(String.format("DELETE FROM %s", table));
    }

    static List<String> createPaExecutionTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS pa_executions(%n" +
                                "    %s VARCHAR(257) PRIMARY KEY,%n" +
                                "    %s VARCHAR(100),%n" +
                                "    %s INTEGER,%n" +
                                "    %s TIMESTAMP,%n" +
                                "    %s TIMESTAMP,%n" +
                                "    %s VARCHAR(50),%n" +
                                "    %s TIMESTAMP,%n" +
                                "    %s VARCHAR(255) NOT NULL,%n" +
                                "    %s INTEGER NOT NULL%n" +
                                "    );",
                        ID,
                        SCHEDULE,
                        PA_WINDOW,
                        PA_WINDOW_START_TIME,
                        PA_WINDOW_END_TIME,
                        STATE,
                        STATE_MODIFIED_TIME,
                        FLM_EXECUTION_ID,
                        NUM_PA_POLICY_INPUT_EVENTS_SENT));
    }

    static List<String> createPaOutputEventsTable() {
        return Collections.singletonList(
                String.format(
                        "CREATE TABLE IF NOT EXISTS pa_output_events(%n" +
                                "    %s VARCHAR(257),%n" +
                                "    %s VARCHAR(257),%n" +
                                "    %s INTEGER,%n" +
                                "    %s BIGINT ,%n" +
                                "    %s TEXT ,%n" +
                                "    PRIMARY KEY (%s, %s) %n" +
                                "    );",
                        FLM_EXECUTION_ID,
                        PA_EXECUTION_ID,
                        PA_WINDOW,
                        SECTOR_ID,
                        DEGRADATION_STATUS,
                        PA_EXECUTION_ID, SECTOR_ID));
    }

    static List<String> insertExecution(final List<PAExecution> paExecutions) {
        final List<String> insertQueries = new ArrayList<>(paExecutions.size());
        for (final PAExecution paExecution : paExecutions) {
            insertQueries.add(String.format("INSERT INTO pa_executions (%s, %s, %s, %s, %s, %s, %s, %s, %s) " +
                    "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                    ID,
                    SCHEDULE,
                    PA_WINDOW,
                    PA_WINDOW_START_TIME,
                    PA_WINDOW_END_TIME,
                    STATE,
                    STATE_MODIFIED_TIME,
                    FLM_EXECUTION_ID,
                    NUM_PA_POLICY_INPUT_EVENTS_SENT,
                    paExecution.getId(),
                    paExecution.getSchedule(),
                    paExecution.getPaWindow(),
                    paExecution.getPaWindowStartTime(),
                    paExecution.getPaWindowEndTime(),
                    paExecution.getState(),
                    Timestamp.from(Instant.now()),
                    paExecution.getFlmExecutionId(),
                    paExecution.getNumPaPolicyInputEventsSent()));
        }
        return insertQueries;
    }

    static List<String> insertPaPolicyOutputEvent(final List<PaPolicyOutputEvent> paPolicyOutputEvents) {
        final List<String> insertQueries = new ArrayList<>(paPolicyOutputEvents.size());
        for (final PaPolicyOutputEvent event : paPolicyOutputEvents) {
            insertQueries.add(String.format("INSERT INTO pa_output_events (%s, %s, %s, %s, %s) " +
                    "VALUES ('%s', '%s', '%s', '%s', '%s')",
                    FLM_EXECUTION_ID,
                    PA_EXECUTION_ID,
                    PA_WINDOW,
                    SECTOR_ID,
                    DEGRADATION_STATUS,
                    event.getFlmExecutionId(),
                    event.getPaExecutionId(),
                    event.getPaWindow(),
                    Long.parseLong(event.getSector().getSectorId()),
                    GSON.toJson(event.getDegradationStatus())));
        }
        return insertQueries;
    }
}

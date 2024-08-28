/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.pa.scheduler;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PASettings;

/**
 * Responsible for creating the {@link PAExecution} (s) for a particular {@link Execution} based on the following {@link PASettings}
 * <ul>
 * <li>{@link PASettings#NUMBER_OF_PA_EXECUTIONS}</li>
 * <li>{@link PASettings#INITIAL_PA_WINDOW_OFFSET_TIME_IN_MINUTES}</li>
 * <li>{@link PASettings#PA_EXECUTION_OFFSET_TIME_IN_MINUTES}</li>
 * <li>{@link PASettings#PA_WINDOW_DURATION_IN_MINUTES}</li>
 * </ul>
 */
class SchedulesMaker {

    private SchedulesMaker() {
        //since all methods in class is static
    }

    /**
     * Makes the cron schedules and pa windows for the {@link PAExecution}(s) based on the start time of the given {@link Execution}.
     * 
     * @param flmExecution
     *            the {@link Execution} to make the {@link PAExecution}(s) for
     * @return a {@link List} of {@link PAExecution}(s)
     */
    static List<PAExecution> makeSchedules(final Execution flmExecution) {
        final List<PAExecution> schedules = new ArrayList<>(PASettings.NUMBER_OF_PA_EXECUTIONS);
        // Need to truncate this due to millis in the flm execution start time see SONP-49138
        Instant paWindowStartInstant = flmExecution.getStartTime().toInstant().truncatedTo(ChronoUnit.SECONDS).plus(
                PASettings.INITIAL_PA_WINDOW_OFFSET_TIME_IN_MINUTES,
                ChronoUnit.MINUTES);
        Instant paWindowEndInstant;
        Instant paExecutionInstant;

        for (int paWindow = 1; paWindow <= PASettings.NUMBER_OF_PA_EXECUTIONS; paWindow++) {
            paWindowEndInstant = paWindowStartInstant.plus(PASettings.PA_WINDOW_DURATION_IN_MINUTES, ChronoUnit.MINUTES);
            paExecutionInstant = paWindowEndInstant.plus(PASettings.PA_EXECUTION_OFFSET_TIME_IN_MINUTES, ChronoUnit.MINUTES);
            schedules.add(new PAExecution(paWindow, makeCron(paExecutionInstant), Timestamp.from(paWindowStartInstant),
                    alignPAWindowEndTimeWithKpiServiceExpectations(paWindowEndInstant), flmExecution.getId()));
            paWindowStartInstant = paWindowEndInstant;
        }
        return schedules;
    }

    /*
     * Method to align the pa window end time with what the kpi service expects otherwise we end up requesting the next hour kpi's aswell. i.e. Shave
     * one second off.
     * @since SONP-49138
     */
    private static Timestamp alignPAWindowEndTimeWithKpiServiceExpectations(final Instant paWindowEndInstant) {
        return Timestamp.from(paWindowEndInstant.minus(1, ChronoUnit.SECONDS));
    }

    private static String makeCron(final Instant paExecutionInstant) {
        //Flm is using the local time zone so we follow suit here to keep aligned.
        final LocalDateTime ldt = Timestamp.from(paExecutionInstant).toLocalDateTime();

        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s",
                ldt.get(ChronoField.SECOND_OF_MINUTE),
                ldt.get(ChronoField.MINUTE_OF_HOUR),
                ldt.get(ChronoField.HOUR_OF_DAY),
                ldt.get(ChronoField.DAY_OF_MONTH),
                ldt.get(ChronoField.MONTH_OF_YEAR),
                "?",
                ldt.get(ChronoField.YEAR));
    }
}

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

package com.ericsson.oss.services.sonom.flm.service.api.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class to get the valid business day.
 */
public final class WeekendDay {
    private static final int APPLY_NO_WEEKEND_DAYS = -1;
    private static final long ONE_BASED_INDEX_OFFSET = 1L;
    private static final String COMMA = ",";

    private final List<DayOfWeek> weekendDays;

    private WeekendDay(final String weekendDays) {
        this.weekendDays = parseWeekendDays(weekendDays);
    }

    /**
     * Creates an instance of weekend days from the {@link WeekendDay#COMMA} separated weekend days.
     * 
     * @param weekendDays
     *            weekend days to parse.
     * @return an instance of weekend days.
     */
    public static WeekendDay of(final String weekendDays) {
        return new WeekendDay(weekendDays);
    }

    /**
     * This method will check for last business day based on weekend, If we pass date which comes in weekend will return previous execution date.
     *
     * @param date
     *            {@link LocalDate} on which {@code weekend days} are applied
     * @return {@link LocalDate} of the last business day
     */
    public LocalDate calculateLastBusinessDayBasedOnWeekend(final LocalDate date) {
        if (weekendDays.isEmpty()) {
            return date;
        }

        final long weekendDayOffset = weekendDays.indexOf(date.getDayOfWeek());

        if (weekendDays.contains(date.getDayOfWeek())) {
            return date.minusDays(weekendDayOffset);
        }

        final int lastWeekendDay;
        final List<DayOfWeek> weekBoundary = Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.MONDAY);
        if (weekendDays.containsAll(weekBoundary)) {
            lastWeekendDay = 1;
        } else {
            lastWeekendDay = weekendDays.stream().mapToInt(DayOfWeek::getValue).max().getAsInt();
        }

        if (weekendDayOffset == APPLY_NO_WEEKEND_DAYS && lastWeekendDay == date.getDayOfWeek().minus(1L).getValue()) {
            return date.minusDays(weekendDays.size());
        }

        return calculateLastBusinessDay(date);
    }

    /**
     * Calculates last business day based on {@link WeekendDay#weekendDays}.
     * <p>
     * If the passed date is not affected by the {@code weekend days} logic than it returns the parameter as the last business day.
     * 
     * @param date
     *            {@link LocalDate} on which {@code weekend days} are applied
     * @return {@link LocalDate} of the last business day
     */
    public LocalDate calculateLastBusinessDay(final LocalDate date) {
        final long weekendDayOffset = weekendDays.indexOf(date.getDayOfWeek());

        return weekendDayOffset == APPLY_NO_WEEKEND_DAYS
                ? date
                : date.minusDays(convertZeroBasedIndexToDayToSubtract(weekendDayOffset));
    }

    private static long convertZeroBasedIndexToDayToSubtract(final long weekendDayOffset) {
        return weekendDayOffset + ONE_BASED_INDEX_OFFSET;
    }

    private static List<DayOfWeek> parseWeekendDays(final String weekendDays) {
        return Objects.isNull(weekendDays) || weekendDays.isEmpty()
                ? Collections.emptyList()
                : Arrays.stream(weekendDays.split(COMMA))
                        .map(String::trim)
                        .map(weekendDay -> weekendDay.toUpperCase(Locale.ENGLISH))
                        .map(DayOfWeek::valueOf)
                        .collect(Collectors.toList());
    }
}

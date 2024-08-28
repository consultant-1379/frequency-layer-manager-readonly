/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021 - 2022
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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link WeekendDay} class.
 */
public final class WeekendDayTest {

    private static final LocalDate DATE = LocalDate.of(2_021, 3, 19);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String weekendDays;

    @Test
    public void whenTheDayBeforeAndTheDayBeforeThatIsWeekend_thenTodayIsReturned() {
        //  date = FRIDAY
        //  weekendDays = [WEDNESDAY,THURSDAY]
        //  expected = FRIDAY
        final String dayBefore = getTheNameOfTheDayBeforeOrAfterNDays(-1);
        final String dayBeforeThat = getTheNameOfTheDayBeforeOrAfterNDays(-2);
        weekendDays = String.format("%s,%s", dayBeforeThat, dayBefore);

        final LocalDate actual = WeekendDay.of(weekendDays).calculateLastBusinessDay(DATE);

        assertThat(actual).isEqualTo(DATE);
    }

    @Test
    public void whenTheDayAfterAndTheDayAfterThatIsWeekend_thenTodayIsReturned() {
        //  date = FRIDAY
        //  weekendDays = [SATURDAY,SUNDAY]
        //  expected = FRIDAY
        final String dayAfter = getTheNameOfTheDayBeforeOrAfterNDays(1);
        final String dayAfterThat = getTheNameOfTheDayBeforeOrAfterNDays(2);
        weekendDays = String.format("%s,%s", dayAfterThat, dayAfter);

        final LocalDate actual = WeekendDay.of(weekendDays).calculateLastBusinessDay(DATE);

        assertThat(actual).isEqualTo(DATE);
    }

    @Test
    public void whenTodayAndTheDayBeforeIsWeekend_then2DaysBeforeIsReturned() {
        //  date = FRIDAY
        //  weekendDays = [THURSDAY,FRIDAY]
        //  expected = WEDNESDAY
        final String today = getTheNameOfTheDayBeforeOrAfterNDays(0);
        final String dayBefore = getTheNameOfTheDayBeforeOrAfterNDays(-1);
        weekendDays = String.format("%s,%s", dayBefore, today);

        final LocalDate actual = WeekendDay.of(weekendDays).calculateLastBusinessDay(DATE);

        final LocalDate expected = DATE.minusDays(2);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void whenTodayIsWeekendDayAndWeekendDaysContainTwoDays_thenYesterdayIsReturned() {
        //  date = FRIDAY
        //  weekendDays = [FRIDAY,SATURDAY]
        //  expected = THURSDAY
        final String today = getTheNameOfTheDayBeforeOrAfterNDays(0);
        final String tomorrow = getTheNameOfTheDayBeforeOrAfterNDays(1);
        weekendDays = String.format("%s,%s", today, tomorrow);

        final LocalDate actual = WeekendDay.of(weekendDays).calculateLastBusinessDay(DATE);

        final LocalDate expected = DATE.minusDays(1);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void whenYesterdayIsWeekend_thenTodayIsReturned() {
        //  date = FRIDAY
        //  weekendDays = [THURSDAY]
        //  expected = FRIDAY
        weekendDays = getTheNameOfTheDayBeforeOrAfterNDays(-1);

        final LocalDate actual = WeekendDay.of(weekendDays).calculateLastBusinessDay(DATE);

        assertThat(actual).isEqualTo(DATE);
    }

    @Test
    public void whenTodayIsWeekend_then1DaysBeforeIsReturned() {
        //  date = FRIDAY
        //  weekendDays = [FRIDAY]
        //  expected = THURSDAY
        final String today = getTheNameOfTheDayBeforeOrAfterNDays(0);
        weekendDays = today;

        final LocalDate actual = WeekendDay.of(weekendDays).calculateLastBusinessDay(DATE);

        final LocalDate expected = DATE.minusDays(1);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void whenTomorrowIsWeekend_thenTodayIsReturned() {
        //  date = FRIDAY
        //  weekendDays = [SATURDAY]
        //  expected = FRIDAY
        final String tomorrow = getTheNameOfTheDayBeforeOrAfterNDays(1);
        weekendDays = tomorrow;
        final LocalDate actual = WeekendDay.of(weekendDays).calculateLastBusinessDay(DATE);

        assertThat(actual).isEqualTo(DATE);
    }

    @Test
    public void whenWeekendDaysIsEmpty_thenTodayIsReturned() {
        weekendDays = "";

        final LocalDate actual = WeekendDay.of(weekendDays).calculateLastBusinessDay(DATE);

        assertThat(actual).isEqualTo(DATE);
    }

    @Test
    public void whenWeekendDaysIsInvalid_thenExceptionIsThrown() {
        thrown.expect(IllegalArgumentException.class);
        WeekendDay.of("invalidDay");
    }

    private String getTheNameOfTheDayBeforeOrAfterNDays(final int numberOfDays) {
        return DATE.plusDays(numberOfDays).getDayOfWeek().name();
    }
}

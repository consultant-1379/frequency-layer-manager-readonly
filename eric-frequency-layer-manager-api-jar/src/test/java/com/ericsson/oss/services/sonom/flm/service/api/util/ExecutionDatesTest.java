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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExecutionDatesTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
    public ExpectedException thrown = ExpectedException.none();
    private  List<String> dates;

    private ExecutionDates objectUnderTest;

    @Before
    public void setUp() {
        dates = new ArrayList<>();
        dates.add("2021-05-06"); //Thursday
        dates.add("2021-05-05"); //Wednesday
        dates.add("2021-05-04"); //Tuesday
        dates.add("2021-05-03"); //Monday
        dates.add("2021-05-02"); //Sunday
        dates.add("2021-05-01"); //Saturday
        dates.add("2021-04-30"); //Friday
    }

    @Test
    public void testAllDays_whenSaturdayIsWeekend() {
        for (final String date : dates) {
            objectUnderTest = ExecutionDates.getExecutionDates(date, "Saturday");
            if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.SATURDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            }
            if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.SUNDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(2).toString());
            } else {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            }
        }

    }

    @Test
    public void testAllDays_whenSatAndSundayIsWeekend() {
        for (final String date : dates) {
            objectUnderTest = ExecutionDates.getExecutionDates(date, "Saturday,Sunday");
            if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.SATURDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            } else if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.SUNDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(2).toString());
            } else if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.MONDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(LocalDate.parse(date).minusDays(2).toString());
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(3).toString());
            } else {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            }
        }

    }

    @Test
    public void testAllDays_whenMondayAndTuesDayIsWeekend() {
        for (final String date : dates) {
            objectUnderTest = ExecutionDates.getExecutionDates(date, "Monday,Tuesday");
            if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.MONDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            } else if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.TUESDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(2).toString());
            } else if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.WEDNESDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(LocalDate.parse(date).minusDays(2).toString());
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(3).toString());
            } else {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            }
        }
    }

    @Test
    public void testAllDays_whenWedAndThursIsWeekend() {
        for (final String date : dates) {
            objectUnderTest = ExecutionDates.getExecutionDates(date, "Wednesday,Thursday");
            if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.WEDNESDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            } else if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.THURSDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(2).toString());
            } else if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.FRIDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(LocalDate.parse(date).minusDays(2).toString());
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(3).toString());
            } else {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            }
        }
    }

    @Test
    public void testAllDays_whenThursDayAndFridayIsWeekend() {
        for (final String date : dates) {
            objectUnderTest = ExecutionDates.getExecutionDates(date, "Thursday,Friday");
            if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.THURSDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            } else if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.FRIDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(2).toString());
            } else if (LocalDate.parse(date).getDayOfWeek() == DayOfWeek.SATURDAY) {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(LocalDate.parse(date).minusDays(2).toString());
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(3).toString());
            } else {
                softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
                softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
            }
        }
    }

    @Test
    public void testAllDays_whenWeekendIsEmpty() {
        for (final String date : dates) {
            objectUnderTest = ExecutionDates.getExecutionDates(date, "");
            softly.assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo(date);
            softly.assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo(LocalDate.parse(date).minusDays(1).toString());
        }
    }

    @Test
    public void whenWeekendDaysCrossWeekBoundary_thenCorrectDatesAreReturned() {
        objectUnderTest = ExecutionDates.getExecutionDates("2021-05-11", "Sunday,Monday");

        assertThat(objectUnderTest.getKpiExecutionStartDate()).isEqualTo("2021-05-08");
        assertThat(objectUnderTest.getKpiExecutionEndDate()).isEqualTo("2021-05-09");
    }
}

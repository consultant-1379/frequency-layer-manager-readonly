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

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TimeUtils} class.
 */
public class TimeUtilsTest {

    private static final LocalDate DATE = LocalDate.of(2_021, 4, 28);

    @Test
    public void whenDayBeforeCalledWithLocalDate_thenLocalDateIsReturnedWithDateOfYesterday() {
        assertThat(TimeUtils.dayBefore(DATE)).isEqualTo(DATE.minusDays(1));
    }

    @Test
    public void whenDayBeforeCalledWithLocalDateTime_thenLocalDateIsReturnedWithDateOfYesterday() {
        assertThat(TimeUtils.dayBefore(LocalDateTime.of(DATE, LocalTime.MIDNIGHT))).isEqualTo(DATE.minusDays(1));
    }

    @Test
    public void whenWeekBeforeCalledWithLocalDate_thenLocalDateIsReturnedWithDateOf7DaysBefore() {
        assertThat(TimeUtils.weekBefore(DATE)).isEqualTo(DATE.minusDays(7));
    }
}

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
package com.ericsson.oss.services.sonom.flm.test.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ScheduledKpiCalculationHelper} class.
 */
public class ScheduledKpiCalculationHelperTest {
    private static final LocalDate CHRISTMAS_2015 = LocalDate.of(2015, Month.DECEMBER, 25);

    @Test
    public void whenGettingScheduledTimeParamsForDate_validateReturnedMap() {
        assertThat(ScheduledKpiCalculationHelper.getScheduledKpiParametersMap(CHRISTMAS_2015))
                .hasSize(2)
                .containsEntry("param.start_date_time", "2015-12-25 02:00:00")
                .containsEntry("param.end_date_time", "2015-12-25 03:00:00");
    }
}

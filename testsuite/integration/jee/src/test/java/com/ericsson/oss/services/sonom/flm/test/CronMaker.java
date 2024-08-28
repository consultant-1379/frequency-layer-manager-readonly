/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

/**
 * Class for creating cron expressions.
 */
public final class CronMaker {

    private CronMaker() {
    }

    /**
     * This method returns cron expression with delay of specified offset time From the current date time.
     * @param offsetInSeconds
     *        delay offset specified in seconds.
     * @return String
     *        cron expression.
     */
    public static String now(final long offsetInSeconds) {
        return make(ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime(), offsetInSeconds);
    }

    /**
     * This method returns cron expression with delay of specified offset time from specified date time.
     * @param time
     *        date and time.
     * @param offsetInSeconds
     *        delay offset specified in seconds.
     * @return String
     *        cron expression.
     */
    public static String make(final LocalDateTime time, final long offsetInSeconds) {
        final LocalDateTime timePlusOffset = time.plusSeconds(offsetInSeconds);
        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s",
                timePlusOffset.get(ChronoField.SECOND_OF_MINUTE),
                timePlusOffset.get(ChronoField.MINUTE_OF_HOUR),
                timePlusOffset.get(ChronoField.HOUR_OF_DAY),
                timePlusOffset.get(ChronoField.DAY_OF_MONTH),
                timePlusOffset.get(ChronoField.MONTH_OF_YEAR),
                "?", "*");
    }
}

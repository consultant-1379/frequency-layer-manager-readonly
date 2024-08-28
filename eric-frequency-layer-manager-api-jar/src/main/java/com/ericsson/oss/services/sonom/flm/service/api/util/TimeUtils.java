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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Util class to common time modifications.
 */
public final class TimeUtils {
    private TimeUtils() {
    }

    /**
     * Method to subtract one day from {@link LocalDate}.
     * @param date {@link LocalDate} to subtract from.
     * @return {@link LocalDate} one day before the entered parameter.
     */
    public static LocalDate dayBefore(final LocalDate date) {
        return date.minusDays(1);
    }

    /**
     * Method to subtract one day from {@link LocalDateTime}.
     * @param date {@link LocalDateTime} to subtract from.
     * @return {@link LocalDate} one day before the entered parameter.
     */
    public static LocalDate dayBefore(final LocalDateTime date) {
        return date.minusDays(1).toLocalDate();
    }

    /**
     * Method to subtract seven days from {@link LocalDate}.
     * @param date {@link LocalDate} to subtract from.
     * @return {@link LocalDate} seven days before the entered parameter.
     */
    public static LocalDate weekBefore(final LocalDate date) {
        return date.minusDays(7);
    }
}

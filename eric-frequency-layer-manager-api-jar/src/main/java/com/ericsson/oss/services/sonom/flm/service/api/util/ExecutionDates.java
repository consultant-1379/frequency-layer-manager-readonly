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
import java.time.format.DateTimeFormatter;

/**
 * Util class to date and time modifications.
 */
public class ExecutionDates {

    private final String executionDate;
    private final String kpiExecutionStartDate;
    private final String kpiExecutionEndDate;

    public ExecutionDates(final String executionDate, final String kpiExecutionStartDate, final String kpiExecutionEndDate) {
        this.executionDate = executionDate;
        this.kpiExecutionStartDate = kpiExecutionStartDate;
        this.kpiExecutionEndDate = kpiExecutionEndDate;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public String getKpiExecutionStartDate() {
        return kpiExecutionStartDate;
    }

    public String getKpiExecutionEndDate() {
        return kpiExecutionEndDate;
    }

    /**
     * Method to calculate execution days taking into consideration the weekend handling.
     * 
     * @param executionDate
     *            current running execution date
     * @param weekendDays
     *            weekends to select from
     * @return {@link ExecutionDates} execution dates taking into consideration the weekend handling.
     */
    public static ExecutionDates getExecutionDates(final String executionDate, final String weekendDays) {
        final LocalDate executionLocalDate = LocalDate.parse(executionDate);

        final String endDateTime = WeekendDay.of(weekendDays).calculateLastBusinessDayBasedOnWeekend(executionLocalDate)
                .format(DateTimeFormatter.ISO_DATE);

        final String startDateTime = LocalDate.parse(endDateTime).minusDays(1).toString();

        return new ExecutionDates(executionDate, startDateTime, endDateTime);
    }
}

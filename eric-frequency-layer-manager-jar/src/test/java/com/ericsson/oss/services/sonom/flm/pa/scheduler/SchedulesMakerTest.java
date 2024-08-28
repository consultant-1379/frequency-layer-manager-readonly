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

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * Unit tests for {@link SchedulesMaker} class.
 */
public class SchedulesMakerTest {
    private static final int EXPECTED_NUMBER_OF_PA_WINDOWS = 3;
    private static final String FLM_TEST_ID = "flm_test_id";
    private static final String SCHEDULE_2AM = "0 0 2 ? * * *";
    private static final String SCHEDULE_6AM = "0 0 6 ? * * *";
    private Timestamp window1End;
    private Timestamp window2End;

    @Test
    public void whenFlmRunIsInTheFirstHalfOfTheYear_thenThreePAExecutionsAreCreated() {
        window1End = Timestamp.valueOf("2021-03-12 08:59:59.0");
        window2End = Timestamp.valueOf("2021-03-12 14:59:59.0");
        final List<PAExecution> expectedPAExecutions = new ArrayList<>(EXPECTED_NUMBER_OF_PA_WINDOWS);
        expectedPAExecutions
                .add(new PAExecution(1, "0 0 11 12 3 ? 2021", Timestamp.valueOf("2021-03-12 03:00:00.0"), window1End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(2, "0 0 17 12 3 ? 2021", Timestamp.valueOf("2021-03-12 09:00:00.0"), window2End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(3, "0 0 23 12 3 ? 2021", Timestamp.valueOf("2021-03-12 15:00:00.0"), Timestamp.valueOf("2021-03-12 20:59:59.0"), FLM_TEST_ID));

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule(SCHEDULE_2AM);
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:00:00.05"));
        flmExecution.setId(FLM_TEST_ID);

        final List<PAExecution> paExecutions = SchedulesMaker.makeSchedules(flmExecution);

        assertThat(paExecutions).containsExactlyElementsOf(expectedPAExecutions);
    }

    @Test
    public void whenFlmRunIsNotExactlyOnAnHour_thenThreePAExecutionsAreCreated() {
        window1End = Timestamp.valueOf("2021-03-12 09:12:24.0");
        window2End = Timestamp.valueOf("2021-03-12 15:12:24.0");
        final List<PAExecution> expectedPAExecutions = new ArrayList<>(EXPECTED_NUMBER_OF_PA_WINDOWS);
        expectedPAExecutions
                .add(new PAExecution(1, "25 12 11 12 3 ? 2021", Timestamp.valueOf("2021-03-12 03:12:25.0"), window1End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(2, "25 12 17 12 3 ? 2021", Timestamp.valueOf("2021-03-12 09:12:25.0"), window2End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(3, "25 12 23 12 3 ? 2021", Timestamp.valueOf("2021-03-12 15:12:25.0"), Timestamp.valueOf("2021-03-12 21:12:24.0"), FLM_TEST_ID));

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule(SCHEDULE_2AM);
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 02:12:25.05"));
        flmExecution.setId(FLM_TEST_ID);

        final List<PAExecution> paExecutions = SchedulesMaker.makeSchedules(flmExecution);

        assertThat(paExecutions).containsExactlyElementsOf(expectedPAExecutions);
    }

    @Test
    public void whenFlmRunIsInTheSecondHalfOfTheYear_thenThreePAExecutionsAreCreated() {
        window1End = Timestamp.valueOf("2021-09-15 08:59:59.0");
        window2End = Timestamp.valueOf("2021-09-15 14:59:59.0");
        final List<PAExecution> expectedPAExecutions = new ArrayList<>(EXPECTED_NUMBER_OF_PA_WINDOWS);
        expectedPAExecutions
                .add(new PAExecution(1, "0 0 11 15 9 ? 2021", Timestamp.valueOf("2021-09-15 03:00:00.0"), window1End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(2, "0 0 17 15 9 ? 2021", Timestamp.valueOf("2021-09-15 09:00:00.0"), window2End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(3, "0 0 23 15 9 ? 2021", Timestamp.valueOf("2021-09-15 15:00:00.0"), Timestamp.valueOf("2021-09-15 20:59:59.0"), FLM_TEST_ID));

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule(SCHEDULE_2AM);
        flmExecution.setStartTime(Timestamp.valueOf("2021-09-15 02:00:00.05"));
        flmExecution.setId(FLM_TEST_ID);

        final List<PAExecution> paExecutions = SchedulesMaker.makeSchedules(flmExecution);

        assertThat(paExecutions).containsExactlyElementsOf(expectedPAExecutions);
    }

    @Test
    public void whenFlmRunIsAt6amInTheMiddleOfTheMonth_thenThreePAExecutionsAreCreatedWithOneGoingAcrossDays() {
        window1End = Timestamp.valueOf("2021-03-12 12:59:59.0");
        window2End = Timestamp.valueOf("2021-03-12 18:59:59.0");
        final List<PAExecution> expectedPAExecutions = new ArrayList<>(EXPECTED_NUMBER_OF_PA_WINDOWS);
        expectedPAExecutions
                .add(new PAExecution(1, "0 0 15 12 3 ? 2021", Timestamp.valueOf("2021-03-12 7:00:00.0"), window1End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(2, "0 0 21 12 3 ? 2021", Timestamp.valueOf("2021-03-12 13:00:00.0"), window2End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(3, "0 0 3 13 3 ? 2021", Timestamp.valueOf("2021-03-12 19:00:00.0"), Timestamp.valueOf("2021-03-13 00:59:59.0"), FLM_TEST_ID));

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule(SCHEDULE_6AM);
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-12 06:00:00.05"));
        flmExecution.setId(FLM_TEST_ID);

        final List<PAExecution> paExecutions = SchedulesMaker.makeSchedules(flmExecution);

        assertThat(paExecutions).containsExactlyElementsOf(expectedPAExecutions);
    }

    @Test
    public void whenFlmRunIsAt6amOnLastDayOfTheMonth_thenThreePAExecutionsAreCreatedWithOneGoingAcrossMonths() {
        window1End = Timestamp.valueOf("2021-03-31 12:59:59.0");
        window2End = Timestamp.valueOf("2021-03-31 18:59:59.0");
        final List<PAExecution> expectedPAExecutions = new ArrayList<>(EXPECTED_NUMBER_OF_PA_WINDOWS);

        expectedPAExecutions
                .add(new PAExecution(1, "0 0 15 31 3 ? 2021", Timestamp.valueOf("2021-03-31 07:00:00.0"), window1End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(2, "0 0 21 31 3 ? 2021", Timestamp.valueOf("2021-03-31 13:00:00.0"), window2End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(3, "0 0 3 1 4 ? 2021", Timestamp.valueOf("2021-03-31 19:00:00.0"), Timestamp.valueOf("2021-04-1 00:59:59.0"), FLM_TEST_ID));

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule(SCHEDULE_6AM);
        flmExecution.setStartTime(Timestamp.valueOf("2021-03-31 06:00:00.05"));
        flmExecution.setId(FLM_TEST_ID);

        final List<PAExecution> paExecutions = SchedulesMaker.makeSchedules(flmExecution);

        assertThat(paExecutions).containsExactlyElementsOf(expectedPAExecutions);
    }

    @Test
    public void whenFlmRunIsAt6amOnLastDayOfTheYear_thenThreePAExecutionsAreCreatedWithOneGoingAcrossYears() {
        window1End = Timestamp.valueOf("2021-12-31 12:59:59.0");
        window2End = Timestamp.valueOf("2021-12-31 18:59:59.0");

        final List<PAExecution> expectedPAExecutions = new ArrayList<>(EXPECTED_NUMBER_OF_PA_WINDOWS);
        expectedPAExecutions
                .add(new PAExecution(1, "0 0 15 31 12 ? 2021", Timestamp.valueOf("2021-12-31 07:00:00.0"), window1End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(2, "0 0 21 31 12 ? 2021", Timestamp.valueOf("2021-12-31 13:00:00.0"), window2End, FLM_TEST_ID));
        expectedPAExecutions
                .add(new PAExecution(3, "0 0 3 1 1 ? 2022", Timestamp.valueOf("2021-12-31 19:00:00.0"), Timestamp.valueOf("2022-01-01 00:59:59.0"), FLM_TEST_ID));

        final Execution flmExecution = new Execution();
        flmExecution.setSchedule(SCHEDULE_6AM);
        flmExecution.setStartTime(Timestamp.valueOf("2021-12-31 06:00:00.05"));
        flmExecution.setId(FLM_TEST_ID);

        final List<PAExecution> paExecutions = SchedulesMaker.makeSchedules(flmExecution);

        assertThat(paExecutions).containsExactlyElementsOf(expectedPAExecutions);
    }

}

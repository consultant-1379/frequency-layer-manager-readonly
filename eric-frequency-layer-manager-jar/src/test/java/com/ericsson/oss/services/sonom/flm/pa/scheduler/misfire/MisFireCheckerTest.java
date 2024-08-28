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

package com.ericsson.oss.services.sonom.flm.pa.scheduler.misfire;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * Unit test for {@link MisFireChecker} class.
 */
public class MisFireCheckerTest {
    //Note: In the tests below in the dummy PAExecution paWindowStartTime and paWindowEndTime values don't matter here so just putting in something but don't make sense in reality
    private static final String DUMMY_PA_WINDOW_START_TIME = "2021-03-12 03:00:00.0";
    private static final String DUMMY_PA_WINDOW_END_TIME = "2021-03-12 08:59:59.0";
    private static final String DUMMY_FLM_EXECUTION_ID = "flm_test_id";

    @Test
    public void whenPAExecutionScheduled_150_MinutesAgo_thenShouldReturnPAExecution_InTheMisfiresThatCanNotBeScheduledList_AsNotWithinThresholdOf_120_Minutes() {
        final List<PAExecution> dummyForNow = new ArrayList<>();
        dummyForNow.add(new PAExecution(1, makeCronInThePast(150), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), DUMMY_FLM_EXECUTION_ID));
        final MisFireCheckerResult misFireCheckerResult = MisFireChecker.check(dummyForNow);
        assertThat(misFireCheckerResult.getMisfiresThatCanBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getMisfiresThatCanNotBeScheduled()).hasSize(1);
        assertThat(misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal()).isEmpty();
    }

    @Test
    public void whenPAExecutionScheduled_121_MinutesAgo_thenShouldReturnPAExecution_InTheMisfiresThatCanNotBeScheduledList_AsNotWithinThresholdOf_120_Minutes() {
        final List<PAExecution> dummyForNow = new ArrayList<>();
        dummyForNow.add(new PAExecution(1, makeCronInThePast(121), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), DUMMY_FLM_EXECUTION_ID));
        final MisFireCheckerResult misFireCheckerResult = MisFireChecker.check(dummyForNow);
        assertThat(misFireCheckerResult.getMisfiresThatCanBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getMisfiresThatCanNotBeScheduled()).hasSize(1);
        assertThat(misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal()).isEmpty();
    }

    @Test
    public void whenPAExecutionScheduled_30_MinutesAgo_thenShouldReturnPAExecution_InTheMisfiresThatCanBeScheduledList_AsWithinTheThresholdOf_120_Minutes() {
        final List<PAExecution> dummyForNow = new ArrayList<>();
        dummyForNow.add(new PAExecution(1, makeCronInThePast(30), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), DUMMY_FLM_EXECUTION_ID));
        final MisFireCheckerResult misFireCheckerResult = MisFireChecker.check(dummyForNow);
        assertThat(misFireCheckerResult.getMisfiresThatCanBeScheduled()).hasSize(1);
        assertThat(misFireCheckerResult.getMisfiresThatCanNotBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal()).isEmpty();
    }

    @Test
    public void whenPAExecutionScheduled_118_MinutesAgo_thenShouldReturnPAExecution_InTheMisfiresThatCanBeScheduledList_AsWithinTheThresholdOf_120_Minutes() {
        final List<PAExecution> dummyForNow = new ArrayList<>();
        dummyForNow
                .add(new PAExecution(1, makeCronInThePast(118),
                        Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                        Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), DUMMY_FLM_EXECUTION_ID));

        final MisFireCheckerResult misFireCheckerResult = MisFireChecker.check(dummyForNow);
        assertThat(misFireCheckerResult.getMisfiresThatCanBeScheduled()).hasSize(1);
        assertThat(misFireCheckerResult.getMisfiresThatCanNotBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal()).isEmpty();
    }

    @Test
    public void whenNoPAExecutionsProvided_thenShouldNotReturnPAExecution_InAnyOfTheThreeMisfireCheckerResultLists() {
        final List<PAExecution> dummyForNow = new ArrayList<>();
        final MisFireCheckerResult misFireCheckerResult = MisFireChecker.check(dummyForNow);
        assertThat(misFireCheckerResult.getMisfiresThatCanBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getMisfiresThatCanNotBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal()).isEmpty();
    }

    @Test
    public void whenPAExecutionScheduleIsInTheFuture_thenShouldReturnPAExecution_InTheNotMisfiredScheduleAgainAsNormalList() {
        final List<PAExecution> dummyForNow = new ArrayList<>();
        dummyForNow
                .add(new PAExecution(1, makeCronInTheFuture(300),
                        Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                        Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), DUMMY_FLM_EXECUTION_ID));
        final MisFireCheckerResult misFireCheckerResult = MisFireChecker.check(dummyForNow);
        assertThat(misFireCheckerResult.getMisfiresThatCanBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getMisfiresThatCanNotBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal()).hasSize(1);

    }

    @Test
    public void whenMultiplePAExecution_thenShouldReturnCorrectMisfireCheckerResult() {
        final List<PAExecution> dummyForNow = new ArrayList<>();
        //In the past within 2hr threshold
        dummyForNow.add(new PAExecution(1, makeCronInThePast(60), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), DUMMY_FLM_EXECUTION_ID));
        //In the future schedule as normal
        dummyForNow.add(new PAExecution(1, makeCronInTheFuture(100),
                Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), DUMMY_FLM_EXECUTION_ID));
        //In the past outside 2hr threshold
        dummyForNow.add(new PAExecution(1, makeCronInThePast(200), Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), DUMMY_FLM_EXECUTION_ID));

        final MisFireCheckerResult misFireCheckerResult = MisFireChecker.check(dummyForNow);
        assertThat(misFireCheckerResult.getMisfiresThatCanBeScheduled()).hasSize(1);
        assertThat(misFireCheckerResult.getMisfiresThatCanNotBeScheduled()).hasSize(1);
        assertThat(misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal()).hasSize(1);

    }

    @Test
    public void whenParseExceptionThrown_thenExceptionIsAbsorbed() {
        final List<PAExecution> dummyForNow = new ArrayList<>();
        //In the past within 2hr threshold
        dummyForNow.add(new PAExecution(1, "bad_cron", Timestamp.valueOf(DUMMY_PA_WINDOW_START_TIME),
                Timestamp.valueOf(DUMMY_PA_WINDOW_END_TIME), DUMMY_FLM_EXECUTION_ID));
        final MisFireCheckerResult misFireCheckerResult = MisFireChecker.check(dummyForNow);
        assertThat(misFireCheckerResult.getMisfiresThatCanBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getMisfiresThatCanNotBeScheduled()).isEmpty();
        assertThat(misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal()).isEmpty();
    }

    private String makeCronInTheFuture(final int numberOfMinutesInTheFuture) {
        final Instant instant = Instant.now().plus(numberOfMinutesInTheFuture, ChronoUnit.MINUTES);
        return getCronAsStringFromInstant(instant);
    }

    private String makeCronInThePast(final int numberOfMinutesInThePast) {
        final Instant instant = Instant.now().minus(numberOfMinutesInThePast, ChronoUnit.MINUTES);
        return getCronAsStringFromInstant(instant);
    }

    private String getCronAsStringFromInstant(final Instant instant) {
        final LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
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

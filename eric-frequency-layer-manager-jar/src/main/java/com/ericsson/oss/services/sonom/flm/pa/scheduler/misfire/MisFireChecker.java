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

package com.ericsson.oss.services.sonom.flm.pa.scheduler.misfire;

import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PASettings;

/**
 * Checks what PAExecutions are misfires and creates a MisFireCheckerResult which contains what PAExecutions are in the past and are within the
 * threshold that should be scheduled again immediately and what ones cannot be scheduled again and what ones are not misfires and can be scheduled as
 * normal. <br>
 * <b>Note:</b> In this context a misfire means we missed a schedule or an incomplete PAExecution run.
 */
public class MisFireChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisFireChecker.class);

    private MisFireChecker() {
        //since all methods in class is static
    }

    /**
     * Checks the {@link List} of {@link PAExecution}(s) to see the following:
     * <ol>
     * <li>What ones should be scheduled as normal i.e. have schedules in the future.</li>
     * <li>What ones are misfires and within the {@link PASettings#PA_EXECUTION_MISFIRE_THRESHOLD_IN_MINUTES} and should be scheduled
     * immediately.</li>
     * <li>What ones are misfires and outside the {@link PASettings#PA_EXECUTION_MISFIRE_THRESHOLD_IN_MINUTES} and shouldn't be scheduled again.</li>
     * </ol>
     * 
     * @param paExecutions
     *            the {@link List} of {@link PAExecution}(s) to check for misfires
     * @return a {@link MisFireCheckerResult}
     */
    public static MisFireCheckerResult check(final List<PAExecution> paExecutions) {
        final MisFireCheckerResult misFireCheckerResult = new MisFireCheckerResult();
        for (final PAExecution paExecution : paExecutions) {
            try {
                final CronExpression cronExpression = getCronExpression(paExecution);
                final Instant now = getInstantNow();
                if (isPAExecutionScheduleInThePast(cronExpression, now)) {
                    if (isPAExecutionScheduleStillWithinTheMisfireThreshold(cronExpression, now)) {
                        misFireCheckerResult.addToMisfiresThatCanBeScheduled(paExecution);
                    } else {
                        misFireCheckerResult.addToMisfiresThanCanNotBeScheduled(paExecution);
                    }
                } else {
                    misFireCheckerResult.addToNotMisfiredScheduleAgainAsNormal(paExecution);
                }
            } catch (final ParseException e) {
                LOGGER.error("Failed to check PAExecution with ID '{}' and schedule '{}' for misfire.", paExecution.getId(),
                        paExecution.getSchedule(), e);
            }
        }
        return misFireCheckerResult;
    }

    private static boolean isPAExecutionScheduleInThePast(final CronExpression cronExpression, final Instant now) {
        return Objects.isNull(cronExpression.getNextValidTimeAfter(Date.from(now)));
    }

    private static boolean isPAExecutionScheduleStillWithinTheMisfireThreshold(final CronExpression cronExpression, final Instant now) {
        final Instant backInTimeByTheMisfireThresholdAmmount = now.minus(PASettings.PA_EXECUTION_MISFIRE_THRESHOLD_IN_MINUTES, ChronoUnit.MINUTES);
        return !Objects.isNull(cronExpression.getNextValidTimeAfter(Date.from(backInTimeByTheMisfireThresholdAmmount)));
    }

    private static CronExpression getCronExpression(final PAExecution paExecution) throws ParseException {
        final CronExpression cronExpression = new CronExpression(paExecution.getSchedule());
        cronExpression.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC.toString()));
        return cronExpression;
    }

    private static Instant getInstantNow() {
        return OffsetDateTime.now(ZoneOffset.UTC).toInstant();
    }
}

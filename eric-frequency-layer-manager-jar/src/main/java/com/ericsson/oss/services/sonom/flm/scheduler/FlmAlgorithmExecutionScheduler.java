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

package com.ericsson.oss.services.sonom.flm.scheduler;

import static com.ericsson.oss.services.sonom.flm.scheduler.FlmExecutionActivity.FLM_ACTIVITY_CONFIGURATION;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.scheduler.Activity;
import com.ericsson.oss.services.sonom.common.scheduler.ActivityScheduler;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.common.scheduler.CronSchedule;
import com.ericsson.oss.services.sonom.flm.database.utils.SchedulerRetry;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;

import io.vavr.CheckedFunction0;

/**
 * Schedules the sending of KPI calculation requests to <code>eric-pm-kpi-calculator</code>.
 */
public final class FlmAlgorithmExecutionScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmAlgorithmExecutionScheduler.class);

    private static final String FLM_EXECUTION_SCHEDULE_PREFIX = "FLM_EXECUTION_SCHEDULE_";
    private static final String FLM_EXECUTION_ACTIVITY_NAME = "FLM_EXECUTION_ACTIVITY";
    private static final int SCHEDULER_MAX_RETRY_ATTEMPTS = 10;
    private static final int SCHEDULER_WAIT_PERIOD_IN_SECONDS = 30;
    private static SchedulerRetry schedulerRetry = new SchedulerRetry(SCHEDULER_MAX_RETRY_ATTEMPTS, SCHEDULER_WAIT_PERIOD_IN_SECONDS);

    private FlmAlgorithmExecutionScheduler() {
        //since all methods in class is static
    }

    /**
     * Creates the Schedule for next execution based on the configuration provided in parameter .
     *
     * @param configuration
     *            The configuration to create a schedule for
     * @return The schedule name for the created schedule
     * @throws ActivitySchedulerException
     *             thrown if anything is wrong with the input CRON expression
     */
    public static String createSchedule(final Configuration configuration) throws ActivitySchedulerException {
        final CheckedFunction0<String> schedulerCreate = () -> performInsert(configuration);
        return schedulerRetry.executeWithRetry(schedulerCreate,
                t -> new ActivitySchedulerException(String.format("Failed to schedule execution for configuration '%s'", configuration)));
    }

    private static String performInsert(final Configuration configuration) throws ActivitySchedulerException {
        final String scheduleName = FLM_EXECUTION_SCHEDULE_PREFIX + configuration.getId();

        final CronSchedule providedCronSchedule = new CronSchedule(scheduleName, Collections.emptyMap(), configuration.getSchedule());
        final CronSchedule cronSchedule = new CronSchedule(scheduleName, Collections.emptyMap(),
                providedCronSchedule.getDailyVersionOfCronExpressionMinusGivenDays(configuration.getWeekendDays()));

        final Activity flmExecutionActivity = getFlmExecutionActivity(scheduleName, configuration);
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        activityScheduler.addCronScheduleForActivity(flmExecutionActivity,
                cronSchedule);
        LOGGER.info("Execution scheduled successfully, next KPI calculation FLM execution scheduled at {}",
                cronSchedule.getNextValidTime());
        LOGGER.info("Execution scheduled successfully, next full FLM execution scheduled at {}", providedCronSchedule.getNextValidTime());
        return scheduleName;
    }

    /**
     * Updates the Schedule for next execution based on the configuration provided in parameter or creates a new one if none exists.
     *
     * @param configuration
     *            The configuration to create a schedule to trigger multiple concurrent FLM executions
     * @throws ActivitySchedulerException
     *             thrown if no activity exist or if anything is wrong with the input CRON expression
     */
    public static void updateSchedule(final Configuration configuration) throws ActivitySchedulerException {
        final String scheduleName = FLM_EXECUTION_SCHEDULE_PREFIX + configuration.getId();
        final String activityScheduleName = FLM_EXECUTION_ACTIVITY_NAME + "_" + scheduleName;
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();

        if (activityScheduler.activityExists(activityScheduleName)) {
            activityScheduler.removeActivity(activityScheduleName);

            final CronSchedule providedCronSchedule = new CronSchedule(scheduleName, Collections.emptyMap(), configuration.getSchedule());
            final CronSchedule cronSchedule = new CronSchedule(scheduleName, Collections.emptyMap(),
                    providedCronSchedule.getDailyVersionOfCronExpressionMinusGivenDays(configuration.getWeekendDays()));

            final Activity flmExecutionActivity = getFlmExecutionActivity(scheduleName, configuration);

            activityScheduler.addCronScheduleForActivity(flmExecutionActivity, cronSchedule);
            LOGGER.info("Schedule updated and scheduled successfully, next KPI calculation FLM execution scheduled at {}",
                    cronSchedule.getNextValidTime());
            LOGGER.info("Schedule updated and scheduled successfully, next full FLM algorithm execution scheduled at {}",
                    providedCronSchedule.getNextValidTime());
        } else {
            createSchedule(configuration);
        }
    }

    /**
     * Deletes the Schedule for next execution based on the configuration provided in parameter.
     *
     * @param configuration
     *            The configuration to create a schedule for
     * @throws ActivitySchedulerException
     *             thrown if no activity exists to be deleted.
     */
    public static void deleteSchedule(final Configuration configuration) throws ActivitySchedulerException {
        final String scheduleName = FLM_EXECUTION_SCHEDULE_PREFIX + configuration.getId();
        final String activityScheduleName = FLM_EXECUTION_ACTIVITY_NAME + "_" + scheduleName;
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        if (activityScheduler.activityExists(activityScheduleName)) {
            activityScheduler.removeActivity(activityScheduleName);
            LOGGER.info("Schedule '{}' deleted", scheduleName);
        } else {
            LOGGER.info("No schedule exists to delete for configuration '{}'", configuration.getName());
            throw new ActivitySchedulerException(String.format("No schedule exists to delete for configuration '%s'", configuration.getName()));
        }
    }

    /**
     * Get a {@link Activity} object with unique name to be scheduled. An instance to the ExecutionService is also thrown in to the context map so
     * that it is available during the run() call.
     *
     * @param scheduleName
     *            the unique name
     * @param configuration
     *            the configuration to include in the execution activity
     * @return the {@link Activity} object
     */
    private static Activity getFlmExecutionActivity(final String scheduleName, final Configuration configuration) {
        final Map<String, Object> context = new HashMap<>(1);
        context.put(FLM_ACTIVITY_CONFIGURATION, configuration);
        return new FlmExecutionActivity(FLM_EXECUTION_ACTIVITY_NAME + "_" + scheduleName, context);
    }

    /**
     * Function to set retry values for the scheduler retry function.
     *
     * @param schedulerMaxRetryAttempts
     *            Max number of times to retry a call.
     * @param schedulerWaitPeriodInSeconds
     *            Max time in seconds to wait between calls.
     */
    public static void setSchedulerRetryParameters(final int schedulerMaxRetryAttempts, final int schedulerWaitPeriodInSeconds) {
        schedulerRetry = new SchedulerRetry(schedulerMaxRetryAttempts, schedulerWaitPeriodInSeconds);
    }

    /**
     * Checks if a schedule exists for the configuration specified.
     *
     * @param configuration
     *            The Configuration to check
     * @return Indicates whether a scheduler exists for the configuration.
     * @throws ActivitySchedulerException
     *             thrown if no activity scheduler instance can be retrieved
     */
    public static boolean checkIfScheduleExists(final Configuration configuration) throws ActivitySchedulerException {
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        final String scheduleName = FLM_EXECUTION_SCHEDULE_PREFIX + configuration.getId();
        final String activityScheduleName = FLM_EXECUTION_ACTIVITY_NAME + "_" + scheduleName;
        return activityScheduler.activityExists(activityScheduleName);
    }
}
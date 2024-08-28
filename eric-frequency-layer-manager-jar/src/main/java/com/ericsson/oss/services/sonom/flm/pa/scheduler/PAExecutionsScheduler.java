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

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionActivity.FLM_EXECUTION_CONTEXT;
import static com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionActivity.PA_EXECUTION_CONTEXT;
import static java.lang.Integer.parseInt;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.scheduler.Activity;
import com.ericsson.oss.services.sonom.common.scheduler.ActivityScheduler;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.common.scheduler.CronSchedule;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.utils.SchedulerRetry;
import com.ericsson.oss.services.sonom.flm.pa.scheduler.misfire.MisFireChecker;
import com.ericsson.oss.services.sonom.flm.pa.scheduler.misfire.MisFireCheckerResult;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PASettings;

import io.vavr.CheckedFunction0;

/**
 * Class responsible for scheduling the {@link PAExecution}(s) associated with a given {@link Execution}.
 */
public final class PAExecutionsScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PAExecutionsScheduler.class);
    private static final String PA_EXECUTION_SCHEDULE_PREFIX = "PA_EXECUTION_SCHEDULE_";
    private static final String PA_EXECUTION_ACTIVITY_NAME = "PA_EXECUTION_ACTIVITY";
    private static final String PA_EXECUTION_SCHEDULE_MISFIRE_PREFIX = PA_EXECUTION_SCHEDULE_PREFIX + "MISFIRE_";
    private static final int PA_SCHEDULER_MAX_RETRY_ATTEMPTS = parseInt(getEnvironmentValue("PA_SCHEDULER_MAX_RETRY_ATTEMPTS", "10"));
    private static final int PA_SCHEDULER_WAIT_PERIOD_IN_SECONDS = parseInt(getEnvironmentValue("PA_SCHEDULER_WAIT_PERIOD_IN_SECONDS", "30"));
    private static final int PA_EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 10;
    private static final int PA_EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 30;
    private static final SchedulerRetry SCHEDULER_RETRY = new SchedulerRetry(PA_SCHEDULER_MAX_RETRY_ATTEMPTS,
            PA_SCHEDULER_WAIT_PERIOD_IN_SECONDS);

    private PAExecutionsScheduler() {
        //since all methods in class is static
    }

    /**
     * Schedules {@link PAExecution}(s) with the Quartz scheduler for the given {@link Execution} if pa is enabled for it and it has the correct
     * {@link ExecutionState} to perform performance assurance on it.
     *
     * @param execution
     *            the{@link Execution} to create the pa execution schedules for.
     */
    public static void createSchedule(final Execution execution) {
        if (execution.isEnablePA()) {
            if (execution.getState().equals(ExecutionState.SUCCEEDED) || execution.getState()
                    .equals(ExecutionState.PARTIALLY_SUCCEEDED)) {
                final List<PAExecution> paExecutionSchedules = SchedulesMaker.makeSchedules(execution);
                schedulePAExecutions(execution, paExecutionSchedules, false);
            } else {
                LOGGER.info("The FLM Execution '{}' has incorrect state '{}' to perform performance assurance", execution.getId(),
                        execution.getState());
            }
        } else {
            LOGGER.info("Performance Assurance is disabled for this FLM Execution '{}'", execution.getId());
        }
    }

    /**
     * Schedules existing {@link PAExecution}(s) that need to be scheduled again with the Quartz scheduler for the given {@link Execution} for
     * resilience scenario where FLM went down and existing schedules are now lost as the Quartz schedule is set up to use RAMStore to store activates
     * i.e. in memory, no persistence of the schedules.
     * 
     * @param flmExecution
     *            the{@link Execution} to create the pa execution schedules for.
     * @param paExecutions
     *            the {@link List} of {@link PAExecution} that need to be scheduled again.
     */
    public static void scheduleExisting(final Execution flmExecution, final List<PAExecution> paExecutions) {
        LOGGER.info("Attempting to reschedule  '{}' PA Execution(s) for FLM Execution with ID '{}'",
                paExecutions.size(), flmExecution.getId());
        final MisFireCheckerResult misFireCheckerResult = MisFireChecker.check(paExecutions);
        LOGGER.info(
                "Scheduling '{}' misfired PA Execution(s) immediately that are still within the '{}' minutes threshold for FLM Execution "
                        + "with ID '{}'",
                misFireCheckerResult.getMisfiresThatCanBeScheduled().size(),
                PASettings.PA_EXECUTION_MISFIRE_THRESHOLD_IN_MINUTES, flmExecution.getId());
        schedulePAExecutionsImmediately(misFireCheckerResult.getMisfiresThatCanBeScheduled(), flmExecution);
        LOGGER.info("Scheduling '{}' PA Execution(s) that can be rescheduled as normal for FLM Execution with ID '{}'",
                misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal().size(), flmExecution.getId());
        schedulePAExecutions(flmExecution, misFireCheckerResult.getNotMisfiredScheduleAgainAsNormal(), true);
        LOGGER.info(
                "Cannot schedule '{}' misfired PA Execution(s) for FLM Execution with ID '{}' as they are outside the '{}' minutes threshold",
                misFireCheckerResult.getMisfiresThatCanNotBeScheduled().size(), flmExecution.getId(),
                PASettings.PA_EXECUTION_MISFIRE_THRESHOLD_IN_MINUTES);
        updatePaExecutionsStateToMisfiredThatCannotBeScheduledAgain(misFireCheckerResult.getMisfiresThatCanNotBeScheduled());
    }

    /**
     * Cancel any {@link PAExecution}(s) with status {@code SCHEDULED} by removing activity from the Quartz scheduler and persist the new
     * {@code CANCELLED} state.
     *
     * @param paExecutions
     *            a {@link List} of {@link PAExecution}s to cancel.
     * @throws ActivitySchedulerException
     *             if an error occurs when removing an activity.
     */
    public static void cancelScheduledExecutions(final Collection<PAExecution> paExecutions) throws ActivitySchedulerException {
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();

        for (final PAExecution paExecution : paExecutions) {
            if (paExecution.getState() == PAExecutionState.SCHEDULED) {
                final String activityName = getActivityName(paExecution, false);
                boolean persistCancelledState = true;
                if (activityScheduler.activityExists(activityName)) {
                    if (activityScheduler.isActivityRunning(activityName)) {
                        persistCancelledState = false;
                        LOGGER.warn("Activity '{}' for PA Execution {} is running and cannot be removed.", activityName, paExecution.getId());
                    } else {
                        activityScheduler.removeActivity(activityName);
                    }
                }
                if (persistCancelledState) {
                    persistPAExecution(paExecution, PAExecutionState.CANCELLED, true);
                }
            }
        }
    }

    /**
     * Terminate any {@link PAExecution}(s) with status {@code STARTED} by sending the interrupt signal to the activity on the Quartz scheduler.
     *
     * @param paExecutions
     *            a {@link List} of {@link PAExecution}s to terminate.
     * @throws ActivitySchedulerException
     *             if an error occurs when interrupting an activity.
     */
    public static void terminateRunningExecution(final Collection<PAExecution> paExecutions) throws ActivitySchedulerException {
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        for (final PAExecution paExecution : paExecutions) {
            if (paExecution.getState() == PAExecutionState.STARTED) {
                if (activityScheduler.activityExists(getActivityName(paExecution, false))) {
                    activityScheduler.interruptActivity(getActivityName(paExecution, false));
                } else if (activityScheduler.activityExists(getActivityName(paExecution, true))) {
                    activityScheduler.interruptActivity(getActivityName(paExecution, true));
                } else {
                    LOGGER.warn("No activity for PA execution {} exists", paExecution.getId());
                }
            }
        }
    }

    static String getActivityName(final PAExecution paExecution, final boolean misfire) {
        return String.format("%s_%s%s", PA_EXECUTION_ACTIVITY_NAME,
                misfire ? PA_EXECUTION_SCHEDULE_MISFIRE_PREFIX : PA_EXECUTION_SCHEDULE_PREFIX, paExecution.getId());
    }

    private static void schedulePAExecutions(final Execution flmExecution, final List<PAExecution> paExecutionSchedules, final boolean existing) {
        for (final PAExecution paExecution : paExecutionSchedules) {
            try {
                LOGGER.info("Scheduling PA Execution '{}'", paExecution.getId());
                schedulePAExecution(flmExecution, paExecution);
                LOGGER.info("Successfully scheduled PA Execution '{}' for '{}'", paExecution.getId(), paExecution.getSchedule());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Successfully scheduled PA Execution '{}'", paExecution);
                }
            } catch (final ActivitySchedulerException e) {
                LOGGER.error("Failed to schedule the PA Execution '{}'", paExecution, e);
                continue;
            }
            persistPAExecution(paExecution, PAExecutionState.SCHEDULED, existing);

        }
    }

    private static void schedulePAExecutionsImmediately(final List<PAExecution> paExecutionsToScheduleImmediately, final Execution flmExecution) {
        for (final PAExecution paExecution : paExecutionsToScheduleImmediately) {
            try {
                schedulePAExecutionImmediately(flmExecution, paExecution);
            } catch (final ActivitySchedulerException e) {
                LOGGER.error("Failed to schedule misfired PA Execution '{}'", paExecution, e);
                persistPAExecution(paExecution, PAExecutionState.FAILED, true);
                continue;
            }
            persistPAExecution(paExecution, PAExecutionState.SCHEDULED, true);
        }

    }

    private static String schedulePAExecution(final Execution flmExecution, final PAExecution paExecution) throws ActivitySchedulerException {
        final CheckedFunction0<String> schedulerCreate = () -> addCronScheduleForPAExecutionActivity(flmExecution, paExecution);
        return SCHEDULER_RETRY.executeWithRetry(schedulerCreate,
                t -> new ActivitySchedulerException(String.format("Failed to schedule the PA Execution '%s'", paExecution)));
    }

    private static String addCronScheduleForPAExecutionActivity(final Execution flmExecution, final PAExecution paExecution)
            throws ActivitySchedulerException {
        final String scheduleName = PA_EXECUTION_SCHEDULE_PREFIX + flmExecution.getId() + "_" + paExecution.getPaWindow();
        final CronSchedule cronSchedule = new CronSchedule(scheduleName, Collections.emptyMap(), paExecution.getSchedule());
        final Activity paExecutionActivity = getPAExecutionActivity(scheduleName, paExecution, flmExecution);
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        activityScheduler.addCronScheduleForActivity(paExecutionActivity, cronSchedule);

        return scheduleName;

    }

    private static String schedulePAExecutionImmediately(final Execution flmExecution,
            final PAExecution paExecution) throws ActivitySchedulerException {
        final CheckedFunction0<String> schedulerCreate = () -> immediatelySchedulePAExecutionActivity(flmExecution, paExecution);
        return SCHEDULER_RETRY.executeWithRetry(schedulerCreate,
                t -> new ActivitySchedulerException(String.format("Failed to schedule the misfired PA Execution '%s'", paExecution)));

    }

    private static String immediatelySchedulePAExecutionActivity(final Execution flmExecution, final PAExecution paExecution)
            throws ActivitySchedulerException {
        final String scheduleName = PA_EXECUTION_SCHEDULE_MISFIRE_PREFIX + paExecution.getPaWindow() + "_" + paExecution.getFlmExecutionId();
        final Activity paExecutionActivity = getPAExecutionActivity(scheduleName, paExecution, flmExecution);
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        activityScheduler.runActivity(paExecutionActivity);
        return scheduleName;

    }

    private static Activity getPAExecutionActivity(final String scheduleName, final PAExecution paExecution, final Execution flmExecution) {
        final Map<String, Object> context = new HashMap<>(2);
        context.put(PA_EXECUTION_CONTEXT, paExecution);
        context.put(FLM_EXECUTION_CONTEXT, flmExecution);
        return new PAExecutionActivity(PA_EXECUTION_ACTIVITY_NAME + "_" + scheduleName, context);
    }

    private static void updatePaExecutionsStateToMisfiredThatCannotBeScheduledAgain(
            final List<PAExecution> misfiredPAExecutionsThatCanNotBeScheduled) {

        for (final PAExecution paExecution : misfiredPAExecutionsThatCanNotBeScheduled) {
            persistPAExecution(paExecution, PAExecutionState.MISFIRED, true);
        }
    }

    private static void persistPAExecution(final PAExecution paExecution, final PAExecutionState state, final boolean existing) {
        final PAExecutionDao paExecutionDao = new PAExecutionDaoImpl(PA_EXECUTION_DAO_MAX_RETRY_ATTEMPTS,
                PA_EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
        try {

            paExecution.setState(state);
            if (existing) {
                LOGGER.info("Updating PA Execution '{}' with state '{}' ", paExecution.getId(), state);
                paExecutionDao.update(paExecution);
            } else {
                LOGGER.info("Persisting PA Execution '{}' with state '{}' ", paExecution.getId(), state);
                paExecutionDao.insert(paExecution);
            }
            LOGGER.info("Successfully persisted PA Execution '{}'", paExecution.getId());
        } catch (final SQLException e) {
            LOGGER.error("Failed to persist the PA Execution '{}'", paExecution, e);
        }
    }

}

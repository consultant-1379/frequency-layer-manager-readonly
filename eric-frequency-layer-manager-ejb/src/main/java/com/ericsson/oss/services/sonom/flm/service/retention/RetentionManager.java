/**
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2020
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.retention;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.env.Environment;
import com.ericsson.oss.services.sonom.common.scheduler.ActivityScheduler;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.common.scheduler.CronSchedule;

/**
 * Class used to manage schedulers for {@link RetentionManager} instance. Can create new activities that will be scheduled to run.
 */
@Singleton
@Startup
public class RetentionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetentionManager.class);
    private static final Map<String, Object> DEFAULT_ACTIVITY_DATA = new HashMap<>(0);
    private static final String FLM_ALGORITHM_RETENTION_SCHEDULER = "flm_algorithm_retention_scheduler";

    private final String cronTimeToCheckRetention = Environment.getEnvironmentValue("CRON_RETENTION_SCHEDULE",
            "0 1 0 1/1 * ? *");

    /**
     * On initialization, creates the scheduler for retention period.
     */
    @PostConstruct
    public void init() {
        try {
            LOGGER.info("Scheduling FLM Algorithm retention period");
            final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
            final RetentionActivity activity = createActivity();
            removeActivityIfExists(activityScheduler, activity);
            final CronSchedule cronSchedule = new CronSchedule(FLM_ALGORITHM_RETENTION_SCHEDULER, DEFAULT_ACTIVITY_DATA, cronTimeToCheckRetention);
            LOGGER.info("Adding activity '{}' to CRON scheduler job at: '{}'", activity.getName(), cronSchedule);
            activityScheduler.addCronScheduleForActivity(activity, cronSchedule);
        } catch (final ActivitySchedulerException e) { //NOSONAR Exception suitably logged
            LOGGER.error("Error while scheduling retention period for cron: '{}'", cronTimeToCheckRetention, e);
            throw new RetentionManagerException("Error while scheduling retention period", e);
        }
    }

    private static void removeActivityIfExists(final ActivityScheduler activityScheduler, final RetentionActivity activity)
            throws ActivitySchedulerException {
        if (activityScheduler.activityExists(activity)) {
            LOGGER.info("Removing existing scheduled activity '{}'", activity.getName());
            activityScheduler.removeActivity(activity);
        }
    }

    private static RetentionActivity createActivity() {
        LOGGER.debug("Creating activity for schedule retention period");
        // NOTE: Retention period may be configurable when the configuration and settings MR is complete
        // possibly as Administrator setting.
        return RetentionActivity.createInstance(RetentionActivity.DEFAULT_RETENTION_PERIOD_DAYS, RetentionActivity.DEFAULT_RETENTION_EXECUTION_COUNT);
    }
}

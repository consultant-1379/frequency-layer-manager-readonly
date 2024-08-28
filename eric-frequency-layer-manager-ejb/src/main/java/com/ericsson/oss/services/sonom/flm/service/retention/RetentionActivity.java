/*
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2020-2021
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

import static com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants.FLM_EXECUTIONS;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.scheduler.Activity;
import com.ericsson.oss.services.sonom.common.scheduler.ActivityScheduler;
import com.ericsson.oss.services.sonom.flm.database.retention.RetentionDao;
import com.ericsson.oss.services.sonom.flm.database.retention.RetentionDaoImpl;

/**
 * Implementation of abstract class {@link Activity}, used with {@link ActivityScheduler} to create the scheduling of the FLM database clean up job.
 */
public class RetentionActivity extends Activity {

    public static final String RETENTION_ACTIVITY_NAME = "flmAlgorithmRetentionActivity";

    public static final int DEFAULT_RETENTION_PERIOD_DAYS = 14;
    public static final int DEFAULT_RETENTION_EXECUTION_COUNT = 140;
    private static final String RETENTION_PERIOD_DAYS = "retentionPeriodDays";
    private static final String RETENTION_COUNT = "retentionCount";

    private static final Logger LOGGER = LoggerFactory.getLogger(RetentionActivity.class);
    private static final int MIN_RETENTION_PERIOD = 1;
    private static final int MAX_RETENTION_PERIOD = 14;

    private static final int MIN_RETENTION_EXECUTION_COUNT = 1;
    private static final int MAX_RETENTION_EXECUTION_COUNT = 141;

    private RetentionDao retentionDao = new RetentionDaoImpl();

    /**
     * Default constructor. Any class extending {@link Activity} must have a no parameter constructor in order to support the Quartz
     * {@link org.quartz.Scheduler}. Any activity related data should be passed in the activityContext {@link Map} in the constructor.
     */
    public RetentionActivity() {
        super();
    }

    /**
     * Constructor with the params.
     *
     * @param retentionDao
     *            a {@link RetentionDao} used for testing.
     */
    RetentionActivity(final RetentionDao retentionDao) {
        super();
        this.retentionDao = retentionDao;
    }

    /**
     * Constructor with the params.
     *
     * @param name
     *            {@link String} name of the activity.
     * @param map
     *            {@link Map} of {@link String} and {@link Object} params used inside the overridden method run.
     */
    public RetentionActivity(final String name, final Map<String, Object> map) {
        super(name, map);
    }

    /**
     * Method used to get {@link RetentionActivity} instance.
     *
     * @param retentionPeriodDays
     *            {@link Integer} the retention period number days
     * @param retentionCount
     *            {@link Integer} the retention execution count.
     * @return {@link RetentionActivity}
     */

    public static RetentionActivity createInstance(final Integer retentionPeriodDays,
                                                   final Integer retentionCount) {
        final Map<String, Object> paramsMap = new HashMap<>(3);
        paramsMap.put(RETENTION_PERIOD_DAYS, retentionPeriodDays);
        paramsMap.put(RETENTION_COUNT, retentionCount);
        return new RetentionActivity(RETENTION_ACTIVITY_NAME, paramsMap);
    }

    /**
     * Method used to get {@link LocalDateTime} Retention Date.
     *
     * @param retentionDays
     *            {@link Integer} the retention period number days.
     * @return {@link LocalDateTime}
     */
    private static LocalDateTime getRetentionDate(final Integer retentionDays) {
        final LocalDate localDate = LocalDate.now(ZoneOffset.UTC).minusDays(retentionDays);
        return localDate.atStartOfDay();
    }

    /**
     * Method to validate retention period and return valid number of days. If param is invalid, return default
     *
     * @param retentionDays
     *            {@link Integer} the retention period number days.
     * @param defaultRetentionPeriod
     *            {@link Integer} the default retention period.
     * @return {@link Integer}
     */
    private static Integer getRetentionDaysWithValidation(final Integer retentionDays, final Integer defaultRetentionPeriod) {
        if (Objects.isNull(retentionDays) || retentionDays < MIN_RETENTION_PERIOD || MAX_RETENTION_PERIOD < retentionDays) {
            LOGGER.warn("Retention period defined is invalid, the default retention period will be used instead: '{}'", defaultRetentionPeriod);
            return defaultRetentionPeriod;
        }
        return retentionDays;
    }

    /**
     * Method to validate historical cell settings execution count and return valid number. If param is invalid, return default
     *
     * @param executionCount
     *            {@link Integer} the execution count number.
     * @return {@link Integer}
     */
    private static Integer getExecutionCountWithValidation(final Integer executionCount, final Integer defaultExecutionCount) {
        if (Objects.isNull(executionCount) || executionCount < MIN_RETENTION_EXECUTION_COUNT || MAX_RETENTION_EXECUTION_COUNT < executionCount) {
            LOGGER.warn("Execution count defined is invalid, the default execution count will be used instead: '{}'",
                    defaultExecutionCount);
            return defaultExecutionCount;
        }
        return executionCount;
    }

    @Override
    public void run(final Map<String, Object> activityContext) {
        LOGGER.info("FLM Algorithm retention service started");
        final Integer retentionDays = getRetentionDaysWithValidation((Integer) activityContext.get(
                RETENTION_PERIOD_DAYS), DEFAULT_RETENTION_PERIOD_DAYS);
        final LocalDateTime retentionDate = getRetentionDate(retentionDays);
        final Integer retentionCount = getExecutionCountWithValidation((Integer)
                activityContext.get(RETENTION_COUNT), DEFAULT_RETENTION_EXECUTION_COUNT);
        runCleanUpExecution(retentionDate, retentionCount);
        runCleanUpHistoricalCellConfiguration(retentionDate, retentionCount);

        LOGGER.info("FLM Algorithm retention service completed");
    }

    private void runCleanUpExecution(final LocalDateTime retentionDate, final Integer retentionExecutionCount) {
        try {
            retentionDao.cleanUpFlmExecutionsTable(retentionDate, retentionExecutionCount);
        } catch (final SQLException e) {
            LOGGER.error("Could not clean up executions from: '{}' within the retention date: '{}'", FLM_EXECUTIONS, retentionDate, e);
        }
    }

    private void runCleanUpHistoricalCellConfiguration(final LocalDateTime retentionDate, final Integer retentionExecutionCount) {
        try {
            retentionDao.cleanUpHistoricalCellConfigurationTable(retentionDate, retentionExecutionCount);
        } catch (final SQLException e) {
            LOGGER.error("Could not clean up historical cell configuration", e);
        }
    }
}

/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.common.scheduler.Activity;
import com.ericsson.oss.services.sonom.common.scheduler.ActivityException;
import com.ericsson.oss.services.sonom.flm.database.retention.RetentionDaoImpl;

/**
 * Unit tests for {@link RetentionActivity} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RetentionActivityTest {

    private static final String RETENTION_PERIOD_DAYS_KEY = "retentionPeriodDays";
    private static final String RETENTION_COUNT_KEY = "retentionCount";
    private static final int RETENTION_PERIOD_DAYS = 3;
    private static final int RETENTION_PERIOD_COUNT = 30;
    private static final int MIN_RETENTION_PERIOD = 1;
    private static final int MAX_RETENTION_PERIOD = 14;
    private static final int MIN_RETENTION_EXECUTION_COUNT = 1;
    private static final int MAX_RETENTION_EXECUTION_COUNT = 141;
    private static final int ONCE = 1;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void whenInstanceCreated_thenContextIsSetProperly() {
        final Activity activity = RetentionActivity.createInstance(RETENTION_PERIOD_DAYS, RETENTION_PERIOD_COUNT);

        assertThat(activity.getContext()).contains(
                entry(RETENTION_PERIOD_DAYS_KEY, RETENTION_PERIOD_DAYS ),
                entry(RETENTION_COUNT_KEY, RETENTION_PERIOD_COUNT)
        );
    }

    @Test
    public void whenActivityRuns_thenCleaningIsDoneProperly() throws SQLException, ActivityException {
        final RetentionDaoImpl retentionDao = mock(RetentionDaoImpl.class);
        final RetentionActivity objectUnderTest = new RetentionActivity(retentionDao);

        doNothing().when(retentionDao).cleanUpFlmExecutionsTable(any(LocalDateTime.class),eq(RETENTION_PERIOD_COUNT));
        doNothing().when(retentionDao).cleanUpHistoricalCellConfigurationTable(any(LocalDateTime.class), eq(RETENTION_PERIOD_COUNT));

        final Map<String, Object> testContext = new HashMap<>();
        testContext.put(RETENTION_PERIOD_DAYS_KEY, RETENTION_PERIOD_DAYS);
        testContext.put(RETENTION_COUNT_KEY, RETENTION_PERIOD_COUNT);

        objectUnderTest.run(testContext);

        verify(retentionDao, times(ONCE))
                .cleanUpFlmExecutionsTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(RETENTION_PERIOD_DAYS).atStartOfDay()), eq(RETENTION_PERIOD_COUNT));
        verify(retentionDao, times(ONCE))
                .cleanUpHistoricalCellConfigurationTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(RETENTION_PERIOD_DAYS)
                        .atStartOfDay()), eq(RETENTION_PERIOD_COUNT));
    }

    @Test
    public void whenActivityRunsWithMaxBoundaryValues_thenCleanUpCallsWithTheRightValue() throws SQLException, ActivityException {
        final RetentionDaoImpl retentionDao = mock(RetentionDaoImpl.class);
        final RetentionActivity objectUnderTest = new RetentionActivity(retentionDao);

        final Map<String, Object> testContextMax = new HashMap<>();
        testContextMax.put(RETENTION_PERIOD_DAYS_KEY, MAX_RETENTION_PERIOD);
        testContextMax.put(RETENTION_COUNT_KEY, MAX_RETENTION_EXECUTION_COUNT);

        objectUnderTest.run(testContextMax);

        verify(retentionDao, times(ONCE))
                .cleanUpFlmExecutionsTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(MAX_RETENTION_PERIOD).atStartOfDay()), eq(MAX_RETENTION_EXECUTION_COUNT));
        verify(retentionDao, times(ONCE))
                .cleanUpHistoricalCellConfigurationTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(MAX_RETENTION_PERIOD)
                        .atStartOfDay()), eq(MAX_RETENTION_EXECUTION_COUNT));
    }

    @Test
    public void whenActivityRunsWithAboveMaxValues_thenCleanUpCallsWithTheDefaultValues() throws SQLException, ActivityException {
        final RetentionDaoImpl retentionDao = mock(RetentionDaoImpl.class);
        final RetentionActivity objectUnderTest = new RetentionActivity(retentionDao);

        final Map<String, Object> testContextMax = new HashMap<>();
        testContextMax.put(RETENTION_PERIOD_DAYS_KEY, MAX_RETENTION_PERIOD + 10);
        testContextMax.put(RETENTION_COUNT_KEY, MAX_RETENTION_EXECUTION_COUNT + 10);

        objectUnderTest.run(testContextMax);

        verify(retentionDao, times(ONCE))
                .cleanUpFlmExecutionsTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(RetentionActivity.DEFAULT_RETENTION_PERIOD_DAYS).atStartOfDay()), eq(RetentionActivity.DEFAULT_RETENTION_EXECUTION_COUNT));
        verify(retentionDao, times(ONCE))
                .cleanUpHistoricalCellConfigurationTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(RetentionActivity.DEFAULT_RETENTION_PERIOD_DAYS)
                        .atStartOfDay()), eq(RetentionActivity.DEFAULT_RETENTION_EXECUTION_COUNT));
    }

    @Test
    public void whenActivityRunsWithMinBoundaryValues_thenCleanUpCallsWithTheRightValue() throws SQLException, ActivityException {
        final RetentionDaoImpl retentionDao = mock(RetentionDaoImpl.class);
        final RetentionActivity objectUnderTest = new RetentionActivity(retentionDao);

        final Map<String, Object> testContextMin = new HashMap<>();
        testContextMin.put(RETENTION_PERIOD_DAYS_KEY, MIN_RETENTION_PERIOD);
        testContextMin.put(RETENTION_COUNT_KEY, MIN_RETENTION_EXECUTION_COUNT);

        objectUnderTest.run(testContextMin);

        verify(retentionDao, times(ONCE))
                .cleanUpFlmExecutionsTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(MIN_RETENTION_PERIOD).atStartOfDay()),eq(MIN_RETENTION_EXECUTION_COUNT));
        verify(retentionDao, times(ONCE))
                .cleanUpHistoricalCellConfigurationTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(MIN_RETENTION_PERIOD)
                        .atStartOfDay()), eq(MIN_RETENTION_EXECUTION_COUNT));
    }

    @Test
    public void whenActivityRunsWithBelowMinValues_thenCleanUpCallsWithTheDefaultValues() throws SQLException, ActivityException {
        final RetentionDaoImpl retentionDao = mock(RetentionDaoImpl.class);
        final RetentionActivity objectUnderTest = new RetentionActivity(retentionDao);

        final Map<String, Object> testContextMin = new HashMap<>();
        testContextMin.put(RETENTION_PERIOD_DAYS_KEY, MIN_RETENTION_PERIOD - 10);
        testContextMin.put(RETENTION_COUNT_KEY, MIN_RETENTION_EXECUTION_COUNT - 10);

        objectUnderTest.run(testContextMin);

        verify(retentionDao, times(ONCE))
                .cleanUpFlmExecutionsTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(RetentionActivity.DEFAULT_RETENTION_PERIOD_DAYS).atStartOfDay()), eq(RetentionActivity.DEFAULT_RETENTION_EXECUTION_COUNT));
        verify(retentionDao, times(ONCE))
                .cleanUpHistoricalCellConfigurationTable(eq(LocalDate.now(ZoneOffset.UTC).minusDays(RetentionActivity.DEFAULT_RETENTION_PERIOD_DAYS)
                        .atStartOfDay()), eq(RetentionActivity.DEFAULT_RETENTION_EXECUTION_COUNT));
    }
}
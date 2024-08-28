/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.common.scheduler.ActivityScheduler;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;

/**
 * Unit tests for {@link RetentionManager} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RetentionManagerTest {

    @Spy
    private RetentionManager objectUnderTest;

    @Before
    public void clear() throws ActivitySchedulerException {
        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        activityScheduler.removeActivity(RetentionActivity.RETENTION_ACTIVITY_NAME);
    }

    @Test
    public void whenRetentionIsTriggered_thenTheActivityIsScheduled() throws ActivitySchedulerException {
        objectUnderTest.init();
        verify(objectUnderTest, times(1)).init();

        final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();
        assertThat(activityScheduler.activityExists(RetentionActivity.RETENTION_ACTIVITY_NAME)).isTrue();
    }

    @Test
    public void whenRetentionIsTriggeredTwice_thenScheduleWithNoError() throws ActivitySchedulerException {
        objectUnderTest.init();

        final ActivityScheduler activitySchedulerFirst = ActivityScheduler.getInstance();
        assertThat(activitySchedulerFirst.activityExists(RetentionActivity.RETENTION_ACTIVITY_NAME)).isTrue();

        objectUnderTest.init();

        final ActivityScheduler activitySchedulerSecond = ActivityScheduler.getInstance();
        assertThat(activitySchedulerSecond.activityExists(RetentionActivity.RETENTION_ACTIVITY_NAME)).isTrue();

        verify(objectUnderTest, times(2)).init();
    }
}

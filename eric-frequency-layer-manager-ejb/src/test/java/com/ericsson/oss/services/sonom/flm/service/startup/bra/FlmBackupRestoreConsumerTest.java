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
package com.ericsson.oss.services.sonom.flm.service.startup.bra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.ericsson.oss.services.sonom.common.bro.notification.ActionType;
import com.ericsson.oss.services.sonom.common.bro.notification.Notification;
import com.ericsson.oss.services.sonom.common.bro.notification.NotificationStatus;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.common.scheduler.Activity;
import com.ericsson.oss.services.sonom.common.scheduler.ActivityScheduler;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.common.scheduler.CronSchedule;
import com.ericsson.oss.services.sonom.flm.service.startup.executions.ExecutionsController;


/**
 * Unit tests for {@link FlmBackupRestoreConsumer} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlmBackupRestoreConsumerTest {

    @ClassRule
    public static final EmbeddedKafkaRule EMBEDDED_KAFKA_RULE = new EmbeddedKafkaRule(1, true, 1, "bro-notification").brokerProperty(
            "auto.create.topics.enable", false);

    private static final String WEEKDAYS_AT_TWO_AM_SCHEDULE = "0 0 */6 ? * MON,TUE,WED,THU,FRI *";
    private static final String FLM_EXECUTION_ACTIVITY_ID = "FLM_EXECUTION_ACTIVITY_FLM_EXECUTION_SCHEDULE_%d";
    private static final String PA_EXECUTION_ACTIVITY_ID = "PA_EXECUTION_ACTIVITY_PA_EXECUTION_SCHEDULE_%d_%d";

    private static final Map<String, Object> DEFAULT_ACTIVITY_DATA = Collections.emptyMap();

    private static ActivityScheduler ACTIVITY_SCHEDULER;

    @Mock
    private ExecutionsController mockExecutionsController;

    @Mock
    private ConsumerRecord<String, Notification> consumerRecord;

    @Mock
    private Notification notification;

    private FlmBackupRestoreConsumer objectUnderTest;

    @BeforeClass
    public static void before() throws ActivitySchedulerException {
        final Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("sampleConsumer", "false", EMBEDDED_KAFKA_RULE.getEmbeddedKafka());
        final String bootstrapServer = consumerProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG).toString();
        System.setProperty("BRO_MESSAGING_HOST_URL", bootstrapServer);

        ACTIVITY_SCHEDULER = ActivityScheduler.getInstance();
    }

    @Before
    public void pre() throws KafkaConsumerInstantiationException {
        objectUnderTest = new FlmBackupRestoreConsumer(mockExecutionsController);
    }

    @Test
    public void whenProcessNotification_thenExistingActivitiesAreRemovedNewActivitiesAreScheduled() throws ActivitySchedulerException {
        when(notification.getAction()).thenReturn(ActionType.RESTORE);
        when(notification.getStatus()).thenReturn(NotificationStatus.COMPLETED);

        when(consumerRecord.value()).thenReturn(notification);

        final String flmExecutionOne = String.format(FLM_EXECUTION_ACTIVITY_ID, 1);
        ACTIVITY_SCHEDULER.addCronScheduleForActivity(getActivityWithName(flmExecutionOne), getCronScheduleWithName(flmExecutionOne));

        final String flmExecutionTwo = String.format(FLM_EXECUTION_ACTIVITY_ID, 2);
        ACTIVITY_SCHEDULER.addCronScheduleForActivity(getActivityWithName(flmExecutionTwo), getCronScheduleWithName(flmExecutionTwo));

        final String paExecutionOne = String.format(PA_EXECUTION_ACTIVITY_ID, 1, 1);
        ACTIVITY_SCHEDULER.addCronScheduleForActivity(getActivityWithName(paExecutionOne), getCronScheduleWithName(paExecutionOne));

        final String paExecutionTwo = String.format(PA_EXECUTION_ACTIVITY_ID, 2, 2);
        ACTIVITY_SCHEDULER.addCronScheduleForActivity(getActivityWithName(paExecutionTwo), getCronScheduleWithName(paExecutionTwo));

        assertThat(ACTIVITY_SCHEDULER.getAllJobNames()).contains(flmExecutionOne, flmExecutionTwo, paExecutionOne, paExecutionTwo);

        objectUnderTest.processNotification(consumerRecord);

        verify(mockExecutionsController, times(1)).resumeOrScheduleExecution();
        assertThat(ACTIVITY_SCHEDULER.getAllJobNames()).doesNotContain(flmExecutionOne, flmExecutionTwo, paExecutionOne, paExecutionTwo);
    }

    @Test
    public void whenProcessNotificationWithIrrelevantActionType_thenNothingHappens() {
        when(notification.getAction()).thenReturn(ActionType.HOUSEKEEPING);

        when(consumerRecord.value()).thenReturn(notification);

        objectUnderTest.processNotification(consumerRecord);
        verify(mockExecutionsController, never()).resumeOrScheduleExecution();

    }

    @Test
    public void whenProcessNotificationWithIrrelevantNotificationStatus_thenNothingHappens() {
        when(notification.getAction()).thenReturn(ActionType.RESTORE);
        when(notification.getStatus()).thenReturn(NotificationStatus.FAILED);

        when(consumerRecord.value()).thenReturn(notification);

        objectUnderTest.processNotification(consumerRecord);
        verify(mockExecutionsController, never()).resumeOrScheduleExecution();

    }

    public static Activity getActivityWithName(final String name) {
        return new DefaultActivity(name);
    }

    public static CronSchedule getCronScheduleWithName(final String name) throws ActivitySchedulerException {
        return new CronSchedule(name, DEFAULT_ACTIVITY_DATA, WEEKDAYS_AT_TWO_AM_SCHEDULE);
    }

    private static class DefaultActivity extends Activity {
        DefaultActivity(final String name) {
            super(name, new HashMap<>());
        }

        @Override
        public void run(final Map<String, Object> map) {
            // Nothing to do...
        }
    }

}
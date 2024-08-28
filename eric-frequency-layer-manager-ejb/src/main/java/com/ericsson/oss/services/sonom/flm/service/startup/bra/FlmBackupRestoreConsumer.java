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
package com.ericsson.oss.services.sonom.flm.service.startup.bra;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.bro.BackupRestoreConsumer;
import com.ericsson.oss.services.sonom.common.bro.notification.ActionType;
import com.ericsson.oss.services.sonom.common.bro.notification.Notification;
import com.ericsson.oss.services.sonom.common.bro.notification.NotificationStatus;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.common.scheduler.ActivityScheduler;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.flm.service.startup.executions.ExecutionsController;

/**
 * The {@code FlmBackupAndRestoreConsumer} is responsible for handling Backup and Restore notifications.
 */
public class FlmBackupRestoreConsumer extends BackupRestoreConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmBackupRestoreConsumer.class);

    private static final String CLIENT_ID = "FlmBackupAndRestoreConsumer";
    private static final String GROUP_ID = FlmBackupRestoreConsumer.class.getName();
    private static final String FLM_EXECUTION_SCHEDULE_PREFIX = "FLM_EXECUTION_SCHEDULE";
    private static final String FLM_EXECUTION_ACTIVITY_NAME = "FLM_EXECUTION_ACTIVITY_";
    private static final String PA_EXECUTION_SCHEDULE_PREFIX = "PA_EXECUTION_SCHEDULE";
    private static final String PA_EXECUTION_ACTIVITY_NAME = "PA_EXECUTION_ACTIVITY_";
    private static final String FLM_EXECUTION_ACTIVITY_ID = FLM_EXECUTION_ACTIVITY_NAME + FLM_EXECUTION_SCHEDULE_PREFIX;
    private static final String PA_EXECUTION_ACTIVITY_ID = PA_EXECUTION_ACTIVITY_NAME + PA_EXECUTION_SCHEDULE_PREFIX;

    private final ExecutionsController executionsController;

    public FlmBackupRestoreConsumer(final ExecutionsController executionsController) throws KafkaConsumerInstantiationException {
        super(CLIENT_ID, GROUP_ID);
        this.executionsController = executionsController;
    }

    @Override
    public void processNotification(final ConsumerRecord<String, Notification> consumerRecord) {
        final Notification broNotification = consumerRecord.value();

        if (ActionType.RESTORE == broNotification.getAction() && NotificationStatus.COMPLETED == broNotification.getStatus()) {
            LOGGER.info("FLM Restore process has completed. Scheduling the FLM configurations as in Restored DB.");
            removeCurrentFlmExecutionActivity();
            executionsController.resumeOrScheduleExecution();
        }
    }

    private void removeCurrentFlmExecutionActivity() {
        try {
            final ActivityScheduler activityScheduler = ActivityScheduler.getInstance();

            activityScheduler.getAllJobNames().stream()
                    .filter(this::filteringSchedulesToDelete)
                    .forEach(jobName -> {
                        LOGGER.info("Removing activity '{}'", jobName);
                        try {
                            activityScheduler.removeActivity(jobName);
                        } catch (final ActivitySchedulerException e) {
                            LOGGER.warn("Failed to remove activity '{}'", jobName, e);
                        }
                    });
        } catch (final ActivitySchedulerException e) {
            LOGGER.warn("Failed to get ActivityScheduler instance", e);
        }
    }

    private boolean filteringSchedulesToDelete(final String jobName) {

        return jobName.startsWith(FLM_EXECUTION_ACTIVITY_ID) || jobName.startsWith(PA_EXECUTION_ACTIVITY_ID);
    }
}
